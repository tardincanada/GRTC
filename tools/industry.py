"""Industrial organics (1.9.0): textures, models, loot and tags for the level 5 organic chemistry blocks and items.

Blocks registered by Industry.java: the Demolition Charge, six colours of Bakelite and the invisible light a
Carbide Lamp leaves where its wearer stands. Items: Carbide Lamp, Welding Torch, PK Blend, NPK Prills, Conveyor
Belt and the four neoprene Wetsuit pieces (their names and tooltips are in machines.ITEMS).
"""
import json
import random

from PIL import Image

INDUSTRY_ITEMS = {"carbide_lamp", "welding_torch", "pk_blend", "npk_prills", "conveyor_belt",
                  "wetsuit_hood", "wetsuit_jacket", "wetsuit_leggings", "wetsuit_boots",
                  # 2.0.0: life sciences.
                  "foam_lagging", "sterile_air_filter", "spent_mycelium", "biomass", "ddt_duster", "weed_killer",
                  "pre_emergent", "field_clear", "prussian_blue_capsule", "mrna_vaccine", "tolerant_seeds"}
# Sprayers: id -> colour of the liquid in the can.
SPRAYERS = {"ddt_duster": (236, 236, 228), "weed_killer": (150, 190, 90), "pre_emergent": (200, 170, 110),
            "field_clear": (110, 150, 200)}
# 2.0.0 effects: id -> (name, icon colour).
EFFECTS = {"immunity": ("Immunity", (90, 200, 120)), "iodide_shield": ("Iodide Shield", (190, 120, 200)),
           "caesium_binder": ("Caesium Binder", (40, 70, 160)), "radioprotection": ("Radioprotection", (230, 200, 60))}
WETSUIT = ["wetsuit_hood", "wetsuit_jacket", "wetsuit_leggings", "wetsuit_boots"]
# Bakelite colours: id -> (name, colour). Brown is Bakelite as it comes from the press; the others are dyed.
BAKELITE = {
    "brown_bakelite": ("Bakelite", (92, 48, 24)),
    "black_bakelite": ("Black Bakelite", (28, 24, 22)),
    "red_bakelite": ("Red Bakelite", (150, 34, 26)),
    "green_bakelite": ("Green Bakelite", (40, 86, 52)),
    "ivory_bakelite": ("Ivory Bakelite", (232, 222, 194)),
    "amber_bakelite": ("Amber Bakelite", (204, 122, 30)),
}
INDUSTRY_BLOCKS = {
    "demolition_charge": ("Demolition Charge",
                          "Light it with flint and steel or redstone: breaks a 3x3x3 block of anything but bedrock, obsidian included"),
    **{b: (n, "Phenolic resin: hard, glossy and it never rots") for b, (n, _) in BAKELITE.items()},
    "insulation": ("Insulation", "Polystyrene foam: ice and snow next to it never melt"),
    "shaped_charge": ("Shaped Charge", "Place it on a wall and light it: bores a tunnel 16 blocks deep through anything but bedrock"),
    "anfo_charge": ("ANFO Charge", "Light it with flint and steel or redstone: breaks a 9x9x9 block of rock, three in four blocks drop"),
}


def light_marker_model(mod_id):
    """A single 2x2 px horizontal quad at the centre of the block, textured fully transparent (3.48.1, Fiat Lux P1)."""
    face = {"uv": [7, 7, 9, 9], "texture": "#marker"}
    return {"ambientocclusion": False,
            "textures": {"marker": f"{mod_id}:block/light_marker", "particle": f"{mod_id}:block/light_marker"},
            "elements": [{"from": [7, 8, 7], "to": [9, 8, 9], "shade": False, "faces": {"up": face}}]}


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _rect(img, x0, y0, x1, y1, color):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(color) + (255,) if len(color) == 3 else tuple(color)


def _noise(base, var, seed, size=(16, 16)):
    rnd = random.Random(seed)
    img = Image.new("RGBA", size)
    px = img.load()
    for y in range(size[1]):
        for x in range(size[0]):
            d = rnd.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


