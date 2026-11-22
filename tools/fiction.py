"""Level 10, stage 6 (3.67.0): the materials of science fiction. The Magnetic Shield (room-temperature superconductor),
Unbihexium and Stability Alloy (the island of stability), the Thermal Suit, three engines (metallic hydrogen,
antimatter, D-He3 fusion; their textures and recipes are in space.py ENGINES) and the Annihilation Reactor (a power
block in machines.POWER). The research projects are rows of data/research.csv, the substances and reactions rows of
substances.csv and reactions.csv; every one of them is marked as fiction, and the guide says where the science stops.

Java: com.periodicelements.space.Fiction (shield, suit, alloy), Propellant (METALLIC, ANTIMATTER, FUSION),
PowerBlockEntity.tickAnnihilation, client/SpaceClient (the fiction line on tooltips). space.py calls generate_fiction
and merges LANG, the guide paragraphs (appended to the Space chapter) and translations(code).
"""
import json
import random

from PIL import Image

import moon
from fiction_i18n import TR, GUIDE_I18N

BLOCKS = ["magnetic_shield"]
ITEMS = ["unbihexium", "stability_alloy_ingot"]
SUIT = ["thermal_suit_helmet", "thermal_suit_chestplate", "thermal_suit_leggings", "thermal_suit_boots"]
ENGINES = ["metallic_hydrogen_engine", "antimatter_engine", "fusion_drive"]
ITEM_UNLOCKS = set(BLOCKS) | set(ITEMS) | set(SUIT) | set(ENGINES)

M = "periodic_elements"
LANG = {
    f"tooltip.{M}.fiction": "✦ Science fiction",
    f"block.{M}.magnetic_shield": "Magnetic Shield",
    f"block.{M}.magnetic_shield.desc": "A coil of warm superconductor: its field turns cosmic rays aside within 16 blocks, with no power",
    f"item.{M}.unbihexium": "Unbihexium",
    f"item.{M}.unbihexium.tip": "Element 126, stable on the island of stability",
    f"item.{M}.stability_alloy_ingot": "Stability Alloy Ingot",
    f"item.{M}.stability_alloy_ingot.tip": "Element-126 alloy that stays strong at 465 °C and 92 atmospheres",
    f"item.{M}.thermal_suit_helmet": "Thermal Suit Helmet",
    f"item.{M}.thermal_suit_helmet.tip": "A heat-proof visor: on Mercury the Sun is seven times as bright",
    f"item.{M}.thermal_suit_chestplate": "Thermal Suit",
    f"item.{M}.thermal_suit_chestplate.tip": "Works as a Space Suit (fill it with an oxygen cylinder) and keeps out fire and hot ground",
    f"item.{M}.thermal_suit_leggings": "Thermal Suit Trousers",
    f"item.{M}.thermal_suit_leggings.tip": "Plates of Stability Alloy over the Space Suit's layers",
    f"item.{M}.thermal_suit_boots": "Thermal Suit Boots",
    f"item.{M}.thermal_suit_boots.tip": "All four pieces: a space suit that walks on ground at 430 °C",
    f"block.{M}.metallic_hydrogen_engine": "Metallic Hydrogen Engine",
    f"block.{M}.metallic_hydrogen_engine.desc": "Metallic hydrogen cooled with liquid hydrogen (Isp 1100 s); lifts up to 6 Propellant Tanks",
    f"block.{M}.antimatter_engine": "Antimatter Engine",
    f"block.{M}.antimatter_engine.desc": "A little antihydrogen heats liquid hydrogen (Isp 2500 s); lifts up to 6 Propellant Tanks",
    f"block.{M}.fusion_drive": "Fusion Drive",
    f"block.{M}.fusion_drive.desc": "Fuses helium-3 with deuterium (Isp 10,000 s); too gentle to lift off, so upper stages only; up to 3 tanks",
    f"message.{M}.rocket.no_liftoff": "%s pushes too gently to lift a rocket off the ground: put it in an upper stage",
}

