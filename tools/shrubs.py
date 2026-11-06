"""Shrubs of the world (3.16.0): real shrub species for every land biome (plan "Plano: arbustos por bioma", approved by
Guilherme on 2026-11-04), each growing only in its own biomes or in a greenhouse.

A shrub is one block with four ages (com.periodicelements.shrubs.ShrubBlock). Ripe, a right-click picks its product and
the bush goes back to age 1; shears on a grown bush cut a cutting (the bush's item) to plant. Some products are seeds
that can be sown too. Breaking a bush gives the product if ripe and a stick or two.

Greenhouses (Guilherme): a closed room (entry by door or trapdoor only) under a roof all of glass, up to 32 x 32 x 32.
Unheated it is temperate; with a lit Greenhouse Heater (level 2) it is warm. Tree saplings follow the same rule.

This module writes the blocks' and items' assets, loot tables, worldgen features, biome tags, runtime shrubs.json, the
compositions and the English lang lines. Translations: tools/shrubs_i18n.py.
"""
import json
import random
import zlib

from PIL import Image

STAGE = 3
# One patch of a species every N chunks of its biomes, 3 to 8 bushes each.
PATCH_CHANCE = 6
WARM, TEMPERATE, COLD = "warm", "temperate", "cold"


def _s(stage, name, block, sci, family, height, iucn, biomes, soil, product, climate, leaf, accent, about, rng,
       sow=False, thorny=False, wood=False):
    return {"stage": stage, "name": name, "block": block, "scientific": sci, "family": family, "height": height,
            "iucn": iucn, "biomes": biomes.split(), "soil": soil, "product": product, "climate": climate, "leaf": leaf,
            "accent": accent, "about": about, "range": rng, "sow": sow, "thorny": thorny, "wood": wood}


SHRUBS = {
    # ---- Stage 1: tropical ----
    "guarana": _s(1, "Guarana", "Guarana Bush", "Paullinia cupana", "Sapindaceae", 3, "NE", "jungle sparse_jungle", "dirt",
                  "guarana_seeds", WARM, "3A6A2A", "C82A1A",
                  "A climbing shrub of the Amazon whose red fruits split open to show black seeds like eyes. The seeds hold "
                  "up to 4% caffeine, more than coffee beans.", "Amazon basin, in Brazil and Venezuela", sow=True),
    "annatto": _s(1, "Annatto", "Annatto Bush", "Bixa orellana", "Bixaceae", 5, "LC", "jungle sparse_jungle", "dirt",
                  "annatto_seeds", WARM, "3A6A30", "C83A2A",
                  "Its spiny red pods hold seeds coated in an orange-red pigment, bixin. Amazon peoples paint their bodies "
                  "with it, and it still colours cheese and butter.", "Tropical America, from Mexico to Brazil", sow=True),
    "ipecac": _s(1, "Ipecac", "Ipecac Bush", "Carapichea ipecacuanha", "Rubiaceae", 0.5, "NE", "jungle", "dirt",
                 "ipecac_root", WARM, "2A5A2A", "3A3A8A",
                 "A small shrub of the rainforest floor whose ringed roots hold emetine. Ipecac syrup made from them was "
                 "given for centuries to make people vomit up poisons.", "Brazil to Central America"),
    "jaborandi": _s(1, "Jaborandi", "Jaborandi Bush", "Pilocarpus microphyllus", "Rutaceae", 4, "NE", "sparse_jungle", "dirt",
                    "jaborandi_leaf", WARM, "3A7A3A", "6A2A4A",
                    "Its leaves hold pilocarpine, which makes the pupil of the eye shrink; it is still the eye drop for "
                    "glaucoma. Wild plants were nearly all picked out.", "Northeast Brazil, in Maranhão and Pará"),
    "malagueta": _s(1, "Malagueta Pepper", "Malagueta Pepper Bush", "Capsicum frutescens", "Solanaceae", 1.5, "NE",
                    "jungle bamboo_jungle", "dirt", "malagueta_pepper", WARM, "3A7A2A", "D82A1A",
                    "A small woody pepper whose tiny red fruits point up for birds, which cannot feel their heat. The burn "
                    "is capsaicin, made only in the pale ribs inside.", "Tropical America; now grown across the tropics",
                    sow=True),
    "tea": _s(1, "Tea", "Tea Bush", "Camellia sinensis", "Theaceae", 2, "NE", "bamboo_jungle cherry_grove", "dirt",
              "tea_leaf", TEMPERATE, "2A5A2A", "F0F0E0",
              "Every tea, green or black, comes from this one shrub: young leaves are picked, rolled and dried. Its "
              "caffeine is the same as coffee's.", "Southwest China and the eastern Himalaya; grown worldwide"),
    "jasmine": _s(1, "Arabian Jasmine", "Jasmine Bush", "Jasminum sambac", "Oleaceae", 2, "NE", "bamboo_jungle", "dirt",
                  "jasmine_flower", WARM, "3A6A3A", "F8F8F0",
                  "Its white flowers open at night; they are strung into garlands across South Asia and scent jasmine "
                  "tea. The scent oil is rich in linalool.", "South and Southeast Asia"),
    "holly_mangrove": _s(1, "Holly Mangrove", "Holly Mangrove", "Acanthus ilicifolius", "Acanthaceae", 2, "LC",
                         "mangrove_swamp", "mud", None, WARM, "3A6A4A", "9A7AC8",
                         "A spiny-leaved shrub of the mangrove edge that sweats the sea salt it takes up out through its "
                         "leaves.", "Coasts of India, Southeast Asia and northern Australia"),
    "pemphis": _s(1, "Pemphis", "Pemphis", "Pemphis acidula", "Lythraceae", 3, "LC", "mangrove_swamp beach", "sand", None,
                  WARM, "4A6A3A", "F0F0F0",
                  "A gnarled shrub of coral shores with wood so hard and heavy it sinks; islanders carved tools from it.",
                  "Coasts of the Indian and Pacific Oceans", wood=True),
    "naupaka": _s(1, "Beach Naupaka", "Naupaka", "Scaevola taccada", "Goodeniaceae", 3, "LC", "mangrove_swamp beach", "sand",
                  None, WARM, "5A8A3A", "F0F0E8",
                  "Its white flowers look torn in half. Its corky fruits float for months, so it has reached nearly every "
                  "tropical beach on its own.", "Tropical coasts of the Indian and Pacific Oceans"),
    # ---- Stage 1: arid ----
    "jatropha": _s(1, "Physic Nut", "Physic Nut Bush", "Jatropha curcas", "Euphorbiaceae", 5, "LC",
                   "savanna savanna_plateau windswept_savanna", "dirt", "jatropha_seed", WARM, "4A7A3A", "C8C040",
                   "A hedge shrub whose poisonous seeds are a third oil. Cape Verde shipped them to Lisbon for street "
                   "lamps; today the oil is made into biodiesel.", "Mexico and Central America; planted across the tropics",
                   sow=True),
    "roselle": _s(1, "Roselle", "Roselle Bush", "Hibiscus sabdariffa", "Malvaceae", 2, "NE",
                  "savanna savanna_plateau windswept_savanna", "dirt", "roselle_calyx", WARM, "3A6A3A", "B01A3A",
                  "The fleshy red cups under its flowers are boiled into the sour drink of West Africa and the Caribbean. "
                  "The sourness is hibiscus and citric acid.", "West Africa; grown across the tropics"),
    "rooibos": _s(1, "Rooibos", "Rooibos Bush", "Aspalathus linearis", "Fabaceae", 1.5, "NE", "savanna_plateau", "dirt",
                  "rooibos_leaf", WARM, "5A7A2A", "E0C040",
                  "A needle-leaved bush of the Cederberg mountains whose leaves turn red when bruised and fermented: the "
                  "caffeine-free red tea of South Africa.", "Cederberg, in South Africa's Western Cape"),
    "calotropis": _s(1, "Apple of Sodom", "Apple of Sodom Bush", "Calotropis procera", "Apocynaceae", 4, "LC",
                     "savanna windswept_savanna desert", "sand", "calotropis_floss", WARM, "8AA090", "D8C8E0",
                     "A grey desert shrub full of poisonous milky latex. Its puffy fruits are empty but for silky seed "
                     "floss, once used to stuff pillows.", "North Africa, Arabia and India"),
    "creosote": _s(1, "Creosote Bush", "Creosote Bush", "Larrea tridentata", "Zygophyllaceae", 2, "LC", "desert badlands",
                   "sand", "creosote_leaf", WARM, "4A6A2A", "E0D030",
                   "It smells of rain and tar; some clonal rings in the Mojave are over 11,000 years old. Its resinous "
                   "leaves are up to a tenth NDGA.", "Deserts of the southwestern United States and Mexico"),
    "jojoba": _s(1, "Jojoba", "Jojoba Bush", "Simmondsia chinensis", "Simmondsiaceae", 2, "LC", "desert", "sand",
                 "jojoba_seed", WARM, "6A8A5A", "6A4A2A",
                 "Its seeds are half a liquid wax, not an oil. It replaced sperm-whale oil in cosmetics and fine "
                 "lubricants after whaling was banned.", "Sonoran Desert of Arizona, California and Mexico", sow=True),
    "henna": _s(1, "Henna", "Henna Bush", "Lawsonia inermis", "Lythraceae", 4, "LC", "desert", "sand", "henna_leaf", WARM,
                "4A7A3A", "F0E8C8",
                "Its dried, ground leaves stain skin and hair red-brown: the dye, lawsone, binds to keratin. Used since "
                "ancient Egypt.", "North Africa to South Asia"),
    "saltbush": _s(1, "Mediterranean Saltbush", "Saltbush", "Atriplex halimus", "Amaranthaceae", 2, "LC",
                   "desert eroded_badlands", "sand", "saltbush_leaf", WARM, "A0B0A0", "9A9A7A",
                   "Its silvery leaves store the salt of the dry soils it grows in. Herders feed it to sheep and goats in "
                   "drought.", "Mediterranean basin and North Africa"),
    "guayule": _s(1, "Guayule", "Guayule Bush", "Parthenium argentatum", "Asteraceae", 0.8, "NE",
                  "badlands eroded_badlands wooded_badlands", "sand", "guayule_stems", WARM, "A0B0A8", "E0E0C0",
                  "A small grey bush that stores rubber in its bark. In the Second World War the United States planted it "
                  "when Japan cut off the rubber of Malaya.", "Chihuahuan Desert of Texas and Mexico"),
    "candelilla": _s(1, "Candelilla", "Candelilla", "Euphorbia antisyphilitica", "Euphorbiaceae", 1, "NE",
                     "badlands wooded_badlands", "sand", "candelilla_stems", WARM, "7A9A7A", "F0F0F0",
                     "Leafless waxy stems like candles. Boiled in soured water the wax floats off; it still glazes chewing "
                     "gum and lipstick.", "Chihuahuan Desert of Texas and Mexico"),
    "sagebrush": _s(1, "Big Sagebrush", "Sagebrush", "Artemisia tridentata", "Asteraceae", 2, "NE",
                    "badlands eroded_badlands wooded_badlands", "sand", "sagebrush_leaf", WARM, "9AA898", "C8C080",
                    "The silvery bush of the Great Basin; after rain the whole desert smells of its camphor. Sage grouse "
                    "live almost only on its leaves.", "Great Basin and western North America"),
}

