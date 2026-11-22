"""Level 9, stage 4 (3.64.0): the Moon. Its dimension (dimension type, biome, generator), rocks, the Space Suit, the
crew and emergency capsules, Life Support, the Earth in the lunar sky, and the guide paragraphs (the Space chapter).

Java: com.periodicelements.space.Moon (blocks, items, gravity, vacuum, travel), MoonChunkGenerator, LifeSupportBlock(+Entity),
client/MoonSky. space.py calls generate_moon and merges LANG, the guide paragraphs and translations(code).
"""
import json
import math
import random

from PIL import Image

from moon_i18n import TR, GUIDE_I18N

ROCKS = ["lunar_regolith", "anorthosite", "mare_basalt", "lunar_ice"]
SUIT = ["space_suit_helmet", "space_suit_chestplate", "space_suit_leggings", "space_suit_boots"]
ITEMS = SUIT + ["crew_capsule", "emergency_capsule"]
BLOCKS = ["life_support"]
ITEM_UNLOCKS = set(ITEMS) | set(BLOCKS)

M = "periodic_elements"
LANG = {
    f"dimension.{M}.moon": "the Moon",
    f"dimension.{M}.earth": "the Earth",
    f"block.{M}.lunar_regolith": "Lunar Regolith",
    f"block.{M}.lunar_regolith.desc": "Grey dust and broken rock over the whole Moon, soaked with helium-3 by the solar wind",
    f"block.{M}.anorthosite": "Anorthosite",
    f"block.{M}.anorthosite.desc": "The bright rock of the lunar highlands: almost pure calcium feldspar",
    f"block.{M}.mare_basalt": "Mare Basalt",
    f"block.{M}.mare_basalt.desc": "Dark lava of the lunar seas, rich in ilmenite: iron, titanium and oxygen",
    f"block.{M}.lunar_ice": "Lunar Ice",
    f"block.{M}.lunar_ice.desc": "Water ice from a crater floor the sun never reaches",
    f"block.{M}.life_support": "Life Support",
    f"block.{M}.life_support.desc": "Feed it oxygen cylinders: it fills the sealed room it stands in with air; doors make airlocks",
    f"item.{M}.space_suit_helmet": "Space Suit Helmet",
    f"item.{M}.space_suit_helmet.tip": "A gold-tinted visor against the unfiltered sun",
    f"item.{M}.space_suit_chestplate": "Space Suit",
    f"item.{M}.space_suit_chestplate.tip": "With an oxygen backpack: use an oxygen cylinder while you wear it to fill it (10 minutes)",
    f"item.{M}.space_suit_leggings": "Space Suit Trousers",
    f"item.{M}.space_suit_leggings.tip": "Layers of nylon and aramid round a rubber bladder",
    f"item.{M}.space_suit_boots": "Moon Boots",
    f"item.{M}.space_suit_boots.tip": "All four pieces keep you alive where there is no air",
    f"item.{M}.space_suit.oxygen": "Oxygen: %s s",
    f"item.{M}.crew_capsule": "Crew Capsule",
    f"item.{M}.crew_capsule.tip": "Load it into a Payload Fairing: you ride it to the Moon, and home again from a rocket built there",
    f"item.{M}.emergency_capsule": "Emergency Return Capsule",
    f"item.{M}.emergency_capsule.tip": "Use it away from the Earth: it brings you home, but your pack stays behind in a chest",
    f"message.{M}.moon.emergency_home": "You are already on the Earth",
    f"message.{M}.moon.emergency": "Back on the Earth. %s stacks stayed behind in a chest at %s, %s, %s",
    f"message.{M}.moon.suit_filled": "Space suit oxygen full",
    f"message.{M}.moon.oxygen_low": "Space suit: %s seconds of oxygen left",
    f"message.{M}.moon.no_oxygen": "Your space suit is out of oxygen!",
    f"message.{M}.moon.vacuum": "No air! Wear a full space suit with oxygen",
    f"message.{M}.moon.landed": "The Eagle has landed: you are on the Moon. Build a rocket here to fly home",
    f"message.{M}.moon.home": "Splashdown: you are back on the Earth",
    f"message.{M}.rocket.short_to": "%s km/s short: %s of the %s km/s needed to reach %s",
    f"message.{M}.rocket.total_to": "Total %s km/s of the %s km/s needed to reach %s",
    f"message.{M}.rocket.crew_missing": "Launch aborted: the crew must stand within %s blocks of the pad",
    f"message.{M}.life_support.full": "Life Support holds 16 cylinders",
    f"message.{M}.life_support.no_oxygen": "Life Support: no oxygen. Use oxygen cylinders on it",
    f"message.{M}.life_support.leak": "Life Support: air escapes at %s, %s, %s; oxygen for %s min",
    f"message.{M}.life_support.sealed": "Life Support: sealed, %s blocks of air, oxygen for %s min (%s cylinders in store)",
    # 3.64.0: the Tokamak also burns helium-3
    f"gui.{M}.power.fusion.no_fuel": "needs deuterium, tritium or helium-3, and a helium flask",
}

