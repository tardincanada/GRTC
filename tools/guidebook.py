"""The in-game Field Guide (1.4.0): guidebook.json, built from the same data as the mod, so it never goes stale.

Each chapter is a list of paragraphs. "# " starts a heading, "- " a bullet; the client wraps and pages them.
"""
import json
import re

INTRO = [
    "# Welcome",
    "Every block and item in this world is made of real elements. This guide follows the mod's tech tree from "
    "the Stone Age to the last elements, one chapter per level.",
    "# Units",
    "1 unit is 1 gram. A block is 1000 units, an ingot or an element or substance item 111.1, a nugget 12.35 "
    "and a bucket 1000. Hover over any block or item to see what it is made of; hold Shift for every part.",
    "# Research",
    "Each level has a research station. Put a project's samples in its 9 slots, pick the project and press "
    "Research. Key projects (marked with a star) raise your tech level; you need a level's key projects before "
    "you can place the next station. /research shows what you know.",
    "Treatises written at a station teach everything you know to another player.",
    "# Machines",
    "A machine breaks each input item into its composition and runs every reaction you have researched on "
    "it while it is hot enough and not too hot. Products come out as items, waste as slag, and some gases are "
    "vented. Liquids and gases fill empty Glass Flasks from the machine's side panel. Some reactions need a "
    "second ingredient in the reagent slot.",
    "Fire-heated machines burn fuel from the fuel slot: wood reaches 650 °C, charcoal and coal 1100 °C, oil "
    "fuels 1200 °C and gas 1300 °C. Bellows (forced air) add 250 °C. Charcoal and coal also serve as the "
    "carbon that reduces ores. Electric machines take current from a Voltaic Pile, Dynamo or other power "
    "block next to them or joined by Copper Wire.",
    "Vanilla furnaces no longer smelt iron, copper or gold ores: metals come from the machines.",
    "# Level 0",
    "With no research at all you can build a Charcoal Pit (8 dirt round a log) and burn logs into charcoal.",
]

CONTAINERS = [
    "# Glass Flasks",
    "Every liquid and gas item is a stoppered Glass Flask holding 111.1 units. Five glass make four flasks, with "
    "no research needed. A machine fills an empty flask from the top slot of its side panel for each liquid or gas "
    "it makes; with no flasks there, it stops and asks for them. When a machine, a recipe, a furnace or a research "
    "project uses a flask's contents, you get the empty flask back.",
    "# Steel Cylinders and the Compressor",
    "Gas Compression (level 4) unlocks Steel Cylinders and the Compressor. A cylinder holds 1000 units of one "
    "compressed gas: nine flasks' worth in one item. Fit a Compressor in one of a machine's two upgrade slots and "
    "put empty cylinders in the second slot of its side panel: the machine then fills cylinders with every gas it "
    "makes, a cylinder at a time. Liquids still go into flasks. A cylinder of a fuel gas burns like nine flasks.",
    "Machines take cylinders as input like any other item and hand the empty cylinder back.",
    "# Storage Tank",
    "Pneumatic Chemistry (level 3) unlocks the Storage Tank. It holds 16,000 units (144 flasks) of one liquid or "
    "gas. Use it holding a filled flask or cylinder to pour it in, holding an empty flask to draw one out, and with "
    "an empty hand to read its level. Hoppers work too: put filled or empty containers in from the top or sides, "
    "and take what comes out from the bottom. A comparator reads how full it is. Breaking it spills its contents.",
]

UPGRADES = [
    "# Upgrade slots",
    "Every machine has two upgrade slots at the bottom of its side panel. An upgrade works once its owner has "
    "researched it; two of the same count once. Upgrades marked fuel-fired do nothing in electric machines.",
    "- Compressor (Gas Compression, level 4): fills Steel Cylinders with the gases the machine makes.",
    "- Output Pusher (Automatic Mill, level 3): moves the machine's products into a chest, barrel or hopper next "
    "to it. It never feeds another machine directly.",
    "- Regenerator (Regenerative Furnace, level 4): a fuel-fired machine loses half as much heat, so it burns less "
    "fuel and heats up faster.",
    "- Refractory Lining (Basic Refractories, level 4): a fuel-fired machine can run 150 °C past its own limit, "
    "if its fuel and Bellows can reach that.",
    "- Gas Recovery Hood (By-Product Recovery, level 4): the liquids and gases the machine would vent (carbon "
    "dioxide, sulfur dioxide, hydrochloric acid and others) go into flasks or cylinders instead. Steam and mixed "
    "gases such as coal gas are still vented. Caught gas never stops the machine: with no container or room, it is "
    "vented after all.",
    "- Mechanical Stirrer (Electric Motor, level 4): works through the charge 50% faster. Its engine burns a quarter "
    "more fuel while the charge reacts, or its motor draws extra current in an electric machine.",
    "- Catalyst Bed (Catalyst Beds, level 5): reactions over a solid catalyst (iron, nickel, platinum, "
    "platinum-rhodium, vanadium pentoxide, cobalt-manganese bromide) run twice as fast: ammonia, the contact "
    "process, nitric acid, coal hydrogenation, reforming, nylon and more.",
    "- Conveyor Belt (Buna Rubber, level 5): an Output Pusher on a rubber belt; it moves finished products four "
    "at a time instead of one.",
    "- Foam Lagging (Styrene and Polystyrene, level 6): a machine whose limit is 300 °C or less loses 70% less heat.",
    "- Sterile Air Filter (Penicillin, level 6): the Fermentation Barrel, Vinegar Vat, Deep-Tank Fermenter and "
    "Bioreactor make 10% more product. The Deep-Tank Fermenter will not run without one.",
]

