"""Trees of the world (2.8.0): five real tree species for every land biome, each growing only in its own biomes.

data/trees.csv lists the species (stage 1 so far: the 52 tropical and arid trees). For each one this module writes the
log, stripped log, planks, leaves and sapling (textures, models, blockstates, loot, tags, planks recipe), the tree's
configured feature and its placed features (one per density class of its biomes), the biome tags, the fruit and bark
items, the compositions, the lang lines, and runtime/trees.json for com.periodicelements.trees.Trees.

Rules Guilherme approved (2026-11-03): a sapling grows only in the biomes listed for its species, bone meal is not used
up elsewhere; the rubber sapling follows the same rule (jungle biomes). Vanilla trees stay; the new ones mix in.
"""
import csv
import json
import random
import zlib
from pathlib import Path

from PIL import Image

import trees_i18n

STAGE = 4
# Worldgen: one tree of a species every N chunks, by how wooded the biome is.
DENSITY = {"dense": 5, "open": 15, "sparse": 40}
BIOME_CLASS = {
    "jungle": "dense", "bamboo_jungle": "dense", "mangrove_swamp": "dense", "old_growth_pine_taiga": "dense",
    "sparse_jungle": "open", "savanna": "open", "savanna_plateau": "open", "windswept_savanna": "open",
    "wooded_badlands": "dense", "beach": "open",
    "desert": "sparse", "badlands": "sparse", "eroded_badlands": "sparse",
    # 2.9.0, temperate (and the cold biomes some temperate species already reach).
    "plains": "sparse", "sunflower_plains": "sparse", "forest": "dense", "flower_forest": "dense", "birch_forest": "dense",
    "old_growth_birch_forest": "dense", "dark_forest": "dense", "swamp": "open", "cherry_grove": "open",
    "windswept_hills": "open", "stony_shore": "open",
    "taiga": "dense", "windswept_forest": "dense", "snowy_taiga": "dense", "snowy_plains": "sparse", "snowy_beach": "open",
    "meadow": "open", "grove": "dense", "stony_peaks": "sparse", "windswept_gravelly_hills": "sparse",
    # 3.0.0, cold and mountain.
    "old_growth_spruce_taiga": "dense", "ice_spikes": "sparse", "snowy_slopes": "sparse", "frozen_peaks": "sparse",
    "jagged_peaks": "sparse",
}
DEFAULT_LOG = "cellulose:62;lignin:25;water:12;potassium_oxide:0.5;calcium_oxide:0.5"
DEFAULT_LEAVES = "block:minecraft:oak_leaves:100"
FRUIT_CHANCE = [0.05, 0.0625, 0.083333336, 0.1]
SAPLING_CHANCE = [0.05, 0.0625, 0.083333336, 0.1]

