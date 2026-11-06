"""Crops and their genetics (3.23.0, stage 1 of the plan "Plano: cultivos e genética", approved by Guilherme on
2026-11-03; genes as Guilherme chose on 2026-11-04: Mendelian dominant and recessive genes for yield, speed and
resistance, plus drought, cold and pest traits, read with a seed analysis tool).

Twenty new crops (com.periodicelements.crops.GeneticCropBlock, ages 0 to 7 like wheat). Each grows wild in a few biomes
(a ripe plant on grass that does not grow; breaking it gives seeds), likes some climates (elsewhere it grows at half
speed) and carries a hidden genome, like the vanilla crops. The Java side holds the genetics; this module writes the
blocks' and items' assets, loot tables, recipes, worldgen, tags, runtime crops.json, compositions and the English lang
lines. Translations: tools/crops_i18n.py.
"""
import json
import random
import zlib

from PIL import Image

WARM, TEMPERATE, COLD = "warm", "temperate", "cold"
# One patch of a wild crop every N chunks of its biomes, 3 to 6 plants each.
PATCH_CHANCE = 8


def _c(name, block, seed, seed_name, product, product_name, climates, biomes, outcross, plant, accent, seed_comp,
       product_comp=None, look="grain", seed_look="seeds", clone=False, perennial=None, water=False, arid=False,
       soil="dirt", count=(1, 2), seeds=(1, 2), food=None, seed_food=None, poison=False, feed=(), seed_feed=(),
       legume=False):
    return {"name": name, "block": block, "seed": seed, "seed_name": seed_name, "product": product or seed,
            "product_name": product_name or seed_name, "climates": climates.split(), "biomes": biomes.split(),
            "outcross": outcross, "plant": plant, "accent": accent, "seed_comp": seed_comp,
            "product_comp": product_comp or seed_comp, "look": look, "seed_look": seed_look, "clone": clone,
            "perennial": perennial, "water": water, "arid": arid, "soil": soil, "count": count, "seeds": seeds,
            "food": food, "seed_food": seed_food, "poison": poison, "feed": feed, "seed_feed": seed_feed,
            "legume": legume}


