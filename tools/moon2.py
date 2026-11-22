"""Level 9, the Moon completed (3.65.0): nothing burns without oxygen, cosmic rays, lava tubes, KREEP basalt and the
Field Atlas on the Moon. Strings, guide paragraphs and the KREEP basalt block's resources."""

import json
import random

from PIL import Image

from moon2_i18n import TR, GUIDE_I18N

ROCKS = ["kreep_basalt"]
M = "periodic_elements"

LANG = {
    f"block.{M}.kreep_basalt": "KREEP Basalt",
    f"block.{M}.kreep_basalt.desc": "Mare basalt rich in potassium, rare earths, phosphorus and thorium: the Moon's own radioactive rock",
    f"message.{M}.vacuum.no_fire": "Nothing burns without oxygen",
}

GUIDE = [
    "@9 # Fire, radiation and shelter",
    "@9 Nothing burns without oxygen: on the Moon fire will not light, torches, candles and campfires cannot be placed, "
    "and furnaces, fuel-burning machines, steam engines and diesel generators go out. Inside a sealed room kept full of "
    "air by Life Support they work as on the Earth. Wind turbines stand still: there is no wind.",
    "@9 With no air and no magnetic field, cosmic rays reach the ground: under an open sky the dose rate is 0.1 Sv/h (the "
    "game raises it about 1700 times over the real one, so that it counts). Every opaque full block above your head "
    "halves it; glass does not count. A base under three or four blocks of regolith is safe, and so are the lava tubes.",
    "@9 # Lava tubes and KREEP",
    "@9 Under the big maria run lava tubes: winding tunnels about 8 blocks wide, a dozen blocks under the ground, left "
    "when the lava of old flows drained away. Here and there the roof has fallen in and a skylight pit opens to the "
    "surface. Safe from cosmic rays, they make good homes for a base.",
    "@9 In some maria the basalt is KREEP basalt, rich in potassium (K), rare earths (REE) and phosphorus (P), and in "
    "thorium: the Moon's own radioactive rock, which Lunar Prospector mapped from orbit by its gamma rays in 1998. "
    "Crushed, it gives monazite and thorium dioxide.",
    "@9 The Field Atlas draws the Moon too, on a map of its own; going back to the Earth brings the Earth's map back.",
]

for _code, _paras in GUIDE_I18N.items():
    assert len(_paras) == len(GUIDE), (_code, len(_paras), len(GUIDE))


def translations(code):
    return dict(TR.get(code, {}))


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def kreep_texture():
    rnd = random.Random(1201)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = rnd.randint(-7, 7)
            px[x, y] = (74 + v, 70 + v, 66 + v, 255)   # basalt, a touch browner than mare basalt
    for _ in range(9):   # yellow-brown phosphate (merrillite, apatite) and monazite grains
        px[rnd.randrange(16), rnd.randrange(16)] = (176, 140, 70, 255)
    for _ in range(6):   # ilmenite
        px[rnd.randrange(16), rnd.randrange(16)] = (32, 32, 36, 255)
    for _ in range(4):   # glints of zircon
        px[rnd.randrange(16), rnd.randrange(16)] = (210, 200, 190, 255)
    return img


def generate_moon2(assets, data_root, lang, mod):
    tex = assets / "textures" / "block"
    tex.mkdir(parents=True, exist_ok=True)
    kreep_texture().save(tex / "kreep_basalt.png")
    for b in ROCKS:
        _dump(assets / "blockstates" / f"{b}.json", {"variants": {"": [{"model": f"{mod}:block/{b}", "y": y}
                                                                       for y in (0, 90, 180, 270)]}})
        _dump(assets / "models" / "block" / f"{b}.json", {"parent": "minecraft:block/cube_all",
                                                          "textures": {"all": f"{mod}:block/{b}"}})
        _dump(assets / "models" / "item" / f"{b}.json", {"parent": f"{mod}:block/{b}"})
        _dump(data_root / mod / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{b}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    tag = data_root / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json"
    values = json.loads(tag.read_text(encoding="utf-8"))["values"] if tag.exists() else []
    values += [f"{mod}:{b}" for b in ROCKS if f"{mod}:{b}" not in values]
    _dump(tag, {"replace": False, "values": values})
    lang.update(LANG)
