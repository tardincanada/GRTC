"""Level 9, stage 5 (3.66.0): Mars. Its dimension (dimension type, biome, generator), rocks and ice, washed soil, the
Mars Crew Capsule, the Methane Rocket Engine, Phobos in the sky, and the guide paragraphs (the Space chapter).

Java: com.periodicelements.space.Mars (blocks, storms, sunlight), MarsChunkGenerator, Moon (gravity, breathing, travel),
client/MarsSky. The research projects are rows of data/research.csv, the oxygen and methane factories rows of
reactions.csv. space.py calls generate_mars and merges LANG, the guide paragraphs and translations(code).
"""
import json
import math
import random

from PIL import Image

from mars_i18n import TR, GUIDE_I18N

ROCKS = ["martian_regolith", "martian_basalt", "martian_sulfate", "hematite_concretion", "martian_ice", "dry_ice"]
BLOCKS = ["washed_martian_soil"]
ITEMS = ["mars_capsule"]
ITEM_UNLOCKS = set(ITEMS) | set(BLOCKS) | {"methalox_engine"}

M = "periodic_elements"
LANG = {
    f"dimension.{M}.mars": "Mars",
    f"block.{M}.martian_regolith": "Martian Regolith",
    f"block.{M}.martian_regolith.desc": "Red dust and sand: iron oxide over everything, and a little toxic perchlorate",
    f"block.{M}.martian_basalt": "Martian Basalt",
    f"block.{M}.martian_basalt.desc": "Dark lava rock of olivine and pyroxene, the floor of most of Mars",
    f"block.{M}.martian_sulfate": "Sulfate Beds",
    f"block.{M}.martian_sulfate.desc": "Layered gypsum, kieserite and jarosite laid down by water that dried away long ago",
    f"block.{M}.hematite_concretion": "Hematite Concretions",
    f"block.{M}.hematite_concretion.desc": "The \"blueberries\" Opportunity found: little balls of hematite grown in wet rock",
    f"block.{M}.martian_ice": "Martian Ice",
    f"block.{M}.martian_ice.desc": "Dusty water ice from under the soil and the polar caps",
    f"block.{M}.dry_ice": "Carbon Dioxide Frost",
    f"block.{M}.dry_ice.desc": "Frozen carbon dioxide over the polar caps: dry ice, at -125 °C",
    f"block.{M}.washed_martian_soil": "Washed Martian Soil",
    f"block.{M}.washed_martian_soil.desc": "Regolith rinsed free of perchlorate: till it with a hoe and farm it in a sealed greenhouse",
    f"block.{M}.methalox_engine": "Methane Rocket Engine",
    f"block.{M}.methalox_engine.desc": "Burns liquid methane with liquid oxygen (Isp 360 s), both made on Mars; lifts up to 6 Propellant Tanks",
    f"item.{M}.mars_capsule": "Mars Crew Capsule",
    f"item.{M}.mars_capsule.tip": "Load it into a Payload Fairing: you ride it to Mars, and home again from a rocket built there",
    f"message.{M}.mars.landed": "You are on Mars. Make methane and oxygen here to fly home",
    f"message.{M}.mars.storm": "A dust storm is rising: solar panels will give little until it settles",
}

