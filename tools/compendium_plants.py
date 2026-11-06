"""Plant Compendium (3.5.0, stage 2 of the compendium plan): the Tree Compendium's twin for every plant that is not a tree.

Flowers, grasses, ferns, shrubs, crops, water plants and the plants of lush caves, each mapped to the real species it
stands for (or marked as a plant of this world only). Mushrooms, the wither rose and the Nether and End plants are left
out (fungi, or plants of other worlds); the sea pickle is an animal.
Translations: tools/compendium_plants_<code>.py (TITLE, GUIDE, UI, PLANTS, BIOMES), read by compendium_i18n.text.
Facts written from general botanical knowledge (2026-11); IUCN "NE" where the species was never assessed globally or
exists mainly in cultivation.
"""

ITEMS = {
    "plant_compendium": ("Plant Compendium",
                         "In your hotbar it shows the biome and names the plants you know; use it to read about them"),
}
SHAPELESS = {"plant_compendium": (["minecraft:book", "#minecraft:small_flowers"], 1)}

LANG = {
    "gui.periodic_elements.compendium.plant_title": "Plant Compendium",
    "gui.periodic_elements.compendium.unknown_plant": "Unknown plant",
    "gui.periodic_elements.compendium.new_plant": "New plant in your Compendium: %s",
    "gui.periodic_elements.compendium.plant_summary": "%s of %s plants and %s of %s biomes noted.",
    "gui.periodic_elements.compendium.plant_help": "Keep this book in your hotbar. Look at a plant for a moment, or "
                                                   "pick or harvest it, to note it; walk into a biome to note the "
                                                   "biome. Choose a biome on the left.",
    "gui.periodic_elements.compendium.plants_here": "Plants",
    "gui.periodic_elements.compendium.plant_this_world": "A plant of this world only: no real species matches it.",
}

GUIDE = [
    "# The Plant Compendium",
    "A book and any small flower make the Plant Compendium; no research is needed. It works like the Tree Compendium: "
    "keep it in your hotbar and the top-left corner names the biome and, once you know it, the plant you look at. "
    "Flowers, grasses, ferns, shrubs, crops, water plants and the plants of lush caves are all in it.",
    "To note a plant, look at it for a moment from up to 8 blocks away, or pick or harvest it, while the book is in your "
    "hotbar; plants under water are noted the same way. Use the book to read: biomes on the left, the plants of each "
    "one on the right.",
    "Compendium Notes copy both books at once: whoever reads them gains every tree, plant and biome in them.",
    "Species Plantarum (level 3): each plant shows its scientific name, its family and where it grows wild in the real "
    "world.",
    "Geography of Plants (level 3): each biome shows its climate, in this book as in the Tree Compendium.",
    "The Red List (level 6): each plant shows how threatened it is in the wild.",
]

# Water biomes only the Plant Compendium lists: id -> (mean temperature °C, rain mm a year, about).
BIOMES = {
    "river": (14, 900, "A river: fresh water running to the sea, with reeds along its banks and water plants in its "
                       "bed."),
    "ocean": (15, 1000, "Open temperate sea like the North Atlantic: cool water stirred by storms, with kelp and "
                        "seagrass near the coasts."),
    "cold_ocean": (6, 900, "Cold sea like the North Pacific: dark water rich in nutrients, where kelp grows into "
                           "forests."),
    "lukewarm_ocean": (21, 700, "Mild sea like the Mediterranean: clear, salty water with seagrass meadows on the sandy "
                                "bottom."),
    "warm_ocean": (27, 1800, "Tropical sea like the Caribbean: warm, clear water over coral reefs and seagrass beds."),
}

_FIELDS = ["plains", "sunflower_plains", "forest", "flower_forest", "birch_forest", "meadow"]
_SHRUBS = ["forest", "flower_forest", "birch_forest", "dark_forest"]
_FARMS = ["plains", "savanna", "desert", "taiga", "snowy_plains"]
_JUNGLE = ["jungle", "sparse_jungle", "bamboo_jungle"]


def _p(name, sci, fam, height, iucn, rng, about, blocks, biomes, products=(), icon=None):
    return {"name": name, "scientific": sci, "family": fam, "height": height, "iucn": iucn, "range": rng,
            "about": about, "blocks": list(blocks), "biomes": list(biomes), "products": list(products),
            "icon": icon or blocks[0]}