SELF, CROSS = 0.05, 0.8
GRAIN_FEED = ("chicken_food",)
CATTLE = ("cow_food", "sheep_food", "goat_food")
CROPS = {
    # ---- food ----
    "rice": _c("Rice", "Rice Plants", "rice", "Rice", None, None, "warm temperate", "swamp mangrove_swamp", SELF,
               "6A9A3A", "D8C890", "starch:78;water:12;protein:7;cellulose:2;vegetable_oil:1", water=True,
               count=(2, 4), feed=GRAIN_FEED),
    "maize": _c("Maize", "Maize Plants", "maize_kernels", "Maize Kernels", "maize", "Maize", "warm temperate",
                "savanna plains", CROSS, "4A8A2A", "E8C030", "starch:62;water:14;protein:9;cellulose:10;vegetable_oil:5",
                "starch:55;water:30;protein:7;cellulose:5;vegetable_oil:3", look="ear", count=(1, 2),
                food=(3, 0.4), feed=("pig_food",), seed_feed=GRAIN_FEED),
    "soy": _c("Soybean", "Soybean Plants", "soybeans", "Soybeans", None, None, "temperate warm", "cherry_grove forest",
              SELF, "5A8A2A", "C8B060", "protein:36;vegetable_oil:20;starch:15;cellulose:10;water:10;glucose:9",
              seed_look="beans", count=(2, 4), feed=("pig_food", "chicken_food"), legume=True),
    "bean": _c("Common Bean", "Bean Plants", "beans", "Beans", None, None, "warm temperate", "jungle savanna", SELF,
               "4A7A2A", "6A2A1A", "starch:45;protein:22;cellulose:15;water:12;glucose:5;vegetable_oil:1",
               seed_look="beans", count=(2, 4), legume=True),
    "cassava": _c("Cassava", "Cassava Plants", "cassava_stem", "Cassava Stem", "cassava_root", "Cassava Root", "warm",
                  "jungle savanna", 0, "3A7A3A", "8A5A3A", "cellulose:50;water:35;lignin:10;starch:5",
                  "water:60;starch:32;cellulose:4;glucose:2;protein:1.5;vegetable_oil:0.5", look="root",
                  seed_look="stem", clone=True, count=(1, 3), food=(2, 0.2), poison=True, feed=("pig_food",)),
    "barley": _c("Barley", "Barley Plants", "barley", "Barley", None, None, "cold temperate", "taiga old_growth_pine_taiga",
                 SELF, "7A9A3A", "D0B860", "starch:62;cellulose:15;protein:11;water:11;vegetable_oil:1", count=(2, 4),
                 feed=CATTLE + ("horse_food",)),
    "tomato": _c("Tomato", "Tomato Plants", "tomato_seeds", "Tomato Seeds", "tomato", "Tomato", "warm temperate",
                 "jungle", SELF, "3A7A2A", "D82A1A", "cellulose:40;protein:25;vegetable_oil:20;water:10;starch:5",
                 "water:94;glucose:3;cellulose:1.5;protein:1;citric_acid:0.5", look="fruit", count=(1, 3),
                 food=(3, 0.3)),
    "coffee": _c("Coffee", "Coffee Bush", "coffee_beans", "Coffee Beans", None, None, "warm", "jungle bamboo_jungle", SELF,
                 "2A5A2A", "B01A1A", "cellulose:40;starch:20;vegetable_oil:13;protein:12;water:10;tannin:3.8;caffeine:1.2",
                 seed_look="beans", perennial=4, count=(1, 3)),
    # ---- industrial ----
    "cotton": _c("Cotton", "Cotton Plants", "cotton_seeds", "Cotton Seeds", "cotton", "Cotton", "warm",
                 "savanna desert", SELF, "4A7A3A", "F4F4F0", "cellulose:40;protein:23;vegetable_oil:20;water:9;starch:8",
                 "cellulose:90;water:7;vegetable_oil:1;protein:1;lignin:1", look="boll", arid=True, soil="sand",
                 count=(1, 2)),
    "flax": _c("Flax", "Flax Plants", "linseed", "Linseed", "flax", "Flax", "temperate cold", "plains meadow", SELF,
               "5A8A4A", "6A80D0", "vegetable_oil:40;cellulose:28;protein:20;water:7;starch:5",
               "cellulose:70;water:15;lignin:10;vegetable_oil:3;protein:2", look="stalk", count=(1, 2)),
    "hemp": _c("Hemp", "Hemp Plants", "hemp_seeds", "Hemp Seeds", "hemp", "Hemp", "temperate", "plains windswept_hills",
               CROSS, "3A7A2A", "8AA040", "vegetable_oil:30;cellulose:30;protein:25;water:8;starch:7",
               "cellulose:70;lignin:15;water:13;protein:2", look="stalk", count=(1, 2)),
    "rapeseed": _c("Rapeseed", "Rapeseed Plants", "rapeseed", "Rapeseed", None, None, "temperate cold",
                   "sunflower_plains flower_forest", CROSS, "5A8A3A", "F0D020",
                   "vegetable_oil:42;cellulose:22;protein:20;water:8;starch:8", count=(2, 4)),
    "castor": _c("Castor", "Castor Plants", "castor_beans", "Castor Beans", None, None, "warm", "savanna desert", SELF,
                 "6A3A3A", "8A2A2A", "vegetable_oil:48;cellulose:22;protein:18;water:6;starch:6", seed_look="beans",
                 arid=True, soil="sand", count=(2, 3), seed_food=(1, 0.0), poison=True),
    "indigo": _c("Indigo", "Indigo Plants", "indigo_seeds", "Indigo Seeds", "indigo_leaves", "Indigo Leaves", "warm",
                 "jungle sparse_jungle", SELF, "3A6A3A", "4A4AC0", "cellulose:40;protein:25;vegetable_oil:10;water:15;starch:10",
                 "water:60;cellulose:28;protein:6;indigo:2;chlorophyll:2;glucose:2", look="leaf", count=(1, 3),
                 legume=True),
    "madder": _c("Madder", "Madder Plants", "madder_seeds", "Madder Seeds", "madder_root", "Madder Root", "temperate",
                 "plains", SELF, "4A7A3A", "B03020", "cellulose:40;protein:20;vegetable_oil:15;water:15;starch:10",
                 "cellulose:40;water:40;starch:12;protein:4;alizarin:2;tannin:2", look="root", count=(1, 2)),
    "tobacco": _c("Tobacco", "Tobacco Plants", "tobacco_seeds", "Tobacco Seeds", "tobacco_leaves", "Tobacco Leaves",
                  "warm temperate", "jungle savanna", SELF, "5A8A3A", "E0A0B0",
                  "vegetable_oil:38;protein:22;cellulose:25;water:8;starch:7",
                  "water:70;cellulose:15;protein:6;glucose:4;nicotine:2;chlorophyll:2;tannin:1", look="leaf",
                  count=(1, 3)),
    # ---- animal feed ----
    "alfalfa": _c("Alfalfa", "Alfalfa Plants", "alfalfa_seeds", "Alfalfa Seeds", "alfalfa", "Alfalfa",
                  "temperate warm", "plains meadow", CROSS, "4A8A3A", "8A6AC8",
                  "protein:35;cellulose:30;vegetable_oil:10;water:10;starch:15",
                  "water:70;cellulose:15;protein:6;lignin:5;glucose:3;chlorophyll:1", look="forage", perennial=3,
                  arid=True, count=(1, 2), feed=CATTLE + ("horse_food",), legume=True),
    "sorghum": _c("Sorghum", "Sorghum Plants", "sorghum", "Sorghum", None, None, "warm", "savanna desert", SELF,
                  "6A8A3A", "A04A2A", "starch:68;protein:11;water:10;cellulose:8;vegetable_oil:3", arid=True,
                  soil="sand", count=(2, 4), feed=CATTLE + GRAIN_FEED),
    "oats": _c("Oats", "Oat Plants", "oats", "Oats", None, None, "temperate cold", "plains taiga", SELF, "7A9A4A",
               "D8D0A0", "starch:55;protein:13;cellulose:12;water:10;vegetable_oil:7;glucose:3", count=(2, 4),
               feed=CATTLE + ("horse_food",) + GRAIN_FEED),
    "turnip": _c("Fodder Turnip", "Turnip Plants", "turnip_seeds", "Turnip Seeds", "turnip", "Turnip", "cold temperate",
                 "snowy_plains windswept_hills", CROSS, "4A8A3A", "E8E0F0",
                 "vegetable_oil:35;protein:22;cellulose:25;water:8;starch:10",
                 "water:90;glucose:5;cellulose:3;protein:1.5;starch:0.5", look="root", count=(1, 2), food=(2, 0.4),
                 feed=CATTLE + ("pig_food",)),
}

