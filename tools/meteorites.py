"""Level 8, stage 2 (3.62.0): meteorites and the Space Telescope.

Three meteorite blocks fall at night (Java: com.periodicelements.space.Meteorites and MeteorEntity): chondrite, iron
meteorite and carbonaceous chondrite, with compositions in data/compositions.csv, entries in the Mineral Compendium
(group "meteorites") and the Mond Reactor reactions of Meteorite Analysis in data/reactions.csv. A Space Telescope in
orbit forecasts each fall. The Meteor Caller is a creative testing item. space.py calls generate_meteorites, merges
LANG, the guide paragraphs (appended to the Space chapter) and translations(code); compendium_minerals.py and its
translations take MINERALS and GROUP from here and from meteorites_i18n.
"""
import json
import random

from PIL import Image

from meteorites_i18n import TR, GUIDE_I18N, GROUP_I18N, MINERALS_I18N

METEORITES = ["chondrite", "iron_meteorite", "carbonaceous_chondrite"]
ITEMS = ["space_telescope"]
CREATIVE = ["meteor_caller"]
ITEM_UNLOCKS = set(ITEMS)

M = "periodic_elements"
LANG = {
    f"block.{M}.chondrite": "Chondrite",
    f"block.{M}.chondrite.desc": "Stony meteorite older than the Earth, full of round chondrules and specks of nickel-iron",
    f"block.{M}.iron_meteorite": "Iron Meteorite",
    f"block.{M}.iron_meteorite.desc": "Nickel-iron from the core of a shattered asteroid; the richest source of iridium",
    f"block.{M}.carbonaceous_chondrite": "Carbonaceous Chondrite",
    f"block.{M}.carbonaceous_chondrite.desc": "Dark, crumbly meteorite holding water and organic matter from the young Solar System",
    f"item.{M}.space_telescope": "Space Telescope",
    f"item.{M}.space_telescope.tip": "Put it in orbit: each evening it tells you where a meteorite will fall that night and marks it on your Atlas",
    f"item.{M}.meteor_caller": "Meteor Caller",
    f"item.{M}.meteor_caller.tip": "Use it on a block: a meteorite falls there a few seconds later",
    f"entity.{M}.meteor": "Meteor",
    f"message.{M}.meteor.fell": "A meteorite fell %s blocks to the %s",
    f"message.{M}.meteor.fell_marked": "A meteorite fell at %s, %s: marked on your Atlas",
    f"message.{M}.meteor.forecast": "Space Telescope: a meteorite will fall tonight near %s, %s at about %s; marked on your Atlas",
    f"message.{M}.meteor.called": "A meteorite is on its way",
    f"message.{M}.dir.n": "north",
    f"message.{M}.dir.ne": "north-east",
    f"message.{M}.dir.e": "east",
    f"message.{M}.dir.se": "south-east",
    f"message.{M}.dir.s": "south",
    f"message.{M}.dir.sw": "south-west",
    f"message.{M}.dir.w": "west",
    f"message.{M}.dir.nw": "north-west",
    f"gui.{M}.atlas.meteor": "Meteorite",
}

GUIDE = [
    "@8 # Meteorites",
    "@8 Some nights a meteorite falls: a streak of fire across the sky, a bang, and a small crater with the meteorite "
    "at the bottom. Mine it with a pickaxe; an iron meteorite needs an iron pickaxe.",
    "@8 Three kinds fall, as in reality. Chondrites are most falls: grey stone with round chondrules and specks of metal. "
    "Iron meteorites are nickel-iron from the cores of broken asteroids. Carbonaceous chondrites are dark, with water and "
    "organic matter. The Mineral Compendium describes each one.",
    "@8 Meteorite Analysis lets the Mond Reactor part their nickel-iron (kamacite and taenite) into iron, nickel and "
    "cobalt; iridium and platinum stay in the residue and come out as metal. An iron meteorite is the richest source of "
    "iridium in the game: real ones hold a few parts per million, which the mod raises so that about 16 blocks make an "
    "iridium ingot.",
    "@8 Players within 256 blocks are told where it fell. If you know Radar, a fall within 512 blocks is also marked on "
    "your Atlas.",
    "@8 # Space Telescope",
    "@8 A Space Telescope in orbit (research Space Telescope) tells you each evening if a meteorite will fall that night, "
    "where and at about what time, and marks the spot on your Atlas before it lands.",
]

