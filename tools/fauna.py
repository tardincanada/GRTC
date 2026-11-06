"""Animals of the world (3.50.0, the fauna plan): entity data, models, skins, items, blocks, loot, recipes, worldgen,
compositions and the Animal Compendium's runtime entries.

Species: tools/fauna_species.py (S, SMALL, VANILLA). Models and skins: tools/fauna_models.py. English text:
tools/fauna_text.py; translations: tools/fauna_i18n.py. Java: com.periodicelements.fauna (Fauna registers everything,
FaunaAnimal / FaunaSwimmer / FaunaSchoolFish are the three entity classes, the blocks and items live beside them),
client/FaunaModel + FaunaRenderer draw them from runtime fauna_models.json.

STAGE limits the species built (the plan's four stages: 1 tropical + arid, 2 temperate + rivers, 3 cold + mountains,
4 sea + coast).
"""
import csv
import json
import math
import random

from PIL import Image, ImageDraw

import fauna_models as FM
import fauna_species as FS
import fauna_text as FT

STAGE = 4
P = "periodic_elements:"


def species(stage=None):
    stage = STAGE if stage is None else stage
    return [s for s in FS.S if s["stage"] <= stage]


def small(stage=None):
    stage = STAGE if stage is None else stage
    return {k: v for k, v in FS.SMALL.items() if v["stage"] <= stage}


# item id -> (English name, composition, food (nutrition, saturation) or None, icon, stage, tooltip or None)
ITEMS = {
    "game_meat": ("Raw Game Meat", "water:70;protein:21;tallow:9", (3, 0.3), "meat", 1, None),
    "cooked_game_meat": ("Cooked Game Meat", "water:58;protein:30;tallow:12", (8, 0.8), "cooked_meat", 1, None),
    "ostrich_egg": ("Ostrich Egg", "water:66;protein:12;tallow:10;calcium_carbonate:12", None, "big_egg", 1, "Cook it: as much as four hen's eggs"),
    "cooked_egg": ("Boiled Egg", "water:74;protein:13;tallow:11;calcium_carbonate:2", (5, 0.6), "boiled_egg", 1, None),
    "scorpion_venom": ("Scorpion Venom", "water:60;protein:40", None, "vial_clear", 1, "Milked into a bottle; the horse serum against it is antivenom"),
    "gila_venom": ("Gila Monster Venom", "water:60;protein:39;exenatide:1", None, "vial_clear", 1, "Milked into a bottle"),
    "antivenom": ("Antivenom", "water:90;protein:10", None, "vial_amber", 1, "Drink it: cures poison"),
    "locust": ("Desert Locust", "protein:60;tallow:15;water:25", (2, 0.3), "bug", 1, None),
    "cochineal": ("Cochineal", "carminic_acid:20;protein:35;tallow:10;water:35", None, "cochineal", 1, "Scraped off cactus; use it on a cactus to start a colony"),
    "sticklac": ("Sticklac", "shellac:70;cellulose:20;protein:5;water:5", None, "sticklac", 1, "Scraped off jungle wood; use it on a jungle log to start a colony"),
    "silkworm": ("Silkworm", "water:78;protein:20;tallow:2", None, "silkworm", 1, "Found shearing white mulberry leaves; put it in a Sericulture Tray"),
    "silk_cocoon": ("Silk Cocoon", "fibroin:75;protein:20;water:5", None, "cocoon", 1, "Boil it (smelt) to reel the silk"),
    "silk": ("Silk Thread", "fibroin:100", None, "silk", 1, None),
    "earthworm": ("Earthworm", "water:85;protein:12;tallow:3", None, "worm", 1, "Dug up with dirt; put it in a Worm Bin"),
    "humus": ("Humus", "lignin:40;cellulose:20;water:30;protein:6;phosphorus_pentoxide:2;potassium_oxide:2", None, "humus", 1, "Worm compost: use it on a plant like bone meal"),
    "leech": ("Medicinal Leech", "water:84;protein:15;hirudin:1", None, "leech", 1, None),
    "manure": ("Manure", "cellulose:40;lignin:15;water:30;protein:7;urea:3;potassium_oxide:2;phosphorus_pentoxide:3", None, "dung", 1, "Use it on a plant like bone meal; dried, it burns"),
    "pheasant_egg": ("Pheasant Egg", "water:66;protein:12;tallow:10;calcium_carbonate:12", None, "egg_olive", 2, None),
    "duck_egg": ("Duck Egg", "water:66;protein:12;tallow:10;calcium_carbonate:12", None, "egg_blue", 2, None),
    "duck_down": ("Duck Down", "protein:90;water:10", None, "down", 2, "The warmest feathers"),
    "antler": ("Antler", "tricalcium_phosphate:55;protein:35;water:10", None, "antler", 2, "Shed once a year; ground, it gives bone meal"),
    "boar_bristle": ("Boar Bristle", "protein:95;water:5", None, "bristle", 2, None),
    "caviar": ("Caviar", "protein:26;tallow:18;water:50;sodium_chloride:6", (4, 0.8), "roe", 2, None),
    "isinglass": ("Isinglass", "gelatin:85;water:15", None, "isinglass", 2, "Sturgeon swim bladder: clears beer and wine"),
    "castoreum": ("Castoreum", "tallow:40;protein:20;water:30;salicin:10", None, "castoreum", 2, "Beaver scent gland: perfume and old medicine; its salicin comes from the willow bark the beaver eats"),
    "galalith": ("Galalith", "casein:95;formaldehyde:5", None, "galalith", 2, "Milk-stone: the plastic of buttons and pens"),
    "yak_wool": ("Yak Wool", "protein:84;tallow:10;water:6", None, "wool_brown", 3, None),
    "alpaca_wool": ("Alpaca Wool", "protein:86;tallow:8;water:6", None, "wool_cream", 3, None),
    "blubber": ("Blubber", "marine_oil:70;protein:10;water:20", None, "blubber", 4, "Rendered, it gives lamp oil"),
    "ivory": ("Walrus Ivory", "tricalcium_phosphate:65;protein:25;water:10", None, "tusk", 4, None),
    "shark_liver": ("Shark Liver", "squalene:40;marine_oil:30;protein:10;water:19;cholecalciferol:1", None, "liver", 4, None),
    "greenland_shark_meat": ("Raw Greenland Shark", "water:70;protein:18;urea:6;tallow:6", (2, 0.1), "shark_meat", 4, "Poisonous raw: cure it (smoke or cook) into hákarl"),
    "hakarl": ("Hákarl", "water:55;protein:30;tallow:9;ammonia:6", (6, 0.6), "hakarl", 4, None),
    "herring": ("Raw Herring", "water:65;protein:18;marine_oil:17", (2, 0.1), "herring", 4, None),
    "cooked_herring": ("Cooked Herring", "water:55;protein:25;marine_oil:20", (6, 0.8), "cooked_herring", 4, None),
    "murex_snail": ("Murex Snail", "calcium_carbonate:60;protein:25;water:14;dibromoindigo:1", None, "murex", 4, "Its gland gives Tyrian purple in a Dye Vat"),
    "oyster_shell": ("Oyster Shell", "calcium_carbonate:95;protein:5", None, "oyster", 4, "Burns to lime in a Lime Kiln"),
    "pearl": ("Pearl", "calcium_carbonate:92;protein:5;water:3", None, "pearl", 4, None),
    "sepia_ink": ("Sepia Ink", "water:75;melanin:20;protein:5", None, "ink_brown", 4, None),
    "cuttlebone": ("Cuttlebone", "calcium_carbonate:85;chitin:10;water:5", None, "cuttlebone", 4, None),
    "horseshoe_crab_blood": ("Horseshoe Crab Blood", "water:88;hemocyanin:8;limulus_lysate:2;protein:2", None, "vial_blue", 4, "Bled into a bottle: the crab lives on"),
    "crab_shell": ("Crab Shell", "calcium_carbonate:45;chitin:35;protein:20", None, "crab_shell", 4, None),
    "spermaceti": ("Spermaceti", "cetyl_palmitate:90;marine_oil:10", None, "spermaceti", 4, "Wax from the head of a stranded sperm whale: the finest candles"),
    "ambergris": ("Ambergris", "ambrein:45;tallow:35;water:20", None, "ambergris", 4, "Washed ashore from a sperm whale's gut: it fixes the scent of perfume"),
    "aequorea_tissue": ("Crystal Jelly Tissue", "water:96;protein:3;gfp:1", None, "jelly", 4, "Glows green under blue light"),
}
FOOD_EFFECTS = {"greenland_shark_meat": [("minecraft:poison", 200, 1), ("minecraft:nausea", 300, 0)]}

