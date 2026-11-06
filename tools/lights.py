"""Light sources for shaders (3.49.1, Fiat Lux request P2): periodic_elements/lights.json.

One entry per block (with the state in which it shines) or item that gives light, and the kind of light, so a shader
such as Fiat Lux can colour it. Machines get their kind from their temperature and how they are heated, so a new
machine is listed without anyone remembering to; the gametest lightsJsonListsEveryLuminousBlock fails if a block
that gives light is missing. Never rename a block listed here: shaders match it by name.
"""
import csv
import json

from machines import ELECTRIC, POWER

# Kinds the shader already knows (P2): sodium_low_pressure, led_cool_white, carbide_flame, tritium, incandescent,
# generic (torch colour). New kinds asked of the shader, with the colour we suggest (sRGB 0-255).
KINDS = {
    "sodium_low_pressure": (None, "Low-pressure sodium lamp, monochrome yellow (shader's own colour)"),
    "led_cool_white": (None, "White LED, cold (shader's own colour)"),
    "carbide_flame": (None, "Acetylene flame of a carbide lamp (shader's own colour)"),
    "tritium": (None, "Tritium vial, pale green phosphor (shader's own colour)"),
    "incandescent": (None, "Tungsten filament, warm (shader's own colour)"),
    "generic": (None, "Torch colour: open wood, charcoal or tallow fire (shader's own colour)"),
    "ember_glow": ((255, 96, 32), "NEW: dull red heat, smouldering charcoal or vessels at 600-900 °C"),
    "furnace_glow": ((255, 150, 60), "NEW: orange heat of a furnace or molten charge at 900-1500 °C"),
    "white_hot": ((255, 226, 170), "NEW: yellow-white heat at 1500 °C or more (blast furnace, Bessemer, molten steel)"),
    "electric_arc": ((205, 220, 255), "NEW: blue-white electric arc, flickers"),
    "bunsen_flame": ((90, 140, 255), "NEW: small blue gas flame (spectroscope's Bunsen burner)"),
    "ion_beam": ((170, 110, 255), "NEW: violet glow of an ion beam in a vacuum (cyclotron, calutron, accelerator)"),
    "cherenkov": ((80, 170, 255), "NEW: Cherenkov blue (reactor pool, gamma irradiator)"),
    "cleanroom_yellow": ((255, 205, 70), "NEW: yellow safelight of a photolithography clean room"),
    "indicator_lamp": ((255, 185, 90), "NEW: small amber panel lamps of an electric machine; no flame, no heat glow"),
}

# Machines whose light the temperature rule gets wrong.
MACHINE_KIND = {
    "electric_arc_furnace": "electric_arc",
    "spectroscope_bench": "bunsen_flame",
    "cyclotron": "ion_beam", "calutron": "ion_beam", "heavy_ion_accelerator": "ion_beam",
    "chip_fab": "cleanroom_yellow",
    "van_arkel_reactor": "incandescent",  # a glowing tungsten wire
    "hot_cell": "incandescent",  # lamps behind lead glass
    "refinery_still": "generic", "cracking_unit": "generic",  # oil-fired furnaces under the still
}

POWER_KIND = {"steam_engine": "generic", "nuclear_reactor": "cherenkov", "radioisotope_generator": "ember_glow"}

# Every other block or item that gives light.
OTHER = [
    {"id": "sodium_lamp", "state": "lit=true", "kind": "sodium_low_pressure", "light": 15},
    {"id": "led_lamp", "state": "lit=true", "kind": "led_cool_white", "light": 15},
    {"id": "explorer_light", "state": "kind=cap", "kind": "incandescent", "light": 15,
     "note": "Invisible light of a worn Electric Cap Lamp"},
    {"id": "explorer_light", "state": "kind=led", "kind": "led_cool_white", "light": 15,
     "note": "Invisible light of a worn LED Spotlight Helmet, also where its beam lands"},
    {"id": "lamp_light", "kind": "carbide_flame", "light": 14, "note": "Invisible light of a held Carbide Lamp"},
    {"id": "tritium_light", "kind": "tritium", "light": 12},
    {"id": "tallow_candle", "kind": "generic", "light": 12},
    {"id": "greenhouse_heater", "state": "lit=true", "kind": "generic", "light": 9, "note": "Brick stove burning fuel"},
    {"id": "gamma_garden", "kind": "cherenkov", "light": 4, "note": "Cobalt-60 gamma irradiator"},
    # Items that light the way when held or worn (for shaders' held-item light).
    {"id": "carbide_lamp", "item": True, "kind": "carbide_flame", "light": 14, "note": "Lit only while it has carbide"},
    {"id": "electric_cap_lamp", "item": True, "kind": "incandescent", "light": 15, "note": "Worn, needs a charged battery"},
    {"id": "led_spotlight_helmet", "item": True, "kind": "led_cool_white", "light": 15, "note": "Worn, needs a charged battery"},
    {"id": "tritium_light", "item": True, "kind": "tritium", "light": 12},
    {"id": "tallow_candle", "item": True, "kind": "generic", "light": 12},
    {"id": "sodium_lamp", "item": True, "kind": "sodium_low_pressure", "light": 15, "note": "Only lit when placed and powered"},
    {"id": "led_lamp", "item": True, "kind": "led_cool_white", "light": 15, "note": "Only lit when placed and powered"},
]


def machine_kind(mid, level, max_c, electric):
    if mid in MACHINE_KIND:
        return MACHINE_KIND[mid]
    if max_c >= 1500:
        return "white_hot"
    if max_c >= 900:
        return "furnace_glow"
    if electric or level >= 4:
        return "indicator_lamp"
    if max_c >= 600:
        return "ember_glow"
    return "generic"


def generate_lights(root, runtime_dir, mod_id):
    entries = []
    with open(root / "data" / "machines.csv", newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            kind = machine_kind(r["id"], int(r["level"]), float(r["max_c"]), r["id"] in ELECTRIC)
            entries.append({"id": f"{mod_id}:{r['id']}", "state": "lit=true", "kind": kind, "light": 13,
                            "note": f"Machine, up to {int(float(r['max_c']))} °C"})
    for pid in POWER:
        entries.append({"id": f"{mod_id}:{pid}", "state": "lit=true", "kind": POWER_KIND.get(pid, "indicator_lamp"),
                        "light": 7, "note": "Power block"})
    for e in OTHER:
        entries.append({**e, "id": f"{mod_id}:{e['id']}"})
    for e in entries:
        assert e["kind"] in KINDS, e
    kinds = {k: ({"rgb": list(rgb)} if rgb else {}) | {"note": note} for k, (rgb, note) in KINDS.items()}
    (runtime_dir / "lights.json").write_text(json.dumps({
        "_comment": "Light sources of Elementar for shaders (Fiat Lux P2). Generated by tools/lights.py. "
                    "state: the block state in which it shines (absent = always). item: true = the item, held or worn. "
                    "light: vanilla light level. Never rename these ids.",
        "kinds": kinds, "lights": entries}, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    return len(entries)