GROUP = ("Meteorites", "Rocks that fell from space: pieces of asteroids, older than the Earth.")

# id -> (block, about, origin, made, formula, system); the Mineral Compendium builds its entries from these.
MINERALS = {
    "chondrite": ("chondrite",
                  "The commonest meteorite: grey stone full of round chondrules, droplets of molten rock that froze in "
                  "space, with bright specks of nickel-iron. At 4.56 billion years it is older than the Earth.",
                  "Dust and droplets of the young Solar System stuck together in an asteroid that never melted.",
                  "olivine, pyroxene, nickel-iron and troilite", None, None),
    "iron_meteorite": ("iron_meteorite",
                       "Nickel-iron from the core of an asteroid. Cut, polished and etched with acid it shows the "
                       "criss-cross Widmanstätten pattern: crystals that could only grow while the metal cooled a few "
                       "degrees every million years.",
                       "The metal core of a small planet that melted early in the Solar System and was later shattered "
                       "by collisions.",
                       "kamacite and taenite (nickel-iron), with troilite and schreibersite", "(Fe,Ni)", "cubic"),
    "carbonaceous_chondrite": ("carbonaceous_chondrite",
                               "Dark, soft meteorite that holds water in its clays and organic matter, amino acids "
                               "among them (Murchison, 1969): the least changed matter of the Solar System.",
                               "Material of the cold outer asteroid belt, soaked by water inside its parent body and "
                               "never heated.",
                               "serpentine clays, magnetite, organic matter and water", None, None),
}

for _code, _paras in GUIDE_I18N.items():
    assert len(_paras) == len(GUIDE), (_code, len(_paras), len(GUIDE))
for _code, _m in MINERALS_I18N.items():
    assert set(_m) == set(MINERALS), _code


def translations(code):
    return dict(TR.get(code, {}))


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _px(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), tuple(int(max(0, min(255, v))) for v in c) + (255,))


def _base(rnd, colour, var):
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            d = rnd.randint(-var, var)
            _px(img, x, y, [c + d for c in colour])
    return img


def _crust(img, rnd, colour, chance):
    """Patches of black fusion crust, the skin melted on the way down."""
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            if rnd.random() < chance * (1.6 if edge == 0 else 1.0 if edge == 1 else 0.35):
                d = rnd.randint(-6, 6)
                _px(img, x, y, [c + d for c in colour])


