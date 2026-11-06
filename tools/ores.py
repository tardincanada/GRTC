"""This mod's ores: blocks, drops, textures and world generation. Called by generate.py.

Writes:
  periodic_elements/ores.json                    runtime table (blocks to register, features to add to biomes)
  assets: blockstates, block and item models, 16x16 textures (ores and raw ore items)
  data: loot tables, worldgen configured and placed features, needs-tool tags
Mining tags (mineable/pickaxe) are merged into the ones machines.py writes, through PICKAXE.
"""
import json
import random

from PIL import Image

# id: name, host rock, mineral colours (main, light, dark), drop, (min, max) drop count, (min, max) xp,
#     tool tier needed, description
ORES = {
    "cassiterite_ore": ("Cassiterite Ore", "stone", ((70, 52, 40), (150, 120, 90), (40, 30, 24)),
                        "periodic_elements:raw_cassiterite", (1, 3), (0, 0), "stone", "Tin oxide in granite veins"),
    "deepslate_cassiterite_ore": ("Deepslate Cassiterite Ore", "deepslate", ((70, 52, 40), (150, 120, 90), (40, 30, 24)),
                                  "periodic_elements:raw_cassiterite", (1, 3), (0, 0), "stone", "Tin oxide in slate"),
    "galena_ore": ("Galena Ore", "stone", ((120, 126, 138), (200, 206, 218), (70, 74, 84)),
                   "periodic_elements:raw_galena", (1, 3), (0, 0), "stone", "Lead sulfide cubes"),
    "deepslate_galena_ore": ("Deepslate Galena Ore", "deepslate", ((120, 126, 138), (200, 206, 218), (70, 74, 84)),
                             "periodic_elements:raw_galena", (1, 3), (0, 0), "stone", "Lead sulfide cubes in slate"),
    "sphalerite_ore": ("Sphalerite Ore", "stone", ((150, 92, 34), (214, 150, 66), (92, 52, 20)),
                       "periodic_elements:raw_sphalerite", (1, 2), (0, 1), "stone", "Resinous zinc sulfide"),
    "deepslate_sphalerite_ore": ("Deepslate Sphalerite Ore", "deepslate", ((150, 92, 34), (214, 150, 66), (92, 52, 20)),
                                 "periodic_elements:raw_sphalerite", (1, 2), (0, 1), "stone", "Resinous zinc sulfide in slate"),
    "nether_sulfur_ore": ("Nether Sulfur Ore", "netherrack", ((226, 208, 52), (250, 240, 130), (170, 150, 30)),
                          "periodic_elements:sulfur", (2, 4), (0, 2), "wood", "Native sulfur from volcanic vents"),
    "deepslate_sulfur_ore": ("Deepslate Sulfur Ore", "deepslate", ((226, 208, 52), (250, 240, 130), (170, 150, 30)),
                             "periodic_elements:sulfur", (2, 4), (0, 2), "wood", "Native sulfur near deep lava"),
    "saltpetre_crust": ("Saltpetre Crust", "sandstone", ((244, 244, 238), (255, 255, 255), (200, 200, 192)),
                        "periodic_elements:potassium_nitrate", (1, 3), (0, 1), "wood", "Nitre crystals grown in dry rock"),
    "cobaltite_ore": ("Cobaltite Ore", "stone", ((196, 184, 206), (236, 228, 244), (130, 118, 140)),
                      "periodic_elements:raw_cobaltite", (1, 3), (0, 0), "stone", "Cobalt arsenide with native bismuth"),
    "deepslate_cobaltite_ore": ("Deepslate Cobaltite Ore", "deepslate", ((196, 184, 206), (236, 228, 244), (130, 118, 140)),
                                "periodic_elements:raw_cobaltite", (1, 3), (0, 0), "stone", "Cobalt arsenide in slate"),
    "stibnite_ore": ("Stibnite Ore", "stone", ((96, 100, 112), (170, 176, 190), (52, 54, 62)),
                     "periodic_elements:raw_stibnite", (1, 3), (0, 0), "stone", "Steel-grey needles of antimony sulfide"),
    "deepslate_stibnite_ore": ("Deepslate Stibnite Ore", "deepslate", ((96, 100, 112), (170, 176, 190), (52, 54, 62)),
                               "periodic_elements:raw_stibnite", (1, 3), (0, 0), "stone", "Antimony sulfide in slate"),
    "arsenopyrite_ore": ("Arsenopyrite Ore", "stone", ((184, 180, 160), (228, 226, 208), (120, 116, 100)),
                         "periodic_elements:raw_arsenopyrite", (1, 3), (0, 0), "stone", "Iron arsenic sulfide; smells of garlic when struck"),
    "deepslate_arsenopyrite_ore": ("Deepslate Arsenopyrite Ore", "deepslate", ((184, 180, 160), (228, 226, 208), (120, 116, 100)),
                                   "periodic_elements:raw_arsenopyrite", (1, 3), (0, 0), "stone", "Iron arsenic sulfide in slate"),
    "barite_ore": ("Barite Ore", "stone", ((226, 218, 206), (250, 246, 238), (170, 190, 210)),
                   "periodic_elements:raw_barite", (1, 3), (0, 0), "stone", "Heavy spar with sky-blue celestine"),
    "spodumene_ore": ("Spodumene Ore", "stone", ((214, 186, 200), (240, 220, 232), (160, 132, 150)),
                      "periodic_elements:raw_spodumene", (1, 3), (0, 0), "stone", "Lilac spodumene laths in pegmatite"),
    "borax_crust": ("Borax Crust", "sandstone", ((246, 244, 232), (255, 255, 250), (214, 210, 196)),
                    "periodic_elements:raw_borax", (1, 3), (0, 1), "wood", "Borax and sylvite left by a dried-up lake"),
    "pitchblende_ore": ("Pitchblende Ore", "stone", ((40, 40, 36), (90, 92, 70), (20, 20, 18)),
                        "periodic_elements:raw_pitchblende", (1, 2), (0, 0), "iron", "Pitch-black uranium ore"),
    "deepslate_pitchblende_ore": ("Deepslate Pitchblende Ore", "deepslate", ((40, 40, 36), (90, 92, 70), (20, 20, 18)),
                                  "periodic_elements:raw_pitchblende", (1, 2), (0, 0), "iron", "Pitch-black uranium ore in slate"),
    "bauxite_ore": ("Bauxite Ore", "stone", ((178, 96, 60), (220, 150, 110), (120, 60, 36)),
                    "periodic_elements:raw_bauxite", (1, 3), (0, 0), "stone", "Red tropical weathering clay, rich in alumina"),
    "pyrolusite_ore": ("Pyrolusite Ore", "stone", ((40, 38, 44), (90, 86, 96), (22, 20, 24)),
                       "periodic_elements:raw_pyrolusite", (1, 3), (0, 0), "stone", "Sooty black manganese oxide"),
    "deepslate_pentlandite_ore": ("Deepslate Pentlandite Ore", "deepslate", ((200, 176, 96), (236, 214, 140), (130, 110, 50)),
                                  "periodic_elements:raw_pentlandite", (1, 2), (0, 1), "iron", "Bronze-yellow nickel sulfide"),
    "wolframite_ore": ("Wolframite Ore", "stone", ((50, 44, 40), (110, 100, 92), (26, 22, 20)),
                       "periodic_elements:raw_wolframite", (1, 2), (0, 1), "iron", "Heavy black tungsten ore in quartz"),
    "vanadinite_ore": ("Vanadinite Ore", "stone", ((208, 90, 40), (240, 140, 80), (140, 50, 20)),
                       "periodic_elements:raw_vanadinite", (1, 3), (0, 0), "stone", "Red hexagonal vanadinite crystals"),
    "fluorite_ore": ("Fluorite Ore", "stone", ((150, 100, 210), (200, 170, 240), (90, 60, 140)),
                     "periodic_elements:raw_fluorite", (1, 3), (0, 0), "stone", "Purple fluorspar cubes"),
    "deepslate_fluorite_ore": ("Deepslate Fluorite Ore", "deepslate", ((150, 100, 210), (200, 170, 240), (90, 60, 140)),
                               "periodic_elements:raw_fluorite", (1, 3), (0, 0), "stone", "Purple fluorspar cubes in slate"),
    # specks: light reddish brown (Guilherme 2026-11-05; were near-black)
    "heavy_mineral_sand": ("Heavy Mineral Sand", "sand", ((196, 130, 102), (224, 166, 138), (158, 98, 74)),
                           "periodic_elements:heavy_mineral_concentrate", (1, 3), (0, 0), "wood",
                           "Black beach sand of ilmenite, zircon and monazite"),
    "deepslate_chromite_ore": ("Deepslate Chromite Ore", "deepslate", ((36, 32, 40), (96, 90, 110), (18, 16, 20)),
                               "periodic_elements:raw_chromite", (1, 2), (0, 1), "iron", "Black chromite pods in serpentine"),
    "molybdenite_ore": ("Molybdenite Ore", "stone", ((120, 128, 144), (186, 194, 210), (70, 74, 86)),
                        "periodic_elements:raw_molybdenite", (1, 2), (0, 1), "iron", "Soft grey molybdenite flakes"),
    "deepslate_molybdenite_ore": ("Deepslate Molybdenite Ore", "deepslate", ((120, 128, 144), (186, 194, 210), (70, 74, 86)),
                                  "periodic_elements:raw_molybdenite", (1, 2), (0, 1), "iron", "Molybdenite flakes in slate"),
    "coltan_ore": ("Coltan Ore", "stone", ((54, 44, 40), (130, 110, 96), (28, 22, 20)),
                   "periodic_elements:raw_coltan", (1, 1), (0, 1), "iron", "Heavy black columbite-tantalite"),
    "deepslate_coltan_ore": ("Deepslate Coltan Ore", "deepslate", ((54, 44, 40), (130, 110, 96), (28, 22, 20)),
                             "periodic_elements:raw_coltan", (1, 1), (0, 1), "iron", "Columbite-tantalite in slate"),
    "oil_shale_ore": ("Oil Shale", "stone", ((52, 40, 30), (96, 78, 60), (28, 22, 16)),
                      "periodic_elements:raw_oil_shale", (1, 3), (0, 1), "wood", "Dark shale rich in kerogen: retort it for oil"),
}