# Blocks: id -> (English name, stage, composition or None, description)
BLOCKS = {
    "cochineal_colony": ("Cochineal Colony", 1, None, None),
    "lac_colony": ("Lac Colony", 1, None, None),
    "sericulture_tray": ("Sericulture Tray", 1, None, "Put silkworms in it and feed it white mulberry leaves: it gives cocoons"),
    "worm_bin": ("Worm Bin", 1, None, "Put an earthworm in it and feed it leaves, plants or manure: it gives humus"),
    "guano": ("Guano", 2, "uric_acid:20;tricalcium_phosphate:25;potassium_nitrate:8;calcium_carbonate:10;water:22;protein:15", None),
    "beaver_dam": ("Beaver Dam", 2, "cellulose:50;lignin:25;water:25", None),
    "reindeer_lichen": ("Reindeer Lichen", 3, "cellulose:30;water:60;protein:4;glucose:6", None),
    "pearl_oyster": ("Pearl Oyster", 4, "calcium_carbonate:70;protein:20;water:10", None),
}
BUCKETS = {"atlantic_herring": "herring_bucket", "cuttlefish": "cuttlefish_bucket", "crystal_jelly": "crystal_jelly_bucket"}

# Research (rows of data/research.csv are written by hand); what each unlocks is checked by generate.check_unlocks.
ITEM_UNLOCKS = {"sericulture_tray", "antivenom"}
GATED = {"sericulture_tray": "sericulture", "antivenom": "antivenom"}