def block_textures():
    out = {}
    # Chondrite: grey with round chondrules and metal flecks.
    rnd = random.Random(801)
    img = _base(rnd, (118, 112, 104), 9)
    for _ in range(9):
        cx, cy, r = rnd.randint(1, 14), rnd.randint(1, 14), rnd.choice((1, 1, 2))
        tone = rnd.choice(((150, 140, 120), (92, 88, 84), (165, 150, 118)))
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r + (1 if r == 1 else 0):
                    _px(img, x, y, tone)
    for _ in range(10):
        _px(img, rnd.randint(0, 15), rnd.randint(0, 15), (205, 205, 196))   # nickel-iron grains
    for _ in range(4):
        _px(img, rnd.randint(0, 15), rnd.randint(0, 15), (150, 92, 50))     # rust round the grains
    _crust(img, rnd, (34, 30, 28), 0.30)
    out["chondrite"] = img
    # Iron meteorite: dark metal with the Widmanstätten pattern showing through the crust.
    rnd = random.Random(802)
    img = _base(rnd, (96, 94, 92), 6)
    for k in range(-16, 32, 4):     # kamacite bands in three directions, as on an etched octahedrite
        for t in range(16):
            for (x, y) in ((t, t + k), (t, -t + k + 15), (t + k // 2, (k * 3) % 16)):
                if rnd.random() < 0.85:
                    _px(img, x, y, (150, 150, 152))
    for _ in range(14):
        _px(img, rnd.randint(0, 15), rnd.randint(0, 15), (178, 178, 182))
    for _ in range(10):
        _px(img, rnd.randint(0, 15), rnd.randint(0, 15), (122, 70, 38))    # rust
    _crust(img, rnd, (38, 34, 32), 0.24)
    out["iron_meteorite"] = img
    # Carbonaceous chondrite: near black, with white inclusions and few chondrules.
    rnd = random.Random(803)
    img = _base(rnd, (42, 40, 38), 6)
    for _ in range(6):
        cx, cy = rnd.randint(1, 14), rnd.randint(1, 14)
        for (x, y) in ((cx, cy), (cx + 1, cy), (cx, cy + 1)):
            _px(img, x, y, (205, 200, 186))    # calcium-aluminium-rich inclusions
    for _ in range(6):
        _px(img, rnd.randint(0, 15), rnd.randint(0, 15), (78, 74, 66))
    _crust(img, rnd, (18, 16, 16), 0.22)
    out["carbonaceous_chondrite"] = img
    return out


def item_texture(iid):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    if iid == "space_telescope":
        for y in range(3, 14):
            for x in range(5, 11):
                _px(img, x, y, (205, 206, 212) if x not in (5, 10) else (160, 162, 170))
        for x in range(5, 11):
            _px(img, x, 2, (40, 40, 46))          # the open aperture door
            _px(img, x, 6, (220, 190, 80))        # gold foil band
        for x0 in (0, 12):
            for y in range(6, 11):
                for x in range(x0, x0 + 4):
                    _px(img, x, y, (40, 60, 140) if (x + y) % 3 else (110, 130, 200))
        return img
    if iid == "meteor_caller":
        for k in range(9):
            _px(img, 2 + k, 2 + k, (255, 200 - 15 * k, 60))
            _px(img, 3 + k, 2 + k, (255, 150 - 10 * k, 40))
        for y in range(10, 15):
            for x in range(10, 15):
                if (x - 12) ** 2 + (y - 12) ** 2 <= 5:
                    _px(img, x, y, (70, 66, 62))
        return img
    raise KeyError(iid)


P = "periodic_elements:"
RECIPES = {
    "space_telescope": (["AGA", "PCP", "T T"], {"A": P + "aluminium", "G": "minecraft:glass", "P": P + "solar_panel",
                                                 "C": P + "integrated_circuit", "T": P + "titanium_alloy_ingot"}, 1),
}


def generate_meteorites(assets, data_root, lang, mod):
    tex_item = assets / "textures" / "item"
    tex_block = assets / "textures" / "block"
    models = assets / "models"
    states = assets / "blockstates"
    for iid in ITEMS + CREATIVE:
        item_texture(iid).save(tex_item / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{iid}"}})
    for name, img in block_textures().items():
        img.save(tex_block / f"{name}.png")
    for b in METEORITES:
        _dump(models / "block" / f"{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod}:block/{b}"}})
        # four rotations so a crater floor of them does not look tiled
        _dump(states / f"{b}.json", {"variants": {"": [{"model": f"{mod}:block/{b}", "y": y} for y in (0, 90, 180, 270)]}})
        _dump(models / "item" / f"{b}.json", {"parent": f"{mod}:block/{b}"})
    data_dir = data_root / mod
    for b in METEORITES:
        _dump(data_dir / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{b}"}],
             "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for rid, (pattern, key, count) in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                                    "key": {k: {"item": v} for k, v in key.items()},
                                                    "result": {"id": f"{mod}:{rid}", "count": count}})
    tags = data_root / "minecraft" / "tags" / "block"
    for name, ids in (("mineable/pickaxe", METEORITES), ("needs_stone_tool", ["chondrite", "carbonaceous_chondrite"]),
                      ("needs_iron_tool", ["iron_meteorite"])):
        path = tags / f"{name}.json"
        values = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
        values += [f"{mod}:{i}" for i in ids if f"{mod}:{i}" not in values]
        _dump(path, {"replace": False, "values": values})
    lang.update(LANG)