# Fruit, nuts and bark: id -> (English name, composition, colour, food (nutrition, saturation) or None, animal food tags).
PRODUCTS = {
    "brazil_nut": ("Brazil Nut", "vegetable_oil:66;protein:14;starch:6;cellulose:8;water:5.99;selenium:0.01", "7A5232", (3, 0.6), ()),
    "sapodilla": ("Sapodilla", "water:78;sucrose:8;glucose:6;cellulose:7;protein:1", "8A6448", (4, 0.4), ()),
    "andiroba_seed": ("Andiroba Seed", "vegetable_oil:50;cellulose:30;water:15;protein:5", "6A4A30", None, ()),
    "acai_berry": ("Acai Berry", "water:60;vegetable_oil:10;cellulose:20;glucose:4;protein:3;starch:3", "3A1A4A", (2, 0.3), ()),
    "babassu_nut": ("Babassu Nut", "vegetable_oil:60;cellulose:30;water:6;protein:4", "8A6A48", (2, 0.4), ()),
    "oil_palm_fruit": ("Oil Palm Fruit", "vegetable_oil:45;water:35;cellulose:15;protein:5", "D2502A", (2, 0.3), ()),
    "nutmeg": ("Nutmeg", "vegetable_oil:35;starch:30;cellulose:15;water:10;protein:7;potassium_oxide:3", "9A6A40", None, ()),
    "baobab_fruit": ("Baobab Fruit", "glucose:25;cellulose:45;water:12;starch:10;protein:3;calcium_oxide:2;potassium_oxide:3", "A8A070", (4, 0.5), ()),
    "marula": ("Marula Fruit", "water:80;sucrose:8;vegetable_oil:5;cellulose:7", "E0C040", (3, 0.4), ()),
    "shea_nut": ("Shea Nut", "vegetable_oil:50;starch:20;cellulose:20;water:7;protein:3", "8A6A40", None, ()),
    "pequi": ("Pequi", "water:50;vegetable_oil:30;cellulose:12;starch:5;protein:3", "C8B040", (3, 0.5), ()),
    "date": ("Date", "glucose:35;sucrose:30;water:21;cellulose:8;protein:2;potassium_oxide:2;starch:2", "8A4A20", (4, 0.6), ()),
    "mesquite_pod": ("Mesquite Pod", "sucrose:30;cellulose:30;protein:12;starch:15;water:10;vegetable_oil:3", "C8A860", (2, 0.3),
                     ("cow_food", "sheep_food", "goat_food")),
    "argan_nut": ("Argan Nut", "vegetable_oil:50;cellulose:40;protein:5;water:5", "A08040", None, ("goat_food",)),
    "palo_verde_seeds": ("Palo Verde Seeds", "protein:30;starch:40;cellulose:15;water:10;vegetable_oil:5", "A0A050", (1, 0.2),
                         ("chicken_food",)),
    "pine_nut": ("Pine Nut", "vegetable_oil:62;protein:14;starch:13;water:6;cellulose:5", "D8C090", (2, 0.5), ()),
    "juniper_berry": ("Juniper Berry", "water:35;glucose:25;cellulose:35;vegetable_oil:2;turpentine:3", "4A5A8A", None, ()),
    "acorn": ("Acorn", "starch:45;water:30;tannin:7;vegetable_oil:5;cellulose:10;protein:3", "8A6430", None, ("pig_food",)),
    "coconut": ("Coconut", "water:45;vegetable_oil:33;cellulose:16;sucrose:3;protein:3", "7A5030", (4, 0.6), ()),
    "tropical_almond": ("Tropical Almond", "vegetable_oil:50;protein:20;cellulose:15;water:5;starch:10", "8AA050", (3, 0.5), ()),
    "pandanus_fruit": ("Pandanus Fruit", "water:70;sucrose:10;cellulose:15;starch:5", "E08A30", (3, 0.4), ()),
    "sea_grape": ("Sea Grapes", "water:80;glucose:12;cellulose:6;protein:2", "6A2A5A", (2, 0.3), ()),
    "nipa_fruit": ("Nipa Fruit", "water:70;starch:20;glucose:5;cellulose:5", "7A5A30", (2, 0.3), ()),
    "buriti": ("Buriti Fruit", "water:50;vegetable_oil:20;cellulose:20;starch:7;glucose:3", "8A3A20", (3, 0.4), ()),
    "cinchona_bark": ("Cinchona Bark", "cellulose:55;lignin:25;quinine:7;tannin:5;water:8", "9A5A3A", None, ()),
    "cinnamon": ("Cinnamon", "cellulose:50;lignin:20;starch:16;cinnamaldehyde:2;water:10;calcium_oxide:2", "B0703A", None, ()),
    # 2.9.0, temperate.
    "walnut": ("Walnut", "vegetable_oil:65;protein:15;cellulose:7;starch:7;water:4;potassium_oxide:2", "9A7A50", (3, 0.6), ()),
    "mulberry": ("Mulberry", "water:85;glucose:9;sucrose:2.5;cellulose:2;protein:1.5", "C8A8C8", (2, 0.3), ()),
    "linden_flower": ("Linden Flower", "water:45;cellulose:40;glucose:10;protein:3;tannin:2", "E8E080", None, ()),
    "chestnut": ("Chestnut", "starch:45;water:44;sucrose:3;protein:3;cellulose:3;vegetable_oil:2", "7A4A2A", (3, 0.5), ("pig_food",)),
    "pear": ("Pear", "water:84;glucose:6;cellulose:6;sucrose:3;protein:1", "C8C050", (4, 0.4), ()),
    "elderberry": ("Elderberry", "water:80;glucose:7;cellulose:7;sucrose:5;protein:1", "3A2A4A", (2, 0.2), ()),
    "rowan_berry": ("Rowan Berry", "water:80;glucose:8;cellulose:8;sucrose:2;tannin:2", "E04A2A", None, ("chicken_food",)),
    "black_walnut": ("Black Walnut", "vegetable_oil:57;protein:24;cellulose:7;starch:6;water:4;potassium_oxide:2", "5A4A30", (3, 0.6), ()),
    "ginkgo_nut": ("Ginkgo Nut", "water:55;starch:35;protein:4;cellulose:4;vegetable_oil:2", "E0C870", (2, 0.3), ()),
    "olive": ("Olive", "water:60;vegetable_oil:20;cellulose:14;glucose:3;sodium_chloride:2;protein:1", "5A6A3A", (2, 0.3), ()),
    "carob_pod": ("Carob Pod", "sucrose:40;cellulose:35;water:10;protein:5;starch:5;tannin:5", "5A3A20", (2, 0.3),
                  ("cow_food", "sheep_food", "goat_food")),
    "cork": ("Cork", "suberin:45;lignin:25;cellulose:15;water:10;tannin:5", "B08050", None, ()),
    "sea_buckthorn_berry": ("Sea Buckthorn Berry", "water:83;glucose:5;vegetable_oil:5;cellulose:5;protein:1;sucrose:1", "F08A20",
                            (2, 0.4), ()),
    # 3.1.0, important species added after the plan (Guilherme).
    "araucaria_seed": ("Parana Pine Seed", "starch:45;water:45;protein:4;cellulose:4;vegetable_oil:2", "8A4A2A", (3, 0.5), ("pig_food",)),
    "mango": ("Mango", "water:83;sucrose:9;glucose:4;cellulose:2;protein:1;citric_acid:1", "F0A030", (4, 0.5), ()),
    "avocado": ("Avocado", "water:73;vegetable_oil:15;cellulose:7;protein:2;glucose:1;potassium_oxide:2", "3A5A20", (4, 0.7), ()),
    "cashew": ("Cashew", "vegetable_oil:44;starch:27;protein:18;water:5;cellulose:4;potassium_oxide:2", "D8C090", (3, 0.6), ()),
    "orange": ("Orange", "water:87;sucrose:5;glucose:4;cellulose:2;citric_acid:1;protein:1", "F08A20", (3, 0.5), ()),
    "lemon": ("Lemon", "water:89;citric_acid:5;glucose:2;cellulose:3;protein:1", "F0E040", (1, 0.2), ()),
    "papaya": ("Papaya", "water:88;glucose:7;sucrose:2;cellulose:2;protein:1", "F0903A", (3, 0.4), ()),
    "kapok_fiber": ("Kapok Fiber", "cellulose:64;lignin:13;water:10;vegetable_oil:3;starch:10", "F0ECE0", None, ()),
    "cherry": ("Cherry", "water:82;glucose:8;sucrose:6;cellulose:2;protein:1;citric_acid:1", "B01A2A", (2, 0.3), ()),
    "plum": ("Plum", "water:87;glucose:6;sucrose:4;cellulose:2;protein:1", "5A2A6A", (2, 0.3), ()),
    "peach": ("Peach", "water:89;sucrose:5;glucose:3;cellulose:2;protein:1", "F0A070", (3, 0.4), ()),
    "apricot": ("Apricot", "water:86;sucrose:6;glucose:4;cellulose:2;protein:1;citric_acid:1", "F0A040", (2, 0.3), ()),
    "fig": ("Fig", "water:79;glucose:9;sucrose:7;cellulose:3;protein:1;calcium_oxide:1", "6A3A5A", (3, 0.4), ()),
    "pomegranate": ("Pomegranate", "water:78;glucose:8;sucrose:6;cellulose:4;tannin:2;protein:2", "C02A2A", (3, 0.4), ()),
    "almond": ("Almond", "vegetable_oil:50;protein:21;cellulose:12;starch:10;water:5;calcium_oxide:2", "C89A6A", (3, 0.6), ()),
    "pistachio": ("Pistachio", "vegetable_oil:45;protein:20;starch:20;cellulose:10;water:4;potassium_oxide:1", "9AB050", (3, 0.6), ()),
    "hazelnut": ("Hazelnut", "vegetable_oil:61;protein:15;starch:10;cellulose:9;water:5", "A0703A", (3, 0.6), ()),
    "pecan": ("Pecan", "vegetable_oil:72;protein:9;starch:10;cellulose:5;water:4", "8A5A30", (3, 0.6), ()),
    "horse_chestnut": ("Horse Chestnut", "starch:40;water:45;tannin:5;cellulose:6;protein:3;vegetable_oil:1", "6A3A1A", None, ()),
    "willow_bark": ("Willow Bark", "cellulose:55;lignin:25;water:8;salicin:7;tannin:5", "8A7058", None, ()),
}
# What the Tree Tap gets from each tapped substance: liquids fill a glass flask, solids drop straight in.
# Vanilla items that a species drops as fruit: id -> (our lang key for the guide, leaf speckle colour). 2.9.0-beta.3:
# oak and dark oak leaves no longer drop apples (Guilherme); the apple tree does.
VANILLA_FRUIT = {"minecraft:apple": ("gui.periodic_elements.fruit.apple", "C83A2A")}
# Vanilla tree features rewritten without the oak in the tropics and the savanna (tools/vanilla_overrides, Guilherme).
VANILLA_OVERRIDES = Path(__file__).resolve().parent / "vanilla_overrides"
# Taps faster than the Tree Tap default (ticks per unit): maple sap is 3% sugar, so Guilherme asked for one a minute.
TAP_TICKS = {"maple_sap": 1200}
TAP_LIQUIDS = {"pine_resin", "copaiba_balsam", "palm_sap", "maple_sap", "urushi", "canada_balsam"}
# Substance items whose composition is not just themselves.
SUBSTANCE_COMPOSITIONS = {"palm_sap": "sucrose:15;water:85", "maple_sap": "sucrose:3;water:97",
                          "urushi": "urushiol:65;water:27;gum_arabic:8"}