GUIDE = [
    "@9 # Mission Control",
    "@9 Level 9, Other Worlds, is researched at Mission Control, made from the Space Centre. Its projects take you to "
    "the Moon and to Mars.",
    "@9 # The Space Suit",
    "@9 Away from the Earth's air you need all four pieces of the Space Suit. Its backpack holds 10 minutes of oxygen: "
    "use an oxygen cylinder while you wear it to fill it. Without the suit, or with the oxygen gone, you suffocate in "
    "seconds.",
    "@9 # Flying to the Moon",
    "@9 Load a Crew Capsule into the Payload Fairing and start the countdown at the Launch Console, then stand within 24 "
    "blocks of the pad: when it ends you ride on top of the rocket. The capsule weighs 3 t and the flight needs 13 km/s, "
    "for example a kerosene stage of 6 tanks under two hydrogen stages of 6 and 3 tanks. Real missions need about "
    "15 km/s; the game shortens the landing.",
    "@9 You land where the pad stood, but on the Moon, with the capsule back in your pack. To come home, build a rocket "
    "there: a Launch Pad, an engine, a tank or two, a fairing with the capsule and a Launch Console. Going home needs "
    "only 2.8 km/s; bring the propellant, or make it on the Moon. If you are stranded, the Emergency Return Capsule "
    "brings you home at once, but everything in your pack (not your hotbar) stays behind in a chest.",
    "@9 # The Moon",
    "@9 Gravity is a sixth of the Earth's: you jump much higher (a little less in the stiff suit) and falls hurt a sixth "
    "as much. The sky is black, with the Earth always in the same place. Dark seas of mare basalt fill the lowlands, "
    "bright anorthosite makes the highlands and grey regolith covers everything. Craters of every size pit the ground; "
    "more than 1000 blocks north or south, the floors of the big ones hold ice, where the sun never shines.",
    "@9 # A base",
    "@9 Life Support fills a sealed room with air: walls, floor and roof of full blocks, with doors or trapdoors as "
    "airlocks (air escapes while one stands open). Use oxygen cylinders on it; one keeps a base breathing for two days. "
    "Use it with an empty hand to see how much air it holds, or where it leaks. Inside you breathe without the suit, "
    "and the suit's backpack refills.",
    "@9 # Lunar resources",
    "@9 Mare basalt and regolith are rich in ilmenite. After Lunar Oxygen, the Sintering Furnace with hydrogen in its "
    "reagent slot takes ilmenite's oxygen away as water, leaving iron and titanium dioxide; split the water in the "
    "Electrolysis Cell. Lunar ice from the polar craters gives water directly.",
    "@9 After Helium-3 Mining, regolith heated in the Sintering Furnace gives up its helium-3 (the game raises its share "
    "enormously: about 22 blocks fill a flask). In the Tokamak, helium-3 burns with deuterium in place of tritium: no "
    "lithium needed, and a fifth more energy.",
]

for _code, _paras in GUIDE_I18N.items():
    assert len(_paras) == len(GUIDE), (_code, len(_paras), len(GUIDE))


def translations(code):
    return dict(TR.get(code, {}))


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _px(img, x, y, c, a=255):
    w, h = img.size
    if 0 <= x < w and 0 <= y < h:
        img.putpixel((x, y), tuple(int(max(0, min(255, v))) for v in c) + (a,))