# id -> (English name, composition per 100, colour, food (nutrition, saturation) or None, animal food tags, look)
PRODUCTS = {
    "guarana_seeds": ("Guarana Seeds", "cellulose:40;starch:30;water:12;tannin:8;protein:6;caffeine:4", "2A1A1A", None, (),
                      "seeds"),
    "annatto_seeds": ("Annatto Seeds", "cellulose:45;starch:20;protein:13;water:12;vegetable_oil:6;bixin:4", "C83A1A", None,
                      (), "seeds"),
    "ipecac_root": ("Ipecac Root", "cellulose:50;starch:30;water:15;lignin:3;emetine:2", "8A6A4A", None, (), "root"),
    "jaborandi_leaf": ("Jaborandi Leaves", "cellulose:45;water:40;protein:10;chlorophyll:3;tannin:1.3;pilocarpine:0.7",
                       "3A7A3A", None, (), "leaf"),
    "malagueta_pepper": ("Malagueta Pepper", "water:88;glucose:5;cellulose:4;protein:2;capsaicin:0.5;vegetable_oil:0.5",
                         "D82A1A", (1, 0.1), (), "pepper"),
    "tea_leaf": ("Tea Leaves", "cellulose:35;water:30;protein:15;tannin:12;glucose:3;caffeine:3;chlorophyll:2", "3A6A2A",
                 None, (), "leaf"),
    "jasmine_flower": ("Jasmine Flowers", "water:80;cellulose:12;glucose:6;protein:1.5;linalool:0.5", "F8F8F0", None, (),
                       "flower"),
    "jatropha_seed": ("Physic Nut Seeds", "vegetable_oil:35;cellulose:30;protein:20;water:8;starch:7", "3A3030", None, (),
                      "seeds"),
    "roselle_calyx": ("Roselle Calyces", "water:85;cellulose:6;glucose:4;citric_acid:3;protein:2", "B01A3A", (1, 0.2), (),
                      "pepper"),
    "rooibos_leaf": ("Rooibos Leaves", "cellulose:50;water:30;protein:8;glucose:8;tannin:4", "A04A2A", None, (), "leaf"),
    "calotropis_floss": ("Sodom Apple Floss", "cellulose:64;lignin:16;water:10;vegetable_oil:4;natural_rubber:6", "F4F0EC",
                         None, (), "floss"),
    "creosote_leaf": ("Creosote Leaves", "cellulose:45;water:30;lignin:10;ndga:10;protein:5", "4A6A2A", None, (), "leaf"),
    "jojoba_seed": ("Jojoba Seeds", "jojoba_wax:50;cellulose:20;protein:15;starch:9;water:6", "6A4A2A", None, (), "seeds"),
    "henna_leaf": ("Henna Leaves", "cellulose:50;water:30;protein:8;glucose:5.5;tannin:5;lawsone:1.5", "6A8A3A", None, (),
                   "leaf"),
    "saltbush_leaf": ("Saltbush Leaves", "water:60;cellulose:18;protein:12;sodium_chloride:8;potassium_chloride:2", "A0B0A0",
                      None, ("sheep_food", "goat_food", "cow_food"), "leaf"),
    "guayule_stems": ("Guayule Stems", "cellulose:45;lignin:20;water:20;natural_rubber:10;rosin:5", "A0A890", None, (),
                      "stems"),
    "candelilla_stems": ("Candelilla Stems", "cellulose:45;water:25;lignin:20;protein:6;vegetable_wax:4", "8AA08A", None, (),
                         "stems"),
    "sagebrush_leaf": ("Sagebrush Leaves", "cellulose:50;water:30;protein:10;lignin:8;camphor:2", "9AA898", None, (),
                       "leaf"),
}

HEATER = "greenhouse_heater"
HEATER_NAME = "Greenhouse Heater"
HEATER_DESC = "Load it with fuel: lit, it keeps the greenhouse it stands in warm enough for tropical and desert plants"
HEATER_RECIPE = {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["BBB", "BFB", "BIB"],
                 "key": {"B": {"item": "minecraft:brick"}, "F": {"item": "minecraft:furnace"}, "I": {"item": "minecraft:iron_ingot"}},
                 "result": {"id": f"periodic_elements:{HEATER}", "count": 1}}
# Ticks of fuel burned per tick: a heater burns slowly (a coal lasts 320 s, a log 60 s).
HEATER_BURN_DIVISOR = 4

LANG = {
    "gui.periodic_elements.shrub.biomes": "Grows only in: %s",
    "gui.periodic_elements.shrub.greenhouse_warm": "or in a heated greenhouse",
    "gui.periodic_elements.shrub.greenhouse_temperate": "or in a greenhouse",
    "gui.periodic_elements.shrub.wrong_place": "This plant grows only in: %s, or in a greenhouse warm enough for it",
    "gui.periodic_elements.greenhouse.heater": "Greenhouse Heater: %s s of fuel left",
    "gui.periodic_elements.greenhouse.heater_empty": "Greenhouse Heater: no fuel; use fuel on it to load it",
    "gui.periodic_elements.greenhouse.warm": "Heated greenhouse",
    "gui.periodic_elements.greenhouse.temperate": "Greenhouse (unheated)",
    "gui.periodic_elements.greenhouse.none": "Not in a greenhouse",
}

GUIDE = [
    "# Shrubs",
    "Shrubs grow wild in every land biome, three to five kinds in each, and like the trees each grows only in its own "
    "biomes. A bush takes about twenty minutes to ripen; bone meal speeds it up.",
    "Right-click a ripe bush to pick what it gives: leaves, seeds, fruit, roots or stems. It stays planted and ripens "
    "again. Shears on a grown bush cut a cutting to plant; some seeds, like peppers and jojoba, can be sown too. "
    "Breaking a bush gives a stick or two, and its harvest if it was ripe.",
    "# Greenhouses",
    "In a greenhouse a shrub or a tree sapling grows far from its biomes. A greenhouse is a closed room, up to 32 by 32 "
    "blocks and 32 high, entered only by a door or a trapdoor, with a roof all of glass or glass panes over every part "
    "of it. Shrubs need two blocks of height; a tree grows only if it fits whole under the roof (common trees 9 to 12 "
    "blocks, tall ones 15 to 18, giants about 26).",
    "An unheated greenhouse is temperate: tea and the shrubs and trees of temperate lands grow in it. A lit Greenhouse "
    "Heater on the floor or against a wall makes it warm, for tropical and desert plants too (Heated Greenhouse, level "
    "2). Load the heater by using fuel on it; it burns a coal in about five minutes.",
]
GUIDE_T = {"shrubs_harvest": "{x}", "shrubs_none": "for show only", "shrubs_wood": "hard wood (sticks)",
           "shrubs_warm": "warm greenhouse", "shrubs_temperate": "greenhouse"}

