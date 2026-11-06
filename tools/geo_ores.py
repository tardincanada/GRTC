"""Ores in each host rock of the Geological world (stage 3a 3.17.0, 3b 3.21.0). Called by generate.py after ores.py.

Every (rock, mineral) pair that a deposit can put in a rock with no ore block of its own gets one: the vanilla rock
texture with a see-through overlay of mineral spots, the same drop as the existing ore, and a composition with that
rock as the gangue. Rocks that already have an ore of the mineral (stone, deepslate, sandstone for the crusts) keep it.
Gravel ores (placers, 3b) are soft: dug with a shovel, no tool tier, and they drop the mineral directly.

Writes:
  periodic_elements/geo_ores.json           runtime table: variants, mineral -> existing ores by host, disabled veins
  assets: one overlay texture per mineral, a shared parent model, block/item models, blockstates
  data: loot tables; needs-tool and shovel tags are rewritten with the variants added
Names are "<ore name> (<rock name>)" in every language, built from the translations that already exist.
"""
import csv
import json
import random

from PIL import Image

import ores

MOD = "periodic_elements"

# Vanilla minerals: name, ores by host, spot colours, drop, (min, max) count, (min, max) xp, tier.
VANILLA = {
    "coal": ("Coal Ore", {"stone": "minecraft:coal_ore", "deepslate": "minecraft:deepslate_coal_ore"},
             ((44, 44, 44), (86, 86, 86), (18, 18, 18)), "minecraft:coal", (1, 1), (0, 2), "wood"),
    "iron": ("Iron Ore", {"stone": "minecraft:iron_ore", "deepslate": "minecraft:deepslate_iron_ore"},
             ((216, 175, 147), (236, 210, 186), (150, 108, 84)), "minecraft:raw_iron", (1, 1), (0, 0), "stone"),
    "copper": ("Copper Ore", {"stone": "minecraft:copper_ore", "deepslate": "minecraft:deepslate_copper_ore"},
               ((214, 120, 76), (240, 166, 112), (78, 160, 128)), "minecraft:raw_copper", (2, 5), (0, 0), "stone"),
    "gold": ("Gold Ore", {"stone": "minecraft:gold_ore", "deepslate": "minecraft:deepslate_gold_ore"},
             ((252, 232, 72), (255, 252, 176), (196, 140, 20)), "minecraft:raw_gold", (1, 1), (0, 0), "iron"),
    "diamond": ("Diamond Ore", {"stone": "minecraft:diamond_ore", "deepslate": "minecraft:deepslate_diamond_ore"},
                ((92, 236, 222), (210, 255, 250), (30, 150, 140)), "minecraft:diamond", (1, 1), (3, 7), "iron"),
}

# Ores of the mod that exist only in deepslate or only as crusts: the plain name their variants take.
PLAIN_NAMES = {"chromite": "Chromite Ore", "pentlandite": "Pentlandite Ore", "sulfur": "Sulfur Ore"}
PLAIN_TR = {
    "Chromite Ore": {"pt_br": "Minério de Cromita", "fr_fr": "Minerai de chromite", "es_es": "Mena de cromita",
                     "nl_nl": "Chromieterts", "it_it": "Minerale di cromite", "de_de": "Chromiterz",
                     "sv_se": "Kromitmalm", "da_dk": "Kromitmalm", "ru_ru": "Руда хромита"},
    "Pentlandite Ore": {"pt_br": "Minério de Pentlandita", "fr_fr": "Minerai de pentlandite",
                        "es_es": "Mena de pentlandita", "nl_nl": "Pentlandieterts", "it_it": "Minerale di pentlandite",
                        "de_de": "Pentlanditerz", "sv_se": "Pentlanditmalm", "da_dk": "Pentlanditmalm",
                        "ru_ru": "Руда пентландита"},
    "Sulfur Ore": {"pt_br": "Minério de Enxofre", "fr_fr": "Minerai de soufre", "es_es": "Mena de azufre",
                   "nl_nl": "Zwavelerts", "it_it": "Minerale di zolfo", "de_de": "Schwefelerz", "sv_se": "Svavelmalm",
                   "da_dk": "Svovlmalm", "ru_ru": "Серная руда"},
}

