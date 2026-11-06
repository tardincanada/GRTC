"""New rocks of the Geological world (stage 4a, 3.42.0): shale, gneiss, peridotite, kimberlite and laterite.

Each rock has a natural block (drops its cobbled form, like stone), a cobbled form, a polished form and polished
stairs, slab and wall, all made the vanilla way (crafting and stonecutter; cobbled smelts back to the rock).
Called by generate.py before geo_ores.py, which adds the ores in these rocks.

Writes:
  periodic_elements/geo_rocks.json   runtime table the Java side registers from (GeoRocks)
  assets: textures, block/item models, blockstates
  data: loot tables, recipes, block/item tags
"""
import json
import random

from PIL import Image

MOD = "periodic_elements"

# id -> (English name, hardness, resistance, map colour, compendium class, composition of the rock)
ROCKS = {
    "shale": ("Shale", 1.2, 5.0, "COLOR_GRAY", "sedimentary",
              "silicon_dioxide:58;aluminium_oxide:16;kaolinite:8;iron_oxide:5;potassium_oxide:3.2;"
              "magnesium_oxide:2.5;calcium_oxide:2;carbon:2;iron_disulfide:1.5;sodium_oxide:1;water:0.8"),
    "gneiss": ("Gneiss", 1.8, 6.0, "STONE_GRAY", "metamorphic",
               "silicon_dioxide:68;aluminium_oxide:15;potassium_oxide:4;sodium_oxide:3.5;iron_ii_oxide:3;"
               "calcium_oxide:2.5;magnesium_oxide:1.8;iron_oxide:1.2;titanium_dioxide:0.5;water:0.5"),
    "peridotite": ("Peridotite", 1.8, 6.0, "DARK_GREEN", "igneous",
                   "silicon_dioxide:43;magnesium_oxide:40;iron_ii_oxide:8;calcium_oxide:2.5;aluminium_oxide:2.5;"
                   "chromite:1.5;iron_oxide:1.5;water:0.7;nickel_oxide:0.3"),
    "kimberlite": ("Kimberlite", 1.5, 6.0, "TERRACOTTA_CYAN", "igneous",
                   "silicon_dioxide:33;magnesium_oxide:28;calcium_carbonate:12;iron_ii_oxide:8;water:8;"
                   "aluminium_oxide:3;ilmenite:3;potassium_oxide:2;titanium_dioxide:2;phosphorus_pentoxide:1"),
    "laterite": ("Laterite", 0.9, 3.0, "TERRACOTTA_RED", "sedimentary",
                 "iron_oxide:38;gibbsite:26;kaolinite:14;water:9;silicon_dioxide:8;titanium_dioxide:2;"
                 "manganese_dioxide:1.5;nickel_oxide:1.5"),
}

FORMS = ("", "cobbled", "polished", "stairs", "slab", "wall")


def block_id(rock, form):
    return {"": rock, "cobbled": f"cobbled_{rock}", "polished": f"polished_{rock}",
            "stairs": f"polished_{rock}_stairs", "slab": f"polished_{rock}_slab",
            "wall": f"polished_{rock}_wall"}[form]


def ids():
    return [block_id(r, f) for r in ROCKS for f in FORMS]


def english(rock, form):
    n = ROCKS[rock][0]
    return {"": n, "cobbled": f"Cobbled {n}", "polished": f"Polished {n}", "stairs": f"Polished {n} Stairs",
            "slab": f"Polished {n} Slab", "wall": f"Polished {n} Wall"}[form]


# ---- textures -------------------------------------------------------------------------------------------------

def _clamp(c):
    return tuple(max(0, min(255, int(v))) for v in c)


def _shade(c, f):
    return _clamp(v * f for v in c)