# ---- Stage 2 (3.22.0): temperate ----
# Four species of the plan are already in the game as vanilla plants or trees, so others of the same lands stand in:
# dog rose (= vanilla rose bush) -> sweet briar; elder (= the elder tree, 2.9.0) -> cornelian cherry;
# lilac (= vanilla lilac) -> mock orange; lingonberry (= vanilla sweet berry bush) -> bog bilberry (stage 3).
SHRUBS.update({
    "sweet_briar": _s(2, "Sweet Briar", "Sweet Briar", "Rosa rubiginosa", "Rosaceae", 3, "LC",
                      "plains sunflower_plains forest", "dirt", "rose_hips", TEMPERATE, "3A6A2A", "D8402A",
                      "A thorny wild rose whose leaves smell of apples after rain. Its scarlet hips hold more vitamin C "
                      "than oranges; in wartime Britain children picked them for syrup.", "Europe and western Asia",
                      thorny=True),
    "blackthorn": _s(2, "Blackthorn", "Blackthorn", "Prunus spinosa", "Rosaceae", 4, "LC", "plains sunflower_plains",
                     "dirt", "sloes", TEMPERATE, "2A4A2A", "3A3A6A",
                     "A dense hedge shrub with long thorns and white flowers before the leaves. Its blue-black sloes are "
                     "too sour to eat until frost, and flavour sloe gin.", "Europe and western Asia", thorny=True),
    "cornelian_cherry": _s(2, "Cornelian Cherry", "Cornelian Cherry", "Cornus mas", "Cornaceae", 6, "LC", "plains forest",
                           "dirt", "cornelian_cherries", TEMPERATE, "3A6A2A", "B01A2A",
                           "A dogwood that flowers yellow in late winter and bears glossy red, sour fruit. Its wood is so "
                           "dense that it sinks; the Greeks made spear shafts of it.", "Southern Europe and western Asia"),
    "raspberry": _s(2, "Raspberry", "Raspberry Bush", "Rubus idaeus", "Rosaceae", 2, "LC",
                    "forest birch_forest old_growth_birch_forest", "dirt", "raspberries", TEMPERATE, "3A6A2A", "D0204A",
                    "Its canes live two years: the first they grow, the second they fruit and die. The berry comes off "
                    "its core hollow, unlike a blackberry.", "Europe and northern Asia"),
    "blackberry": _s(2, "Blackberry", "Bramble", "Rubus fruticosus", "Rosaceae", 3, "NE",
                     "forest flower_forest dark_forest", "dirt", "blackberries", TEMPERATE, "2A5A2A", "2A1A3A",
                     "The bramble: arching thorny canes that root where their tips touch the ground and take over any "
                     "clearing. Its berries have fed people since the Stone Age.", "Europe", thorny=True),
    "blackcurrant": _s(2, "Blackcurrant", "Blackcurrant Bush", "Ribes nigrum", "Grossulariaceae", 1.5, "NE",
                       "forest birch_forest old_growth_birch_forest", "dirt", "blackcurrants", TEMPERATE, "3A6A2A", "1A1A2A",
                       "Its strong-smelling berries are rich in vitamin C; in the Second World War Britain gave blackcurrant "
                       "syrup to children free.", "Northern Europe and Asia"),
    "bilberry": _s(2, "Bilberry", "Bilberry Bush", "Vaccinium myrtillus", "Ericaceae", 0.5, "LC",
                   "birch_forest old_growth_birch_forest taiga old_growth_pine_taiga old_growth_spruce_taiga meadow grove",
                   "dirt", "bilberries", TEMPERATE, "3A7A3A", "2A2A5A",
                   "A knee-high shrub of acid woodland floors. Unlike the farmed blueberry, its berries are blue right "
                   "through and stain mouths and fingers.", "Europe and northern Asia"),
    "damask_rose": _s(2, "Damask Rose", "Damask Rose", "Rosa × damascena", "Rosaceae", 2, "NE", "flower_forest", "dirt",
                      "rose_petals", TEMPERATE, "3A6A2A", "E070A0",
                      "The rose of perfume: in Bulgaria's Valley of Roses about 3,000 kilograms of petals are distilled "
                      "for one kilogram of attar, rich in geraniol.", "A garden rose from Syria and Persia", thorny=True),
    "mock_orange": _s(2, "Mock Orange", "Mock Orange", "Philadelphus coronarius", "Hydrangeaceae", 3, "LC",
                      "flower_forest sunflower_plains", "dirt", None, TEMPERATE, "4A7A3A", "F8F8F0",
                      "In early summer it is covered in white flowers that smell of orange blossom. Its hollow stems were "
                      "once made into pipe stems.", "Southern Europe and the Caucasus"),
    "rhododendron": _s(2, "Pontic Rhododendron", "Rhododendron", "Rhododendron ponticum", "Ericaceae", 5, "LC",
                       "dark_forest", "dirt", "rhododendron_flowers", TEMPERATE, "2A4A2A", "B060C0",
                       "Every part is poisonous, even the honey made from its flowers: 'mad honey' felled Xenophon's "
                       "soldiers in 401 BC. Its purple flowers give a dye.", "Iberia, the Caucasus and the Black Sea coast"),
    "box": _s(2, "Box", "Box Bush", "Buxus sempervirens", "Buxaceae", 6, "LC", "dark_forest", "dirt", None, TEMPERATE,
              "2A4A2A", "8A9A4A",
              "A slow evergreen clipped into hedges since Roman gardens. Its yellow wood is the hardest in Europe, for "
              "chess pieces, rulers and woodcut blocks.", "Europe, North Africa and western Asia", wood=True),
    "butchers_broom": _s(2, "Butcher's Broom", "Butcher's Broom", "Ruscus aculeatus", "Asparagaceae", 0.8, "LC",
                         "dark_forest", "dirt", "butchers_broom_berries", TEMPERATE, "2A5A2A", "D82020",
                         "Its spiny 'leaves' are flattened stems, with a flower in the middle of each. Butchers tied the "
                         "stiff shoots into brooms to scrub their blocks.", "Europe, North Africa and the Middle East"),
    "wax_myrtle": _s(2, "Wax Myrtle", "Wax Myrtle", "Morella cerifera", "Myricaceae", 5, "LC", "swamp", "dirt",
                     "bayberries", TEMPERATE, "4A6A3A", "C0C8D0",
                     "Its grey berries are coated in wax. American colonists boiled them and skimmed the wax off for "
                     "bayberry candles, which burn with a clean scent.", "Eastern North America and the Caribbean"),
    "cranberry": _s(2, "Cranberry", "Cranberry", "Vaccinium macrocarpon", "Ericaceae", 0.2, "LC", "swamp", "dirt",
                    "cranberries", TEMPERATE, "3A5A2A", "C01A2A",
                    "A creeping shrub of bogs. The berries hold air pockets, so growers flood the fields and the berries "
                    "float up to be gathered.", "Bogs of northeastern North America"),
    "buttonbush": _s(2, "Buttonbush", "Buttonbush", "Cephalanthus occidentalis", "Rubiaceae", 3, "LC", "swamp", "dirt",
                     None, TEMPERATE, "3A6A2A", "F0F0E0",
                     "It grows with its feet in water and bears round white flower heads like pincushions, loved by bees "
                     "and butterflies.", "Eastern North America"),
    "japanese_camellia": _s(2, "Japanese Camellia", "Japanese Camellia", "Camellia japonica", "Theaceae", 6, "LC",
                            "cherry_grove", "dirt", "camellia_seeds", TEMPERATE, "1A4A2A", "D02A3A",
                            "The tea plant's cousin, with glossy leaves and red flowers that fall whole. Japanese women "
                            "dressed their hair with tsubaki oil pressed from its seeds.", "Japan, Korea and eastern China",
                            sow=True),
    "japanese_quince": _s(2, "Japanese Quince", "Japanese Quince", "Chaenomeles japonica", "Rosaceae", 1, "NE",
                          "cherry_grove", "dirt", "japanese_quinces", TEMPERATE, "3A6A2A", "E8C040",
                          "A low spiny shrub with orange-red flowers on bare twigs. Its small hard fruits are too sour "
                          "raw but fill a room with their scent.", "Japan"),
    "hydrangea": _s(2, "Hydrangea", "Hydrangea", "Hydrangea macrophylla", "Hydrangeaceae", 2, "NE",
                    "cherry_grove flower_forest", "dirt", None, TEMPERATE, "3A6A2A", "E080B0",
                    "Its flowers are pink in limy soil and blue in acid soil, where aluminium dissolves and colours them. "
                    "Water it with aluminium sulfate to turn it blue, or with lime to turn it pink.", "Japan"),
    "rosemary": _s(2, "Rosemary", "Rosemary", "Salvia rosmarinus", "Lamiaceae", 1.5, "LC",
                   "windswept_hills stony_shore", "rock", "rosemary_leaf", TEMPERATE, "4A6A4A", "8AA0E0",
                   "'Dew of the sea': a resinous shrub of sea cliffs. Greek students wore it while studying, to help the "
                   "memory. Its oil is mostly eucalyptol and camphor.", "Mediterranean coasts"),
    "lavender": _s(2, "Lavender", "Lavender", "Lavandula angustifolia", "Lamiaceae", 0.8, "LC",
                   "windswept_hills flower_forest", "rock", "lavender_flowers", TEMPERATE, "8AA08A", "8A6AC8",
                   "Its name comes from the Latin for washing: Romans scented their baths with it. The oil distilled from "
                   "its flowers is mostly linalool.", "Mountains of the western Mediterranean"),
    "myrtle": _s(2, "Myrtle", "Myrtle", "Myrtus communis", "Myrtaceae", 4, "LC", "windswept_hills stony_shore", "rock",
                 "myrtle_berries", TEMPERATE, "2A5A2A", "2A1A3A",
                 "The evergreen of Aphrodite, worn in bridal wreaths. In Sardinia its dark berries are steeped in spirit "
                 "for mirto liqueur.", "Mediterranean basin"),
    "caper": _s(2, "Caper", "Caper Bush", "Capparis spinosa", "Capparaceae", 1, "LC", "stony_shore", "rock", "capers",
                TEMPERATE, "4A7A4A", "F0E0F0",
                "A sprawling thorny shrub that grows out of old walls and cliffs. Capers are its flower buds, picked "
                "before they open and pickled in salt.", "Mediterranean to Central Asia", thorny=True),
    "rockrose": _s(2, "Gum Rockrose", "Rockrose", "Cistus ladanifer", "Cistaceae", 2, "LC",
                   "windswept_hills windswept_gravelly_hills", "rock", "labdanum", TEMPERATE, "2A4A2A", "F8F8F0",
                   "Its sticky leaves sweat a fragrant resin, labdanum. Cretan shepherds combed it from their goats' "
                   "beards; perfumers still use it.", "Iberia and northwest Africa"),
    "sumac": _s(2, "Sicilian Sumac", "Sumac", "Rhus coriaria", "Anacardiaceae", 3, "LC", "windswept_hills", "rock",
                "sumac_leaf", TEMPERATE, "4A7A2A", "8A1A2A",
                "Its leaves are a quarter tannin: the fine Moroccan leathers were tanned with them. Its sour red berries "
                "are the spice of the Middle East.", "Mediterranean to Iran"),
    "heather": _s(2, "Heather", "Heather", "Calluna vulgaris", "Ericaceae", 0.6, "LC",
                  "windswept_gravelly_hills windswept_forest taiga", "rock", "heather_flowers", TEMPERATE, "4A5A3A", "B070C0",
                  "The ling of moors, burnt in patches to keep it young for grouse and sheep. It made brooms, thatch and "
                  "bedding, dyed wool yellow, and brewed ale and tea.", "Europe"),
    "gorse": _s(2, "Gorse", "Gorse", "Ulex europaeus", "Fabaceae", 2.5, "LC", "windswept_gravelly_hills stony_shore",
                "rock", None, TEMPERATE, "3A5A2A", "F0D020",
                "Spines instead of leaves and yellow flowers smelling of coconut nearly all year. Its oily wood burns "
                "hot, the old fuel for bread ovens.", "Western Europe", thorny=True, wood=True),
    "broom": _s(2, "Broom", "Broom", "Cytisus scoparius", "Fabaceae", 2, "LC", "windswept_gravelly_hills windswept_forest",
                "rock", "broom_twigs", TEMPERATE, "3A6A2A", "F0D030",
                "Green whippy twigs that gave the broom its name. The Plantagenet kings took theirs from it, planta "
                "genista; the fibres were spun into coarse cloth.", "Western and central Europe"),
    "rugosa_rose": _s(2, "Beach Rose", "Beach Rose", "Rosa rugosa", "Rosaceae", 1.5, "LC", "beach snowy_beach", "sand",
                      "rose_hips", TEMPERATE, "3A6A2A", "D02A2A",
                      "A bristly rose of sand dunes that shrugs off salt spray and frost. Its big tomato-red hips are the "
                      "richest in vitamin C of all roses.", "Coasts of Japan, Korea, northeast China and the Russian Far East",
                      thorny=True),
    "beach_plum": _s(2, "Beach Plum", "Beach Plum", "Prunus maritima", "Rosaceae", 2, "NE", "beach", "sand", "beach_plums",
                     TEMPERATE, "4A7A3A", "5A2A6A",
                     "A shrub of the dunes of New England whose small dusky plums are gathered each autumn for jam.",
                     "Atlantic coast of North America"),
    # ---- Stage 3 (3.22.0): cold and mountain ----
    "bog_bilberry": _s(3, "Bog Bilberry", "Bog Bilberry", "Vaccinium uliginosum", "Ericaceae", 0.6, "LC",
                       "taiga snowy_taiga old_growth_pine_taiga old_growth_spruce_taiga", "dirt", "bog_bilberries", TEMPERATE,
                       "6A8A6A", "3A4A7A",
                       "A grey-leaved cousin of the bilberry from bogs and tundra. Its berries are white inside and were "
                       "said to give headaches, though the blame lies with the Labrador tea beside it.",
                       "Cool and cold regions all round the Northern Hemisphere"),
    "labrador_tea": _s(3, "Labrador Tea", "Labrador Tea", "Rhododendron tomentosum", "Ericaceae", 1, "LC",
                       "taiga snowy_taiga", "dirt", "labrador_tea_leaf", TEMPERATE, "3A5A2A", "F0F0F0",
                       "Its leathery leaves are rusty-woolly underneath. Northern peoples and fur traders brewed them into "
                       "tea; strong, it makes people drunk, and Scandinavians kept moths away with it.",
                       "Northern Europe, Asia and North America"),
    "crowberry": _s(3, "Crowberry", "Crowberry", "Empetrum nigrum", "Ericaceae", 0.3, "LC",
                    "snowy_taiga snowy_plains snowy_beach", "alpine", "crowberries", COLD, "2A4A2A", "1A1A1A",
                    "A mat of needle-like leaves on the tundra. Its black berries are watery and seedy, but they keep under "
                    "the snow all winter and feed birds, bears and the Sámi.", "Arctic and northern mountains"),
    "cloudberry": _s(3, "Cloudberry", "Cloudberry", "Rubus chamaemorus", "Rosaceae", 0.25, "LC",
                     "snowy_plains ice_spikes snowy_beach", "alpine", "cloudberries", COLD, "3A6A2A", "F0A030",
                     "A bramble only a hand high, with one amber berry per stem. In Finland and Norway it is the costliest "
                     "wild berry, and is on the Finnish two-euro coin.", "Arctic and boreal bogs"),
    "dwarf_birch": _s(3, "Dwarf Birch", "Dwarf Birch", "Betula nana", "Betulaceae", 1, "LC",
                      "snowy_plains ice_spikes snowy_slopes", "alpine", None, COLD, "4A6A2A", "C08030",
                      "A birch that creeps along the ground, out of the wind, with leaves the size of a coin. It is the "
                      "firewood of the tundra.", "Arctic tundra and northern mountains", wood=True),
    "bearberry": _s(3, "Bearberry", "Bearberry", "Arctostaphylos uva-ursi", "Ericaceae", 0.3, "LC",
                    "old_growth_pine_taiga grove snowy_slopes jagged_peaks stony_peaks", "alpine", "bearberry_leaf",
                    TEMPERATE, "2A5A2A", "C02A2A",
                    "A trailing evergreen whose mealy red berries bears love. Its leaves are rich in arbutin and tannin, "
                    "and were a medicine for the bladder and a tan for Russian leather.", "All round the North, on mountains"),
    "alpenrose": _s(3, "Alpenrose", "Alpenrose", "Rhododendron ferrugineum", "Ericaceae", 1, "LC",
                    "meadow grove jagged_peaks stony_peaks", "alpine", None, TEMPERATE, "2A4A2A", "D03070",
                    "The rose of the Alps, a rhododendron that colours whole slopes red in July above the tree line. Its "
                    "leaves are rusty underneath.", "Alps and Pyrenees"),
    "mountain_avens": _s(3, "Mountain Avens", "Mountain Avens", "Dryas octopetala", "Rosaceae", 0.1, "LC",
                         "snowy_slopes frozen_peaks jagged_peaks stony_peaks", "alpine", None, COLD, "3A5A2A", "F8F8E0",
                         "White flowers with eight petals that turn to follow the sun. Its pollen in lake mud marks the "
                         "cold spells at the end of the last ice age, the Younger Dryas.", "Arctic and high mountains"),
    "dwarf_willow": _s(3, "Dwarf Willow", "Dwarf Willow", "Salix herbacea", "Salicaceae", 0.05, "LC",
                       "ice_spikes snowy_slopes frozen_peaks", "alpine", "dwarf_willow_bark", COLD, "4A7A3A", "E0D080",
                       "Perhaps the smallest tree in the world: a willow a few centimetres tall whose trunk runs under the "
                       "ground. Its bark holds salicin like any willow's.", "Arctic and high mountains"),
    "arctic_bell_heather": _s(3, "Arctic Bell-heather", "Arctic Bell-heather", "Cassiope tetragona", "Ericaceae", 0.3, "LC",
                              "ice_spikes frozen_peaks", "alpine", None, COLD, "3A4A2A", "F8F8F8",
                              "Four-angled shoots of tiny scale leaves and white bells. Rich in resin, it burns even when "
                              "wet: the Inuit fuel of the high Arctic.", "High Arctic", wood=True),
    "gooseberry": _s(3, "Gooseberry", "Gooseberry Bush", "Ribes uva-crispa", "Grossulariaceae", 1.5, "LC",
                     "meadow windswept_forest", "dirt", "gooseberries", TEMPERATE, "4A7A3A", "A0C060",
                     "A spiny bush of mountain woods. English growers held gooseberry shows from the 1700s, racing to "
                     "grow the heaviest berry.", "Europe, the Caucasus and North Africa", thorny=True),
    "red_elderberry": _s(3, "Red Elderberry", "Red Elder", "Sambucus racemosa", "Adoxaceae", 4, "LC",
                         "meadow grove old_growth_spruce_taiga", "dirt", "red_elderberries", TEMPERATE, "3A6A2A", "E02A1A",
                         "Its bright red berries make you sick raw, but cooked they made jelly and wine for the peoples of "
                         "the Pacific coast.", "Cool forests and mountains of the Northern Hemisphere"),
})

