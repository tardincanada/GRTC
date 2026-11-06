"""Random rotations for natural blocks, so a wall of the same rock or ore doesn't show a repeating grid.

Vanilla does the same for stone, dirt, sand and grass: the blockstate lists several rotated copies of the model and
the game picks one per position. Runs after every other generator and rewrites only single-variant blockstates of:
  - cube_all blocks (rocks, soils, ores, crusts, asphalt...): 16 x/y rotations, every face can turn;
  - geo_ore blocks: 16 rotations when top, side and bottom are the same texture, else only 4 around y, so a sandstone or
    basalt top stays on top.
  - layered rocks (shale, gneiss, oil shale, deepslate ores; Guilherme 2026-11-05): only 4 around y, so the layers
    stay level next to vanilla rock. Cobbled versions are rubble and turn fully.
Planks, bakelite, polished stone, tank blocks and a few others keep their single variant: their texture has a
direction or a pattern that should line up.
"""
import json

SKIP_SUFFIX = ("_planks", "_bakelite")
SKIP_PREFIX = ("polished_", "tank_")
SKIP = {"insulation", "tritium_light", "rubber_leaves"}
COLUMN_ROCKS = {"gneiss", "shale"}  # rocks with their own top (tools/geo_rocks.py TOPS); other cube_column blocks are machines
LAYERED_PREFIX = ("shale", "gneiss", "deepslate_", "oil_shale")
LAYERED = {"terracotta_oil_shale_ore", "brown_terracotta_oil_shale_ore"}


def _rotations(model, full):
    xs = (0, 90, 180, 270) if full else (0,)
    out = []
    for x in xs:
        for y in (0, 90, 180, 270):
            v = {"model": model}
            if x:
                v["x"] = x
            if y:
                v["y"] = y
            out.append(v)
    return out


def randomize_rotations(assets, mod_id):
    """Returns how many blockstates now pick a random rotation."""
    n = 0
    for path in sorted((assets / "blockstates").glob("*.json")):
        bid = path.stem
        if bid.endswith(SKIP_SUFFIX) or bid.startswith(SKIP_PREFIX) or bid in SKIP:
            continue
        state = json.loads(path.read_text(encoding="utf-8"))
        variant = state.get("variants", {}).get("")
        if not isinstance(variant, dict) or set(variant) != {"model"}:
            continue
        model = variant["model"]
        ns, _, name = model.partition(":")
        if ns != mod_id:
            continue
        mpath = assets / "models" / f"{name}.json"
        if not mpath.exists():
            continue
        m = json.loads(mpath.read_text(encoding="utf-8"))
        parent, tex = m.get("parent"), m.get("textures", {})
        if parent == "minecraft:block/cube_all":
            full = True
        elif parent == "minecraft:block/cube_column" and bid in COLUMN_ROCKS:  # own top and bottom: only around y
            full = False
        elif parent == f"{mod_id}:block/geo_ore":
            full = tex.get("top") == tex.get("side") == tex.get("bottom")
        else:
            continue
        if bid.startswith(LAYERED_PREFIX) or bid in LAYERED:
            full = False
        path.write_text(json.dumps({"variants": {"": _rotations(model, full)}}, indent=2) + "\n", encoding="utf-8")
        n += 1
    return n


# Vanilla blocks the Geological generator (and vanilla ore veins) place in the open, which vanilla draws with a single
# variant. The mod overrides their blockstates in assets/minecraft so they rotate too (Guilherme 2026-11-05: the
# basalt plateau still showed a grid). Blocks vanilla already rotates (stone, deepslate, dirt, sand, grass, podzol,
# mycelium, rooted dirt...) are left alone. This changes how the blocks look in every world, not where they generate.
_COLOURS = ("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan",
            "purple", "blue", "brown", "green", "red", "black")
VANILLA_FULL = (
    "granite", "diorite", "andesite", "tuff", "calcite", "smooth_basalt", "gravel", "clay", "mud", "packed_ice",
    "snow_block", "coarse_dirt", "magma_block", "terracotta",
    *(f"{c}_terracotta" for c in _COLOURS),
    "coal_ore", "iron_ore", "copper_ore", "gold_ore", "diamond_ore", "redstone_ore", "lapis_ore", "emerald_ore",
)
# Top differs from the sides (sandstone) or the rock is layered (deepslate ores): only around y.
VANILLA_Y = (
    "sandstone", "red_sandstone",
    *(f"deepslate_{o}_ore" for o in ("coal", "iron", "copper", "gold", "diamond", "redstone", "lapis", "emerald")),
)


def randomize_vanilla_rotations(res):
    """Writes assets/minecraft/blockstates overrides; returns how many."""
    out_dir = res / "assets" / "minecraft" / "blockstates"
    out_dir.mkdir(parents=True, exist_ok=True)
    states = {}
    for bid in VANILLA_FULL:
        states[bid] = {"variants": {"": _rotations(f"minecraft:block/{bid}", True)}}
    for bid in VANILLA_Y:
        states[bid] = {"variants": {"": _rotations(f"minecraft:block/{bid}", False)}}
    # Basalt is a pillar: upright basalt (all the generator places) turns around y; lying basalt stays as vanilla.
    states["basalt"] = {"variants": {
        "axis=x": {"model": "minecraft:block/basalt", "x": 90, "y": 90},
        "axis=y": _rotations("minecraft:block/basalt", False),
        "axis=z": {"model": "minecraft:block/basalt", "x": 90},
    }}
    for bid, state in states.items():
        (out_dir / f"{bid}.json").write_text(json.dumps(state, indent=2) + "\n", encoding="utf-8")
    return len(states)