FARM = [
    "# Rendering and soap",
    "Rendering (level 1) unlocks the Soap Kettle. Meat, bones, eggs and milk go in; the kettle simmers them at 80 to "
    "150 °C. Tallow floats off, and the meat and bone left over come out as Protein Meal, which cows, sheep, goats, "
    "pigs and chickens eat and which composts well. Tallow with string makes Tallow Candles: light 12, smoky, and "
    "they burn away three days after you place them.",
    "Soap Boiling: put tallow or vegetable oil in the kettle and wood ash in the reagent slot for soft soap, with "
    "glycerol in flasks. Hard Soap (level 2) uses soda ash instead, or turns soft soap hard with salt.",
    "Use a bar of soap to wash: you are Clean for ten minutes, and Clean washes off the Hunger of rotten flesh and raw "
    "chicken. With dyed leather armour in your other hand, the soap scrubs the dye out instead. Soap with wool or a "
    "bed in the crafting grid washes it white.",
    "# Oil, leather and mash",
    "Oil Pressing (level 1): the Oil Press squeezes pumpkin, melon, wheat and cocoa seeds and sunflowers cold. "
    "Vegetable oil fills flasks; the Oil Cake left is cattle feed. Tallow and vegetable oil also burn as fuel.",
    "Pit Tanning (level 1): the Tanning Pit turns flesh and hides into leather with no crafting grid, slowly: four "
    "rotten flesh make one leather in about three game days. Put oak or spruce logs in the reagent slot for their "
    "tannin; the Spent Tan Bark left burns as fuel. Alum Making (level 2) gives potash alum, and two rabbit hides with "
    "one alum taw into leather in the crafting grid.",
    "Mashing (level 1): the Fermentation Barrel turns the starch of wheat and potatoes into sugar and then alcohol. "
    "Starch Sugar (level 3) does the first step faster with a little sulfuric acid in the Digestion Vat. Sweet Oil of "
    "Vitriol (level 2): ethanol distilled with sulfuric acid in the Alembic gives diethyl ether.",
    "# Fertilizer",
    "Superphosphate (level 3) dissolves bones or phosphate rock in sulfuric acid; Gas Liquor (level 3) washes ammonia "
    "out of the Coke Oven (keep flasks in its flask slot) and the Lead Chamber turns it into sulfate of ammonia. Use "
    "either fertilizer on farmland: for the next eight harvests the crop on it grows half again as fast and a ripe "
    "harvest gives one more of its crop.",
    "# Rubber",
    "Rubber trees grow wild in jungles (jungle, sparse jungle and bamboo jungle). Their leaves drop saplings, and the "
    "sapling grows anywhere, but a rubber tree gives latex only in a jungle. Vulcanization (level 3) unlocks the Tree "
    "Tap: fix it to the side of a rubber log and use it with an empty Glass Flask; it fills one flask every five "
    "minutes and holds four. Hoppers can feed it empty flasks from above and take latex from below. Outside the "
    "jungle it reports the wrong climate.",
    "In the Vulcanizer, latex with sulfur in the reagent slot cures into vulcanized rubber at 130 to 160 °C; without "
    "sulfur it only dries to raw rubber. Vulcanized rubber makes Sneakers (your steps make no vibrations, and magma "
    "and turtle eggs don't notice you) and Rubber Wheels: use them on a minecart and it runs 25% faster.",
]

TAR = [
    "# Coal tar",
    "Tar Distillation (level 3): with it researched, the Coke Oven catches the tar of the coal in flasks (keep empty "
    "flasks in its flask slot), about one flask per 13 coal. The Tar Still cuts coal tar into benzene, toluene, phenol "
    "(carbolic acid) and anthracene, and leaves coal tar pitch. Pitch with gravel and sand makes Tarmac, a road "
    "block as fast as Asphalt. Benzene, phenol, tar and pitch all burn as fuel. Nitrobenzene (level 3) nitrates "
    "benzene in the Digestion Vat, and Aniline (level 4) reduces it with iron.",
    "# Dyes",
    "Prussian Blue (level 2) unlocks the Dye Vat: rotten flesh or meat with green vitriol in the reagent slot makes "
    "the pigment, and the vat turns it into blue dye, 8 to an item. The coal tar dyes give 32 vanilla dyes to a "
    "flask: Fuchsine (aniline and white arsenic, magenta), Picric Acid (phenol and nitric acid in the Digestion Vat, "
    "yellow) and Alizarin (anthracene and caustic soda in the Autoclave, red). Put the dye in the Dye Vat to get "
    "vanilla dyes.",
    "# Infection and medicine",
    "A hit from a zombie, husk, drowned or zombie villager infects you 1 time in 200, and eating rotten flesh 1 time "
    "in 100; being Clean halves both. An Infection lasts a game day with Weakness and Hunger. Untreated, it clears "
    "up on its own 7 times in 10; the other times it turns to Sepsis, 1 damage every 10 seconds until you die. "
    "Carbolic Soap (Antiseptic Surgery: phenol and hard soap) and Aspirin (level 4: phenol in the Autoclave with "
    "soda ash, then acetic acid in the Digestion Vat) cure an Infection before it turns. Only Sulfa Drugs (level 5) and "
    "the antibiotics of level 6 cure Sepsis. Milk cures neither. The game rule periodicElementsInfection turns infection off.",
    "Aspirin also clears Nausea and Mining Fatigue and gives 10 seconds of Regeneration. Ether Anaesthetic (ether on "
    "wool), used on a zombie villager whose cure has begun, halves the time the cure takes.",
    "# Explosives",
    "Dynamite (level 4): glycerol from soap boiling, nitrated cold (5 to 25 °C) in the Digestion Vat, gives "
    "nitroglycerin; with sand and paper it makes dynamite, and four sticks with string make a Mining Charge. Light "
    "it with flint and steel or redstone: after 4 seconds it breaks every block in a 5x5x5 cube that is weaker "
    "than obsidian and every one of them drops. With mobGriefing off it only hurts what stands near. Cordite "
    "(level 4) kneads nitroglycerin with smokeless powder; Cordite Cartridges fire from the Breech-Loading Rifle 25% "
    "harder and without smoke.",
    "# Crops and pests",
    "Bordeaux Mixture (blue vitriol, quicklime and water) sprayed on a crop or berry bush keeps it from washing out "
    "or wilting in the dark, and berry bushes stop scratching. Insect Powder: the Oil Press grinds oxeye daisies "
    "to pyrethrum, and an Insect Spray hurts spiders, silverfish, endermites and bees 4 blocks ahead.",
    "# Vacuum Pump",
    "Vacuum Distillation (level 4) unlocks the Vacuum Pump upgrade: in an Alembic, Tar Still, Soap Kettle, "
    "Crystallizer, Salt Pan or Refinery Still, every reaction runs 100 °C cooler, so the machine heats up sooner and loses less heat.",
]