def _rock_texture(rock, seed):
    rnd = random.Random(seed)
    img = Image.new("RGB", (16, 16))
    px = img.load()
    if rock == "shale":  # thin dark laminae
        for y in range(16):
            band = 0.86 + 0.14 * ((y * 7 + 3) % 5) / 4
            for x in range(16):
                px[x, y] = _shade((86, 88, 94), band * (0.93 + 0.12 * rnd.random()))
            if y % 4 == 1:
                for x in range(16):
                    if rnd.random() < 0.8:
                        px[x, y] = _shade((54, 55, 60), 0.95 + 0.1 * rnd.random())
    elif rock == "gneiss":  # wavy light and dark bands
        for x in range(16):
            off = int(round(1.5 * __import__("math").sin(x / 2.6)))
            for y in range(16):
                b = (y + off) % 8
                base = (196, 176, 168) if b < 3 else (62, 60, 64) if b < 5 else (150, 146, 150)
                px[x, y] = _shade(base, 0.9 + 0.18 * rnd.random())
    elif rock == "peridotite":  # olive grains with black specks
        for y in range(16):
            for x in range(16):
                r = rnd.random()
                base = (98, 120, 62) if r < 0.55 else (128, 150, 76) if r < 0.85 else (70, 86, 46)
                if r > 0.95:
                    base = (28, 30, 26)
                px[x, y] = _shade(base, 0.92 + 0.12 * rnd.random())
    elif rock == "kimberlite":  # blue-green matrix with angular clasts
        for y in range(16):
            for x in range(16):
                px[x, y] = _shade((84, 110, 112), 0.9 + 0.16 * rnd.random())
        for _ in range(6):
            cx, cy, w, h = rnd.randint(0, 13), rnd.randint(0, 13), rnd.randint(2, 4), rnd.randint(2, 3)
            col = rnd.choice([(150, 148, 132), (60, 70, 66), (122, 98, 80), (170, 176, 160)])
            for y in range(cy, min(16, cy + h)):
                for x in range(cx, min(16, cx + w)):
                    px[x, y] = _shade(col, 0.92 + 0.1 * rnd.random())
    elif rock == "laterite":  # rust red with dark pores and ochre spots
        for y in range(16):
            for x in range(16):
                r = rnd.random()
                base = (158, 66, 38) if r < 0.6 else (182, 92, 46) if r < 0.85 else (120, 46, 30)
                if r > 0.96:
                    base = (66, 26, 18)
                px[x, y] = _shade(base, 0.92 + 0.12 * rnd.random())
    return img