# Rocks: vanilla block, textures (side, top, bottom), the minerals deposits put in them, soft (gravel).
ROCKS = {
    "granite": ("minecraft:granite", ("granite",) * 3,
                ["iron", "copper", "gold", "molybdenite", "cassiterite", "wolframite", "spodumene", "coltan",
                 "pitchblende", "fluorite", "arsenopyrite", "stibnite"], False),
    "diorite": ("minecraft:diorite", ("diorite",) * 3,
                ["iron", "copper", "gold", "molybdenite", "arsenopyrite", "cobaltite"], False),
    "andesite": ("minecraft:andesite", ("andesite",) * 3,
                 ["iron", "copper", "gold", "molybdenite", "stibnite", "arsenopyrite", "cobaltite", "cassiterite",
                  "wolframite", "sulfur"], False),
    "tuff": ("minecraft:tuff", ("tuff",) * 3, ["coal", "iron", "copper", "gold", "sulfur", "diamond"], False),
    "basalt": ("minecraft:basalt", ("basalt_side", "basalt_top", "basalt_top"),
               ["coal", "iron", "copper", "sulfur", "chromite", "pentlandite"], False),
    "sandstone": ("minecraft:sandstone", ("sandstone", "sandstone_top", "sandstone_bottom"),
                  ["coal", "iron", "copper", "galena", "sphalerite", "barite", "fluorite", "vanadinite", "sulfur"],
                  False),
    "terracotta": ("minecraft:terracotta", ("terracotta",) * 3,
                   ["coal", "iron", "copper", "oil_shale", "saltpetre", "borax"], False),
    "brown_terracotta": ("minecraft:brown_terracotta", ("brown_terracotta",) * 3,
                         ["coal", "iron", "oil_shale", "saltpetre", "borax"], False),
    "calcite": ("minecraft:calcite", ("calcite",) * 3,
                ["iron", "galena", "sphalerite", "fluorite", "barite", "vanadinite", "sulfur"], False),
    # laterite crust (3b): stands in for the iron- and aluminium-rich red earth of the hot, wet tropics
    "orange_terracotta": ("minecraft:orange_terracotta", ("orange_terracotta",) * 3, ["bauxite", "pyrolusite"], False),
    # placers (3b): ore grains and nuggets in river gravel
    "gravel": ("minecraft:gravel", ("gravel",) * 3, ["gold", "cassiterite", "coltan", "diamond"], True),
    # stage 4a (3.42.0): the mod's own rocks (tools/geo_rocks.py)
    "shale": ("periodic_elements:shale", ("periodic_elements:shale", "periodic_elements:shale_top",
                                          "periodic_elements:shale_top"),
              ["coal", "iron", "copper", "oil_shale", "saltpetre", "borax"], False),
    "gneiss": ("periodic_elements:gneiss", ("periodic_elements:gneiss", "periodic_elements:gneiss_top",
                                            "periodic_elements:gneiss_top"),
               ["iron", "copper", "gold", "molybdenite", "cassiterite", "wolframite", "spodumene", "coltan",
                "pitchblende", "fluorite", "arsenopyrite", "stibnite", "cobaltite"], False),
    "peridotite": ("periodic_elements:peridotite", ("periodic_elements:peridotite",) * 3,
                   ["chromite", "pentlandite", "iron", "copper", "gold", "cobaltite"], False),
    "kimberlite": ("periodic_elements:kimberlite", ("periodic_elements:kimberlite",) * 3, ["diamond"], False),
    "laterite": ("periodic_elements:laterite", ("periodic_elements:laterite",) * 3, ["bauxite", "pyrolusite"], False),
}