GUIDE = [
    "@10 # Where science stops",
    "@10 Level 10, the Inner Planets, is researched at the Frontier Science Laboratory, made from Mission Control. From "
    "here on the game goes past what anyone can build. Each material below starts from a real idea and goes one step "
    "further; each says so on its tooltip (✦ Science fiction), and this chapter tells where the science stops.",
    "@10 # Room-temperature superconductor",
    "@10 Real: a superconductor carries current with no loss at all, but only far below freezing, as the Tokamak's "
    "coils do in liquid helium. In 2023 a team claimed lutetium hydride with a little nitrogen did it at room "
    "temperature; the paper was retracted. Fiction: here it works. The High Pressure Reactor makes it from lutetium and "
    "hydrogen at 10,000 atm.",
    "@10 The Magnetic Shield is a coil of it. Its field turns charged particles aside as the Earth's own field does: "
    "within 16 blocks the cosmic dose on the Moon and Mars falls to a tenth. It needs no power, because a current in a "
    "superconductor never stops, and it glows faintly where the particles strike the field.",
    "@10 # The island of stability",
    "@10 Real: past uranium each element falls apart faster, but since 1966 theory has predicted an island of "
    "longer-lived nuclei near 114 to 126 protons. Nobody has reached it; the heaviest elements ever made last "
    "milliseconds. Fiction: here element 126, unbihexium, is stable. The Heavy-Ion Accelerator makes it from curium "
    "struck by chromium.",
    "@10 Crafted with tungsten and titanium alloy it gives Stability Alloy, which stays strong at 465 °C under 92 "
    "atmospheres, the surface of Venus. Six ingots round a piece of the Space Suit make a piece of the Thermal Suit: "
    "it works as a space suit, is far tougher, and keeps out fire and hot ground, though not lava.",
    "@10 # Metallic hydrogen",
    "@10 Real: crushed at millions of atmospheres, hydrogen should turn into a metal (Wigner and Huntington 1935), and "
    "it probably fills the core of Jupiter. A 2017 claim to have made it is disputed, and it would turn back to gas "
    "as soon as the pressure dropped. Fiction: here it stays metal, made in the High Pressure Reactor from liquid "
    "hydrogen.",
    "@10 Turning back into gas it gives far more energy than any burning fuel. The Metallic Hydrogen Engine (Isp "
    "1100 s) burns it with liquid hydrogen to keep the chamber whole: a tank holds 10 metallic and 4 liquid hydrogen, "
    "and two stages of it go far past any chemical rocket.",
    "@10 # Antimatter",
    "@10 Real: when matter meets antimatter all of the mass becomes energy, a thousand times what fission gives. CERN's "
    "ALPHA held a few hundred atoms of antihydrogen in a magnetic trap in 2010. Fiction: here the Heavy-Ion Accelerator "
    "fills whole flasks, one for every hundred of hydrogen fed in.",
    "@10 The Annihilation Reactor meets a flask of antihydrogen with a flask of hydrogen: 1000 kJ per tick for a whole "
    "day, four times a Tokamak, and it needs no air. The Antimatter Engine heats liquid hydrogen with a little "
    "antihydrogen (Isp 2500 s; a tank holds 1 antihydrogen and 20 liquid hydrogen).",
    "@10 # Fusion propulsion",
    "@10 Real: Project Daedalus (1978) drew a starship pushed by fusing deuterium with helium-3, which gives off almost "
    "no neutrons; no fusion engine has ever been built. Fiction: the Fusion Drive is a compact tokamak with warm "
    "superconducting coils. Fed helium-3 from the Moon and deuterium, it reaches Isp 10,000 s, but it pushes too "
    "gently to lift off: put it in an upper stage, over chemical or metallic hydrogen stages.",
    "@10 These materials and engines are what will reach the worlds real rockets cannot: Mercury and Venus come next.",
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


def shield_texture():
    """Dark steel frame, violet windings of superconductor, a faint green glow where the field turns particles."""
    img = _noise(3001, (52, 50, 64), 4)
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            _px(img, x, y, (96, 96, 110))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if 4.2 <= d <= 6.4:
                _px(img, x, y, (150, 100, 230) if (x + y) % 2 else (120, 80, 200))   # the coil
            elif d < 2.6:
                _px(img, x, y, (140, 255, 190))   # the aurora glow at the centre
            elif d < 4.2:
                _px(img, x, y, (70, 150, 120))
    return img


def reactor_textures():
    """The Annihilation Reactor: a dark casing round a ring-shaped Penning trap; lit, a hard blue-white core."""
    out = {}
    side = _noise(3002, (44, 44, 56), 4)
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            _px(side, x, y, (150, 156, 172))
    for x in range(2, 14, 4):
        _rect(side, x, 3, x + 1, 12, (70, 72, 90))   # cooling fins
    out["annihilation_reactor_side"] = side
    top = side.copy()
    for y in range(16):
        for x in range(16):
            if 4.5 <= ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5 <= 6:
                _px(top, x, y, (150, 100, 230))
    out["annihilation_reactor_top"] = top
    for lit in (False, True):
        f = side.copy()
        _rect(f, 2, 2, 13, 13, (20, 20, 28))
        for y in range(16):
            for x in range(16):
                d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
                if 4.2 <= d <= 5.6:
                    _px(f, x, y, (170, 120, 250) if lit else (90, 70, 130))   # the trap's ring
                elif d < 4.2 and lit:
                    k = max(0.0, 1 - d / 4.2)
                    _px(f, x, y, (200 + 55 * k, 210 + 45 * k, 255))   # matter meeting antimatter
                elif d < 4.2:
                    _px(f, x, y, (30, 30, 40))
        out["annihilation_reactor_front_on" if lit else "annihilation_reactor_front"] = f
    return out


def unbihexium_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(3, 13):
        for x in range(3, 13):
            if (x - 7.5) ** 2 / 22 + (y - 8) ** 2 / 18 <= 1:
                shade = 1.15 - 0.07 * ((x - 4) + (y - 4)) / 2
                _px(img, x, y, [c * shade for c in (200, 196, 220)])
    for (x, y) in ((5, 6), (6, 5), (9, 9), (10, 8)):
        _px(img, x, y, (255, 255, 255))
    _px(img, 7, 8, (190, 130, 255))   # a violet glint: fiction
    _px(img, 8, 8, (190, 130, 255))
    return img


def alloy_ingot_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(5, 12):
        inset = (11 - y) // 2 if y < 8 else 0
        for x in range(2 + inset, 14 - inset):
            top = y <= 7
            _px(img, x, y, (176, 196, 214) if top else (120, 136, 160))
    for x in range(4, 12):
        _px(img, x, 5, (226, 236, 248))
    _px(img, 6, 9, (190, 130, 255))
    _px(img, 9, 10, (190, 130, 255))
    return img


ALLOY = (150, 162, 184)
VISOR = (150, 90, 230)


def _recolour(img):
    """The Space Suit's white made Stability Alloy; its gold visor made violet."""
    out = img.copy()
    w, h = out.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = out.getpixel((x, y))
            if a == 0:
                continue
            if r > 180 and g > 180 and b > 170:   # white cloth
                k = (r + g + b) / (3 * 236)
                out.putpixel((x, y), tuple(int(min(255, c * k)) for c in ALLOY) + (a,))
            elif r > 190 and g > 140 and b < 130:   # gold visor
                out.putpixel((x, y), VISOR + (a,))
    return out


def suit_texture(iid):
    return _recolour(moon.item_texture(iid.replace("thermal_suit", "space_suit")))


def armor_layer(n):
    layer = _recolour(moon.armor_layer(n))
    for x in range(0, 64, 4):
        _rect(layer, x, 16, x, 16, (110, 120, 140))   # plate seams
    return layer


P = "periodic_elements:"
RECIPES = {
    "magnetic_shield": (["SPS", "PCP", "SPS"], {"S": P + "stainless_steel_ingot", "P": P + "room_temperature_superconductor",
                                                "C": P + "integrated_circuit"}, 1),
    "thermal_suit_helmet": (["AAA", "AHA"], {"A": P + "stability_alloy_ingot", "H": P + "space_suit_helmet"}, 1),
    "thermal_suit_chestplate": (["A A", "ASA", "AAA"], {"A": P + "stability_alloy_ingot", "S": P + "space_suit_chestplate"}, 1),
    "thermal_suit_leggings": (["AAA", "ALA", "A A"], {"A": P + "stability_alloy_ingot", "L": P + "space_suit_leggings"}, 1),
    "thermal_suit_boots": (["ABA", "A A", "A A"], {"A": P + "stability_alloy_ingot", "B": P + "space_suit_boots"}, 1),
}


def generate_fiction(assets, data_root, lang, mod):
    tex_block = assets / "textures" / "block"
    tex_item = assets / "textures" / "item"
    models = assets / "models"
    states = assets / "blockstates"
    shield_texture().save(tex_block / "magnetic_shield.png")
    for name, img in reactor_textures().items():   # over the generic power-block textures from machines.py
        img.save(tex_block / f"{name}.png")
    _dump(models / "block" / "magnetic_shield.json", {"parent": "minecraft:block/cube_all",
                                                       "textures": {"all": f"{mod}:block/magnetic_shield"}})
    _dump(models / "item" / "magnetic_shield.json", {"parent": f"{mod}:block/magnetic_shield"})
    _dump(states / "magnetic_shield.json", {"variants": {"": {"model": f"{mod}:block/magnetic_shield"}}})
    textures = {"unbihexium": unbihexium_texture(), "stability_alloy_ingot": alloy_ingot_texture(),
                **{s: suit_texture(s) for s in SUIT}}
    for iid, img in textures.items():
        img.save(tex_item / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{iid}"}})
    arm = assets / "textures" / "models" / "armor"
    arm.mkdir(parents=True, exist_ok=True)
    for n in (1, 2):
        armor_layer(n).save(arm / f"thermal_suit_layer_{n}.png")

    data_dir = data_root / mod
    _dump(data_dir / "loot_table" / "blocks" / "magnetic_shield.json", {"type": "minecraft:block", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:magnetic_shield"}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for rid, (pattern, key, count) in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                                    "key": {k: {"item": v} for k, v in key.items()},
                                                    "result": {"id": f"{mod}:{rid}", "count": count}})
    # the alloy: element 126 with tungsten and titanium alloy
    _dump(data_dir / "recipe" / "stability_alloy_ingot.json", {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"item": f"{mod}:unbihexium"}, {"item": f"{mod}:tungsten"}, {"item": f"{mod}:titanium_alloy_ingot"}],
        "result": {"id": f"{mod}:stability_alloy_ingot", "count": 2}})
    tag = data_root / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json"
    values = json.loads(tag.read_text(encoding="utf-8"))["values"] if tag.exists() else []
    if f"{mod}:magnetic_shield" not in values:
        values.append(f"{mod}:magnetic_shield")
    _dump(tag, {"replace": False, "values": values})
    lang.update(LANG)