INDUSTRY = [
    "# Carbide and acetylene",
    "Calcium Carbide (level 5): quicklime with coke in the fuel slot of the Electric Arc Furnace fuses into carbide "
    "at 1800 °C and more. In the Digestion Vat with water in the reagent slot, carbide gives acetylene (keep "
    "flasks, or cylinders and a Compressor, in the vat) and leaves slaked lime.",
    "The Carbide Lamp (brass, glass, carbide and a water bucket) gives light 14 held in either hand or worn on the "
    "head, for half an hour. Use it with calcium carbide in your pack to refill it; an empty lamp stays whole.",
    "Oxy-Acetylene Welding: craft a Welding Torch from an acetylene and an oxygen cylinder, a steel ingot and a "
    "copper ingot. Hold a damaged metal tool or piece of armour in your other hand and use the torch: one ingot of "
    "its own metal from your pack mends a quarter of it, with no experience. The torch does 64 welds; craft it "
    "again with fresh cylinders to refill it.",
    "Nitrolime: carbide in the Lime Kiln at 1000 to 1150 °C with nitrogen in the reagent slot takes up the "
    "nitrogen as calcium cyanamide, a fertilizer.",
    "# Fertilizer from the air",
    "Urea Synthesis: ammonia in the High-Pressure Reactor at 170 to 200 °C, with carbon dioxide in the reagent "
    "slot (catch it from a Lime Kiln or Fermentation Barrel with a Gas Recovery Hood). Prilled Fertilizer unlocks "
    "the Prilling Tower, an electric machine: ammonia with nitric acid becomes ammonium nitrate, and urea with PK "
    "Blend (superphosphate and potash) becomes NPK Prills.",
    "Nitrolime, urea and ammonium nitrate work on farmland like superphosphate. NPK Prills make the crop grow twice "
    "as fast, for eight harvests, each giving one more of its crop.",
    "# Plastics and rubber",
    "Formaldehyde: methanol over silver in the Ostwald Converter at 600 to 700 °C. Bakelite: phenol with "
    "formaldehyde in the reagent slot, in the Autoclave at 150 to 200 °C. Four Bakelite make four Bakelite blocks, "
    "brown, or black, red, green, ivory or amber with a dye. Bakelite also cases a Geiger Counter in place of brass.",
    "Neoprene: acetylene with hydrochloric acid in the High-Pressure Reactor. A full neoprene Wetsuit gives Dolphin's "
    "Grace in water, and no piece of it lets the cold of powder snow in.",
    "Buna Rubber, with no jungle: ethanol over the catalyst in the Cracking Unit at 380 to 450 °C gives butadiene; "
    "benzene with ethylene in the reagent slot at 550 to 600 °C gives styrene; butadiene with styrene in the Digestion "
    "Vat at 40 to 60 °C gives Buna rubber. It makes Rubber Wheels and the Conveyor Belt upgrade.",
    "# Explosives",
    "TNT: toluene from the Tar Still, nitrated in the Digestion Vat. One TNT with three paper makes three blocks of "
    "vanilla TNT. Four TNT, a steel ingot and string make a Demolition Charge: lit like a Mining Charge, it breaks a "
    "3x3x3 cube of anything but bedrock, obsidian included, and drops it all.",
    "# Fermentation, fuel and medicine",
    "Acetone Fermentation: wheat or potatoes in the Fermentation Barrel, with calcite in the reagent slot, ferment to "
    "acetone, butanol and a little ethanol instead of sugar. Biodiesel: tallow or vegetable oil in the Soap Kettle at "
    "40 to 150 °C with methanol in the reagent slot. Biodiesel and butanol run the Diesel Generator and burn in "
    "furnaces.",
    "Sulfa Drugs: aniline with ammonium sulfate in the Digestion Vat gives sulfanilamide. Use it to cure an "
    "Infection at either stage, Sepsis included.",
]

LIFE = [
    "# Antibiotics",
    "Penicillin (level 6) unlocks the Deep-Tank Fermenter, an electric machine that runs only with a Sterile Air "
    "Filter. Glucose with milk in the reagent slot (for its lactose) grows penicillin at 20 to 37 °C; Streptomycin "
    "takes protein meal in the reagent slot instead. Either antibiotic cures an Infection or Sepsis and makes you "
    "immune to Infection for 20 minutes. Use penicillin on a zombie villager with Weakness and its cure begins, "
    "with no golden apple. The tank's Spent Mycelium is fertilizer.",
    "# Pesticides",
    "DDT: benzene with chlorine in the Digestion Vat. A DDT Duster used on the ground leaves it deadly to spiders, "
    "silverfish, endermites and bees for three game days; chickens on it lay half as often. Weed Killer (2,4-D, from "
    "phenol and chlorine) kills flowers, ferns, vines, bushes, saplings and leaves around the block, but spares "
    "grass and crops. Pre-Emergent (atrazine, from ammonia and chlorine with coke in the High-Pressure Reactor) "
    "keeps grass and mycelium from spreading onto the ground for ten game days. Each sprayer has 16 uses and works "
    "4 blocks round.",
    "# Plastics",
    "Styrene and Polystyrene: styrene in the High-Pressure Reactor. Eight polystyrene round a flask of methane make "
    "Foam Lagging; four polystyrene and methane make four Insulation blocks, and ice or snow touching Insulation "
    "never melts.",
    "# Explosives",
    "Plastic Explosive: formaldehyde and ammonia make hexamine, and hexamine nitrated cold makes RDX. Two RDX, a "
    "copper ingot, a steel ingot and string make a Shaped Charge: place it against a wall and light it, and it bores "
    "a 1x1 tunnel 16 blocks deep through anything but bedrock. ANFO: six ammonium nitrate, diesel, paper and string "
    "make an ANFO Charge that breaks a 9x9x9 cube of rock; three blocks in four drop.",
    "# Radiation countermeasures",
    "Potassium iodide (sodium iodide from kelp with potash salt in the Crystallizer) cuts the dose from fission "
    "products by 40% for a game day. A Prussian Blue Capsule (Prussian blue and protein meal) cuts it by a quarter. "
    "Amifostine (level 7) halves the harm of all radiation for an hour.",
    "# Biotechnology",
    "Recombinant DNA (level 7) unlocks the Bioreactor (it needs an integrated circuit). It ferments cellulose from "
    "logs, leaves or straw into ethanol, glucose into PLA, and protein with vegetable oil into the mRNA Vaccine, "
    "seven game days with no Infection. Its Biomass is fertilizer. PLA makes flower pots and item frames, and "
    "composts.",
    "Glyphosate: formaldehyde with phosphoric acid in the High-Pressure Reactor. Field Clear kills every plant round "
    "the block, crops too, except wheat grown from Tolerant Seeds (eight wheat seeds round a glyphosate). Tolerant "
    "wheat also grows a tenth faster.",
]

MINING = [
    "# Hammers and excavators",
    "Quarry Hammers (level 2) teaches the Sledgehammer (five ingots round a stick, over a stick) and the Excavator "
    "(three ingots and two sticks), in steel and every later tool metal. Each breaks a 3x3 square across the face "
    "you hit, at three tenths of the speed, and wears one point per block. Sneak to mine a single block. Only blocks "
    "the tool is right for and no harder than the one you aimed at break; nothing with an inventory or a machine "
    "ever does; the drops go straight into your pack.",
    "# Blasting",
    "Rock Blasting (level 2): four black powder, paper and string make two Blasting Charges. Lit with flint and "
    "steel or redstone, one breaks a 3x3x3 block of stone and drops all of it; dirt, wood and machines are spared.",
    "# Drills and bits",
    "@4 Every drill needs a bit: hold the bit in your other hand, sneak and use the drill. The bit sets what it can mine "
    "and how fast (tungsten steel 8.5, high-speed steel 10, cemented carbide 12, diamond 13) and wears one point a "
    "swing. Enchant the bit, not the drill; a fitted bit lends the drill its enchantments, all but Mending. Sneak "
    "and use with no bit in your other hand to change mode. A drill pays 1.5 kJ per point of hardness of each block: "
    "2.25 for stone, 4.5 for deepslate.",
    "Rock Drill (level 4): the Air Compressor, an electric machine, fills Steel Cylinders with Compressed Air, no "
    "Compressor upgrade needed. The Pneumatic Rock Drill burns one cylinder per 600 kJ (about 400 stone) and gives "
    "back the empty; it mines one block, a 1x3 tunnel or 3x3, and slows you while you hold it.",
    "# Battery tools",
    "Lead-Acid Battery (level 4): the Battery Pack holds 1,500 kJ. Join a Charging Bench to the wire and use the "
    "pack on it; it fills at up to 5 kJ a tick.",
    "Rotary Hammer (level 5) is the rock drill on a pack, with no slowness. The Chainsaw (level 5) fells a whole tree, "
    "up to 128 logs, for 3 kJ a log.",
    "The Heavy Mining Drill (level 6) adds a 5x5 mode and a vein mode that follows one ore up to 32 blocks; on a "
    "lead-acid pack it slows you.",
    "The Lithium-Ion Pack (level 7) holds 6,000 kJ, is drawn first and lifts that slowness. Ore-Seeking (level 7): in "
    "vein mode, use the Heavy Mining Drill on an ore to read the vein's size and elements before you dig.",
]