# Stage 2 and 3 products. Food may carry an effect: (effect id, ticks, chance).
PRODUCTS.update({
    "rose_hips": ("Rose Hips", "water:60;cellulose:20;glucose:15;protein:2;tannin:2;vitamin_c:1", "D8402A", (2, 0.2), (),
                  "pepper"),
    "sloes": ("Sloes", "water:80;glucose:8;cellulose:8;tannin:3;protein:1", "3A3A6A", (1, 0.1), (), "berries"),
    "cornelian_cherries": ("Cornelian Cherries", "water:80;glucose:10;cellulose:6;citric_acid:2;tannin:1.5;protein:0.5",
                           "B01A2A", (2, 0.3), (), "berries"),
    "raspberries": ("Raspberries", "water:86;glucose:6;cellulose:6;protein:1;citric_acid:1", "D0204A", (2, 0.2), (),
                    "berries"),
    "blackberries": ("Blackberries", "water:88;glucose:5;cellulose:5;protein:1.5;citric_acid:0.5", "2A1A3A", (2, 0.2), (),
                     "berries"),
    "blackcurrants": ("Blackcurrants", "water:82;glucose:7;cellulose:7;citric_acid:2.5;protein:1.3;vitamin_c:0.2", "1A1A2A",
                      (2, 0.2), (), "berries"),
    "bilberries": ("Bilberries", "water:85;glucose:7;cellulose:6;protein:1;tannin:1", "2A2A5A", (2, 0.2), (), "berries"),
    "rose_petals": ("Damask Rose Petals", "water:80;cellulose:12;glucose:6;protein:1.5;geraniol:0.5", "E070A0", None, (),
                    "flower"),
    "rhododendron_flowers": ("Rhododendron Flowers", "water:85;cellulose:10;glucose:4;protein:1", "B060C0", None, (),
                             "flower"),
    "butchers_broom_berries": ("Butcher's Broom Berries", "water:70;cellulose:20;glucose:8;protein:2", "D82020", None, (),
                               "berries"),
    "bayberries": ("Bayberries", "cellulose:40;vegetable_wax:30;water:20;protein:5;starch:5", "C0C8D0", None, (), "berries"),
    "cranberries": ("Cranberries", "water:87;cellulose:5;glucose:4;citric_acid:3;protein:1", "C01A2A", (2, 0.1), (),
                    "berries"),
    "camellia_seeds": ("Camellia Seeds", "vegetable_oil:40;cellulose:30;protein:10;water:10;starch:10", "5A3A2A", None, (),
                       "seeds"),
    "japanese_quinces": ("Japanese Quinces", "water:83;glucose:6;cellulose:6;citric_acid:3;tannin:1.5;protein:0.5", "E8C040",
                         (2, 0.2), (), "berries"),
    "rosemary_leaf": ("Rosemary Sprigs", "cellulose:45;water:40;protein:6;lignin:6;eucalyptol:2;tannin:1", "4A6A4A", None, (),
                      "leaf"),
    "lavender_flowers": ("Lavender Flowers", "water:60;cellulose:30;glucose:6;protein:2;linalool:2", "8A6AC8", None, (),
                         "flower"),
    "myrtle_berries": ("Myrtle Berries", "water:70;glucose:15;cellulose:12;tannin:2;eucalyptol:1", "2A1A3A", (1, 0.1), (),
                       "berries"),
    "capers": ("Capers", "water:84;cellulose:7;protein:5;glucose:3;sodium_chloride:1", "5A7A3A", (1, 0.2), (), "berries"),
    "labdanum": ("Labdanum", "rosin:60;cellulose:20;water:10;lignin:10", "5A3A1A", None, (), "resin"),
    "sumac_leaf": ("Sumac Leaves", "cellulose:45;water:25;tannin:25;protein:5", "4A7A2A", None, (), "leaf"),
    "heather_flowers": ("Heather Flowers", "water:45;cellulose:40;glucose:8;protein:4;tannin:3", "B070C0", None, (),
                        "flower"),
    "broom_twigs": ("Broom Twigs", "cellulose:60;lignin:20;water:15;protein:5", "4A7A2A", None, (), "stems"),
    "beach_plums": ("Beach Plums", "water:85;glucose:9;cellulose:5;protein:1", "5A2A6A", (2, 0.3), (), "berries"),
    "bog_bilberries": ("Bog Bilberries", "water:86;glucose:7;cellulose:6;protein:1", "3A4A7A", (2, 0.2), (), "berries"),
    "labrador_tea_leaf": ("Labrador Tea Leaves", "cellulose:50;water:35;protein:8;tannin:5;lignin:2", "5A6A3A", None, (),
                          "leaf"),
    "crowberries": ("Crowberries", "water:85;cellulose:8;glucose:5;protein:1;tannin:1", "1A1A1A", (1, 0.1), (), "berries"),
    "cloudberries": ("Cloudberries", "water:87;glucose:6;cellulose:5;citric_acid:1.3;protein:0.5;vitamin_c:0.2", "F0A030",
                     (3, 0.4), (), "berries"),
    "bearberry_leaf": ("Bearberry Leaves", "cellulose:45;water:30;tannin:12;arbutin:8;protein:5", "2A5A2A", None, (), "leaf"),
    "dwarf_willow_bark": ("Dwarf Willow Bark", "cellulose:50;lignin:25;water:15;tannin:5;salicin:5", "8A6A4A", None, (),
                          "stems"),
    "gooseberries": ("Gooseberries", "water:88;glucose:6;cellulose:4;citric_acid:1.5;protein:0.5", "A0C060", (2, 0.2), (),
                     "berries"),
    "red_elderberries": ("Red Elderberries", "water:80;glucose:8;cellulose:10;protein:2", "E02A1A", (1, 0.1), (), "berries"),
})
# Raw red elderberries upset the stomach.
FOOD_EFFECTS = {"red_elderberries": ("minecraft:nausea", 200, 0.8)}
# Products that turn into something by hand (shapeless crafting): product -> (result, count, how many products).
CRAFTS = {"rhododendron_flowers": ("minecraft:magenta_dye", 1, 1), "butchers_broom_berries": ("minecraft:red_dye", 1, 1),
          "heather_flowers": ("minecraft:yellow_dye", 1, 2), "broom_twigs": ("minecraft:string", 1, 3)}
