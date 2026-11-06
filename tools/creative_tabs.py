"""Creative inventory tabs (3.51.0): every mod item goes to exactly one tab.

Writes periodic_elements/creative_tabs.json: {"tabs": [ids in order], "items": {item id: tab id}}.
The order of items inside a tab follows the order of registration in the game.
Substances go to the solid, liquid or gas tab by their state at SATP (25 C, 1 atm), the `state` column of
substances.csv. Elements always stay in the elements tab.
"""
import csv
import json
from pathlib import Path

import creative_items
import crops
import exploration
import fauna
import genetics
import geo_rocks
import logistics
import machines
import mining
import multiblock
import shrubs
import tanks

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "data"

TABS = ["elements", "solid_substances", "liquid_substances", "gas_substances", "materials", "ores_rocks", "trees",
        "shrubs", "crops", "fauna", "food_medicine", "machines", "power_logistics", "science", "tools", "combat",
        "exploration", "creative"]

# icon item for each tab
ICONS = {"elements": "carbon", "solid_substances": "sodium_chloride", "liquid_substances": "sulfuric_acid",
         "gas_substances": "methane_cylinder", "materials": "steel_ingot", "ores_rocks": "raw_bauxite",
         "trees": "mahogany_sapling", "shrubs": "guarana_cutting", "crops": "rice", "fauna": "capybara_spawn_egg",
         "food_medicine": "antivenom", "machines": "bessemer_converter", "power_logistics": "dynamo",
         "science": "field_guide", "tools": "manganese_steel_pickaxe", "combat": "breech_loading_rifle",
         "exploration": "leather_backpack", "creative": "creative_generator"}

NAMES = {  # English; the lang strings and translations are in creative_items.py
    "elements": "Elements", "solid_substances": "Solid Substances", "liquid_substances": "Liquid Substances",
    "gas_substances": "Gases", "materials": "Materials and Intermediates", "ores_rocks": "Ores, Rocks and Soils",
    "trees": "Trees and Wood", "shrubs": "Shrubs", "crops": "Crops and Seeds", "fauna": "Animals",
    "food_medicine": "Food and Medicine", "machines": "Machines", "power_logistics": "Power and Logistics",
    "science": "Science", "tools": "Tools", "combat": "Combat", "exploration": "Exploration",
    "creative": "Creative Testing"}

# Explicit placements win over every rule below.
EXPLICIT = {
    **{i: "creative" for i in creative_items.BLOCKS + creative_items.ITEMS + ["progress_book"]},
    **{i: "fauna" for i in ("herring_bucket",)},
    **{i: "food_medicine" for i in ("cooked_rice", "cooked_beans", "cassava_flour") + tuple(shrubs.TEAS)},
    # science: books, research, instruments
    **{i: "science" for i in ("field_guide", "treatise_manuscript", "treatise_printed", "magnifying_lens", "electroscope",
                              "geiger_counter", "element_analyzer", "fibre_communicator", "field_atlas", "atlas_copy",
                              "compendium_notes", "tree_compendium", "plant_compendium", "mineral_compendium",
                              "animal_compendium", "crossing_notebook", "seed_lens",
                              "prospecting_magnetometer", "core_drill", "handheld_radar", "gps_receiver", "satellite",
                              "survey_balloon", "sample_case", "carbon_ink", "iron_gall_ink", "dna_sequencer",
                              "thermocycler", "seed_irradiator", "gamma_garden", "gene_gun", "crispr_kit", "bt_gene",
                              "epsps_gene", "golden_gene", "dreb_gene", "cobalt_60_source", "launch_pad")},
    # food and medicine
    **{i: "food_medicine" for i in ("leavened_bread", "anaesthetic", "carbolic_soap", "prussian_blue_capsule",
                                    "mrna_vaccine", "antivenom", "cooked_game_meat", "cooked_egg", "cooked_herring",
                                    "hakarl", "caviar")},
    # exploration
    **{i: "exploration" for i in list(exploration.ITEMS) + list(exploration.BLOCKS)
       + ["parachute", "carbide_lamp", "wetsuit_hood", "wetsuit_jacket", "wetsuit_leggings", "wetsuit_boots",
          "sneakers", "street_lamp", "explorer_light"]},
    # combat
    **{i: "combat" for i in ("dynamite", "cordite_cartridge", "tungsten_carbide_arrow", "depleted_uranium_arrow",
                             "black_powder", "smokeless_powder", "anfo_charge", "breech_loading_rifle", "hand_cannon",
                             "penetrator_arrow")},
    # tools
    **{i: "tools" for i in list(mining.MINING_ITEMS) + ["welding_torch", "pollination_brush", "ddt_duster", "glass_flask",
                                                        "steel_cylinder", "seawater_bucket", "brine_bucket"]},
    # farming chemicals and crop helpers go with crops
    **{i: "crops" for i in ("weed_killer", "pre_emergent", "field_clear", "insect_spray", "bordeaux_mixture",
                            "tolerant_seeds", "pk_blend", "npk_prills", "feeding_trough", "silage", "inoculant",
                            "balanced_feed", "silo", "protein_meal", "oil_cake", "biomass", "spent_mycelium")},
    **{i: "science" for i in ("alchemists_laboratory", "natural_philosophers_laboratory", "potters_workshop",
                              "university_laboratory", "industrial_laboratory", "national_laboratory",
                              "international_research_centre", "radar_station", "tracking_station", "radio_transmitter")},
    **{i: "power_logistics" for i in ("dynamo", "steam_engine", "diesel_generator", "solar_panel", "wind_turbine",
                                      "radioisotope_generator", "nuclear_reactor", "voltaic_pile", "charging_bench",
                                      "lithium_ion_battery", "insulation")},
    **{i: "machines" for i in ("helium_collector", "greenhouse_heater", "greenhouse_cooler", "compressor", "regenerator",
                               "refractory_lining", "gas_recovery_hood", "mechanical_stirrer", "catalyst_bed",
                               "output_pusher", "vacuum_pump", "foam_lagging", "sterile_air_filter")},
    **{i: "materials" for i in ("asphalt", "tarmac", "amber_bakelite", "black_bakelite", "brown_bakelite", "green_bakelite",
                                "ivory_bakelite", "red_bakelite", "galalith")},
    **{i: "ores_rocks" for i in ("heavy_mineral_sand", "heavy_mineral_concentrate", "terra_roxa")},
    **{i: "tools" for i in ("blasting_charge", "demolition_charge", "mining_charge", "shaped_charge", "tree_tap")},
    **{i: "exploration" for i in ("tallow_candle", "tritium_light")},
    "flintlock_musket": "combat",
    # power and logistics
    **{i: "power_logistics" for i in list(tanks.BLOCKS) + list(logistics.BELTS) + list(logistics.HOPPERS)
       + list(logistics.CHESTS) + [logistics.SORTER, "copper_wire", "insulated_cable", "storage_tank", "conveyor_belt",
                                   "lead_acid_pack", "li_ion_pack", "smoke_detector", "relief_valve"]},
}