ISOTOPES = [
    "# Radiation",
    "Radioactive items give off a dose that builds up while you carry them or stand near them. An "
    "Electroscope, later a Geiger Counter, measures it. Lead walls, the Hot Cell and the Nuclear Reactor "
    "shield what they hold. Some samples decay on the game clock (one game day is a real day for them) and "
    "turn into their daughters in your inventory.",
    "# Blue glow",
    "The strongest emitters glow blue in the dark, as Cherenkov light does: radium and stronger in air, and in "
    "water also polonium, americium, actinium and pitchblende, brighter. A running Nuclear Reactor lights up its pool, and running accelerators glow too. "
    "The glow follows how concentrated the activity is, not how much you have; uranium never glows. Glowing items "
    "also glow in their inventory slot.",
    "# Natural isotopes",
    "Every stable element's tooltip lists its natural isotopes, largest first.",
    "# Ratios that change",
    "Uranium (U-235), plutonium (Pu-240), lithium (Li-6), boron (B-10), calcium (Ca-48) and hydrogen (H-2) "
    "items show their own ratio. An item at the natural ratio stacks with any other; an enriched or depleted "
    "one stacks only with items at the same ratio. Machines mix ratios by mass.",
    "- The Calutron (Calutron Enrichment) splits uranium, lithium or boron into a small enriched cut and "
    "depleted tails. Feed the cut back for more.",
    "- Uranium metal with 3% U-235 or more sinters into reactor fuel pellets.",
    "- Plutonium bred in the reactor is weapons grade (6% Pu-240); plutonium from spent fuel is reactor grade.",
    "- Lithium over 90% Li-6 in a reactor's slots turns into tritium, which lights Tritium Lights for ever.",
    "- Boron over 90% B-10 makes control rods: with one in its slots a reactor stops burning fuel while it is full.",
    "- Heavy water from the Enrichment Cascade (feed it ice or snow) lets natural uranium fuel rods burn.",
]


LEVEL_RE = re.compile(r"[Ll]evel (\d)")


def _tag(text, base, explicit=None):
    """Tech level of each paragraph: the guide shows it once the reader reaches the level before it.

    explicit[i] (if not None) wins; then an "@N " prefix (stripped); then the first "level N" in the paragraph; else
    the level of the paragraph before. A heading takes the level of the paragraph after it."""
    out_text, levels = [], []
    last = base
    for i, p in enumerate(text):
        lvl = None
        m = re.match(r"@(\d) ", p)
        if m:
            p, lvl = p[m.end():], int(m.group(1))
        if explicit is not None and explicit[i] is not None:
            lvl = explicit[i]
        heading = p.startswith("# ")
        if lvl is None and not heading:
            found = LEVEL_RE.search(p)
            lvl = int(found.group(1)) if found else last
        if not heading:
            last = lvl
        out_text.append(p)
        levels.append(lvl)
    nxt = None
    for i in range(len(levels) - 1, -1, -1):
        if levels[i] is None:
            levels[i] = nxt if nxt is not None else base
        nxt = levels[i]
    return out_text, levels


EN_T = {
    "start": "Getting Started", "group": "Level {n}", "research": "Research", "machines": "Machines",
    "reactions": "Reactions", "recipe": " Recipe: {r}.", "recipe_head": "Recipe: {r}.",
    "projects": "# Projects (★ = key)", "needs": " Needs {x}.", "samples": " Samples: {x}.", "unlocks": " Unlocks {x}.",
    "up_to": "{desc}. Up to {c} °C", "electric": ", electric.", "fuel": ", fuel-fired.",
    "amounts": "Amounts are per 100 units of feed.", "ores": "Ores", "ores_head": "# Ores",
    "ores_intro": "Ores generate in newly explored chunks. Silk Touch and Fortune work as on vanilla ores.",
    "ore_line": "{name}: {desc}. Drops {lo}-{hi} {drop}; needs a {tier} pickaxe. ", "found": "Found at {w}.",
    "where": "y {lo} to {hi} in {b}", "overworld": "the Overworld", "nether": "the Nether", "power": "Power",
    "power_head": "# Power",
    "power_intro": "Electric machines take current from the power blocks next to them, or joined to them by "
                   "Copper Wire (up to 64 blocks of wire) or Insulated Cable (four times as far).",
    "power_line": "{name} (level {lvl}, {project}): {desc}.", "isotopes": "Radiation and Isotopes",
    "containers": "Containers", "upgrades": "Machine Upgrades", "farm": "Farm and Fat", "tar": "Coal Tar and Medicine",
    "industry": "Industrial Organics", "life": "Life Sciences", "mining": "Mining Tools", "atlas": "Field Atlas", "compendium": "Tree Compendium", "plant_compendium": "Plant Compendium", "mineral_compendium": "Mineral Compendium", "trees": "Trees of the World", "shrubs": "Shrubs and Greenhouses", "crafting": "Crafting",
    "craft_head": "# Crafting",
    "craft_intro": "Shapeless recipes the mod adds (machine and block recipes are in the level chapters).",
    "shaped_head": "# Shaped recipes (rows top to bottom)", "or": " or ", "any": "any {x}", "empty": "empty",
    "reagent": "(reagent)", "carbon": " + {c} carbon", "nothing": "nothing kept", "slag": "; slag: ",
    "vents": "; vents ", "temp": ". {a} to {b} °C.",
}