# Bushes bees treat as flowers.
BEE_FLOWERS = ("buttonbush", "lavender", "heather", "rosemary", "mock_orange")

# Teas (Guilherme, 2026-11-04): two leaves or flowers and a water bottle. Each gives a mild effect tied to the plant and
# cures one bad effect, as milk cures all of them.
# id -> (English name, source product, effect, seconds, cured effect, colour, composition)
TEAS = {
    "black_tea": ("Black Tea", "tea_leaf", "haste", 45, "mining_fatigue", "6A2A10", "water:97;tannin:1.5;glucose:1;caffeine:0.5"),
    "rooibos_tea": ("Rooibos Tea", "rooibos_leaf", "resistance", 30, "hunger", "A04A2A", "water:97.5;glucose:1.5;tannin:1"),
    "hibiscus_tea": ("Hibiscus Tea", "roselle_calyx", "fire_resistance", 30, "weakness", "B01A3A",
                     "water:96;glucose:2;citric_acid:2"),
    "rosehip_tea": ("Rose Hip Tea", "rose_hips", "absorption", 60, "poison", "D8603A",
                    "water:96.5;glucose:2.5;tannin:0.8;vitamin_c:0.2"),
    "lavender_tea": ("Lavender Tea", "lavender_flowers", "regeneration", 10, "nausea", "A08AD0",
                     "water:98;glucose:1.8;linalool:0.2"),
    "rosemary_tea": ("Rosemary Tea", "rosemary_leaf", "night_vision", 45, "blindness", "8AA060",
                     "water:98;tannin:1;glucose:0.8;eucalyptol:0.2"),
    "heather_tea": ("Heather Tea", "heather_flowers", "speed", 30, "slowness", "B08A6A", "water:98;glucose:1;tannin:1"),
    "labrador_tea": ("Brewed Labrador Tea", "labrador_tea_leaf", "health_boost", 60, "wither", "8A6A2A", "water:98;tannin:1.5;glucose:0.5"),
}