# id -> entry; blocks and products are vanilla ids without the namespace. Order: the book's order.
PLANTS = {
    # ---- Flowers ----
    "dandelion": _p("Dandelion", "Taraxacum officinale", "Asteraceae", 0.3, "NE",
                    "Europe and Asia; now on every continent but Antarctica",
                    "Its yellow head closes at night and ripens into a ball of seeds that ride the wind for kilometres. "
                    "Every part is edible, and its leaves were a spring salad long before lettuce.",
                    ["dandelion"], _FIELDS, ["yellow_dye"]),
    "poppy": _p("Poppy", "Papaver rhoeas", "Papaveraceae", 0.7, "LC", "Europe, North Africa and western Asia",
                "The red field poppy follows the plough: its seeds wait in the soil for decades and sprout when the "
                "ground is turned, which is why it covered the battlefields of Flanders after 1914.",
                ["poppy"], _FIELDS, ["red_dye"]),
    "blue_orchid": _p("Blue Orchid", "Vanda coerulea", "Orchidaceae", 0.8, "NE",
                      "Hill forests of Assam, Myanmar and northern Thailand",
                      "One of the very few truly blue orchids. In the wild it clings to tree trunks in cool hill "
                      "forests; collectors stripped whole valleys of it, and trade in wild plants is now banned.",
                      ["blue_orchid"], ["swamp"], ["light_blue_dye"]),
    "allium": _p("Allium", "Allium giganteum", "Amaryllidaceae", 1.5, "NE", "Mountains of Iran, Afghanistan and "
                 "Central Asia",
                 "An ornamental onion: a ball of hundreds of small purple flowers on a tall stalk. Crush a leaf and it "
                 "smells of onion, like its kin garlic and leek.",
                 ["allium"], ["flower_forest", "meadow"], ["magenta_dye"]),
    "azure_bluet": _p("Azure Bluet", "Houstonia caerulea", "Rubiaceae", 0.15, "NE", "Eastern North America",
                      "A tiny four-petalled flower that covers lawns and meadows with pale blue in spring. Small as it "
                      "is, it belongs to the coffee family.",
                      ["azure_bluet"], ["plains", "sunflower_plains", "flower_forest", "meadow"], ["light_gray_dye"]),
    "tulip": _p("Tulip", "Tulipa gesneriana", "Liliaceae", 0.6, "NE",
                "Mountain steppes of Turkey and Central Asia",
                "Brought from the Ottoman Empire to the Netherlands in the 1500s; in 1637 a single bulb could cost more "
                "than a house until the market crashed. The striped petals prized then were caused by a virus.",
                ["red_tulip", "orange_tulip", "white_tulip", "pink_tulip"], ["plains", "flower_forest"],
                ["red_dye", "orange_dye", "light_gray_dye", "pink_dye"]),
    "oxeye_daisy": _p("Oxeye Daisy", "Leucanthemum vulgare", "Asteraceae", 0.6, "NE", "Europe and temperate Asia",
                      "The white daisy of meadows and roadsides. Each flower head is really hundreds of tiny flowers: "
                      "yellow ones in the middle, white ones around the edge.",
                      ["oxeye_daisy"], ["plains", "sunflower_plains", "flower_forest", "meadow"], ["light_gray_dye"]),
    "cornflower": _p("Cornflower", "Centaurea cyanus", "Asteraceae", 0.8, "LC",
                     "Southeastern Europe and the Middle East, spread with grain across Europe",
                     "Once a weed of every wheat field, it almost vanished from Europe when farmers began cleaning "
                     "their seed and spraying herbicides. Its blue gave its name to a colour.",
                     ["cornflower"], ["plains", "sunflower_plains", "flower_forest", "meadow"], ["blue_dye"]),
    "lily_of_the_valley": _p("Lily of the Valley", "Convallaria majalis", "Asparagaceae", 0.25, "LC",
                             "Cool woodlands across Europe and Asia",
                             "Sweet-scented white bells in shady spring woods. Every part is poisonous: it holds heart "
                             "glycosides like the foxglove, which medicine uses for heart failure.",
                             ["lily_of_the_valley"], ["flower_forest"], ["white_dye"]),
    "sunflower": _p("Sunflower", "Helianthus annuus", "Asteraceae", 3, "NE",
                    "North America; first grown in Mexico about 4,000 years ago",
                    "Young heads follow the sun from east to west each day; open flowers face east for good. Its seeds "
                    "are pressed for one of the world's main cooking oils.",
                    ["sunflower"], ["sunflower_plains"], ["yellow_dye"]),
    "lilac": _p("Lilac", "Syringa vulgaris", "Oleaceae", 6, "NE", "Rocky hills of the Balkans",
                "A shrub of the olive family with clusters of fragrant purple flowers in late spring. It grows wild "
                "only on Balkan hillsides, but gardens have carried it around the world.",
                ["lilac"], _SHRUBS, ["magenta_dye"]),
    "rose_bush": _p("Rose Bush", "Rosa canina", "Rosaceae", 3, "LC", "Europe, northwest Africa and western Asia",
                    "The dog rose climbs through hedges on hooked thorns. Its red hips hold more vitamin C than "
                    "oranges; in wartime Britain children gathered them to make syrup.",
                    ["rose_bush"], _SHRUBS, ["red_dye"]),
    "peony": _p("Peony", "Paeonia lactiflora", "Paeoniaceae", 1, "NE", "Siberia, Mongolia and northern China",
                "Large pink flowers whose buds weep sugar that ants come to drink. Bred in China for more than a "
                "thousand years, it is called the king of flowers there.",
                ["peony"], _SHRUBS, ["pink_dye"]),
    "pitcher_plant": _p("Pitcher Plant", "Sarracenia purpurea", "Sarraceniaceae", 0.3, "LC",
                        "Bogs of eastern North America",
                        "A meat-eating plant of poor, wet soils: its leaves are jars of rainwater where insects drown "
                        "and are digested. In this world it grows only from ancient seeds that sniffers dig up.",
                        ["pitcher_plant", "pitcher_crop"], ["swamp"], ["cyan_dye"], icon="pitcher_plant"),
    # ---- Grasses, ferns and shrubs ----
    "grass": _p("Grass", "Poa pratensis", "Poaceae", 0.9, "NE", "Europe, northern Asia and North America",
                "Meadow grass, the grass of pastures and lawns. Grasses grow from the base of the leaf, not the tip, so "
                "they survive grazing and mowing that would kill most plants.",
                ["short_grass", "tall_grass"],
                ["plains", "sunflower_plains", "savanna", "savanna_plateau", "windswept_savanna", "forest",
                 "flower_forest", "birch_forest", "old_growth_birch_forest", "dark_forest", "jungle", "sparse_jungle",
                 "taiga", "old_growth_pine_taiga", "old_growth_spruce_taiga", "windswept_hills", "windswept_forest",
                 "meadow", "swamp"],
                ["wheat_seeds"]),
    "fern": _p("Fern", "Pteridium aquilinum", "Dennstaedtiaceae", 2, "LC",
               "Almost worldwide, on every continent but Antarctica",
               "Bracken is older than any flower: it has no flowers or seeds and spreads by spores and creeping "
               "underground stems. Its fronds are poisonous to cattle and horses.",
               ["fern", "large_fern"],
               ["taiga", "snowy_taiga", "old_growth_pine_taiga", "old_growth_spruce_taiga", "jungle", "sparse_jungle"]),
    "dead_bush": _p("Dead Bush", "Larrea tridentata", "Zygophyllaceae", 3, "NE",
                    "Deserts of the southwestern United States and northern Mexico",
                    "Not dead, only waiting: the creosote bush stops growing in drought and turns green after rain. A "
                    "ring of its clones in the Mojave, King Clone, is about 11,700 years old.",
                    ["dead_bush"], ["desert", "badlands", "wooded_badlands", "eroded_badlands"], ["stick"]),
    "sweet_berry_bush": _p("Sweet Berry Bush", "Vaccinium vitis-idaea", "Ericaceae", 0.4, "LC",
                           "Northern forests and tundra of Europe, Asia and North America",
                           "The lingonberry, a low evergreen shrub of the northern forest. Its tart red berries keep "
                           "all winter without spoiling, thanks to their own benzoic acid.",
                           ["sweet_berry_bush"],
                           ["taiga", "snowy_taiga", "old_growth_pine_taiga", "old_growth_spruce_taiga"],
                           ["sweet_berries"], icon="sweet_berries"),
    "bamboo": _p("Bamboo", "Phyllostachys edulis", "Poaceae", 28, "NE", "Central and southern China",
                 "A giant grass: a moso bamboo shoot can grow a metre in a day and reaches full height in two months. "
                 "A whole stand flowers once in many decades, all together, and then dies.",
                 ["bamboo", "bamboo_sapling"], ["bamboo_jungle", "jungle"], ["bamboo"]),
    "cactus": _p("Cactus", "Carnegiea gigantea", "Cactaceae", 12, "LC", "The Sonoran Desert of Arizona and Mexico",
                 "The saguaro stores water in a pleated trunk that swells after rain; a grown one can hold over a "
                 "tonne. It lives up to 150 years and grows its first arm at about 70.",
                 ["cactus"], ["desert", "badlands", "eroded_badlands"], ["green_dye"]),
    "sugar_cane": _p("Sugar Cane", "Saccharum officinarum", "Poaceae", 5, "NE",
                     "New Guinea, where it was first grown about 8,000 years ago",
                     "A giant grass whose stems are full of sweet juice; boiled down, it gives most of the world's "
                     "sugar. The plantations of the Americas were built on it and on the forced labour of enslaved "
                     "people.",
                     ["sugar_cane"], ["beach", "swamp", "desert", "jungle", "river"], ["sugar", "paper"]),
    "moss": _p("Moss", "Polytrichum commune", "Polytrichaceae", 0.4, "NE", "Damp ground across the temperate world",
               "Haircap moss, one of the tallest mosses. Mosses have no roots, flowers or seeds; they drink through "
               "their leaves and need a film of water for their sperm to swim to the eggs.",
               ["moss_block", "moss_carpet"], ["lush_caves"]),
    # ---- Crops ----
    "wheat": _p("Wheat", "Triticum aestivum", "Poaceae", 1.2, "NE",
                "The Fertile Crescent, from three wild grasses crossed about 9,000 years ago",
                "Bread wheat carries the whole genomes of three wild ancestors. It gives a fifth of the calories "
                "people eat, and its gluten lets dough stretch and hold the gas of rising bread.",
                ["wheat"], _FARMS, ["wheat", "wheat_seeds"], icon="wheat"),
    "carrot": _p("Carrot", "Daucus carota", "Apiaceae", 1, "NE",
                 "Wild across Europe and southwest Asia; first grown for its root in Afghanistan",
                 "The first carrots grown for food were purple or yellow; orange ones were bred in the Netherlands in "
                 "the 1600s. The orange is beta-carotene, which the body turns into vitamin A.",
                 ["carrots"], _FARMS, ["carrot"], icon="carrot"),
    "potato": _p("Potato", "Solanum tuberosum", "Solanaceae", 1, "NE", "The Andes of Peru and Bolivia",
                 "Andean farmers grew thousands of kinds; Europe planted only a few, and when blight struck in 1845 "
                 "Ireland lost a million people to famine. Green potatoes hold the poison solanine.",
                 ["potatoes"], _FARMS, ["potato", "poisonous_potato"], icon="potato"),
    "beetroot": _p("Beetroot", "Beta vulgaris", "Amaranthaceae", 1, "NE",
                   "Coasts of the Mediterranean and the Atlantic, as the wild sea beet",
                   "Beetroot, chard and sugar beet are one species bred three ways. The sugar beet, bred in Prussia in "
                   "the 1700s, now gives a fifth of the world's sugar.",
                   ["beetroots"], _FARMS, ["beetroot", "beetroot_seeds"], icon="beetroot"),
    "melon": _p("Melon", "Citrullus lanatus", "Cucurbitaceae", 0.5, "NE", "Northeast Africa",
                "The watermelon is about nine tenths water. Its seeds were left in Egyptian tombs, and desert "
                "travellers carried it as a flask.",
                ["melon", "melon_stem", "attached_melon_stem"], _JUNGLE, ["melon_slice", "melon_seeds"]),
    "pumpkin": _p("Pumpkin", "Cucurbita maxima", "Cucurbitaceae", 0.5, "NE", "Argentina and Uruguay",
                  "A creeping vine whose giant pumpkins have passed 1,200 kilograms, the heaviest fruits of any "
                  "plant. The peoples of the Americas grew squashes with maize and beans.",
                  ["pumpkin", "pumpkin_stem", "attached_pumpkin_stem"], ["plains", "forest", "taiga", "snowy_plains"],
                  ["pumpkin_seeds"]),
    "cocoa": _p("Cocoa", "Theobroma cacao", "Malvaceae", 8, "NE", "The upper Amazon basin",
                "A small tree of the rainforest shade whose pods grow straight from the trunk. The Maya and Aztecs "
                "drank its roasted, ground seeds; its name means food of the gods.",
                ["cocoa"], _JUNGLE, ["cocoa_beans"], icon="cocoa_beans"),
    # ---- Water plants ----
    "kelp": _p("Kelp", "Macrocystis pyrifera", "Laminariaceae", 45, "NE",
               "Cold coasts of the Pacific and the southern oceans",
               "Giant kelp is not a plant but a brown alga. Gas-filled bladders hold its fronds up, and it can grow "
               "half a metre a day into underwater forests full of fish and sea otters.",
               ["kelp", "kelp_plant"], ["ocean", "cold_ocean", "lukewarm_ocean"], ["kelp"], icon="kelp"),
    "seagrass": _p("Seagrass", "Zostera marina", "Zosteraceae", 1, "LC",
                   "Shallow coasts of the North Atlantic and North Pacific",
                   "A flowering plant that went back to the sea and even flowers under water. Its meadows shelter young "
                   "fish and bury carbon in the mud.",
                   ["seagrass", "tall_seagrass"], ["ocean", "lukewarm_ocean", "warm_ocean", "cold_ocean", "river"]),
    "lily_pad": _p("Lily Pad", "Nymphaea alba", "Nymphaeaceae", 0.1, "LC",
                   "Lakes and ponds of Europe, North Africa and the Middle East",
                   "The white water lily roots in the mud and floats its leaves on long stalks, with air channels "
                   "carrying oxygen down to the roots. Its leaves breathe through their upper side.",
                   ["lily_pad"], ["swamp", "mangrove_swamp"]),
    # ---- Plants of this world only ----
    "torchflower": _p("Torchflower", None, None, 0.6, None, None,
                      "An ancient flower that grew before the world began; sniffers dig its seeds out of the sand of "
                      "warm ocean ruins. It looks like a flame but gives no light.",
                      ["torchflower", "torchflower_crop"], ["warm_ocean"], ["orange_dye"]),
    "pink_petals": _p("Pink Petals", None, None, 0.1, None, None,
                      "Carpets of small pink flowers under cherry trees, up to four to a block. No real flower grows "
                      "like this: it looks like fallen cherry blossom that took root.",
                      ["pink_petals"], ["cherry_grove"], ["pink_dye"]),
    "spore_blossom": _p("Spore Blossom", None, None, 0.3, None, None,
                        "A flower that hangs from cave ceilings and drips a slow rain of green spores. Flowers make "
                        "pollen and seeds, not spores; this one needs no sunlight at all.",
                        ["spore_blossom"], ["lush_caves"]),
    "dripleaf": _p("Dripleaf", None, None, 2, None, None,
                   "Wide leaves on tall stalks that tip over a moment after something stands on them, like a trap "
                   "door. It grows in the clay and water of lush caves.",
                   ["big_dripleaf", "big_dripleaf_stem", "small_dripleaf"], ["lush_caves"], icon="big_dripleaf"),
    "glow_berries": _p("Glow Berries", None, None, 10, None, None,
                       "Vines that hang from cave roofs with berries that shine with their own light. Fireflies and "
                       "some fungi make light, but no real fruit does.",
                       ["cave_vines", "cave_vines_plant"], ["lush_caves"], ["glow_berries"], icon="glow_berries"),
    "glow_lichen": _p("Glow Lichen", None, None, 0.1, None, None,
                      "A lichen that glows on cave walls. Real lichens are partnerships of a fungus and an alga or a "
                      "bacterium; none of them glows.",
                      ["glow_lichen"], ["lush_caves"], ["glow_lichen"]),
    "vine": _p("Vines", None, None, 20, None, None,
               "Leafy vines with no flowers or fruit that hang from jungle trees and swamp oaks and grow downward. Real "
               "climbing plants grow up toward the light.",
               ["vine"], ["jungle", "swamp", "mangrove_swamp", "bamboo_jungle"]),
}