class _Ctx:
    """One language: its lang map, templates, words for tags/biomes/tiers, and vanilla item names (None = English)."""

    def __init__(self, lang, mod, t, words=None, vanilla=None, decimal="."):
        self.lang, self.mod, self.t, self.words, self.vanilla, self.decimal = lang, mod, t, words or {}, vanilla, decimal

    def w(self, s):
        return self.words.get(s, s)

    def num(self, x):
        return f"{x:g}".replace(".", self.decimal)

    def name(self, ident):
        lang, mod = self.lang, self.mod
        if "|" in ident:
            return self.t["or"].join(self.name(i) for i in ident.split("|"))
        ident = ident.split("@")[0]
        for prefix in ("item:", "block:"):
            if ident.startswith(prefix):
                ident = ident[len(prefix):]
        if ident.startswith("#"):
            return self.t["any"].format(x=self.w(ident.split(":")[-1].replace("_", " ")))
        ns, _, path = ident.rpartition(":")
        if ns in ("", mod):
            for kind in ("item", "block", "research"):
                if f"{kind}.{mod}.{path}" in lang:
                    return lang[f"{kind}.{mod}.{path}"]
        if self.vanilla is not None and ns in ("", "minecraft"):
            for kind in ("item", "block"):
                if f"{kind}.minecraft.{path}" in self.vanilla:
                    return self.vanilla[f"{kind}.minecraft.{path}"]
        return path.replace("_", " ").title()

    def recipe(self, r):
        rows = []
        for row in r["pattern"]:
            rows.append(", ".join(self.name(r["key"][c]) if c != " " else self.t["empty"] for c in row))
        return " / ".join(rows)

    # Tags are not bound on the title screen, so the guide names items that stand for them.
    TAG_ITEMS = {"#minecraft:logs": "minecraft:oak_log+minecraft:spruce_log+minecraft:birch_log",
                 "#minecraft:logs_that_burn": "minecraft:oak_log+minecraft:spruce_log+minecraft:birch_log",
                 "#minecraft:planks": "minecraft:oak_planks+minecraft:spruce_planks+minecraft:birch_planks"}

    def grid(self, result, r, count=None):
        """3.3.0: a shaped recipe as a paragraph the guide draws as a 3x3 grid of icons:
        "@grid <result>*<count>|a,b,c/d,e,f/g,h,i", alternatives joined by "+" (the guide cycles them)."""
        def cell(c):
            if c == " ":
                return ""
            opts = []
            for i in r["key"][c].split("|"):
                i = i.split("@")[0]
                opts.append(self.TAG_ITEMS.get(i, i if ":" in i else f"minecraft:{i}"))
            return "+".join(opts)
        rows = "/".join(",".join(cell(c) for c in row) for row in r["pattern"])
        n = count if count is not None else r.get("count", 1)
        return f"@grid {result}*{n}|{rows}"

    def unlock(self, u):
        kind, _, what = u.partition(":")
        if kind == "reaction":
            return self.lang.get(f"reaction.{self.mod}.{what}", what)
        if kind == "upgrade":
            return self.w(what.replace("_", " "))
        return self.name(what)

    def reaction(self, r):
        t, n = self.t, self.num
        made = [f"{n(p['units'])} {self.name(p['id'])}" for p in r["products"] if not p["vented"] and not p["slag"]]
        slag = [self.name(p["id"]) for p in r["products"] if p["slag"]]
        gas = [self.name(p["id"]) for p in r["products"] if p["vented"]]
        text = f"{self.lang.get(f'reaction.{self.mod}.' + r['id'], r['id'])}: 100 {self.name(r['feed'])}"
        if r["reagents"]:
            text += " + " + ", ".join(f"{n(g['units'])} {self.name(g['id'])} {t['reagent']}" for g in r["reagents"])
        if r["carbon"]:
            text += t["carbon"].format(c=n(r["carbon"]))
        text += " -> " + (", ".join(made) or t["nothing"])
        if slag:
            text += t["slag"] + ", ".join(slag)
        if gas:
            text += t["vents"] + ", ".join(gas)
        text += t["temp"].format(a=n(r["tMin"]), b=n(r["tMax"]))
        return "- " + text


def write_guidebook(runtime_dir, lang, mod, research, reactions, machines, translations=None):
    """Writes guidebook.json (English) and guidebook_<code>.json for each {code: merged lang} in translations.

    Each language has tools/guide_<code>.py (hand-written chapters, templates T, WORDS, DECIMAL) and
    data/lang/vanilla_<code>.json (Minecraft's own item names)."""
    import importlib
    from pathlib import Path
    en = _build(_Ctx(lang, mod, EN_T), HAND, research, reactions, machines)
    _write(runtime_dir / "guidebook.json", en)
    for code, tr_lang in (translations or {}).items():
        g = importlib.import_module(f"guide_{code}")
        vanilla = json.loads((Path(__file__).resolve().parent.parent / "data" / "lang" / f"vanilla_{code}.json")
                             .read_text(encoding="utf-8"))
        import multiblock_i18n
        import shrubs_i18n
        import crops_i18n
        import geo_guide
        import exploration
        hand = {k: getattr(g, k, None) or (exploration.GUIDE_I18N[code] if k == "EXPLORATION" else None) or (__import__("fauna_guide").GUIDE_I18N[code] if k == "ANIMALS" else None) or multiblock_i18n.GUIDE[code].get(k) or shrubs_i18n.GUIDE[code].get(k)
                or crops_i18n.GUIDE[code].get(k) or geo_guide.I18N[code].get(k) for k in HAND}
        hand["CONTAINERS"] = hand["CONTAINERS"] + _VENT_GUIDE[code] + _TANKS_GUIDE[code] + _WATER_GUIDE[code]
        for k in HAND:
            assert len(hand[k]) == len(HAND[k]), (code, k, len(hand[k]), len(HAND[k]))
            assert [p.startswith("# ") for p in hand[k]] == [p.startswith("# ") for p in HAND[k]], (code, k, "headings")
        out = _build(_Ctx(tr_lang, mod, {**EN_T, **g.T, **multiblock_i18n.T[code], **shrubs_i18n.T[code], **crops_i18n.T[code], **geo_guide.T[code], **__import__("exploration").T_I18N[code], **__import__("fauna_guide").T_I18N[code]}, g.WORDS, vanilla, getattr(g, "DECIMAL", ",")), hand,
                     research, reactions, machines)
        # Same paragraphs, same levels: the levels come from the English text.
        for ce, cp in zip(en, out, strict=True):
            assert ce["id"] == cp["id"] and len(ce["text"]) == len(cp["text"]), (code, ce["id"])
            cp["levels"], cp["level"] = ce["levels"], ce["level"]
        _write(runtime_dir / f"guidebook_{code}.json", out)
    return sum(len(c["text"]) for c in en)