# Rock compositions the base table lacks.
ROCK_ROWS = {
    "minecraft:terracotta": "silicon_dioxide:58;aluminium_oxide:18;iron_oxide:6;potassium_oxide:3.5;calcium_oxide:3;"
                            "magnesium_oxide:2.5;sodium_oxide:1.5;titanium_dioxide:0.8;water:6.7",
    "minecraft:brown_terracotta": "silicon_dioxide:55;aluminium_oxide:17;iron_oxide:9;carbon:2;potassium_oxide:3.5;"
                                  "calcium_oxide:3;magnesium_oxide:2.5;sodium_oxide:1.2;titanium_dioxide:0.8;water:6",
    "minecraft:orange_terracotta": "iron_oxide:30;aluminium_oxide:28;silicon_dioxide:20;water:16;titanium_dioxide:2;"
                                   "manganese_dioxide:1;kaolinite:3",
}


def _mineral_of(oid):
    for p in ("deepslate_", "nether_"):
        if oid.startswith(p):
            oid = oid[len(p):]
    for s in ("_ore", "_crust"):
        if oid.endswith(s):
            return oid[:-len(s)]
    return oid


def minerals():
    """mineral -> (name, {host: ore id}, colours, drop, count, xp, tier, desc or None, id suffix)."""
    out = {}
    for m, (name, bases, colors, drop, count, xp, tier) in VANILLA.items():
        out[m] = (name, dict(bases), colors, drop, count, xp, tier, None, f"{m}_ore")
    for oid, (name, host, colors, drop, count, xp, tier, desc) in ores.ORES.items():
        if host in ("netherrack", "sand"):
            continue
        m = _mineral_of(oid)
        suffix = oid[len("deepslate_"):] if oid.startswith("deepslate_") else oid
        if m in out:
            out[m][1][host] = f"{MOD}:{oid}"
            if host != "deepslate" and out[m][7] is None:  # prefer the plain ore's name and description
                out[m] = (name, out[m][1], colors, drop, count, xp, tier, desc, suffix)
            continue
        plain = name if host != "deepslate" else PLAIN_NAMES.get(m, name)
        out[m] = (plain, {host: f"{MOD}:{oid}"}, colors, drop, count, xp, tier, desc, suffix)
    return out


def variants():
    """(variant id, rock, mineral), in a fixed order (3a's first, so their ids never move)."""
    mins = minerals()
    out = []
    for rock, (_, _, ms, _) in ROCKS.items():
        for m in ms:
            assert m in mins, f"{rock}: unknown mineral {m}"
            out.append((f"{rock}_{mins[m][8]}", rock, m))
    return out


def overlay_texture(colors, seed):
    """Mineral spots on a clear background, drawn over the rock texture by the model. Each spot has a dark rim so
    that pale minerals still show on pale rocks (granite, diorite, calcite)."""
    main, light, dark = colors
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    spots = set()
    clumps = [(2, 2), (10, 1), (6, 7), (12, 9), (1, 11), (8, 13)]
    for cx, cy in clumps:
        cx += rnd.randint(-1, 1)
        cy += rnd.randint(-1, 1)
        cells = [(cx, cy), (cx + 1, cy), (cx, cy + 1), (cx + 1, cy + 1)]
        if rnd.random() < 0.7:
            cells.append((cx + 2, cy + rnd.randint(0, 1)))
        if rnd.random() < 0.6:
            cells.append((cx + rnd.randint(0, 1), cy + 2))
        for x, y in cells:
            if 0 <= x < 16 and 0 <= y < 16:
                px[x, y] = main + (255,)
                spots.add((x, y))
        if 0 <= cx < 16 and 0 <= cy < 16:
            px[cx, cy] = light + (255,)
        bx, by = cx + 1, cy + 1
        if 0 <= bx < 16 and 0 <= by < 16:
            px[bx, by] = dark + (255,)
    rim = tuple(int(c * 0.35) for c in dark) + (190,)
    for x, y in list(spots):
        for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in spots:
                px[nx, ny] = rim
    return img