LANG = {
    "itemGroup.periodic_elements.fauna": "Animals of the World",
    "item.periodic_elements.animal_compendium": "Animal Compendium",
    "item.periodic_elements.animal_compendium.desc": "In your hotbar it shows the biome and names the animals you know; use it to read about them",
    "gui.periodic_elements.compendium.animal_title": "Animal Compendium",
    "gui.periodic_elements.compendium.animal_summary": "Animals noted: %s of %s. Biomes visited: %s of %s.",
    "gui.periodic_elements.compendium.animal_help": "Look at an animal for a second (within 16 blocks), catch it in a trap, or pick up a small one to note it. Choose a biome on the left.",
    "gui.periodic_elements.compendium.animals_here": "Animals here:",
    "gui.periodic_elements.compendium.lives": "Lives in: %s",
    "gui.periodic_elements.compendium.length": "Length: %s m",
    "gui.periodic_elements.compendium.animal_family": "Family, order and class: %s",
    "gui.periodic_elements.compendium.evolution": "Evolution: %s",
    "gui.periodic_elements.compendium.animal_hint": "Systema Naturae (level 3) adds the scientific name; On the Origin of Species (level 4) adds its story; The Red List (level 6) its status.",
    "gui.periodic_elements.compendium.new_animal": "Animal Compendium: noted %s",
    "gui.periodic_elements.compendium.unknown_animal": "Unknown animal",
    "gui.periodic_elements.compendium.animal_this_world": "An animal of this world, as Minecraft draws it.",
    "message.periodic_elements.fauna.out_of_climate": "%s breed slowly away from their climate: a heated or cooled greenhouse helps",
    "message.periodic_elements.fauna.poisoned": "The frog's skin is poison: do not touch it bare-handed; rub arrows on it instead",
    "message.periodic_elements.fauna.leech": "A leech has latched on: salt (sodium chloride) takes it off",
    "message.periodic_elements.fauna.no_more": "Wait: it has none to give yet",
    "effect.periodic_elements.leech": "Leech",
    "item.periodic_elements.fauna.spawns": "Lives in: %s",
}


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def _hex(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


# ------------------------------------------------------------------ item icons

def _icon(kind, seed):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = random.Random(seed)

    def blob(box, fill, outline=None):
        d.ellipse(box, fill=fill + (255,), outline=(outline + (255,)) if outline else None)

    def speck(box, col, n):
        for _ in range(n):
            x, y = rng.randint(box[0], box[2]), rng.randint(box[1], box[3])
            if img.getpixel((x, y))[3]:
                img.putpixel((x, y), col + (255,))

    if kind in ("meat", "cooked_meat"):
        c = (170, 50, 50) if kind == "meat" else (130, 80, 50)
        blob((2, 4, 13, 12), c, (90, 30, 30) if kind == "meat" else (80, 50, 30))
        d.rectangle((11, 9, 14, 11), fill=(235, 225, 210, 255))
        speck((3, 5, 12, 11), (230, 200, 190) if kind == "meat" else (170, 110, 70), 8)
    elif kind in ("big_egg", "egg_olive", "egg_blue", "boiled_egg"):
        c = {"big_egg": (238, 232, 214), "egg_olive": (150, 140, 100), "egg_blue": (190, 214, 200), "boiled_egg": (250, 250, 245)}[kind]
        box = (2, 1, 13, 14) if kind == "big_egg" else (4, 3, 11, 13)
        blob(box, c, tuple(max(0, v - 50) for v in c))
        if kind == "boiled_egg":
            blob((6, 6, 9, 9), (240, 190, 40))
        speck(box, tuple(max(0, v - 30) for v in c), 5)
    elif kind.startswith("vial"):
        c = {"vial_clear": (220, 230, 210), "vial_amber": (200, 140, 40), "vial_blue": (60, 110, 200)}[kind]
        d.rectangle((6, 1, 9, 4), fill=(200, 200, 210, 255))
        d.rectangle((6, 0, 9, 1), fill=(120, 90, 60, 255))
        blob((3, 4, 12, 15), (200, 210, 220))
        blob((4, 7, 11, 14), c)
    elif kind == "bug":
        d.rectangle((3, 7, 12, 9), fill=(200, 168, 48, 255))
        d.rectangle((10, 6, 13, 9), fill=(170, 140, 40, 255))
        d.line((4, 6, 11, 6), fill=(180, 170, 130, 255))
        d.line((5, 10, 2, 13), fill=(120, 100, 30, 255))
        d.line((8, 10, 6, 13), fill=(120, 100, 30, 255))
        img.putpixel((12, 7), (30, 20, 10, 255))
    elif kind == "cochineal":
        for x, y in [(4, 5), (9, 4), (6, 9), (11, 9), (8, 12), (3, 11)]:
            blob((x, y, x + 3, y + 2), (235, 235, 230))
            img.putpixel((x + 1, y + 1), (160, 20, 40, 255))
    elif kind == "sticklac":
        d.line((2, 13, 13, 2), fill=(110, 80, 50, 255), width=3)
        for x, y in [(4, 10), (6, 8), (8, 6), (10, 4)]:
            blob((x - 1, y - 1, x + 2, y + 2), (180, 70, 30), (120, 40, 20))
    elif kind == "silkworm":
        for i in range(5):
            blob((2 + 2 * i, 6 + (i % 2), 5 + 2 * i, 10 + (i % 2)), (236, 232, 222), (190, 186, 176))
        img.putpixel((13, 7), (40, 40, 40, 255))
    elif kind == "cocoon":
        blob((3, 4, 12, 12), (245, 240, 220), (200, 190, 160))
        for y in range(5, 12, 2):
            d.line((4, y, 11, y), fill=(225, 218, 195, 255))
    elif kind == "silk":
        for i in range(4):
            d.arc((2 + i, 2 + i, 13 - i, 13 - i), 0, 360, fill=(250, 246, 232, 255))
        d.line((7, 8, 14, 15), fill=(250, 246, 232, 255))
    elif kind == "worm":
        pts = [(2, 10), (4, 8), (6, 9), (8, 7), (10, 8), (12, 6), (13, 7)]
        d.line(pts, fill=(196, 110, 110, 255), width=2)
    elif kind in ("humus", "dung"):
        c = (70, 50, 34) if kind == "humus" else (100, 80, 40)
        blob((2, 6, 13, 14), c, tuple(max(0, v - 25) for v in c))
        speck((3, 7, 12, 13), (50, 36, 24) if kind == "humus" else (140, 120, 70), 12)
    elif kind == "leech":
        d.line([(2, 11), (5, 8), (9, 9), (12, 6)], fill=(50, 40, 30, 255), width=3)
        d.line([(3, 11), (6, 8)], fill=(150, 110, 40, 255))
    elif kind == "down":
        blob((3, 4, 12, 12), (246, 244, 236))
        speck((3, 4, 12, 12), (220, 214, 200), 10)
    elif kind == "antler":
        d.line([(3, 14), (6, 9), (8, 4), (9, 1)], fill=(220, 205, 175, 255), width=2)
        d.line([(6, 9), (11, 7)], fill=(220, 205, 175, 255), width=1)
        d.line([(7, 6), (12, 3)], fill=(220, 205, 175, 255), width=1)
    elif kind == "bristle":
        for x in range(4, 12):
            d.line([(x, 3 + (x % 3)), (x - 1, 13)], fill=(60 + 4 * x, 48, 40, 255))
    elif kind == "roe":
        for _ in range(16):
            x, y = rng.randint(3, 11), rng.randint(4, 11)
            blob((x, y, x + 2, y + 2), (40, 40, 36), (20, 20, 20))
    elif kind == "isinglass":
        d.polygon([(3, 3), (12, 2), (13, 12), (4, 13)], fill=(240, 228, 190, 200), outline=(200, 180, 130, 255))
    elif kind == "castoreum":
        blob((3, 4, 8, 12), (120, 80, 40), (70, 44, 20))
        blob((8, 4, 13, 12), (130, 88, 44), (70, 44, 20))
    elif kind == "spermaceti":
        d.rectangle((4, 3, 11, 13), fill=(246, 242, 230, 255), outline=(190, 184, 166, 255))
        d.line([(5, 5), (10, 5)], fill=(220, 214, 196, 255))
    elif kind == "ambergris":
        blob((2, 4, 13, 13), (170, 150, 110), (110, 96, 70))
        speck((4, 6, 11, 11), (120, 104, 80), 8)
    elif kind == "galalith":
        for x, y, c in [(3, 3, (200, 160, 90)), (9, 4, (60, 120, 170)), (5, 9, (180, 60, 60))]:
            blob((x, y, x + 5, y + 5), c, tuple(max(0, v - 50) for v in c))
            img.putpixel((x + 2, y + 2), (30, 30, 30, 0))
    elif kind in ("wool_brown", "wool_cream"):
        c = (60, 48, 38) if kind == "wool_brown" else (232, 220, 196)
        for x, y in [(3, 6), (7, 4), (9, 8), (4, 9)]:
            blob((x, y, x + 5, y + 5), c, tuple(max(0, v - 30) for v in c))
    elif kind == "blubber":
        d.rectangle((2, 4, 13, 12), fill=(240, 230, 200, 255), outline=(120, 110, 100, 255))
        d.rectangle((2, 4, 13, 5), fill=(70, 70, 76, 255))
    elif kind == "tusk":
        d.line([(3, 2), (5, 7), (8, 11), (12, 14)], fill=(240, 232, 214, 255), width=3)
    elif kind == "liver":
        blob((2, 4, 13, 12), (130, 60, 50), (80, 40, 30))
    elif kind in ("shark_meat", "hakarl"):
        c = (220, 210, 196) if kind == "shark_meat" else (210, 180, 120)
        d.rectangle((3, 4, 12, 12), fill=c + (255,), outline=tuple(max(0, v - 60) for v in c) + (255,))
    elif kind in ("herring", "cooked_herring"):
        c = (90, 120, 140) if kind == "herring" else (170, 120, 70)
        blob((2, 6, 12, 10), c)
        d.polygon([(11, 8), (14, 5), (14, 11)], fill=c + (255,))
        d.line([(3, 9), (11, 9)], fill=(220, 226, 230, 255) if kind == "herring" else (210, 160, 100, 255))
        img.putpixel((4, 7), (20, 20, 20, 255))
    elif kind == "murex":
        blob((4, 3, 11, 11), (216, 200, 168), (150, 130, 100))
        d.line([(7, 11), (6, 15)], fill=(200, 180, 150, 255), width=1)
        for x, y in [(3, 6), (11, 6), (4, 9), (11, 9)]:
            img.putpixel((x, y), (180, 150, 120, 255))
    elif kind == "oyster":
        blob((2, 3, 13, 13), (110, 100, 90), (60, 54, 48))
        blob((4, 5, 11, 11), (200, 205, 215))
    elif kind == "pearl":
        blob((4, 4, 11, 11), (240, 238, 245), (190, 185, 200))
        img.putpixel((6, 6), (255, 255, 255, 255))
    elif kind == "ink_brown":
        blob((3, 4, 12, 13), (90, 60, 40))
        blob((5, 6, 8, 9), (130, 90, 60))
    elif kind == "cuttlebone":
        blob((3, 2, 12, 14), (245, 240, 230), (200, 195, 185))
        for y in range(4, 13, 2):
            d.line([(5, y), (10, y)], fill=(225, 218, 205, 255))
    elif kind == "crab_shell":
        blob((2, 4, 13, 12), (80, 110, 50), (50, 70, 30))
        speck((3, 5, 12, 11), (110, 140, 70), 8)
    elif kind == "jelly":
        blob((2, 3, 13, 12), (190, 230, 240), (140, 200, 220))
        d.arc((4, 6, 11, 11), 0, 360, fill=(110, 240, 160, 255))
    else:
        raise KeyError(kind)
    return img


def spawn_egg_colors(s):
    pal = s["pal"]
    a = pal.get("body") or pal.get("shell")
    b = pal.get("belly") or pal.get("stripe") or pal.get("spot") or pal.get("blotch") or pal.get("claw") or pal.get("beak") or a
    if a == b:
        b = "%02X%02X%02X" % tuple(min(255, v + 60) for v in _hex(a))
    return int(a, 16), int(b, 16)


# ------------------------------------------------------------------ block textures

def _block_textures():
    out = {}
    rng = random.Random(7)
    # colonies: a thin crust drawn on the face it hangs on (transparent elsewhere), three ages
    for name, fg, dot in (("cochineal", (238, 238, 232), (170, 24, 44)), ("lac", (168, 72, 32), (110, 44, 20))):
        for age in range(3):
            img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
            n = 6 + 12 * age
            r2 = random.Random(name + str(age))
            for _ in range(n):
                x, y = r2.randint(2, 13), r2.randint(1, 14)
                img.putpixel((x, y), fg + (255,))
                if x + 1 < 16:
                    img.putpixel((x + 1, y), fg + (255,))
                if r2.random() < 0.4:
                    img.putpixel((x, y), dot + (255,))
            out[f"{name}_colony_{age}"] = img
    guano = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            v = rng.randint(-14, 14)
            c = (232 + v, 228 + v, 210 + v) if rng.random() < 0.8 else (180 + v, 170 + v, 140 + v)
            guano.putpixel((x, y), tuple(max(0, min(255, k)) for k in c) + (255,))
    out["guano"] = guano
    dam = Image.new("RGBA", (16, 16), (60, 46, 30, 255))
    d = ImageDraw.Draw(dam)
    for i in range(9):
        y = rng.randint(0, 15)
        d.line([(0, y), (15, (y + rng.randint(-5, 5)) % 16)], fill=(120 + rng.randint(-20, 20), 88, 52, 255), width=1)
    for _ in range(18):
        dam.putpixel((rng.randint(0, 15), rng.randint(0, 15)), (90, 70, 50, 255))
    out["beaver_dam"] = dam
    lichen = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(lichen)
    for _ in range(14):
        x = rng.randint(1, 14)
        top = rng.randint(4, 10)
        d.line([(x, 15), (x, top)], fill=(214, 216, 196, 255))
        d.line([(x, top), (x + rng.choice((-1, 1)), top - 2)], fill=(232, 232, 214, 255))
    out["reindeer_lichen"] = lichen
    shell = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            v = rng.randint(-10, 10) + (12 if (x + y // 2) % 5 == 0 else 0)
            shell.putpixel((x, y), (96 + v, 86 + v, 76 + v, 255))
    out["pearl_oyster"] = shell
    inner = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            v = rng.randint(-8, 8)
            inner.putpixel((x, y), (205 + v, 212 + v, 225 + v, 255))
    out["pearl_oyster_inner"] = inner
    for name, base in (("sericulture_tray", (176, 140, 96)), ("worm_bin", (110, 82, 54))):
        side = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                v = rng.randint(-8, 8) - (20 if y in (0, 15) or x in (0, 15) else 0)
                side.putpixel((x, y), tuple(max(0, c + v) for c in base) + (255,))
        out[name + "_side"] = side
    for st, fill in enumerate(((0, 0, 0, 0), (120, 160, 70), (236, 232, 222), (250, 246, 228))):
        top = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                edge = x in (0, 15) or y in (0, 15)
                if edge:
                    top.putpixel((x, y), (150, 116, 76, 255))
                elif st == 0:
                    top.putpixel((x, y), (196, 170, 120, 255) if (x + y) % 3 else (176, 150, 104, 255))
                elif st == 1:
                    top.putpixel((x, y), (90 + rng.randint(-20, 20), 150 + rng.randint(-20, 20), 60, 255)
                                 if rng.random() < 0.8 else (236, 232, 222, 255))
                else:
                    top.putpixel((x, y), (196, 170, 120, 255))
        if st >= 2:
            d = ImageDraw.Draw(top)
            for cx, cy in [(4, 4), (10, 4), (7, 9), (3, 11), (11, 11)]:
                d.ellipse((cx - 2, cy - 1, cx + 2, cy + 2), fill=fill + (255,))
        out[f"sericulture_tray_top_{st}"] = top
    for st in range(3):
        top = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                if x in (0, 15) or y in (0, 15):
                    top.putpixel((x, y), (80, 58, 36, 255))
                else:
                    v = rng.randint(-12, 12)
                    c = (60 + v, 44 + v, 30 + v) if st < 2 else (40 + v, 30 + v, 22 + v)
                    top.putpixel((x, y), c + (255,))
        if st >= 1:
            d = ImageDraw.Draw(top)
            for _ in range(3):
                x, y = rng.randint(2, 10), rng.randint(2, 12)
                d.line([(x, y), (x + 2, y + 1), (x + 4, y)], fill=(196, 110, 110, 255))
        out[f"worm_bin_top_{st}"] = top
    return out


def _cube_model(mod, side, top, bottom=None, height=16):
    tex = {"particle": side, "side": side, "top": top, "bottom": bottom or side}
    faces = {"north": {"texture": "#side", "uv": [0, 16 - height, 16, 16]}, "south": {"texture": "#side", "uv": [0, 16 - height, 16, 16]},
             "east": {"texture": "#side", "uv": [0, 16 - height, 16, 16]}, "west": {"texture": "#side", "uv": [0, 16 - height, 16, 16]},
             "up": {"texture": "#top"}, "down": {"texture": "#bottom"}}
    return {"parent": "minecraft:block/block", "textures": tex, "elements": [{"from": [0, 0, 0], "to": [16, height, 16], "faces": faces}]}


def _colony_model(mod, name, age):
    t = f"{mod}:block/{name}_colony_{age}"
    return {"parent": "minecraft:block/block", "ambientocclusion": False, "textures": {"particle": t, "c": t},
            "elements": [{"from": [0, 0, 15.2], "to": [16, 16, 15.2], "faces": {"north": {"texture": "#c"}, "south": {"texture": "#c"}}}]}


def generate_fauna(assets, data_root, runtime_dir, lang, mod, stage=None):
    sps = species(stage)
    smalls = small(stage)
    stage_now = STAGE if stage is None else stage
    tex_i, tex_b, tex_e = assets / "textures" / "item", assets / "textures" / "block", assets / "textures" / "entity" / "fauna"
    for t in (tex_i, tex_b, tex_e):
        t.mkdir(parents=True, exist_ok=True)
    models = assets / "models"
    data = data_root / mod
    runtime, model_json = [], {}
    for s in sps:
        mj, parts, size = FM.build(s)
        model_json[s["id"]] = mj
        FM.paint(s, parts, size).save(tex_e / f"{s['id']}.png")
        if "winter_coat" in s["traits"]:
            FM.paint(s, parts, size, winter=True).save(tex_e / f"{s['id']}_winter.png")
        a, b = spawn_egg_colors(s)
        group = {"water": "water_creature", "school": "water_ambient"}.get(s["kind"], "creature")
        if s["body"] == "snail" or s["id"] == "horseshoe_crab" and False:
            group = "water_creature"
        if s["id"] == "crystal_jelly":
            group = "water_ambient"
        runtime.append({
            "id": s["id"], "stage": s["stage"], "kind": s["kind"], "body": s["body"], "behaviour": s["behaviour"],
            "biomes": s["biomes"], "group": list(s["group"]), "weight": s["weight"], "size": list(s["size"]),
            "hp": s["hp"], "speed": s["speed"], "attack": s["attack"], "food": s["food"], "traits": s["traits"],
            "spawn_group": group, "egg": [a, b], "bucket": BUCKETS.get(s["id"]),
        })
        lang[f"entity.{mod}.{s['id']}"] = s["name"]
        lang[f"item.{mod}.{s['id']}_spawn_egg"] = f"{s['name']} Spawn Egg"
        _dump(models / "item" / f"{s['id']}_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})
        # loot
        entries = []
        for item, lo, hi in s["drops"]:
            entries.append({"rolls": 1, "entries": [{"type": "minecraft:item", "name": item, "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}},
                {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting",
                 "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]
                + ([{"function": "minecraft:furnace_smelt", "conditions": [{"condition": "minecraft:entity_properties", "entity": "this",
                                                                             "predicate": {"flags": {"is_on_fire": True}}}]}]
                   if item.endswith("game_meat") or item.endswith("herring") else [])}]})
        _dump(data / "loot_table" / "entities" / f"{s['id']}.json", {"type": "minecraft:entity", "pools": entries})
    for sid, bucket in BUCKETS.items():
        if any(s["id"] == sid for s in sps):
            name = next(s["name"] for s in sps if s["id"] == sid)
            lang[f"item.{mod}.{bucket}"] = f"Bucket of {name}"
            img = Image.open(assets.parent.parent.parent.parent / "tools" / "_bucket.png") if False else None
            bimg = _bucket_icon(sid)
            bimg.save(tex_i / f"{bucket}.png")
            _dump(models / "item" / f"{bucket}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{bucket}"}})

    # the leech status effect's icon
    eff = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    de = ImageDraw.Draw(eff)
    de.line([(3, 12), (6, 7), (10, 6), (14, 9), (15, 13)], fill=(58, 42, 30, 255), width=3)
    de.line([(4, 12), (7, 8), (10, 7), (13, 9)], fill=(120, 96, 50, 255), width=1)
    de.point([(15, 13), (3, 12)], fill=(150, 30, 30, 255))
    (assets / "textures" / "mob_effect").mkdir(parents=True, exist_ok=True)
    eff.save(assets / "textures" / "mob_effect" / "leech.png")
    items_rt = []
    for iid, (name, comp, food, icon, st, tip) in ITEMS.items():
        if st > stage_now:
            continue
        _icon(icon, iid).save(tex_i / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{iid}"}})
        lang[f"item.{mod}.{iid}"] = name
        if tip:
            lang[f"item.{mod}.{iid}.desc"] = tip
        items_rt.append({"id": iid, "food": list(food) if food else None, "effects": FOOD_EFFECTS.get(iid, []),
                         "tip": bool(tip)})
    # blocks
    for name, img in _block_textures().items():
        img.save(tex_b / f"{name}.png")
    blocks_rt = []
    states = assets / "blockstates"
    for bid, (name, st, comp, desc) in BLOCKS.items():
        if st > stage_now:
            continue
        lang[f"block.{mod}.{bid}"] = name
        if desc:
            lang[f"block.{mod}.{bid}.desc"] = desc
        blocks_rt.append(bid)
    if stage_now >= 1:
        for name in ("cochineal", "lac"):
            variants = {}
            for age in range(3):
                _dump(models / "block" / f"{name}_colony_{age}.json", _colony_model(mod, name, age))
                for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
                    v = {"model": f"{mod}:block/{name}_colony_{age}"}
                    if y:
                        v["y"] = y
                    variants[f"age={age},facing={f}"] = v
            _dump(states / f"{name}_colony.json", {"variants": variants})
            _dump(data / "loot_table" / "blocks" / f"{name}_colony.json", {"type": "minecraft:block", "pools": [{
                "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{'cochineal' if name == 'cochineal' else 'sticklac'}"}],
                "conditions": [{"condition": "minecraft:block_state_property", "block": f"{mod}:{name}_colony", "properties": {"age": "2"}}]}]})
        for name, n in (("sericulture_tray", 4), ("worm_bin", 3)):
            variants = {}
            for st in range(n):
                h = 6 if name == "sericulture_tray" else 14
                _dump(models / "block" / f"{name}_{st}.json", _cube_model(mod, f"{mod}:block/{name}_side", f"{mod}:block/{name}_top_{st}",
                                                                          f"{mod}:block/{name}_side", h))
                variants[f"stage={st}"] = {"model": f"{mod}:block/{name}_{st}"}
            _dump(states / f"{name}.json", {"variants": variants})
            _dump(models / "item" / f"{name}.json", {"parent": f"{mod}:block/{name}_0"})
            _dump(data / "loot_table" / "blocks" / f"{name}.json", {"type": "minecraft:block", "pools": [{
                "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{name}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    if stage_now >= 2:
        variants = {}
        for layers in range(1, 9):
            _dump(models / "block" / f"guano_{layers}.json", {"parent": f"minecraft:block/snow_height{2 * layers}" if layers < 8 else "minecraft:block/cube_all",
                                                              "textures": ({"texture": f"{mod}:block/guano", "particle": f"{mod}:block/guano"} if layers < 8
                                                                           else {"all": f"{mod}:block/guano"})})
            variants[f"layers={layers}"] = {"model": f"{mod}:block/guano_{layers}"}
        _dump(states / "guano.json", {"variants": variants})
        _dump(models / "item" / "guano.json", {"parent": f"{mod}:block/guano_1"})
        _dump(data / "loot_table" / "blocks" / "guano.json", {"type": "minecraft:block", "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:guano", "functions": [
                {"function": "minecraft:set_count", "count": float(n), "conditions": [{"condition": "minecraft:block_state_property",
                                                                                       "block": f"{mod}:guano", "properties": {"layers": str(n)}}]}
                for n in range(1, 9)]}]}]})
        _dump(models / "block" / "beaver_dam.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod}:block/beaver_dam"}})
        _dump(states / "beaver_dam.json", {"variants": {"": {"model": f"{mod}:block/beaver_dam"}}})
        _dump(models / "item" / "beaver_dam.json", {"parent": f"{mod}:block/beaver_dam"})
        _dump(data / "loot_table" / "blocks" / "beaver_dam.json", {"type": "minecraft:block", "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:stick", "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 5}}]}]}]})
    if stage_now >= 3:
        _dump(models / "block" / "reindeer_lichen.json", {"parent": "minecraft:block/cross", "textures": {"cross": f"{mod}:block/reindeer_lichen"}})
        _dump(states / "reindeer_lichen.json", {"variants": {"": {"model": f"{mod}:block/reindeer_lichen"}}})
        _dump(models / "item" / "reindeer_lichen.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:block/reindeer_lichen"}})
        _dump(data / "loot_table" / "blocks" / "reindeer_lichen.json", {"type": "minecraft:block", "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:reindeer_lichen"}]}]})
    if stage_now >= 4:
        _dump(models / "block" / "pearl_oyster.json", {"parent": "minecraft:block/block", "textures": {
            "particle": f"{mod}:block/pearl_oyster", "s": f"{mod}:block/pearl_oyster", "i": f"{mod}:block/pearl_oyster_inner"},
            "elements": [{"from": [3, 0, 3], "to": [13, 2, 13], "faces": {f: {"texture": "#s"} for f in ("north", "south", "east", "west", "up", "down")}},
                         {"from": [4, 2, 4], "to": [12, 3, 12], "faces": {f: {"texture": "#i" if f == "up" else "#s"} for f in ("north", "south", "east", "west", "up")}},
                         {"from": [3, 3, 3], "to": [13, 4, 13], "rotation": {"angle": -22.5, "axis": "x", "origin": [8, 3, 13]},
                          "faces": {f: {"texture": "#s"} for f in ("north", "south", "east", "west", "up", "down")}}]})
        _dump(states / "pearl_oyster.json", {"variants": {"": {"model": f"{mod}:block/pearl_oyster"}}})
        _dump(models / "item" / "pearl_oyster.json", {"parent": f"{mod}:block/pearl_oyster"})
        _dump(data / "loot_table" / "blocks" / "pearl_oyster.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:oyster_shell", "functions": [
                {"function": "minecraft:set_count", "count": 2}]}]},
            {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": 0.15}],
             "entries": [{"type": "minecraft:item", "name": f"{mod}:pearl"}]}]})

    # recipes
    rec = data / "recipe"
    cooking = [("game_meat", "cooked_game_meat", 1, 0.35), ("ostrich_egg", "cooked_egg", 4, 0.35), ("silk_cocoon", "silk", 3, 0.1)]
    if stage_now >= 2:
        cooking += [("pheasant_egg", "cooked_egg", 1, 0.35), ("duck_egg", "cooked_egg", 1, 0.35)]
    if stage_now >= 4:
        cooking += [("herring", "cooked_herring", 1, 0.35), ("greenland_shark_meat", "hakarl", 1, 0.35)]
    for src, dst, n, xp in cooking:
        for kind, t in (("smelting", 200), ("smoking", 100), ("campfire_cooking", 600)):
            if src == "silk_cocoon" and kind != "smelting":
                continue
            _dump(rec / f"{dst}_from_{src}_{kind}.json", {"type": f"minecraft:{kind}", "category": "food",
                                                         "ingredient": {"item": f"{mod}:{src}"},
                                                         "result": {"id": f"{mod}:{dst}", "count": n}, "experience": xp, "cookingtime": t})
    shaped = {
        "sericulture_tray": ([" L ", "SSS"], {"L": "minecraft:white_carpet", "S": "minecraft:oak_slab"}, 1, "sericulture_tray"),
        "worm_bin": (["P P", "PDP", "PPP"], {"P": "#minecraft:planks", "D": "minecraft:dirt"}, 1, "worm_bin"),
    }
    shapeless = {
        "string_from_silk": ([f"{mod}:silk"], "minecraft:string", 2),
        "red_dye_from_cochineal": ([f"{mod}:cochineal", f"{mod}:cochineal"], "minecraft:red_dye", 1),
        "antivenom_from_scorpion": ([f"{mod}:scorpion_venom", "minecraft:glass_bottle", f"{mod}:sodium_chloride"], f"{mod}:antivenom", 1),
        "antivenom_from_gila": ([f"{mod}:gila_venom", "minecraft:glass_bottle", f"{mod}:sodium_chloride"], f"{mod}:antivenom", 1),
    }
    if stage_now >= 2:
        shapeless["bone_meal_from_antler"] = ([f"{mod}:antler"], "minecraft:bone_meal", 4)
        shapeless["brush_from_bristles"] = ([f"{mod}:boar_bristle", f"{mod}:boar_bristle", "minecraft:stick", "minecraft:copper_ingot"], "minecraft:brush", 1)
    if stage_now >= 3:
        shaped["white_wool_from_alpaca"] = (["WW", "WW"], {"W": f"{mod}:alpaca_wool"}, 1, "minecraft:white_wool")
        shaped["brown_wool_from_yak"] = (["WW", "WW"], {"W": f"{mod}:yak_wool"}, 1, "minecraft:brown_wool")
    if stage_now >= 4:
        shapeless["candle_from_spermaceti"] = ([f"{mod}:spermaceti", "minecraft:string"], "minecraft:candle", 1)
        shapeless["brown_dye_from_sepia"] = ([f"{mod}:sepia_ink"], "minecraft:brown_dye", 1)
    for rid, (pattern, key, n, result) in shaped.items():
        _dump(rec / f"{rid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                    "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in key.items()},
                                    "result": {"id": result if ":" in result else f"{mod}:{result}", "count": n}})
    for rid, (ings, result, n) in shapeless.items():
        _dump(rec / f"{rid}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
                                    "ingredients": [{"item": i} for i in ings], "result": {"id": result, "count": n}})
    # animals the Rope Net holds
    import exploration as EX
    small_ids = [s["id"] for s in sps if s["size"][0] <= 0.6 and s["kind"] in ("land", "amphibious") and s["id"] != "sperm_whale"]
    _dump(data / "tags" / "entity_type" / "trap_small.json",
          {"values": [f"minecraft:{a}" for a in EX.SMALL_ANIMALS] + [f"{mod}:{i}" for i in small_ids]})
    # herbivores that leave manure in a pen (the mod's own say so with the "manure" trait)
    _dump(data / "tags" / "entity_type" / "manure_animals.json", {"values": [f"minecraft:{a}" for a in (
        "cow", "sheep", "pig", "horse", "donkey", "mule", "goat", "llama", "camel", "mooshroom")]})
    _dump(runtime_dir / "fauna.json", {"species": runtime, "items": items_rt, "blocks": blocks_rt, "stage": stage_now,
                                       "gated": GATED})
    _dump(runtime_dir / "fauna_models.json", model_json)
    lang.update(LANG)
    for k, v in FT.LANG.items():
        if not k.startswith("_"):
            lang[k] = v
    write_csv(sps)
    return runtime


