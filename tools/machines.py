"""Reactions, fuels, machines, research stations, and their blocks and items. Called by generate.py.

Reads data/reactions.csv, data/fuels.csv and data/machines.csv, checks them against the
substances, compositions and research tables, and writes:
  periodic_elements/reactions.json, fuels.json, machines.json   runtime tables
  assets: blockstates, block and item models, 16x16 textures
  data: loot tables, crafting recipes, tags
"""
import csv
import json
import random
from pathlib import Path

from PIL import Image

import animations

# Research stations: id -> (level, name, description). Level N's station researches level N projects.
STATIONS = {
    "potters_workshop": (1, "Potter's and Smith's Workshop", "Research station for level 1, the Bronze and Iron Age"),
    "alchemists_laboratory": (2, "Alchemist's Laboratory", "Research station for level 2, Alchemy and early modern"),
    "natural_philosophers_laboratory": (3, "Natural Philosopher's Laboratory",
                                        "Research station for level 3, the Chemical Revolution"),
    "industrial_laboratory": (4, "Industrial Research Laboratory", "Research station for level 4, industrial chemistry"),
    "university_laboratory": (5, "University Research Institute", "Research station for level 5, the age of steel alloys and rare earths"),
    "national_laboratory": (6, "National Laboratory", "Research station for level 6: titanium, crystals, rare earths and the atom"),
    "international_research_centre": (7, "International Research Centre",
                                      "Research station for level 7: chips, superconductors and the last elements"),
}

# Machines powered by electricity from Voltaic Piles next to them instead of a fire.
ELECTRIC = {"electrolysis_cell", "hall_heroult_cell", "chlor_alkali_cell", "air_liquefier", "electrolytic_refinery",
            "moissan_cell", "electric_arc_furnace", "flotation_cell", "sintering_furnace", "van_arkel_reactor",
            "ion_exchange_column", "crystal_puller", "zone_refiner", "diamond_press", "enrichment_cascade", "hot_cell", "cyclotron", "calutron", "chip_fab",
            "heavy_ion_accelerator", "prilling_tower", "deep_tank_fermenter", "bioreactor", "air_compressor"}

# Machines that draw their charge from the air when the input is empty: id -> units per 1000 (one block of air).
# Real air by mass is 75.5% N, 23.1% O, 1.29% Ar; neon, krypton and xenon are boosted about a hundredfold for play.
# 3.36.0: helium too (real 0.7 ppm by mass), boosted about as much as neon: a flask every ~37 minutes.
INTAKE = {"air_compressor": {"compressed_air": 1000.0},
          "air_liquefier": {"nitrogen": 749.6, "oxygen": 231.0, "argon": 12.9, "neon": 3.0, "krypton": 2.0, "xenon": 1.0,
                            "helium": 0.5}}

# 3.36.0: the Helium Collector. Every alpha particle a radioactive atom gives off becomes a helium atom (Rutherford and
# Royds 1909); radium sealed in a tube fills it with helium (Ramsay and Soddy 1903), and this is where the Earth's
# helium comes from. Real alpha activity per gram of each element as the mod holds it (natural uranium and thorium
# without their daughters, radium with its short-lived daughters, the usual isotope of each synthetic element), in Bq/g:
ALPHA_BQ_PER_G = {
    "uranium": 2.5e4, "thorium": 4.1e3, "samarium": 1.24e2, "protactinium": 1.75e9, "neptunium": 2.6e7,
    "plutonium": 2.3e9, "radium": 1.48e11, "actinium": 1.3e13, "polonium": 1.66e14, "radon": 1.7e16,
    "americium": 1.27e11, "curium": 3.0e12, "californium": 2.0e13, "einsteinium": 9.3e14, "fermium": 3.7e14,
    "mendelevium": 7e14, "astatine": 3.2e16,
}
# Items whose isotope is far hotter than the element's usual one (plutonium-238 rather than -239).
ALPHA_ITEM_BQ_PER_G = {"periodic_elements:plutonium_238_pellet": 6.3e11}
# Real rates span twelve decades, so the game takes them to the power 0.4 (radium about 500 times uranium, not
# 6 million), and scales them so that a stack of 64 Raw Pitchblende fills one flask per Minecraft day.
ALPHA_EXPONENT = 0.4
HELIUM_CALIBRATION = ("periodic_elements:raw_pitchblende", 64, 24000)


def alpha_factor(bq):
    return (bq / ALPHA_BQ_PER_G["uranium"]) ** ALPHA_EXPONENT


# Power blocks: id -> (level, project, name, description, settings for the runtime).
#   voltaic_pile: zinc (kJ per item) dissolves into zinc oxide; an electrolyte item is used up every few zinc.
#   steam_engine: burns any fuel; while running, bellows machines next to it get a hotter blast.
POWER = {
    "voltaic_pile": (3, "voltaic_pile", "Voltaic Pile",
                     "Zinc and copper discs in brine: put zinc and an electrolyte in; powers machines next to it",
                     {"kind": "electric", "fuel": "periodic_elements:zinc", "fuelEnergy": 720.0,
                      "electrolytes": ["periodic_elements:sulfuric_acid", "periodic_elements:seawater_bucket",
                                       "periodic_elements:sodium_chloride"],
                      "zincPerElectrolyte": 4, "waste": "periodic_elements:zinc_oxide", "wasteUnits": 138.3,
                      "output": 2.0, "store": 800.0}),
    "steam_engine": (3, "steam_power", "Steam Engine",
                     "Burns fuel with a water bucket in it; blows furnaces with Bellows next to it 200 °C hotter",
                     {"kind": "shaft", "burn": 20.0, "waterTicks": 1200, "blastBonus": 200.0}),
    # Half of the engine's 20 kJ per tick reaches the wire (a real steam plant managed 10-15% of the coal's heat).
    "dynamo": (4, "dynamo", "Dynamo",
               "Put it next to a Steam Engine: powers electric machines next to it or joined to it by Copper Wire",
               {"kind": "dynamo", "efficiency": 0.5, "output": 12.0, "store": 2000.0}),
    # Game-scaled: a block of real cells in full sun gives about 0.15 kW; this one gives 1 kJ per tick by day.
    "solar_panel": (6, "solar_cell", "Solar Panel",
                    "Silicon cells: current by day under open sky; powers machines next to it or joined by Copper Wire",
                    {"kind": "solar", "gain": 1.0, "output": 4.0, "store": 1000.0}),
    # Game-scaled: a rod burns for three Minecraft days (an hour of play) at 30 kJ per tick, while water touches the
    # block. Every irradiateTicks while it runs, one item of each target stack in its slots captures neutrons.
    "nuclear_reactor": (6, "nuclear_reactor", "Nuclear Reactor",
                        "Burns uranium fuel rods while water touches it; targets in its slots turn into new elements",
                        {"kind": "reactor", "fuel": "periodic_elements:uranium_fuel_rod",
                         "naturalFuel": "periodic_elements:natural_uranium_fuel_rod",
                         "moderator": "periodic_elements:heavy_water", "controlRod": "periodic_elements:boron_control_rod",
                         "spent": "periodic_elements:spent_fuel_rod", "rodTicks": 72000, "gain": 30.0, "output": 30.0,
                         "store": 6000.0, "irradiateTicks": 6000,
                         "irradiation": {
                             "periodic_elements:depleted_uranium_ingot": "periodic_elements:plutonium",
                             "periodic_elements:uranium": "periodic_elements:plutonium",
                             "periodic_elements:plutonium": "periodic_elements:americium",
                             "periodic_elements:americium": "periodic_elements:curium",
                             "periodic_elements:californium_sample": "periodic_elements:einsteinium_sample",
                             "periodic_elements:einsteinium_sample": "periodic_elements:fermium_sample",
                             "periodic_elements:neptunium": "periodic_elements:plutonium_238_pellet"}}),
    # Game-scaled: each plutonium-238 pellet gives 0.25 kJ per tick for as long as you like (real: 88-year half-life).
    "radioisotope_generator": (6, "radioisotope_generator", "Radioisotope Generator",
                               "Thermocouples round plutonium-238 pellets: steady current with no fuel to feed",
                               {"kind": "rtg", "fuel": "periodic_elements:plutonium_238_pellet", "perPellet": 0.25,
                                "output": 4.0, "store": 1000.0}),
    # 3.36.0: needs no power and uses nothing up. "alpha" is each element's game weight per unit (alpha_factor);
    # the runtime scales the weights by the calibration item. Holds one flask of gas while it has no empty flask.
    "helium_collector": (4, "radiogenic_helium", "Helium Collector",
                         "A sealed lead-glass vessel: radioactive things kept in it slowly fill Glass Flasks with helium",
                         {"kind": "helium", "product": "periodic_elements:helium", "flask": "periodic_elements:glass_flask",
                          "hold": 1000.0 / 9,
                          "alpha": {el: round(alpha_factor(bq), 4) for el, bq in ALPHA_BQ_PER_G.items()},
                          "alphaItems": {i: round(alpha_factor(bq), 4) for i, bq in ALPHA_ITEM_BQ_PER_G.items()},
                          "calibration": {"item": HELIUM_CALIBRATION[0], "count": HELIUM_CALIBRATION[1],
                                          "ticks": HELIUM_CALIBRATION[2]}}),
    # Stores current from the sources it is wired to (not other batteries) and gives it back when they stop.
    "lithium_ion_battery": (7, "lithium_ion_battery", "Lithium-Ion Battery",
                            "Stores current from the dynamos, panels and reactors it is wired to, and gives it back when they stop",
                            {"kind": "battery", "chargeRate": 10.0, "output": 20.0, "store": 20000.0}),
    # A diesel engine on a generator: 60% of its 20 kJ per tick of oil reaches the wire, about twice what a steam
    # engine and dynamo give per item of fuel (real: 35-40% against 10-15%). Runs only while it has room to store.
    "diesel_generator": (4, "diesel_engine", "Diesel Generator",
                         "Burns diesel, biodiesel, kerosene, gasoline, naphtha or butanol into current: no boiler, no water",
                         {"kind": "generator", "fuels": ["periodic_elements:diesel", "periodic_elements:kerosene",
                                                         "periodic_elements:gasoline", "periodic_elements:naphtha",
                                                         "periodic_elements:biodiesel", "periodic_elements:butanol"],
                          "burn": 20.0, "efficiency": 0.6, "output": 12.0, "store": 4000.0}),
    # Game-scaled: up to 2 kJ per tick under open sky, more the higher it stands (full at y 128) and in storms.
    "wind_turbine": (7, "permanent_magnets", "Wind Turbine",
                     "A direct-drive generator with neodymium magnets: current from the wind, more the higher it stands",
                     {"kind": "wind", "gain": 2.0, "output": 6.0, "store": 2000.0, "seaLevel": 64, "fullHeight": 128,
                      "rainFactor": 1.25, "thunderFactor": 1.5}),
}

# Copper wire: carries current from piles and dynamos to electric machines (up to WIRE_REACH blocks of wire).
# Insulated cable counts as a quarter of a block of wire, so current goes four times as far through it.
WIRE_REACH = 64
CABLE_COST = 0.25

MACHINE_NAMES = {
    "charcoal_pit": "Charcoal Pit",
    "lime_kiln": "Lime Kiln",
    "smelting_furnace": "Smelting Furnace",
    "bloomery": "Bloomery",
    "crucible": "Crucible",
    "cupel": "Cupel Hearth",
    "alembic": "Alembic",
    "crystallizer": "Crystallizer",
    "charcoal_blast_furnace": "Charcoal Blast Furnace",
    "cementation_furnace": "Cementation Furnace",
    "zinc_retort": "Zinc Retort",
    "amalgamation_pan": "Amalgamation Pan",
    "phosphorus_retort": "Phosphorus Retort",
    "reverberatory_furnace": "Reverberatory Furnace",
    "pottery_kiln": "Pottery Kiln",
    "salt_pan": "Salt Pan",
    "desalination_still": "Seawater Still",
    "fermentation_barrel": "Fermentation Barrel",
    "vinegar_vat": "Vinegar Vat",
    "glass_furnace": "Glass Furnace",
    "porcelain_kiln": "Porcelain Kiln",
    "coke_oven": "Coke Oven",
    "huntsman_furnace": "Huntsman Crucible Furnace",
    "pneumatic_trough": "Pneumatic Trough",
    "electrolysis_cell": "Electrolysis Cell",
    "digestion_vat": "Digestion Vat",
    "reduction_crucible": "Reduction Crucible",
    "lead_chamber": "Lead Chamber",
    "bessemer_converter": "Bessemer Converter",
    "bayer_digester": "Bayer Digester",
    "hall_heroult_cell": "Hall-Heroult Cell",
    "chlor_alkali_cell": "Chlor-Alkali Cell",
    "solvay_tower": "Solvay Tower",
    "contact_converter": "Contact Converter",
    "air_liquefier": "Air Liquefier",
    "spectroscope_bench": "Spectroscope Bench",
    "electrolytic_refinery": "Electrolytic Refinery",
    "mond_reactor": "Mond Reactor",
    "moissan_cell": "Moissan Cell",
    "electric_arc_furnace": "Electric Arc Furnace",
    "high_pressure_reactor": "High-Pressure Reactor",
    "ostwald_converter": "Ostwald Converter",
    "flotation_cell": "Flotation Cell",
    "sintering_furnace": "Sintering Furnace",
    "fractional_crystallizer": "Fractional Crystallizer",
    "van_arkel_reactor": "Van Arkel Reactor",
    "chlorinator": "Chlorinator",
    "kroll_reactor": "Kroll Reactor",
    "ion_exchange_column": "Ion-Exchange Column",
    "crystal_puller": "Crystal Puller",
    "zone_refiner": "Zone Refiner",
    "diamond_press": "Diamond Press",
    "enrichment_cascade": "Enrichment Cascade",
    "hot_cell": "Hot Cell",
    "cyclotron": "Cyclotron",
    "calutron": "Calutron",
    "chip_fab": "Chip Fab",
    "heavy_ion_accelerator": "Heavy-Ion Accelerator",
    "refinery_still": "Oil Refinery Still",
    "cracking_unit": "Cracking Unit",
    "soap_kettle": "Soap Kettle",
    "oil_press": "Oil Press",
    "tanning_pit": "Tanning Pit",
    "vulcanizer": "Vulcanizer",
    "dye_vat": "Dye Vat",
    "tar_still": "Tar Still",
    "autoclave": "Autoclave",
    "prilling_tower": "Prilling Tower",
    "deep_tank_fermenter": "Deep-Tank Fermenter",
    "bioreactor": "Bioreactor",
    "air_compressor": "Air Compressor",
}