COOLER = "greenhouse_cooler"
COOLER_NAME = "Greenhouse Cooler"
COOLER_DESC = ("Cools the greenhouse it stands in for arctic and alpine plants; it melts ice put in it, or runs on "
               "current from a wire")
COOLER_RECIPE = {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["III", "CRC", "III"],
                 "key": {"I": {"item": "minecraft:iron_ingot"}, "C": {"item": "minecraft:copper_ingot"},
                         "R": {"item": "minecraft:redstone"}},
                 "result": {"id": f"periodic_elements:{COOLER}", "count": 1}}
# Ticks of cold each item gives, and the current (kJ per tick) the cooler draws when it has no ice.
COOLER_ICE = {"minecraft:snow_block": 600, "minecraft:ice": 2400, "minecraft:packed_ice": 21600,
              "minecraft:blue_ice": 72000}
COOLER_POWER = 1.0
# Hydrangea colours: what turns it blue (aluminium) and pink (lime).
HYDRANGEA_BLUE = ("periodic_elements:aluminium_sulfate", "periodic_elements:potassium_alum")
HYDRANGEA_PINK = ("periodic_elements:calcium_oxide", "periodic_elements:calcium_hydroxide",
                  "periodic_elements:calcium_carbonate")

LANG23 = {
    "gui.periodic_elements.shrub.greenhouse_cold": "or in a cooled greenhouse",
    "gui.periodic_elements.greenhouse.cooler": "Greenhouse Cooler: %s s of ice left",
    "gui.periodic_elements.greenhouse.cooler_power": "Greenhouse Cooler: running on current",
    "gui.periodic_elements.greenhouse.cooler_empty": "Greenhouse Cooler: no ice and no current; use ice on it or wire it to a dynamo",
    "gui.periodic_elements.greenhouse.cold": "Cooled greenhouse",
    "gui.periodic_elements.tea.cures": "Cures %s",
    "gui.periodic_elements.hydrangea.blue": "The hydrangea will flower blue",
    "gui.periodic_elements.hydrangea.pink": "The hydrangea will flower pink",
}
LANG.update(LANG23)

GUIDE23 = [
    "# Thorns",
    "Brambles, blackthorn, roses, gorse, capers and gooseberries prick whoever walks through them, a little at a time, "
    "like a sweet berry bush. Foxes and bees go through unharmed.",
    "# Teas",
    "Two leaves or flowers and a water bottle make a tea. Each gives a mild effect for a short while and cures one bad "
    "effect, as milk cures them all: black tea gives haste and cures mining fatigue; rooibos, resistance (hunger); "
    "hibiscus, fire resistance (weakness); rose hip, absorption (poison); lavender, regeneration (nausea); rosemary, "
    "night vision (blindness); heather, speed (slowness); Labrador tea, health boost (wither).",
    "# Hydrangea",
    "The hydrangea flowers pink in limy soil and blue in acid soil, where aluminium dissolves into it. Use aluminium "
    "sulfate or alum on it to make it flower blue, and quicklime, slaked lime or chalk to make it pink again.",
    "# Cold greenhouses",
    "Arctic and alpine plants need a cooled greenhouse. A running Greenhouse Cooler on the floor or in a wall makes it "
    "cold (Cold Greenhouse, level 4). It melts ice put in it: a snow block lasts 30 seconds, ice 2 minutes, packed ice "
    "18 minutes, blue ice an hour. With no ice it runs on current from a copper wire, 20 kilowatts. Temperate plants "
    "grow in a cold greenhouse too; a heater and a cooler in the same room cancel out.",
]
GUIDE += GUIDE23
GUIDE_T["shrubs_cold"] = "cooled greenhouse"


def species(stage=STAGE):
    return {k: v for k, v in SHRUBS.items() if v["stage"] <= stage}


# ---------- textures ----------

