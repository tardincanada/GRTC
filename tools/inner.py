"""Level 10, stage 7 (3.68.0): Mercury and floating Venus. Their dimensions (dimension types, biomes, generators), rocks,
Venus's acid clouds, the Aerostat Deck, the Mercury and Venus Crew Capsules, the Venus Probe, the acid damage type and
the guide paragraphs (appended to the Space chapter, after the materials of fiction).

Java: com.periodicelements.space.Mercury (dawn line, heat and cold), MercuryChunkGenerator, Venus (clouds, deck, probes,
acid), VenusChunkGenerator, Moon (travel), Rocket (delta-v), client/InnerSky. The research projects are rows of
data/research.csv, the Air Liquefier's Venus fractions rows of reactions.csv. space.py calls generate_inner and merges
LANG, the guide paragraphs and translations(code).
"""
import json
import random

from PIL import Image

from inner_i18n import TR, GUIDE_I18N

ROCKS = ["mercurian_regolith", "mercurian_crust", "mercurian_hollows", "venusian_basalt", "metallic_snow"]
CLOUD = "venus_cloud"
BLOCKS = ["aerostat_deck"]
ITEMS = ["mercury_capsule", "venus_capsule", "venus_probe"]
ITEM_UNLOCKS = set(ITEMS) | set(BLOCKS)

M = "periodic_elements"
LANG = {
    f"dimension.{M}.mercury": "Mercury",
    f"dimension.{M}.venus": "Venus",
    f"block.{M}.mercurian_regolith": "Mercurian Regolith",
    f"block.{M}.mercurian_regolith.desc": "Dark dust of a crust blackened by graphite, baked and frozen by turns",
    f"block.{M}.mercurian_crust": "Mercurian Crust",
    f"block.{M}.mercurian_crust.desc": "Low-iron volcanic rock rich in magnesium and sulfur, darkened by graphite",
    f"block.{M}.mercurian_hollows": "Mercurian Hollows",
    f"block.{M}.mercurian_hollows.desc": "Bright, crumbly ground where sulfur boiled off in the heat (MESSENGER, 2011)",
    f"block.{M}.venusian_basalt": "Venusian Basalt",
    f"block.{M}.venusian_basalt.desc": "Lava rock of the plains of Venus, sent up by a probe from ground at 465 °C",
    f"block.{M}.metallic_snow": "Metallic Snow",
    f"block.{M}.metallic_snow.desc": "Galena and bismuth sulfide frosted on Venus's high peaks, where it is a little cooler",
    f"block.{M}.venus_cloud": "Acid Cloud",
    f"block.{M}.venus_cloud.desc": "Droplets of sulfuric acid: it holds nothing up, and burns all but Stability Alloy",
    f"block.{M}.aerostat_deck": "Aerostat Deck",
    f"block.{M}.aerostat_deck.desc": "Aramid over a gasbag of breathable air, which floats in Venus's heavy carbon dioxide",
    f"item.{M}.mercury_capsule": "Mercury Crew Capsule",
    f"item.{M}.mercury_capsule.tip": "Load it into a Payload Fairing: it lands you in Mercury's dawn band. Wear the Thermal Suit",
    f"item.{M}.venus_capsule": "Venus Crew Capsule",
    f"item.{M}.venus_capsule.tip": "Load it into a Payload Fairing: its balloon opens into a deck 50 km over Venus",
    f"item.{M}.venus_probe": "Venus Probe",
    f"item.{M}.venus_probe.tip": "Use it on Venus: it falls to the ground and sends rock and metallic snow back up",
    f"message.{M}.mercury.landed": "You are on Mercury, in the dawn band. The dawn crawls west: keep up with it",
    f"message.{M}.mercury.temperature": "%s °C, %s",
    f"message.{M}.mercury.east": "%s blocks into the day",
    f"message.{M}.mercury.west": "%s blocks into the night",
    f"message.{M}.venus.landed": "You are 50 km over Venus, on a deck held up by a gasbag of air. Do not fall",
    f"message.{M}.venus.acid": "Acid clouds: they burn skin and armour, but not the Thermal Suit",
    f"message.{M}.venus.probe_only": "Probes are dropped over Venus",
    f"message.{M}.venus.probe_dropped": "The probe falls towards the ground; its sample comes back up on a balloon in %s seconds",
    f"message.{M}.venus.probe_back": "A probe's balloon brought back basalt and metallic snow from the ground of Venus",
    f"death.attack.{M}.acid": "%1$s dissolved in the acid clouds of Venus",
    f"death.attack.{M}.acid.player": "%1$s dissolved in the acid clouds of Venus fleeing %2$s",
}

