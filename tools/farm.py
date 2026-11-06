"""Farm and fat (1.7.0): the rubber tree, the Tree Tap, tallow candles, and the items of the farm processes.

Writes block and item assets, loot tables, tags, the rubber tree's worldgen features, and lang lines. The Java side
(com.periodicelements.farm.Farm) registers the blocks with the same ids.

Rules Guilherme set: the rubber tree grows wild only in jungle biomes (jungle, sparse jungle, bamboo jungle); its
sapling grows anywhere, but a Tree Tap gives latex only in a jungle biome.
"""
import json
import random

from PIL import Image

# Items whose textures this module draws (their names and tooltips are in machines.ITEMS).
FARM_ITEMS = {"protein_meal", "oil_cake", "spent_bark", "sneakers", "rubber_wheels",
              "dynamite", "cordite_cartridge", "carbolic_soap", "anaesthetic", "bordeaux_mixture", "insect_spray", "vacuum_pump"}
# Blocks registered by Farm.java: id -> (name, description).
FARM_BLOCKS = {
    "rubber_log": ("Rubber Log", "Para rubber wood; a Tree Tap on its bark gives latex in the jungle"),
    "rubber_leaves": ("Rubber Leaves", "Sometimes drops a rubber sapling"),
    "rubber_planks": ("Rubber Planks", "Pale rubberwood"),
    "rubber_sapling": ("Rubber Sapling", "Grows only in the jungle, like the wild rubber trees"),
    "tree_tap": ("Tree Tap", "Fix it to a rubber log in the jungle, or to a tree that gives sap or resin: it collects some every five minutes"),
    "tallow_candle": ("Tallow Candle", "Bright, smoky light that burns down in three days"),
    "tarmac": ("Tarmac", "Gravel and sand bound with coal tar pitch: you walk and ride faster on it"),
    "mining_charge": ("Mining Charge", "Light it with flint and steel or redstone: breaks a 5x5x5 block of rock and drops all of it"),
}
JUNGLES = ["minecraft:jungle", "minecraft:sparse_jungle", "minecraft:bamboo_jungle"]
# Ticks per latex flask and how many a tap holds; burn time of a candle (3 game days).
TAP_TICKS = 6000
TAP_HOLDS = 4
CANDLE_TICKS = 72000
# Fertilized farmland: harvests it lasts, extra growth.
FERTILIZER_HARVESTS = 8


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _noise(base, var, seed, size=16):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for y in range(size):
        for x in range(size):
            d = rnd.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


def _rect(img, x0, y0, x1, y1, color):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(color) + (255,) if len(color) == 3 else tuple(color)


def farm_item_texture(iid):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = random.Random(hash(iid) & 0xFFFF)
    px = img.load()
    if iid in ("protein_meal", "oil_cake", "spent_bark"):
        base = {"protein_meal": (196, 160, 120), "oil_cake": (170, 140, 70), "spent_bark": (110, 76, 46)}[iid]
        for y in range(4, 14):
            for x in range(2, 14):
                if (x - 8) ** 2 / 36 + (y - 9) ** 2 / 20 <= 1 and rnd.random() < 0.9:
                    d = rnd.randint(-24, 24)
                    px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    elif iid == "sneakers":
        canvas, sole, lace = (230, 230, 226), (46, 42, 40), (200, 60, 50)
        _rect(img, 3, 5, 8, 11, canvas)
        _rect(img, 3, 11, 13, 12, canvas)
        _rect(img, 2, 13, 14, 14, sole)
        for y in (6, 8, 10):
            _rect(img, 5, y, 7, y, lace)
        _rect(img, 12, 11, 13, 12, (210, 210, 206))
    elif iid == "rubber_wheels":
        tyre, hub = (40, 38, 36), (170, 174, 180)
        for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11)):
            for y in range(cy - 3, cy + 4):
                for x in range(cx - 3, cx + 4):
                    d = (x - cx) ** 2 + (y - cy) ** 2
                    if d <= 10:
                        px[x, y] = (hub if d <= 2 else tyre) + (255,)
        _rect(img, 4, 7, 11, 8, (120, 124, 130))
    elif iid == "dynamite":
        for x0 in (3, 7, 11):
            _rect(img, x0, 4, x0 + 2, 13, (196, 50, 40))
            _rect(img, x0, 4, x0 + 2, 4, (230, 210, 170))
        _rect(img, 3, 9, 13, 9, (120, 90, 50))
        _rect(img, 8, 1, 8, 3, (60, 50, 40))
    elif iid == "cordite_cartridge":
        _rect(img, 6, 6, 9, 14, (214, 176, 80))
        _rect(img, 6, 14, 9, 14, (170, 136, 60))
        _rect(img, 7, 2, 8, 5, (110, 110, 118))
    elif iid in ("carbolic_soap",):
        _rect(img, 3, 6, 12, 11, (226, 140, 120))
        _rect(img, 4, 7, 11, 8, (240, 176, 160))
    elif iid == "anaesthetic":
        _rect(img, 3, 5, 12, 12, (238, 238, 232))
        _rect(img, 5, 7, 10, 10, (210, 226, 236))
    elif iid == "bordeaux_mixture":
        _rect(img, 6, 2, 9, 3, (150, 110, 70))
        _rect(img, 4, 4, 11, 14, (200, 220, 230))
        _rect(img, 5, 6, 10, 13, (70, 150, 220))
    elif iid == "insect_spray":
        _rect(img, 5, 6, 10, 14, (190, 196, 204))
        _rect(img, 6, 7, 9, 13, (230, 216, 140))
        _rect(img, 7, 3, 8, 5, (90, 94, 100))
        _rect(img, 9, 3, 12, 3, (90, 94, 100))
    elif iid == "vacuum_pump":
        _rect(img, 3, 9, 12, 14, (120, 124, 130))
        _rect(img, 6, 2, 9, 8, (150, 154, 160))
        _rect(img, 7, 3, 8, 7, (210, 230, 240))
        _rect(img, 12, 10, 14, 11, (200, 140, 60))
    return img