def _noise(seed, colour, var, size=(16, 16)):
    rnd = random.Random(seed)
    img = Image.new("RGBA", size)
    for y in range(size[1]):
        for x in range(size[0]):
            d = rnd.randint(-var, var)
            _px(img, x, y, [c + d for c in colour])
    return img


def _rect(img, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            _px(img, x, y, c)


def block_textures():
    out = {}
    reg = _noise(1101, (132, 132, 134), 9)
    rnd = random.Random(1102)
    for _ in range(14):   # pebbles and glass beads
        x, y = rnd.randrange(16), rnd.randrange(16)
        _px(reg, x, y, (96, 96, 100) if rnd.random() < 0.7 else (170, 168, 160))
    out["lunar_regolith"] = reg
    an = _noise(1103, (196, 196, 192), 6)
    for _ in range(10):   # plagioclase laths
        x, y = rnd.randrange(14), rnd.randrange(16)
        _rect(an, x, y, x + 2, y, (226, 226, 222))
    for _ in range(6):
        _px(an, rnd.randrange(16), rnd.randrange(16), (120, 118, 116))
    out["anorthosite"] = an
    mb = _noise(1104, (64, 64, 70), 6)
    for _ in range(16):   # ilmenite specks, black and metallic
        x, y = rnd.randrange(16), rnd.randrange(16)
        _px(mb, x, y, (30, 30, 34) if rnd.random() < 0.6 else (150, 150, 160))
    for _ in range(5):    # gas bubbles of the lava
        _px(mb, rnd.randrange(16), rnd.randrange(16), (40, 40, 44))
    out["mare_basalt"] = mb
    ice = _noise(1105, (190, 204, 214), 7)
    for _ in range(18):
        _px(ice, rnd.randrange(16), rnd.randrange(16), (120, 120, 124))
    out["lunar_ice"] = ice
    side = _noise(1106, (226, 228, 232), 4)
    _rect(side, 0, 0, 15, 1, (160, 164, 172))
    _rect(side, 0, 14, 15, 15, (160, 164, 172))
    for x in (3, 7, 11):   # air ducts
        _rect(side, x, 4, x + 1, 11, (150, 154, 162))
    out["life_support_side"] = side
    for lit in (False, True):
        f = side.copy()
        _rect(f, 3, 3, 12, 8, (24, 28, 34))
        _rect(f, 4, 4, 11, 7, (60, 200, 120) if lit else (60, 70, 76))   # the gauge panel
        _rect(f, 5, 10, 10, 12, (90, 140, 220))   # oxygen cylinders behind a window
        _rect(f, 7, 10, 8, 12, (120, 170, 240))
        out["life_support_front_lit" if lit else "life_support_front"] = f
    top = _noise(1107, (180, 184, 190), 5)
    for y in range(16):
        for x in range(16):
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 22 and (x + y) % 2 == 0:
                _px(top, x, y, (100, 104, 112))   # fan grille
    out["life_support_top"] = top
    return out


WHITE = (236, 236, 232)


def item_texture(iid):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    if iid == "space_suit_helmet":
        for y in range(2, 14):
            for x in range(2, 14):
                if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 34:
                    _px(img, x, y, WHITE)
        for y in range(5, 11):
            for x in range(4, 12):
                if (x - 7.5) ** 2 / 14 + (y - 7.8) ** 2 / 8 <= 1:
                    _px(img, x, y, (210, 160, 50) if (x + y) % 5 else (250, 220, 120))   # the gold visor
        _rect(img, 4, 13, 11, 14, (150, 154, 160))
    elif iid == "space_suit_chestplate":
        _rect(img, 3, 3, 12, 13, WHITE)
        _rect(img, 1, 3, 2, 9, WHITE)
        _rect(img, 13, 3, 14, 9, WHITE)
        _rect(img, 6, 5, 9, 8, (90, 110, 170))   # the control box
        _px(img, 7, 6, (230, 70, 60))
        _px(img, 8, 6, (80, 220, 120))
        _rect(img, 3, 10, 12, 10, (200, 60, 50))   # red commander stripe
    elif iid == "space_suit_leggings":
        _rect(img, 3, 2, 12, 4, WHITE)
        _rect(img, 3, 5, 7, 14, WHITE)
        _rect(img, 8, 5, 12, 14, WHITE)
        _rect(img, 3, 8, 7, 8, (190, 192, 196))
        _rect(img, 8, 8, 12, 8, (190, 192, 196))
    elif iid == "space_suit_boots":
        _rect(img, 2, 7, 6, 12, WHITE)
        _rect(img, 9, 7, 13, 12, WHITE)
        _rect(img, 1, 12, 7, 13, (60, 60, 66))
        _rect(img, 8, 12, 14, 13, (60, 60, 66))
    elif iid in ("crew_capsule", "emergency_capsule"):
        body = (200, 204, 210) if iid == "crew_capsule" else (230, 120, 40)
        for y in range(2, 14):
            half = 1 + (y - 2) * 6 // 11
            _rect(img, 8 - half, y, 7 + half, y, body)
        _rect(img, 1, 13, 14, 14, (110, 80, 50))   # heat shield
        _rect(img, 6, 6, 9, 8, (40, 60, 90))      # window
        if iid == "emergency_capsule":
            _rect(img, 7, 0, 8, 1, (240, 240, 240))   # parachute cord
    else:
        raise KeyError(iid)
    return img


def earth_texture():
    """The Earth seen from the Moon: blue ocean, brown and green land, white cloud, a bright limb."""
    size = 32
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    rnd = random.Random(1969)
    land = [(rnd.uniform(4, 28), rnd.uniform(4, 28), rnd.uniform(3, 7)) for _ in range(5)]
    clouds = [(rnd.uniform(2, 30), rnd.uniform(2, 30), rnd.uniform(2, 4)) for _ in range(9)]
    for y in range(size):
        for x in range(size):
            dx, dy = x - 15.5, y - 15.5
            r = math.hypot(dx, dy)
            if r > 15:
                continue
            c = (30, 70, 170)
            if any(math.hypot(x - lx, y - ly) < lr for lx, ly, lr in land):
                c = (80, 130, 60) if (x * 7 + y * 3) % 5 else (150, 120, 70)
            if abs(dy) > 12:
                c = (240, 244, 250)   # polar ice
            if any(math.hypot(x - cx, (y - cy) * 1.6) < cr for cx, cy, cr in clouds):
                c = (245, 245, 250)
            shade = 1.0 - max(0.0, (dx + dy) / 30)   # lit from the upper left
            if r > 14:
                c = (170, 200, 255)
            _px(img, x, y, [v * (0.55 + 0.45 * shade) for v in c])
    return img


def armor_layer(n):
    layer = _noise(1960 + n, WHITE, 5, (64, 32))
    for x in range(0, 64, 8):
        _rect(layer, x, 20, x, 31, (196, 198, 204))   # seams
    if n == 1:
        _rect(layer, 8, 8, 15, 15, (210, 160, 50))   # the visor on the helmet's face
        _rect(layer, 20, 20, 27, 22, (90, 110, 170))   # chest control box
        _rect(layer, 32, 20, 39, 31, (180, 184, 190))   # backpack
    return layer


P = "periodic_elements:"
RECIPES = {
    "space_suit_helmet": (["ANA", "AGA"], {"A": P + "aramid", "N": P + "nylon", "G": "minecraft:glass_pane"}, 1),
    "space_suit_chestplate": (["A A", "NCN", "AOA"], {"A": P + "aramid", "N": P + "nylon", "C": P + "integrated_circuit",
                                                      "O": P + "steel_cylinder"}, 1),
    "space_suit_leggings": (["ANA", "A A", "A A"], {"A": P + "aramid", "N": P + "nylon"}, 1),
    "space_suit_boots": (["A A", "N N"], {"A": P + "aramid", "N": P + "nylon"}, 1),
    "crew_capsule": (["TCT", "TRT", "PPP"], {"T": P + "titanium_alloy_ingot", "C": P + "integrated_circuit",
                                             "R": P + "parachute", "P": P + "composite_plate"}, 1),
    "emergency_capsule": (["ACA", "ARA", "PPP"], {"A": P + "aluminium", "C": P + "integrated_circuit",
                                                  "R": P + "parachute", "P": P + "composite_plate"}, 1),
    "life_support": (["SCS", "WOW", "SCS"], {"S": P + "stainless_steel_ingot", "C": P + "integrated_circuit",
                                             "W": P + "copper_wire", "O": P + "steel_cylinder"}, 1),
}

DIMENSION_TYPE = {
    "ultrawarm": False, "natural": False, "coordinate_scale": 1.0, "has_skylight": True, "has_ceiling": False,
    "ambient_light": 0.0, "fixed_time": 4000, "monster_spawn_light_level": 0, "monster_spawn_block_light_limit": 0,
    "piglin_safe": False, "bed_works": True, "respawn_anchor_works": False, "has_raids": False,
    "min_y": 0, "height": 256, "logical_height": 256, "infiniburn": "#minecraft:infiniburn_overworld",
    "effects": "periodic_elements:moon",
}

BIOME = {
    "has_precipitation": False, "temperature": 0.5, "downfall": 0.0,
    "effects": {"sky_color": 0, "fog_color": 0, "water_color": 4159204, "water_fog_color": 329011,
                "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}},
    "spawners": {}, "spawn_costs": {}, "carvers": {}, "features": [],
}


def generate_moon(assets, data_root, lang, mod):
    tex_block = assets / "textures" / "block"
    tex_item = assets / "textures" / "item"
    models = assets / "models"
    states = assets / "blockstates"
    for name, img in block_textures().items():
        img.save(tex_block / f"{name}.png")
    for b in ROCKS:
        _dump(models / "block" / f"{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod}:block/{b}"}})
        _dump(models / "item" / f"{b}.json", {"parent": f"{mod}:block/{b}"})
        if b == "lunar_ice":
            _dump(states / f"{b}.json", {"variants": {"": {"model": f"{mod}:block/{b}"}}})
        else:   # natural ground turns at random, like the other rocks
            _dump(states / f"{b}.json", {"variants": {"": [{"model": f"{mod}:block/{b}", "y": y} for y in (0, 90, 180, 270)]}})
    for lit in (False, True):
        name = "life_support_lit" if lit else "life_support"
        _dump(models / "block" / f"{name}.json", {"parent": "minecraft:block/cube", "textures": {
            "particle": f"{mod}:block/life_support_side",
            "north": f"{mod}:block/life_support_front" + ("_lit" if lit else ""),
            "south": f"{mod}:block/life_support_side", "east": f"{mod}:block/life_support_side",
            "west": f"{mod}:block/life_support_side", "up": f"{mod}:block/life_support_top",
            "down": f"{mod}:block/life_support_top"}})
    _dump(states / "life_support.json", {"variants": {"lit=false": {"model": f"{mod}:block/life_support"},
                                                      "lit=true": {"model": f"{mod}:block/life_support_lit"}}})
    _dump(models / "item" / "life_support.json", {"parent": f"{mod}:block/life_support"})
    for iid in ITEMS:
        item_texture(iid).save(tex_item / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{iid}"}})
    arm = assets / "textures" / "models" / "armor"
    arm.mkdir(parents=True, exist_ok=True)
    for n in (1, 2):
        armor_layer(n).save(arm / f"space_suit_layer_{n}.png")
    env = assets / "textures" / "environment"
    env.mkdir(parents=True, exist_ok=True)
    earth_texture().save(env / "earth.png")

    data_dir = data_root / mod
    for b in ROCKS + BLOCKS:
        _dump(data_dir / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{b}"}],
             "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for rid, (pattern, key, count) in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                                    "key": {k: {"item": v} for k, v in key.items()},
                                                    "result": {"id": f"{mod}:{rid}", "count": count}})
    _dump(data_dir / "dimension_type" / "moon.json", DIMENSION_TYPE)
    _dump(data_dir / "worldgen" / "biome" / "moon.json", BIOME)
    _dump(data_dir / "dimension" / "moon.json", {"type": f"{mod}:moon", "generator": {
        "type": f"{mod}:moon", "biome_source": {"type": "minecraft:fixed", "biome": f"{mod}:moon"}}})
    for tool, ids in (("pickaxe", ["anorthosite", "mare_basalt", "lunar_ice", "life_support"]), ("shovel", ["lunar_regolith"])):
        tag = data_root / "minecraft" / "tags" / "block" / "mineable" / f"{tool}.json"
        values = json.loads(tag.read_text(encoding="utf-8"))["values"] if tag.exists() else []
        values += [f"{mod}:{b}" for b in ids if f"{mod}:{b}" not in values]
        _dump(tag, {"replace": False, "values": values})
    lang.update(LANG)