# Cooked foods and other made items: id -> (English name, composition, food (nutrition, saturation) or None, look, colour).
MADE = {
    "cooked_rice": ("Cooked Rice", "water:68;starch:28;protein:3;cellulose:1", (5, 0.6), "bowl", "F4F0E4"),
    "cooked_beans": ("Cooked Beans", "water:65;starch:22;protein:9;cellulose:4", (6, 0.6), "beans", "6A3A2A"),
    "cassava_flour": ("Cassava Flour", "starch:85;water:9;cellulose:4;protein:1.5;vegetable_oil:0.5", (5, 0.6), "flour",
                      "F0E8D0"),
    "coffee": ("Coffee", "water:98;caffeine:0.1;glucose:1.9", (1, 0.1), "drink", "4A2A1A"),
    "tobacco_wash": ("Tobacco Wash", "water:96;nicotine:2;tannin:1;glucose:1", None, "spray", "8A7030"),
}
SEED_LENS = "seed_lens"
BRUSH = "pollination_brush"
TROUGH = "feeding_trough"

# Genes: (letter, English name, is the useful allele dominant?, the useful phenotype, the plain one).
GENES = [
    ("C", "Cycle", False, "early", "normal cycle"),
    ("P", "Yield", True, "abundant", "ordinary yield"),
    ("R", "Disease resistance", True, "resists rust", "takes rust"),
    ("D", "Drought tolerance", False, "drought tolerant", "needs water"),
    ("F", "Cold tolerance", True, "cold tolerant", "feels the cold"),
    ("I", "Pest resistance", True, "resists caterpillars", "takes caterpillars"),
]
# Vanilla crops: how they cross (clones are planted from a piece of the plant and copy it).
VANILLA = {"wheat": SELF, "carrots": CROSS, "potatoes": 0, "beetroots": CROSS}

import genetics as _genetics  # 3.37.0: crop genetics stages 2 and 3

RESEARCH_UNLOCKS = {"item": {SEED_LENS, BRUSH, TROUGH, "tobacco_wash"} | _genetics.RESEARCH_ITEMS,
                    "crop": {"genotype", "rotation"} | _genetics.RESEARCH_CROP}

LANG = {
    "gui.periodic_elements.crop.climate_ok": "Climate: suits it",
    "gui.periodic_elements.crop.climate_slow": "Climate: wrong for it, grows at %s%% speed",
    "gui.periodic_elements.crop.needs_water": "Rice: needs water next to its farmland to grow",
    "gui.periodic_elements.crop.pest": "Caterpillars! It has stopped growing",
    "gui.periodic_elements.crop.disease": "Rust! It has stopped growing",
    "gui.periodic_elements.crop.healthy": "Healthy",
    "gui.periodic_elements.crop.pollinated": "Pollinated by a neighbour",
    "gui.periodic_elements.crop.selfed": "Pollinated by itself",
    "gui.periodic_elements.crop.hand_pollinated": "Pollinated by hand",
    "gui.periodic_elements.crop.not_pollinated": "Not yet pollinated",
    "gui.periodic_elements.crop.wild": "Wild plant",
    "gui.periodic_elements.crop.traits": "Traits: %s",
    "gui.periodic_elements.crop.traits_none": "Traits: nothing out of the ordinary",
    "gui.periodic_elements.crop.genotype": "Genotype: %s",
    "gui.periodic_elements.crop.clone": "Grown from a cutting: a copy of its parent",
    "gui.periodic_elements.lens.needs": "You do not know what to look for yet (Domestication, level 1)",
    "gui.periodic_elements.lens.nothing": "Hold seeds in your other hand, or use the lens on a crop",
    "gui.periodic_elements.lens.analysed": "Analysed %s seeds",
    "gui.periodic_elements.brush.needs": "You do not know how to cross plants by hand yet (Plant Hybridization, level 3)",
    "gui.periodic_elements.brush.collected": "Pollen collected from %s",
    "gui.periodic_elements.brush.applied": "Pollinated by hand with pollen of %s",
    "gui.periodic_elements.brush.not_ripe": "Only a ripe plant gives pollen",
    "gui.periodic_elements.brush.wrong_species": "This pollen is from %s: it will not take on another kind of plant",
    "gui.periodic_elements.wash.needs": "You do not know this remedy yet (Tobacco Wash, level 3)",
    "gui.periodic_elements.wash.cured": "Caterpillars washed off %s plants",
    "gui.periodic_elements.spray.cured": "Caterpillars killed on %s plants",
    "gui.periodic_elements.trough.contents": "Feeding Trough: %s %s",
    "gui.periodic_elements.trough.empty": "Feeding Trough: empty; use feed on it to fill it",
    "gui.periodic_elements.trough.full": "The trough holds only one kind of feed",
    "tooltip.periodic_elements.genome.unknown": "Not analysed (Seed Lens)",
    "tooltip.periodic_elements.genome.traits": "Traits: %s",
    "tooltip.periodic_elements.genome.plain": "Traits: nothing out of the ordinary",
    "tooltip.periodic_elements.genome.genotype": "Genotype: %s",
    "item.periodic_elements.seed_lens.tip": "With Domestication: use it holding seeds in your other hand, or on a crop, to "
                                            "see their traits; with Mendel's Laws, their genes",
    "item.periodic_elements.pollination_brush.tip": "Plant Hybridization: take pollen from a ripe plant, then brush it "
                                                    "onto a plant of the same kind to choose its father",
    "item.periodic_elements.tobacco_wash.tip": "Tobacco Wash: use it on a crop to wash the caterpillars off the plants "
                                               "around it",
    "block.periodic_elements.feeding_trough.desc": "Animal Husbandry: fill it with feed; it feeds the animals within 6 "
                                                   "blocks that eat it, so they breed and their young grow",
    "gamerule.periodicElementsCropPests": "Crop pests and diseases",
}
for _g in GENES:
    LANG[f"gene.periodic_elements.{_g[0]}"] = _g[1]
    LANG[f"gene.periodic_elements.{_g[0]}.good"] = _g[3]
    LANG[f"gene.periodic_elements.{_g[0]}.plain"] = _g[4]