def _rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def _shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def _put(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.load()[x, y] = tuple(c) + (255,)


def bush_texture(sid, age, accent=None):
    sp = SHRUBS[sid]
    leaf, accent = _rgb(sp["leaf"]), _rgb(accent or sp["accent"])
    stem = (90, 70, 45)
    rnd = random.Random(zlib.crc32(f"{sid}{age}".encode()))
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    top = [12, 8, 4, 3][age]
    half = [2, 4, 6, 7][age]
    for y in range(top + 3, 16):
        _put(img, 7, y, stem)
        if age >= 2 and y > 11:
            _put(img, 8 + (y % 2), y, stem)
    for y in range(top, 15 if age else 15):
        w = half * (1 - abs((y - (top + 15) / 2) / max(1, (15 - top) / 2)) ** 2) + 1
        for x in range(16):
            if abs(x - 7.5) < w and rnd.random() < 0.82:
                _put(img, x, y, leaf if rnd.random() < 0.65 else _shade(leaf, 0.75 if rnd.random() < 0.5 else 1.2))
    if age == 3 and sid == "hydrangea":
        for cx, cy in ((5, top + 3), (10, top + 2), (8, top + 6), (4, top + 8), (11, top + 7)):
            for dy in (-1, 0, 1):
                for dx in (-1, 0, 1):
                    if abs(dx) + abs(dy) < 2 or rnd.random() < 0.5:
                        _put(img, cx + dx, cy + dy, accent if rnd.random() < 0.7 else _shade(accent, 1.15))
    elif age == 3 and sp["product"]:
        for _ in range(7):
            x, y = rnd.randint(3, 12), rnd.randint(top + 1, 12)
            _put(img, x, y, accent)
            _put(img, x + 1, y, _shade(accent, 0.8))
    elif age == 3:
        for _ in range(5):
            _put(img, rnd.randint(3, 12), rnd.randint(top + 1, 12), accent)
    return img


def cutting_texture(sid):
    sp = SHRUBS[sid]
    leaf = _rgb(sp["leaf"])
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for i in range(10):
        _put(img, 4 + i // 2 + i % 2, 14 - i, (110, 85, 55))
    for x, y in ((6, 6), (7, 5), (9, 5), (10, 4), (8, 3), (10, 2), (11, 3), (5, 8), (4, 7), (9, 7), (10, 7)):
        _put(img, x, y, leaf)
    _put(img, 8, 4, _shade(leaf, 0.75))
    return img


def product_texture(pid):
    name, _, color, _, _, look = PRODUCTS[pid]
    c = _rgb(color)
    hi, lo = _shade(c, 1.3), _shade(c, 0.7)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = random.Random(zlib.crc32(pid.encode()))
    if look == "seeds":
        for sx, sy in ((5, 6), (9, 5), (7, 9), (10, 10), (5, 11), (11, 7)):
            for y in range(sy - 1, sy + 2):
                for x in range(sx - 1, sx + 2):
                    if abs(x - sx) + abs(y - sy) <= 1:
                        _put(img, x, y, hi if (x < sx or y < sy) else c)
    elif look == "leaf":
        for y in range(2, 15):
            w = 4 * (1 - abs((y - 8) / 6.5) ** 2)
            for x in range(16):
                if abs(x - 7.5) < w:
                    _put(img, x, y, lo if abs(x - 7.5) < 0.6 else (c if (x + y) % 5 else hi))
        _put(img, 7, 15, (90, 70, 45))
    elif look == "root":
        for i in range(13):
            x, y = 3 + i, 4 + int(i * 0.6)
            for d in (-1, 0, 1):
                _put(img, x, y + d, lo if i % 3 == 0 else c)
    elif look == "pepper":
        for i in range(10):
            x, y = 4 + i, 12 - i
            r = 1.8 if i < 7 else 1.0
            for yy in range(y - 2, y + 3):
                for xx in range(x - 2, x + 3):
                    if (xx - x) ** 2 + (yy - y) ** 2 <= r * r:
                        _put(img, xx, yy, hi if yy < y else c)
        for x, y in ((13, 3), (14, 2), (13, 2)):
            _put(img, x, y, (60, 110, 40))
    elif look == "flower":
        for cx, cy in ((5, 6), (10, 9), (6, 11)):
            for dx, dy in ((0, -2), (2, 0), (0, 2), (-2, 0), (1, -1), (1, 1), (-1, 1), (-1, -1)):
                _put(img, cx + dx, cy + dy, c)
            _put(img, cx, cy, (230, 210, 120))
    elif look == "berries":
        for bx, by in ((5, 6), (9, 5), (7, 9), (11, 9), (5, 11), (9, 12)):
            for y in range(by - 1, by + 2):
                for x in range(bx - 1, bx + 2):
                    if (x - bx) ** 2 + (y - by) ** 2 <= 1.5:
                        _put(img, x, y, hi if (x < bx and y < by) else c)
            _put(img, bx + 1, by + 1, lo)
        for x, y in ((7, 3), (8, 2), (9, 3)):
            _put(img, x, y, (60, 110, 40))
    elif look == "resin":
        for y in range(4, 14):
            for x in range(16):
                r = 4.5 - abs(y - 9) * 0.45
                if abs(x - 7.5 + (y % 3 == 0) * 0.5) < r:
                    _put(img, x, y, hi if x < 7 and y < 9 else c if (x + y) % 4 else lo)
    elif look == "floss":
        for _ in range(70):
            x, y = int(rnd.gauss(7.5, 2.6)), int(rnd.gauss(8, 2.6))
            _put(img, x, y, c if rnd.random() < 0.7 else _shade(c, 0.85))
    else:  # stems
        for i, x in enumerate((5, 8, 11)):
            for y in range(2 + i, 15):
                _put(img, x, y, c if y % 4 else hi)
                _put(img, x + 1, y, lo)
    return img


def tea_texture(tid):
    c = _rgb(TEAS[tid][5])
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    glass, cork = (200, 220, 230), (150, 110, 70)
    for y in range(1, 4):
        for x in range(6, 10):
            _put(img, x, y, cork)
    for y in range(4, 7):
        _put(img, 6, y, glass)
        _put(img, 9, y, glass)
    for y in range(7, 15):
        half = 4 if y < 14 else 3
        for x in range(8 - half, 8 + half):
            edge = x in (8 - half, 8 + half - 1) or y == 14
            _put(img, x, y, glass if edge else (_shade(c, 1.25) if x == 9 - half + 1 and y < 11 else c))
    for y in range(4, 7):
        for x in range(7, 9):
            _put(img, x, y, (230, 240, 245))
    return img


def cooler_textures():
    rnd = random.Random(11)
    metal = (175, 182, 190)
    side = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            side.load()[x, y] = (_shade(metal, 0.7) if edge else _shade(metal, 0.92 + rnd.random() * 0.1)) + (255,)
    for y in range(3, 13, 3):
        for x in range(2, 14):
            side.load()[x, y] = _shade(metal, 0.6) + (255,)
    top = side.copy()
    for x in range(4, 12):
        for y in range(4, 12):
            top.load()[x, y] = ((184, 115, 51) if (x + y) % 3 else (150, 90, 40)) + (255,)
    out = {"greenhouse_cooler_side": side, "greenhouse_cooler_top": top}
    for on in (False, True):
        front = side.copy()
        for y in range(3, 13):
            for x in range(3, 13):
                if x in (3, 12) or y in (3, 12):
                    front.load()[x, y] = (70, 75, 82, 255)
                elif y % 2:
                    front.load()[x, y] = ((200, 235, 255, 255) if on and rnd.random() < 0.6 else
                                          (120, 180, 230, 255) if on else (40, 44, 50, 255))
                else:
                    front.load()[x, y] = (60, 64, 70, 255)
        out["greenhouse_cooler_front" + ("_on" if on else "")] = front
    return out


def heater_textures():
    rnd = random.Random(7)
    brick, mortar = (150, 70, 50), (170, 160, 150)
    side = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            row = y // 4
            edge = y % 4 == 3 or (x + (4 if row % 2 else 0)) % 8 == 7
            c = mortar if edge else _shade(brick, 0.9 + rnd.random() * 0.2)
            side.load()[x, y] = c + (255,)
    top = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            top.load()[x, y] = _shade((90, 90, 95), 0.9 + rnd.random() * 0.2) + (255,)
    for x in range(5, 11):
        for y in range(5, 11):
            top.load()[x, y] = (40, 40, 42, 255)
    out = {"greenhouse_heater_side": side, "greenhouse_heater_top": top}
    for lit in (False, True):
        front = side.copy()
        for y in range(6, 14):
            for x in range(4, 12):
                if x in (4, 11) or y in (6, 13):
                    front.load()[x, y] = (60, 60, 64, 255)
                elif lit:
                    front.load()[x, y] = (255, 160 + rnd.randint(0, 60), 40, 255) if (x + y) % 2 else (230, 90, 20, 255)
                else:
                    front.load()[x, y] = (30, 28, 28, 255)
        out["greenhouse_heater_front" + ("_on" if lit else "")] = front
    return out


# ---------- data ----------

def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def composition_rows(root=None):
    rows = []
    for sid, sp in species().items():
        rows.append((f"periodic_elements:{sid}_bush", 250.0, "cellulose:45;water:40;lignin:12;protein:3"))
    for pid, p in PRODUCTS.items():
        rows.append((f"periodic_elements:{pid}", 1000.0 / 9.0, p[1]))
    for tid, t in TEAS.items():
        rows.append((f"periodic_elements:{tid}", 1000.0 / 9.0, t[6]))
    return rows


def generate_shrubs(assets, data_dir, runtime_dir, lang, mod):
    shrubs = species()
    tex_b, tex_i = assets / "textures" / "block", assets / "textures" / "item"
    tex_b.mkdir(parents=True, exist_ok=True)
    tex_i.mkdir(parents=True, exist_ok=True)
    models, states = assets / "models", assets / "blockstates"
    loot = data_dir / mod / "loot_table" / "blocks"
    wg = data_dir / mod / "worldgen"
    runtime = []
    products_used = set()
    bee_flowers = []
    for sid, sp in shrubs.items():
        bush = f"{sid}_bush"
        variants = {}
        for age in range(4):
            name = f"{bush}_stage{age}"
            bush_texture(sid, age).save(tex_b / f"{name}.png")
            _dump(models / "block" / f"{name}.json", {"parent": "minecraft:block/cross",
                                                     "textures": {"cross": f"{mod}:block/{name}"}})
            if sid == "hydrangea":
                blue = f"{name}_blue" if age == 3 else name
                if age == 3:
                    bush_texture(sid, age, "6080E0").save(tex_b / f"{blue}.png")
                    _dump(models / "block" / f"{blue}.json", {"parent": "minecraft:block/cross",
                                                             "textures": {"cross": f"{mod}:block/{blue}"}})
                variants[f"age={age},blue=false"] = {"model": f"{mod}:block/{name}"}
                variants[f"age={age},blue=true"] = {"model": f"{mod}:block/{blue}"}
                continue
            variants[f"age={age}"] = {"model": f"{mod}:block/{name}"}
        _dump(states / f"{bush}.json", {"variants": variants})
        cutting_texture(sid).save(tex_i / f"{sid}_cutting.png")
        _dump(models / "item" / f"{sid}_cutting.json", {"parent": "minecraft:item/generated",
                                                        "textures": {"layer0": f"{mod}:item/{sid}_cutting"}})
        lang[f"block.{mod}.{bush}"] = sp["block"]
        lang[f"item.{mod}.{sid}_cutting"] = f"{sp['name']} Cutting"
        lang[f"shrub.{mod}.{sid}"] = sp["name"]

        # Loot: the harvest when ripe, and a stick or two (two or three for hard-wooded pemphis).
        pools = [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:stick", "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2 if sp["wood"] else 1,
                                                          "max": 3 if sp["wood"] else 2}}]}],
                  "conditions": [{"condition": "minecraft:survives_explosion"}]}]
        if sp["product"]:
            products_used.add(sp["product"])
            pools.append({"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{sp['product']}", "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}],
                          "conditions": [{"condition": "minecraft:block_state_property", "block": f"{mod}:{bush}",
                                          "properties": {"age": "3"}}]})
        _dump(loot / f"{bush}.json", {"type": "minecraft:block", "pools": pools})

        # Biome tag and worldgen: patches of ripe bushes.
        _dump(data_dir / mod / "tags" / "worldgen" / "biome" / "shrubs" / f"{sid}.json",
              {"values": [f"minecraft:{b}" for b in sp["biomes"]]})
        _dump(wg / "configured_feature" / "shrub" / f"{sid}.json", {"type": "minecraft:random_patch", "config": {
            "tries": 12, "xz_spread": 3, "y_spread": 1, "feature": {"feature": {
                "type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:simple_state_provider", "state": {
                    "Name": f"{mod}:{bush}", "Properties": {"age": "3"}}}}},
                "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
                    {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
                    {"type": "minecraft:would_survive", "state": {"Name": f"{mod}:{bush}", "Properties": {"age": "0"}}}]}}]}}})
        _dump(wg / "placed_feature" / "shrub" / f"{sid}.json", {"feature": f"{mod}:shrub/{sid}", "placement": [
            {"type": "minecraft:rarity_filter", "chance": PATCH_CHANCE},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"}]})
        runtime.append({"id": sid, "biomes": sp["biomes"], "soil": sp["soil"], "product": sp["product"],
                        "climate": sp["climate"], "sow": sp["sow"], "thorny": sp["thorny"]})
        if sid in BEE_FLOWERS:
            bee_flowers.append(f"{mod}:{bush}")

    for pid in sorted(products_used):
        name, _, _, food, feed, _ = PRODUCTS[pid]
        if pid in CRAFTS:
            result, count, n = CRAFTS[pid]
            _dump(data_dir / mod / "recipe" / f"{pid}_to_{result.split(':')[1]}.json", {
                "type": "minecraft:crafting_shapeless", "category": "misc",
                "ingredients": [{"item": f"{mod}:{pid}"}] * n, "result": {"id": result, "count": count}})
        lang[f"item.{mod}.{pid}"] = name
        product_texture(pid).save(tex_i / f"{pid}.png")
        _dump(models / "item" / f"{pid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{pid}"}})
        for t in feed:
            path = data_dir / "minecraft" / "tags" / "item" / f"{t}.json"
            old = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
            if f"{mod}:{pid}" not in old:
                old.append(f"{mod}:{pid}")
            _dump(path, {"replace": False, "values": old})

    # Greenhouse Heater.
    for name, img in heater_textures().items():
        img.save(tex_b / f"{name}.png")
    for lit in (False, True):
        suffix = "_on" if lit else ""
        _dump(models / "block" / f"{HEATER}{suffix}.json", {"parent": "minecraft:block/orientable", "textures": {
            "top": f"{mod}:block/greenhouse_heater_top", "front": f"{mod}:block/greenhouse_heater_front{suffix}",
            "side": f"{mod}:block/greenhouse_heater_side"}})
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for lit in (False, True):
            v = {"model": f"{mod}:block/{HEATER}{'_on' if lit else ''}"}
            if y:
                v["y"] = y
            variants[f"facing={facing},lit={'true' if lit else 'false'}"] = v
    _dump(states / f"{HEATER}.json", {"variants": variants})
    _dump(models / "item" / f"{HEATER}.json", {"parent": f"{mod}:block/{HEATER}"})
    _dump(loot / f"{HEATER}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{mod}:{HEATER}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    _dump(data_dir / mod / "recipe" / f"{HEATER}.json", HEATER_RECIPE)
    path = data_dir / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json"
    old = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
    if f"{mod}:{HEATER}" not in old:
        old.append(f"{mod}:{HEATER}")
    _dump(path, {"replace": False, "values": old})
    # Greenhouse Cooler (3.22.0).
    for name, img in cooler_textures().items():
        img.save(tex_b / f"{name}.png")
    for on in (False, True):
        suffix = "_on" if on else ""
        _dump(models / "block" / f"{COOLER}{suffix}.json", {"parent": "minecraft:block/orientable", "textures": {
            "top": f"{mod}:block/greenhouse_cooler_top", "front": f"{mod}:block/greenhouse_cooler_front{suffix}",
            "side": f"{mod}:block/greenhouse_cooler_side"}})
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for on in (False, True):
            v = {"model": f"{mod}:block/{COOLER}{'_on' if on else ''}"}
            if y:
                v["y"] = y
            variants[f"facing={facing},lit={'true' if on else 'false'}"] = v
    _dump(states / f"{COOLER}.json", {"variants": variants})
    _dump(models / "item" / f"{COOLER}.json", {"parent": f"{mod}:block/{COOLER}"})
    _dump(loot / f"{COOLER}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{mod}:{COOLER}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    _dump(data_dir / mod / "recipe" / f"{COOLER}.json", COOLER_RECIPE)
    if f"{mod}:{COOLER}" not in old:
        old.append(f"{mod}:{COOLER}")
    _dump(path, {"replace": False, "values": old})
    lang[f"block.{mod}.{COOLER}"] = COOLER_NAME
    lang[f"block.{mod}.{COOLER}.desc"] = COOLER_DESC

    # Bushes that bees visit as flowers.
    path = data_dir / "minecraft" / "tags" / "block" / "flowers.json"
    old = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
    _dump(path, {"replace": False, "values": old + [b for b in bee_flowers if b not in old]})

    # Teas: two leaves or flowers and a water bottle.
    teas = []
    for tid, (name, source, effect, secs, cure, _, _) in TEAS.items():
        if source not in products_used:
            continue
        tea_texture(tid).save(tex_i / f"{tid}.png")
        _dump(models / "item" / f"{tid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{tid}"}})
        _dump(data_dir / mod / "recipe" / f"{tid}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [{"item": f"{mod}:{source}"}, {"item": f"{mod}:{source}"},
                            {"fabric:type": "fabric:components", "base": {"item": "minecraft:potion"},
                             "components": {"minecraft:potion_contents": {"potion": "minecraft:water"}}}],
            "result": {"id": f"{mod}:{tid}", "count": 1}})
        lang[f"item.{mod}.{tid}"] = name
        teas.append({"id": tid, "effect": f"minecraft:{effect}", "ticks": secs * 20, "cures": f"minecraft:{cure}"})

    lang[f"block.{mod}.{HEATER}"] = HEATER_NAME
    lang[f"block.{mod}.{HEATER}.desc"] = HEATER_DESC
    lang.update(LANG)

    _dump(runtime_dir / "shrubs.json", {"shrubs": runtime, "products": {
        pid: {"food": list(PRODUCTS[pid][3]) if PRODUCTS[pid][3] else None,
              "effect": list(FOOD_EFFECTS[pid]) if pid in FOOD_EFFECTS else None,
              "sow": next((s for s, sp in shrubs.items() if sp["product"] == pid and sp["sow"]), None)}
        for pid in sorted(products_used)}, "heaterBurnDivisor": HEATER_BURN_DIVISOR, "teas": teas,
        "coolerIce": COOLER_ICE, "coolerPower": COOLER_POWER,
        "hydrangeaBlue": list(HYDRANGEA_BLUE), "hydrangeaPink": list(HYDRANGEA_PINK)})
    return shrubs


