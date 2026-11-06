"""Mining tools (2.1.0): textures, models, loot, tags and text for the drills, drill bits, battery packs, the Charging
Bench and the Blasting Charge. Sledgehammers and Excavators are gear (tools/gear.py, AREA_TOOLS); the Air Compressor is
a machine (data/machines.csv). Java: com.periodicelements.mining.
"""
import json
import random

from PIL import Image

DRILLS = ["pneumatic_rock_drill", "rotary_hammer", "heavy_mining_drill", "chainsaw"]
BITS = {"tungsten_steel_bit": (104, 112, 124), "high_speed_steel_bit": (120, 116, 130),
        "cemented_carbide_bit": (80, 84, 92), "synthetic_diamond_bit": (150, 220, 230)}
PACKS = {"lead_acid_pack": ((70, 72, 78), (200, 60, 50)), "li_ion_pack": ((40, 44, 52), (90, 160, 240))}
MINING_ITEMS = set(DRILLS) | set(BITS) | set(PACKS)
MINING_BLOCKS = {
    "charging_bench": ("Charging Bench",
                       "Join it to the wire and use a battery pack on it: fills the pack at up to 5 kJ a tick"),
    "blasting_charge": ("Blasting Charge",
                        "Light it with flint and steel or redstone: breaks a 3x3x3 block of stone, drops all of it, spares machines"),
}
STEEL, STEEL_L, STEEL_D = (150, 156, 166), (204, 210, 220), (88, 92, 104)


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _rect(img, x0, y0, x1, y1, color):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(color) + (255,)


def _noise(base, var, seed, size=(16, 16)):
    rnd = random.Random(seed)
    img = Image.new("RGBA", size)
    px = img.load()
    for y in range(size[1]):
        for x in range(size[0]):
            d = rnd.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


def _diag(img, x0, y0, n, color, width=1):
    """A line from (x0, y0) up and to the right, n pixels long."""
    for i in range(n):
        _rect(img, x0 + i, y0 - i, x0 + i + width - 1, y0 - i, color)


def mining_item_texture(iid):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    if iid in BITS:
        c = BITS[iid]
        hi = tuple(min(255, k + 50) for k in c)
        _rect(img, 2, 12, 5, 13, STEEL_D)               # shank
        _diag(img, 4, 12, 8, c, 2)                      # fluted body
        for i in range(0, 8, 2):
            _rect(img, 5 + i, 11 - i, 5 + i, 11 - i, hi)
        _rect(img, 12, 3, 13, 4, hi)                     # tip
    elif iid in PACKS:
        case, band = PACKS[iid]
        _rect(img, 3, 4, 12, 14, case)
        _rect(img, 4, 5, 11, 13, tuple(min(255, k + 20) for k in case))
        _rect(img, 3, 7, 12, 8, band)
        _rect(img, 5, 2, 6, 3, (200, 200, 200))          # terminals
        _rect(img, 9, 2, 10, 3, (200, 200, 200))
    elif iid == "pneumatic_rock_drill":
        _rect(img, 3, 3, 9, 7, STEEL)                    # cylinder
        _rect(img, 3, 3, 9, 3, STEEL_L)
        _rect(img, 1, 4, 2, 6, STEEL_D)                  # handles
        _rect(img, 9, 8, 10, 9, (40, 40, 44))            # air hose
        _rect(img, 11, 10, 12, 14, (40, 40, 44))
        _rect(img, 10, 5, 14, 5, STEEL_D)                # steel
    elif iid == "rotary_hammer":
        _rect(img, 2, 4, 10, 8, (30, 90, 160))           # body
        _rect(img, 2, 4, 10, 4, (70, 130, 200))
        _rect(img, 4, 9, 6, 14, (40, 40, 44))            # grip
        _rect(img, 4, 14, 8, 15, (200, 60, 50))          # pack
        _rect(img, 11, 5, 15, 7, STEEL_D)                # chuck and bit
    elif iid == "heavy_mining_drill":
        _rect(img, 1, 3, 10, 10, (230, 180, 40))         # body
        _rect(img, 1, 3, 10, 3, (250, 220, 110))
        _rect(img, 3, 11, 5, 14, (40, 40, 44))
        _rect(img, 11, 4, 14, 9, STEEL)                  # 5x5 head
        _rect(img, 12, 5, 13, 8, (150, 220, 230))
    elif iid == "chainsaw":
        _rect(img, 1, 6, 7, 11, (230, 110, 30))          # body
        _rect(img, 1, 6, 7, 6, (250, 160, 90))
        _rect(img, 2, 3, 6, 4, (40, 40, 44))             # handle
        _rect(img, 8, 7, 15, 10, STEEL)                  # bar
        for x in range(8, 16, 2):                        # chain teeth
            _rect(img, x, 6, x, 6, STEEL_D)
            _rect(img, x + 1, 11, x + 1, 11, STEEL_D)
    return img