RECIPES = {
    SEED_LENS: {"type": "minecraft:crafting_shapeless", "category": "equipment", "ingredients": [
        {"item": "minecraft:glass_pane"}, {"item": "minecraft:stick"}, {"item": "minecraft:wheat_seeds"}],
        "result": {"id": f"periodic_elements:{SEED_LENS}", "count": 1}},
    BRUSH: {"type": "minecraft:crafting_shapeless", "category": "equipment", "ingredients": [
        {"item": "minecraft:feather"}, {"item": "minecraft:stick"}, {"item": "minecraft:string"}],
        "result": {"id": f"periodic_elements:{BRUSH}", "count": 1}},
    TROUGH: {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["P P", "PSP"],
             "key": {"P": {"tag": "minecraft:planks"}, "S": {"tag": "minecraft:wooden_slabs"}},
             "result": {"id": f"periodic_elements:{TROUGH}", "count": 1}},
    "tobacco_wash": {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [
        {"item": "periodic_elements:tobacco_leaves"}, {"item": "periodic_elements:tobacco_leaves"},
        {"item": "periodic_elements:tobacco_leaves"}, {"item": "minecraft:potion"}],
        "result": {"id": "periodic_elements:tobacco_wash", "count": 1}},
    "coffee": {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [
        {"item": "periodic_elements:coffee_beans"}, {"item": "periodic_elements:coffee_beans"}, {"item": "minecraft:potion"}],
        "result": {"id": "periodic_elements:coffee", "count": 1}},
    "string_from_cotton": {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [
        {"item": "periodic_elements:cotton"}, {"item": "periodic_elements:cotton"}],
        "result": {"id": "minecraft:string", "count": 2}},
    "string_from_flax": {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [
        {"item": "periodic_elements:flax"}, {"item": "periodic_elements:flax"}],
        "result": {"id": "minecraft:string", "count": 1}},
    "string_from_hemp": {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [
        {"item": "periodic_elements:hemp"}, {"item": "periodic_elements:hemp"}],
        "result": {"id": "minecraft:string", "count": 1}},
    "lead_from_hemp": {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [
        {"item": "periodic_elements:hemp"}] * 4, "result": {"id": "minecraft:lead", "count": 1}},
    "wool_from_cotton": {"type": "minecraft:crafting_shaped", "category": "building", "pattern": ["CC", "CC"],
                         "key": {"C": {"item": "periodic_elements:cotton"}}, "result": {"id": "minecraft:white_wool", "count": 1}},
    "maize_kernels": {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [
        {"item": "periodic_elements:maize"}], "result": {"id": "periodic_elements:maize_kernels", "count": 2}},
}
SMELTING = {"cooked_rice": "rice", "cooked_beans": "beans", "cassava_flour": "cassava_root"}

