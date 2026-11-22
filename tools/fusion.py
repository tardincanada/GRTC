"""Level 8, stage 3 (3.63.0): fusion power. The Tokamak (a power block, kind "fusion", in machines.POWER) and the four
parts of its 5 x 5 x 3 ring: Toroidal Field Coil, Central Solenoid, Vacuum Vessel (glows while the plasma burns) and
Lithium Blanket. Deuterium comes from heavy water in the Electrolysis Cell (data/reactions.csv).

Java: com.periodicelements.space.Fusion (blocks, the ring check) and PowerBlockEntity.tickFusion. space.py calls
generate_fusion and merges LANG, the guide paragraphs (appended to the Space chapter) and translations(code).
"""
import json
import random

from PIL import Image

from fusion_i18n import TR, GUIDE_I18N

PARTS = ["toroidal_field_coil", "central_solenoid", "vacuum_vessel", "lithium_blanket"]
ITEM_UNLOCKS = set(PARTS)

M = "periodic_elements"
LANG = {
    f"block.{M}.toroidal_field_coil": "Toroidal Field Coil",
    f"block.{M}.toroidal_field_coil.desc": "Niobium-titanium magnet cooled by liquid helium: 48 make the cage of a Tokamak",
    f"block.{M}.central_solenoid": "Central Solenoid",
    f"block.{M}.central_solenoid.desc": "The magnet column in the hole of the doughnut: it drives a current through the plasma",
    f"block.{M}.vacuum_vessel": "Vacuum Vessel",
    f"block.{M}.vacuum_vessel.desc": "Steel chamber lined with beryllium and tungsten, where the plasma burns at 150 million °C",
    f"block.{M}.lithium_blanket": "Lithium Blanket",
    f"block.{M}.lithium_blanket.desc": "Lithium behind the wall: the plasma's neutrons turn it into tritium for the next burn",
    f"gui.{M}.power.fusion": "Tokamak: %s/%s kJ stored; %s",
    f"gui.{M}.power.fusion.missing": "%s missing at %s, %s, %s",
    f"gui.{M}.power.fusion.no_ring": "no ring: put it against the middle of one side of the 5 x 5 x 3 ring, on the middle layer",
    f"gui.{M}.power.fusion.no_fuel": "needs deuterium, a tritium vial and a helium flask",
    f"gui.{M}.power.fusion.charging": "lighting the plasma: %s of %s kJ drawn from the wire",
    f"gui.{M}.power.fusion.burning": "plasma burning, %s%% of this fuel used; %s lithium to breed tritium",
}

GUIDE = [
    "@8 # Fusion power",
    "@8 The Tokamak is the strongest power source in the game: 250 kJ per tick, about eight times a fission reactor. "
    "Real fusion is still being tested (ITER); here it works as if it were ready.",
    "@8 It needs a ring 5 × 5 wide and 3 high. Middle layer: a Central Solenoid in the centre, a ring of 8 Vacuum "
    "Vessels round it and a ring of 16 Toroidal Field Coils outside. The layers above and below: a Central Solenoid in "
    "the centre, 8 Lithium Blankets and 16 coils. The Tokamak block goes against the middle of one side, on the middle "
    "layer.",
    "@8 Fuel: deuterium (split from heavy water in the Electrolysis Cell), tritium vials, lithium and helium flasks. "
    "Each burn uses a deuterium flask and a tritium vial and lasts 10 minutes. The blankets turn one lithium into a new "
    "tritium vial each burn, so after the first vial from a fission reactor it breeds its own. One helium flask keeps the "
    "magnets cold for 4 burns.",
    "@8 To light the plasma the Tokamak first draws 20,000 kJ from the power blocks wired to it: dynamos, batteries or "
    "a fission reactor. Once lit it burns while it has fuel, and its vessels glow.",
]

for _code, _paras in GUIDE_I18N.items():
    assert len(_paras) == len(GUIDE), (_code, len(_paras), len(GUIDE))


def translations(code):
    return dict(TR.get(code, {}))


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _px(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), tuple(int(max(0, min(255, v))) for v in c) + (255,))


def _noise(seed, colour, var):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            d = rnd.randint(-var, var)
            _px(img, x, y, [c + d for c in colour])
    return img