def generate_mining(assets, data_root, runtime_dir, lang, mod_id):
    tex = assets / "textures" / "block"
    models = assets / "models"
    states = assets / "blockstates"
    data_dir = data_root / mod_id
    m = lambda n: f"{mod_id}:block/{n}"

    for d in DRILLS:
        _dump(models / "item" / f"{d}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{mod_id}:item/{d}"}})

    # Charging Bench: a steel bench with a copper bus bar and a green charge lamp.
    side = _noise((120, 126, 136), 6, 2101)
    _rect(side, 0, 0, 15, 2, (200, 120, 60))
    _rect(side, 6, 6, 9, 9, (60, 200, 90))
    side.save(tex / "charging_bench_side.png")
    top = _noise((96, 100, 108), 6, 2102)
    _rect(top, 2, 2, 13, 13, (60, 62, 68))
    _rect(top, 4, 7, 11, 8, (200, 120, 60))
    top.save(tex / "charging_bench_top.png")
    _dump(models / "block" / "charging_bench.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "side": m("charging_bench_side"), "top": m("charging_bench_top"), "bottom": m("charging_bench_side")}})
    _dump(states / "charging_bench.json", {"variants": {"": {"model": m("charging_bench")}}})
    _dump(models / "item" / "charging_bench.json", {"parent": m("charging_bench")})

    # Blasting Charge: brown paper cartridges of black powder tied with string, a fuse on top.
    side = _noise((150, 110, 70), 8, 2103)
    for x in (0, 5, 10, 15):
        _rect(side, x, 0, x, 15, (110, 80, 50))
    _rect(side, 0, 7, 15, 8, (230, 230, 220))
    side.save(tex / "blasting_charge_side.png")
    top = _noise((150, 110, 70), 8, 2104)
    _rect(top, 7, 7, 8, 8, (40, 36, 32))
    top.save(tex / "blasting_charge_top.png")
    _dump(models / "block" / "blasting_charge.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "side": m("blasting_charge_side"), "top": m("blasting_charge_top"), "bottom": m("blasting_charge_top")}})
    _dump(states / "blasting_charge.json", {"variants": {"": {"model": m("blasting_charge")}}})
    _dump(models / "item" / "blasting_charge.json", {"parent": m("blasting_charge")})

    loot = data_dir / "loot_table" / "blocks"
    for b in MINING_BLOCKS:
        _dump(loot / f"{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": f"{mod_id}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    # Bits take the mining enchantments (the drill copies them while the bit is in it); drills take none themselves.
    for (kind, tag), ids in {("block", "mineable/pickaxe"): ["charging_bench"],
                             ("item", "enchantable/mining"): list(BITS),
                             ("item", "enchantable/mining_loot"): list(BITS),
                             ("item", "enchantable/durability"): list(BITS)}.items():
        path = data_root / "minecraft" / "tags" / kind / f"{tag}.json"
        values = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
        values += [f"{mod_id}:{i}" for i in ids if f"{mod_id}:{i}" not in values]
        _dump(path, {"replace": False, "values": values})

    for b, (name, desc) in MINING_BLOCKS.items():
        lang[f"block.{mod_id}.{b}"] = name
        lang[f"block.{mod_id}.{b}.desc"] = desc
    g = f"gui.{mod_id}"
    lang.update({
        f"{g}.drill.mode": "Mode: %s",
        f"{g}.drill.mode.single": "one block",
        f"{g}.drill.mode.tunnel": "1x3 tunnel",
        f"{g}.drill.mode.area3": "3x3",
        f"{g}.drill.mode.area5": "5x5",
        f"{g}.drill.mode.vein": "vein (up to 32)",
        f"{g}.drill.mode.tree": "whole tree",
        f"{g}.drill.bit": "Bit: %s",
        f"{g}.drill.no_bit": "No bit: sneak and use with a drill bit in your other hand",
        f"{g}.drill.bit_fitted": "Fitted: %s",
        f"{g}.drill.bit_broken": "The drill bit is worn out",
        f"{g}.drill.energy": "Charge: %s of %s kJ",
        f"{g}.drill.air": "Air: %s of %s kJ",
        f"{g}.drill.needs": "Needs %s research for this mode",
        f"{g}.drill.vein": "Vein of %s %s: %s",
        f"{g}.drill.vein.none": "Not an ore: vein mode follows ores only",
        f"{g}.pack.energy": "%s of %s kJ",
        f"{g}.bench.empty": "Use a battery pack on the bench to charge it",
        f"{g}.bench.charging": "%s: %s of %s kJ",
        f"{g}.bench.no_power": "%s: %s of %s kJ (no current: join the bench to the wire)",
        f"{g}.charge.blasting": "The blasting fuse is lit: stand clear!",
        f"{g}.area.needs": "Learn %s to work more than one block at a time",
        f"tooltip.{mod_id}.sledgehammer": "Breaks 3x3 of stone at 0.3 speed, one wear per block; sneak for one block",
        f"tooltip.{mod_id}.excavator": "Digs 3x3 of earth at 0.3 speed, one wear per block; sneak for one block",
    })