NEOPRENE = (30, 30, 34)
TRIM = (40, 110, 190)


def industry_item_texture(iid):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    if iid == "carbide_lamp":
        _rect(img, 5, 8, 10, 14, (200, 160, 70))      # brass carbide chamber
        _rect(img, 6, 9, 9, 13, (226, 186, 96))
        _rect(img, 4, 3, 11, 7, (210, 210, 200))       # reflector
        _rect(img, 6, 4, 9, 6, (250, 240, 170))        # flame
        _rect(img, 7, 5, 8, 5, (255, 255, 230))
        _rect(img, 11, 9, 12, 12, (150, 120, 50))      # hook
    elif iid == "welding_torch":
        for i in range(9):
            _rect(img, 3 + i, 12 - i, 4 + i, 13 - i, (200, 150, 60) if i < 5 else (150, 154, 160))
        _rect(img, 12, 2, 13, 3, (120, 200, 255))      # flame
        _rect(img, 14, 1, 14, 1, (220, 240, 255))
        _rect(img, 4, 9, 5, 10, (170, 40, 30))         # valves
        _rect(img, 6, 11, 7, 12, (40, 110, 60))
    elif iid in ("pk_blend", "npk_prills"):
        rnd = random.Random(1900 if iid == "pk_blend" else 1901)
        cols = [(214, 196, 200), (190, 170, 176), (240, 236, 236)] if iid == "pk_blend" else \
            [(250, 250, 248), (226, 230, 236), (210, 214, 222)]
        for _ in range(40 if iid == "pk_blend" else 18):
            x, y = rnd.randint(3, 12), rnd.randint(4, 13)
            if iid == "npk_prills":
                _rect(img, x, y, min(14, x + 1), min(14, y + 1), rnd.choice(cols))
            else:
                _rect(img, x, y, x, y, rnd.choice(cols))
    elif iid == "conveyor_belt":
        _rect(img, 1, 6, 14, 10, (36, 34, 32))         # rubber belt
        for x in range(2, 14, 3):
            _rect(img, x, 7, x, 9, (60, 58, 56))
        for x in (1, 13):
            _rect(img, x, 11, x + 1, 13, (150, 154, 160))  # rollers
        _rect(img, 7, 11, 8, 13, (150, 154, 160))
    elif iid == "wetsuit_hood":
        _rect(img, 3, 3, 12, 11, NEOPRENE)
        _rect(img, 5, 6, 10, 11, (0, 0, 0, 0))
        _rect(img, 3, 3, 12, 3, TRIM)
    elif iid == "wetsuit_jacket":
        _rect(img, 2, 2, 13, 6, NEOPRENE)
        _rect(img, 4, 7, 11, 14, NEOPRENE)
        _rect(img, 7, 2, 8, 14, TRIM)
    elif iid == "wetsuit_leggings":
        _rect(img, 3, 2, 12, 5, NEOPRENE)
        _rect(img, 3, 6, 6, 14, NEOPRENE)
        _rect(img, 9, 6, 12, 14, NEOPRENE)
        _rect(img, 3, 13, 6, 14, TRIM)
        _rect(img, 9, 13, 12, 14, TRIM)
    elif iid in SPRAYERS:
        _rect(img, 5, 6, 10, 14, (190, 196, 204))
        _rect(img, 6, 7, 9, 13, SPRAYERS[iid])
        _rect(img, 7, 3, 8, 5, (90, 94, 100))
        _rect(img, 9, 3, 12, 3, (90, 94, 100))
        _rect(img, 6, 11, 9, 11, (60, 60, 64))
    elif iid == "foam_lagging":
        _rect(img, 2, 3, 13, 12, (240, 240, 236))
        rnd = random.Random(2001)
        for _ in range(16):
            x, y = rnd.randint(2, 13), rnd.randint(3, 12)
            _rect(img, x, y, x, y, (214, 214, 210))
        _rect(img, 2, 12, 13, 13, (200, 200, 196))
    elif iid == "sterile_air_filter":
        _rect(img, 2, 2, 13, 13, (150, 154, 160))
        _rect(img, 3, 3, 12, 12, (246, 246, 240))
        for x in range(4, 12, 2):
            _rect(img, x, 3, x, 12, (220, 220, 214))
    elif iid in ("spent_mycelium", "biomass"):
        rnd = random.Random(2002 if iid == "biomass" else 2003)
        base = (120, 150, 80) if iid == "biomass" else (200, 190, 160)
        for _ in range(60):
            x, y = rnd.randint(3, 12), rnd.randint(5, 13)
            d = rnd.randint(-25, 25)
            _rect(img, x, y, x, y, tuple(max(0, min(255, c + d)) for c in base))
    elif iid == "prussian_blue_capsule":
        _rect(img, 4, 6, 7, 9, (40, 70, 160))
        _rect(img, 8, 6, 11, 9, (230, 230, 220))
        _rect(img, 5, 7, 6, 7, (90, 120, 210))
    elif iid == "mrna_vaccine":
        _rect(img, 7, 1, 8, 4, (150, 154, 160))      # needle
        _rect(img, 5, 5, 10, 13, (220, 236, 245))    # vial
        _rect(img, 6, 8, 9, 12, (230, 120, 140))
        _rect(img, 5, 5, 10, 5, (120, 124, 130))
    elif iid == "tolerant_seeds":
        rnd = random.Random(2004)
        for _ in range(10):
            x, y = rnd.randint(3, 11), rnd.randint(4, 11)
            _rect(img, x, y, x + 1, y + 1, (110, 170, 60))
            _rect(img, x, y, x, y, (60, 120, 200))
    elif iid == "wetsuit_boots":
        _rect(img, 2, 7, 6, 13, NEOPRENE)
        _rect(img, 9, 7, 13, 13, NEOPRENE)
        _rect(img, 1, 12, 6, 14, TRIM)
        _rect(img, 9, 12, 14, 14, TRIM)
    return img