# The Field Guide's hand-written paragraphs (each tools/trees_i18n.GUIDE language has the same three).
GUIDE = [
    "Every land biome has five trees of its own, real species that grow wild there. A sapling grows only in the biomes its "
    "species comes from: anywhere else it stays a sapling, and bone meal does nothing. Its tooltip names those biomes.",
    "Fruit and nuts drop from the leaves now and then. Strip a log with an axe to peel off the bark where the bark is the "
    "useful part. Fix a Tree Tap to a tree that gives sap or resin: liquids need an empty flask, hard resins drop straight "
    "into the tap.",
    "In the machines: pine resin in the Alembic gives turpentine and rosin; cinnamon bark, camphor wood, eucalyptus leaves "
    "and sandalwood give their oils in the Alembic; cinchona bark in the Digestion Vat gives quinine; brazilwood in the Dye "
    "Vat gives red dye. Oily fruit goes to the Oil Press, sweet fruit to the Fermentation Barrel.",
    "In the temperate woods: tap sugar and red maples for maple sap and boil it down in the Salt Pan for sugar. Strip cork "
    "from the cork oak and willow bark from the white willow; the Digestion Vat draws salicin from the bark, and later turns "
    "it into salicylic acid. Cedar of Lebanon gives cedar oil in the Alembic. The lacquer tree gives urushi lacquer. Yew "
    "leaves hold a trace of taxol, which only the last level can extract.",
    "In the cold and on the mountains: larches, firs and pines fill the taigas; the coast redwood and the giant sequoia, "
    "the tallest and the biggest trees on Earth, grow only in the old growth spruce taiga. Above the tree line, dwarf pines, "
    "junipers and the arctic willow creep low and take root in stone, gravel and snow. Tap the balsam fir for Canada "
    "balsam, the clear cement of lens makers, and the Scots pine and the larch for pine resin.",
    "Fruit trees: mango, orange, lemon, papaya, avocado and cashew in the tropics; cherry, plum, peach, apricot, fig, "
    "pomegranate, almond, pistachio, hazel and pecan in the temperate lands. The Parana pine of southern Brazil gives "
    "edible seeds and resin. Lemons carry citric acid, which the Digestion Vat crystallizes out. Horse chestnuts are not "
    "for eating.",
]
GUIDE_T = {"trees_leaves": "{x} from the leaves", "trees_bark": "{x} from the bark", "trees_tap": "{x} from a Tree Tap",
           "trees_wood": "wood with {x}", "trees_leafy": "leaves with {x}", "trees_fuel": "dense firewood", "trees_none": "timber"}