def _gneiss_top(seed=31, light_share=0.45, dots=8,
                light=(196, 176, 168), mid=(150, 146, 150), dark=(62, 60, 64), black=(30, 28, 32)):
    """Top and bottom of gneiss (Guilherme 2026-11-05, proposal B): no bands, granular like diorite, in the colours of
    the bands, with small scattered black dots. Tiles seamlessly (the grain noise wraps around)."""
    rnd = random.Random(seed)
    v = [[0.0] * 16 for _ in range(16)]
    for _ in range(40):
        cx, cy, r, a = rnd.random() * 16, rnd.random() * 16, 0.9 + rnd.random() * 1.1, rnd.choice((-1, 1))
        for y in range(16):
            for x in range(16):
                dx = min(abs(x - cx), 16 - abs(x - cx))
                dy = min(abs(y - cy), 16 - abs(y - cy))
                d = (dx * dx + dy * dy) / (r * r)
                if d < 1:
                    v[y][x] += a * (1 - d)
    flat = sorted(v[y][x] + rnd.random() * 0.35 for y in range(16) for x in range(16))
    cut1 = flat[int(256 * (1 - light_share))]
    cut0 = flat[int(256 * (1 - light_share) * 0.35)]
    tones_l = [_shade(light, 1.08), light, _shade(light, 0.93)]
    tones_m = [_shade(mid, 1.06), mid, _shade(mid, 0.9)]
    img = Image.new("RGB", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            n = v[y][x] + rnd.random() * 0.35
            px[x, y] = rnd.choice(tones_l) if n >= cut1 else rnd.choice(tones_m) if n >= cut0 else _shade(mid, 0.78)
    placed = []
    while len(placed) < dots:
        x, y = rnd.randrange(16), rnd.randrange(16)
        if all(min(abs(x - a), 16 - abs(x - a)) > 2 or min(abs(y - b), 16 - abs(y - b)) > 2 for a, b in placed):
            placed.append((x, y))
            px[x, y] = black
            if rnd.random() < 0.3:
                px[(x + 1) % 16, y] = _shade(dark, 0.9)
    return img


def _gneiss_cobbled(seed=5, n=10, light=(196, 176, 168), mid=(150, 146, 150), dark=(62, 60, 64), joint=0.75):
    """Cobbled gneiss (Guilherme 2026-11-05, proposal B "bandas soltas"): irregular cobbles like the basalt top, each a
    piece of banded gneiss with its bands at its own angle, so the block has no main direction. Tiles seamlessly."""
    import math
    rnd = random.Random(seed)
    pts = [(rnd.random() * 16, rnd.random() * 16) for _ in range(n)]
    [rnd.choice((light, light, light, mid, mid, dark)) for _ in pts]  # keeps the random sequence of the proposal
    ang = [rnd.random() * math.pi for _ in pts]

    def near(x, y):
        ds = sorted((min(abs(x - a), 16 - abs(x - a)) ** 2 + min(abs(y - b), 16 - abs(y - b)) ** 2, i)
                    for i, (a, b) in enumerate(pts))
        return ds[0][1], math.sqrt(ds[1][0]) - math.sqrt(ds[0][0])

    img = Image.new("RGB", (16, 16))
    px = img.load()
    edge = {}
    for y in range(16):
        for x in range(16):
            i, e = near(x + 0.5, y + 0.5)
            edge[x, y] = e
            t = (x - pts[i][0]) * math.cos(ang[i]) + (y - pts[i][1]) * math.sin(ang[i])
            px[x, y] = _shade((light, dark, mid)[int(math.floor(t / 1.6)) % 3], 0.92 + 0.14 * rnd.random())
    for y in range(16):
        for x in range(16):
            if edge[x, y] < 0.32:
                px[x, y] = _shade(dark, joint)
    for y in range(16):
        for x in range(16):
            if edge[x, y] >= 0.32:
                if edge[(x + 1) % 16, y] < 0.32 or edge[x, (y + 1) % 16] < 0.32:
                    px[x, y] = _shade(px[x, y], 0.82)
                elif edge[(x - 1) % 16, y] < 0.32 or edge[x, (y - 1) % 16] < 0.32:
                    px[x, y] = _shade(px[x, y], 1.1)
    return img


# Rocks whose natural block has its own top and bottom (cube_column: "end" on top and bottom, the rock on the sides).
SHALE = dict(light=(100, 102, 109), mid=(84, 86, 92), dark=(54, 55, 60))
TOPS = {"gneiss": _gneiss_top,
        # shale (Guilherme 2026-11-05, "o mesmo"): grains in the laminae colours, a few black dots
        "shale": lambda: _gneiss_top(seed=47, light_share=0.5, dots=7, black=(28, 28, 32), **SHALE)}
COBBLES = {"gneiss": _gneiss_cobbled, "shale": lambda: _gneiss_cobbled(seed=9, joint=0.5, **SHALE)}


def _cobbled(base, seed):
    """Rounded cobbles of the rock's colours with dark joints, the layout of vanilla cobblestone."""
    rnd = random.Random(seed)
    img = base.copy()
    px = img.load()
    joints = {(x, y) for x in range(16) for y in range(16)
              if (y in (4, 9, 13) and rnd.random() < 0.85)
              or (x in ((3, 10)[y // 5 % 2], (7, 14)[y // 5 % 2]) and rnd.random() < 0.9)}
    for x, y in joints:
        px[x, y] = _shade(px[x, y], 0.45)
    for y in range(16):
        for x in range(16):
            if (x, y) not in joints and ((x + 1, y) in joints or (x, y + 1) in joints):
                px[x, y] = _shade(px[x, y], 0.78)
            elif (x, y) not in joints and ((x - 1, y) in joints or (x, y - 1) in joints):
                px[x, y] = _shade(px[x, y], 1.15)
    return img


def _polished(base):
    """Smoothed rock: its colours blurred and lightened, with a bevelled frame like polished granite."""
    src = base.load()
    img = Image.new("RGB", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            acc = [0, 0, 0]
            for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                c = src[(x + dx) % 16, (y + dy) % 16]
                for i in range(3):
                    acc[i] += c[i]
            px[x, y] = _shade([a / 5 for a in acc], 1.08)
    for i in range(16):
        px[i, 0] = _shade(px[i, 0], 1.18)
        px[0, i] = _shade(px[0, i], 1.18)
        px[i, 15] = _shade(px[i, 15], 0.72)
        px[15, i] = _shade(px[15, i], 0.72)
    return img


# ---- assets ---------------------------------------------------------------------------------------------------

def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _stairs_states(m):
    out = {}
    rot = {"east": 0, "south": 90, "west": 180, "north": 270}
    for facing, y in rot.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                model = m if shape == "straight" else m + ("_inner" if shape.startswith("inner") else "_outer")
                ry = y
                if shape.endswith("left"):
                    ry = (y + 270) % 360
                if half == "top":
                    if shape.endswith("left"):
                        ry = (ry + 90) % 360
                    elif shape.endswith("right"):
                        ry = (ry + 90) % 360
                v = {"model": model}
                if half == "top":
                    v["x"] = 180
                if ry:
                    v["y"] = ry
                if half == "top" or ry:
                    v["uvlock"] = True
                out[f"facing={facing},half={half},shape={shape}"] = v
    return {"variants": out}


def _wall_states(m):
    parts = [{"when": {"up": "true"}, "apply": {"model": m + "_post"}}]
    for side, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for height, suffix in (("low", "_side"), ("tall", "_side_tall")):
            a = {"model": m + suffix, "uvlock": True}
            if y:
                a["y"] = y
            parts.append({"when": {side: height}, "apply": a})
    return {"multipart": parts}


def _loot_self(bid):
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0, "entries": [
        {"type": "minecraft:item", "name": f"{MOD}:{bid}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}],
            "random_sequence": f"{MOD}:blocks/{bid}"}


def _loot_rock(rock):
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0, "entries": [
        {"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{MOD}:{rock}", "conditions": [{
                "condition": "minecraft:match_tool",
                "predicate": {"predicates": {"minecraft:enchantments": [
                    {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
            {"type": "minecraft:item", "name": f"{MOD}:cobbled_{rock}",
             "conditions": [{"condition": "minecraft:survives_explosion"}]}]}]}],
            "random_sequence": f"{MOD}:blocks/{rock}"}


def _loot_slab(bid):
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0, "entries": [
        {"type": "minecraft:item", "name": f"{MOD}:{bid}", "functions": [
            {"function": "minecraft:set_count", "add": False, "count": 2.0, "conditions": [{
                "condition": "minecraft:block_state_property", "block": f"{MOD}:{bid}",
                "properties": {"type": "double"}}]},
            {"function": "minecraft:explosion_decay"}]}]}], "random_sequence": f"{MOD}:blocks/{bid}"}


def _shaped(pattern, key, result, count):
    return {"type": "minecraft:crafting_shaped", "category": "building", "pattern": pattern,
            "key": {k: {"item": v} for k, v in key.items()}, "result": {"id": result, "count": count}}


def _cut(src, result, count=1):
    return {"type": "minecraft:stonecutting", "ingredient": {"item": src}, "result": {"id": result, "count": count}}


def _add_tag(path, values):
    tag = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {"replace": False, "values": []}
    tag["values"] += [v for v in values if v not in tag["values"]]
    _dump(path, tag)


PICKAXE = []


def generate_geo_rocks(assets, data_dir, runtime_dir, lang):
    tex = assets / "textures" / "block"
    tex.mkdir(parents=True, exist_ok=True)
    models, states, items = assets / "models" / "block", assets / "blockstates", assets / "models" / "item"
    loot, recipes = data_dir / "loot_table" / "blocks", data_dir / "recipe"
    table = []
    for i, (rock, (name, hard, resist, color, _cls, _comp)) in enumerate(ROCKS.items()):
        base = _rock_texture(rock, 4200 + i)
        base.save(tex / f"{rock}.png")
        (COBBLES[rock]() if rock in COBBLES else _cobbled(base, 4300 + i)).save(tex / f"cobbled_{rock}.png")
        _polished(base).save(tex / f"polished_{rock}.png")
        if rock in TOPS:
            TOPS[rock]().save(tex / f"{rock}_top.png")
        for form in FORMS:
            bid = block_id(rock, form)
            lang[f"block.{MOD}.{bid}"] = english(rock, form)
            full = f"{MOD}:{bid}"
            t = f"{MOD}:block/polished_{rock}"
            if form in ("", "cobbled", "polished"):
                if form == "" and rock in TOPS:
                    _dump(models / f"{bid}.json", {"parent": "minecraft:block/cube_column",
                                                    "textures": {"end": f"{MOD}:block/{bid}_top",
                                                                 "side": f"{MOD}:block/{bid}"}})
                else:
                    _dump(models / f"{bid}.json", {"parent": "minecraft:block/cube_all",
                                                    "textures": {"all": f"{MOD}:block/{bid}"}})
                _dump(states / f"{bid}.json", {"variants": {"": {"model": f"{MOD}:block/{bid}"}}})
                _dump(items / f"{bid}.json", {"parent": f"{MOD}:block/{bid}"})
            elif form == "stairs":
                for suffix, parent in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
                    _dump(models / f"{bid}{suffix}.json", {"parent": f"minecraft:block/{parent}",
                                                           "textures": {"bottom": t, "top": t, "side": t}})
                _dump(states / f"{bid}.json", _stairs_states(f"{MOD}:block/{bid}"))
                _dump(items / f"{bid}.json", {"parent": f"{MOD}:block/{bid}"})
            elif form == "slab":
                _dump(models / f"{bid}.json", {"parent": "minecraft:block/slab",
                                                "textures": {"bottom": t, "top": t, "side": t}})
                _dump(models / f"{bid}_top.json", {"parent": "minecraft:block/slab_top",
                                                    "textures": {"bottom": t, "top": t, "side": t}})
                _dump(states / f"{bid}.json", {"variants": {
                    "type=bottom": {"model": f"{MOD}:block/{bid}"}, "type=top": {"model": f"{MOD}:block/{bid}_top"},
                    "type=double": {"model": f"{MOD}:block/polished_{rock}"}}})
                _dump(items / f"{bid}.json", {"parent": f"{MOD}:block/{bid}"})
            else:
                for suffix, parent in (("_post", "template_wall_post"), ("_side", "template_wall_side"),
                                       ("_side_tall", "template_wall_side_tall"), ("_inventory", "wall_inventory")):
                    _dump(models / f"{bid}{suffix}.json", {"parent": f"minecraft:block/{parent}",
                                                           "textures": {"wall": t}})
                _dump(states / f"{bid}.json", _wall_states(f"{MOD}:block/{bid}"))
                _dump(items / f"{bid}.json", {"parent": f"{MOD}:block/{bid}_inventory"})
            _dump(loot / f"{bid}.json", _loot_rock(rock) if form == "" else _loot_slab(bid) if form == "slab"
                  else _loot_self(bid))
            PICKAXE.append(bid)
            table.append({"id": bid, "form": form or "rock", "base": f"{MOD}:polished_{rock}",
                          "hardness": hard, "resistance": resist, "color": color})
        r, c, p = f"{MOD}:{rock}", f"{MOD}:cobbled_{rock}", f"{MOD}:polished_{rock}"
        st, sl, wa = f"{MOD}:polished_{rock}_stairs", f"{MOD}:polished_{rock}_slab", f"{MOD}:polished_{rock}_wall"
        _dump(recipes / f"{rock}_from_smelting.json", {
            "type": "minecraft:smelting", "category": "blocks", "ingredient": {"item": c}, "result": {"id": r},
            "experience": 0.1, "cookingtime": 200})
        _dump(recipes / f"polished_{rock}.json", _shaped(["##", "##"], {"#": r}, p, 4))
        _dump(recipes / f"polished_{rock}_stairs.json", _shaped(["#  ", "## ", "###"], {"#": p}, st, 4))
        _dump(recipes / f"polished_{rock}_slab.json", _shaped(["###"], {"#": p}, sl, 6))
        _dump(recipes / f"polished_{rock}_wall.json", _shaped(["###", "###"], {"#": p}, wa, 6))
        for src, tag in ((r, rock), (p, f"polished_{rock}")):
            if src == r:
                _dump(recipes / f"polished_{rock}_from_{rock}_stonecutting.json", _cut(r, p))
            _dump(recipes / f"polished_{rock}_stairs_from_{tag}_stonecutting.json", _cut(src, st))
            _dump(recipes / f"polished_{rock}_slab_from_{tag}_stonecutting.json", _cut(src, sl, 2))
            _dump(recipes / f"polished_{rock}_wall_from_{tag}_stonecutting.json", _cut(src, wa))
    mc = data_dir.parent / "minecraft" / "tags"
    for kind in ("block", "item"):
        _add_tag(mc / kind / "stairs.json", [f"{MOD}:polished_{r}_stairs" for r in ROCKS])
        _add_tag(mc / kind / "slabs.json", [f"{MOD}:polished_{r}_slab" for r in ROCKS])
        _add_tag(mc / kind / "walls.json", [f"{MOD}:polished_{r}_wall" for r in ROCKS])
    _add_tag(mc / "item" / "stone_crafting_materials.json", [f"{MOD}:cobbled_{r}" for r in ROCKS])
    _add_tag(mc / "item" / "stone_tool_materials.json", [f"{MOD}:cobbled_{r}" for r in ROCKS])
    _add_tag(mc / "block" / "base_stone_overworld.json", [f"{MOD}:{r}" for r in ROCKS])
    _dump(runtime_dir / "geo_rocks.json", {"blocks": table})
    return [b["id"] for b in table]


# ---- soils (stage 4c) ------------------------------------------------------------------------------------------

# id -> (English name, base colours, composition). Dug with a shovel like dirt; a hoe turns them into farmland.
SOILS = {
    "terra_roxa": ("Terra Roxa", ((112, 40, 30), (138, 56, 38), (84, 30, 24)),
                   "kaolinite:30;silicon_dioxide:25;iron_oxide:18;water:12;gibbsite:8;titanium_dioxide:3;carbon:2;"
                   "magnesium_oxide:2"),
    "chalky_soil": ("Chalky Soil", ((176, 164, 140), (200, 190, 168), (140, 128, 108)),
                    "calcium_carbonate:45;silicon_dioxide:22;kaolinite:15;water:10;carbon:4;iron_oxide:2;"
                    "magnesium_oxide:2"),
}
SOIL_TR = {
    "pt_br": ("Terra Roxa", "Solo Calcário"), "es_es": ("Terra roxa", "Suelo calcáreo"),
    "fr_fr": ("Terra roxa", "Sol calcaire"), "it_it": ("Terra roxa", "Suolo calcareo"),
    "de_de": ("Terra Roxa", "Kalkboden"), "nl_nl": ("Terra roxa", "Kalkbodem"), "sv_se": ("Terra roxa", "Kalkjord"),
    "da_dk": ("Terra roxa", "Kalkjord"), "ru_ru": ("Терра-роша", "Карбонатная почва"),
}


def _soil_texture(colors, seed):
    rnd = random.Random(seed)
    main, light, dark = colors
    img = Image.new("RGB", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            base = main if r < 0.6 else light if r < 0.82 else dark
            px[x, y] = _shade(base, 0.9 + 0.16 * rnd.random())
    for _ in range(5):  # a few pebbles
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        px[x, y] = _shade(light, 1.2)
    return img


def generate_soils(assets, data_dir, runtime_dir, lang):
    tex = assets / "textures" / "block"
    out = []
    for i, (sid, (name, colors, _comp)) in enumerate(SOILS.items()):
        _soil_texture(colors, 4400 + i).save(tex / f"{sid}.png")
        lang[f"block.{MOD}.{sid}"] = name
        _dump(assets / "models" / "block" / f"{sid}.json", {"parent": "minecraft:block/cube_all",
                                                             "textures": {"all": f"{MOD}:block/{sid}"}})
        _dump(assets / "blockstates" / f"{sid}.json", {"variants": {"": {"model": f"{MOD}:block/{sid}"}}})
        _dump(assets / "models" / "item" / f"{sid}.json", {"parent": f"{MOD}:block/{sid}"})
        _dump(data_dir / "loot_table" / "blocks" / f"{sid}.json", _loot_self(sid))
        __import__("geo_ores").SHOVEL.append(f"{MOD}:{sid}")
        out.append(sid)
    _add_tag(data_dir.parent / "minecraft" / "tags" / "block" / "dirt.json", [f"{MOD}:{s}" for s in SOILS])
    _add_tag(data_dir.parent / "minecraft" / "tags" / "item" / "dirt.json", [f"{MOD}:{s}" for s in SOILS])
    table = json.loads((runtime_dir / "geo_rocks.json").read_text(encoding="utf-8"))
    table["soils"] = out
    _dump(runtime_dir / "geo_rocks.json", table)
    return out



def composition_rows():
    rows = []
    for rock, (_n, _h, _r, _c, _cls, comp) in ROCKS.items():
        rows.append((f"{MOD}:{rock}", 1000.0, comp))
        rows.append((f"{MOD}:cobbled_{rock}", 1000.0, f"block:{MOD}:{rock}:100"))
        rows.append((f"{MOD}:polished_{rock}", 1000.0, f"block:{MOD}:{rock}:100"))
        rows.append((f"{MOD}:polished_{rock}_stairs", 1000.0, f"block:{MOD}:polished_{rock}:100"))
        rows.append((f"{MOD}:polished_{rock}_slab", 500.0, f"block:{MOD}:polished_{rock}:100"))
        rows.append((f"{MOD}:polished_{rock}_wall", 1000.0, f"block:{MOD}:polished_{rock}:100"))
    for sid, (_n, _c, comp) in SOILS.items():
        rows.append((f"{MOD}:{sid}", 1000.0, comp))
    return rows


# ---- translations ---------------------------------------------------------------------------------------------

# Per language: rock -> (name, gender or genitive), and how the forms are built.
_NAMES = {
    "pt_br": {"shale": ("Folhelho", "m"), "gneiss": ("Gnaisse", "m"), "peridotite": ("Peridotito", "m"),
              "kimberlite": ("Kimberlito", "m"), "laterite": ("Laterita", "f")},
    "es_es": {"shale": ("Lutita", "f"), "gneiss": ("Gneis", "m"), "peridotite": ("Peridotita", "f"),
              "kimberlite": ("Kimberlita", "f"), "laterite": ("Laterita", "f")},
    "fr_fr": {"shale": ("Schiste argileux", "m"), "gneiss": ("Gneiss", "m"), "peridotite": ("Péridotite", "f"),
              "kimberlite": ("Kimberlite", "f"), "laterite": ("Latérite", "f")},
    "it_it": {"shale": ("Argillite", "f"), "gneiss": ("Gneiss", "m"), "peridotite": ("Peridotite", "f"),
              "kimberlite": ("Kimberlite", "f"), "laterite": ("Laterite", "f")},
    "de_de": {"shale": ("Schieferton", ""), "gneiss": ("Gneis", ""), "peridotite": ("Peridotit", ""),
              "kimberlite": ("Kimberlit", ""), "laterite": ("Laterit", "")},
    "nl_nl": {"shale": ("Schalie", ""), "gneiss": ("Gneis", ""), "peridotite": ("Peridotiet", ""),
              "kimberlite": ("Kimberliet", ""), "laterite": ("Lateriet", "")},
    "sv_se": {"shale": ("Lerskiffer", ""), "gneiss": ("Gnejs", ""), "peridotite": ("Peridotit", ""),
              "kimberlite": ("Kimberlit", ""), "laterite": ("Laterit", "")},
    "da_dk": {"shale": ("Lerskifer", ""), "gneiss": ("Gnejs", ""), "peridotite": ("Peridotit", ""),
              "kimberlite": ("Kimberlit", ""), "laterite": ("Laterit", "")},
    "ru_ru": {"shale": ("Глинистый сланец", "глинистого сланца"), "gneiss": ("Гнейс", "гнейса"),
              "peridotite": ("Перидотит", "перидотита"), "kimberlite": ("Кимберлит", "кимберлита"),
              "laterite": ("Латерит", "латерита")},
}


def _forms(code, rock):
    n, g = _NAMES[code][rock]
    lo = n[0].lower() + n[1:]
    if code == "pt_br":
        p = f"{n} {'Polida' if g == 'f' else 'Polido'}"
        return n, f"Pedregulho de {n}", p, f"Escadas de {p}", f"Laje de {p}", f"Muro de {p}"
    if code == "es_es":
        p = f"{n} {'pulida' if g == 'f' else 'pulido'}"
        pl = p[0].lower() + p[1:]
        return n, f"{n} {'rocosa' if g == 'f' else 'rocoso'}", p, f"Escaleras de {pl}", f"Losa de {pl}", f"Muro de {pl}"
    if code == "fr_fr":
        p = f"{n} {'polie' if g == 'f' else 'poli'}"
        pl = p[0].lower() + p[1:]
        return n, f"{n} {'concassée' if g == 'f' else 'concassé'}", p, f"Escalier en {pl}", f"Dalle en {pl}", \
            f"Muret en {pl}"
    if code == "it_it":
        p = f"{n} {'levigata' if g == 'f' else 'levigato'}"
        pl = p[0].lower() + p[1:]
        return n, f"{n} {'grezza' if g == 'f' else 'grezzo'}", p, f"Scalini di {pl}", f"Lastra di {pl}", \
            f"Muretto di {pl}"
    if code == "de_de":
        return n, f"{n}bruchstein", f"Polierter {n}", f"Polierte {n}treppe", f"Polierte {n}stufe", \
            f"Polierte {n}mauer"
    if code == "nl_nl":
        return n, f"Gebroken {lo}", f"Gepolijst {lo}", f"Gepolijste {lo}trap", f"Gepolijste {lo}plaat", \
            f"Gepolijste {lo}muur"
    if code == "sv_se":
        return n, f"Krossad {lo}", f"Polerad {lo}", f"Trappa av polerad {lo}", f"Platta av polerad {lo}", \
            f"Mur av polerad {lo}"
    if code == "da_dk":
        return n, f"Knust {lo}", f"Poleret {lo}", f"Trappe af poleret {lo}", f"Plade af poleret {lo}", \
            f"Mur af poleret {lo}"
    if code == "ru_ru":
        return n, f"Колотый {lo}", f"Полированный {lo}", f"Ступеньки из полированного {g}", \
            f"Плита из полированного {g}", f"Стена из полированного {g}"
    raise KeyError(code)


def rock_name(rock, code):
    return _NAMES[code][rock][0]


def translations(code):
    out = {}
    for rock in ROCKS:
        for form, text in zip(FORMS, _forms(code, rock)):
            out[english(rock, form)] = text
    for (sid, (name, _c, _comp)), text in zip(SOILS.items(), SOIL_TR[code]):
        out[name] = text
    return out