# Plain items: id -> (name, tooltip)
ITEMS = {
    "carbon_ink": ("Carbon Ink", "Soot and gum in water; writes treatises"),
    "slag": ("Slag", "Glassy waste of silicates and metal oxides"),
    "wood_ash": ("Wood Ash", "Potash and lime left from burnt wood"),
    "treatise_manuscript": ("Treatise (Manuscript)", "A leather-bound record of research"),
    "bronze_ingot": ("Bronze Ingot", "Copper with a tenth of tin; melted in a Crucible"),
    "brass_ingot": ("Brass Ingot", "Copper with zinc; melted in a Crucible"),
    "steel_ingot": ("Steel Ingot", "Iron with 1% carbon from the Cementation Furnace or the Bessemer Converter"),
    "cast_iron_ingot": ("Cast Iron Ingot", "Pig iron with 4% carbon from the Charcoal Blast Furnace"),
    "type_metal": ("Type Metal", "Lead, antimony and tin: casts sharp printing type"),
    "black_powder": ("Black Powder", "Saltpetre, charcoal and sulfur; fires the hand cannon"),
    "bittern": ("Bittern", "Magnesium salts left in the Salt Pan after the common salt"),
    "porcelain": ("Porcelain", "Hard white kaolin ware fired at 1300 °C"),
    "aqua_regia": ("Aqua Regia", "Nitric and hydrochloric acid: dissolves gold"),
    "iron_gall_ink": ("Iron-Gall Ink", "Permanent ink of green vitriol and oak tannin; writes treatises"),
    "treatise_printed": ("Treatise (Printed)", "A printed record of research; printing makes two copies"),
    "seawater_bucket": ("Bucket of Seawater", "Salt water from the sea, 3.5% salts: dry it in a Salt Pan, or boil it in a Seawater Still for fresh water"),
    "brine_bucket": ("Bucket of Brine", "Spring brine, 26% salts: a Salt Pan dries it to far more salt than seawater gives"),
    "leavened_bread": ("Leavened Bread", "Risen bread: restores more hunger than plain bread"),
    "magnifying_lens": ("Magnifying Lens", "Hold it to see the elements you know in the block you look at"),
    "coke": ("Coke", "Baked coal: burns at 1600 °C, hot enough to melt steel"),
    "crucible_steel_ingot": ("Crucible Steel Ingot", "Even steel melted in the Huntsman Crucible Furnace"),
    "chamber_sludge": ("Chamber Sludge", "Red mud from the Lead Chamber; holds selenium and thallium"),
    "manganese_steel_ingot": ("Manganese Steel Ingot", "Hadfield steel with 12% manganese from the Bessemer Converter"),
    "tungsten_steel_ingot": ("Tungsten Steel Ingot", "Mushet's tungsten tool steel from the Huntsman furnace"),
    "anode_slime": ("Anode Slime", "Mud from the Electrolytic Refinery: precious metals and tellurium"),
    "zinc_flue_dust": ("Zinc Flue Dust", "Dust from the Zinc Retort flue: indium and germanium"),
    "pitchblende_residue": ("Pitchblende Residue", "Radium, polonium and actinium from roasted pitchblende; dangerously radioactive"),
    "liquid_air": ("Liquid Air", "Air the Air Liquefier could not yet separate"),
    "smokeless_powder": ("Smokeless Powder", "Gelled guncotton: fires the breech-loading rifle"),
    "radon_seed": ("Radon Seed", "A gold tube sealed around a few millicuries of radon"),
    "electroscope": ("Gold-Leaf Electroscope", "Use it to read the radiation where you stand and the dose you carry"),
    "ferrochrome": ("Ferrochrome", "Chromium and iron from the Arc Furnace: makes stainless steel"),
    "molybdenite_concentrate": ("Molybdenite Concentrate", "Froth from the Flotation Cell: molybdenum and rhenium"),
    "molybdenite_flue_dust": ("Molybdenite Flue Dust", "Rhenium oxide caught when molybdenite is roasted"),
    "crude_zirconia": ("Crude Zirconia", "Zirconia from zircon, with 3% hafnia hidden in it"),
    "crude_zirconium": ("Crude Zirconium", "Brittle zirconium powder: purify it in a Van Arkel Reactor"),
    "crude_hafnium": ("Crude Hafnium", "Brittle hafnium powder: purify it in a Van Arkel Reactor"),
    "rare_earth_sulfates": ("Rare Earth Sulfates", "Cracked monazite: part it in a Fractional Crystallizer"),
    "francium_precipitate": ("Francium Precipitate", "Caesium salt carrying a few atoms of francium"),
    "stainless_steel_ingot": ("Stainless Steel Ingot", "Iron with 12.8% chromium from the Arc Furnace: never rusts"),
    "high_speed_steel_ingot": ("High-Speed Steel Ingot", "Tungsten-molybdenum tool steel from the Arc Furnace"),
    "cemented_carbide": ("Cemented Carbide", "Tungsten carbide in cobalt from the Sintering Furnace"),
    "beryllium_copper_ingot": ("Beryllium Copper Ingot", "Copper with 2% beryllium: hard, and it never sparks"),
    "geiger_counter": ("Geiger Counter", "Hold it to hear and read the radiation around you"),
    "heavy_rare_earth_chlorides": ("Heavy Rare Earth Chlorides", "Yttrium and the heavy rare earths: part them in an Ion-Exchange Column"),
    "aluminium_vanadium_master_alloy": ("Aluminium-Vanadium Master Alloy", "60% aluminium, 40% vanadium: alloys titanium"),
    "titanium_alloy_ingot": ("Titanium Alloy Ingot", "Ti-6Al-4V from the Arc Furnace: light and very strong"),
    "silicon_wafer": ("Silicon Wafer", "A slice of one perfect silicon crystal: makes solar cells"),
    "germanium_crystal": ("Germanium Crystal", "Zone-refined germanium, the first transistor material"),
    "synthetic_diamond": ("Synthetic Diamond", "Diamond grit from the Diamond Press: tips the best tools"),
    "zircaloy_ingot": ("Zircaloy Ingot", "Zirconium with 1.5% tin: fuel cladding that neutrons pass through"),
    "uranium_fuel_pellet": ("Uranium Fuel Pellet", "Sintered 3.5% enriched uranium dioxide: three make a fuel rod"),
    "uranium_fuel_rod": ("Uranium Fuel Rod", "Burns for three Minecraft days in a Nuclear Reactor"),
    "natural_uranium_fuel_rod": ("Natural Uranium Fuel Rod", "Burns in a Nuclear Reactor only with heavy water in its slots"),
    "boron_control_rod": ("Boron Control Rod", "In a reactor's slots: the reactor stops burning fuel while it is full"),
    "tritium_vial": ("Tritium Vial", "Hydrogen-3 from lithium-6 in the reactor: glows on zinc sulfide"),
    "spent_fuel_rod": ("Spent Fuel Rod", "Plutonium, actinides and fission products: open it in a Hot Cell"),
    "depleted_uranium_ingot": ("Depleted Uranium Ingot", "Uranium-238 left by enrichment: dense, and it burns as it strikes"),
    "high_level_waste": ("High-Level Waste", "Fission products and cladding from the Hot Cell: store it far away"),
    "astatine_sample": ("Astatine Sample", "A milligram of astatine-211 from the Cyclotron"),
    "berkelium_sample": ("Berkelium Sample", "A milligram of berkelium-249 from the Cyclotron"),
    "californium_sample": ("Californium Sample", "A milligram of californium-252: a fierce neutron source"),
    "einsteinium_sample": ("Einsteinium Sample", "A milligram of einsteinium-253, from californium in the Nuclear Reactor"),
    "fermium_sample": ("Fermium Sample", "A milligram of fermium-257, from einsteinium in the Nuclear Reactor"),
    "mendelevium_sample": ("Mendelevium Sample", "A milligram of mendelevium-258 from the Cyclotron"),
    "tungsten_carbide_arrow": ("Tungsten Carbide Arrow", "A hard penetrator: 50% more damage than an arrow"),
    "depleted_uranium_arrow": ("Depleted Uranium Arrow", "A dense penetrator: 75% more damage, and it sets the target alight"),
    "niobium_titanium_wire": ("Niobium-Titanium Wire", "Superconducting below 10 K: winds accelerator magnets"),
    "integrated_circuit": ("Integrated Circuit", "A whole circuit on one silicon chip, from the Chip Fab"),
    "gallium_arsenide_wafer": ("Gallium Arsenide Wafer", "Makes LEDs, lasers and the accelerator's fast detectors"),
    "iron_boron_alloy": ("Iron-Boron Alloy", "Iron with 1.4% boron: the iron and boron of a neodymium magnet"),
    "neodymium_magnet": ("Neodymium Magnet", "Nd2Fe14B, the strongest permanent magnet: drives wind turbines"),
    "optical_fibre": ("Optical Fibre", "Erbium-doped silica fibre: amplifies the light it carries"),
    "plutonium_238_pellet": ("Plutonium-238 Pellet", "Glows red with its own decay heat: fuel for a Radioisotope Generator"),
    "fibre_communicator": ("Fibre Communicator", "Use it to find every other player on the network: distance and direction"),
    "element_analyzer": ("Element Analyzer", "Use it on a block to read all of its elements, even ones you haven't found"),
    "parachute": ("Parachute", "Hold it in either hand while falling and it opens: you float down"),
    "field_guide": ("Field Guide", "Use it to read about every level, machine, reaction, ore and power block"),
    "composite_plate": ("Composite Plate", "Boron carbide on titanium alloy with aramid backing: makes composite armour"),
    "nobelium_record": ("Nobelium Detection Record", "The decay chains of a few nobelium atoms, seen once by the detectors"),
    "lawrencium_record": ("Lawrencium Detection Record", "The decay chains of a few lawrencium atoms, seen once by the detectors"),
    "rutherfordium_record": ("Rutherfordium Detection Record", "The decay chains of a few rutherfordium atoms, seen once by the detectors"),
    "dubnium_record": ("Dubnium Detection Record", "The decay chains of a few dubnium atoms, seen once by the detectors"),
    "seaborgium_record": ("Seaborgium Detection Record", "The decay chains of a few seaborgium atoms, seen once by the detectors"),
    "bohrium_record": ("Bohrium Detection Record", "The decay chains of a few bohrium atoms, seen once by the detectors"),
    "hassium_record": ("Hassium Detection Record", "The decay chains of a few hassium atoms, seen once by the detectors"),
    "meitnerium_record": ("Meitnerium Detection Record", "The decay chains of a few meitnerium atoms, seen once by the detectors"),
    "darmstadtium_record": ("Darmstadtium Detection Record", "The decay chains of a few darmstadtium atoms, seen once by the detectors"),
    "roentgenium_record": ("Roentgenium Detection Record", "The decay chains of a few roentgenium atoms, seen once by the detectors"),
    "copernicium_record": ("Copernicium Detection Record", "The decay chains of a few copernicium atoms, seen once by the detectors"),
    "nihonium_record": ("Nihonium Detection Record", "The decay chains of a few nihonium atoms, seen once by the detectors"),
    "flerovium_record": ("Flerovium Detection Record", "The decay chains of a few flerovium atoms, seen once by the detectors"),
    "moscovium_record": ("Moscovium Detection Record", "The decay chains of a few moscovium atoms, seen once by the detectors"),
    "livermorium_record": ("Livermorium Detection Record", "The decay chains of a few livermorium atoms, seen once by the detectors"),
    "tennessine_record": ("Tennessine Detection Record", "The decay chains of a few tennessine atoms, seen once by the detectors"),
    "oganesson_record": ("Oganesson Detection Record", "The decay chains of a few oganesson atoms, seen once by the detectors"),
    "glass_flask": ("Glass Flask", "Holds 111.1 units of a liquid or gas: any machine fills it from its flask slot"),
    "steel_cylinder": ("Steel Cylinder", "Holds 1000 units of compressed gas: only a machine with a Compressor fills it"),
    "compressor": ("Compressor", "Machine upgrade: the machine fills Steel Cylinders with the gases it makes"),
    "regenerator": ("Regenerator", "Machine upgrade: brick checkerwork reuses the flue heat; a fuel-fired machine loses half as much heat"),
    "refractory_lining": ("Refractory Lining", "Machine upgrade: a basic lime-brick lining; a fuel-fired machine can run 150 °C hotter"),
    "gas_recovery_hood": ("Gas Recovery Hood", "Machine upgrade: catches the gases the machine vents into flasks or cylinders"),
    "mechanical_stirrer": ("Mechanical Stirrer", "Machine upgrade: works through the charge 50% faster, at a quarter more fuel or current"),
    "catalyst_bed": ("Catalyst Bed", "Machine upgrade: reactions over a solid catalyst (iron, nickel, platinum, V2O5) run twice as fast"),
    "output_pusher": ("Output Pusher", "Machine upgrade: moves finished products into a chest, barrel or hopper next to the machine"),
    "protein_meal": ("Protein Meal", "Greaves and bone from rendering: feed for animals, or compost"),
    "oil_cake": ("Oil Cake", "Pressed seed left in the Oil Press: cattle and sheep eat it"),
    "spent_bark": ("Spent Tan Bark", "Bark from the Tanning Pit with its tannin gone: burns as fuel"),
    "sneakers": ("Sneakers", "Rubber soles: your steps make no vibrations, and magma and turtle eggs don't notice you"),
    "rubber_wheels": ("Rubber Wheels", "Use on a minecart: it runs 25% faster on rails"),
    "dynamite": ("Dynamite", "Nitroglycerin in diatomite: safe until lit; four make a Mining Charge"),
    "cordite_cartridge": ("Cordite Cartridge", "For the Breech-Loading Rifle: hits 25% harder and makes no smoke"),
    "carbolic_soap": ("Carbolic Soap", "Use to wash: Clean for ten minutes, and cures an early Infection"),
    "anaesthetic": ("Ether Anaesthetic", "Use on a zombie villager being cured: the cure takes half as long"),
    "bordeaux_mixture": ("Bordeaux Mixture", "Use on a crop or berry bush: it never washes out or wilts, and berries stop scratching"),
    "insect_spray": ("Insect Spray", "Use to spray pyrethrum 4 blocks ahead: hurts spiders, silverfish, endermites and bees"),
    "vacuum_pump": ("Vacuum Pump", "Machine upgrade: a still, kettle or crystallizer works 100 °C cooler"),
    "carbide_lamp": ("Carbide Lamp", "Light 14 held or worn on the head; use it with calcium carbide in your pack to refill"),
    "welding_torch": ("Welding Torch", "Hold a damaged metal tool or armour in the other hand and use: an ingot mends a quarter of it"),
    "pk_blend": ("PK Blend", "Superphosphate and potash: the Prilling Tower's reagent for NPK prills"),
    "npk_prills": ("NPK Prills", "Use on farmland: crops grow twice as fast and give one more, for 8 harvests"),
    "conveyor_belt": ("Conveyor Belt", "Machine upgrade: moves finished products four at a time into a chest, barrel or hopper"),
    "wetsuit_hood": ("Wetsuit Hood", "Neoprene: with the whole suit on you swim like a dolphin; keeps out the cold of powder snow"),
    "wetsuit_jacket": ("Wetsuit Jacket", "Neoprene: with the whole suit on you swim like a dolphin; keeps out the cold of powder snow"),
    "wetsuit_leggings": ("Wetsuit Leggings", "Neoprene: with the whole suit on you swim like a dolphin; keeps out the cold of powder snow"),
    "wetsuit_boots": ("Wetsuit Boots", "Neoprene: with the whole suit on you swim like a dolphin; keeps out the cold of powder snow"),
    "foam_lagging": ("Foam Lagging", "Machine upgrade: a machine that works below 300 °C loses 70% less heat"),
    "sterile_air_filter": ("Sterile Air Filter", "Machine upgrade: fermenters make 10% more; the Deep-Tank Fermenter needs one to run"),
    "spent_mycelium": ("Spent Mycelium", "Filtered mould from the Deep-Tank Fermenter: use on farmland as fertilizer"),
    "biomass": ("Biomass", "Spent microbes from the Bioreactor: use on farmland as fertilizer"),
    "ddt_duster": ("DDT Duster", "Use on the ground: for three days insects there take harm, and chickens lay half as often"),
    "weed_killer": ("Weed Killer", "Use on a block: kills flowers, ferns, vines and leaves around it; spares grass and crops"),
    "pre_emergent": ("Pre-Emergent", "Use on the ground: for ten days no grass or mycelium spreads onto it"),
    "field_clear": ("Field Clear", "Use on a block: kills every plant around it except crops from Tolerant Seeds"),
    "prussian_blue_capsule": ("Prussian Blue Capsule", "Swallow: for a day the dose from fission products drops by a quarter"),
    "mrna_vaccine": ("mRNA Vaccine", "Use: immune to Infection for seven game days"),
    "tolerant_seeds": ("Tolerant Seeds", "Plant on farmland: wheat that Field Clear spares and that grows a tenth faster"),
    # 2.1.0: mining tools (registered by com.periodicelements.mining.Mining; textures in tools/mining.py).
    "pneumatic_rock_drill": ("Pneumatic Rock Drill", "Runs on Compressed Air cylinders in your pack; needs a drill bit"),
    "rotary_hammer": ("Electric Rotary Hammer", "Runs on a battery pack in your pack; needs a drill bit"),
    "heavy_mining_drill": ("Heavy Mining Drill", "Battery powered; adds 5x5 and vein modes; needs a drill bit"),
    "chainsaw": ("Chainsaw", "Battery powered: fells a whole tree, up to 128 logs"),
    "tungsten_steel_bit": ("Tungsten Steel Drill Bit", "Mines up to diamond, speed 8.5"),
    "high_speed_steel_bit": ("High-Speed Steel Drill Bit", "Mines up to netherite, speed 10"),
    "cemented_carbide_bit": ("Cemented Carbide Drill Bit", "Mines up to netherite, speed 12"),
    "synthetic_diamond_bit": ("Diamond Drill Bit", "Mines up to netherite, speed 13"),
    "lead_acid_pack": ("Lead-Acid Battery Pack", "Holds 1,500 kJ for drills; fill it on a Charging Bench"),
    "li_ion_pack": ("Lithium-Ion Pack", "Holds 6,000 kJ for drills; the Heavy Mining Drill no longer slows you"),
}
# 2.7.0: the Field Atlas (registered by com.periodicelements.atlas.Atlas; textures in tools/atlas.py).
from atlas import ATLAS_ITEMS as _ATLAS_ITEMS, SHAPED as _ATLAS_SHAPED, SHAPELESS as _ATLAS_SHAPELESS, LANG as _ATLAS_LANG
ITEMS.update(_ATLAS_ITEMS)
# 3.1.0: the Tree Compendium (com.periodicelements.compendium; textures in tools/compendium.py).
from compendium import ITEMS as _COMP_ITEMS, SHAPELESS as _COMP_SHAPELESS, LANG as _COMP_LANG
ITEMS.update(_COMP_ITEMS)
# Items with their own classes in Java; every other item is a plain item registered by ModContent.
SPECIAL_ITEMS = {"treatise_manuscript", "treatise_printed", "seawater_bucket", "brine_bucket", "leavened_bread", "magnifying_lens", "electroscope",
                 "geiger_counter", "tungsten_carbide_arrow", "depleted_uranium_arrow",
                 "fibre_communicator", "element_analyzer", "parachute", "field_guide",
                 "glass_flask", "steel_cylinder", "sneakers", "rubber_wheels",
                 "insect_spray", "carbide_lamp", "welding_torch",
                 "wetsuit_hood", "wetsuit_jacket", "wetsuit_leggings", "wetsuit_boots",
                 "ddt_duster", "weed_killer", "pre_emergent", "field_clear", "tolerant_seeds",
                 "pneumatic_rock_drill", "rotary_hammer", "heavy_mining_drill", "chainsaw", "tungsten_steel_bit",
                 "high_speed_steel_bit", "cemented_carbide_bit", "synthetic_diamond_bit", "lead_acid_pack", "li_ion_pack"} | set(_ATLAS_ITEMS) | set(_COMP_ITEMS)
PLAIN_ITEMS = [i for i in ITEMS if i not in SPECIAL_ITEMS]