NOTABLE = {"brazilin", "camphor", "santalol", "tannin", "sodium_chloride", "eucalyptol", "taxol", "cedrol"}

MANGROVE_ROOTS = {"type": "minecraft:mangrove_root_placer", "above_root_placement": {
    "above_root_placement_chance": 0.5, "above_root_provider": {"type": "minecraft:simple_state_provider",
                                                                "state": {"Name": "minecraft:moss_carpet"}}},
    "mangrove_root_placement": {"can_grow_through": "#minecraft:mangrove_roots_can_grow_through", "max_root_length": 15,
                                "max_root_width": 8, "muddy_roots_in": ["minecraft:mud", "minecraft:muddy_mangrove_roots"],
                                "muddy_roots_provider": {"type": "minecraft:simple_state_provider", "state": {
                                    "Name": "minecraft:muddy_mangrove_roots", "Properties": {"axis": "y"}}},
                                "random_skew_chance": 0.2},
    "root_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:mangrove_roots",
                                                                           "Properties": {"waterlogged": "false"}}},
    "trunk_offset_y": {"type": "minecraft:uniform", "max_inclusive": 3, "min_inclusive": 1}}


def _u(lo, hi):
    return {"type": "minecraft:uniform", "min_inclusive": lo, "max_inclusive": hi}


def _shape(shape, mod):
    """Trunk placer, foliage placer and minimum size of each crown shape."""
    two = lambda limit=1, lower=0, upper=1: {"type": "minecraft:two_layers_feature_size", "limit": limit,
                                             "lower_size": lower, "upper_size": upper}
    straight = lambda b, a, c=0: {"type": "minecraft:straight_trunk_placer", "base_height": b, "height_rand_a": a, "height_rand_b": c}
    blob = lambda r, h=3, o=0: {"type": "minecraft:blob_foliage_placer", "radius": r, "offset": o, "height": h}
    if shape == "round":
        return straight(5, 2), blob(2), two()
    if shape == "tall":
        return straight(10, 4), blob(3, 3), two(1, 0, 2)
    if shape == "fancy":
        return ({"type": "minecraft:fancy_trunk_placer", "base_height": 8, "height_rand_a": 4, "height_rand_b": 0},
                {"type": "minecraft:fancy_foliage_placer", "radius": 2, "offset": 4, "height": 4},
                {"type": "minecraft:two_layers_feature_size", "limit": 0, "lower_size": 0, "upper_size": 0, "min_clipped_height": 4})
    if shape == "giant":
        return ({"type": "minecraft:giant_trunk_placer", "base_height": 14, "height_rand_a": 4, "height_rand_b": 4},
                blob(4, 3), two(1, 1, 2))
    if shape == "giant_short":
        return ({"type": "minecraft:giant_trunk_placer", "base_height": 6, "height_rand_a": 2, "height_rand_b": 0},
                {"type": "minecraft:acacia_foliage_placer", "radius": 3, "offset": 0}, two(1, 1, 2))
    if shape == "umbrella":
        return ({"type": "minecraft:forking_trunk_placer", "base_height": 4, "height_rand_a": 2, "height_rand_b": 2},
                {"type": "minecraft:acacia_foliage_placer", "radius": 2, "offset": 0}, two())
    if shape == "bush":
        return straight(1, 1), {"type": "minecraft:bush_foliage_placer", "radius": 2, "offset": 1, "height": 2}, two(0, 0, 0)
    if shape == "pine":
        return straight(9, 4), {"type": "minecraft:pine_foliage_placer", "radius": 1, "offset": 1, "height": _u(3, 4)}, two(2, 0, 2)
    if shape == "cone":
        return (straight(5, 2, 1), {"type": "minecraft:spruce_foliage_placer", "radius": _u(2, 3), "offset": _u(0, 2),
                                     "trunk_height": _u(1, 2)}, two(2, 0, 2))
    if shape == "palm":
        return straight(6, 3), {"type": f"{mod}:palm_foliage_placer", "radius": 3, "offset": 0}, two(1, 0, 1)
    if shape == "palm_short":
        return straight(2, 1), {"type": f"{mod}:palm_foliage_placer", "radius": 3, "offset": 0}, two(1, 0, 1)
    if shape == "tall_cone":
        return (straight(14, 6, 2), {"type": "minecraft:spruce_foliage_placer", "radius": _u(2, 3), "offset": _u(0, 2),
                                      "trunk_height": _u(3, 5)}, two(2, 0, 2))
    if shape == "mega":
        return ({"type": "minecraft:giant_trunk_placer", "base_height": 26, "height_rand_a": 6, "height_rand_b": 8},
                {"type": "minecraft:mega_pine_foliage_placer", "radius": 0, "offset": 0, "crown_height": _u(12, 16)},
                two(1, 1, 2))
    if shape == "creeping":
        return straight(1, 0), {"type": "minecraft:bush_foliage_placer", "radius": 2, "offset": 0, "height": 1}, two(0, 0, 0)
    if shape == "dark":
        return ({"type": "minecraft:dark_oak_trunk_placer", "base_height": 6, "height_rand_a": 2, "height_rand_b": 1},
                {"type": "minecraft:dark_oak_foliage_placer", "radius": 0, "offset": 0},
                {"type": "minecraft:three_layers_feature_size", "limit": 1, "upper_limit": 1, "lower_size": 0, "middle_size": 1,
                 "upper_size": 2})
    if shape in ("spreading", "weeping"):
        weep = shape == "weeping"
        return ({"type": "minecraft:cherry_trunk_placer", "base_height": 6 if weep else 7, "height_rand_a": 1, "height_rand_b": 0,
                 "branch_count": {"type": "minecraft:weighted_list", "distribution": [
                     {"data": 1, "weight": 1}, {"data": 2, "weight": 1}, {"data": 3, "weight": 1}]},
                 "branch_horizontal_length": _u(2, 4), "branch_start_offset_from_top": {"min_inclusive": -4, "max_inclusive": -3},
                 "branch_end_offset_from_top": _u(-1, 0)},
                {"type": "minecraft:cherry_foliage_placer", "radius": 4, "offset": 0, "height": 5,
                 "wide_bottom_layer_hole_chance": 0.25, "corner_hole_chance": 0.25,
                 "hanging_leaves_chance": 0.6 if weep else 0.16666667,
                 "hanging_leaves_extension_chance": 0.7 if weep else 0.33333334}, two(1, 0, 2))
    if shape == "mangrove":
        return ({"type": "minecraft:upwards_branching_trunk_placer", "base_height": 2, "can_grow_through": f"#{mod}:mangrove_grow_through",
                 "extra_branch_length": _u(0, 1), "extra_branch_steps": _u(1, 4), "height_rand_a": 1, "height_rand_b": 4,
                 "place_branch_per_log_probability": 0.5},
                {"type": "minecraft:random_spread_foliage_placer", "foliage_height": 2, "leaf_placement_attempts": 70,
                 "offset": 0, "radius": 3}, two(2, 0, 2))
    raise ValueError(shape)