def compendium_entries(mod):
    """Plant Compendium entries (stage 2 of the compendium plan) for the shrubs."""
    out = []
    for sid, sp in species().items():
        out.append({"id": sid, "vanilla": False, "icon": f"{mod}:{sid}_cutting", "scientific": sp["scientific"],
                    "family": sp["family"], "height": sp["height"], "iucn": sp["iucn"], "biomes": sp["biomes"],
                    "blocks": [f"{mod}:{sid}_bush"], "products": [f"{mod}:{sp['product']}"] if sp["product"] else []})
    return out


def guide_paragraphs(L):
    """One heading per biome, one line per shrub: what it gives and which greenhouse it needs."""
    lang, mod, T = L.lang, L.mod, L.t
    by_biome = {}
    for sid, sp in species().items():
        for b in sp["biomes"]:
            by_biome.setdefault(b, []).append(sid)
    text = []
    from trees import BIOME_CLASS
    for b in BIOME_CLASS:
        if b not in by_biome:
            continue
        text.append("# " + lang[f"gui.{mod}.biome.{b}"])
        for sid in by_biome[b]:
            sp = SHRUBS[sid]
            what = L.name(sp["product"]) if sp["product"] else T["shrubs_wood"] if sp["wood"] else T["shrubs_none"]
            green = T["shrubs_" + sp["climate"]]
            text.append(f"- {lang[f'shrub.{mod}.{sid}']} ({sp['scientific']}): {what}; {green}")
    return text


def translations(code):
    import shrubs_i18n
    return shrubs_i18n.text(code)