def _csv(name):
    with open(DATA / name, newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def classify(ids, extra=None):
    """Map every id in {@code ids} to a tab; returns (mapping, unplaced ids)."""
    elements = {r["name"].lower() for r in _csv("elements.csv")}
    state = {r["id"]: r["state"] for r in _csv("substances.csv")}
    trees = [r["id"] for r in _csv("trees.csv")]
    tree_extra = {r[k] for r in _csv("trees.csv") for k in ("fruit", "bark_item", "tap") if r.get(k)}
    animals = [r["id"] for r in _csv("fauna.csv")]
    machine_ids = {r["id"] for r in _csv("machines.csv")}
    shrub_ids = set(shrubs.SHRUBS) | set(shrubs.PRODUCTS) | set(shrubs.TEAS) | {s + "_cutting" for s in shrubs.SHRUBS}
    crop_ids = set(crops.items())
    rock_ids = set(geo_rocks.ids())
    fauna_ids = set(fauna.ITEMS) | set(fauna.BLOCKS) | {a + "_spawn_egg" for a in animals} | {a + "_bucket" for a in animals}
    out, rest = {}, []
    for i in ids:
        tab = (extra or {}).get(i) or EXPLICIT.get(i)
        if tab is None:
            if i in elements:
                tab = "elements"
            elif i in state:
                tab = {"solid": "solid_substances", "liquid": "liquid_substances", "gas": "gas_substances"}[state[i]]
            elif i.endswith("_cylinder") and i[:-9] in state:
                tab = "gas_substances" if state[i[:-9]] == "gas" else "liquid_substances"
            elif i.endswith("_cylinder") and i[:-9] in elements:
                tab = "elements"
            elif i.startswith("stripped_") or i.startswith("rubber_"):
                tab = "trees"
            elif i.endswith("_crust"):
                tab = "ores_rocks"
            elif i in machine_ids or i in multiblock.BLOCKS or i in genetics.BLOCKS:
                tab = "machines"
            elif i in fauna_ids:
                tab = "fauna"
            elif i in shrub_ids:
                tab = "shrubs"
            elif i in crop_ids:
                tab = "crops"
            elif i in tree_extra or any(i == t or i.startswith(t + "_") for t in trees):
                tab = "trees"
            elif i in rock_ids or i.endswith("_ore") or i.startswith("raw_") or i.endswith("_soil"):
                tab = "ores_rocks"
            elif i.endswith(("_pickaxe", "_axe", "_shovel", "_hoe", "_sledgehammer", "_excavator", "_hammer")):
                tab = "tools"
            elif i.endswith(("_sword", "_helmet", "_chestplate", "_leggings", "_boots")):
                tab = "combat"
            elif i.endswith("_record") or i.endswith("_sample"):
                tab = "elements"
            elif i in machines.ITEMS:
                tab = "materials"
        if tab is None:
            rest.append(i)
        else:
            out[i] = tab
    return out, rest


# item models that are not items of their own (compass needle frames, filled traps)
NOT_ITEMS_PREFIX = ("explorer_compass_",)
NOT_ITEMS = {"rope_net_full", "steel_cage_full"}


def write(res_dir, models_dir, extra=None):
    """Classifies every item model in models_dir; returns the ids it could not place."""
    ids = sorted(p.stem for p in models_dir.glob("*.json")
                 if p.stem not in NOT_ITEMS and not p.stem.startswith(NOT_ITEMS_PREFIX))
    mapping, rest = classify(ids, extra)
    data = {"tabs": [{"id": t, "icon": ICONS[t]} for t in TABS], "items": mapping}
    (res_dir / "creative_tabs.json").write_text(json.dumps(data, indent=1, sort_keys=False) + "\n", encoding="utf-8")
    return rest


if __name__ == "__main__":
    import sys
    ids = sorted(p.stem for p in (ROOT / "src/main/resources/assets/periodic_elements/models/item").glob("*.json"))
    m, rest = classify(ids)
    from collections import Counter
    print(Counter(m.values()))
    print(len(rest), " ".join(rest))
    for t in TABS:
        if t not in ("trees", "solid_substances", "elements", "liquid_substances"):
            print("==", t, " ".join(i for i in ids if m.get(i) == t))