GUIDE = [
    "# Crops",
    "Twenty crops grow wild in the world: a ripe plant standing on grass or sand that does not grow. Break it for its "
    "seeds (cassava gives stem cuttings) and sow them on farmland like wheat. Rice needs water right next to its "
    "farmland. Coffee and alfalfa stay planted: right-click them when ripe to pick them, and they grow back.",
    "Each crop likes some climates (warm, temperate or cold). Elsewhere it grows at half speed, never not at all. "
    "In a dry land (desert, savanna, badlands) a field that is not fully watered also grows at half speed, unless "
    "the crop is a dry-land one (cotton, castor, alfalfa, sorghum). A heated greenhouse counts as warm.",
    "# Genes",
    "Every plant, the vanilla crops too, carries six genes, two copies of each, one from each parent. They act from "
    "the first sowing; research only lets you see them. The useful form of each gene and how it shows:",
    "- C, Cycle: cc plants ripen about 30% sooner (recessive: Cc looks normal).",
    "- P, Yield: one P is enough for a 30% chance of one more crop at harvest (dominant).",
    "- R, Disease resistance: one R makes rust four times rarer (dominant).",
    "- D, Drought tolerance: dd plants grow at full speed in dry lands without water (recessive).",
    "- F, Cold tolerance: one F lets a crop of warmer lands grow at full speed in the cold (dominant).",
    "- I, Pest resistance: one I makes caterpillars four times rarer (dominant).",
    "Seeds from shops, villages and vanilla stacks have the plain forms (CC pp rr DD ff ii). Wild plants carry the "
    "useful forms, often hidden in one copy: that is where improvement comes from. Wheat seeds from grass are "
    "sometimes wild too.",
    "# Crossing",
    "When a plant ripens it takes pollen from a ripe plant of its kind within 2 blocks: maize, hemp, rapeseed, "
    "alfalfa, turnip, carrot and beetroot do so 80% of the time, the others only 5% (they pollinate themselves). "
    "Each seed then gets one copy of each gene from the mother and one from the father, at random. One gene in 500 "
    "mutates. Cassava and potato are planted from pieces of the plant: they copy it exactly. Seeds with different "
    "genes do not stack.",
    "@1 Domestication (level 1) teaches the Seed Lens: use it holding seeds in your other hand, or on a crop, to see "
    "their traits. A plant that carries a recessive form in one copy looks plain: only its offspring tell.",
    "@1 Animal Husbandry (level 1): the Feeding Trough holds a stack of feed and feeds the animals within 6 blocks that "
    "eat it: grown animals breed and the young grow up faster. Cows, sheep and goats eat alfalfa, oats, barley, "
    "sorghum and turnips; pigs maize, cassava, soybeans and turnips; chickens grain; horses oats and alfalfa.",
    "@2 Crop Rotation (level 2): soybeans, beans, indigo and alfalfa harvested ripe leave their farmland fertilized. "
    "Natural Dyes (level 2): the Dye Vat makes blue dye from indigo leaves and red dye from madder root.",
    "@3 Plant Hybridization (level 3): the Pollination Brush takes pollen from a ripe plant; brushed onto a plant of "
    "the same kind, anywhere, it makes that plant the father of its seeds. Tobacco Wash (level 3): three tobacco "
    "leaves and a water bottle wash the caterpillars off the crops around where you use it.",
    "@4 Mendel's Laws (level 4): the Seed Lens shows each gene's two letters, so Cc tells apart from CC. Cross two Cc "
    "plants and a quarter of the seeds are cc.",
    "# Pests and diseases",
    "@0 A plant growing among more than 20 of its kind in a 9 by 9 square can catch caterpillars or rust. It stops "
    "growing, gives one crop less and passes it to its neighbours. Mixed fields catch nothing. Break it, or use "
    "Tobacco Wash or Insect Spray against caterpillars (crops on DDT ground never catch them) and Bordeaux mixture "
    "against rust. The game rule periodicElementsCropPests turns pests and diseases off.",
    "# Where they grow wild",
]
GUIDE[-1:-1] = _genetics.GUIDE
GUIDE_T = {"crops": "Crops and Genetics"}
NEW_SUBSTANCES = {
    # id: (tier, level, name, formula, state, sector, colour, use)
    "indigo": (4, 1, "Indigo", "C16H10N2O2", "solid", "agriculture", "2A2A90", "The blue of jeans, from fermented indigo leaves"),
    "nicotine": (4, 1, "Nicotine", "C10H14N2", "liquid", "agriculture", "C0A060", "The poison of tobacco: an old insecticide"),
}
NEW_REACTIONS = [
    "indigo_dyeing,2,dye_vat,natural_dyes,indigo,item:minecraft:blue_dye:100,0,20,100,1,0,,Indigo leaves steeped and "
    "fermented in the vat; the cloth turns blue in the air: 32 blue dyes to a flask's worth,",
    "madder_dyeing,2,dye_vat,natural_dyes,alizarin,item:minecraft:red_dye:100,0,20,100,1,0,,Madder root ground and "
    "boiled with alum: the old Turkey red: 32 red dyes to a flask's worth,",
]
NEW_RESEARCH = [
    "domestication,1,no,food,Domestication,c. 8000 BCE,,minecraft:wheat_seeds*16;minecraft:carrot*8,item:seed_lens,"
    "\"Keep the seeds of the best plants: the Seed Lens shows what traits a seed or a crop has\"",
    "animal_husbandry,1,no,food,Animal Husbandry,c. 7000 BCE,,minecraft:hay_block*4;minecraft:wheat*16,item:feeding_trough,"
    "\"Pen the herd and feed it: a Feeding Trough feeds the animals around it so they breed and grow\"",
    "crop_rotation,2,no,food,Crop Rotation,c. 800,domestication,periodic_elements:beans|periodic_elements:soybeans*8;"
    "minecraft:bone_meal*8,crop:rotation,\"Three-field rotation: soybeans, beans, indigo and alfalfa leave the ground "
    "fertilized when harvested\"",
    "natural_dyes,2,no,food,Natural Dyes,c. 1200,domestication,periodic_elements:indigo_leaves*8;"
    "periodic_elements:madder_root*8,reaction:indigo_dyeing;reaction:madder_dyeing,\"Blue from indigo, red from madder "
    "root: the Dye Vat dyes with plants\"",
    "plant_hybridization,3,no,food,Plant Hybridization,1761,domestication,minecraft:feather*4;periodic_elements:maize_kernels*16,"
    "item:pollination_brush,\"Kölreuter crossed tobacco plants by hand: the Pollination Brush chooses the father of a "
    "plant's seeds\"",
    "tobacco_wash,3,no,food,Tobacco Wash,1763,domestication,periodic_elements:tobacco_leaves*8;minecraft:glass_bottle*4,"
    "item:tobacco_wash,\"Tobacco steeped in water kills caterpillars on crops: the first insecticide of the farm\"",
    "mendels_laws,4,no,food,Mendel's Laws,1866,plant_hybridization,periodic_elements:beans*16;minecraft:paper*8,"
    "crop:genotype,\"Mendel counted his peas: each trait is two factors, one from each parent. The Seed Lens shows "
    "the genes themselves\"",
]


def items():
    """Every crop item id (seeds, products, made items, tools)."""
    out = []
    for c in CROPS.values():
        for i in (c["seed"], c["product"]):
            if i not in out:
                out.append(i)
    return out + list(MADE) + [SEED_LENS, BRUSH, TROUGH] + list(_genetics.ITEMS) + list(_genetics.BLOCKS)


# ---------- textures ----------