GUIDE = [
    "@10 # Mercury",
    "@10 Fiction is in the getting there: with Mercury Landing, a Mercury Crew Capsule needs 22 km/s, which only "
    "metallic hydrogen, antimatter or fusion engines give (two metallic hydrogen stages of 6 tanks, or a chemical stage "
    "under a Fusion Drive). Home again takes 9 km/s. Gravity is 0.38 of the Earth's, there is no air, and the Sun's wind "
    "adds half again to the cosmic dose.",
    "@10 A day on Mercury lasts 176 Earth days, so the line between night and day crawls across the ground, one block "
    "west every minute. East of it the Sun climbs and the rock bakes up to 430 °C; west of it the night falls to "
    "-180 °C. The capsule lands you just past the dawn, where it is mild: the action bar shows the temperature and how "
    "far you are into the day or the night.",
    "@10 Past 150 °C or below -120 °C only the full Thermal Suit keeps you alive; a roof keeps off the Sun but not the "
    "hot ground. People live in the dawn band and move west with it, as the dawn moves. A solar panel gets nothing in "
    "the night and up to 6.7 times the Earth's light in the day.",
    "@10 MESSENGER (2011 to 2015) found a crust dark with graphite, rich in sulfur and magnesium and poor in iron, bright "
    "hollows where sulfur boils off in the heat, and ice in the shade of the polar craters, as on the Moon.",
    "@10 # Venus",
    "@10 The ground of Venus is at 465 °C under 92 atmospheres. But 50 km up the air is at 1 atm and 0 to 50 °C: NASA's "
    "HAVOC study (2015) drew crews living in airships there, and the Soviet Vega balloons flew there in 1985. Air we "
    "breathe is lighter than Venus's carbon dioxide, so a bubble of it floats: the base is the balloon.",
    "@10 Fiction is again in the getting there: after Venus Aerostat, a Venus Crew Capsule needs 18 km/s, and opens "
    "into an Aerostat Deck 50 km up; home takes 10 km/s. Gravity is 0.9 of the Earth's. The air is carbon dioxide, so "
    "you need the Space Suit and Life Support to breathe, and nothing burns; but so much air above you stops all the "
    "cosmic rays, and solar panels get 1.9 times the Earth's light over the clouds.",
    "@10 Below the deck lie clouds of sulfuric acid. They hold nothing up: fall into them and the acid burns you and "
    "eats your armour, unless you wear the Thermal Suit; fall through them and nothing comes back. Build out from the "
    "deck with more Aerostat Deck. On Venus the Air Liquefier draws carbon dioxide, a little nitrogen, sulfur dioxide "
    "and acid from the clouds.",
    "@10 Nobody goes down. Use a Venus Probe on the deck: it falls to the ground and, like the Venera landers (1970 to "
    "1985), lasts long enough to send a sample back up on a balloon: basalt of the plains and the metallic snow of "
    "galena and bismuth sulfide that frosts the high peaks.",
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
    rnd = random.Random(4001)
    reg = _noise(4002, (70, 68, 66), 8)
    for _ in range(16):   # darker graphite grains and paler glass
        _px(reg, rnd.randrange(16), rnd.randrange(16), (40, 40, 42) if rnd.random() < 0.6 else (112, 108, 100))
    out["mercurian_regolith"] = reg
    crust = _noise(4003, (58, 58, 60), 6)
    for _ in range(14):   # graphite flecks with a dull shine
        x, y = rnd.randrange(16), rnd.randrange(16)
        _px(crust, x, y, (30, 30, 34))
        _px(crust, x + 1, y, (84, 86, 92))
    for _ in range(6):    # pale enstatite
        _px(crust, rnd.randrange(16), rnd.randrange(16), (122, 118, 108))
    out["mercurian_crust"] = crust
    hol = _noise(4004, (176, 190, 200), 8)
    for _ in range(10):   # pits where the sulfur boiled off, and a bluish tint
        x, y = rnd.randrange(15), rnd.randrange(15)
        _rect(hol, x, y, x + 1, y + 1, (120, 130, 142))
    for _ in range(8):
        _px(hol, rnd.randrange(16), rnd.randrange(16), (214, 224, 232))
    out["mercurian_hollows"] = hol
    bas = _noise(4005, (66, 52, 44), 7)
    for _ in range(12):   # red-brown weathered surface of a world at 465 °C
        _px(bas, rnd.randrange(16), rnd.randrange(16), (112, 70, 48))
    for _ in range(8):
        _px(bas, rnd.randrange(16), rnd.randrange(16), (34, 30, 30))
    out["venusian_basalt"] = bas
    snow = _noise(4006, (160, 164, 172), 10)
    for _ in range(18):   # galena cubes and bismuth sulfide needles shining
        x, y = rnd.randrange(16), rnd.randrange(16)
        _px(snow, x, y, (226, 230, 238))
        if rnd.random() < 0.5:
            _px(snow, x, y + 1, (96, 100, 112))
    out["metallic_snow"] = snow
    deck = _noise(4007, (232, 228, 214), 4)
    for i in range(16):
        _px(deck, i, 0, (196, 186, 150))
        _px(deck, 0, i, (196, 186, 150))
        if i % 4 == 2:   # aramid weave seams
            _rect(deck, 0, i, 15, i, (212, 200, 160))
    out["aerostat_deck"] = deck
    return out


def cloud_texture():
    """Pale yellow droplets of acid, half see-through."""
    rnd = random.Random(4008)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            d = rnd.randint(-8, 8)
            _px(img, x, y, (246 + d, 238 + d, 196 + d), 115 + rnd.randint(-20, 20))
    return img


def capsule_texture(band, body=(224, 214, 196)):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(1, 14):
        half = 2 + (y - 1) * 5 // 12
        _rect(img, 8 - half, y, 7 + half, y, body)
    _rect(img, 3, 9, 12, 9, band)
    _rect(img, 1, 13, 14, 14, (110, 80, 50))   # heat shield
    _rect(img, 6, 4, 9, 6, (40, 60, 90))       # window
    return img


def probe_texture():
    """A Venera lander: a round pressure sphere on a crushable ring, its balloon folded on top."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if (x - 7.5) ** 2 + (y - 8.5) ** 2 <= 20:
                k = 1.1 - 0.05 * ((x - 5) + (y - 6)) / 2
                _px(img, x, y, [c * k for c in (180, 186, 196)])
    _rect(img, 2, 12, 13, 13, (120, 112, 100))   # the landing ring
    _rect(img, 6, 1, 9, 3, (230, 220, 160))      # the balloon, folded
    _px(img, 7, 7, (60, 80, 110))
    _px(img, 8, 7, (60, 80, 110))
    return img


P = "periodic_elements:"
RECIPES = {
    "mercury_capsule": (["ACA", "ALA", "PRP"], {"A": P + "stability_alloy_ingot", "C": P + "integrated_circuit",
                                                "L": P + "life_support", "R": P + "parachute", "P": P + "composite_plate"}, 1),
    "venus_capsule": (["TCT", "TLT", "DAD"], {"T": P + "titanium_alloy_ingot", "C": P + "integrated_circuit",
                                              "L": P + "life_support", "D": P + "aerostat_deck", "A": P + "composite_plate"}, 1),
    "aerostat_deck": (["KKK", "PNP"], {"K": P + "aramid", "P": P + "composite_plate", "N": P + "nitrogen"}, 6),
    "venus_probe": ([" K ", "TCT", " T "], {"K": P + "aramid", "T": P + "titanium_alloy_ingot", "C": P + "integrated_circuit"}, 2),
}


def _dimension_type(effects, fixed_time=None):
    d = {"ultrawarm": False, "natural": False, "coordinate_scale": 1.0, "has_skylight": True, "has_ceiling": False,
         "ambient_light": 0.0, "monster_spawn_light_level": 0, "monster_spawn_block_light_limit": 0,
         "piglin_safe": False, "bed_works": True, "respawn_anchor_works": False, "has_raids": False,
         "min_y": 0, "height": 256, "logical_height": 256, "infiniburn": "#minecraft:infiniburn_overworld",
         "effects": effects}
    if fixed_time is not None:
        d["fixed_time"] = fixed_time
    return d


def _biome(sky, fog):
    return {"has_precipitation": False, "temperature": 0.0, "downfall": 0.0,
            "effects": {"sky_color": sky, "fog_color": fog, "water_color": 4159204, "water_fog_color": 329011,
                        "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8,
                                       "offset": 2.0}},
            "spawners": {}, "spawn_costs": {}, "carvers": {}, "features": []}


def generate_inner(assets, data_root, lang, mod):
    tex_block = assets / "textures" / "block"
    tex_item = assets / "textures" / "item"
    models = assets / "models"
    states = assets / "blockstates"
    textures = block_textures()
    textures[CLOUD] = cloud_texture()
    for name, img in textures.items():
        img.save(tex_block / f"{name}.png")
    for b in ROCKS + BLOCKS + [CLOUD]:
        _dump(models / "block" / f"{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod}:block/{b}"}})
        _dump(models / "item" / f"{b}.json", {"parent": f"{mod}:block/{b}"})
        if b in ROCKS:   # natural ground turns at random, like the other rocks
            _dump(states / f"{b}.json", {"variants": {"": [{"model": f"{mod}:block/{b}", "y": y} for y in (0, 90, 180, 270)]}})
        else:
            _dump(states / f"{b}.json", {"variants": {"": {"model": f"{mod}:block/{b}"}}})
    items = {"mercury_capsule": capsule_texture((150, 100, 230), (176, 186, 204)),
             "venus_capsule": capsule_texture((230, 200, 90)), "venus_probe": probe_texture()}
    for iid, img in items.items():
        img.save(tex_item / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{iid}"}})

    data_dir = data_root / mod
    for b in ROCKS + BLOCKS:
        _dump(data_dir / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{b}"}],
             "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for rid, (pattern, key, count) in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                                    "key": {k: {"item": v} for k, v in key.items()},
                                                    "result": {"id": f"{mod}:{rid}", "count": count}})
    # Mercury: a black sky over the dawn band, always morning by the clock (the Sun's height comes from where you stand)
    _dump(data_dir / "dimension_type" / "mercury.json", _dimension_type(f"{mod}:mercury", 1000))
    _dump(data_dir / "worldgen" / "biome" / "mercury.json", _biome(0, 0))
    _dump(data_dir / "dimension" / "mercury.json", {"type": f"{mod}:mercury", "generator": {
        "type": f"{mod}:mercury", "biome_source": {"type": "minecraft:fixed", "biome": f"{mod}:mercury"}}})
    # Venus: a pale yellow sky, days and nights as the deck drifts round with the wind
    _dump(data_dir / "dimension_type" / "venus.json", _dimension_type(f"{mod}:venus"))
    _dump(data_dir / "worldgen" / "biome" / "venus.json", _biome(0xE8D9A0, 0xEDDCA6))
    _dump(data_dir / "dimension" / "venus.json", {"type": f"{mod}:venus", "generator": {
        "type": f"{mod}:venus", "biome_source": {"type": "minecraft:fixed", "biome": f"{mod}:venus"}}})
    _dump(data_dir / "damage_type" / "acid.json", {"message_id": f"{mod}.acid", "scaling": "never", "exhaustion": 0.1})
    for tool, ids in (("pickaxe", ["mercurian_crust", "venusian_basalt", "metallic_snow"]),
                      ("shovel", ["mercurian_regolith", "mercurian_hollows"])):
        tag = data_root / "minecraft" / "tags" / "block" / "mineable" / f"{tool}.json"
        values = json.loads(tag.read_text(encoding="utf-8"))["values"] if tag.exists() else []
        values += [f"{mod}:{b}" for b in ids if f"{mod}:{b}" not in values]
        _dump(tag, {"replace": False, "values": values})
    lang.update(LANG)