def _bucket_icon(sid):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(3, 5), (12, 5), (11, 14), (4, 14)], fill=(160, 166, 176, 255), outline=(90, 96, 104, 255))
    d.rectangle((4, 6, 11, 9), fill=(60, 110, 200, 255))
    c = {"atlantic_herring": (200, 210, 220), "cuttlefish": (150, 110, 90), "crystal_jelly": (120, 240, 170)}[sid]
    d.ellipse((5, 6, 10, 9), fill=c + (255,))
    d.arc((2, 0, 13, 10), 180, 360, fill=(90, 96, 104, 255))
    return img


def write_csv(sps):
    from pathlib import Path
    path = Path(__file__).resolve().parent.parent / "data" / "fauna.csv"
    cols = ["id", "stage", "name", "scientific", "family", "body", "kind", "behaviour", "biomes", "group", "weight",
            "hitbox", "length_m", "hp", "speed", "attack", "food", "drops", "traits", "iucn"]
    with path.open("w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(cols)
        for s in FS.S:
            w.writerow([s["id"], s["stage"], s["name"], s["sci"], s["family"], s["body"], s["kind"], s["behaviour"],
                        ";".join(s["biomes"]), f"{s['group'][0]}-{s['group'][1]}", s["weight"], f"{s['size'][0]}x{s['size'][1]}",
                        s["length"], s["hp"], s["speed"], s["attack"], ";".join(s["food"]),
                        ";".join(f"{i}:{a}-{b}" for i, a, b in s["drops"]), ";".join(s["traits"]), s["iucn"] or ""])


def composition_rows(stage=None):
    stage_now = STAGE if stage is None else stage
    rows = []
    for iid, (name, comp, food, icon, st, tip) in ITEMS.items():
        if st <= stage_now:
            rows.append((P + iid, 4000.0 / 9.0 if iid == "antler" else 1000.0 / 9.0, comp))  # an antler grinds to 4 bone meal
    for bid, (name, st, comp, desc) in BLOCKS.items():
        if st <= stage_now and comp:
            rows.append((P + bid, 1000.0 / 8.0 if bid == "guano" else 1000.0 / 9.0 if bid == "reindeer_lichen" else 250.0, comp))
    return rows


def compendium_entries(mod, stage=None):
    """The Animal Compendium's entries (runtime compendium.json "animals")."""
    stage_now = STAGE if stage is None else stage
    out = []
    for s in species(stage):
        products = sorted({i for i, _, _ in s["drops"]} | {t.split(":", 1)[1] for t in s["traits"]
                                                            if t.startswith(("eggs:", "shear:", "venom:"))})
        if "down" in s["traits"]:
            products.append(P + "duck_down")
        if "antlers" in s["traits"]:
            products.append(P + "antler")
        if "milk" in s["traits"]:
            products.append("minecraft:milk_bucket")
        if "blood" in s["traits"]:
            products.append(P + "horseshoe_crab_blood")
        if "guano" in s["traits"]:
            products.append(P + "guano")
        if "manure" in s["traits"]:
            products.append(P + "manure")
        if s["id"] == "sperm_whale":
            products.append(P + "ambergris")
        out.append({"id": s["id"], "vanilla": False, "icon": f"{mod}:{s['id']}_spawn_egg", "scientific": s["sci"],
                    "family": s["family"], "height": s["length"], "iucn": s["iucn"], "biomes": s["biomes"],
                    "blocks": [f"{mod}:{s['id']}"], "products": products, "entity": True})
    for sid, v in small(stage).items():
        out.append({"id": sid, "vanilla": False, "icon": v["items"][0], "scientific": v["sci"], "family": v["family"],
                    "height": v["length"], "iucn": v["iucn"], "biomes": v["biomes"], "blocks": v["blocks"],
                    "products": v["items"], "entity": False, "items": v["items"]})
    for vid, (sci, fam, length, iucn, products) in FS.VANILLA.items():
        out.append({"id": vid, "vanilla": True, "icon": "minecraft:sea_pickle" if vid == "sea_pickle" else f"minecraft:{vid}_spawn_egg",
                    "scientific": sci, "family": fam, "height": length, "iucn": iucn,
                    "biomes": FS.VANILLA_BIOMES[vid].split(";"), "blocks": FS.VANILLA_BLOCKS.get(vid, [f"minecraft:{vid}"]),
                    "products": [p for p in products if p != P + "guano" or stage_now >= 2], "entity": vid != "sea_pickle"})
    return out


def compendium_lang(lang, mod, stage=None):
    for e in compendium_entries(mod, stage):
        sid = e["id"]
        lang[f"animal.{mod}.{sid}"] = FT.NAMES.get(sid) or next((s["name"] for s in FS.S if s["id"] == sid), None) or FS.SMALL[sid]["name"]
        about, evo = FT.TEXT[sid]
        lang[f"compendium.{mod}.animal.{sid}.about"] = about
        lang[f"compendium.{mod}.animal.{sid}.evolution"] = evo
    for b, (t, r, about) in FT.BIOMES.items():
        lang[f"compendium.{mod}.biome.{b}.about"] = about


def translations(code):
    import fauna_i18n as I
    return I.text(code)