def runtime_entries():
    out = []
    for pid, p in PLANTS.items():
        out.append({"id": pid, "vanilla": True, "icon": f"minecraft:{p['icon']}", "scientific": p["scientific"],
                    "family": p["family"], "height": p["height"], "iucn": p["iucn"], "biomes": p["biomes"],
                    "blocks": [f"minecraft:{b}" for b in p["blocks"]],
                    "products": [f"minecraft:{x}" for x in p["products"]]})
    return out


def item_texture():
    """A blue cloth-bound book with a pressed flower on the cover."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cloth, cloth_d, paper = (52, 70, 120), (34, 46, 84), (236, 226, 196)
    for y in range(1, 15):
        for x in range(2, 14):
            px[x, y] = cloth_d + (255,)
    for y in range(1, 14):
        for x in range(3, 14):
            px[x, y] = cloth + (255,)
    for x in range(3, 14):
        px[x, 14] = paper + (255,)
    petal, centre, stem = (236, 196, 70), (190, 90, 40), (90, 160, 60)
    for x, y in [(8, 3), (7, 4), (9, 4), (8, 5), (6, 4), (10, 4), (8, 2)]:
        px[x, y] = petal + (255,)
    px[8, 4] = centre + (255,)
    for y in range(6, 11):
        px[8, y] = stem + (255,)
    px[7, 8] = stem + (255,)
    px[9, 9] = stem + (255,)
    return img