GUIDE = [
    "@9 # Flying to Mars",
    "@9 After Mars Transit, load a Mars Crew Capsule into the Payload Fairing in place of the Crew Capsule. The flight "
    "needs 14 km/s: four stages (two kerosene stages of 6 tanks under hydrogen stages of 6 and 2 tanks), or three if "
    "methane engines lift the first two. Real missions need a little more, but Mars's air brakes the landing.",
    "@9 To come home you need 6.4 km/s: the climb out of Mars's deeper gravity well and the push towards the Earth. "
    "Hydrogen boils away on the long trip, so the way home is a Methane Rocket Engine fed with methane and oxygen made "
    "on Mars, as in Robert Zubrin's Mars Direct plan (1990).",
    "@9 # Mars",
    "@9 Gravity is 0.38 of the Earth's. The air is a hundredth as thick and almost all carbon dioxide: wear the Space "
    "Suit, and nothing burns. It still stops a little of the cosmic rays (half the Moon's dose), and a light wind turns "
    "turbines slowly. Sunlight is 43% of the Earth's.",
    "@9 Fine red dust in the air makes the day sky butterscotch and the sunset blue. Every few days a dust storm may "
    "rise: the sky goes brown, you can see only a few blocks, and solar panels give almost nothing for a day or two.",
    "@9 The north is a smooth lowland and the south old highlands full of craters. Giant shield volcanoes of basalt "
    "rise over a hundred blocks; long canyons cut down to layered sulfate beds; dry river beds wind across the plains, "
    "with hematite concretions on their floors. More than 800 blocks north or south, ice lies a few blocks under the "
    "soil; past 1600 are the polar caps of ice, frosted with frozen carbon dioxide. Nothing lives on Mars.",
    "@9 # Living off the land",
    "@9 On Mars the Air Liquefier draws Martian air: carbon dioxide, with some nitrogen and argon. After Oxygen from "
    "Martian Air, the Electrolysis Cell splits carbon dioxide into carbon monoxide and oxygen, like MOXIE on the "
    "Perseverance rover (2021). Ice gives water, and water hydrogen.",
    "@9 After the Sabatier Process, the High Pressure Reactor makes methane and water from carbon dioxide and hydrogen; "
    "after Methane Rockets, the Air Liquefier turns methane into liquid methane for the Methane Rocket Engine.",
    "@9 # A greenhouse",
    "@9 Martian soil holds perchlorate, a salt that poisons plants. After Martian Greenhouse, craft 8 regolith with a "
    "water bucket into Washed Martian Soil and till it with a hoe. Plants need air: on Mars and the Moon nothing grows "
    "outside a sealed room kept full of air by Life Support.",
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
    rnd = random.Random(2001)
    reg = _noise(2002, (178, 96, 56), 10)
    for _ in range(16):   # grains of darker basalt sand and paler dust
        _px(reg, rnd.randrange(16), rnd.randrange(16), (120, 66, 42) if rnd.random() < 0.6 else (206, 140, 96))
    for _ in range(3):    # a pebble
        x, y = rnd.randrange(15), rnd.randrange(15)
        _rect(reg, x, y, x + 1, y, (92, 72, 64))
    out["martian_regolith"] = reg
    bas = _noise(2003, (82, 68, 62), 7)
    for _ in range(12):   # olivine, bottle green
        _px(bas, rnd.randrange(16), rnd.randrange(16), (112, 130, 62))
    for _ in range(10):   # pyroxene, near black
        _px(bas, rnd.randrange(16), rnd.randrange(16), (44, 40, 40))
    for _ in range(8):    # red dust in the pores
        _px(bas, rnd.randrange(16), rnd.randrange(16), (150, 82, 50))
    out["martian_basalt"] = bas
    sul = _noise(2004, (196, 170, 122), 6)
    for y in range(16):   # thin beds, each a little different
        if y % 4 == 0:
            _rect(sul, 0, y, 15, y, (168, 140, 96))
        elif y % 4 == 2:
            for x in range(16):
                if rnd.random() < 0.5:
                    _px(sul, x, y, (214, 196, 150))
    for _ in range(5):    # jarosite, mustard yellow
        _px(sul, rnd.randrange(16), rnd.randrange(16), (200, 160, 60))
    out["martian_sulfate"] = sul
    hem = sul.copy()
    for _ in range(9):    # the blueberries
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        _px(hem, x, y, (64, 66, 80))
        _px(hem, x + 1, y, (56, 58, 70))
        _px(hem, x, y + 1, (52, 54, 66))
        _px(hem, x + 1, y + 1, (88, 92, 110))
    out["hematite_concretion"] = hem
    ice = _noise(2005, (232, 228, 224), 5)
    for y in range(16):
        if y % 5 == 1:   # dust layers of the polar deposits
            for x in range(16):
                _px(ice, x, y, (204, 170, 146) if rnd.random() < 0.7 else (220, 200, 186))
    out["martian_ice"] = ice
    dry = _noise(2006, (240, 242, 246), 4)
    for _ in range(14):
        _px(dry, rnd.randrange(16), rnd.randrange(16), (255, 255, 255))
    for _ in range(4):
        _px(dry, rnd.randrange(16), rnd.randrange(16), (214, 200, 196))
    out["dry_ice"] = dry
    soil = _noise(2007, (112, 66, 46), 9)
    for _ in range(10):
        _px(soil, rnd.randrange(16), rnd.randrange(16), (84, 50, 36))
    for _ in range(6):
        _px(soil, rnd.randrange(16), rnd.randrange(16), (140, 90, 64))
    out["washed_martian_soil"] = soil
    return out


def mars_capsule_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    body = (224, 214, 196)
    for y in range(1, 14):
        half = 2 + (y - 1) * 5 // 12
        _rect(img, 8 - half, y, 7 + half, y, body)
    _rect(img, 3, 9, 12, 9, (190, 70, 40))     # red band: bound for Mars
    _rect(img, 1, 13, 14, 14, (110, 80, 50))   # heat shield
    _rect(img, 6, 4, 9, 6, (40, 60, 90))       # window
    return img


def phobos_texture():
    """Phobos: a small, dark, lumpy moon, lit from one side."""
    size = 16
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    rnd = random.Random(1877)
    bumps = [(rnd.uniform(0, math.tau), rnd.uniform(0.6, 1.0)) for _ in range(5)]
    for y in range(size):
        for x in range(size):
            dx, dy = (x - 7.5) / 7.5, (y - 7.5) / 5.5
            ang = math.atan2(dy, dx)
            r = 1.0 - 0.18 * sum(b * math.cos(2 * (ang - a)) for a, b in bumps) / len(bumps)
            if math.hypot(dx, dy) > r:
                continue
            shade = 0.45 + 0.55 * max(0.0, 1 - (dx + 1) / 2.2)
            c = (122, 106, 94)
            if (x * 5 + y * 3) % 11 == 0:
                c = (90, 80, 72)   # craters, Stickney among them
            _px(img, x, y, [v * shade for v in c])
    return img


P = "periodic_elements:"
RECIPES = {
    "mars_capsule": (["TCT", "TLT", "PRP"], {"T": P + "titanium_alloy_ingot", "C": P + "integrated_circuit",
                                             "L": P + "life_support", "R": P + "parachute", "P": P + "composite_plate"}, 1),
}

DIMENSION_TYPE = {
    "ultrawarm": False, "natural": False, "coordinate_scale": 1.0, "has_skylight": True, "has_ceiling": False,
    "ambient_light": 0.0, "monster_spawn_light_level": 0, "monster_spawn_block_light_limit": 0,
    "piglin_safe": False, "bed_works": True, "respawn_anchor_works": False, "has_raids": False,
    "min_y": 0, "height": 256, "logical_height": 256, "infiniburn": "#minecraft:infiniburn_overworld",
    "effects": "periodic_elements:mars",
}

BIOME = {
    "has_precipitation": False, "temperature": 0.0, "downfall": 0.0,
    "effects": {"sky_color": 0xD2A47A, "fog_color": 0xDBB78F, "water_color": 4159204, "water_fog_color": 329011,
                "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}},
    "spawners": {}, "spawn_costs": {}, "carvers": {}, "features": [],
}


def generate_mars(assets, data_root, lang, mod):
    tex_block = assets / "textures" / "block"
    tex_item = assets / "textures" / "item"
    models = assets / "models"
    states = assets / "blockstates"
    for name, img in block_textures().items():
        img.save(tex_block / f"{name}.png")
    for b in ROCKS + BLOCKS:
        _dump(models / "block" / f"{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod}:block/{b}"}})
        _dump(models / "item" / f"{b}.json", {"parent": f"{mod}:block/{b}"})
        if b in ("martian_ice", "dry_ice"):
            _dump(states / f"{b}.json", {"variants": {"": {"model": f"{mod}:block/{b}"}}})
        else:   # natural ground turns at random, like the other rocks; turning about y keeps the beds level
            _dump(states / f"{b}.json", {"variants": {"": [{"model": f"{mod}:block/{b}", "y": y} for y in (0, 90, 180, 270)]}})
    for iid in ITEMS:
        mars_capsule_texture().save(tex_item / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{iid}"}})
    env = assets / "textures" / "environment"
    env.mkdir(parents=True, exist_ok=True)
    phobos_texture().save(env / "phobos.png")

    data_dir = data_root / mod
    for b in ROCKS + BLOCKS:
        _dump(data_dir / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{b}"}],
             "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for rid, (pattern, key, count) in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                                    "key": {k: {"item": v} for k, v in key.items()},
                                                    "result": {"id": f"{mod}:{rid}", "count": count}})
    # rinsing the perchlorate out: 8 regolith and a bucket of water (the bucket comes back)
    _dump(data_dir / "recipe" / "washed_martian_soil.json", {
        "type": "minecraft:crafting_shapeless", "category": "building",
        "ingredients": [{"item": f"{mod}:martian_regolith"}] * 8 + [{"item": "minecraft:water_bucket"}],
        "result": {"id": f"{mod}:washed_martian_soil", "count": 8}})
    _dump(data_dir / "dimension_type" / "mars.json", DIMENSION_TYPE)
    _dump(data_dir / "worldgen" / "biome" / "mars.json", BIOME)
    _dump(data_dir / "dimension" / "mars.json", {"type": f"{mod}:mars", "generator": {
        "type": f"{mod}:mars", "biome_source": {"type": "minecraft:fixed", "biome": f"{mod}:mars"}}})
    for tool, ids in (("pickaxe", ["martian_basalt", "martian_sulfate", "hematite_concretion", "martian_ice"]),
                      ("shovel", ["martian_regolith", "dry_ice", "washed_martian_soil"])):
        tag = data_root / "minecraft" / "tags" / "block" / "mineable" / f"{tool}.json"
        values = json.loads(tag.read_text(encoding="utf-8"))["values"] if tag.exists() else []
        values += [f"{mod}:{b}" for b in ids if f"{mod}:{b}" not in values]
        _dump(tag, {"replace": False, "values": values})
    # washed soil holds saplings and the like, as dirt does
    tag = data_root / "minecraft" / "tags" / "block" / "dirt.json"
    values = json.loads(tag.read_text(encoding="utf-8"))["values"] if tag.exists() else []
    if f"{mod}:washed_martian_soil" not in values:
        values.append(f"{mod}:washed_martian_soil")
    _dump(tag, {"replace": False, "values": values})
    lang.update(LANG)