def _write(path, chapters):
    path.write_text(json.dumps({"chapters": chapters}, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")


from atlas import GUIDE as ATLAS
from multiblock import GUIDE_INTRO as STRUCT_INTRO, GUIDE_FURNACE as STRUCT_FURNACE, GUIDE_BATTERY as STRUCT_BATTERY, GUIDE_T as _MB_T
from multiblock import GUIDE_OUTLINE as STRUCT_OUTLINE, GUIDE_CHAMBER as STRUCT_CHAMBER, GUIDE_COLUMN as STRUCT_COLUMN
from multiblock import GUIDE_CASCADE as STRUCT_CASCADE, GUIDE_REACTOR as STRUCT_REACTOR
EN_T.update(_MB_T)
from compendium import GUIDE as COMPENDIUM
from compendium_plants import GUIDE as PLANT_COMPENDIUM
from compendium_minerals import GUIDE as MINERAL_COMPENDIUM
from trees import GUIDE as TREES, GUIDE_T as _TREES_T
from shrubs import GUIDE as SHRUBS, GUIDE_T as _SHRUBS_T
EN_T.update(_SHRUBS_T)
from crops import GUIDE as CROPS, GUIDE_T as _CROPS_T
from geo_guide import GUIDE as GEOLOGY, TITLE_EN as _GEO_TITLE
EN_T["geology"] = _GEO_TITLE
EN_T.update(_CROPS_T)
EN_T.update(_TREES_T)
EN_T["exploration"] = "Exploration"  # 3.47.0
EN_T["animals"] = "Animals of the World"  # 3.50.0
from vent import GUIDE as _VENT_GUIDE
CONTAINERS = CONTAINERS + _VENT_GUIDE["en_us"]  # 3.29.0 relief valve
from tanks import GUIDE as _TANKS_GUIDE, SHAPED as _TANKS_SHAPED
CONTAINERS = CONTAINERS + _TANKS_GUIDE["en_us"]  # 3.38.0 tanks and pipes
from water import GUIDE as _WATER_GUIDE
CONTAINERS = CONTAINERS + _WATER_GUIDE["en_us"]  # 3.45.0 salt water and fresh water


def _tank_grids(L, text):
    """3.38.0: "=grid <id>" paragraphs (after an optional "@N " level) become that block's recipe grid."""
    out = []
    for p in text:
        m = re.match(r"(@\d )?=grid (\w+)$", p)
        out.append((m.group(1) or "") + L.grid(f"{L.mod}:{m.group(2)}", _TANKS_SHAPED.get(m.group(2)) or __import__("machines").RECIPES[m.group(2)]) if m else p)
    return out

HAND = {"GEOLOGY": GEOLOGY, "TREES": TREES, "SHRUBS": SHRUBS, "CROPS": CROPS, "ATLAS": ATLAS, "COMPENDIUM": COMPENDIUM, "PLANT_COMPENDIUM": PLANT_COMPENDIUM, "MINERAL_COMPENDIUM": MINERAL_COMPENDIUM, "INTRO": INTRO, "CONTAINERS": CONTAINERS, "UPGRADES": UPGRADES, "FARM": FARM, "TAR": TAR,
        "INDUSTRY": INDUSTRY, "LIFE": LIFE, "MINING": MINING, "ISOTOPES": ISOTOPES, "EXPLORATION": __import__("exploration").GUIDE, "ANIMALS": __import__("fauna_guide").GUIDE,
        "STRUCT_INTRO": STRUCT_INTRO, "STRUCT_FURNACE": STRUCT_FURNACE, "STRUCT_BATTERY": STRUCT_BATTERY,
        "STRUCT_OUTLINE": STRUCT_OUTLINE, "STRUCT_CHAMBER": STRUCT_CHAMBER, "STRUCT_COLUMN": STRUCT_COLUMN,
        "STRUCT_CASCADE": STRUCT_CASCADE, "STRUCT_REACTOR": STRUCT_REACTOR}


def _structures(L, H):
    """3.3.0: the Structures chapter: hand-written text, then for each structure its gains, the recipe grids of its
    blocks, the layer-by-layer view and the 3D view ("@layers <kind>", "@3d <kind>": drawn by the guide)."""
    import multiblock as mb
    T, mod = L.t, L.mod

    def pct(x):
        return f"{round(x * 100)}"

    def num(x):
        return L.num(round(x, 1) if x < 10 else round(x))
    text, lv = [], []

    def add(paragraphs, level):
        for p in paragraphs:
            text.append(p)
            lv.append(level)
    add([p.split(" ", 1)[1] if p.startswith("@") else p for p in H["STRUCT_INTRO"]], 2)
    add([p.split(" ", 1)[1] if p.startswith("@") else p for p in H["STRUCT_OUTLINE"]], 2)
    add([p.split(" ", 1)[1] if p.startswith("@") else p for p in H["STRUCT_FURNACE"]], 2)
    for a, h in mb.FURNACE_ROWS:
        blocks, speed, loss = mb.furnace(a, h)
        add(["- " + T["gain_furnace"].format(a=a, h=h, blocks=blocks, speed=num(speed), loss=pct(loss))], 2)
    for bid in ("refractory_brick", "tuyere", "structure_port"):
        add([L.grid(f"{mod}:{bid}", mb.SHAPED[bid])], 2)
    add([T["assembly"], "@layers blast_furnace", T["view"], "@3d blast_furnace"], 2)
    add([p.split(" ", 1)[1] if p.startswith("@") else p for p in H["STRUCT_BATTERY"]], 3)
    for n in mb.BATTERY_ROWS:
        blocks, speed, loss = mb.battery(n)
        key = "gain_battery_gas" if n >= 4 else "gain_battery"
        add(["- " + T[key].format(n=n, blocks=blocks, speed=num(speed), loss=pct(loss))], 3)
    add([L.grid(f"{mod}:silica_brick", mb.SHAPED["silica_brick"])], 3)
    add([T["assembly_battery"], "@layers coke_battery", T["view"], "@3d coke_battery"], 3)
    # 3.8.0: stage 2.
    add([p.split(" ", 1)[1] if p.startswith("@") else p for p in H["STRUCT_CHAMBER"]], 3)
    for a, b, h, tower in mb.CHAMBER_ROWS:
        blocks, speed, loss = mb.chamber(a, b, h, tower)
        key = "gain_chamber_tower" if tower else "gain_chamber"
        add(["- " + T[key].format(a=a, b=b, h=h, blocks=blocks, speed=num(speed), loss=pct(loss))], 3)
    for bid in ("lead_sheet", "gay_lussac_tower"):
        add([L.grid(f"{mod}:{bid}", mb.SHAPED[bid])], 3)
    add([T["assembly_chamber"], "@layers lead_chamber", T["view"], "@3d lead_chamber"], 3)
    add([p.split(" ", 1)[1] if p.startswith("@") else p for p in H["STRUCT_COLUMN"]], 4)
    for w, h in mb.COLUMN_ROWS:
        trays, speed, loss, cut = mb.column(w, h)
        add(["- " + T["gain_column"].format(w=w, h=h, speed=num(speed), loss=pct(loss), cut=pct(cut))], 4)
    add([L.grid(f"{mod}:distillation_tray", mb.SHAPED["distillation_tray"])], 4)
    add([T["assembly_column"], "@layers distillation_column", T["view"], "@3d distillation_column"], 4)
    # 3.13.0: stage 3.
    add([p.split(" ", 1)[1] if p.startswith("@") else p for p in H["STRUCT_CASCADE"]], 6)
    for w, h, l in mb.CASCADE_ROWS:
        n, speed, loss, enriched = mb.cascade(w, h, l)
        add(["- " + T["gain_cascade"].format(n=n, speed=num(speed), loss=pct(loss), enriched=L.num(round(enriched, 1)))], 6)
    add([L.grid(f"{mod}:diffuser_stage", mb.SHAPED["diffuser_stage"])], 6)
    add([T["assembly_cascade"], "@layers enrichment_cascade", T["view"], "@3d enrichment_cascade"], 6)
    add([p.split(" ", 1)[1] if p.startswith("@") else p for p in H["STRUCT_REACTOR"]], 6)
    for a, b, h in mb.REACTOR_ROWS:
        ch, kj, life, ke, kn = mb.reactor(a, b, h)
        add(["- " + T["gain_reactor"].format(a=a, b=b, h=h, ch=ch, kj=round(kj), life=L.num(round(life, 2)),
                                             ke=L.num(round(ke, 2)), kn=L.num(round(kn, 2)))], 6)
    for bid in ("graphite_block", "fuel_channel", "reactor_shielding"):
        add([L.grid(f"{mod}:{bid}", mb.SHAPED[bid])], 6)
    add([T["assembly_reactor"], "@layers nuclear_reactor", T["view"], "@3d nuclear_reactor"], 6)
    return {"id": "structures", "title": T["structures"], "icon": f"{mod}:refractory_brick", "text": text, "explicit": lv}


def _build(L, H, research, reactions, machines):
    lang, mod, T = L.lang, L.mod, L.t
    from machines import POWER, RECIPES, SHAPELESS, EXTRA_SHAPELESS, STATIONS, MACHINE_NAMES
    from ores import ORES, FEATURES
    chapters = [{"id": "start", "title": T["start"], "icon": f"{mod}:field_guide", "text": H["INTRO"]}]
    # The level at which each item first appears: unlocked by research, or made by a reaction.
    first = {}

    def seen(ident, lvl):
        ident = ident.split("@")[0]
        if ident.startswith("item:"):
            ident = ident[5:]
        ident = ident.split(":")[-1]
        if lvl < first.get(ident, 99):
            first[ident] = lvl
    for p in research.values():
        for u in p["unlocks"]:
            if u.split(":", 1)[0] in ("item", "recipe", "machine"):
                seen(u.split(":", 1)[1], p["level"])
    for r in reactions:
        for pr in r["products"]:
            seen(pr["id"], max(1, r["level"]))

    def item_level(ident):
        if ident.startswith("minecraft:") or ident.startswith("#"):
            return 0
        if "|" in ident:
            return min(item_level(i) for i in ident.split("|"))
        return first.get(ident.split("@")[0].split(":")[-1], 1)

    def recipe_level(result, ingredients):
        return max([item_level(result)] + [item_level(i) for i in ingredients])

    stations = {lvl: sid for sid, (lvl, _, _) in STATIONS.items()}
    for lvl in range(1, 8):
        group = T["group"].format(n=lvl)
        sid = stations[lvl]
        research_text = [f"# {lang[f'block.{mod}.{sid}']}", lang[f"block.{mod}.{sid}.desc"].rstrip(".") + "."]
        if sid in RECIPES:
            research_text.append(L.grid(f"{mod}:{sid}", RECIPES[sid]))
        research_text.append(T["projects"])
        for p in research.values():
            if p["level"] != lvl:
                continue
            star = "★ " if p["key"] else ""
            research_text.append(f"# {star}{lang[f'research.{mod}.' + p['id']]} ({p['date']})")
            line = lang[f'research.{mod}.' + p['id'] + '.desc'].rstrip('.') + "."
            if p["requires"]:
                line += T["needs"].format(x=", ".join(lang[f"research.{mod}.{q}"] for q in p["requires"]))
            line += T["samples"].format(x=", ".join(f"{s['count']} {L.name(s['item'])}" for s in p["samples"]))
            if p["unlocks"]:
                line += T["unlocks"].format(x=", ".join(L.unlock(u) for u in p["unlocks"]))
            research_text.append(line)
        chapters.append({"id": f"level_{lvl}_research", "group": group, "title": T["research"], "icon": f"{mod}:{sid}",
                         "text": research_text})
        machine_text = []
        for m in (m for m in machines.values() if m["level"] == lvl):
            machine_text.append(f"# {lang[f'block.{mod}.' + m['id']]}")
            line = T["up_to"].format(desc=lang[f'block.{mod}.' + m['id'] + '.desc'].rstrip('.'), c=L.num(m['maxC']))
            line += T["electric"] if m["electric"] else T["fuel"]
            machine_text.append(line)
            if m["id"] in RECIPES:
                machine_text.append(L.grid(f"{mod}:{m['id']}", RECIPES[m["id"]]))
        for pid, (_, _, name, desc, cfg) in ((k, v) for k, v in POWER.items() if v[0] == lvl):
            machine_text.append(f"# {lang[f'block.{mod}.{pid}']}")
            line = lang[f"block.{mod}.{pid}.desc"].rstrip(".") + "."
            machine_text.append(line)
            if pid in RECIPES:
                machine_text.append(L.grid(f"{mod}:{pid}", RECIPES[pid]))
        if machine_text:
            chapters.append({"id": f"level_{lvl}_machines", "group": group, "title": T["machines"],
                             "icon": f"{mod}:{next((m['id'] for m in machines.values() if m['level'] == lvl), sid)}",
                             "text": machine_text})
        rx = [r for r in reactions if r["level"] == lvl or (lvl == 1 and r["level"] == 0)]
        if rx:
            text = [T["amounts"]]
            by_machine = {}
            for r in rx:
                by_machine.setdefault(r["machines"][0], []).append(r)
            for mid, rs in by_machine.items():
                text.append(f"# {lang.get(f'block.{mod}.{mid}', MACHINE_NAMES.get(mid, mid))}")
                text.extend(L.reaction(r) for r in rs)
            chapters.append({"id": f"level_{lvl}_reactions", "group": group, "title": T["reactions"],
                             "icon": "minecraft:blaze_powder", "text": text})

    ores = [T["ores_head"], T["ores_intro"]]
    ores_lv = [None, 1]
    for oid, v in ORES.items():
        name, host, _, drop, (lo, hi), _, tier, desc = v
        where = []
        for fid, (targets, size, count, height, biomes) in FEATURES.items():
            if any(o == oid for _, o in targets):
                lo_y = height["min_inclusive"]["absolute"]
                hi_y = height["max_inclusive"]["absolute"]
                b = T["overworld"] if biomes == "overworld" else T["nether"] if biomes == "nether" else \
                    T.get("biomes", "{b}").format(b=", ".join(L.w(x.lstrip("#").split(":")[-1].replace("is_", "").replace("_", " "))
                                                       for x in biomes.split("|")))
                where.append(T["where"].format(lo=lo_y, hi=hi_y, b=b))
        sampled = [p["level"] for p in research.values() if any(s["item"].split("@")[0] == v[3] for s in p["samples"])]
        ores_lv.append(min(sampled) if sampled else 1)
        ores.append(T["ore_line"].format(name=lang[f"block.{mod}.{oid}"], desc=lang[f"block.{mod}.{oid}.desc"].rstrip("."),
                                      lo=lo, hi=hi, drop=L.name(drop), tier=L.w(tier))
                    + (T["found"].format(w=T.get("sep", "; ").join(where)) if where else ""))
    chapters.append({"id": "ores", "title": T["ores"], "icon": f"{mod}:raw_pitchblende", "text": ores, "explicit": ores_lv})

    power = [T["power_head"], T["power_intro"]]
    power_lv = [None, min(v[0] for v in POWER.values())]
    for pid, (lvl, project, name, desc, cfg) in POWER.items():
        power_lv.append(lvl)
        power.append(T["power_line"].format(name=lang[f"block.{mod}.{pid}"], lvl=lvl, project=lang[f"research.{mod}." + project],
                                            desc=lang[f"block.{mod}.{pid}.desc"].rstrip(".")))
    chapters.append({"id": "power", "title": T["power"], "icon": f"{mod}:dynamo", "text": power, "explicit": power_lv})
    chapters.append({"id": "isotopes", "title": T["isotopes"], "icon": f"{mod}:uranium", "text": H["ISOTOPES"]})

    chapters.append({"id": "containers", "title": T["containers"], "icon": f"{mod}:glass_flask", "text": _tank_grids(L, H["CONTAINERS"])})
    chapters.append({"id": "upgrades", "title": T["upgrades"], "icon": f"{mod}:compressor", "text": H["UPGRADES"]})
    chapters.append({"id": "farm", "title": T["farm"], "icon": f"{mod}:tallow", "text": H["FARM"]})
    chapters.append({"id": "tar", "title": T["tar"], "icon": f"{mod}:coal_tar", "text": H["TAR"]})
    chapters.append({"id": "industry", "title": T["industry"], "icon": f"{mod}:bakelite", "text": H["INDUSTRY"]})
    chapters.append({"id": "life", "title": T["life"], "icon": f"{mod}:penicillin", "text": H["LIFE"]})
    chapters.append({"id": "mining", "title": T["mining"], "icon": f"{mod}:rotary_hammer", "text": H["MINING"]})
    chapters.append({"id": "exploration", "title": T["exploration"], "icon": f"{mod}:leather_backpack", "text": H["EXPLORATION"]})
    chapters.append({"id": "animals", "title": T["animals"], "icon": f"{mod}:animal_compendium", "text": H["ANIMALS"]})
    chapters.append({"id": "atlas", "title": T["atlas"], "icon": f"{mod}:field_atlas", "text": H["ATLAS"]})
    chapters.append({"id": "compendium", "title": T["compendium"], "icon": f"{mod}:tree_compendium",
                     "text": H["COMPENDIUM"]})
    chapters.append({"id": "plant_compendium", "title": T["plant_compendium"], "icon": f"{mod}:plant_compendium",
                     "text": H["PLANT_COMPENDIUM"]})
    chapters.append({"id": "mineral_compendium", "title": T["mineral_compendium"], "icon": f"{mod}:mineral_compendium",
                     "text": H["MINERAL_COMPENDIUM"]})
    # 2.8.0: trees of the world: the hand-written paragraphs, then a heading per biome and a line per tree.
    from trees import guide_paragraphs, load_species
    from pathlib import Path
    tree_text = H["TREES"] + guide_paragraphs(L, load_species(Path(__file__).resolve().parent.parent))
    chapters.append({"id": "trees", "title": T["trees"], "icon": f"{mod}:mahogany_sapling", "text": tree_text,
                     "explicit": [0] * len(tree_text)})
    # 3.16.0: shrubs and greenhouses; the greenhouse heater paragraph is level 2.
    from shrubs import guide_paragraphs as shrub_paragraphs
    shrub_text = H["SHRUBS"] + shrub_paragraphs(L)
    shrub_lv = [0] * len(shrub_text)
    shrub_lv[5] = 2
    # 3.22.0: the cooler paragraph is level 4 (Cold Greenhouse).
    import shrubs as _sh
    shrub_lv[_sh.GUIDE.index("# Cold greenhouses") + 1] = 4
    chapters.append({"id": "shrubs", "title": T["shrubs"], "icon": f"{mod}:tea_cutting", "text": shrub_text,
                     "explicit": shrub_lv})
    # 3.23.0: crops and their genes; the list of crops at the end is level 0.
    from crops import guide_paragraphs as crop_paragraphs
    crop_list = crop_paragraphs(L)
    chapters.append({"id": "crops", "title": T["crops"], "icon": f"{mod}:maize", "text": H["CROPS"] + crop_list,
                     "explicit": [None] * len(H["CROPS"]) + [0] * len(crop_list)})
    # 3.42.0: the Geological world type (stage 4c); all of it level 0.
    chapters.append({"id": "geology", "title": T["geology"], "icon": f"{mod}:gneiss", "text": H["GEOLOGY"],
                     "explicit": [0] * len(H["GEOLOGY"])})

    craft = [T["craft_head"], T["craft_intro"]]
    craft_lv = [None, 0]
    for rid, (ings, count) in SHAPELESS.items():
        craft_lv.append(recipe_level(rid, ings))
        craft.append(f"- {count} {L.name(rid)}: " + ", ".join(L.name(i) for i in ings))
    for rid, (ings, result, count) in EXTRA_SHAPELESS.items():
        craft_lv.append(max(recipe_level(result, ings), first.get(rid, 0)))
        craft.append(f"- {count} {L.name(result)}: " + ", ".join(L.name(i) for i in ings))
    from machines import EXTRA_SHAPED
    from multiblock import SHAPED as MB_SHAPED
    shaped = [rid for rid in RECIPES if rid not in machines and rid not in POWER and rid not in STATIONS and rid not in MB_SHAPED]
    if shaped:
        craft.append(T["shaped_head"])
        craft_lv.append(None)
        for rid in shaped:
            craft_lv.append(recipe_level(rid, list(RECIPES[rid]["key"].values())))
            craft.append(L.grid(f"{mod}:{rid}", RECIPES[rid]))
        for rid, (pattern, key, result, count) in EXTRA_SHAPED.items():
            craft_lv.append(max(recipe_level(result, list(key.values())), first.get(rid, 0)))
            craft.append(L.grid(result, {"pattern": pattern, "key": key}, count))
    chapters.append(_structures(L, H))
    chapters.append({"id": "crafting", "title": T["crafting"], "icon": "minecraft:crafting_table", "text": craft,
                     "explicit": craft_lv})

    # Each paragraph's level; a chapter's level is its lowest. The client shows what the reader has reached.
    base = {"structures": 2, "start": 0, "containers": 1, "upgrades": 3, "farm": 1, "tar": 3, "industry": 5, "life": 6, "mining": 2, "exploration": 1, "animals": 0, "atlas": 1, "compendium": 0, "plant_compendium": 0, "mineral_compendium": 0,
            "isotopes": 5, "crops": 0}
    for c in chapters:
        lvl_ch = re.match(r"level_(\d)_", c["id"])
        if lvl_ch:
            c["levels"] = [int(lvl_ch.group(1))] * len(c["text"])
        else:
            c["text"], c["levels"] = _tag(c["text"], base.get(c["id"], 1), c.pop("explicit", None))
        c.pop("explicit", None)
        c["level"] = min(c["levels"])

    return chapters