def load_species(root):
    out = []
    with (Path(root) / "data" / "trees.csv").open(newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            if int(r["stage"]) > STAGE:
                continue
            r["biomes"] = r["biomes"].split(";")
            r["fuel"] = int(r["fuel"])
            for b in r["biomes"]:
                assert b in BIOME_CLASS, (r["id"], b)
            for k in ("fruit", "bark_item"):
                assert not r[k] or r[k] in PRODUCTS or r[k] in VANILLA_FRUIT, (r["id"], r[k])
            assert r["id"] in trees_i18n.SPECIES, r["id"]
            out.append(r)
    return out


def _rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def _shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def _seed(s):
    return zlib.crc32(s.encode())


def _noise(base, var, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = rnd.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


def _put(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.load()[x, y] = tuple(c) + (255,) if len(c) == 3 else tuple(c)


def _textures(sp, tex):
    sid, bark, wood, leaf = sp["id"], _rgb(sp["bark"]), _rgb(sp["wood"]), _rgb(sp["leaf"])
    rnd = random.Random(_seed(sid))
    # Bark: noise with darker vertical furrows.
    side = _noise(bark, 9, _seed(sid + "b"))
    for x in range(16):
        if rnd.random() < 0.35:
            y0 = rnd.randint(0, 15)
            for y in range(y0, min(16, y0 + rnd.randint(4, 12))):
                _put(side, x, y, _shade(bark, 0.72))
    side.save(tex / f"{sid}_log.png")
    # Log end: growth rings in the wood colour, the bark round the edge.
    for name, rim in ((f"{sid}_log_top", bark), (f"stripped_{sid}_log_top", _shade(wood, 0.8))):
        top = _noise(wood, 6, _seed(name))
        for y in range(16):
            for x in range(16):
                r = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
                if abs(r - 5.5) < 0.6 or abs(r - 2.8) < 0.6:
                    _put(top, x, y, _shade(wood, 0.82))
                if x in (0, 15) or y in (0, 15):
                    _put(top, x, y, rim)
        top.save(tex / f"{name}.png")
    # Stripped side: the wood, with fine vertical grain.
    strip = _noise(wood, 6, _seed(sid + "s"))
    for x in range(0, 16, 3):
        for y in range(16):
            if rnd.random() < 0.7:
                _put(strip, x, y, _shade(wood, 0.86))
    strip.save(tex / f"stripped_{sid}_log.png")
    # Planks: four boards with dark seams and staggered joints.
    planks = _noise(_shade(wood, 1.05), 6, _seed(sid + "p"))
    for y in (3, 7, 11, 15):
        for x in range(16):
            _put(planks, x, y, _shade(wood, 0.7))
    for i, y in enumerate((0, 4, 8, 12)):
        jx = (5 + i * 7) % 16
        for yy in range(y, y + 3):
            _put(planks, jx, yy, _shade(wood, 0.7))
    planks.save(tex / f"{sid}_planks.png")
    # Leaves: speckled, with gaps (cutout).
    leaves = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.8:
                g = rnd.randint(-24, 24)
                _put(leaves, x, y, tuple(max(0, min(255, c + g)) for c in leaf))
    if sp["fruit"]:
        fc = _rgb(VANILLA_FRUIT[sp["fruit"]][1] if sp["fruit"] in VANILLA_FRUIT else PRODUCTS[sp["fruit"]][2])
        for _ in range(3):
            x, y = rnd.randint(1, 14), rnd.randint(1, 14)
            _put(leaves, x, y, fc)
    leaves.save(tex / f"{sid}_leaves.png")
    _sapling(sp, leaf, bark).save(tex / f"{sid}_sapling.png")


def _sapling(sp, leaf, bark):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    shape = sp["shape"]
    dark = _shade(leaf, 0.8)
    if shape.startswith("palm"):
        for y in range(9, 16):
            _put(img, 7, y, bark)
        for dx, dy in ((-1, 0), (1, 0), (-1, -1), (1, -1), (0, -1)):
            for i in range(1, 6):
                _put(img, 7 + dx * i, 8 + dy * i + (i * i) // 6, leaf if i % 2 else dark)
    elif shape in ("pine", "cone"):
        for y in range(11, 16):
            _put(img, 7, y, bark)
        for row, half in enumerate((0, 1, 1, 2, 2, 3, 3, 4)):
            for x in range(7 - half, 8 + half):
                _put(img, x, 3 + row, leaf if (x + row) % 3 else dark)
    elif shape in ("umbrella", "giant_short"):
        for y in range(8, 16):
            _put(img, 7, y, bark)
        _put(img, 6, 9, bark)
        _put(img, 8, 9, bark)
        for x in range(2, 14):
            for y in (5, 6, 7):
                if abs(x - 7.5) < 6 - (y == 5) * 2:
                    _put(img, x, y, leaf if (x + y) % 3 else dark)
    else:
        for y in range(9, 16):
            _put(img, 7, y, bark)
        for y in range(2, 10):
            for x in range(3, 13):
                if (x - 7.5) ** 2 + (y - 5.5) ** 2 < 15:
                    _put(img, x, y, leaf if (x * 3 + y) % 4 else dark)
    return img


def product_texture(pid):
    name, _, color, _, _ = PRODUCTS[pid]
    c = _rgb(color)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    hi, lo = _shade(c, 1.3), _shade(c, 0.7)
    if pid in ("cinchona_bark", "cinnamon"):
        for y in range(3, 14):
            for x in range(4, 12):
                edge = x in (4, 11)
                _put(img, x, y, lo if edge else (c if (y + x) % 4 else hi))
        if pid == "cinnamon":
            for y in range(3, 14):
                _put(img, 7, y, lo)
        return img
    if pid in ("mesquite_pod",):
        for i in range(12):
            x, y = 2 + i, 12 - i + (i // 4)
            _put(img, x, y, c)
            _put(img, x, y - 1, hi)
            _put(img, x, y + 1, lo)
        return img
    if pid in ("palo_verde_seeds", "sea_grape", "acai_berry", "juniper_berry", "date"):
        spots = [(5, 6), (9, 5), (7, 9), (10, 10), (5, 11)] if pid != "date" else [(6, 6), (9, 9)]
        r = 2 if pid != "date" else 3
        for sx, sy in spots:
            for y in range(sy - r, sy + r + 1):
                for x in range(sx - r + 1, sx + r):
                    if (x - sx) ** 2 + ((y - sy) * (0.7 if pid == "date" else 1)) ** 2 <= r * r:
                        _put(img, x, y, hi if (x < sx and y < sy) else c)
        return img
    # A round fruit or nut with a highlight and a stalk.
    rx, ry = (5, 5) if pid in ("coconut", "baobab_fruit", "babassu_nut", "pequi") else (4, 4)
    if pid in ("baobab_fruit", "tropical_almond", "brazil_nut", "andiroba_seed", "argan_nut", "shea_nut", "acorn"):
        ry += 1
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) / rx) ** 2 + ((y - 8.5) / ry) ** 2
            if d <= 1:
                _put(img, x, y, hi if (x < 7 and y < 7) else (lo if d > 0.7 else c))
    if pid == "acorn":
        for y in range(3, 7):
            for x in range(4, 12):
                _put(img, x, y, (110, 80, 40))
    else:
        _put(img, 8, 2, (90, 70, 40))
        _put(img, 8, 3, (90, 70, 40))
    return img


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def block_names_en(sp):
    n = sp["name"]
    stem = n[:-5] if n.endswith(" Tree") else n
    return [f"{stem} Log", f"Stripped {stem} Log", f"{stem} Planks", f"{stem} Leaves", f"{stem} Sapling"]


def ids(sid):
    return [f"{sid}_log", f"stripped_{sid}_log", f"{sid}_planks", f"{sid}_leaves", f"{sid}_sapling"]


def composition_rows(root):
    """Extra rows for compositions.csv: (id, amount, composition)."""
    rows = []
    for sp in load_species(root):
        sid = sp["id"]
        log = sp["log"] or DEFAULT_LOG
        rows.append((f"periodic_elements:{sid}_log", 1000.0, log))
        rows.append((f"periodic_elements:stripped_{sid}_log", 1000.0, log))
        rows.append((f"periodic_elements:{sid}_planks", 250.0, f"block:periodic_elements:{sid}_log:100"))
        rows.append((f"periodic_elements:{sid}_leaves", 1000.0, sp["leaves"] or DEFAULT_LEAVES))
    for pid, (_, comp, _, _, _) in PRODUCTS.items():
        rows.append((f"periodic_elements:{pid}", 1000.0 / 9.0, comp))
    for sid, comp in SUBSTANCE_COMPOSITIONS.items():
        rows.append((f"periodic_elements:{sid}", 1000.0 / 9.0, comp))
    return rows


def generate_trees(root, assets, data_dir, runtime_dir, lang, mod):
    species = load_species(root)
    tex = assets / "textures" / "block"
    tex.mkdir(parents=True, exist_ok=True)
    models, states = assets / "models", assets / "blockstates"
    m = lambda n: f"{mod}:block/{n}"
    loot = data_dir / mod / "loot_table" / "blocks"
    mc_tags = data_dir / "minecraft" / "tags"
    tags = {}

    def tag(kind, name, value):
        tags.setdefault((kind, name), []).append(f"{mod}:{value}")

    runtime = []
    for sp in species:
        sid = sp["id"]
        log, stripped, planks, leaves, sapling = ids(sid)
        _textures(sp, tex)
        for b, top in ((log, f"{sid}_log_top"), (stripped, f"stripped_{sid}_log_top")):
            _dump(models / "block" / f"{b}.json", {"parent": "minecraft:block/cube_column",
                                                   "textures": {"end": m(top), "side": m(b)}})
            _dump(models / "block" / f"{b}_horizontal.json", {"parent": "minecraft:block/cube_column_horizontal",
                                                              "textures": {"end": m(top), "side": m(b)}})
            _dump(states / f"{b}.json", {"variants": {
                "axis=y": {"model": m(b)}, "axis=z": {"model": m(f"{b}_horizontal"), "x": 90},
                "axis=x": {"model": m(f"{b}_horizontal"), "x": 90, "y": 90}}})
        _dump(models / "block" / f"{planks}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m(planks)}})
        _dump(states / f"{planks}.json", {"variants": {"": {"model": m(planks)}}})
        _dump(models / "block" / f"{leaves}.json", {"parent": "minecraft:block/leaves", "textures": {"all": m(leaves)}})
        _dump(states / f"{leaves}.json", {"variants": {"": {"model": m(leaves)}}})
        _dump(models / "block" / f"{sapling}.json", {"parent": "minecraft:block/cross", "textures": {"cross": m(sapling)}})
        _dump(states / f"{sapling}.json", {"variants": {"": {"model": m(sapling)}}})
        for b in (log, stripped, planks, leaves):
            _dump(models / "item" / f"{b}.json", {"parent": m(b)})
        _dump(models / "item" / f"{sapling}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": m(sapling)}})

        # Loot: logs, planks and saplings drop themselves; leaves give the sapling, sticks and fruit now and then.
        for b in (log, stripped, planks, sapling):
            _dump(loot / f"{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
                {"type": "minecraft:item", "name": f"{mod}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
        shears = {"condition": "minecraft:any_of", "terms": [
            {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
            {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
                {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}
        pools = [
            {"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": f"{mod}:{leaves}", "conditions": [shears]},
                {"type": "minecraft:item", "name": f"{mod}:{sapling}", "conditions": [
                    {"condition": "minecraft:survives_explosion"},
                    {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": SAPLING_CHANCE}]}]}]},
            {"rolls": 1, "conditions": [{"condition": "minecraft:inverted", "term": shears}], "entries": [
                {"type": "minecraft:item", "name": "minecraft:stick", "conditions": [
                    {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                     "chances": [0.02, 0.022222223, 0.025, 0.033333335, 0.1]}]}]}]
        if sp["fruit"]:
            pools.append({"rolls": 1, "conditions": [{"condition": "minecraft:inverted", "term": shears}], "entries": [
                {"type": "minecraft:item", "name": sp["fruit"] if ":" in sp["fruit"] else f"{mod}:{sp['fruit']}", "conditions": [
                    {"condition": "minecraft:survives_explosion"},
                    {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": FRUIT_CHANCE}]}]})
        _dump(loot / f"{leaves}.json", {"type": "minecraft:block", "pools": pools})

        # Planks from either log.
        _dump(data_dir / mod / "tags" / "item" / f"{sid}_logs.json", {"values": [f"{mod}:{log}", f"{mod}:{stripped}"]})
        _dump(data_dir / mod / "recipe" / f"{planks}.json", {
            "type": "minecraft:crafting_shapeless", "category": "building", "group": "planks",
            "ingredients": [{"tag": f"{mod}:{sid}_logs"}], "result": {"id": f"{mod}:{planks}", "count": 4}})

        for kind in ("block", "item"):
            for b in (log, stripped):
                tag(kind, "logs", b)
                tag(kind, "logs_that_burn", b)
            tag(kind, "planks", planks)
            tag(kind, "leaves", leaves)
            tag(kind, "saplings", sapling)
        for b in (log, stripped, planks):
            tag("block", "mineable/axe", b)
        tag("block", "mineable/hoe", leaves)

        # Biome tags: where it may grow, and where it is placed at each density.
        _dump(data_dir / mod / "tags" / "worldgen" / "biome" / "trees" / f"{sid}.json",
              {"values": [f"minecraft:{b}" for b in sp["biomes"]]})
        classes = sorted({BIOME_CLASS[b] for b in sp["biomes"]})
        wg = data_dir / mod / "worldgen"
        trunk, foliage, size = _shape(sp["shape"], mod)
        soil = {"dirt": "minecraft:dirt", "sand": "minecraft:dirt", "mud": "minecraft:mud", "rock": "minecraft:coarse_dirt",
                "alpine": "minecraft:coarse_dirt"}[sp["soil"]]
        config = {"ignore_vines": True, "force_dirt": False, "minimum_size": size,
                  "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": soil}},
                  "trunk_provider": {"type": "minecraft:simple_state_provider",
                                     "state": {"Name": f"{mod}:{log}", "Properties": {"axis": "y"}}},
                  "foliage_provider": {"type": "minecraft:simple_state_provider", "state": {
                      "Name": f"{mod}:{leaves}", "Properties": {"distance": "7", "persistent": "false", "waterlogged": "false"}}},
                  "trunk_placer": trunk, "foliage_placer": foliage, "decorators": []}
        if sp["shape"] == "mangrove":
            config["root_placer"] = MANGROVE_ROOTS
        _dump(wg / "configured_feature" / "tree" / f"{sid}.json", {"type": "minecraft:tree", "config": config})
        for cls in classes:
            _dump(data_dir / mod / "tags" / "worldgen" / "biome" / "trees" / f"{sid}_{cls}.json",
                  {"values": [f"minecraft:{b}" for b in sp["biomes"] if BIOME_CLASS[b] == cls]})
            _dump(wg / "placed_feature" / "tree" / f"{sid}_{cls}.json", {"feature": f"{mod}:tree/{sid}", "placement": [
                {"type": "minecraft:rarity_filter", "chance": DENSITY[cls]},
                {"type": "minecraft:in_square"},
                {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 2 if sp["soil"] == "mud" else 0},
                {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
                {"type": "minecraft:block_predicate_filter", "predicate": {
                    "type": "minecraft:would_survive", "state": {"Name": f"{mod}:{sapling}", "Properties": {"stage": "0"}}}},
                {"type": "minecraft:biome"}]})

        en = block_names_en(sp)
        for b, name in zip(ids(sid), en):
            lang[f"block.{mod}.{b}"] = name
        lang[f"tree.{mod}.{sid}"] = sp["name"]
        tap = sp["tap"] or None
        runtime.append({"id": sid, "biomes": sp["biomes"], "soil": sp["soil"], "fuel": sp["fuel"], "classes": classes,
                        "fruit": sp["fruit"] or None, "bark": sp["bark_item"] or None,
                        "tap": tap, "tapFlask": tap in TAP_LIQUIDS if tap else False,
                        "tapTicks": TAP_TICKS.get(tap, 0)})

    # Fruit, nuts and bark.
    for pid, (name, _, _, food, feed) in PRODUCTS.items():
        lang[f"item.{mod}.{pid}"] = name
        _dump(models / "item" / f"{pid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{pid}"}})
        for t in feed:
            tags.setdefault(("item", t), []).append(f"{mod}:{pid}")
    # Mangroves may branch through their own leaves and logs.
    _dump(data_dir / mod / "tags" / "block" / "mangrove_grow_through.json", {"values": ["#minecraft:mangrove_logs_can_grow_through"] + [
        f"{mod}:{b}" for sp in species if sp["shape"] == "mangrove" for b in (f"{sp['id']}_log", f"{sp['id']}_leaves")]})

    for (kind, name), values in tags.items():
        path = mc_tags / kind / f"{name}.json"
        old = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
        old += [v for v in values if v not in old]
        _dump(path, {"replace": False, "values": old})

    _dump(runtime_dir / "trees.json", {"species": runtime, "products": {
        pid: {"food": list(p[3]) if p[3] else None} for pid, p in PRODUCTS.items()}})
    for b in BIOME_CLASS:
        lang[f"gui.{mod}.biome.{b}"] = trees_i18n.BIOMES[b].split(" / ")[0]
    lang[f"gui.{mod}.fruit.apple"] = "Apple"
    for src in VANILLA_OVERRIDES.rglob("*.json"):
        dst = data_dir / "minecraft" / src.relative_to(VANILLA_OVERRIDES)
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
    lang[f"gui.{mod}.sapling.biomes"] = "Grows only in: %s"
    lang[f"gui.{mod}.sapling.wrong_biome"] = "This sapling grows only in: %s"
    lang[f"gui.{mod}.tap.ready"] = "Tree Tap: %s of %s ready (%s)"
    lang[f"gui.{mod}.tap.wild"] = "Nothing flows: this tree gives it only where it grows wild"
    return species


def write_item_textures(assets):
    out = assets / "textures" / "item"
    for pid in PRODUCTS:
        product_texture(pid).save(out / f"{pid}.png")


def translations(code):
    """English -> translation for everything trees add, in one language."""
    out = trees_i18n.text(code)
    with (Path(__file__).resolve().parent.parent / "data" / "trees.csv").open(newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            if int(r["stage"]) > STAGE:
                continue
            names, tree = trees_i18n.block_names(code, r["id"])
            for en, tr in zip(block_names_en(r), names):
                out[en] = tr
            out[r["name"]] = tree
    for b in BIOME_CLASS:
        out[trees_i18n.BIOMES[b].split(" / ")[0]] = trees_i18n.biome_name(code, b)
    return out


def guide_paragraphs(L, species):
    """The generated part of the chapter: one heading per biome, one line per tree."""
    lang, mod, T = L.lang, L.mod, L.t
    text = []
    by_biome = {}
    for sp in species:
        for b in sp["biomes"]:
            by_biome.setdefault(b, []).append(sp)
    sub_name = lambda s: L.name(s)
    for b in BIOME_CLASS:
        if b not in by_biome:
            continue
        text.append("# " + lang[f"gui.{mod}.biome.{b}"])
        for sp in by_biome[b]:
            what = []
            if sp["fruit"]:
                what.append(T["trees_leaves"].format(
                    x=lang[VANILLA_FRUIT[sp["fruit"]][0]] if sp["fruit"] in VANILLA_FRUIT else L.name(sp["fruit"])))
            if sp["bark_item"]:
                what.append(T["trees_bark"].format(x=L.name(sp["bark_item"])))
            if sp["tap"]:
                what.append(T["trees_tap"].format(x=L.name(sp["tap"])))
            for part, key in ((sp["log"], "trees_wood"), (sp["leaves"], "trees_leafy")):
                for entry in (part or "").split(";"):
                    sub = entry.split(":")[0]
                    if sub in NOTABLE:
                        what.append(T[key].format(x=sub_name(sub)))
            if sp["fuel"] >= 450:
                what.append(T["trees_fuel"])
            if not what:
                what.append(T["trees_none"])
            tree = lang[f"tree.{mod}.{sp['id']}"]
            text.append(f"- {tree} ({sp['scientific']}): " + "; ".join(what))
    return text