def _tex(t):
    ns, _, name = t.rpartition(":")
    return f"{ns or 'minecraft'}:block/{name}"


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _loot(vid, drop, lo, hi):
    count = [] if lo == hi == 1 else [{"function": "minecraft:set_count", "add": False,
                                        "count": {"type": "minecraft:uniform", "min": float(lo), "max": float(hi)}}]
    return {
        "type": "minecraft:block",
        "pools": [{"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{MOD}:{vid}", "conditions": [{
                "condition": "minecraft:match_tool",
                "predicate": {"predicates": {"minecraft:enchantments": [
                    {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
            {"type": "minecraft:item", "name": drop, "functions": count + [
                {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                 "formula": "minecraft:ore_drops"},
                {"function": "minecraft:explosion_decay"}]}]}]}],
        "random_sequence": f"{MOD}:blocks/{vid}"}


def generate_geo_ores(assets, data_dir, runtime_dir, lang):
    """Writes the variant blocks; returns their ids. Call after ores.generate_ores, before machines (mining tags)."""
    mins = minerals()
    tex = assets / "textures" / "block"
    for i, m in enumerate(sorted({m for _, _, m in variants()})):
        overlay_texture(mins[m][2], 500 + i).save(tex / f"ore_overlay_{m}.png")
    face = lambda t, cull: {"texture": t, "cullface": cull}
    sides = ("north", "south", "east", "west")
    _dump(assets / "models" / "block" / "geo_ore.json", {
        "parent": "minecraft:block/block",
        "textures": {"particle": "#side"},
        "elements": [
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {
                "down": face("#bottom", "down"), "up": face("#top", "up"), **{s: face("#side", s) for s in sides}}},
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {
                d: face("#overlay", d) for d in ("down", "up") + sides}}]})
    table = []
    stone_tier, iron_tier, shovel, pick = [], [], [], []
    for vid, rock, m in variants():
        name, bases, colors, drop, (lo, hi), (xlo, xhi), tier, desc, _ = mins[m]
        side, top, bottom = ROCKS[rock][1]
        soft = ROCKS[rock][3]
        _dump(assets / "models" / "block" / f"{vid}.json", {
            "parent": f"{MOD}:block/geo_ore",
            "textures": {"side": _tex(side), "top": _tex(top), "bottom": _tex(bottom),
                         "overlay": f"{MOD}:block/ore_overlay_{m}"}})
        _dump(assets / "blockstates" / f"{vid}.json", {"variants": {"": {"model": f"{MOD}:block/{vid}"}}})
        _dump(assets / "models" / "item" / f"{vid}.json", {"parent": f"{MOD}:block/{vid}"})
        lang[f"block.{MOD}.{vid}"] = f"{name} ({rock_name(rock)})"
        if desc:
            lang[f"block.{MOD}.{vid}.desc"] = desc
        _dump(data_dir / "loot_table" / "blocks" / f"{vid}.json", _loot(vid, drop, lo, hi))
        if soft:
            shovel.append(f"{MOD}:{vid}")
        else:
            pick.append(vid)
            (stone_tier if tier == "stone" else iron_tier if tier == "iron" else []).append(f"{MOD}:{vid}")
        table.append({"id": vid, "rock": ROCKS[rock][0], "mineral": m, "base": next(iter(bases.values())),
                      "xpMin": 0 if soft else xlo, "xpMax": 0 if soft else xhi, "desc": bool(desc), "soft": soft})
    mc_tags = data_dir.parent / "minecraft" / "tags" / "block"
    for fname, extra in (("needs_stone_tool.json", stone_tier), ("needs_iron_tool.json", iron_tier)):
        path = mc_tags / fname
        tag = json.loads(path.read_text(encoding="utf-8"))
        tag["values"] += [v for v in extra if v not in tag["values"]]
        _dump(path, tag)
    hosts = {m: {f"minecraft:{h}": b for h, b in v[1].items()} for m, v in sorted(mins.items())}
    _dump(runtime_dir / "geo_ores.json", {"variants": table, "minerals": hosts,
                                         "disabledFeatures": disabled_features()})
    ores.PICKAXE.extend(pick)
    SHOVEL.extend(shovel)
    return [vid for vid, _, _ in variants()]


# Shovel-mined variants (gravel placers); machines.py merges these into mineable/shovel.
SHOVEL = []

# Vanilla veins the Geological world's deposits replace. Redstone, lapis and emerald keep their veins: they have no
# clear geological pair (the plan keeps them close to the game: redstone and lapis deep, emerald in the mountains).
VANILLA_FEATURES = ["ore_coal_upper", "ore_coal_lower", "ore_iron_upper", "ore_iron_middle", "ore_iron_small",
                    "ore_copper", "ore_copper_large", "ore_gold", "ore_gold_lower", "ore_gold_extra",
                    "ore_diamond", "ore_diamond_medium", "ore_diamond_large", "ore_diamond_buried",
                    "ore_granite_upper", "ore_granite_lower", "ore_diorite_upper", "ore_diorite_lower",
                    "ore_andesite_upper", "ore_andesite_lower", "ore_tuff", "ore_dirt", "ore_gravel"]


def disabled_features():
    """Placed features switched off in the Geological world: vanilla ones above, and every overworld mod vein whose
    ores are all minerals the deposits now place (the heavy mineral sand of beaches stays)."""
    covered = {m for _, _, m in variants()}
    out = [f"minecraft:{f}" for f in VANILLA_FEATURES]
    for fid, (targets, _size, _count, _height, biomes) in ores.FEATURES.items():
        if biomes == "nether":
            continue
        if all(_mineral_of(o) in covered for _, o in targets):
            out.append(f"{MOD}:{fid}")
    return out


ROCK_NAMES = {"granite": "Granite", "diorite": "Diorite", "andesite": "Andesite", "tuff": "Tuff", "basalt": "Basalt",
              "sandstone": "Sandstone", "terracotta": "Terracotta", "brown_terracotta": "Brown Terracotta",
              "calcite": "Calcite", "orange_terracotta": "Orange Terracotta", "gravel": "Gravel",
              "shale": "Shale", "gneiss": "Gneiss", "peridotite": "Peridotite", "kimberlite": "Kimberlite",
              "laterite": "Laterite"}


def rock_name(rock):
    return ROCK_NAMES[rock]


def composition_rows(root):
    """Extra compositions.csv rows: the missing rocks, then each variant = an existing ore of the mineral with the
    rock as gangue in place of that ore's host."""
    path = root / "data" / "compositions.csv"
    have = {}
    with path.open(encoding="utf-8") as f:
        for row in csv.reader(f):
            if row and not row[0].startswith("#") and len(row) >= 3:
                have[row[0]] = (row[1], row[2])
    rows = [(rid, 1000.0, comp) for rid, comp in ROCK_ROWS.items() if rid not in have]
    mins = minerals()
    for vid, rock, m in variants():
        for host, base in mins[m][1].items():
            if base in have and f"block:minecraft:{host}:" in have[base][1]:
                amount, comp = have[base]
                rows.append((f"{MOD}:{vid}", float(amount),
                             comp.replace(f"block:minecraft:{host}:", f"block:{ROCKS[rock][0]}:")))
                break
        else:
            raise AssertionError(f"{vid}: no composition row with a rock gangue among {mins[m][1]}")
    return rows


def translations(code):
    """English variant name -> its name in the language, from the mod's and the game's own translations."""
    from pathlib import Path
    root = Path(__file__).resolve().parent.parent / "data" / "lang"
    tr = json.loads((root / f"{code}.json").read_text(encoding="utf-8"))
    vanilla = json.loads((root / f"vanilla_{code}.json").read_text(encoding="utf-8"))
    mins = minerals()
    out = {}
    for vid, rock, m in variants():
        name = mins[m][0]
        if m in VANILLA:
            ore = vanilla.get(f"block.minecraft.{m}_ore")
        else:
            ore = tr.get(name) or PLAIN_TR.get(name, {}).get(code)
        rk = vanilla.get(f"block.{ROCKS[rock][0].replace(':', '.')}")
        if rk is None and ROCKS[rock][0].startswith(f"{MOD}:"):
            import geo_rocks
            rk = geo_rocks.rock_name(rock, code)
        if ore and rk:
            out[f"{name} ({rock_name(rock)})"] = f"{ore} ({rk})"
    return out