def generate_industry(assets, data_root, lang, mod_id):
    tex = assets / "textures" / "block"
    models = assets / "models"
    states = assets / "blockstates"
    data_dir = data_root / mod_id
    m = lambda n: f"{mod_id}:block/{n}"

    # Demolition Charge: a grey steel case of cast TNT with a yellow band.
    side = _noise((120, 124, 130), 6, 1910)
    _rect(side, 0, 6, 15, 9, (230, 190, 60))
    for x in range(0, 16, 4):
        _rect(side, x, 7, x + 1, 8, (30, 30, 30))
    side.save(tex / "demolition_charge_side.png")
    top = _noise((120, 124, 130), 6, 1911)
    _rect(top, 6, 6, 9, 9, (230, 190, 60))
    _rect(top, 7, 7, 8, 8, (40, 36, 32))
    top.save(tex / "demolition_charge_top.png")
    _dump(models / "block" / "demolition_charge.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "side": m("demolition_charge_side"), "top": m("demolition_charge_top"), "bottom": m("demolition_charge_top")}})
    _dump(states / "demolition_charge.json", {"variants": {"": {"model": m("demolition_charge")}}})
    _dump(models / "item" / "demolition_charge.json", {"parent": m("demolition_charge")})

    # Bakelite: a moulded, polished slab with a light catch on the top edge.
    for i, (b, (_, col)) in enumerate(BAKELITE.items()):
        t = _noise(col, 4, 1920 + i)
        hi = tuple(min(255, c + 40) for c in col)
        lo = tuple(max(0, c - 30) for c in col)
        _rect(t, 0, 0, 15, 0, hi)
        _rect(t, 0, 0, 0, 15, hi)
        _rect(t, 0, 15, 15, 15, lo)
        _rect(t, 15, 0, 15, 15, lo)
        _rect(t, 3, 3, 5, 3, hi)
        t.save(tex / f"{b}.png")
        _dump(models / "block" / f"{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m(b)}})
        _dump(states / f"{b}.json", {"variants": {"": {"model": m(b)}}})
        _dump(models / "item" / f"{b}.json", {"parent": m(b)})

    # 2.0.0: Insulation (white foam), Shaped Charge (a copper cone on a steel can, facing out of its wall), ANFO Charge.
    t = _noise((238, 238, 232), 6, 2010)
    rnd = random.Random(2011)
    for _ in range(30):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        _rect(t, x, y, x, y, (216, 216, 210))
    t.save(tex / "insulation.png")
    _dump(models / "block" / "insulation.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m("insulation")}})
    _dump(states / "insulation.json", {"variants": {"": {"model": m("insulation")}}})
    _dump(models / "item" / "insulation.json", {"parent": m("insulation")})
    side = _noise((120, 124, 130), 6, 2012)
    _rect(side, 0, 0, 15, 2, (200, 120, 60))
    _rect(side, 7, 6, 8, 9, (230, 190, 60))
    side.save(tex / "shaped_charge_side.png")
    front = _noise((120, 124, 130), 6, 2013)
    for r in range(7, 0, -2):
        _rect(front, 8 - r, 8 - r, 7 + r, 7 + r, (200, 120, 60) if r % 4 == 3 else (170, 96, 46))
    front.save(tex / "shaped_charge_front.png")
    back = _noise((120, 124, 130), 6, 2014)
    _rect(back, 7, 7, 8, 8, (40, 36, 32))
    back.save(tex / "shaped_charge_back.png")
    _dump(models / "block" / "shaped_charge.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "front": m("shaped_charge_front"), "side": m("shaped_charge_side"), "top": m("shaped_charge_side"),
        "bottom": m("shaped_charge_back")}})
    # The front (the copper cone) faces into the wall the charge was put against.
    rot = {"north": (0, 0), "east": (0, 90), "south": (0, 180), "west": (0, 270), "up": (270, 0), "down": (90, 0)}
    _dump(states / "shaped_charge.json", {"variants": {
        f"facing={f},lit={l}": dict({"model": m("shaped_charge")}, **({"x": x} if x else {}), **({"y": y} if y else {}))
        for f, (x, y) in rot.items() for l in ("false", "true")}})
    _dump(models / "item" / "shaped_charge.json", {"parent": m("shaped_charge")})
    side = _noise((210, 200, 180), 8, 2015)
    for y0 in (3, 11):
        _rect(side, 0, y0, 15, y0 + 1, (140, 110, 70))
    _rect(side, 5, 6, 10, 8, (200, 40, 30))
    side.save(tex / "anfo_charge_side.png")
    top = _noise((210, 200, 180), 8, 2016)
    _rect(top, 7, 7, 8, 8, (40, 36, 32))
    top.save(tex / "anfo_charge_top.png")
    _dump(models / "block" / "anfo_charge.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "side": m("anfo_charge_side"), "top": m("anfo_charge_top"), "bottom": m("anfo_charge_top")}})
    _dump(states / "anfo_charge.json", {"variants": {"": {"model": m("anfo_charge")}}})
    _dump(models / "item" / "anfo_charge.json", {"parent": m("anfo_charge")})
    eff = assets / "textures" / "mob_effect"
    eff.mkdir(parents=True, exist_ok=True)
    for e, (_, col) in EFFECTS.items():
        icon = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
        px = icon.load()
        for y in range(18):
            for x in range(18):
                d = ((x - 8.5) ** 2 + (y - 8.5) ** 2) ** 0.5
                if d <= 7:
                    px[x, y] = (255, 255, 255, 255) if d > 6 else col + (230,)
        _rect(icon, 8, 4, 9, 13, (255, 255, 255))
        _rect(icon, 4, 8, 13, 9, (255, 255, 255))
        icon.save(eff / f"{e}.png")

    # The Carbide Lamp's light (3.48.1): one 2x2 px quad with a fully transparent texture, drawn in the cutout layer,
    # so the player sees nothing but shaders that build their light map from block geometry (Fiat Lux) find it.
    Image.new("RGBA", (16, 16), (0, 0, 0, 0)).save(tex / "light_marker.png")
    _dump(models / "block" / "lamp_light.json", light_marker_model(mod_id))
    _dump(states / "lamp_light.json", {"variants": {"": {"model": m("lamp_light")}}})

    # Wetsuit layers: black neoprene with blue seams (layer 1: hood, jacket, boots; layer 2: leggings).
    arm = assets / "textures" / "models" / "armor"
    arm.mkdir(parents=True, exist_ok=True)
    for n in (1, 2):
        layer = _noise(NEOPRENE, 5, 1930 + n, (64, 32))
        for x in range(0, 64, 8):
            _rect(layer, x, 20, x, 31, TRIM)
        layer.save(arm / f"wetsuit_layer_{n}.png")

    loot = data_dir / "loot_table" / "blocks"
    for b in ["demolition_charge", "insulation", "shaped_charge", "anfo_charge"] + list(BAKELITE):
        _dump(loot / f"{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": f"{mod_id}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for (kind, tag), ids in {("block", "mineable/pickaxe"): list(BAKELITE),
                             ("item", "freeze_immune_wearables"): WETSUIT,
                             ("item", "head_armor"): ["wetsuit_hood"], ("item", "chest_armor"): ["wetsuit_jacket"],
                             ("item", "leg_armor"): ["wetsuit_leggings"], ("item", "foot_armor"): ["wetsuit_boots"]}.items():
        path = data_root / "minecraft" / "tags" / kind / f"{tag}.json"
        values = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
        values += [f"{mod_id}:{i}" for i in ids if f"{mod_id}:{i}" not in values]
        _dump(path, {"replace": False, "values": values})

    for b, (name, desc) in INDUSTRY_BLOCKS.items():
        lang[f"block.{mod_id}.{b}"] = name
        lang[f"block.{mod_id}.{b}.desc"] = desc
    lang[f"block.{mod_id}.lamp_light"] = "Carbide Lamp Light"
    lang.update({
        f"gui.{mod_id}.lamp.refilled": "Fresh carbide in the lamp",
        f"gui.{mod_id}.lamp.empty": "The lamp is out of carbide: use it with calcium carbide in your pack",
        f"gui.{mod_id}.lamp.full": "The lamp is still full",
        f"gui.{mod_id}.weld.mended": "Welded: %s mended with %s",
        f"gui.{mod_id}.weld.nothing": "Hold a damaged metal tool or armour in your other hand",
        f"gui.{mod_id}.weld.no_ingot": "You need an ingot of its metal in your pack to weld this",
        f"gui.{mod_id}.weld.empty": "The torch's cylinders are empty: craft it with fresh acetylene and oxygen",
        f"gui.{mod_id}.sulfa": "You take a sulfa drug: the infection is beaten",
        f"gui.{mod_id}.fertilized.npk": "NPK prills: crops grow twice as fast for %s harvests",
        f"gui.{mod_id}.charge.demolition": "The demolition fuse is lit: stand clear!",
        f"gui.{mod_id}.antibiotic": "You take the antibiotic: the infection is beaten, and you are immune for 20 minutes",
        f"gui.{mod_id}.vaccinated": "Vaccinated: immune to Infection for seven days",
        f"gui.{mod_id}.iodide": "You take a potassium iodide tablet: radioiodine passes you by for a day",
        f"gui.{mod_id}.capsule": "You swallow the Prussian blue: it will bind radiocaesium for a day",
        f"gui.{mod_id}.amifostine": "Amifostine: for an hour radiation does half the harm",
        f"gui.{mod_id}.zombie_cure": "The penicillin takes hold: the villager's cure has begun",
        f"gui.{mod_id}.zombie_cure.weakness": "Weaken the zombie villager first (a splash of Weakness)",
        f"gui.{mod_id}.dusted": "Dusted with DDT for three days",
        f"gui.{mod_id}.weeded": "Weed Killer: %s plants killed",
        f"gui.{mod_id}.pre_emergent": "Treated: no grass will spread here for ten days",
        f"gui.{mod_id}.field_clear": "Field Clear: %s plants killed",
        f"gui.{mod_id}.tolerant": "Tolerant wheat planted",
        f"gui.{mod_id}.status.needs_filter": "Needs a Sterile Air Filter",
        **{f"effect.{mod_id}.{e}": n for e, (n, _) in EFFECTS.items()},
    })