STONE = {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}
DEEPSLATE = {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}


def _block(b):
    return {"predicate_type": "minecraft:block_match", "block": b}


def _uniform(lo, hi):
    return {"type": "minecraft:uniform", "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}}


def _trapezoid(lo, hi):
    return {"type": "minecraft:trapezoid", "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}}


# id: targets [(predicate, ore)], vein size, veins per chunk, height, biomes
# Biomes: "overworld", "nether", "#tag", or a biome id; join several with "|".
FEATURES = {
    "ore_cassiterite": ([(STONE, "cassiterite_ore"), (DEEPSLATE, "deepslate_cassiterite_ore")], 8, 8,
                        _trapezoid(-16, 80), "overworld"),
    "ore_cassiterite_mountains": ([(STONE, "cassiterite_ore")], 8, 10, _uniform(32, 160), "#minecraft:is_mountain"),
    "ore_galena": ([(STONE, "galena_ore"), (DEEPSLATE, "deepslate_galena_ore")], 8, 7, _uniform(-32, 48), "overworld"),
    "ore_sphalerite": ([(STONE, "sphalerite_ore"), (DEEPSLATE, "deepslate_sphalerite_ore")], 7, 6,
                       _uniform(-16, 64), "overworld"),
    "ore_sulfur_nether": ([(_block("minecraft:netherrack"), "nether_sulfur_ore")], 10, 12, _uniform(10, 117), "nether"),
    "ore_sulfur_deltas": ([(_block("minecraft:netherrack"), "nether_sulfur_ore"),
                           (_block("minecraft:basalt"), "nether_sulfur_ore"),
                           (_block("minecraft:blackstone"), "nether_sulfur_ore")], 12, 16, _uniform(10, 117),
                          "minecraft:basalt_deltas"),
    "ore_sulfur_deep": ([(DEEPSLATE, "deepslate_sulfur_ore")], 6, 5, _uniform(-64, -40), "overworld"),
    "ore_saltpetre_desert": ([(_block("minecraft:sandstone"), "saltpetre_crust"),
                              (_block("minecraft:red_sandstone"), "saltpetre_crust")], 10, 14, _uniform(30, 100),
                             "minecraft:desert|#minecraft:is_badlands"),
    "ore_saltpetre_caves": ([(STONE, "saltpetre_crust"), (_block("minecraft:dripstone_block"), "saltpetre_crust")],
                            8, 10, _uniform(0, 64), "minecraft:dripstone_caves"),
    "ore_cobaltite": ([(STONE, "cobaltite_ore"), (DEEPSLATE, "deepslate_cobaltite_ore")], 6, 3,
                      _trapezoid(-48, 48), "overworld"),
    "ore_cobaltite_mountains": ([(STONE, "cobaltite_ore")], 6, 6, _uniform(0, 96), "#minecraft:is_mountain"),
    "ore_stibnite": ([(STONE, "stibnite_ore"), (DEEPSLATE, "deepslate_stibnite_ore")], 5, 2,
                     _uniform(-16, 64), "overworld"),
    "ore_stibnite_mountains": ([(STONE, "stibnite_ore")], 6, 6, _uniform(32, 128), "#minecraft:is_mountain"),
    "ore_arsenopyrite": ([(STONE, "arsenopyrite_ore"), (DEEPSLATE, "deepslate_arsenopyrite_ore")], 6, 5,
                         _uniform(-48, 32), "overworld"),
    "ore_barite": ([(STONE, "barite_ore")], 8, 3, _uniform(0, 80), "overworld"),
    "ore_barite_dry": ([(STONE, "barite_ore")], 10, 8, _uniform(20, 100), "minecraft:desert|#minecraft:is_badlands"),
    "ore_spodumene_mountains": ([(STONE, "spodumene_ore")], 7, 6, _uniform(32, 160), "#minecraft:is_mountain"),
    "ore_spodumene": ([(STONE, "spodumene_ore")], 6, 3, _uniform(0, 64), "overworld"),
    "ore_borax_desert": ([(_block("minecraft:sandstone"), "borax_crust"),
                          (_block("minecraft:red_sandstone"), "borax_crust")], 10, 10, _uniform(40, 100),
                         "minecraft:desert|#minecraft:is_badlands"),
    "ore_pitchblende": ([(STONE, "pitchblende_ore"), (DEEPSLATE, "deepslate_pitchblende_ore")], 6, 4,
                        _uniform(-64, 0), "overworld"),
    # Uraninite in mountain granite pegmatites (balance 1.4.1: the nuclear levels need far more than deep veins give).
    "ore_pitchblende_granite": ([(STONE, "pitchblende_ore")], 6, 3, _uniform(0, 96), "#minecraft:is_mountain"),
    "ore_bauxite": ([(STONE, "bauxite_ore")], 12, 8, _uniform(40, 100), "#minecraft:is_jungle|#minecraft:is_savanna"),
    "ore_bauxite_rare": ([(STONE, "bauxite_ore")], 10, 3, _uniform(30, 90), "overworld"),
    "ore_pyrolusite": ([(STONE, "pyrolusite_ore")], 9, 6, _uniform(0, 64),
                       "#minecraft:is_ocean|minecraft:swamp|minecraft:mangrove_swamp"),
    "ore_pyrolusite_rare": ([(STONE, "pyrolusite_ore")], 6, 2, _uniform(0, 48), "overworld"),
    "ore_pentlandite": ([(DEEPSLATE, "deepslate_pentlandite_ore")], 7, 4, _uniform(-64, -8), "overworld"),
    "ore_wolframite_mountains": ([(STONE, "wolframite_ore")], 6, 6, _uniform(32, 160), "#minecraft:is_mountain"),
    "ore_wolframite": ([(STONE, "wolframite_ore")], 6, 4, _uniform(-16, 48), "overworld"),
    "ore_vanadinite": ([(STONE, "vanadinite_ore")], 8, 8, _uniform(32, 120), "#minecraft:is_badlands"),
    "ore_vanadinite_rare": ([(STONE, "vanadinite_ore")], 6, 3, _uniform(0, 64), "overworld"),
    "ore_fluorite": ([(STONE, "fluorite_ore"), (DEEPSLATE, "deepslate_fluorite_ore")], 8, 4, _uniform(-32, 48), "overworld"),
    "ore_heavy_mineral_sand": ([(_block("minecraft:sand"), "heavy_mineral_sand")], 12, 24, _uniform(54, 68),
                               "#minecraft:is_beach|#minecraft:is_river"),
    "ore_chromite": ([(DEEPSLATE, "deepslate_chromite_ore")], 9, 5, _uniform(-64, -16), "overworld"),
    "ore_molybdenite": ([(STONE, "molybdenite_ore"), (DEEPSLATE, "deepslate_molybdenite_ore")], 7, 4, _uniform(-48, 16),
                        "overworld"),
    # Shale beds: big flat-ish veins high in the stone, most of all under deserts, badlands and seas.
    "ore_oil_shale": ([(STONE, "oil_shale_ore")], 16, 4, _uniform(0, 72), "overworld"),
    "ore_oil_shale_basins": ([(STONE, "oil_shale_ore")], 20, 8, _uniform(0, 64),
                             "minecraft:desert|#minecraft:is_badlands|#minecraft:is_ocean"),
    "ore_coltan": ([(STONE, "coltan_ore"), (DEEPSLATE, "deepslate_coltan_ore")], 5, 3, _uniform(-64, 0), "overworld"),
    # Coltan in the same mountain pegmatites as spodumene.
    "ore_coltan_mountains": ([(STONE, "coltan_ore")], 6, 5, _uniform(32, 128), "#minecraft:is_mountain"),
}

PICKAXE = list(ORES)


# ---------------------------------------------------------------- textures

def _host(host, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            if host == "stone":
                v = rnd.randint(-14, 14)
                c = (126 + v, 126 + v, 126 + v)
            elif host == "deepslate":
                v = rnd.randint(-9, 9)
                band = -12 if (y + (x // 5)) % 4 == 0 else 0
                c = (78 + v + band, 78 + v + band, 84 + v + band)
            elif host == "netherrack":
                v = rnd.randint(-18, 18)
                c = (112 + v, 46 + v // 2, 44 + v // 2)
            elif host == "sand":
                v = rnd.randint(-8, 8)
                c = (219 + v, 207 + v, 163 + v)
            else:  # sandstone
                v = rnd.randint(-7, 7)
                band = -14 if y % 5 == 0 else 0
                c = (216 + v + band, 202 + v + band, 152 + v + band)
            px[x, y] = tuple(max(0, min(255, k)) for k in c) + (255,)
    return img


def ore_texture(oid, host, colors, seed):
    img = _host(host, seed)
    px = img.load()
    main, light, dark = colors
    rnd = random.Random(seed * 31 + 7)
    clumps = [(3, 3), (10, 2), (6, 8), (12, 10), (2, 12), (9, 13)]
    for cx, cy in clumps:
        cx += rnd.randint(-1, 1)
        cy += rnd.randint(-1, 1)
        cells = [(cx, cy), (cx + 1, cy), (cx, cy + 1), (cx + 1, cy + 1)]
        if rnd.random() < 0.6:
            cells.append((cx + 2, cy + rnd.randint(0, 1)))
        for x, y in cells:
            if 0 <= x < 16 and 0 <= y < 16:
                px[x, y] = main + (255,)
        if 0 <= cx < 16 and 0 <= cy < 16:
            px[cx, cy] = light + (255,)
        bx, by = cx + 1, cy + 1
        if 0 <= bx < 16 and 0 <= by < 16:
            px[bx, by] = dark + (255,)
    return img


def raw_texture(colors, seed):
    main, light, dark = colors
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(3, 14):
        for x in range(2, 14):
            if (x - 7.5) ** 2 / 30 + (y - 8.5) ** 2 / 25 <= 1 + rnd.uniform(-0.15, 0.1):
                shade = light if (x + y) < 12 else (dark if (x + y) > 19 else main)
                v = rnd.randint(-10, 10)
                px[x, y] = tuple(max(0, min(255, c + v)) for c in shade) + (255,)
    # a little quartz gangue
    for x, y in ((5, 10), (10, 6), (8, 11)):
        px[x, y] = (225, 220, 210, 255)
    return img


RAW_ITEMS = {
    "raw_sphalerite": ("Raw Sphalerite", "Zinc ore with a little cadmium: roast and distil in a Zinc Retort",
                       ORES["sphalerite_ore"][2]),
    "raw_barite": ("Raw Barite", "Barium and strontium ore: roast with charcoal in a Reverberatory Furnace",
                   ORES["barite_ore"][2]),
    "raw_spodumene": ("Raw Spodumene", "Lithium ore: leach with hydrochloric acid in a Digestion Vat",
                      ORES["spodumene_ore"][2]),
    "raw_borax": ("Raw Borax", "Borax and sylvite: boric acid in a Digestion Vat, potassium by electrolysis",
                  ORES["borax_crust"][2]),
    "raw_pitchblende": ("Raw Pitchblende", "Uranium ore: roast in a Reverberatory Furnace for uranium oxide",
                        ORES["pitchblende_ore"][2]),
    "raw_bauxite": ("Raw Bauxite", "Aluminium ore: digest with lye in a Bayer Digester", ORES["bauxite_ore"][2]),
    "raw_pyrolusite": ("Raw Pyrolusite", "Manganese ore: smelt with coke in a Charcoal Blast Furnace", ORES["pyrolusite_ore"][2]),
    "raw_pentlandite": ("Raw Pentlandite", "Nickel ore: roast in a Reverberatory Furnace, refine in a Mond Reactor",
                        ORES["deepslate_pentlandite_ore"][2]),
    "raw_wolframite": ("Raw Wolframite", "Tungsten ore: fuse with soda in a Reverberatory Furnace", ORES["wolframite_ore"][2]),
    "raw_vanadinite": ("Raw Vanadinite", "Vanadium and lead ore: roast with soda in a Reverberatory Furnace",
                       ORES["vanadinite_ore"][2]),
    "raw_fluorite": ("Raw Fluorite", "Fluorspar with cryolite: digest with sulfuric acid for hydrogen fluoride",
                     ORES["fluorite_ore"][2]),
    "heavy_mineral_concentrate": ("Heavy Mineral Concentrate", "Black sand: thorium, zircon and monazite; titanium later",
                                  ((40, 36, 38), (150, 110, 70), (20, 18, 20))),  # the item keeps the black sand
    "raw_chromite": ("Raw Chromite", "Chromium ore: ferrochrome in an Arc Furnace, or roast with soda",
                     ORES["deepslate_chromite_ore"][2]),
    "raw_molybdenite": ("Raw Molybdenite", "Molybdenum and rhenium ore: concentrate it in a Flotation Cell",
                        ORES["molybdenite_ore"][2]),
    "raw_coltan": ("Raw Coltan", "Niobium and tantalum ore: digest in hydrofluoric acid in a Crystallizer",
                   ORES["coltan_ore"][2]),
    "raw_oil_shale": ("Raw Oil Shale", "Kerogen in rock: retort it in an Oil Refinery Still for crude oil",
                      ORES["oil_shale_ore"][2]),
    "raw_cassiterite": ("Raw Cassiterite", "Tin ore: smelt with charcoal in a Smelting Furnace",
                        ORES["cassiterite_ore"][2]),
    "raw_galena": ("Raw Galena", "Lead ore with some silver: smelt for lead, or cupel for silver", ORES["galena_ore"][2]),
    "raw_cobaltite": ("Raw Cobaltite", "Cobalt, arsenic and bismuth ore: work it in a Reverberatory Furnace",
                      ORES["cobaltite_ore"][2]),
    "raw_stibnite": ("Raw Stibnite", "Antimony ore: roast and reduce in a Reverberatory Furnace", ORES["stibnite_ore"][2]),
    "raw_arsenopyrite": ("Raw Arsenopyrite", "Arsenic ore: roast in a Reverberatory Furnace for white arsenic",
                         ORES["arsenopyrite_ore"][2]),
}


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def generate_ores(assets, data_dir, runtime_dir, lang, mod_id, item_ids):
    """item_ids: every mod item id that already exists (elements, substances); raw ores are added here."""
    known = set(item_ids) | set(RAW_ITEMS)
    tex_block = assets / "textures" / "block"
    tex_item = assets / "textures" / "item"
    for i, (oid, (name, host, colors, drop, (lo, hi), (xlo, xhi), tier, desc)) in enumerate(ORES.items()):
        assert drop.startswith(mod_id + ":") and drop.split(":")[1] in known, f"{oid}: unknown drop {drop}"
        ore_texture(oid, host, colors, 100 + i).save(tex_block / f"{oid}.png")
        _dump(assets / "models" / "block" / f"{oid}.json",
              {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod_id}:block/{oid}"}})
        _dump(assets / "blockstates" / f"{oid}.json", {"variants": {"": {"model": f"{mod_id}:block/{oid}"}}})
        _dump(assets / "models" / "item" / f"{oid}.json", {"parent": f"{mod_id}:block/{oid}"})
        lang[f"block.{mod_id}.{oid}"] = name
        lang[f"block.{mod_id}.{oid}.desc"] = desc
        count = [] if lo == hi == 1 else [{"function": "minecraft:set_count", "add": False,
                                            "count": {"type": "minecraft:uniform", "min": float(lo), "max": float(hi)}}]
        _dump(data_dir / "loot_table" / "blocks" / f"{oid}.json", {
            "type": "minecraft:block",
            "pools": [{"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": f"{mod_id}:{oid}", "conditions": [{
                    "condition": "minecraft:match_tool",
                    "predicate": {"predicates": {"minecraft:enchantments": [
                        {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
                {"type": "minecraft:item", "name": drop, "functions": count + [
                    {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                     "formula": "minecraft:ore_drops"},
                    {"function": "minecraft:explosion_decay"}]}]}]}],
            "random_sequence": f"{mod_id}:blocks/{oid}"})
    for rid, (name, tip, colors) in RAW_ITEMS.items():
        raw_texture(colors, len(rid) * 13).save(tex_item / f"{rid}.png")
        _dump(assets / "models" / "item" / f"{rid}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{rid}"}})
        lang[f"item.{mod_id}.{rid}"] = name
        lang[f"item.{mod_id}.{rid}.tip"] = tip

    mc_tags = data_dir.parent / "minecraft" / "tags" / "block"
    _dump(mc_tags / "needs_stone_tool.json",
          {"replace": False, "values": [f"{mod_id}:{o}" for o, v in ORES.items() if v[6] == "stone"]})
    _dump(mc_tags / "needs_iron_tool.json",
          {"replace": False, "values": [f"{mod_id}:{o}" for o, v in ORES.items() if v[6] == "iron"]})

    features = []
    for fid, (targets, size, count, height, biomes) in FEATURES.items():
        for _, ore in targets:
            assert ore in ORES, f"{fid}: unknown ore {ore}"
        _dump(data_dir / "worldgen" / "configured_feature" / f"{fid}.json", {
            "type": "minecraft:ore",
            "config": {"size": size, "discard_chance_on_air_exposure": 0.0,
                       "targets": [{"target": t, "state": {"Name": f"{mod_id}:{o}"}} for t, o in targets]}})
        _dump(data_dir / "worldgen" / "placed_feature" / f"{fid}.json", {
            "feature": f"{mod_id}:{fid}",
            "placement": [{"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
                          {"type": "minecraft:height_range", "height": height}, {"type": "minecraft:biome"}]})
        features.append({"id": fid, "biomes": biomes})

    ores = [{"id": oid, "host": v[1], "xpMin": v[5][0], "xpMax": v[5][1]} for oid, v in ORES.items()]
    _dump(runtime_dir / "ores.json", {"ores": ores, "features": features, "rawItems": list(RAW_ITEMS)})
    drops = {v[3] for v in ORES.values()}
    return ORES, RAW_ITEMS, drops