def _rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def _shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def _put(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.load()[x, y] = tuple(c) + (255,)


def crop_texture(cid, stage):
    """Four growth stages, drawn for the vanilla crop model (four planes in a #)."""
    c = CROPS[cid]
    leaf, accent = _rgb(c["plant"]), _rgb(c["accent"])
    rnd = random.Random(zlib.crc32(f"{cid}{stage}".encode()))
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    top = [12, 8, 5, 2][stage]
    for x in (2, 5, 8, 11, 14):
        lean = rnd.choice((-1, 0, 1))
        for y in range(top + rnd.randint(0, 2), 16):
            xx = x + (lean if y < (top + 16) // 2 else 0)
            _put(img, xx, y, _shade(leaf, 0.85 + 0.3 * rnd.random()))
            if rnd.random() < 0.35:
                _put(img, xx + rnd.choice((-1, 1)), y, _shade(leaf, 1.15))
    if stage == 3:
        look = c["look"]
        for x in (2, 5, 8, 11, 14):
            if look in ("grain", "ear"):
                for y in range(top, top + (5 if look == "grain" else 4)):
                    _put(img, x, y, accent if y % 2 else _shade(accent, 0.8))
                    if look == "ear":
                        _put(img, x + 1, y, _shade(accent, 0.9))
            elif look in ("fruit", "boll"):
                y = top + rnd.randint(1, 5)
                for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
                    _put(img, x + dx - 1, y + dy, accent if (dx + dy) % 2 == 0 else _shade(accent, 0.85))
            elif look in ("leaf", "forage", "stalk"):
                _put(img, x, top, accent)
                _put(img, x, top + 1, _shade(accent, 0.8))
            else:  # roots show leaves on top and the crown of the root
                _put(img, x, 14, accent)
                _put(img, x, 15, _shade(accent, 0.8))
    elif stage == 2 and c["look"] in ("grain", "ear"):
        for x in (2, 8, 14):
            _put(img, x, top, _shade(leaf, 1.3))
    return img


def item_texture(look, color, seed=0):
    c = _rgb(color)
    hi, lo = _shade(c, 1.3), _shade(c, 0.7)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = random.Random(seed)
    if look in ("seeds", "grain"):
        for sx, sy in ((5, 6), (9, 5), (7, 9), (10, 10), (5, 11), (11, 7), (8, 13)):
            _put(img, sx, sy, hi)
            _put(img, sx, sy + 1, c)
            _put(img, sx + 1, sy + 1, lo)
    elif look == "beans":
        for sx, sy in ((4, 6), (9, 4), (7, 9), (11, 9), (5, 12)):
            for dx, dy in ((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1)):
                _put(img, sx + dx, sy + dy, hi if dy == 0 and dx < 2 else c)
            _put(img, sx + 1, sy + 1, lo)
    elif look == "ear":
        for y in range(2, 14):
            for x in range(6, 10):
                _put(img, x, y, hi if (x + y) % 2 else c)
        for y in range(9, 16):
            _put(img, 5, y, (110, 150, 60))
            _put(img, 10, y, (110, 150, 60))
    elif look == "root":
        for i in range(12):
            w = max(1, 3 - i // 4)
            for dx in range(-w, w + 1):
                _put(img, 7 + dx + i // 6, 3 + i, c if dx else hi)
        for x, y in ((6, 1), (8, 0), (9, 2)):
            _put(img, x, y, (70, 130, 50))
    elif look == "leaf":
        for y in range(2, 15):
            w = 4 * (1 - abs((y - 8) / 6.5) ** 2)
            for x in range(16):
                if abs(x - 7.5) < w:
                    _put(img, x, y, lo if abs(x - 7.5) < 0.6 else (c if (x + y) % 5 else hi))
    elif look == "fruit":
        for y in range(16):
            for x in range(16):
                if (x - 7.5) ** 2 + (y - 8.5) ** 2 <= 26:
                    _put(img, x, y, hi if x < 7 and y < 7 else c if (x - 7.5) ** 2 + (y - 8.5) ** 2 < 18 else lo)
        for x, y in ((7, 2), (8, 2), (6, 3), (9, 3)):
            _put(img, x, y, (60, 120, 40))
    elif look == "boll":
        for _ in range(80):
            x, y = int(rnd.gauss(7.5, 2.4)), int(rnd.gauss(7, 2.4))
            _put(img, x, y, c if rnd.random() < 0.7 else _shade(c, 0.88))
        for x, y in ((6, 13), (7, 14), (8, 13), (9, 14)):
            _put(img, x, y, (110, 80, 50))
    elif look in ("stalk", "stem", "forage"):
        for i, x in enumerate((4, 7, 10, 13) if look != "stem" else (6, 9)):
            for y in range(2 + i % 2, 15):
                _put(img, x - (y // 5 if look == "forage" else 0), y, c if y % 4 else hi)
        if look == "stem":
            for y in (5, 9, 13):
                _put(img, 6, y, lo)
                _put(img, 9, y, lo)
    elif look == "bowl":
        for x in range(2, 14):
            for y in range(9, 14):
                if (x - 7.5) ** 2 / 36 + (y - 9) ** 2 / 16 <= 1:
                    _put(img, x, y, (110, 80, 50))
        for x in range(3, 13):
            _put(img, x, 8, c)
            _put(img, x, 7, hi if x % 2 else c)
    elif look == "flour":
        for y in range(5, 15):
            for x in range(2, 14):
                if abs(x - 7.5) < (y - 4) * 0.7:
                    _put(img, x, y, c if rnd.random() < 0.8 else hi)
    elif look in ("drink", "spray"):
        glass = (200, 220, 230)
        for y in range(4, 15):
            for x in range(4, 12):
                edge = x in (4, 11) or y == 14
                _put(img, x, y, glass if edge else (c if y > 6 else glass))
        for y in (1, 2, 3):
            _put(img, 7, y, (140, 100, 60))
            _put(img, 8, y, (140, 100, 60))
    return img


def lens_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = (x - 9.5) ** 2 + (y - 6.5) ** 2
            if d <= 20:
                _put(img, x, y, (190, 220, 235) if d < 12 else (120, 90, 50))
    for i in range(6):
        _put(img, 6 - i, 9 + i, (110, 80, 50))
        _put(img, 7 - i, 9 + i, (90, 65, 40))
    for x, y in ((9, 6), (10, 6), (9, 7)):
        _put(img, x, y, (230, 200, 120))
    return img


def brush_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for i in range(9):
        _put(img, 3 + i, 14 - i, (130, 95, 55))
    for x, y in ((11, 4), (12, 3), (13, 2), (12, 4), (13, 3), (14, 2), (12, 5), (13, 4), (14, 3), (14, 1), (13, 1)):
        _put(img, x, y, (245, 230, 140))
    return img


def trough_textures():
    rnd = random.Random(11)
    wood = (150, 110, 65)
    side = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(8, 16):
        for x in range(16):
            side.load()[x, y] = _shade(wood, 0.85 + rnd.random() * 0.25 if y % 4 else 0.7) + (255,)
    top = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            rim = x in (0, 15) or y in (0, 15)
            top.load()[x, y] = (_shade(wood, 0.8) if rim else _shade((190, 170, 90), 0.8 + rnd.random() * 0.3)) + (255,)
    return {"feeding_trough_side": side, "feeding_trough_top": top}


# ---------- data ----------

def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def composition_rows(root=None):
    rows = []
    for cid, c in CROPS.items():
        rows.append((f"periodic_elements:{cid}_crop", 100.0, "water:70;cellulose:20;protein:4;lignin:4;glucose:2"))
        # Maize kernels are shelled from the cob, two per cob: half a cob each, so shelling makes nothing from nothing.
        seed_amount = 1000.0 / 18.0 if cid == "maize" else 1000.0 / 9.0
        rows.append((f"periodic_elements:{c['seed']}", seed_amount, c["seed_comp"]))
        if c["product"] != c["seed"]:
            rows.append((f"periodic_elements:{c['product']}", 1000.0 / 9.0, c["product_comp"]))
    for mid, m in MADE.items():
        rows.append((f"periodic_elements:{mid}", 1000.0 / 9.0, m[1]))
    rows.append((f"periodic_elements:{SEED_LENS}", 200.0, "silicon_dioxide:60;sodium_oxide:10;calcium_oxide:8;cellulose:22"))
    rows.append((f"periodic_elements:{BRUSH}", 100.0, "protein:60;cellulose:40"))
    rows.append((f"periodic_elements:{TROUGH}", 1000.0, "cellulose:50;lignin:25;water:15;starch:10"))
    return rows + _genetics.composition_rows()


def _tag(data_dir, path, values):
    p = data_dir / path
    old = json.loads(p.read_text(encoding="utf-8"))["values"] if p.exists() else []
    for v in values:
        if v not in old:
            old.append(v)
    _dump(p, {"replace": False, "values": old})


def generate_crops(assets, data_dir, runtime_dir, lang, mod):
    tex_b, tex_i = assets / "textures" / "block", assets / "textures" / "item"
    tex_b.mkdir(parents=True, exist_ok=True)
    tex_i.mkdir(parents=True, exist_ok=True)
    models, states = assets / "models", assets / "blockstates"
    loot = data_dir / mod / "loot_table" / "blocks"
    wg = data_dir / mod / "worldgen"
    runtime = []
    feed_tags = {}
    item_looks = {}
    for cid, c in CROPS.items():
        block = f"{cid}_crop"
        for stage in range(4):
            name = f"{block}_stage{stage}"
            crop_texture(cid, stage).save(tex_b / f"{name}.png")
            _dump(models / "block" / f"{name}.json", {"parent": "minecraft:block/crop",
                                                     "textures": {"crop": f"{mod}:block/{name}"}})
        variants = {}
        for age in range(8):
            stage = [0, 0, 1, 1, 2, 2, 2, 3][age]
            for wild in ("false", "true"):
                variants[f"age={age},wild={wild}"] = {"model": f"{mod}:block/{block}_stage{stage}"}
        _dump(states / f"{block}.json", {"variants": variants})
        lang[f"block.{mod}.{block}"] = c["block"]
        lang[f"crop.{mod}.{cid}"] = c["name"]
        item_looks[c["seed"]] = (c["seed_look"], c["accent"] if c["seed"] == c["product"] and c["seed_look"] != "stem"
                                 else "C8B080" if c["seed_look"] == "seeds" else c["plant"] if c["seed_look"] == "stem"
                                 else c["accent"])
        lang[f"item.{mod}.{c['seed']}"] = c["seed_name"]
        if c["product"] != c["seed"]:
            item_looks[c["product"]] = (c["look"], c["accent"])
            lang[f"item.{mod}.{c['product']}"] = c["product_name"]
        for t in c["feed"]:
            feed_tags.setdefault(t, []).append(f"{mod}:{c['product']}")
        for t in c["seed_feed"]:
            feed_tags.setdefault(t, []).append(f"{mod}:{c['seed']}")

        # Loot: ripe, the crop and seeds (or just more of it, when the crop is its own seed); unripe, one seed. The
        # Java side gives each seed its genes, the yield bonus and the pest loss.
        ripe = {"condition": "minecraft:block_state_property", "block": f"{mod}:{block}", "properties": {"age": "7"}}
        lo, hi = c["count"]
        pools = []
        if c["product"] != c["seed"]:
            pools.append({"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{c['product']}", "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}],
                "conditions": [ripe]})
            slo, shi = c["seeds"]
            pools.append({"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": f"{mod}:{c['seed']}", "conditions": [ripe], "functions": [
                    {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": slo, "max": shi}}]},
                {"type": "minecraft:item", "name": f"{mod}:{c['seed']}"}]}]})
        else:
            pools.append({"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": f"{mod}:{c['seed']}", "conditions": [ripe], "functions": [
                    {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]},
                {"type": "minecraft:item", "name": f"{mod}:{c['seed']}"}]}]})
        _dump(loot / f"{block}.json", {"type": "minecraft:block", "pools": pools,
                                       "functions": [{"function": "minecraft:explosion_decay"}]})

        # Biome tag and worldgen: small patches of ripe wild plants.
        _dump(data_dir / mod / "tags" / "worldgen" / "biome" / "crops" / f"{cid}.json",
              {"values": [f"minecraft:{b}" for b in c["biomes"]]})
        wild = {"Name": f"{mod}:{block}", "Properties": {"age": "7", "wild": "true"}}
        _dump(wg / "configured_feature" / "crop" / f"{cid}.json", {"type": "minecraft:random_patch", "config": {
            "tries": 10, "xz_spread": 3, "y_spread": 1, "feature": {"feature": {
                "type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:simple_state_provider",
                                                                         "state": wild}}},
                "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
                    {"type": "minecraft:matching_blocks", "blocks": ["minecraft:air", "minecraft:short_grass", "minecraft:snow"]},
                    {"type": "minecraft:would_survive", "state": wild}]}}]}}})
        _dump(wg / "placed_feature" / "crop" / f"{cid}.json", {"feature": f"{mod}:crop/{cid}", "placement": [
            {"type": "minecraft:rarity_filter", "chance": PATCH_CHANCE},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"}]})
        runtime.append({"id": cid, "seed": c["seed"], "product": c["product"], "climates": c["climates"],
                        "biomes": c["biomes"], "outcross": c["outcross"], "clone": c["clone"],
                        "perennial": c["perennial"], "water": c["water"], "arid": c["arid"], "soil": c["soil"],
                        "legume": c["legume"], "count": list(c["count"]),
                        "seedFood": list(c["seed_food"]) if c["seed_food"] else None,
                        "productFood": list(c["food"]) if c["food"] else None, "poison": c["poison"]})

    for mid, m in MADE.items():
        item_looks[mid] = (m[3], m[4])
        lang[f"item.{mod}.{mid}"] = m[0]
    for iid, (look, color) in item_looks.items():
        item_texture(look, color, zlib.crc32(iid.encode())).save(tex_i / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{iid}"}})
    for iid, img in ((SEED_LENS, lens_texture()), (BRUSH, brush_texture())):
        img.save(tex_i / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{mod}:item/{iid}"}})
    lang[f"item.{mod}.{SEED_LENS}"] = "Seed Lens"
    lang[f"item.{mod}.{BRUSH}"] = "Pollination Brush"

    # Feeding Trough: a low wooden box.
    for name, img in trough_textures().items():
        img.save(tex_b / f"{name}.png")
    _dump(models / "block" / f"{TROUGH}.json", {"parent": "minecraft:block/block", "textures": {
        "particle": f"{mod}:block/feeding_trough_side", "side": f"{mod}:block/feeding_trough_side",
        "top": f"{mod}:block/feeding_trough_top"}, "elements": [
        {"from": [0, 0, 0], "to": [16, 8, 16], "faces": {
            "down": {"texture": "#top", "cullface": "down"}, "up": {"texture": "#top"},
            "north": {"texture": "#side", "cullface": "north"}, "south": {"texture": "#side", "cullface": "south"},
            "west": {"texture": "#side", "cullface": "west"}, "east": {"texture": "#side", "cullface": "east"}}}]})
    _dump(states / f"{TROUGH}.json", {"variants": {"": {"model": f"{mod}:block/{TROUGH}"}}})
    _dump(models / "item" / f"{TROUGH}.json", {"parent": f"{mod}:block/{TROUGH}"})
    _dump(loot / f"{TROUGH}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{mod}:{TROUGH}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    _tag(data_dir, "minecraft/tags/block/mineable/axe.json", [f"{mod}:{TROUGH}"])
    lang[f"block.{mod}.{TROUGH}"] = "Feeding Trough"

    for rid, r in RECIPES.items():
        _dump(data_dir / mod / "recipe" / f"{rid}.json", r)
    for out, inp in SMELTING.items():
        for kind, time in (("smelting", 200), ("smoking", 100), ("campfire_cooking", 600)):
            _dump(data_dir / mod / "recipe" / f"{out}_from_{kind}.json", {
                "type": f"minecraft:{kind}", "category": "food", "ingredient": {"item": f"{mod}:{inp}"},
                "result": {"id": f"{mod}:{out}"}, "experience": 0.35, "cookingtime": time})
    for t, vals in feed_tags.items():
        _tag(data_dir, f"minecraft/tags/item/{t}.json", vals)
    _tag(data_dir, "minecraft/tags/block/crops.json", [f"{mod}:{cid}_crop" for cid in CROPS])
    _tag(data_dir, "minecraft/tags/block/maintains_farmland.json", [f"{mod}:{cid}_crop" for cid in CROPS])
    _tag(data_dir, "minecraft/tags/block/bee_growables.json", [f"{mod}:{cid}_crop" for cid in CROPS])
    lang.update(LANG)
    _genetics.generate(assets, data_dir, lang, mod, _tag)

    _dump(runtime_dir / "crops.json", {"crops": runtime, "vanilla": VANILLA, "patchChance": PATCH_CHANCE,
                                       "made": {k: {"food": list(v[2]) if v[2] else None} for k, v in MADE.items()}})
    return CROPS


def guide_paragraphs(L):
    """One line per crop: where it grows wild, what it likes and what it gives."""
    lang, mod = L.lang, L.mod
    text = []
    for cid, c in CROPS.items():
        biomes = ", ".join(lang.get(f"gui.{mod}.biome.{b}", b) for b in c["biomes"])
        what = L.name(f"{mod}:{c['product']}")
        text.append(f"- {lang[f'crop.{mod}.{cid}']}: {what}. {biomes}")
    return text


def translations(code):
    import crops_i18n
    return crops_i18n.text(code)