RECIPES = {
    "potters_workshop": {"pattern": ["BRB", "CTC", "SSS"],
                         "key": {"B": "minecraft:brick", "R": "minecraft:raw_copper", "C": "minecraft:charcoal",
                                 "T": "minecraft:crafting_table", "S": "minecraft:cobblestone"}},
    "alchemists_laboratory": {"pattern": ["GIG", "CBC", "KKK"],
                              "key": {"G": "minecraft:glass", "I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot",
                                      "B": "periodic_elements:potters_workshop", "K": "minecraft:bricks"}},
    "charcoal_pit": {"pattern": ["DDD", "DLD", "DDD"], "key": {"D": "minecraft:dirt", "L": "#minecraft:logs_that_burn"}},
    "lime_kiln": {"pattern": ["CCC", "C C", "BBB"], "key": {"C": "minecraft:cobblestone", "B": "minecraft:brick"}},
    "smelting_furnace": {"pattern": ["BKB", "B B", "BBB"], "key": {"B": "minecraft:brick", "K": "minecraft:clay_ball"}},
    "bloomery": {"pattern": ["KBK", "B B", "KLK"],
                 "key": {"K": "minecraft:clay", "B": "minecraft:brick", "L": "minecraft:leather"}},
    "crucible": {"pattern": ["K K", "K K", "BKB"], "key": {"K": "minecraft:clay_ball", "B": "minecraft:brick"}},
    "cupel": {"pattern": ["BMB", "BBB"], "key": {"B": "minecraft:brick", "M": "minecraft:bone_meal"}},
    "alembic": {"pattern": ["GGG", "GCG", "BBB"],
                "key": {"G": "minecraft:glass", "C": "minecraft:copper_ingot", "B": "minecraft:brick"}},
    "crystallizer": {"pattern": ["C C", "CWC", "BBB"],
                     "key": {"C": "minecraft:copper_ingot", "W": "minecraft:barrel", "B": "minecraft:brick"}},
    "charcoal_blast_furnace": {"pattern": ["KIK", "K K", "KFK"],
                               "key": {"K": "minecraft:bricks", "I": "minecraft:iron_ingot",
                                       "F": "periodic_elements:bloomery"}},
    "cementation_furnace": {"pattern": ["KKK", "KIK", "KKK"],
                            "key": {"K": "minecraft:bricks", "I": "minecraft:iron_ingot"}},
    "zinc_retort": {"pattern": ["KCK", "KFK", "KKK"],
                    "key": {"K": "minecraft:bricks", "C": "minecraft:clay_ball",
                            "F": "periodic_elements:smelting_furnace"}},
    "amalgamation_pan": {"pattern": ["I I", "SSS"], "key": {"I": "minecraft:iron_ingot", "S": "minecraft:smooth_stone"}},
    "phosphorus_retort": {"pattern": ["GCG", "K K", "KKK"],
                          "key": {"G": "minecraft:glass", "C": "minecraft:clay_ball", "K": "minecraft:bricks"}},
    "reverberatory_furnace": {"pattern": ["KIK", "KFK", "KKK"],
                              "key": {"K": "minecraft:bricks", "I": "minecraft:iron_ingot",
                                      "F": "periodic_elements:smelting_furnace"}},
    "pottery_kiln": {"pattern": ["BBB", "K K", "BBB"], "key": {"B": "minecraft:brick", "K": "minecraft:clay_ball"}},
    "salt_pan": {"pattern": ["BKB", "BBB"], "key": {"B": "minecraft:brick", "K": "minecraft:clay_ball"}},
    "desalination_still": {"pattern": ["CGC", "CUC", "BBB"],
                           "key": {"C": "minecraft:copper_ingot", "G": "minecraft:glass", "U": "minecraft:bucket", "B": "minecraft:brick"}},
    "fermentation_barrel": {"pattern": ["PKP", "PWP", "PPP"],
                            "key": {"P": "#minecraft:planks", "K": "minecraft:clay_ball", "W": "minecraft:barrel"}},
    "vinegar_vat": {"pattern": ["P P", "PWP", "PPP"], "key": {"P": "#minecraft:planks", "W": "minecraft:barrel"}},
    "glass_furnace": {"pattern": ["KSK", "KFK", "KKK"],
                      "key": {"K": "minecraft:bricks", "S": "minecraft:sand", "F": "periodic_elements:smelting_furnace"}},
    "porcelain_kiln": {"pattern": ["KQK", "KFK", "KKK"],
                       "key": {"K": "minecraft:bricks", "Q": "minecraft:quartz", "F": "periodic_elements:pottery_kiln"}},
    "natural_philosophers_laboratory": {"pattern": ["GBG", "SLS", "KKK"],
                                        "key": {"G": "minecraft:glass", "B": "periodic_elements:brass_ingot",
                                                "S": "periodic_elements:steel_ingot",
                                                "L": "periodic_elements:alchemists_laboratory", "K": "minecraft:bricks"}},
    "coke_oven": {"pattern": ["KKK", "K K", "KCK"], "key": {"K": "minecraft:bricks", "C": "periodic_elements:cast_iron_ingot"}},
    "huntsman_furnace": {"pattern": ["KCK", "KPK", "KFK"],
                         "key": {"K": "minecraft:bricks", "C": "periodic_elements:coke",
                                 "P": "periodic_elements:porcelain", "F": "periodic_elements:coke_oven"}},
    "pneumatic_trough": {"pattern": ["G G", "GBG", "LLL"],
                         "key": {"G": "minecraft:glass", "B": "minecraft:water_bucket", "L": "periodic_elements:lead"}},
    "electrolysis_cell": {"pattern": ["CPC", "IAI", "KKK"],
                          "key": {"C": "minecraft:copper_ingot", "P": "periodic_elements:platinum",
                                  "A": "periodic_elements:coke",
                                  "I": "minecraft:iron_ingot", "K": "minecraft:bricks"}},
    "digestion_vat": {"pattern": ["P P", "PLP", "PPP"], "key": {"P": "periodic_elements:porcelain", "L": "periodic_elements:lead"}},
    "reduction_crucible": {"pattern": ["I I", "IKI", "III"], "key": {"I": "minecraft:iron_ingot", "K": "periodic_elements:crucible"}},
    "lead_chamber": {"pattern": ["LLL", "L L", "LLL"], "key": {"L": "periodic_elements:lead"}},
    "voltaic_pile": {"pattern": ["ZCZ", "CPC", "ZCZ"],
                     "key": {"Z": "periodic_elements:zinc", "C": "minecraft:copper_ingot", "P": "minecraft:paper"}},
    "steam_engine": {"pattern": ["CCC", "BFB", "III"],
                     "key": {"C": "periodic_elements:cast_iron_ingot", "B": "periodic_elements:brass_ingot",
                             "F": "minecraft:furnace", "I": "minecraft:iron_ingot"}},
    "industrial_laboratory": {"pattern": ["GSG", "CLC", "TTT"],
                              "key": {"G": "minecraft:glass", "S": "periodic_elements:crucible_steel_ingot",
                                      "C": "minecraft:copper_ingot", "L": "periodic_elements:natural_philosophers_laboratory",
                                      "T": "periodic_elements:steel_ingot"}},
    "dynamo": {"pattern": ["WIW", "ICI", "WIW"],
               "key": {"W": "periodic_elements:copper_wire", "I": "minecraft:iron_ingot", "C": "periodic_elements:cast_iron_ingot"}},
    "copper_wire": {"pattern": ["CCC"], "key": {"C": "minecraft:copper_ingot"}, "count": 12},
    "bessemer_converter": {"pattern": ["S S", "SBS", "KKK"],
                           "key": {"S": "periodic_elements:steel_ingot", "B": "periodic_elements:charcoal_blast_furnace",
                                   "K": "minecraft:bricks"}},
    "bayer_digester": {"pattern": ["SSS", "SCS", "SSS"],
                       "key": {"S": "periodic_elements:steel_ingot", "C": "minecraft:cauldron"}},
    "hall_heroult_cell": {"pattern": ["CWC", "SKS", "SSS"],
                          "key": {"C": "periodic_elements:coke", "W": "periodic_elements:copper_wire",
                                  "S": "periodic_elements:steel_ingot", "K": "minecraft:bricks"}},
    "chlor_alkali_cell": {"pattern": ["WMW", "SGS", "SSS"],
                          "key": {"W": "periodic_elements:copper_wire", "M": "periodic_elements:mercury",
                                  "S": "periodic_elements:steel_ingot", "G": "minecraft:glass"}},
    "solvay_tower": {"pattern": ["III", "IBI", "III"], "key": {"I": "periodic_elements:cast_iron_ingot", "B": "minecraft:barrel"}},
    "contact_converter": {"pattern": ["SVS", "S S", "SVS"],
                          "key": {"S": "periodic_elements:steel_ingot", "V": "periodic_elements:vanadium_pentoxide"}},
    "air_liquefier": {"pattern": ["CWC", "SPS", "SSS"],
                      "key": {"C": "minecraft:copper_ingot", "W": "periodic_elements:copper_wire",
                              "S": "periodic_elements:crucible_steel_ingot", "P": "minecraft:piston"}},
    "spectroscope_bench": {"pattern": ["GLG", "BPB"],
                           "key": {"G": "minecraft:glass", "L": "periodic_elements:magnifying_lens",
                                   "B": "periodic_elements:brass_ingot", "P": "#minecraft:planks"}},
    "electrolytic_refinery": {"pattern": ["WLW", "L L", "LLL"],
                              "key": {"W": "periodic_elements:copper_wire", "L": "periodic_elements:lead"}},
    "mond_reactor": {"pattern": ["SCS", "S S", "SFS"],
                     "key": {"S": "periodic_elements:steel_ingot", "C": "minecraft:copper_ingot", "F": "minecraft:furnace"}},
    "moissan_cell": {"pattern": ["WPW", "PCP", "SSS"],
                     "key": {"W": "periodic_elements:copper_wire", "P": "periodic_elements:platinum",
                             "C": "periodic_elements:calcium_fluoride", "S": "periodic_elements:steel_ingot"}},
    "university_laboratory": {"pattern": ["GAG", "WLW", "SSS"],
                              "key": {"G": "minecraft:glass", "A": "periodic_elements:aluminium",
                                      "W": "periodic_elements:copper_wire", "L": "periodic_elements:industrial_laboratory",
                                      "S": "periodic_elements:steel_ingot"}},
    "electric_arc_furnace": {"pattern": ["CWC", "MBM", "MMM"],
                             "key": {"C": "periodic_elements:coke", "W": "periodic_elements:copper_wire",
                                     "M": "periodic_elements:magnesium_oxide", "B": "periodic_elements:bessemer_converter"}},
    "high_pressure_reactor": {"pattern": ["SSS", "SIS", "SSS"],
                              "key": {"S": "periodic_elements:manganese_steel_ingot", "I": "minecraft:iron_ingot"}},
    "ostwald_converter": {"pattern": ["SPS", "SRS", "SSS"],
                          "key": {"S": "periodic_elements:steel_ingot", "P": "periodic_elements:platinum",
                                  "R": "periodic_elements:rhodium"}},
    "flotation_cell": {"pattern": ["WPW", "S S", "SSS"],
                       "key": {"W": "periodic_elements:copper_wire", "P": "minecraft:piston", "S": "periodic_elements:steel_ingot"}},
    "sintering_furnace": {"pattern": ["WTW", "KFK", "SSS"],
                          "key": {"W": "periodic_elements:copper_wire", "T": "periodic_elements:tungsten",
                                  "K": "minecraft:bricks", "F": "minecraft:furnace", "S": "periodic_elements:steel_ingot"}},
    "fractional_crystallizer": {"pattern": ["PGP", "PCP", "KKK"],
                                "key": {"P": "periodic_elements:porcelain", "G": "minecraft:glass",
                                        "C": "periodic_elements:crystallizer", "K": "minecraft:bricks"}},
    "van_arkel_reactor": {"pattern": ["GTG", "GWG", "SSS"],
                          "key": {"G": "minecraft:glass", "T": "periodic_elements:tungsten",
                                  "W": "periodic_elements:copper_wire", "S": "periodic_elements:steel_ingot"}},
    "geiger_counter": {"pattern": ["GWG", "WEW", "BWB"],
                       "key": {"G": "minecraft:glass", "W": "periodic_elements:copper_wire",
                               "E": "periodic_elements:electroscope", "B": "periodic_elements:brass_ingot"}},
    "national_laboratory": {"pattern": ["GXG", "WUW", "SSS"],
                            "key": {"G": "minecraft:glass", "X": "periodic_elements:stainless_steel_ingot",
                                    "W": "periodic_elements:copper_wire", "U": "periodic_elements:university_laboratory",
                                    "S": "periodic_elements:stainless_steel_ingot"}},
    "chlorinator": {"pattern": ["KGK", "K K", "KFK"],
                    "key": {"K": "minecraft:bricks", "G": "minecraft:glass", "F": "periodic_elements:reverberatory_furnace"}},
    "kroll_reactor": {"pattern": ["SAS", "S S", "SFS"],
                      "key": {"S": "periodic_elements:stainless_steel_ingot", "A": "periodic_elements:argon",
                              "F": "minecraft:furnace"}},
    "ion_exchange_column": {"pattern": ["GWG", "G G", "GSG"],
                            "key": {"G": "minecraft:glass", "W": "periodic_elements:copper_wire",
                                    "S": "periodic_elements:stainless_steel_ingot"}},
    "crystal_puller": {"pattern": ["WAW", "SQS", "SSS"],
                       "key": {"W": "periodic_elements:copper_wire", "A": "periodic_elements:argon",
                               "Q": "minecraft:quartz_block", "S": "periodic_elements:stainless_steel_ingot"}},
    "zone_refiner": {"pattern": ["WWW", "QQQ", "SSS"],
                     "key": {"W": "periodic_elements:copper_wire", "Q": "minecraft:quartz",
                             "S": "periodic_elements:stainless_steel_ingot"}},
    "diamond_press": {"pattern": ["CWC", "SPS", "CSC"],
                      "key": {"C": "periodic_elements:cemented_carbide", "W": "periodic_elements:copper_wire",
                              "S": "periodic_elements:stainless_steel_ingot", "P": "minecraft:piston"}},
    "solar_panel": {"pattern": ["WWW", "GCG", "SSS"],
                    "key": {"W": "periodic_elements:silicon_wafer", "G": "minecraft:glass",
                            "C": "periodic_elements:copper_wire", "S": "periodic_elements:aluminium"}},
    "enrichment_cascade": {"pattern": ["NWN", "NSN", "NWN"],
                           "key": {"N": "periodic_elements:nickel", "W": "periodic_elements:copper_wire",
                                   "S": "periodic_elements:stainless_steel_ingot"}},
    "nuclear_reactor": {"pattern": ["SCS", "KZK", "SCS"],
                        "key": {"S": "periodic_elements:stainless_steel_ingot", "C": "periodic_elements:cadmium",
                                "K": "periodic_elements:coke", "Z": "periodic_elements:zircaloy_ingot"}},
    "hot_cell": {"pattern": ["LGL", "WPW", "LLL"],
                 "key": {"L": "periodic_elements:lead", "G": "minecraft:glass", "W": "periodic_elements:copper_wire",
                         "P": "minecraft:piston"}},
    "cyclotron": {"pattern": ["IWI", "CSC", "IWI"],
                  "key": {"I": "minecraft:iron_block", "W": "periodic_elements:copper_wire", "C": "minecraft:copper_block",
                          "S": "periodic_elements:stainless_steel_ingot"}},
    "tungsten_carbide_arrow": {"pattern": ["C", "S", "F"],
                               "key": {"C": "periodic_elements:cemented_carbide", "S": "minecraft:stick", "F": "minecraft:feather"},
                               "count": 8},
    "depleted_uranium_arrow": {"pattern": ["D", "S", "F"],
                               "key": {"D": "periodic_elements:depleted_uranium_ingot", "S": "minecraft:stick",
                                       "F": "minecraft:feather"}, "count": 8},
    "international_research_centre": {"pattern": ["GTG", "WNW", "TTT"],
                                      "key": {"G": "minecraft:glass", "T": "periodic_elements:titanium_alloy_ingot",
                                              "W": "periodic_elements:silicon_wafer",
                                              "N": "periodic_elements:national_laboratory"}},
    "calutron": {"pattern": ["IWI", "ISI", "IWI"],
                 "key": {"I": "minecraft:iron_block", "W": "periodic_elements:niobium_titanium_wire",
                         "S": "periodic_elements:stainless_steel_ingot"}},
    "chip_fab": {"pattern": ["GQG", "CFC", "SSS"],
                 "key": {"G": "minecraft:glass", "Q": "minecraft:quartz_block", "C": "periodic_elements:copper_wire",
                         "F": "periodic_elements:sintering_furnace", "S": "periodic_elements:stainless_steel_ingot"}},
    "heavy_ion_accelerator": {"pattern": ["NIN", "GCG", "NSN"],
                              "key": {"N": "periodic_elements:niobium_titanium_wire", "I": "periodic_elements:integrated_circuit",
                                      "G": "periodic_elements:gallium_arsenide_wafer", "C": "periodic_elements:cyclotron",
                                      "S": "periodic_elements:stainless_steel_ingot"}},
    "helium_collector": {"pattern": ["LGL", "GFG", "LLL"],
                         "key": {"L": "periodic_elements:lead", "G": "minecraft:glass", "F": "periodic_elements:glass_flask"}},
    "radioisotope_generator": {"pattern": ["LTL", "TPT", "LTL"],
                               "key": {"L": "periodic_elements:lead", "T": "periodic_elements:tellurium",
                                       "P": "periodic_elements:stainless_steel_ingot"}},
    # The polyethylene is the porous separator film between the electrodes.
    "lithium_ion_battery": {"pattern": ["ACA", "LWL", "APA"],
                            "key": {"A": "periodic_elements:aluminium", "C": "periodic_elements:coke",
                                    "P": "periodic_elements:polyethylene",
                                    "L": "periodic_elements:lithium_cobalt_oxide", "W": "periodic_elements:copper_wire"}},
    "smoke_detector": {"pattern": ["NAN", "RQR", "NNN"],
                       "key": {"N": "minecraft:iron_nugget", "A": "periodic_elements:americium_dioxide",
                               "R": "minecraft:redstone", "Q": "minecraft:quartz"}, "count": 16},
    "fibre_communicator": {"pattern": ["FGF", "FIF", "FLF"],
                           "key": {"F": "periodic_elements:optical_fibre", "G": "periodic_elements:gallium_arsenide_wafer",
                                   "I": "periodic_elements:integrated_circuit", "L": "periodic_elements:lithium_cobalt_oxide"}},
    "element_analyzer": {"pattern": ["TCT", "WIW", "TTT"],
                         "key": {"T": "periodic_elements:titanium_alloy_ingot", "C": "periodic_elements:curium_oxide",
                                 "W": "periodic_elements:silicon_wafer", "I": "periodic_elements:integrated_circuit"}},
    "refinery_still": {"pattern": ["SGS", "S S", "BFB"],
                       "key": {"S": "periodic_elements:steel_ingot", "G": "minecraft:glass", "B": "minecraft:bricks",
                               "F": "minecraft:furnace"}},
    "cracking_unit": {"pattern": ["SSS", "SRS", "BFB"],
                      "key": {"S": "periodic_elements:manganese_steel_ingot", "R": "periodic_elements:refinery_still",
                              "B": "minecraft:bricks", "F": "minecraft:blast_furnace"}},
    "diesel_generator": {"pattern": ["SPS", "WDW", "SSS"],
                         "key": {"S": "periodic_elements:steel_ingot", "P": "minecraft:piston",
                                 "W": "periodic_elements:copper_wire", "D": "periodic_elements:dynamo"}},
    "parachute": {"pattern": ["NNN", "N N", "S S"],
                  "key": {"N": "periodic_elements:nylon", "S": "minecraft:string"}},
    "wind_turbine": {"pattern": ["ASA", "WMW", " S "],
                     "key": {"A": "periodic_elements:aluminium", "S": "periodic_elements:stainless_steel_ingot",
                             "W": "periodic_elements:copper_wire", "M": "periodic_elements:neodymium_magnet"}},
    "electroscope": {"pattern": ["GCG", "GAG", "GGG"],
                     "key": {"G": "minecraft:glass_pane", "C": "minecraft:copper_ingot", "A": "minecraft:gold_nugget"}},
    # Five glass blow four flasks; a stoppered flask holds one item's worth (111.1 units) of a liquid or gas.
    "glass_flask": {"pattern": ["G G", "GGG"], "key": {"G": "minecraft:glass"}, "count": 4},
    # Seamless drawn steel with a brass valve: two cylinders.
    "steel_cylinder": {"pattern": [" B ", "S S", "SSS"],
                       "key": {"B": "periodic_elements:brass_ingot", "S": "periodic_elements:steel_ingot"}, "count": 2},
    "compressor": {"pattern": ["SPS", "C C", "SSS"],
                   "key": {"S": "periodic_elements:steel_ingot", "P": "minecraft:piston", "C": "minecraft:copper_ingot"}},
    "regenerator": {"pattern": ["KKK", "KCK", "KKK"],
                    "key": {"K": "minecraft:bricks", "C": "periodic_elements:cast_iron_ingot"}},
    "refractory_lining": {"pattern": ["BLB", "LBL", "BLB"],
                          "key": {"B": "minecraft:bricks", "L": "periodic_elements:calcium_oxide"}},
    "gas_recovery_hood": {"pattern": ["III", "G G", " F "],
                          "key": {"I": "minecraft:iron_ingot", "G": "minecraft:glass", "F": "periodic_elements:glass_flask"}},
    "mechanical_stirrer": {"pattern": ["WIW", " I ", "III"],
                           "key": {"W": "periodic_elements:copper_wire", "I": "minecraft:iron_ingot"}},
    "catalyst_bed": {"pattern": ["SPS", "NNN", "SSS"],
                     "key": {"S": "periodic_elements:steel_ingot", "P": "periodic_elements:platinum",
                             "N": "periodic_elements:nickel"}},
    "output_pusher": {"pattern": ["IPI", "IHI"],
                      "key": {"I": "minecraft:iron_ingot", "P": "minecraft:piston", "H": "minecraft:hopper"}},
    "storage_tank": {"pattern": ["ICI", "G G", "ICI"],
                     "key": {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot", "G": "minecraft:glass"}},
    "soap_kettle": {"pattern": ["C C", "CKC", "BBB"],
                    "key": {"C": "minecraft:copper_ingot", "K": "minecraft:cauldron", "B": "minecraft:brick"}},
    "oil_press": {"pattern": ["PSP", "P P", "LLL"],
                  "key": {"P": "#minecraft:planks", "S": "minecraft:stick", "L": "#minecraft:logs"}},
    "tanning_pit": {"pattern": ["P P", "PBP", "DDD"],
                    "key": {"P": "#minecraft:planks", "B": "minecraft:barrel", "D": "minecraft:dirt"}},
    "vulcanizer": {"pattern": ["III", "ICI", "BFB"],
                   "key": {"I": "minecraft:iron_ingot", "C": "minecraft:cauldron", "B": "minecraft:bricks",
                           "F": "minecraft:furnace"}},
    "sneakers": {"pattern": ["L L", "R R"],
                 "key": {"L": "minecraft:leather", "R": "periodic_elements:vulcanized_rubber"}},
    "rubber_wheels": {"pattern": ["R R", " I ", "R R"],
                      "key": {"R": "periodic_elements:vulcanized_rubber", "I": "minecraft:iron_ingot"}},
    "dye_vat": {"pattern": ["P P", "PCP", "BBB"],
                "key": {"P": "#minecraft:planks", "C": "minecraft:cauldron", "B": "minecraft:bricks"}},
    "tar_still": {"pattern": ["ICI", "I I", "BFB"],
                  "key": {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot", "B": "minecraft:bricks",
                          "F": "minecraft:furnace"}},
    "autoclave": {"pattern": ["SSS", "S S", "SFS"],
                  "key": {"S": "periodic_elements:steel_ingot", "F": "minecraft:furnace"}},
    "vacuum_pump": {"pattern": ["GIG", "IPI", "III"],
                    "key": {"G": "minecraft:glass", "I": "minecraft:iron_ingot", "P": "minecraft:piston"}},
    "prilling_tower": {"pattern": ["SGS", "S S", "SWS"],
                       "key": {"S": "periodic_elements:steel_ingot", "G": "minecraft:iron_bars", "W": "periodic_elements:copper_wire"}},
    "conveyor_belt": {"pattern": ["RRR", "IPI"],
                      "key": {"R": "periodic_elements:buna_rubber", "I": "minecraft:iron_ingot", "P": "periodic_elements:output_pusher"}},
    "carbide_lamp": {"pattern": [" G ", "BCB", " W "],
                     "key": {"G": "minecraft:glass_pane", "B": "periodic_elements:brass_ingot", "C": "periodic_elements:calcium_carbide",
                             "W": "minecraft:water_bucket"}},
    "wetsuit_hood": {"pattern": ["NNN", "N N"], "key": {"N": "periodic_elements:neoprene"}},
    "wetsuit_jacket": {"pattern": ["N N", "NNN", "NNN"], "key": {"N": "periodic_elements:neoprene"}},
    "wetsuit_leggings": {"pattern": ["NNN", "N N", "N N"], "key": {"N": "periodic_elements:neoprene"}},
    "wetsuit_boots": {"pattern": ["N N", "N N"], "key": {"N": "periodic_elements:neoprene"}},
    "deep_tank_fermenter": {"pattern": ["SGS", "SMS", "SWS"],
                            "key": {"S": "periodic_elements:steel_ingot", "G": "minecraft:glass",
                                    "M": "periodic_elements:mechanical_stirrer", "W": "periodic_elements:copper_wire"}},
    "bioreactor": {"pattern": ["GCG", "SMS", "SWS"],
                   "key": {"G": "minecraft:glass", "C": "periodic_elements:integrated_circuit",
                           "S": "periodic_elements:stainless_steel_ingot", "M": "periodic_elements:mechanical_stirrer",
                           "W": "periodic_elements:copper_wire"}},
    "foam_lagging": {"pattern": ["PPP", "PCP", "PPP"],
                     "key": {"P": "periodic_elements:polystyrene", "C": "periodic_elements:methane"}},
    "air_compressor": {"pattern": ["SPS", "PCP", "SWS"],
                       "key": {"S": "periodic_elements:steel_ingot", "P": "minecraft:piston",
                               "C": "periodic_elements:steel_cylinder", "W": "periodic_elements:copper_wire"}},
    "pneumatic_rock_drill": {"pattern": ["SPS", " T ", " T "],
                             "key": {"S": "periodic_elements:steel_ingot", "P": "minecraft:piston",
                                     "T": "periodic_elements:tungsten_steel_ingot"}},
    "rotary_hammer": {"pattern": ["HWH", " B ", " H "],
                      "key": {"H": "periodic_elements:high_speed_steel_ingot", "W": "periodic_elements:copper_wire",
                              "B": "periodic_elements:brown_bakelite"}},
    "chainsaw": {"pattern": ["SSS", "WBS"],
                 "key": {"S": "periodic_elements:steel_ingot", "W": "periodic_elements:copper_wire",
                         "B": "periodic_elements:brown_bakelite"}},
    "heavy_mining_drill": {"pattern": ["TMT", "TWT", " D "],
                           "key": {"T": "periodic_elements:titanium_alloy_ingot", "M": "periodic_elements:mechanical_stirrer",
                                   "W": "periodic_elements:copper_wire", "D": "periodic_elements:synthetic_diamond"}},
    **{f"{m}_bit": {"pattern": [" X ", "XSX"], "key": {"X": x, "S": "periodic_elements:steel_ingot"}}
       for m, x in (("tungsten_steel", "periodic_elements:tungsten_steel_ingot"),
                    ("high_speed_steel", "periodic_elements:high_speed_steel_ingot"),
                    ("cemented_carbide", "periodic_elements:cemented_carbide"),
                    ("synthetic_diamond", "periodic_elements:synthetic_diamond"))},
    "lead_acid_pack": {"pattern": ["LWL", "LAL", "LLL"],
                       "key": {"L": "periodic_elements:lead", "W": "periodic_elements:copper_wire",
                               "A": "periodic_elements:sulfuric_acid"}},
    "li_ion_pack": {"pattern": ["AWA", "LCL", "AAA"],
                    "key": {"A": "periodic_elements:aluminium", "W": "periodic_elements:copper_wire",
                            "L": "periodic_elements:lithium_cobalt_oxide", "C": "periodic_elements:integrated_circuit"}},
    "charging_bench": {"pattern": ["CWC", "SSS"],
                       "key": {"C": "minecraft:copper_ingot", "W": "periodic_elements:copper_wire",
                               "S": "periodic_elements:steel_ingot"}},
    "sterile_air_filter": {"pattern": ["SWS", "WGW", "SWS"],
                           "key": {"S": "periodic_elements:steel_ingot", "W": "minecraft:white_wool", "G": "minecraft:glass_pane"}},
    "magnifying_lens": {"pattern": ["CGC", " S "],
                        "key": {"C": "minecraft:copper_ingot", "G": "minecraft:glass_pane", "S": "minecraft:stick"}},
}
SHAPELESS = {
    "carbon_ink": (["minecraft:charcoal", "minecraft:sugar", "minecraft:glass_bottle"], 2),
    "field_guide": (["minecraft:book", "minecraft:charcoal"], 1),
    # 75:12.5:12.5 by mass: 6 saltpetre + 1 charcoal + 1 sulfur = 8 black powder (888.9 units either way).
    "black_powder": (["periodic_elements:potassium_nitrate"] * 6 + ["minecraft:charcoal", "periodic_elements:sulfur"], 8),
    # 1 nitric to 2 hydrochloric by mass (about 1:3 by moles).
    "aqua_regia": (["periodic_elements:nitric_acid"] + ["periodic_elements:hydrochloric_acid"] * 2, 3),
    "iron_gall_ink": (["periodic_elements:iron_sulfate", "#minecraft:oak_logs", "minecraft:glass_bottle"], 3),
    "leavened_bread": (["minecraft:wheat"] * 3 + ["minecraft:sugar", "minecraft:water_bucket"], 1),
    # 3 pellets (333.3 units) in 1 zircaloy tube (111.1 units) = one 444.4 unit rod.
    "uranium_fuel_rod": (["periodic_elements:uranium_fuel_pellet"] * 3 + ["periodic_elements:zircaloy_ingot"], 1),
    # Three natural uranium slugs (333.3 units) in a zircaloy tube: CANDU-style fuel.
    "natural_uranium_fuel_rod": (["periodic_elements:uranium"] * 3 + ["periodic_elements:zircaloy_ingot"], 1),
    # One vial lights four glass blocks lined with zinc sulfide.
    "tritium_light": (["periodic_elements:tritium_vial", "periodic_elements:zinc_sulfide"] + ["minecraft:glass"] * 4, 4),
    # One boron carbide tile, one titanium alloy ingot and one aramid (333.3 units) = one plate.
    "composite_plate": (["periodic_elements:boron_carbide", "periodic_elements:titanium_alloy_ingot", "periodic_elements:aramid"], 1),
    # About 5% bitumen by mass: 3 bitumen (333 units) with 3 gravel and 3 sand (6000 units) make 6 blocks.
    "asphalt": (["periodic_elements:bitumen"] * 3 + ["minecraft:gravel"] * 3 + ["minecraft:sand"] * 3, 6),
    # Six lengths of wire in one item of PVC or polyethylene.
    "insulated_cable": (["periodic_elements:copper_wire"] * 6 + ["#periodic_elements:cable_insulation"], 6),
    # An iron spout and a flask to catch the latex, on a stick driven into the bark.
    "tree_tap": (["minecraft:iron_ingot", "periodic_elements:glass_flask", "minecraft:stick"], 1),
    # Dipped candles: one item of tallow (111 g) on a length of wick makes two.
    "tallow_candle": (["periodic_elements:tallow", "minecraft:string"], 2),
    "rubber_planks": (["periodic_elements:rubber_log"], 4),
    # Like asphalt, about 5% binder: 3 pitch with 3 gravel and 3 sand make 6 blocks.
    "tarmac": (["periodic_elements:pitch"] * 3 + ["minecraft:gravel"] * 3 + ["minecraft:sand"] * 3, 6),
    # Three parts nitroglycerin to one of diatomite (sand stands in for kieselguhr), rolled in paper.
    "dynamite": (["periodic_elements:nitroglycerin", "minecraft:sand", "minecraft:paper"], 2),
    "mining_charge": (["periodic_elements:dynamite"] * 4 + ["minecraft:string"], 1),
    "cordite_cartridge": (["periodic_elements:cordite", "periodic_elements:lead", "periodic_elements:brass_ingot"], 4),
    "carbolic_soap": (["periodic_elements:phenol"] + ["periodic_elements:hard_soap"] * 4, 4),
    "anaesthetic": (["periodic_elements:diethyl_ether", "#minecraft:wool"], 1),
    "bordeaux_mixture": (["periodic_elements:copper_sulfate", "periodic_elements:calcium_oxide", "minecraft:water_bucket"], 4),
    "insect_spray": (["periodic_elements:pyrethrin"] * 3 + ["minecraft:iron_ingot", "periodic_elements:glass_flask"], 1),
    # 1.9.0: industrial organics.
    "welding_torch": (["periodic_elements:acetylene_cylinder", "periodic_elements:oxygen_cylinder",
                       "periodic_elements:steel_ingot", "minecraft:copper_ingot"], 1),
    "pk_blend": (["periodic_elements:superphosphate", "periodic_elements:potassium_chloride"], 2),
    "demolition_charge": (["periodic_elements:trinitrotoluene"] * 4 + ["periodic_elements:steel_ingot", "minecraft:string"], 1),
    "brown_bakelite": (["periodic_elements:bakelite"] * 4, 4),
    "black_bakelite": (["periodic_elements:bakelite"] * 4 + ["minecraft:black_dye"], 4),
    "red_bakelite": (["periodic_elements:bakelite"] * 4 + ["minecraft:red_dye"], 4),
    "green_bakelite": (["periodic_elements:bakelite"] * 4 + ["minecraft:green_dye"], 4),
    "ivory_bakelite": (["periodic_elements:bakelite"] * 4 + ["minecraft:white_dye"], 4),
    "amber_bakelite": (["periodic_elements:bakelite"] * 4 + ["minecraft:orange_dye"], 4),
    # 2.0.0: life sciences.
    "insulation": (["periodic_elements:polystyrene"] * 4 + ["periodic_elements:methane"], 4),
    "ddt_duster": (["periodic_elements:ddt"] * 3 + ["minecraft:iron_ingot", "periodic_elements:glass_flask"], 1),
    "weed_killer": (["periodic_elements:two_four_d"] * 3 + ["minecraft:iron_ingot", "periodic_elements:glass_flask"], 1),
    "pre_emergent": (["periodic_elements:atrazine"] * 3 + ["minecraft:iron_ingot", "periodic_elements:glass_flask"], 1),
    "field_clear": (["periodic_elements:glyphosate"] * 3 + ["minecraft:iron_ingot", "periodic_elements:glass_flask"], 1),
    # A copper cone over the RDX focuses the blast into a jet (the Munroe effect).
    "blasting_charge": (["periodic_elements:black_powder"] * 4 + ["minecraft:paper", "minecraft:string"], 2),
    "shaped_charge": (["periodic_elements:rdx"] * 2 + ["minecraft:copper_ingot", "periodic_elements:steel_ingot", "minecraft:string"], 1),
    # 94 parts prills to 6 of diesel, by mass.
    "anfo_charge": (["periodic_elements:ammonium_nitrate"] * 6 + ["periodic_elements:diesel", "minecraft:paper", "minecraft:string"], 1),
    "prussian_blue_capsule": (["periodic_elements:prussian_blue", "periodic_elements:protein_meal"], 4),
    "tolerant_seeds": (["minecraft:wheat_seeds"] * 8 + ["periodic_elements:glyphosate"], 8),
}
RECIPES.update(_ATLAS_SHAPED)
# 3.2.0: multiblock shell blocks (registered by com.periodicelements.multiblock.Multiblock; assets in tools/multiblock.py).
from multiblock import SHAPED as _MULTIBLOCK_SHAPED  # noqa: E402
RECIPES.update(_MULTIBLOCK_SHAPED)
# 3.38.0: tanks and pipes (registered by com.periodicelements.fluid.FluidContent; assets in tools/tanks.py).
from tanks import SHAPED as _TANK_SHAPED  # noqa: E402
RECIPES.update(_TANK_SHAPED)
SHAPELESS.update(_ATLAS_SHAPELESS)
SHAPELESS.update(_COMP_SHAPELESS)
EXTRA_SHAPELESS = {
    "gunpowder_from_black_powder": (["periodic_elements:black_powder"], "minecraft:gunpowder", 1),
    # CaO + H2O -> Ca(OH)2: 3 quicklime (333 units) take 107 units of the bucket's water and give 440 units.
    "lime_slaking": (["periodic_elements:calcium_oxide"] * 3 + ["minecraft:water_bucket"],
                     "periodic_elements:calcium_hydroxide", 4),
    # Hides soaked for months in water with crushed oak bark (tannin).
    "bark_tanning": (["minecraft:rotten_flesh"] * 2 + ["#minecraft:oak_logs", "minecraft:water_bucket"],
                     "minecraft:leather", 1),
    # One item of nylon (111 g) spins into a lot of thread.
    "nylon_string": (["periodic_elements:nylon"], "minecraft:string", 8),
    # Kerosene and liquid oxygen in a paper tube: a rocket with flight duration 3, like three gunpowder.
    "kerosene_rocket": (["periodic_elements:kerosene", "periodic_elements:oxygen", "minecraft:paper"],
                        "minecraft:firework_rocket", 3),
    # Tawing: rabbit skins soaked in alum and salt come out as soft white leather.
    "alum_tawing": (["minecraft:rabbit_hide"] * 2 + ["periodic_elements:potassium_alum"], "minecraft:leather", 1),
    # Soap washes the dye out of wool and beds.
    "wool_washing": (["#periodic_elements:soap", "#minecraft:wool"], "minecraft:white_wool", 1),
    "bed_washing": (["#periodic_elements:soap", "#minecraft:beds"], "minecraft:white_bed", 1),
    # Cast TNT in paper: one item of trinitrotoluene fills three blocks of TNT.
    "tnt_from_trinitrotoluene": (["periodic_elements:trinitrotoluene"] + ["minecraft:paper"] * 3, "minecraft:tnt", 3),
    "pla_flower_pot": (["periodic_elements:polylactic_acid"] * 2, "minecraft:flower_pot", 2),
    "pla_item_frame": (["periodic_elements:polylactic_acid"] * 2 + ["minecraft:stick"], "minecraft:item_frame", 2),
    # A spent torch takes fresh cylinders (the empties come back).
    "welding_torch_refill": (["periodic_elements:welding_torch", "periodic_elements:acetylene_cylinder",
                              "periodic_elements:oxygen_cylinder"], "periodic_elements:welding_torch", 1),
}
# Second shaped recipes for items that already have one: recipe id -> (pattern, key, result, count).
EXTRA_SHAPED = {
    "rubber_wheels_buna": (["R R", " I ", "R R"], {"R": "periodic_elements:buna_rubber", "I": "minecraft:iron_ingot"},
                           "periodic_elements:rubber_wheels", 1),
    "geiger_counter_bakelite": (["GWG", "WEW", "KWK"], {"G": "minecraft:glass", "W": "periodic_elements:copper_wire",
                                                        "E": "periodic_elements:electroscope", "K": "periodic_elements:bakelite"},
                                "periodic_elements:geiger_counter", 1),
}
# Data components on an EXTRA_SHAPELESS result.
RESULT_COMPONENTS = {"kerosene_rocket": {"minecraft:fireworks": {"flight_duration": 3, "explosions": []}}}

# The machines give vanilla ingots; these turn an ingot into the element item and back (1:1, same 111.1 units).
INGOT_ELEMENTS = {"copper": "minecraft:copper_ingot", "iron": "minecraft:iron_ingot", "gold": "minecraft:gold_ingot"}

DISABLED_SMELTING = {
    "iron": ["raw_iron", "iron_ore", "deepslate_iron_ore"],
    "copper": ["raw_copper", "copper_ore", "deepslate_copper_ore"],
    "gold": ["raw_gold", "gold_ore", "deepslate_gold_ore", "nether_gold_ore"],
}

MINEABLE = {"lead_chamber": "pickaxe", "charcoal_pit": "shovel", "potters_workshop": "axe", "fermentation_barrel": "axe", "vinegar_vat": "axe",
            "oil_press": "axe", "tanning_pit": "axe", "dye_vat": "axe"}

# Generic look for machines without a hand-made texture: wall style, wall colour, mortar, top colour, top centre.
LOOKS = {
    "crucible": ("noise", (172, 120, 90), None, (150, 100, 76), (240, 160, 60)),
    "cupel": ("bricks", (210, 205, 190), (150, 140, 125), (200, 195, 180), (235, 235, 240)),
    "alembic": ("bricks", (150, 82, 60), (110, 100, 90), (200, 120, 60), (190, 225, 235)),
    "crystallizer": ("planks", (150, 110, 70), (100, 72, 42), (200, 120, 60), (235, 240, 245)),
    "charcoal_blast_furnace": ("bricks", (140, 70, 52), (90, 80, 74), (60, 50, 46), (255, 150, 40)),
    "cementation_furnace": ("bricks", (130, 120, 110), (80, 74, 70), (110, 100, 92), (40, 34, 30)),
    "zinc_retort": ("bricks", (165, 150, 120), (110, 100, 90), (150, 140, 115), (200, 200, 210)),
    "amalgamation_pan": ("noise", (150, 150, 150), None, (175, 175, 180), (205, 210, 220)),
    "phosphorus_retort": ("bricks", (175, 140, 110), (110, 100, 90), (160, 130, 100), (120, 200, 120)),
    "reverberatory_furnace": ("bricks", (120, 60, 45), (80, 70, 65), (70, 60, 55), (30, 26, 24)),
    "pottery_kiln": ("bricks", (170, 92, 64), (120, 105, 92), (150, 84, 60), (60, 40, 32)),
    "salt_pan": ("noise", (176, 128, 96), None, (150, 190, 210), (240, 240, 236)),
    "desalination_still": ("bricks", (184, 112, 70), (120, 96, 80), (196, 120, 72), (150, 200, 230)),
    "fermentation_barrel": ("planks", (130, 96, 58), (86, 62, 36), (150, 112, 70), (200, 170, 90)),
    "vinegar_vat": ("planks", (120, 88, 52), (80, 58, 34), (140, 104, 64), (170, 110, 80)),
    "glass_furnace": ("bricks", (160, 110, 80), (110, 96, 86), (130, 100, 80), (255, 190, 90)),
    "porcelain_kiln": ("bricks", (190, 180, 170), (130, 122, 115), (170, 160, 150), (245, 245, 250)),
    "coke_oven": ("bricks", (110, 60, 46), (70, 62, 58), (60, 54, 50), (20, 18, 18)),
    "huntsman_furnace": ("bricks", (150, 76, 56), (96, 86, 80), (90, 80, 74), (255, 230, 160)),
    "pneumatic_trough": ("noise", (120, 124, 132), None, (90, 130, 190), (220, 236, 245)),
    "electrolysis_cell": ("bricks", (140, 120, 100), (90, 84, 78), (200, 120, 60), (240, 240, 120)),
    "digestion_vat": ("noise", (232, 232, 236), None, (210, 210, 214), (190, 220, 120)),
    "reduction_crucible": ("noise", (110, 110, 116), None, (90, 90, 96), (255, 140, 60)),
    "lead_chamber": ("planks", (112, 118, 132), (80, 84, 96), (100, 106, 120), (230, 230, 180)),
    "voltaic_pile": ("planks", (200, 120, 70), (150, 150, 160), (150, 150, 160), (200, 120, 70)),
    "steam_engine": ("noise", (70, 70, 76), None, (60, 60, 66), (200, 200, 210)),
    "dynamo": ("planks", (90, 90, 96), (200, 120, 70), (70, 70, 76), (200, 120, 70)),
    "bessemer_converter": ("noise", (96, 96, 104), None, (60, 56, 54), (255, 200, 90)),
    "bayer_digester": ("noise", (150, 156, 166), None, (130, 136, 146), (170, 70, 50)),
    "hall_heroult_cell": ("noise", (60, 60, 64), None, (50, 50, 54), (220, 222, 230)),
    "chlor_alkali_cell": ("planks", (70, 76, 86), (40, 44, 52), (60, 66, 76), (190, 210, 120)),
    "solvay_tower": ("planks", (88, 88, 94), (60, 60, 66), (80, 80, 86), (246, 246, 240)),
    "contact_converter": ("noise", (140, 146, 156), None, (120, 126, 136), (224, 144, 48)),
    "air_liquefier": ("planks", (170, 176, 186), (200, 120, 70), (150, 156, 166), (170, 210, 240)),
    "spectroscope_bench": ("planks", (150, 110, 70), (100, 72, 42), (200, 160, 80), (120, 80, 200)),
    "electrolytic_refinery": ("planks", (112, 118, 132), (80, 84, 96), (100, 106, 120), (220, 130, 80)),
    "mond_reactor": ("noise", (130, 136, 146), None, (110, 116, 126), (80, 130, 70)),
    "moissan_cell": ("noise", (210, 210, 216), None, (190, 190, 196), (190, 230, 90)),
    "electric_arc_furnace": ("bricks", (200, 196, 186), (120, 116, 110), (50, 50, 54), (220, 240, 255)),
    "high_pressure_reactor": ("noise", (110, 104, 118), None, (90, 86, 98), (200, 230, 255)),
    "ostwald_converter": ("noise", (150, 156, 166), None, (130, 136, 146), (230, 200, 150)),
    "flotation_cell": ("planks", (130, 120, 104), (90, 82, 70), (120, 124, 132), (200, 205, 215)),
    "sintering_furnace": ("bricks", (160, 150, 140), (100, 94, 88), (110, 114, 124), (255, 120, 60)),
    "fractional_crystallizer": ("planks", (220, 220, 226), (170, 170, 180), (200, 200, 206), (150, 220, 140)),
    "van_arkel_reactor": ("noise", (200, 225, 235), None, (150, 156, 166), (255, 250, 200)),
    "chlorinator": ("bricks", (170, 160, 120), (110, 104, 80), (120, 120, 90), (210, 230, 120)),
    "kroll_reactor": ("noise", (196, 202, 210), None, (170, 176, 186), (240, 240, 250)),
    "ion_exchange_column": ("planks", (210, 225, 230), (150, 160, 170), (196, 202, 210), (230, 200, 120)),
    "crystal_puller": ("noise", (186, 192, 200), None, (160, 166, 176), (120, 120, 140)),
    "zone_refiner": ("planks", (196, 202, 210), (200, 120, 70), (170, 176, 186), (255, 160, 80)),
    "diamond_press": ("noise", (80, 84, 92), None, (60, 64, 70), (230, 245, 255)),
    "solar_panel": ("planks", (40, 50, 110), (190, 196, 206), (30, 40, 100), (60, 80, 160)),
    "enrichment_cascade": ("planks", (176, 182, 190), (120, 126, 136), (150, 156, 166), (220, 230, 140)),
    "hot_cell": ("bricks", (112, 118, 132), (80, 84, 96), (100, 106, 120), (255, 214, 120)),
    "cyclotron": ("planks", (90, 90, 96), (200, 120, 70), (70, 70, 76), (150, 120, 255)),
    "nuclear_reactor": ("noise", (196, 196, 190), None, (40, 70, 140), (90, 200, 255)),
    "calutron": ("noise", (110, 116, 126), None, (90, 96, 106), (200, 120, 70)),
    "chip_fab": ("planks", (236, 236, 240), (200, 200, 210), (220, 220, 228), (120, 200, 120)),
    "heavy_ion_accelerator": ("planks", (60, 90, 160), (200, 206, 214), (40, 60, 120), (255, 120, 60)),
    "wind_turbine": ("planks", (236, 238, 240), (180, 184, 190), (220, 222, 226), (140, 146, 156)),
    "radioisotope_generator": ("planks", (90, 94, 100), (60, 62, 66), (80, 84, 90), (255, 90, 40)),
    "helium_collector": ("planks", (100, 104, 116), (200, 214, 222), (84, 88, 100), (250, 220, 120)),
    "lithium_ion_battery": ("planks", (40, 44, 52), (90, 160, 240), (30, 34, 40), (90, 160, 240)),
    "refinery_still": ("planks", (120, 124, 130), (70, 72, 76), (100, 104, 110), (60, 50, 30)),
    "cracking_unit": ("bricks", (150, 90, 60), (90, 60, 44), (110, 112, 118), (240, 200, 80)),
    "diesel_generator": ("planks", (200, 150, 40), (60, 62, 66), (90, 94, 100), (200, 150, 40)),
    "soap_kettle": ("bricks", (170, 92, 64), (120, 105, 92), (200, 120, 60), (236, 226, 196)),
    "oil_press": ("planks", (150, 110, 70), (100, 72, 42), (130, 96, 58), (230, 200, 90)),
    "tanning_pit": ("planks", (120, 88, 52), (80, 58, 34), (110, 80, 50), (120, 70, 40)),
    "vulcanizer": ("noise", (110, 112, 118), None, (90, 92, 98), (46, 42, 40)),
    "dye_vat": ("planks", (130, 96, 58), (86, 62, 36), (200, 120, 60), (160, 40, 120)),
    "tar_still": ("noise", (70, 66, 62), None, (50, 46, 42), (20, 18, 16)),
    "autoclave": ("noise", (160, 166, 176), None, (130, 136, 146), (220, 140, 60)),
    "prilling_tower": ("noise", (176, 180, 186), None, (150, 154, 160), (250, 250, 245)),
    "deep_tank_fermenter": ("noise", (186, 190, 196), None, (160, 164, 170), (230, 220, 120)),
    "bioreactor": ("noise", (210, 214, 220), None, (180, 184, 190), (120, 200, 140)),
    "air_compressor": ("planks", (90, 110, 90), (200, 200, 190), (70, 86, 70), (230, 230, 220)),
}


def _split(s):
    return [x for x in s.split(";") if x]


def _amounts(s):
    out = []
    for part in _split(s):
        ident, units = part.rsplit(":", 1)
        out.append((ident, float(units)))
    return out


def generate_machines(root, assets, data_dir, runtime_dir, lang, mod_id, substances, elements, compositions, research):
    sub_ids = {s["id"] for s in substances}
    el_ids = {e["id"] for e in elements}
    chem_ids = sub_ids | el_ids
    unlocked_by = {}
    for p in research.values():
        for u in p["unlocks"]:
            unlocked_by[u] = p["id"]

    machines = {}
    with (root / "data" / "machines.csv").open(newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            mid = r["id"]
            assert mid in MACHINE_NAMES, f"add a display name for machine {mid}"
            project = r["project"] or None
            if project:
                assert project in research, f"machine {mid}: unknown project {project}"
                assert unlocked_by.get(f"machine:{mid}") == project, f"machine {mid} must be unlocked by {project}"
            machines[mid] = {
                "id": mid, "level": int(r["level"]), "project": project, "maxC": float(r["max_c"]),
                "firePower": float(r["fire_kw"]), "loss": float(r["loss"]), "heatCapacity": float(r["heat_capacity"]),
                "throughput": int(r["throughput"]), "bellows": r["bellows"] == "yes", "residue": r["residue"],
                "electric": mid in ELECTRIC, "intake": INTAKE.get(mid, {}),
            }
            assert r["residue"] in ITEMS or r["residue"] in sub_ids, r["residue"]
            lang[f"block.{mod_id}.{mid}"] = MACHINE_NAMES[mid]
            lang[f"block.{mod_id}.{mid}.desc"] = r["description"]

    reactions = []
    with (root / "data" / "reactions.csv").open(newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            rid = r["id"]
            ms = _split(r["machines"])
            for m in ms:
                assert m in machines, f"reaction {rid}: unknown machine {m}"
            project = r["project"] or None
            if project:
                assert project in research, f"reaction {rid}: unknown project {project}"
                assert unlocked_by.get(f"reaction:{rid}") == project, f"reaction {rid} is not unlocked by {project}"
            assert r["feed"] in chem_ids, f"reaction {rid}: feed {r['feed']} is not a substance or element"
            products = []
            for ident, units in _amounts(r["products"].replace("@slag", "")):
                vented = ident.startswith("~")
                ident = ident.lstrip("~")
                to_slag = f"{ident}:{units:g}@slag" in r["products"] or any(
                    p.startswith(ident + ":") and p.endswith("@slag") for p in _split(r["products"]))
                if not vented:
                    if ident.startswith("item:"):
                        assert ident[5:] in compositions, f"reaction {rid}: {ident} needs a composition (its unit size)"
                    else:
                        assert ident in chem_ids, f"reaction {rid}: unknown product {ident}"
                products.append({"id": ident, "units": units, "vented": vented, "slag": to_slag})
            reagents = []
            for ident, units in _amounts(r.get("reagents") or ""):
                assert ident in chem_ids, f"reaction {rid}: unknown reagent {ident}"
                reagents.append({"id": ident, "units": units})
            inputs = 100 + float(r["carbon"] or 0) + sum(x["units"] for x in reagents)
            outputs = sum(p["units"] for p in products)
            assert outputs >= inputs - 0.6, f"reaction {rid}: products ({outputs:g}) weigh less than inputs ({inputs:g})"
            tmin, tmax = float(r["t_min"]), float(r["t_max"])
            assert tmin < tmax, rid
            for m in ms:
                assert tmin <= machines[m]["maxC"] + 0.01, f"{rid}: {m} can never reach {tmin} °C"
            reactions.append({
                "id": rid, "level": int(r["level"]), "machines": ms, "project": project, "feed": r["feed"],
                "products": products, "carbon": float(r["carbon"] or 0), "tMin": tmin, "tMax": tmax,
                "pressure": float(r["pressure_atm"]), "energy": float(r["energy_kj"]), "catalyst": r["catalyst"],
                "reagents": reagents,
            })
            lang[f"reaction.{mod_id}.{rid}"] = rid.replace("_", " ").capitalize().replace("Ndga", "NDGA").replace("Vitamin c", "Vitamin C").replace("Vitamin d", "Vitamin D").replace("Gfp", "GFP").replace("Lal", "LAL")
            lang[f"reaction.{mod_id}.{rid}.desc"] = r["description"]
    for m in machines.values():
        assert any(m["id"] in x["machines"] for x in reactions), f"machine {m['id']} has no reactions"

    fuels = []
    with (root / "data" / "fuels.csv").open(newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            fuels.append({"fuel": r["fuel"], "energy": float(r["energy_kj"]), "flame": float(r["flame_c"])})

    stations = [{"id": sid, "level": lvl} for sid, (lvl, _, _) in STATIONS.items()]
    power = []
    for pid, (lvl, project, name, desc, cfg) in POWER.items():
        assert project in research and unlocked_by.get(f"machine:{pid}") == project, f"power block {pid} must be unlocked by {project}"
        power.append(dict(cfg, id=pid, level=lvl, project=project))
        lang[f"block.{mod_id}.{pid}"] = name
        lang[f"block.{mod_id}.{pid}.desc"] = desc
    for sid, (lvl, name, desc) in STATIONS.items():
        assert any(p["level"] == lvl for p in research.values()), f"no projects for station {sid}"
        lang[f"block.{mod_id}.{sid}"] = name
        lang[f"block.{mod_id}.{sid}.desc"] = desc

    (runtime_dir / "reactions.json").write_text(json.dumps(reactions, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    (runtime_dir / "machines.json").write_text(json.dumps({"machines": list(machines.values()), "stations": stations,
                                                           "fuels": fuels, "plainItems": PLAIN_ITEMS,
                                                           "power": power, "wireReach": WIRE_REACH,
                                                           "cableCost": CABLE_COST}, indent=1) + "\n", encoding="utf-8")

    for iid, (name, tip) in ITEMS.items():
        lang[f"item.{mod_id}.{iid}"] = name
        lang[f"item.{mod_id}.{iid}.tip"] = tip

    write_assets(assets, data_dir, mod_id, list(machines) + list(POWER), list(STATIONS))
    return reactions, machines


# ---------------------------------------------------------------- textures

def _noise(base, var, seed, size=16):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for y in range(size):
        for x in range(size):
            d = rnd.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


def _bricks(base, mortar, seed):
    img = _noise(base, 12, seed)
    px = img.load()
    for y in range(16):
        for x in range(16):
            row = y // 4
            off = 0 if row % 2 == 0 else 4
            if y % 4 == 3 or (x + off) % 8 == 7:
                px[x, y] = mortar + (255,)
    return img


def _rect(img, x0, y0, x1, y1, color):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = color + (255,)


def _opening(img, lit, x0=4, y0=7, x1=11, y1=14):
    """A dark firebox opening; glowing when lit."""
    _rect(img, x0 - 1, y0 - 1, x1 + 1, y1, (40, 34, 30))
    if lit:
        rnd = random.Random(7)
        px = img.load()
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                t = (y - y0) / max(1, (y1 - y0))
                px[x, y] = (255, int(120 + 100 * t) + rnd.randint(-20, 20) // 2, int(30 * t), 255)
    else:
        _rect(img, x0, y0, x1, y1, (18, 16, 15))


def _ember_frame(img, x0, y0, x1, y1, phase, seed):
    """One frame of a smouldering opening: deep red coals with embers that brighten and fade with the phase."""
    import math
    rnd = random.Random(seed)
    px = img.load()
    _rect(img, x0 - 1, y0 - 1, x1 + 1, y1, (40, 34, 30))
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            t = (y - y0) / max(1, (y1 - y0))           # 0 at the top of the opening, 1 at the bed of coals
            off = rnd.random() * 2 * math.pi           # each pixel flickers on its own beat
            f = 0.5 + 0.5 * math.sin(phase + off)
            heat = 0.35 + 0.65 * t * (0.6 + 0.4 * f)
            r = int(90 + 165 * heat)
            g = int(20 + 150 * heat * heat * (0.7 + 0.3 * f))
            b = int(10 + 40 * heat ** 3)
            px[x, y] = (min(255, r), min(255, g), min(255, b), 255)
    # A few sparks that rise through the opening, one row per frame.
    for k in range(3):
        sx = x0 + (k * 2 + seed) % (x1 - x0 + 1)
        sy = y1 - int((phase / (2 * math.pi)) * (y1 - y0 + 2) + k * 2) % (y1 - y0 + 1)
        px[sx, sy] = (255, 236, 150, 255)
    # Warm light bleeding onto the earth around the opening.
    for x in range(x0 - 1, x1 + 2):
        c = px[x, y0 - 2]
        w = 0.25 + 0.15 * math.sin(phase)
        px[x, y0 - 2] = (min(255, int(c[0] + 120 * w)), min(255, int(c[1] + 40 * w)), c[2], 255)


# Machines whose "active" state (a reaction running) shows an animated texture: texture name ->
# (frame list, ticks per frame). Each strip is the 16x16 frames stacked top to bottom, with a .png.mcmeta.
ANIMATED = ("charcoal_pit", "pottery_kiln", "enrichment_cascade")
ANIM_FRAMES = 8


def _flame_frame(img, x0, y0, x1, y1, i, n):
    """One frame of an open wood fire: glowing coals along the bottom and flame tongues that rise and fall."""
    import math
    px = img.load()
    _rect(img, x0 - 1, y0 - 1, x1 + 1, y1, (40, 34, 30))
    _rect(img, x0, y0, x1, y1, (26, 18, 14))
    h = y1 - y0 + 1
    for x in range(x0, x1 + 1):
        c = x - x0
        # Each column's tongue height follows two waves out of step, so the fire never looks like it repeats in place.
        a = 2 * math.pi * i / n
        tall = 0.45 + 0.3 * math.sin(a + c * 1.7) + 0.2 * math.sin(2 * a - c * 0.9)
        top = y1 - int(round(max(0.2, min(1.0, tall)) * (h - 1)))
        for y in range(top, y1 + 1):
            d = (y - top) / max(1, y1 - top)       # 0 at the tip, 1 at the base
            if d < 0.25:
                col = (250, 120, 30)
            elif d < 0.6:
                col = (255, 175, 50)
            else:
                col = (255, 225, 120)
            px[x, y] = col + (255,)
    # Coals along the bottom row, pulsing.
    for x in range(x0, x1 + 1):
        k = 0.5 + 0.5 * math.sin(2 * math.pi * i / n * 2 + x)
        px[x, y1] = (int(200 + 55 * k), int(70 + 60 * k), 20, 255)
    # Firelight on the brick above the opening.
    w = 0.3 + 0.15 * math.sin(2 * math.pi * i / n * 3)
    for x in range(x0 - 1, x1 + 2):
        c = px[x, y0 - 2]
        px[x, y0 - 2] = (min(255, int(c[0] + 110 * w)), min(255, int(c[1] + 45 * w)), c[2], 255)


def _pottery_kiln_animations(base):
    import math
    fronts, tops = [], []
    for i in range(ANIM_FRAMES):
        f = base["side"].copy()
        _flame_frame(f, 5, 8, 10, 14, i, ANIM_FRAMES)
        fronts.append(f)
        # From above: the flue glows and hot air shimmers over it.
        t = base["top"].copy()
        px = t.load()
        phase = 2 * math.pi * i / ANIM_FRAMES
        for y in range(5, 11):
            for x in range(5, 11):
                edge = x in (5, 10) or y in (5, 10)
                k = (0.55 + 0.45 * math.sin(phase + (x + y) * 0.8)) * (0.6 if edge else 1.0)
                px[x, y] = (int(110 + 145 * k), int(40 + 120 * k * k), int(20 + 30 * k), 255)
        tops.append(t)
    return {"front_active": (fronts, 2), "top_active": (tops, 3)}


def machine_animations(mid):
    import math
    if mid == "pottery_kiln":
        return _pottery_kiln_animations(machine_textures(mid))
    if mid == "enrichment_cascade":
        return _cascade_animations(machine_textures(mid))
    if mid != "charcoal_pit":
        return {}
    base = machine_textures(mid)
    fronts, tops = [], []
    for i in range(ANIM_FRAMES):
        phase = 2 * math.pi * i / ANIM_FRAMES
        f = base["side"].copy()
        _ember_frame(f, 5, 9, 10, 14, phase, 11)
        fronts.append(f)
        t = base["top"].copy()
        px = t.load()
        glow = 0.5 + 0.5 * math.sin(phase)
        for y in range(6, 10):
            for x in range(6, 10):
                edge = x in (6, 9) or y in (6, 9)
                k = glow * (0.55 if edge else 1.0)
                px[x, y] = (int(120 + 135 * k), int(30 + 110 * k * k), int(15 + 25 * k), 255)
        # A wisp of smoke drifting off the vent, one pixel further each frame.
        for j in range(3):
            sx = (10 + i + j) % 16
            sy = (5 - j) % 16
            c = px[sx, sy]
            px[sx, sy] = tuple(int(c[n] * 0.4 + 150 * 0.6) for n in range(3)) + (255,)
        tops.append(t)
    return {"front_active": (fronts, 3), "top_active": (tops, 4)}


def save_animation(path, frames, frametime, interpolate=True):
    """Saves frames as a vertical strip plus the .png.mcmeta that tells the game to animate it."""
    w, h = frames[0].size
    strip = Image.new("RGBA", (w, h * len(frames)))
    for i, f in enumerate(frames):
        strip.paste(f, (0, i * h))
    strip.save(path)
    path.with_name(path.name + ".mcmeta").write_text(
        json.dumps({"animation": {"frametime": frametime, "interpolate": interpolate}}, indent=2) + "\n", encoding="utf-8")


def machine_textures(mid):
    """Returns dict of texture name -> image for a block id."""
    t = {}
    if mid == "charcoal_pit":
        side = _noise((121, 85, 58), 14, 1)
        _rect(side, 0, 0, 15, 2, (89, 125, 54))
        top = _noise((100, 72, 50), 14, 2)
        _rect(top, 6, 6, 9, 9, (30, 26, 24))
        front = side.copy()
        _opening(front, False, 5, 9, 10, 14)
        front_on = side.copy()
        _opening(front_on, True, 5, 9, 10, 14)
        t.update(side=side, top=top, front=front, front_on=front_on)
    elif mid == "lime_kiln":
        side = _bricks((128, 128, 128), (90, 90, 90), 3)
        top = _noise((110, 110, 110), 10, 4)
        _rect(top, 4, 4, 11, 11, (235, 232, 222))
        front = side.copy()
        _opening(front, False)
        front_on = side.copy()
        _opening(front_on, True)
        t.update(side=side, top=top, front=front, front_on=front_on)
    elif mid == "smelting_furnace":
        side = _bricks((150, 82, 60), (110, 100, 90), 5)
        top = _noise((140, 80, 60), 10, 6)
        _rect(top, 5, 5, 10, 10, (35, 25, 20))
        front = side.copy()
        _opening(front, False, 5, 8, 10, 14)
        front_on = side.copy()
        _opening(front_on, True, 5, 8, 10, 14)
        _rect(front_on, 7, 15, 8, 15, (230, 140, 60))
        t.update(side=side, top=top, front=front, front_on=front_on)
    elif mid == "bloomery":
        side = _noise((160, 120, 90), 14, 7)
        for y in (4, 9, 14):
            _rect(side, 0, y, 15, y, (120, 88, 66))
        top = _noise((150, 110, 85), 12, 8)
        _rect(top, 5, 5, 10, 10, (25, 20, 18))
        front = side.copy()
        _opening(front, False, 6, 10, 9, 14)
        _rect(front, 1, 12, 3, 13, (110, 75, 45))  # tuyere for the bellows
        front_on = side.copy()
        _opening(front_on, True, 6, 10, 9, 14)
        _rect(front_on, 1, 12, 3, 13, (110, 75, 45))
        t.update(side=side, top=top, front=front, front_on=front_on)
    elif mid == "potters_workshop":
        side = _noise((150, 115, 70), 10, 9)
        for y in (0, 15):
            _rect(side, 0, y, 15, y, (100, 72, 42))
        top = _noise((165, 128, 80), 8, 10)
        _rect(top, 4, 4, 11, 11, (140, 140, 140))  # potter's wheel
        _rect(top, 6, 6, 9, 9, (170, 110, 80))      # clay
        front = side.copy()
        _rect(front, 2, 3, 6, 7, (200, 120, 70))    # copper ingot
        _rect(front, 9, 3, 13, 8, (160, 85, 60))    # brick
        _rect(front, 3, 10, 12, 12, (90, 90, 95))   # hammer head
        _rect(front, 7, 12, 8, 14, (110, 80, 50))
        t.update(side=side, top=top, front=front)
    elif mid == "alchemists_laboratory":
        side = _bricks((120, 70, 55), (80, 70, 65), 11)
        top = _noise((90, 70, 55), 8, 12)
        _rect(top, 3, 3, 6, 6, (180, 220, 230))
        _rect(top, 9, 8, 12, 12, (90, 170, 80))
        front = _noise((105, 78, 55), 8, 13)
        _rect(front, 3, 2, 5, 9, (190, 225, 235))   # alembic neck
        _rect(front, 2, 9, 7, 13, (200, 230, 240))  # flask
        _rect(front, 3, 10, 6, 12, (120, 200, 90))  # liquid
        _rect(front, 9, 5, 13, 13, (200, 120, 60))  # copper still
        t.update(side=side, top=top, front=front)
    elif mid == "natural_philosophers_laboratory":
        side = _noise((112, 78, 50), 8, 14)          # polished wood cabinet
        for y in (0, 7, 15):
            _rect(side, 0, y, 15, y, (78, 52, 32))
        top = _noise((120, 86, 56), 8, 15)
        _rect(top, 2, 2, 6, 6, (200, 230, 240))      # bell jar
        _rect(top, 9, 9, 13, 13, (184, 115, 51))     # copper plates of a pile
        front = side.copy()
        _rect(front, 2, 1, 5, 6, (200, 230, 240))    # pneumatic jar
        _rect(front, 3, 3, 4, 5, (150, 200, 220))
        for i, y in enumerate(range(1, 7)):          # voltaic pile: zinc and copper discs
            _rect(front, 9, y, 13, y, (184, 115, 51) if i % 2 else (170, 175, 180))
        _rect(front, 2, 9, 13, 9, (60, 60, 64))      # balance beam
        _rect(front, 7, 9, 8, 14, (60, 60, 64))
        _rect(front, 2, 10, 4, 11, (210, 170, 70))   # pans
        _rect(front, 11, 10, 13, 11, (210, 170, 70))
        t.update(side=side, top=top, front=front)
    elif mid == "industrial_laboratory":
        side = _noise((150, 156, 166), 8, 16)        # riveted steel panels
        for y in (0, 15):
            _rect(side, 0, y, 15, y, (96, 100, 110))
        for x in (0, 15):
            _rect(side, x, 0, x, 15, (96, 100, 110))
        px = side.load()
        for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
            px[x, y] = (210, 214, 222, 255)
        top = side.copy()
        _rect(top, 4, 4, 11, 11, (184, 115, 51))     # coil of copper wire
        _rect(top, 6, 6, 9, 9, (90, 94, 102))
        front = side.copy()
        _rect(front, 2, 2, 7, 7, (235, 232, 220))    # gauge
        _rect(front, 4, 4, 5, 5, (40, 40, 44))
        _rect(front, 9, 2, 13, 7, (200, 230, 240))   # spectroscope prism window
        _rect(front, 10, 4, 12, 5, (150, 90, 200))
        for i, x in enumerate(range(2, 14, 2)):      # coloured spectral lines
            _rect(front, x, 10, x, 13, [(220, 60, 60), (240, 200, 60), (80, 200, 90), (70, 120, 230), (150, 80, 210), (230, 120, 50)][i])
        t.update(side=side, top=top, front=front)
    elif mid == "university_laboratory":
        side = _bricks((150, 64, 50), (200, 190, 170), 17)   # red-brick institute
        top = _noise((90, 96, 104), 8, 18)
        _rect(top, 3, 3, 12, 12, (200, 206, 214))            # stainless bench
        _rect(top, 6, 6, 9, 9, (220, 240, 255))              # arc glow
        front = side.copy()
        _rect(front, 2, 2, 13, 8, (40, 44, 52))              # blackboard
        for x, y in ((3, 3), (5, 4), (7, 3), (9, 5), (11, 4)):
            front.load()[x, y] = (235, 235, 235, 255)
        _rect(front, 3, 6, 12, 6, (235, 235, 235))
        _rect(front, 2, 10, 5, 14, (200, 230, 240))          # flask
        _rect(front, 3, 12, 4, 13, (150, 220, 140))
        _rect(front, 8, 10, 13, 14, (120, 116, 130))         # instrument cabinet
        _rect(front, 9, 11, 10, 12, (240, 200, 70))
        t.update(side=side, top=top, front=front)
    elif mid == "international_research_centre":
        side = _noise((60, 90, 160), 8, 31)                  # glass curtain wall
        for x in (4, 8, 12):
            _rect(side, x, 0, x, 15, (190, 196, 206))
        top = _noise((200, 202, 206), 6, 32)
        px = top.load()
        for y in range(16):
            for x in range(16):
                if 30 <= (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 42:
                    px[x, y] = (40, 60, 120, 255)            # the ring
        front = side.copy()
        _rect(front, 2, 3, 13, 10, (24, 28, 34))             # a wall of screens
        for i, x in enumerate(range(3, 13, 3)):
            _rect(front, x, 4, x + 1, 5, [(80, 220, 120), (90, 160, 240), (240, 200, 70), (230, 70, 60)][i])
            _rect(front, x, 7, x + 1, 8, (60, 140, 200))
        t.update(side=side, top=top, front=front)
    elif mid == "national_laboratory":
        side = _noise((210, 212, 206), 6, 19)                # concrete
        for y in (5, 10):
            _rect(side, 0, y, 15, y, (170, 172, 166))
        top = _noise((190, 192, 186), 6, 20)
        _rect(top, 3, 3, 12, 12, (60, 70, 90))                # control desk
        _rect(top, 5, 5, 6, 6, (80, 220, 120))
        _rect(top, 9, 9, 10, 10, (230, 70, 60))
        front = side.copy()
        _rect(front, 2, 2, 13, 7, (30, 34, 40))               # dials and lamps
        for i, x in enumerate(range(3, 13, 3)):
            _rect(front, x, 3, x + 1, 4, [(240, 200, 70), (80, 220, 120), (230, 70, 60), (90, 160, 240)][i])
        _rect(front, 3, 9, 12, 14, (240, 200, 40))            # radiation trefoil sign
        _rect(front, 7, 11, 8, 12, (20, 20, 20))
        for x, y in ((5, 10), (10, 10), (7, 13)):
            _rect(front, x, y, x + 1, y + 1, (20, 20, 20))
        t.update(side=side, top=top, front=front)
    if not t and mid in LOOKS:
        style, wall, mortar, top_c, centre = LOOKS[mid]
        seed = sum(map(ord, mid))
        if style == "bricks":
            side = _bricks(wall, mortar, seed)
        elif style == "planks":
            side = _noise(wall, 10, seed)
            for y in (3, 7, 11, 15):
                _rect(side, 0, y, 15, y, mortar)
        else:
            side = _noise(wall, 14, seed)
        top = _noise(top_c, 10, seed + 1)
        _rect(top, 5, 5, 10, 10, centre)
        front = side.copy()
        _opening(front, False, 5, 8, 10, 14)
        front_on = side.copy()
        _opening(front_on, True, 5, 8, 10, 14)
        t.update(side=side, top=top, front=front, front_on=front_on)
    if mid in ELECTRIC and mid != "enrichment_cascade" and "side" in t:
        # 3.33.0: an electric machine has no firebox; its front is an inspection window.
        w = animations.FRONT_WINDOW.get(mid, animations.DEFAULT_FRONT)
        t["front"] = animations.viewport(t["side"], w, False)
        t["front_on"] = animations.viewport(t["side"], w, True)
    if mid == "enrichment_cascade":
        # An electric plant has no firebox: its front is a control panel (3.28.2).
        t["front"] = _cascade_panel(t["side"], None)
        t["front_on"] = _cascade_panel(t["side"], -1)
    return t


def _cascade_panel(side, frame, n=8):
    """The enrichment cascade's control panel. frame None = switched off, -1 = powered but idle,
    0..n-1 = one frame of the running animation: stage lamps chase along, gas pulses down the pipe, the fan turns."""
    import math
    img = side.copy()
    px = img.load()
    _rect(img, 2, 3, 13, 14, (70, 74, 82))     # panel frame
    _rect(img, 3, 4, 12, 13, (38, 42, 48))     # panel face
    lamps = (4, 6, 8, 10, 12)
    for k, x in enumerate(lamps):
        if frame is None:
            col = (60, 52, 52)
        elif frame < 0:
            col = (150, 40, 36) if k == 0 else (60, 52, 52)   # red "standby" lamp
        else:
            d = (frame * len(lamps) // n - k) % len(lamps)
            col = (120, 255, 120) if d == 0 else (60, 160, 70) if d == 1 else (40, 80, 46)
        px[x - 1, 5] = col + (255,)
    # The gas line: a pipe across the panel, with a bright pulse of UF6 flowing left to right.
    _rect(img, 3, 8, 12, 9, (120, 126, 136))
    if frame is not None and frame >= 0:
        head = 3 + frame * 10 // n
        for x in range(3, 13):
            d = (head - x) % 10
            if d < 3:
                k = 1 - d / 3
                px[x, 8] = (int(150 + 80 * k), int(160 + 80 * k), int(110 + 30 * k), 255)
                px[x, 9] = (int(130 + 70 * k), int(140 + 70 * k), int(100 + 20 * k), 255)
    # Three bar meters (stage pressures) that rise and fall while the gas flows.
    _rect(img, 4, 10, 11, 13, (24, 26, 30))
    for k, x in enumerate((4, 7, 10)):
        if frame is None:
            h = 0
        elif frame < 0:
            h = 1
        else:
            h = 1 + int(round(1.5 + 1.5 * math.sin(2 * math.pi * frame / n * (1 + k % 2) + k * 2.1)))
        for j in range(h):
            col = (230, 200, 70) if j == 3 else (110, 210, 120)
            _rect(img, x, 13 - j, x + 1, 13 - j, col)
    return img


def _cascade_animations(base):
    import math
    fronts = [_cascade_panel(base["side"], i, ANIM_FRAMES) for i in range(ANIM_FRAMES)]
    tops = []
    for i in range(ANIM_FRAMES):
        # From above: the big axial compressor turning under its grille, with a ring of status light.
        t = base["top"].copy()
        px = t.load()
        a0 = 2 * math.pi * i / ANIM_FRAMES / 4   # 4 blades: a quarter turn repeats
        c = 7.5
        for y in range(3, 13):
            for x in range(3, 13):
                dx, dy = x - c, y - c
                d = math.hypot(dx, dy)
                if d > 5:
                    continue
                if d > 4.2:
                    k = 0.5 + 0.5 * math.sin(2 * math.pi * i / ANIM_FRAMES + math.atan2(dy, dx))
                    px[x, y] = (int(90 + 100 * k), int(170 + 70 * k), int(80 + 40 * k), 255)
                elif d < 1.1:
                    px[x, y] = (90, 94, 100, 255)
                else:
                    ang = math.atan2(dy, dx) - a0 - d * 0.25   # swept blades
                    px[x, y] = ((196, 202, 210) if math.cos(4 * ang) > 0.2 else (40, 44, 50)) + (255,)
        tops.append(t)
    return {"front_active": (fronts, 3, False), "top_active": (tops, 1, False)}


def _ingot(img, color):
    dark = tuple(max(0, c - 50) for c in color)
    light = tuple(min(255, c + 45) for c in color)
    for y in range(6, 12):
        x0 = 3 + (11 - y) // 2
        x1 = 12 - (y - 6) // 3
        _rect(img, x0, y, x1, y, color)
    _rect(img, 5, 6, 10, 6, light)
    _rect(img, 2, 11, 13, 11, dark)


INGOT_COLORS = {"bronze_ingot": (196, 132, 64), "brass_ingot": (222, 186, 82), "steel_ingot": (170, 176, 186),
                "cast_iron_ingot": (92, 92, 98), "crucible_steel_ingot": (150, 166, 190),
                "manganese_steel_ingot": (128, 120, 136), "tungsten_steel_ingot": (104, 112, 124),
                "stainless_steel_ingot": (200, 206, 214), "high_speed_steel_ingot": (120, 116, 130),
                "beryllium_copper_ingot": (214, 120, 86), "ferrochrome": (140, 146, 156),
                "titanium_alloy_ingot": (170, 176, 190), "aluminium_vanadium_master_alloy": (190, 196, 200),
                "zircaloy_ingot": (180, 184, 176), "depleted_uranium_ingot": (70, 76, 70),
                "iron_boron_alloy": (120, 124, 130)}


POWDERS = {"anode_slime": (70, 62, 52), "zinc_flue_dust": (196, 192, 170), "pitchblende_residue": (150, 160, 120),
           "smokeless_powder": (200, 180, 120), "molybdenite_concentrate": (110, 118, 132),
           "molybdenite_flue_dust": (226, 220, 150), "crude_zirconia": (236, 232, 222), "crude_zirconium": (120, 122, 126),
           "crude_hafnium": (104, 106, 112), "rare_earth_sulfates": (230, 220, 210), "francium_precipitate": (240, 240, 236),
           "heavy_rare_earth_chlorides": (232, 228, 212), "synthetic_diamond": (220, 236, 245),
           "high_level_waste": (80, 84, 70)}

# Sealed quartz ampoules holding a milligram of a heavy element: glow colour of each.
SAMPLES = {"astatine_sample": (120, 90, 80), "berkelium_sample": (150, 100, 230), "californium_sample": (170, 70, 220),
           "einsteinium_sample": (90, 160, 255), "fermium_sample": (190, 60, 200), "mendelevium_sample": (180, 40, 170)}
RECORDS = {f"{e}_record" for e in ("nobelium", "lawrencium", "rutherfordium", "dubnium", "seaborgium", "bohrium",
                                     "hassium", "meitnerium", "darmstadtium", "roentgenium", "copernicium", "nihonium",
                                     "flerovium", "moscovium", "livermorium", "tennessine", "oganesson")}
ARROWS = {"tungsten_carbide_arrow": (80, 84, 92), "depleted_uranium_arrow": (70, 76, 70)}


def item_texture(iid):
    from farm import FARM_ITEMS, farm_item_texture
    from industry import INDUSTRY_ITEMS, industry_item_texture
    from mining import MINING_ITEMS, mining_item_texture
    from atlas import ATLAS_ITEMS, atlas_item_texture
    if iid in ATLAS_ITEMS:
        return atlas_item_texture(iid)
    if iid in _COMP_ITEMS:
        from compendium import item_texture as _comp_texture
        return _comp_texture(iid)
    if iid in MINING_ITEMS:
        return mining_item_texture(iid)
    if iid in INDUSTRY_ITEMS:
        return industry_item_texture(iid)
    if iid in FARM_ITEMS:
        return farm_item_texture(iid)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    if iid == "glass_flask":
        from substances import _gas_flask
        _gas_flask(img.load(), (225, 238, 245))
    elif iid == "steel_cylinder":
        from substances import cylinder_texture
        img = cylinder_texture((150, 154, 160))
    elif iid == "compressor":
        _rect(img, 2, 8, 13, 14, (110, 114, 120))     # crankcase
        _rect(img, 3, 9, 12, 13, (140, 144, 150))
        _rect(img, 5, 2, 10, 7, (150, 154, 160))      # cylinder head with fins
        for y in (3, 5):
            _rect(img, 4, y, 11, y, (90, 94, 100))
        _rect(img, 12, 4, 14, 5, (200, 140, 60))      # brass outlet
        _rect(img, 4, 11, 6, 11, (60, 60, 64))
    elif iid == "regenerator":
        for y in range(2, 15):
            for x in range(2, 14):
                brick = (150, 70, 50) if ((x + (y // 3) * 2) % 4 and y % 3) else (200, 190, 175)
                _rect(img, x, y, x, y, brick)
        _rect(img, 6, 6, 9, 10, (60, 50, 46))
    elif iid == "refractory_lining":
        for y in range(2, 15):
            _rect(img, 2, y, 13, y, (232, 228, 214) if y % 4 else (170, 160, 140))
        for y in range(2, 15, 4):
            for x in range(2 + (y // 4 % 2) * 3, 14, 6):
                _rect(img, x, y, x, min(14, y + 3), (170, 160, 140))
    elif iid == "gas_recovery_hood":
        for y in range(3, 10):
            half = 2 + (y - 3)
            _rect(img, 8 - half, y, 7 + half, y, (150, 154, 160) if y < 9 else (90, 94, 100))
        _rect(img, 6, 1, 9, 3, (120, 124, 130))
        _rect(img, 6, 11, 9, 14, (200, 225, 240))
        _rect(img, 7, 10, 8, 10, (150, 105, 60))
    elif iid == "mechanical_stirrer":
        _rect(img, 5, 1, 10, 5, (190, 120, 60))      # motor
        _rect(img, 6, 2, 9, 4, (220, 150, 80))
        _rect(img, 7, 6, 8, 12, (150, 154, 160))     # shaft
        _rect(img, 3, 12, 12, 13, (120, 124, 130))   # paddles
        _rect(img, 3, 11, 4, 14, (120, 124, 130))
        _rect(img, 11, 11, 12, 14, (120, 124, 130))
    elif iid == "catalyst_bed":
        _rect(img, 2, 3, 13, 14, (110, 114, 120))
        _rect(img, 3, 4, 12, 13, (60, 62, 66))
        rnd = random.Random(31)
        for _ in range(26):
            x, y = rnd.randint(3, 12), rnd.randint(4, 13)
            _rect(img, x, y, x, y, rnd.choice([(200, 200, 205), (170, 170, 120), (140, 150, 160)]))
    elif iid == "output_pusher":
        _rect(img, 2, 5, 13, 12, (120, 124, 130))
        _rect(img, 3, 6, 9, 11, (160, 164, 170))
        _rect(img, 10, 7, 13, 10, (150, 110, 70))    # piston head
        _rect(img, 13, 6, 14, 11, (200, 160, 100))
    elif iid == "carbon_ink":
        _rect(img, 6, 2, 9, 4, (150, 110, 70))      # cork
        _rect(img, 5, 5, 10, 6, (200, 220, 230))
        _rect(img, 4, 7, 11, 14, (200, 220, 230))
        _rect(img, 5, 8, 10, 13, (25, 25, 30))
    elif iid == "slag":
        rnd = random.Random(21)
        px = img.load()
        for y in range(5, 14):
            for x in range(3, 13):
                if (x - 7.5) ** 2 / 25 + (y - 9.5) ** 2 / 16 <= 1:
                    v = rnd.randint(-15, 15)
                    px[x, y] = (60 + v, 62 + v, 70 + v, 255) if rnd.random() > 0.15 else (110, 70, 50, 255)
    elif iid == "wood_ash":
        rnd = random.Random(22)
        px = img.load()
        for y in range(7, 14):
            for x in range(2, 14):
                if (x - 7.5) ** 2 / 36 + (y - 13) ** 2 / 36 <= 1:
                    v = rnd.randint(-20, 20)
                    px[x, y] = (175 + v, 172 + v, 165 + v, 255)
    elif iid == "treatise_manuscript":
        _rect(img, 2, 2, 13, 14, (110, 66, 36))     # leather cover
        _rect(img, 3, 3, 12, 13, (135, 84, 48))
        _rect(img, 12, 3, 13, 13, (235, 225, 200))  # page edges
        _rect(img, 5, 5, 10, 5, (210, 170, 70))     # gilt lines
        _rect(img, 5, 7, 10, 7, (210, 170, 70))
        _rect(img, 2, 2, 3, 14, (80, 46, 24))       # spine
    elif iid in INGOT_COLORS:
        _ingot(img, INGOT_COLORS[iid])
    elif iid == "type_metal":
        for i, (x, y) in enumerate(((3, 4), (8, 4), (3, 9), (8, 9))):
            _rect(img, x, y, x + 4, y + 4, (120, 124, 132))
            _rect(img, x + 1, y + 1, x + 3, y + 1, (175, 180, 188))
            _rect(img, x + 2, y + 2, x + 2, y + 3, (60, 62, 68))
    elif iid == "black_powder":
        rnd = random.Random(23)
        px = img.load()
        for y in range(7, 14):
            for x in range(2, 14):
                if (x - 7.5) ** 2 / 36 + (y - 13) ** 2 / 36 <= 1:
                    v = rnd.randint(-12, 12)
                    px[x, y] = (40 + v, 40 + v, 44 + v, 255)
    elif iid == "coke":
        rnd = random.Random(25)
        px = img.load()
        for y in range(4, 14):
            for x in range(3, 13):
                if (x - 7.5) ** 2 / 25 + (y - 9) ** 2 / 25 <= 1:
                    v = rnd.randint(-14, 14)
                    px[x, y] = (70 + v, 72 + v, 78 + v, 255) if rnd.random() > 0.25 else (30, 30, 34, 255)
    elif iid == "chamber_sludge":
        rnd = random.Random(26)
        px = img.load()
        for y in range(7, 14):
            for x in range(2, 14):
                if (x - 7.5) ** 2 / 36 + (y - 13) ** 2 / 36 <= 1:
                    v = rnd.randint(-15, 15)
                    px[x, y] = (160 + v, 50 + v // 2, 40 + v // 2, 255)
    elif iid in POWDERS:
        base = POWDERS[iid]
        rnd = random.Random(len(iid) * 7)
        px = img.load()
        for y in range(7, 14):
            for x in range(2, 14):
                if (x - 7.5) ** 2 / 36 + (y - 13) ** 2 / 36 <= 1:
                    v = rnd.randint(-15, 15)
                    px[x, y] = tuple(max(0, min(255, c + v)) for c in base) + (255,)
        if iid == "pitchblende_residue":
            for x, y in ((5, 11), (9, 10), (11, 12)):
                px[x, y] = (120, 255, 140, 255)
    elif iid in SAMPLES:
        _rect(img, 6, 2, 9, 3, (220, 226, 232))
        _rect(img, 5, 4, 10, 13, (220, 230, 236))
        _rect(img, 6, 5, 9, 12, (240, 246, 250))
        _rect(img, 7, 10, 8, 11, SAMPLES[iid])
        _rect(img, 6, 13, 9, 14, (200, 206, 212))
    elif iid in RECORDS:
        rnd = random.Random(len(iid))
        _rect(img, 3, 1, 12, 14, (246, 244, 236))            # a printout with a decay chain plotted on it
        _rect(img, 3, 1, 3, 14, (210, 206, 196))
        y = 3
        for x in range(5, 11):
            y = min(12, y + rnd.randint(0, 2))
            _rect(img, x, y, x, y, (200, 40, 40))
        for yy in (4, 7, 10, 13):
            _rect(img, 5, yy, 10, yy, (190, 196, 210))
    elif iid == "niobium_titanium_wire":
        for i in range(12):
            _rect(img, 2 + i, 7 + (i % 4 < 2), 2 + i, 8 + (i % 4 < 2), (150, 156, 170))
        _rect(img, 2, 5, 13, 5, (200, 120, 70))
        _rect(img, 2, 11, 13, 11, (200, 120, 70))
    elif iid == "integrated_circuit":
        _rect(img, 4, 4, 11, 11, (40, 40, 44))
        for k in range(5, 11, 2):
            _rect(img, 2, k, 3, k, (200, 200, 206))
            _rect(img, 12, k, 13, k, (200, 200, 206))
        _rect(img, 6, 6, 9, 9, (70, 76, 110))
    elif iid == "gallium_arsenide_wafer":
        px = img.load()
        for y in range(16):
            for x in range(16):
                if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 42:
                    px[x, y] = (110, 110, 120, 255) if (x * y) % 5 else (170, 170, 190, 255)
    elif iid == "optical_fibre":
        for i in range(12):
            _rect(img, 2 + i, 12 - i, 2 + i, 12 - i, (200, 230, 240))
        _rect(img, 13, 1, 14, 2, (255, 120, 160))
        _rect(img, 1, 13, 2, 14, (150, 170, 180))
    elif iid == "plutonium_238_pellet":
        _rect(img, 5, 5, 10, 11, (190, 60, 30))
        _rect(img, 6, 6, 9, 10, (255, 110, 50))
        _rect(img, 7, 7, 8, 8, (255, 200, 120))
    elif iid == "fibre_communicator":
        _rect(img, 4, 2, 11, 14, (40, 44, 52))
        _rect(img, 5, 3, 10, 9, (90, 160, 240))
        _rect(img, 5, 11, 10, 12, (120, 124, 132))
        _rect(img, 11, 0, 11, 2, (200, 230, 240))
    elif iid == "field_guide":
        _rect(img, 3, 2, 12, 14, (40, 90, 60))
        _rect(img, 4, 3, 11, 13, (60, 130, 85))
        _rect(img, 12, 3, 13, 13, (236, 230, 210))
        _rect(img, 6, 6, 9, 9, (230, 200, 90))
    elif iid == "parachute":
        for y in range(2, 8):
            half = min(7, 2 + y)
            _rect(img, 8 - half, y, 7 + half, y, (230, 90, 40) if (y % 2) else (245, 240, 230))
        for x0 in (2, 7, 13):
            for y in range(8, 13):
                img.load()[min(15, max(0, x0 + (7 - x0) * (y - 8) // 5)), y] = (200, 200, 190, 255)
        _rect(img, 6, 13, 9, 15, (110, 80, 50))
    elif iid == "element_analyzer":
        _rect(img, 3, 4, 12, 12, (170, 176, 190))
        _rect(img, 4, 5, 9, 9, (30, 60, 40))
        _rect(img, 5, 6, 8, 6, (90, 220, 120))
        _rect(img, 5, 8, 7, 8, (90, 220, 120))
        _rect(img, 12, 7, 14, 9, (120, 80, 200))
    elif iid == "neodymium_magnet":
        _rect(img, 3, 5, 12, 11, (150, 156, 166))
        _rect(img, 3, 5, 7, 11, (200, 60, 60))
        _rect(img, 4, 6, 11, 6, (210, 214, 222))
    elif iid == "composite_plate":
        _rect(img, 2, 3, 13, 12, (232, 200, 64))
        _rect(img, 3, 4, 12, 10, (48, 52, 58))
        for x in (5, 9):
            _rect(img, x, 5, x + 1, 9, (70, 76, 84))
    elif iid in ARROWS:
        tip = ARROWS[iid]
        for i in range(10):
            _rect(img, 3 + i, 12 - i, 3 + i, 12 - i, (120, 90, 50))
        _rect(img, 12, 2, 13, 3, tip)
        _rect(img, 11, 2, 11, 2, tip)
        _rect(img, 13, 4, 13, 4, tip)
        _rect(img, 2, 12, 3, 13, (230, 230, 230))
        _rect(img, 1, 11, 2, 11, (230, 230, 230))
        _rect(img, 4, 14, 4, 14, (230, 230, 230))
    elif iid == "uranium_fuel_pellet":
        for x0 in (3, 9):
            _rect(img, x0, 6, x0 + 3, 11, (46, 46, 42))
            _rect(img, x0, 6, x0 + 3, 6, (90, 90, 84))
    elif iid == "boron_control_rod":
        _rect(img, 7, 1, 8, 14, (60, 62, 70))
        _rect(img, 6, 1, 9, 2, (170, 176, 186))
    elif iid == "tritium_vial":
        _rect(img, 6, 3, 9, 13, (200, 230, 210))
        _rect(img, 7, 5, 8, 12, (140, 255, 160))
        _rect(img, 6, 2, 9, 3, (150, 150, 150))
    elif iid in ("uranium_fuel_rod", "spent_fuel_rod", "natural_uranium_fuel_rod"):
        for i in range(12):
            _rect(img, 2 + i, 13 - i, 3 + i, 13 - i, (180, 184, 176))
            _rect(img, 3 + i, 12 - i, 3 + i, 12 - i, (215, 218, 210))
        if iid == "spent_fuel_rod":
            for x, y in ((5, 10), (8, 7), (11, 4)):
                _rect(img, x, y, x, y, (90, 200, 255))
    elif iid == "liquid_air":
        _bottle(img, (170, 210, 240))
    elif iid == "radon_seed":
        _rect(img, 4, 7, 11, 8, (230, 190, 60))
        _rect(img, 5, 6, 10, 6, (250, 220, 120))
        _rect(img, 4, 9, 11, 9, (170, 130, 30))
    elif iid == "cemented_carbide":
        for x0 in (3, 9):
            _rect(img, x0, 5, x0 + 4, 11, (80, 84, 92))
            _rect(img, x0 + 1, 6, x0 + 3, 6, (150, 156, 168))
            _rect(img, x0, 11, x0 + 4, 11, (50, 52, 58))
    elif iid == "silicon_wafer":
        px = img.load()
        for y in range(16):
            for x in range(16):
                if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 42:
                    px[x, y] = (70, 76, 110, 255) if (x + y) % 4 else (150, 160, 200, 255)
        _rect(img, 6, 13, 9, 13, (0, 0, 0))
    elif iid == "germanium_crystal":
        for y in range(2, 15):
            w = 2 if y < 4 else 3
            _rect(img, 8 - w, y, 7 + w, y, (150, 156, 166) if y % 3 else (200, 206, 216))
    elif iid == "geiger_counter":
        _rect(img, 3, 6, 12, 14, (150, 110, 60))
        _rect(img, 4, 7, 11, 10, (235, 232, 220))
        _rect(img, 7, 8, 9, 8, (30, 30, 34))
        _rect(img, 5, 12, 6, 13, (40, 40, 44))
        _rect(img, 9, 12, 10, 13, (200, 40, 40))
        _rect(img, 11, 1, 12, 5, (200, 210, 220))
        _rect(img, 12, 5, 12, 6, (200, 120, 70))
    elif iid == "electroscope":
        _rect(img, 4, 4, 11, 14, (200, 225, 235))
        _rect(img, 5, 5, 10, 13, (40, 44, 52))
        _rect(img, 7, 1, 8, 7, (200, 120, 70))
        _rect(img, 6, 8, 6, 11, (240, 200, 70))
        _rect(img, 9, 8, 9, 11, (240, 200, 70))
    elif iid == "bittern":
        _bottle(img, (196, 214, 200))
    elif iid == "aqua_regia":
        _bottle(img, (232, 160, 60))
    elif iid == "iron_gall_ink":
        _rect(img, 6, 2, 9, 4, (150, 110, 70))
        _rect(img, 5, 5, 10, 6, (200, 220, 230))
        _rect(img, 4, 7, 11, 14, (200, 220, 230))
        _rect(img, 5, 8, 10, 13, (34, 30, 56))
    elif iid == "porcelain":
        for y in range(4, 14):   # a white bowl
            half = 6 if y < 9 else 6 - (y - 8)
            _rect(img, 8 - half, y, 7 + half, y, (240, 240, 244) if y > 4 else (90, 120, 200))
        _rect(img, 3, 7, 12, 7, (70, 100, 190))
    elif iid == "treatise_printed":
        _rect(img, 2, 2, 13, 14, (60, 46, 90))
        _rect(img, 3, 3, 12, 13, (80, 62, 120))
        _rect(img, 12, 3, 13, 13, (240, 236, 222))
        for y in (5, 7, 9):
            _rect(img, 5, y, 10, y, (220, 200, 120))
        _rect(img, 2, 2, 3, 14, (40, 30, 64))
    elif iid == "seawater_bucket":
        _bucket(img, (44, 92, 160))
    elif iid == "brine_bucket":
        _bucket(img, (150, 176, 186))
    elif iid == "leavened_bread":
        for y in range(6, 13):
            half = 6 - abs(y - 9) // 2
            _rect(img, 8 - half, y, 7 + half, y, (196, 140, 64) if y < 10 else (160, 104, 44))
        for x in (5, 8, 11):
            _rect(img, x, 7, x, 8, (232, 196, 120))
    elif iid == "magnifying_lens":
        rnd = random.Random(24)
        px = img.load()
        for y in range(1, 11):
            for x in range(4, 14):
                d = (x - 8.5) ** 2 + (y - 5.5) ** 2
                if d <= 20.5:
                    px[x, y] = (196, 132, 64, 255) if d > 12 else (190, 225, 240, 200 + rnd.randint(0, 40))
        for i in range(5):
            _rect(img, 5 - i, 10 + i, 6 - i, 10 + i, (110, 78, 44))
    return img


def _bottle(img, liquid):
    _rect(img, 6, 2, 9, 3, (150, 110, 70))
    _rect(img, 6, 4, 9, 6, (200, 220, 230))
    _rect(img, 4, 7, 11, 14, (200, 220, 230))
    _rect(img, 5, 8, 10, 13, liquid)


def _bucket(img, water):
    grey, dark, light = (150, 150, 156), (90, 90, 96), (200, 200, 206)
    _rect(img, 3, 4, 12, 4, dark)
    _rect(img, 4, 5, 11, 6, water)
    for y in range(5, 14):
        x0 = 3 + (y - 4) // 4
        _rect(img, x0, y, x0, y, dark)
        _rect(img, 15 - x0, y, 15 - x0, y, dark)
        if y > 6:
            _rect(img, x0 + 1, y, 14 - x0, y, grey if y % 3 else light)
    _rect(img, 5, 14, 10, 14, dark)
    _rect(img, 2, 3, 2, 4, dark)
    _rect(img, 13, 3, 13, 4, dark)


# ---------------------------------------------------------------- json assets

def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _ingredient(s):
    return {"tag": s[1:]} if s.startswith("#") else {"item": s}


def write_assets(assets, data_dir, mod_id, machine_ids, station_ids):
    tex_block = assets / "textures" / "block"
    tex_block.mkdir(parents=True, exist_ok=True)
    with open(animations.ROOT / "data" / "machines.csv", encoding="utf-8") as f:
        mrows = list(csv.DictReader(f))
    with open(animations.ROOT / "data" / "reactions.csv", encoding="utf-8") as f:
        rrows = list(csv.DictReader(f))
    anim_plan = animations.plan(mrows, rrows)
    runtime = assets.parent.parent / mod_id
    runtime.mkdir(parents=True, exist_ok=True)
    (runtime / "animations.json").write_text(json.dumps({m: modes for m, (_, modes) in anim_plan.items()},
                                                        indent=1, sort_keys=True) + "\n", encoding="utf-8")
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    for bid in machine_ids + station_ids:
        lit = bid in machine_ids
        for name, img in machine_textures(bid).items():
            img.save(tex_block / f"{bid}_{name}.png")
        base = {"parent": "minecraft:block/orientable",
                "textures": {"top": f"{mod_id}:block/{bid}_top", "front": f"{mod_id}:block/{bid}_front",
                             "side": f"{mod_id}:block/{bid}_side"}}
        _dump(assets / "models" / "block" / f"{bid}.json", base)
        variants = {}
        if lit and bid in anim_plan:
            # 3.33.0: every machine animates while a reaction runs, one look per kind of process (animations.py).
            looks, _ = anim_plan[bid]
            textures = machine_textures(bid)
            hand = machine_animations(bid) if bid in ANIMATED else {}
            on = json.loads(json.dumps(base))
            on["textures"]["front"] = f"{mod_id}:block/{bid}_front_on"
            _dump(assets / "models" / "block" / f"{bid}_on.json", on)
            for m, look in enumerate(looks):
                sfx = "" if m == 0 else f"_{m}"
                if m == 0 and hand:
                    for name, (frames, ft, *interp) in hand.items():
                        save_animation(tex_block / f"{bid}_{name}.png", frames, ft, *interp)
                else:
                    fronts, tops, ft, interp = animations.render(bid, textures, look)
                    save_animation(tex_block / f"{bid}_front_active{sfx}.png", fronts, ft, interp)
                    save_animation(tex_block / f"{bid}_top_active{sfx}.png", tops, ft, interp)
                act = json.loads(json.dumps(base))
                act["textures"]["front"] = f"{mod_id}:block/{bid}_front_active{sfx}"
                act["textures"]["top"] = f"{mod_id}:block/{bid}_top_active{sfx}"
                _dump(assets / "models" / "block" / f"{bid}_active{sfx}.json", act)
            for facing, y in rot.items():
                for state in ("false", "true"):
                    v = {"model": f"{mod_id}:block/{bid}{'_on' if state == 'true' else ''}"}
                    if y:
                        v["y"] = y
                    variants[f"facing={facing},lit={state},active=false"] = v
                for m in range(animations.MAX_MODES):
                    sfx = "" if m >= len(looks) or m == 0 else f"_{m}"
                    v = {"model": f"{mod_id}:block/{bid}_active{sfx}"}
                    if y:
                        v["y"] = y
                    variants[f"facing={facing},active=true,mode={m}"] = v
        elif bid == "lithium_ion_battery":
            # 3.41.0: a charge bar on the front and a button on top, in the charge's colour; no animation.
            textures = machine_textures(bid)
            for band in range(len(animations.BATTERY_COLOURS)):
                animations.battery_top(textures["top"], band).save(tex_block / f"{bid}_top_{band}.png")
            for level in range(animations.BATTERY_ROWS + 1):
                for band in range(len(animations.BATTERY_COLOURS)):
                    if (level == 0) != (band == 0) or (level == animations.BATTERY_ROWS) != (band == 4):
                        continue  # never shown: empty is black and only full is blue
                    animations.battery_front(textures["side"], level, band).save(tex_block / f"{bid}_front_{level}_{band}.png")
                    mdl = json.loads(json.dumps(base))
                    mdl["textures"]["front"] = f"{mod_id}:block/{bid}_front_{level}_{band}"
                    mdl["textures"]["top"] = f"{mod_id}:block/{bid}_top_{band}"
                    _dump(assets / "models" / "block" / f"{bid}_{level}_{band}.json", mdl)
            for facing, y in rot.items():
                for level in range(animations.BATTERY_ROWS + 1):
                    for band in range(len(animations.BATTERY_COLOURS)):
                        ok = not ((level == 0) != (band == 0) or (level == animations.BATTERY_ROWS) != (band == 4))
                        v = {"model": f"{mod_id}:block/{bid}_{level}_{band}" if ok else f"{mod_id}:block/{bid}_0_0"}
                        if y:
                            v["y"] = y
                        variants[f"facing={facing},level={level},band={band}"] = v
            # The item shows the battery empty.
            base = json.loads(json.dumps(base))
            base["textures"]["front"] = f"{mod_id}:block/{bid}_front_0_0"
            base["textures"]["top"] = f"{mod_id}:block/{bid}_top_0"
            _dump(assets / "models" / "block" / f"{bid}.json", base)
        elif lit:
            on = json.loads(json.dumps(base))
            on["textures"]["front"] = f"{mod_id}:block/{bid}_front_on"
            if bid in animations.POWER_LOOKS:
                # 3.33.1: a power block animates while it runs (lit=true).
                fronts, tops, ft, interp = animations.render_power(bid, machine_textures(bid))
                save_animation(tex_block / f"{bid}_front_on.png", fronts, ft, interp)
                save_animation(tex_block / f"{bid}_top_on.png", tops, ft, interp)
                on["textures"]["top"] = f"{mod_id}:block/{bid}_top_on"
            _dump(assets / "models" / "block" / f"{bid}_on.json", on)
            for facing, y in rot.items():
                for state in ("false", "true"):
                    v = {"model": f"{mod_id}:block/{bid}{'_on' if state == 'true' else ''}"}
                    if y:
                        v["y"] = y
                    variants[f"facing={facing},lit={state}"] = v
        else:
            for facing, y in rot.items():
                v = {"model": f"{mod_id}:block/{bid}"}
                if y:
                    v["y"] = y
                variants[f"facing={facing}"] = v
        _dump(assets / "blockstates" / f"{bid}.json", {"variants": variants})
        _dump(assets / "models" / "item" / f"{bid}.json", {"parent": f"{mod_id}:block/{bid}"})
        _dump(data_dir / "loot_table" / "blocks" / f"{bid}.json", {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod_id}:{bid}"}],
                       "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

    # Copper wire: a thin cable block that joins electric machines to piles and dynamos.
    wire_tex = _noise((196, 120, 72), 18, 77)
    wire_tex.save(tex_block / "copper_wire.png")
    _dump(assets / "models" / "block" / "copper_wire.json", {
        "parent": "minecraft:block/block", "textures": {"particle": f"{mod_id}:block/copper_wire", "wire": f"{mod_id}:block/copper_wire"},
        "elements": [{"from": [5, 5, 0], "to": [11, 11, 16],
                      "faces": {f: {"texture": "#wire"} for f in ("north", "south", "east", "west", "up", "down")}},
                     {"from": [0, 5, 5], "to": [16, 11, 11],
                      "faces": {f: {"texture": "#wire"} for f in ("north", "south", "east", "west", "up", "down")}},
                     {"from": [5, 0, 5], "to": [11, 16, 11],
                      "faces": {f: {"texture": "#wire"} for f in ("north", "south", "east", "west", "up", "down")}}]})
    _dump(assets / "blockstates" / "copper_wire.json", {"variants": {"": {"model": f"{mod_id}:block/copper_wire"}}})
    _dump(assets / "models" / "item" / "copper_wire.json", {"parent": f"{mod_id}:block/copper_wire"})
    _dump(data_dir / "loot_table" / "blocks" / "copper_wire.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod_id}:copper_wire"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

    # Insulated cable: the same cross shape as copper wire, sleeved in grey PVC with copper at the ends.
    cable_tex = _noise((72, 76, 82), 8, 79)
    _rect(cable_tex, 6, 6, 9, 9, (196, 120, 72))
    cable_tex.save(tex_block / "insulated_cable.png")
    _dump(assets / "models" / "block" / "insulated_cable.json", {
        "parent": "minecraft:block/block", "textures": {"particle": f"{mod_id}:block/insulated_cable", "wire": f"{mod_id}:block/insulated_cable"},
        "elements": [{"from": [5, 5, 0], "to": [11, 11, 16],
                      "faces": {f: {"texture": "#wire"} for f in ("north", "south", "east", "west", "up", "down")}},
                     {"from": [0, 5, 5], "to": [16, 11, 11],
                      "faces": {f: {"texture": "#wire"} for f in ("north", "south", "east", "west", "up", "down")}},
                     {"from": [5, 0, 5], "to": [11, 16, 11],
                      "faces": {f: {"texture": "#wire"} for f in ("north", "south", "east", "west", "up", "down")}}]})
    _dump(assets / "blockstates" / "insulated_cable.json", {"variants": {"": {"model": f"{mod_id}:block/insulated_cable"}}})
    _dump(assets / "models" / "item" / "insulated_cable.json", {"parent": f"{mod_id}:block/insulated_cable"})
    # Asphalt: a full block of near-black aggregate.
    asphalt = _noise((44, 44, 46), 10, 81)
    rnd = random.Random(82)
    for _ in range(14):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        asphalt.load()[x, y] = (110, 108, 104, 255)
    asphalt.save(tex_block / "asphalt.png")
    _dump(assets / "models" / "block" / "asphalt.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod_id}:block/asphalt"}})
    _dump(assets / "blockstates" / "asphalt.json", {"variants": {"": {"model": f"{mod_id}:block/asphalt"}}})
    _dump(assets / "models" / "item" / "asphalt.json", {"parent": f"{mod_id}:block/asphalt"})
    # Tritium light: pale green glow behind glass.
    glow = _noise((150, 240, 160), 10, 83)
    _rect(glow, 0, 0, 15, 0, (220, 236, 230)); _rect(glow, 0, 15, 15, 15, (220, 236, 230))
    _rect(glow, 0, 0, 0, 15, (220, 236, 230)); _rect(glow, 15, 0, 15, 15, (220, 236, 230))
    glow.save(tex_block / "tritium_light.png")
    _dump(assets / "models" / "block" / "tritium_light.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod_id}:block/tritium_light"}})
    _dump(assets / "blockstates" / "tritium_light.json", {"variants": {"": {"model": f"{mod_id}:block/tritium_light"}}})
    _dump(assets / "models" / "item" / "tritium_light.json", {"parent": f"{mod_id}:block/tritium_light"})
    # Storage tank: riveted iron bands round a glass sight window.
    tank_side = _noise((150, 154, 160), 8, 84)
    _rect(tank_side, 0, 0, 15, 1, (90, 94, 100)); _rect(tank_side, 0, 14, 15, 15, (90, 94, 100))
    _rect(tank_side, 5, 3, 10, 12, (190, 220, 235))
    _rect(tank_side, 6, 4, 6, 11, (235, 248, 255))
    for x in (1, 14):
        for y in (3, 7, 11):
            _rect(tank_side, x, y, x, y, (210, 214, 220))
    tank_side.save(tex_block / "storage_tank_side.png")
    tank_top = _noise((120, 124, 130), 8, 85)
    _rect(tank_top, 6, 6, 9, 9, (200, 140, 60))
    tank_top.save(tex_block / "storage_tank_top.png")
    _dump(assets / "models" / "block" / "storage_tank.json",
          {"parent": "minecraft:block/cube_column", "textures": {"side": f"{mod_id}:block/storage_tank_side",
                                                                 "end": f"{mod_id}:block/storage_tank_top"}})
    _dump(assets / "blockstates" / "storage_tank.json", {"variants": {"": {"model": f"{mod_id}:block/storage_tank"}}})
    _dump(assets / "models" / "item" / "storage_tank.json", {"parent": f"{mod_id}:block/storage_tank"})
    for bid in ("insulated_cable", "asphalt", "tritium_light", "storage_tank"):
        _dump(data_dir / "loot_table" / "blocks" / f"{bid}.json", {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod_id}:{bid}"}],
                       "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    _dump(data_dir / "tags" / "item" / "cable_insulation.json",
          {"values": [f"{mod_id}:polyvinyl_chloride", f"{mod_id}:polyethylene"]})

    # Smoke detector: a small white disc on the ceiling or a wall; red lamp when it alarms.
    for name, lamp in (("smoke_detector", (120, 30, 30)), ("smoke_detector_on", (255, 40, 40))):
        t = _noise((236, 236, 232), 4, 78)
        _rect(t, 6, 6, 9, 9, lamp)
        t.save(tex_block / f"{name}.png")
        _dump(assets / "models" / "block" / f"{name}.json", {
            "parent": "minecraft:block/block", "textures": {"particle": f"{mod_id}:block/{name}", "all": f"{mod_id}:block/{name}"},
            "elements": [{"from": [4, 0, 4], "to": [12, 2, 12],
                          "faces": {f: {"texture": "#all"} for f in ("north", "south", "east", "west", "up", "down")}}]})
    _dump(assets / "blockstates" / "smoke_detector.json", {"variants": {
        "powered=false": {"model": f"{mod_id}:block/smoke_detector"},
        "powered=true": {"model": f"{mod_id}:block/smoke_detector_on"}}})
    _dump(assets / "models" / "item" / "smoke_detector.json", {"parent": f"{mod_id}:block/smoke_detector"})
    _dump(data_dir / "loot_table" / "blocks" / "smoke_detector.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod_id}:smoke_detector"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

    for iid in ITEMS:
        item_texture(iid).save(assets / "textures" / "item" / f"{iid}.png")
        _dump(assets / "models" / "item" / f"{iid}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{iid}"}})

    for rid, r in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {
            "type": "minecraft:crafting_shaped", "category": "misc", "pattern": r["pattern"],
            "key": {k: _ingredient(v) for k, v in r["key"].items()},
            "result": {"id": f"{mod_id}:{rid}", "count": r.get("count", 1)}})
    for rid, (ings, count) in SHAPELESS.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {
            "type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [_ingredient(i) for i in ings], "result": {"id": f"{mod_id}:{rid}", "count": count}})

    for rid, (ings, result, count) in EXTRA_SHAPELESS.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {
            "type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [_ingredient(i) for i in ings],
            "result": dict({"id": result, "count": count}, **({"components": RESULT_COMPONENTS[rid]} if rid in RESULT_COMPONENTS else {}))})

    for rid, (pattern, key, result, count) in EXTRA_SHAPED.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {
            "type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
            "key": {k: _ingredient(v) for k, v in key.items()}, "result": {"id": result, "count": count}})

    for el, ingot in INGOT_ELEMENTS.items():
        _dump(data_dir / "recipe" / f"{el}_from_ingot.json", {
            "type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [_ingredient(ingot)], "result": {"id": f"{mod_id}:{el}", "count": 1}})
        _dump(data_dir / "recipe" / f"{ingot.split(':')[1]}_from_{el}.json", {
            "type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [_ingredient(f"{mod_id}:{el}")], "result": {"id": ingot, "count": 1}})

    # Vanilla furnaces and blast furnaces no longer smelt metal ores: metals come from the mod's machines.
    disabled = {"load_conditions": [{"condition": "fabric:not", "value": {"condition": "fabric:true"}}]}
    mc_recipes = data_dir.parent / "minecraft" / "recipe"
    for metal, sources in DISABLED_SMELTING.items():
        for source in sources:
            for method in ("smelting", "blasting"):
                _dump(mc_recipes / f"{metal}_ingot_from_{method}_{source}.json",
                      {"fabric:load_conditions": disabled["load_conditions"]})

    # Bows and crossbows fire anything in #minecraft:arrows.
    _dump(data_dir.parent / "minecraft" / "tags" / "item" / "arrows.json",
          {"replace": False, "values": [f"{mod_id}:{a}" for a in ARROWS]})
    _dump(data_dir / "tags" / "item" / "ink.json", {"values": [f"{mod_id}:carbon_ink", f"{mod_id}:iron_gall_ink", "minecraft:ink_sac"]})
    from ores import PICKAXE
    from geo_ores import SHOVEL
    mine = {"pickaxe": [f"{mod_id}:{o}" for o in PICKAXE], "axe": [], "shovel": list(SHOVEL)}
    for bid in machine_ids + station_ids + ["copper_wire", "smoke_detector", "insulated_cable", "asphalt", "tritium_light", "storage_tank"]:
        mine[MINEABLE.get(bid, "pickaxe")].append(f"{mod_id}:{bid}")
    from ores import ORES
    for oid, v in ORES.items():
        if v[1] == "sand":
            mine["pickaxe"].remove(f"{mod_id}:{oid}")
            mine["shovel"].append(f"{mod_id}:{oid}")
    mc = data_dir.parent / "minecraft" / "tags" / "block" / "mineable"
    for tool, ids in mine.items():
        _dump(mc / f"{tool}.json", {"replace": False, "values": ids})


from geo_options_lang import GEO_OPTIONS_LANG as _GEO_OPTIONS_LANG
GUI_LANG = {**_ATLAS_LANG, **_COMP_LANG, **_GEO_OPTIONS_LANG,
    "tag.item.periodic_elements.ink": "Ink",
    "itemGroup.periodic_elements.workshop": "Research and Machines",
    "generator.periodic_elements.geological": "Geological",
    "message.periodic_elements.level_up": "You reached tech level %s: %s",
    "message.periodic_elements.needs_research": "You need to research %s first",
    "message.periodic_elements.research.done": "Research finished: %s",
    "message.periodic_elements.research.started": "Researching %s",
    "message.periodic_elements.research.busy": "This station is already researching something",
    "message.periodic_elements.research.wrong_station": "That project belongs to another era's station",
    "message.periodic_elements.research.known": "You already know this",
    "message.periodic_elements.research.needs_level": "Finish the key projects of the previous level first",
    "message.periodic_elements.research.needs_projects": "Research the required projects first",
    "message.periodic_elements.research.missing_samples": "Put the samples in the station's slots",
    "message.periodic_elements.treatise.written": "Treatise written",
    "message.periodic_elements.treatise.needs_writing": "Research Writing and Ink to write treatises",
    "message.periodic_elements.treatise.nothing": "You have no research to write down yet",
    "message.periodic_elements.treatise.missing": "Needs 1 leather, plus 1 paper and 1 ink per project, in the station's slots",
    "message.periodic_elements.treatise.missing_printed": "Printing needs 1 type metal, 2 leather, and 1 paper and 1 ink per 2 projects",
    "hud.periodic_elements.lens": "%s: %s units",
    "hud.periodic_elements.lens.nothing": "%s: no known composition",
    "hud.periodic_elements.lens.other": "%s other (elements you haven't discovered)",
    "message.periodic_elements.treatise.blank": "This treatise is blank",
    "message.periodic_elements.treatise.nothing_new": "You already know everything in %s's treatise",
    "message.periodic_elements.treatise.learned": "You learned %s projects from %s's treatise",
    "tooltip.periodic_elements.treatise.blank": "Blank: write one at a research station",
    "tooltip.periodic_elements.treatise.author": "Written by %s",
    "tooltip.periodic_elements.treatise.count": "%s projects (author at tech level %s)",
    "tooltip.periodic_elements.treatise.more": " ...and %s more",
    "tooltip.periodic_elements.treatise.use": "Use to learn all of it; the treatise is consumed",
    "tooltip.periodic_elements.requires": "Requires: %s",
    "tooltip.periodic_elements.requires.nothing": "nothing (Stone Age)",
    "tooltip.periodic_elements.requires.level": "tech level %s (%s)",
    "command.periodic_elements.research.level": "%s is at tech level %s: %s (* = key, green = done, yellow = available)",
    "command.periodic_elements.research.unknown": "Unknown project: %s",
    "command.periodic_elements.research.granted": "Granted %s projects to %s players",
    "command.periodic_elements.research.revoked": "Revoked %s projects from %s players",
    "gui.periodic_elements.machine.max": "limit %s °C",
    "gui.periodic_elements.machine.pressure": "%s atm (open)",
    "gui.periodic_elements.machine.conditions": "%s–%s °C · %s atm · %s",
    "gui.periodic_elements.machine.carbon": "Charcoal reductant: %s units of carbon",
    "gui.periodic_elements.machine.bellows": "Bellows: +250 °C flame",
    "gui.periodic_elements.machine.releases": "gives %s kJ",
    "gui.periodic_elements.machine.needs": "%s kJ",
    "gui.periodic_elements.machine.temperature": "Temperature: %s °C",
    "gui.periodic_elements.machine.cap": "This fuel can reach %s °C",
    "gui.periodic_elements.machine.needs_temp": "Reaction needs %s °C",
    "gui.periodic_elements.machine.catalyst": "Needs: %s",
    "gui.periodic_elements.machine.vented": " (vented)",
    "gui.periodic_elements.machine.to_slag": " (to slag)",
    "gui.periodic_elements.machine.per100": "per 100 units of %s",
    "gui.periodic_elements.status.idle": "Idle: add something to process",
    "gui.periodic_elements.status.working": "Working",
    "gui.periodic_elements.status.heating": "Heating up to %s °C",
    "gui.periodic_elements.status.too_hot": "Too hot: must stay under %s °C",
    "gui.periodic_elements.status.needs_carbon": "Needs charcoal or coal as reductant",
    "gui.periodic_elements.status.needs_research": "The owner hasn't researched this process",
    "gui.periodic_elements.status.output_full": "Output full",
    "gui.periodic_elements.status.no_fuel": "Out of fuel: needs %s °C",
    "gui.periodic_elements.status.needs_reagent": "Needs %s in the reagent slot",
    "gui.periodic_elements.status.needs_flasks": "Needs empty Glass Flasks",
    "gui.periodic_elements.status.needs_cylinders": "Needs empty Steel Cylinders",
    "gui.periodic_elements.machine.flasks": "Empty Glass Flasks: filled with the liquids and gases the machine makes",
    "gui.periodic_elements.machine.cylinders": "Empty Steel Cylinders: filled with compressed gas when a Compressor is fitted",
    "gui.periodic_elements.machine.upgrade": "Upgrade slot: Compressor, Regenerator, Refractory Lining, Gas Recovery Hood, Mechanical Stirrer, Catalyst Bed, Output Pusher, Vacuum Pump, Conveyor Belt, Foam Lagging or Sterile Air Filter",
    "gui.periodic_elements.machine.compressor_locked": "Research Gas Compression to use the Compressor",
    "block.periodic_elements.storage_tank": "Storage Tank",
    "block.periodic_elements.storage_tank.desc": "Holds 16,000 units of one liquid or gas: pour flasks and cylinders in, draw flasks out",
    "gui.periodic_elements.tank.empty": "Storage Tank: empty (holds %s units)",
    "gui.periodic_elements.tank.holds": "Storage Tank: %s / %s units of %s",
    "gui.periodic_elements.tank.wrong": "This tank holds %s: it can't take anything else",
    "gui.periodic_elements.tank.full": "The tank is full",
    "gui.periodic_elements.status.no_power": "No power: put a charged Voltaic Pile or a running Dynamo next to it, or join one with Copper Wire",
    "block.periodic_elements.copper_wire": "Copper Wire",
    "block.periodic_elements.smoke_detector": "Smoke Detector",
    "block.periodic_elements.insulated_cable": "Insulated Cable",
    "block.periodic_elements.insulated_cable.desc": "Carries current like copper wire, but four times as far",
    "gui.periodic_elements.guide.title": "Field Guide",
    "gui.periodic_elements.guide.pick": "Pick a chapter",
    "gui.periodic_elements.guide.more": "Tech level %s: chapters for level %s and later appear as you research",
    "gui.periodic_elements.guide.all": "Every chapter is open",
    "gui.periodic_elements.guide.pages": "Pages %s-%s of %s",
    "block.periodic_elements.tritium_light": "Tritium Light",
    "block.periodic_elements.tritium_light.desc": "Glows for ever with no power: tritium's beta rays on zinc sulfide",
    "block.periodic_elements.asphalt": "Asphalt",
    "block.periodic_elements.asphalt.desc": "A smooth road surface: you walk and ride a quarter faster on it",
    "tag.item.periodic_elements.cable_insulation": "Cable Insulation",
    "gui.periodic_elements.power.generator": "Diesel Generator: %s of %s kJ stored; %s",
    "gui.periodic_elements.power.generator.running": "running (%s s of fuel left)",
    "gui.periodic_elements.power.generator.idle": "idle: needs diesel, kerosene, gasoline or naphtha, or is full",
    "block.periodic_elements.smoke_detector.desc": "Gives a redstone signal and beeps when fire burns within 4 blocks",
    "message.periodic_elements.communicator.needs": "Research Optical Fibre to use the communicator",
    "message.periodic_elements.communicator.alone": "No one else is on the network",
    "message.periodic_elements.communicator.player": "%s: %s blocks %s",
    "message.periodic_elements.communicator.other_world": "%s: in %s",
    "message.periodic_elements.analyzer.needs": "Research the Element Analyzer to use it",
    "message.periodic_elements.analyzer.nothing": "%s: no composition known",
    "message.periodic_elements.analyzer.result": "%s: %s",
    "gui.periodic_elements.power.rtg": "Radioisotope Generator: %s of %s kJ stored; %s plutonium-238 pellets give %s kJ per tick",
    "gui.periodic_elements.power.battery": "Lithium-Ion Battery: %s of %s kJ stored",
    "gui.periodic_elements.power.helium": "Helium Collector: %s of %s units of helium; %s units per day; %s",
    "gui.periodic_elements.power.helium.flasks": "%s empty flasks",
    "gui.periodic_elements.power.helium.no_flask": "full: needs an empty Glass Flask",
    "gui.periodic_elements.power.helium.nothing": "put radioactive things in it",
    "block.periodic_elements.copper_wire.desc": "Carries current from Voltaic Piles and Dynamos to electric machines",
    "gui.periodic_elements.power.dynamo": "Dynamo: %s of %s kJ stored; %s",
    "gui.periodic_elements.power.solar": "Solar Panel: %s of %s kJ stored; %s",
    "gui.periodic_elements.power.solar.sun": "in sunlight",
    "gui.periodic_elements.power.solar.dark": "dark: needs open sky by day",
    "gui.periodic_elements.power.dynamo.turning": "turning",
    "gui.periodic_elements.power.dynamo.still": "still: needs a running Steam Engine next to it",
    "gamerule.periodicRadiation": "Radiation (Elementar)",
    "gamerule.periodicRadiation.description": "realistic: real dose rates and effects; milder: a tenth of the dose; off: no radiation",
    "message.periodic_elements.electroscope": "Radiation here: %s · recent dose %s · lifetime %s",
    "message.periodic_elements.electroscope.off": "Radiation is off in this world (gamerule periodicRadiation)",
    "message.periodic_elements.electroscope.needs": "Research Radioactivity to read the electroscope",
    "message.periodic_elements.geiger_counter.needs": "Research the Geiger Counter to use it",
    "message.periodic_elements.radiation.sick": "You feel sick: radiation dose %s",
    "tooltip.periodic_elements.radioactive": "Radioactive: %s at 1 m",
    "tooltip.periodic_elements.isotope": "Isotope: %s",
    "tooltip.periodic_elements.natural_isotopes": "Natural isotopes: %s",
    "tooltip.periodic_elements.isotope_ratio": "%s: %s%% (%s)",
    "isotope.periodic_elements.natural": "natural",
    "isotope.periodic_elements.enriched": "enriched",
    "isotope.periodic_elements.depleted": "depleted",
    "isotope.periodic_elements.weapons_grade": "weapons grade",
    "isotope.periodic_elements.fuel_grade": "fuel grade",
    "isotope.periodic_elements.reactor_grade": "reactor grade",
    "tooltip.periodic_elements.decay": "Decays (%s): half-life %s of game time (%s real)",
    "tooltip.periodic_elements.decay.into": "Decays into %s",
    "gui.periodic_elements.power.reactor": "Nuclear Reactor: %s of %s kJ stored; %s; %s",
    "gui.periodic_elements.power.reactor.burning": "rod %s%% burnt",
    "gui.periodic_elements.power.wind": "Wind Turbine: %s of %s kJ stored; making %s kJ per tick (needs open sky; more up high and in storms)",
    "gui.periodic_elements.power.reactor.no_rod": "no fuel rod",
    "gui.periodic_elements.power.reactor.cooled": "cooled by water",
    "gui.periodic_elements.power.reactor.dry": "stopped: needs water touching it",
    "stat.periodic_elements.radiation_dose": "Radiation dose (µSv)",
    "effect.periodic_elements.radiation": "Radiation Sickness",
    "gui.periodic_elements.power.pile": "Voltaic Pile: %s of %s kJ stored; electrolyte good for %s more zinc",
    "gui.periodic_elements.power.engine_running": "Steam Engine running: fuel for %s s, water for %s s",
    "gui.periodic_elements.power.engine_idle": "Steam Engine idle: runs with fuel and a water bucket while a furnace with Bellows next to it works",
    "gui.periodic_elements.machine.reagent": "Reagent slot: a second ingredient",
    "gui.periodic_elements.machine.reagent_line": " + %s %s (reagent)",
    "gui.periodic_elements.station.research": "Research",
    "gui.periodic_elements.station.treatise": "Treatise",
    "gui.periodic_elements.station.your_level": "Your tech level: %s",
    "gui.periodic_elements.station.idle": "Station idle",
    "gui.periodic_elements.station.researching": "Researching...",
    "gui.periodic_elements.station.cost": "Treatise cost",
    "gui.periodic_elements.station.samples": "Samples",
    "gui.periodic_elements.station.key": "key project",
    "gui.periodic_elements.station.optional": "optional",
    "gui.periodic_elements.station.requires": "Requires:",
    "gui.periodic_elements.station.needs_level": "Needs tech level %s",
    "gui.periodic_elements.station.unlocks": "Unlocks:",
    "gui.periodic_elements.station.time": "Takes %s seconds",
}