def _block_textures(tex):
    rnd = random.Random(1700)
    # Log side: smooth grey-brown bark with diagonal tapping scars.
    side = _noise((132, 116, 96), 10, 1701)
    for y in range(16):
        for x in (2, 7, 12):
            if rnd.random() < 0.6:
                _rect(side, x, y, x, y, (100, 86, 70))
    for i in range(5):
        _rect(side, 4 + i, 9 - i, 4 + i, 9 - i, (210, 200, 180))
    side.save(tex / "rubber_log.png")
    top = _noise((214, 196, 160), 8, 1702)
    for r, c in ((7, (190, 170, 136)), (4, (200, 182, 146))):
        for y in range(16):
            for x in range(16):
                if abs(((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5 - r) < 0.6:
                    _rect(top, x, y, x, y, c)
    for i in range(16):
        _rect(top, i, 0, i, 0, (132, 116, 96)); _rect(top, i, 15, i, 15, (132, 116, 96))
        _rect(top, 0, i, 0, i, (132, 116, 96)); _rect(top, 15, i, 15, i, (132, 116, 96))
    top.save(tex / "rubber_log_top.png")
    planks = _noise((218, 200, 164), 8, 1703)
    for y in (3, 7, 11, 15):
        _rect(planks, 0, y, 15, y, (170, 150, 116))
    planks.save(tex / "rubber_planks.png")
    leaves = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    lp = leaves.load()
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.82:
                g = rnd.randint(-22, 22)
                lp[x, y] = (40 + g // 2, 110 + g, 40 + g // 2, 255)
    leaves.save(tex / "rubber_leaves.png")
    sap = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    _rect(sap, 7, 8, 8, 15, (110, 90, 66))
    for (x, y) in ((5, 4), (10, 4), (4, 7), (11, 7), (7, 2), (6, 6), (9, 6)):
        _rect(sap, x, y, x + 1, y + 1, (60, 140, 50))
    sap.save(tex / "rubber_sapling.png")
    tap = _noise((150, 154, 160), 6, 1704)
    tap.save(tex / "tree_tap.png")
    flask = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    _rect(flask, 0, 0, 15, 15, (225, 238, 245))
    _rect(flask, 2, 6, 13, 13, (246, 244, 236))
    flask.save(tex / "tree_tap_flask.png")
    candle = _noise((240, 232, 206), 6, 1705)
    _rect(candle, 7, 0, 8, 2, (60, 50, 40))
    candle.save(tex / "tallow_candle.png")


def _extra_textures(assets):
    """The Clean effect icon and the Sneakers armor layers."""
    eff = assets / "textures" / "mob_effect"
    eff.mkdir(parents=True, exist_ok=True)
    icon = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    px = icon.load()
    for y in range(18):
        for x in range(18):
            for cx, cy, r in ((7, 8, 5), (12, 5, 3), (12, 13, 2.5)):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
                if d <= r:
                    px[x, y] = (255, 255, 255, 255) if d > r - 1.2 else (190, 232, 245, 200)
    icon.save(eff / "clean.png")
    arm = assets / "textures" / "models" / "armor"
    arm.mkdir(parents=True, exist_ok=True)
    layer = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Boots use the leg box (u 0-16, v 16-32): canvas above a dark rubber sole.
    for y in range(22, 32):
        for x in range(0, 16):
            layer.load()[x, y] = (46, 42, 40, 255) if y >= 29 else (232, 232, 228, 255)
    layer.save(arm / "sneakers_layer_1.png")
    Image.new("RGBA", (64, 32), (0, 0, 0, 0)).save(arm / "sneakers_layer_2.png")


def _cube(f, t):
    return {"from": f, "to": t, "faces": {d: {"texture": "#all"} for d in ("north", "south", "east", "west", "up", "down")}}


def generate_farm(assets, data_dir, runtime_dir, lang, mod_id):
    tex = assets / "textures" / "block"
    tex.mkdir(parents=True, exist_ok=True)
    _block_textures(tex)
    _extra_textures(assets)
    models = assets / "models"
    states = assets / "blockstates"
    m = lambda n: f"{mod_id}:block/{n}"

    # Log: a pillar with three axes.
    _dump(models / "block" / "rubber_log.json", {"parent": "minecraft:block/cube_column",
                                                "textures": {"end": m("rubber_log_top"), "side": m("rubber_log")}})
    _dump(models / "block" / "rubber_log_horizontal.json", {"parent": "minecraft:block/cube_column_horizontal",
                                                           "textures": {"end": m("rubber_log_top"), "side": m("rubber_log")}})
    _dump(states / "rubber_log.json", {"variants": {
        "axis=y": {"model": m("rubber_log")},
        "axis=z": {"model": m("rubber_log_horizontal"), "x": 90},
        "axis=x": {"model": m("rubber_log_horizontal"), "x": 90, "y": 90}}})
    _dump(models / "block" / "rubber_planks.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m("rubber_planks")}})
    _dump(states / "rubber_planks.json", {"variants": {"": {"model": m("rubber_planks")}}})
    # Leaves come coloured in the texture (no biome tint), drawn with the cutout layer.
    _dump(models / "block" / "rubber_leaves.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m("rubber_leaves")}})
    _dump(states / "rubber_leaves.json", {"variants": {"": {"model": m("rubber_leaves")}}})
    _dump(models / "block" / "rubber_sapling.json", {"parent": "minecraft:block/cross", "textures": {"cross": m("rubber_sapling")}})
    _dump(states / "rubber_sapling.json", {"variants": {"": {"model": m("rubber_sapling")}}})
    for b in ("rubber_log", "rubber_planks", "rubber_leaves"):
        _dump(models / "item" / f"{b}.json", {"parent": m(b)})
    _dump(models / "item" / "rubber_sapling.json", {"parent": "minecraft:item/generated", "textures": {"layer0": m("rubber_sapling")}})

    # Tree Tap: a spout from the north face of its block (the log side it is fixed to) with a flask under it.
    tap = {"parent": "minecraft:block/block", "textures": {"particle": m("tree_tap"), "all": m("tree_tap"), "flask": m("tree_tap_flask")},
           "elements": [_cube([7, 9, 0], [9, 10, 5]),
                        {"from": [5, 2, 3], "to": [11, 8, 9],
                         "faces": {d: {"texture": "#flask"} for d in ("north", "south", "east", "west", "up", "down")}}]}
    _dump(models / "block" / "tree_tap.json", tap)
    _dump(states / "tree_tap.json", {"variants": {f"facing={f}": {"model": m("tree_tap"), "y": y}
                                                  for f, y in (("south", 0), ("west", 90), ("north", 180), ("east", 270))}})
    _dump(models / "item" / "tree_tap.json", {"parent": m("tree_tap")})
    candle = {"parent": "minecraft:block/block", "textures": {"particle": m("tallow_candle"), "all": m("tallow_candle")},
              "elements": [_cube([6, 0, 6], [10, 9, 10])]}
    _dump(models / "block" / "tallow_candle.json", candle)
    _dump(states / "tallow_candle.json", {"variants": {"": {"model": m("tallow_candle")}}})
    _dump(models / "item" / "tallow_candle.json", {"parent": m("tallow_candle")})
    # Tarmac: black stone chips in pitch. Mining Charge: red sticks bound to a board, fuse on top.
    t = _noise((40, 38, 36), 14, 1801)
    rnd2 = random.Random(1802)
    for _ in range(18):
        x, y = rnd2.randint(0, 15), rnd2.randint(0, 15)
        _rect(t, x, y, x, y, (110, 106, 100))
    t.save(tex / "tarmac.png")
    side = _noise((150, 110, 70), 6, 1803)
    for x0 in (1, 6, 11):
        _rect(side, x0, 2, x0 + 3, 13, (196, 50, 40))
    _rect(side, 0, 7, 15, 8, (90, 70, 40))
    side.save(tex / "mining_charge_side.png")
    top = _noise((150, 110, 70), 6, 1804)
    _rect(top, 7, 7, 8, 8, (40, 36, 32))
    top.save(tex / "mining_charge_top.png")
    for b in ("tarmac",):
        _dump(models / "block" / f"{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m(b)}})
        _dump(states / f"{b}.json", {"variants": {"": {"model": m(b)}}})
        _dump(models / "item" / f"{b}.json", {"parent": m(b)})
    _dump(models / "block" / "mining_charge.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "side": m("mining_charge_side"), "top": m("mining_charge_top"), "bottom": m("mining_charge_top")}})
    _dump(states / "mining_charge.json", {"variants": {"": {"model": m("mining_charge")}}})
    _dump(models / "item" / "mining_charge.json", {"parent": m("mining_charge")})

    # Loot: most blocks drop themselves; leaves drop a sapling now and then (shears or silk touch keep the leaves).
    loot = data_dir / "loot_table" / "blocks"
    for b in ("rubber_log", "rubber_planks", "rubber_sapling", "tree_tap", "tallow_candle", "tarmac", "mining_charge"):
        _dump(loot / f"{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": f"{mod_id}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    shears = {"condition": "minecraft:any_of", "terms": [
        {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
        {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
            {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}
    _dump(loot / "rubber_leaves.json", {"type": "minecraft:block", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{mod_id}:rubber_leaves", "conditions": [shears]},
            {"type": "minecraft:item", "name": f"{mod_id}:rubber_sapling", "conditions": [
                {"condition": "minecraft:survives_explosion"},
                {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                 "chances": [0.05, 0.0625, 0.083333336, 0.1]}]}]}]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:inverted", "term": shears}], "entries": [
            {"type": "minecraft:item", "name": "minecraft:stick", "conditions": [
                {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                 "chances": [0.02, 0.022222223, 0.025, 0.033333335, 0.1]}]}]}]})

    # Tags: the log and planks work as wood everywhere (crafting, fuel, the charcoal pit).
    mc = data_dir.parent / "minecraft" / "tags"
    tags = {("block", "logs"): ["rubber_log"], ("block", "logs_that_burn"): ["rubber_log"], ("block", "planks"): ["rubber_planks"],
            ("block", "leaves"): ["rubber_leaves"], ("block", "saplings"): ["rubber_sapling"],
            ("item", "logs"): ["rubber_log"], ("item", "logs_that_burn"): ["rubber_log"], ("item", "planks"): ["rubber_planks"],
            ("item", "leaves"): ["rubber_leaves"], ("item", "saplings"): ["rubber_sapling"],
            ("block", "mineable/axe"): ["rubber_log", "rubber_planks", "tree_tap"],
            ("block", "mineable/hoe"): ["rubber_leaves"],
            ("block", "mineable/pickaxe"): ["tarmac"],
            # Protein meal and oil cake feed animals.
            ("item", "cow_food"): ["oil_cake", "protein_meal"], ("item", "sheep_food"): ["oil_cake", "protein_meal"],
            ("item", "goat_food"): ["oil_cake", "protein_meal"], ("item", "chicken_food"): ["protein_meal"],
            ("item", "pig_food"): ["protein_meal"]}
    for (kind, name), ids in tags.items():
        path = mc / kind / f"{name}.json"
        values = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
        values += [f"{mod_id}:{i}" for i in ids if f"{mod_id}:{i}" not in values]
        _dump(path, {"replace": False, "values": values})
    _dump(data_dir / "tags" / "item" / "soap.json", {"values": [f"{mod_id}:soft_soap", f"{mod_id}:hard_soap", f"{mod_id}:carbolic_soap"]})
    _dump(data_dir / "tags" / "item" / "fertilizer.json", {"values": [f"{mod_id}:{i}" for i in (
        "superphosphate", "ammonium_sulfate", "calcium_cyanamide", "urea", "ammonium_nitrate", "npk_prills",
        "spent_mycelium", "biomass")]})
    _dump(data_dir / "tags" / "worldgen" / "biome" / "rubber_tree_biomes.json", {"values": JUNGLES})

    # The rubber tree: a straight trunk 6 to 8 high with a round crown, one per jungle chunk.
    tree = {"type": "minecraft:tree", "config": {
        "ignore_vines": True, "force_dirt": False,
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
        "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:dirt"}},
        "trunk_provider": {"type": "minecraft:simple_state_provider",
                           "state": {"Name": f"{mod_id}:rubber_log", "Properties": {"axis": "y"}}},
        "foliage_provider": {"type": "minecraft:simple_state_provider", "state": {
            "Name": f"{mod_id}:rubber_leaves", "Properties": {"distance": "7", "persistent": "false", "waterlogged": "false"}}},
        "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": 6, "height_rand_a": 2, "height_rand_b": 0},
        "foliage_placer": {"type": "minecraft:blob_foliage_placer", "radius": 2, "offset": 0, "height": 3},
        "decorators": []}}
    wg = data_dir / "worldgen"
    _dump(wg / "configured_feature" / "rubber_tree.json", tree)
    _dump(wg / "placed_feature" / "rubber_tree.json", {"feature": f"{mod_id}:rubber_tree", "placement": [
        {"type": "minecraft:count", "count": 1},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
        {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
        {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive",
                                                                     "state": {"Name": f"{mod_id}:rubber_sapling", "Properties": {"stage": "0"}}}},
        {"type": "minecraft:biome"}]})
    with (runtime_dir / "farm.json").open("w", encoding="utf-8") as f:
        json.dump({"tapTicks": TAP_TICKS, "tapHolds": TAP_HOLDS, "candleTicks": CANDLE_TICKS,
                   "fertilizerHarvests": FERTILIZER_HARVESTS}, f, indent=1)
        f.write("\n")

    for b, (name, desc) in FARM_BLOCKS.items():
        lang[f"block.{mod_id}.{b}"] = name
        lang[f"block.{mod_id}.{b}.desc"] = desc
    lang.update({
        f"effect.{mod_id}.clean": "Clean",
        f"gui.{mod_id}.tap.latex": "Tree Tap: %s of %s flasks of latex ready",
        f"gui.{mod_id}.tap.climate": "No latex: wrong climate. Rubber trees give latex only in the jungle",
        f"gui.{mod_id}.tap.log": "A Tree Tap has to be fixed to the side of a rubber log or a tree that gives sap or resin",
        f"gui.{mod_id}.fertilized": "Fertilized: %s harvests left",
        f"gui.{mod_id}.clean": "You wash with soap: Clean for ten minutes",
        f"gui.{mod_id}.undyed": "You scrub the dye out of the leather",
        f"gui.{mod_id}.wheels": "Rubber wheels fitted: this minecart runs 25% faster",
        f"gui.{mod_id}.wheels.already": "This minecart already has rubber wheels",
        f"effect.{mod_id}.infection": "Infection",
        f"effect.{mod_id}.sepsis": "Sepsis",
        f"gui.{mod_id}.infected": "The wound looks angry: you have an Infection. Carbolic soap or aspirin will cure it",
        f"gui.{mod_id}.recovered": "The fever breaks: the Infection is over",
        f"gui.{mod_id}.sepsis": "The infection has spread to your blood: sepsis. Only a sulfa drug or an antibiotic can stop it now",
        f"gui.{mod_id}.cured": "The Infection is cured",
        f"gui.{mod_id}.aspirin": "You take an aspirin",
        f"gui.{mod_id}.anaesthetic": "The ether takes effect: the cure will take half as long",
        f"gui.{mod_id}.anaesthetic.not_curing": "Anaesthetic helps only a zombie villager already being cured",
        f"gui.{mod_id}.sprayed": "Sprayed with Bordeaux mixture",
        f"gui.{mod_id}.charge.lit": "The fuse is lit!",
        f"death.attack.{mod_id}.sepsis": "%1$s died of sepsis",
        f"gamerule.periodicElementsInfection": "Infection (rare wound infections; medicines cure them)",
        f"gamerule.periodicElementsInfection.description": "Zombie hits and rotten flesh can cause an Infection",
    })
    return list(FARM_BLOCKS)