def _frame(img, colour):
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            _px(img, x, y, colour)


def block_textures():
    out = {}
    coil = _noise(901, (150, 156, 168), 5)
    _frame(coil, (90, 96, 108))
    for y in range(3, 13):          # the D-shaped winding pack, copper-coloured
        for x in range(3, 13):
            if (x - 6) ** 2 / 16 + (y - 8) ** 2 / 25 <= 1.0 and x >= 4:
                _px(coil, x, y, (176, 104, 56) if (x + y) % 2 else (196, 120, 66))
    for y in range(5, 11):
        for x in range(6, 10):
            _px(coil, x, y, (60, 64, 72))
    out["toroidal_field_coil"] = coil
    sol = _noise(902, (120, 126, 138), 4)
    _frame(sol, (80, 84, 94))
    for y in range(1, 15, 2):
        for x in range(2, 14):
            _px(sol, x, y, (186, 112, 60))
    out["central_solenoid"] = sol
    for lit in (False, True):
        v = _noise(903, (78, 80, 86), 4)
        _frame(v, (120, 124, 132))
        for ty in (2, 9):
            for tx in (2, 9):
                for y in range(ty, ty + 5):
                    for x in range(tx, tx + 5):
                        if lit:
                            _px(v, x, y, (255, 140 + 10 * ((x + y) % 3), 235))   # pink-violet plasma light on the tiles
                        else:
                            _px(v, x, y, (52, 54, 60) if (x + y) % 4 else (150, 150, 160))   # tungsten tiles
        out["vacuum_vessel_lit" if lit else "vacuum_vessel"] = v
    bl = _noise(904, (170, 174, 182), 4)
    _frame(bl, (110, 114, 122))
    for y in range(3, 13):
        for x in range(3, 13):
            _px(bl, x, y, (214, 214, 220) if (x // 2 + y // 2) % 2 else (196, 196, 204))
    out["lithium_blanket"] = bl
    return out


P = "periodic_elements:"
RECIPES = {
    "toroidal_field_coil": (["WSW", "W W", "WSW"], {"W": P + "niobium_titanium_wire", "S": P + "stainless_steel_ingot"}, 4),
    "central_solenoid": (["WIW", "WIW", "WIW"], {"W": P + "niobium_titanium_wire", "I": "minecraft:iron_block"}, 1),
    "vacuum_vessel": (["BTB", "S S", "BTB"], {"B": P + "beryllium", "T": P + "tungsten", "S": P + "stainless_steel_ingot"}, 4),
    "lithium_blanket": (["SLS", "L L", "SLS"], {"L": P + "lithium", "S": P + "stainless_steel_ingot"}, 4),
}


def generate_fusion(assets, data_root, lang, mod):
    tex_block = assets / "textures" / "block"
    models = assets / "models"
    states = assets / "blockstates"
    for name, img in block_textures().items():
        img.save(tex_block / f"{name}.png")
    for b in PARTS:
        _dump(models / "block" / f"{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod}:block/{b}"}})
        _dump(models / "item" / f"{b}.json", {"parent": f"{mod}:block/{b}"})
        if b == "vacuum_vessel":
            _dump(models / "block" / "vacuum_vessel_lit.json", {"parent": "minecraft:block/cube_all",
                                                                "textures": {"all": f"{mod}:block/vacuum_vessel_lit"}})
            _dump(states / f"{b}.json", {"variants": {"lit=false": {"model": f"{mod}:block/{b}"},
                                                      "lit=true": {"model": f"{mod}:block/vacuum_vessel_lit"}}})
        else:
            _dump(states / f"{b}.json", {"variants": {"": {"model": f"{mod}:block/{b}"}}})
    data_dir = data_root / mod
    for b in PARTS:
        _dump(data_dir / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{b}"}],
             "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for rid, (pattern, key, count) in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                                    "key": {k: {"item": v} for k, v in key.items()},
                                                    "result": {"id": f"{mod}:{rid}", "count": count}})
    tag = data_root / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json"
    values = json.loads(tag.read_text(encoding="utf-8"))["values"] if tag.exists() else []
    values += [f"{mod}:{b}" for b in PARTS if f"{mod}:{b}" not in values]
    _dump(tag, {"replace": False, "values": values})
    lang.update(LANG)
