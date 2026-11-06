"""Animated textures for machines while they run (3.33.0).

Each machine shows an animation while a reaction runs (block state active=true). The look depends on the process:
`mode` (0..15) picks one of the machine's animated models. Processes that look alike share a mode, so a machine
with 41 reactions still has only a handful of looks.

How a look is chosen:
  * KIND: what you see through the machine's window - flames, a melt, bubbles, drips from a still, crystals,
    gas flow, an electric arc, a particle beam... Every machine has a default kind (MACHINE_KIND); a reaction can
    override it (REACTION_KIND), e.g. the Enrichment Cascade shows liquid for heavy water and gas for UF6.
  * COLOUR: the colour of the reaction's main product (substances.csv / elements.csv colours, or the average of the
    item's texture), so copper glows copper-red and bromine fumes brown.
  * Flames also take a tint from the reaction's top temperature (red -> orange -> yellow -> white).

Output, written by machines.write_assets: textures/block/<id>_front_active[_<m>].png and _top_active[_<m>].png
(vertical frame strips + .png.mcmeta), models <id>_active[_<m>].json, blockstate keys active=true,mode=<m>,
and periodic_elements/animations.json (machine -> reaction -> mode) for MachineAnimations.java.

Hand-drawn machines (charcoal pit, pottery kiln, enrichment cascade: 3.28.x) keep their own mode-0 animations.
"""
import csv
import colorsys
import math
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
FRAMES = 8
MAX_MODES = 16

# Window on the front face (x0, y0, x1, y1, inclusive) where the process shows, and the area of the top face that
# animates. Generic machines use the firebox opening 5..10 x 8..14 and the top centre 5..10.
FRONT_WINDOW = {"lime_kiln": (4, 7, 11, 14), "bloomery": (6, 10, 9, 14), "charcoal_pit": (5, 9, 10, 14)}
TOP_WINDOW = {"lime_kiln": (4, 4, 11, 11), "charcoal_pit": (6, 6, 9, 9)}
DEFAULT_FRONT = (5, 8, 10, 14)
DEFAULT_TOP = (5, 5, 10, 10)

MACHINE_KIND = {
    # Level 0-1
    "charcoal_pit": "embers", "lime_kiln": "flame", "smelting_furnace": "flame", "bloomery": "flame",
    "crucible": "melt", "cupel": "melt", "pottery_kiln": "flame", "salt_pan": "crystals", "desalination_still": "distill",
    "fermentation_barrel": "ferment", "vinegar_vat": "ferment", "glass_furnace": "melt", "soap_kettle": "boil",
    "oil_press": "press", "tanning_pit": "stir",
    # Level 2
    "alembic": "distill", "crystallizer": "crystals", "charcoal_blast_furnace": "melt", "cementation_furnace": "flame",
    "zinc_retort": "distill", "amalgamation_pan": "stir", "phosphorus_retort": "distill",
    "reverberatory_furnace": "flame", "porcelain_kiln": "flame", "dye_vat": "stir",
    # Level 3
    "coke_oven": "embers", "huntsman_furnace": "melt", "pneumatic_trough": "gasjar", "electrolysis_cell": "electro",
    "digestion_vat": "boil", "reduction_crucible": "melt", "lead_chamber": "gas", "vulcanizer": "press",
    "tar_still": "distill",
    # Level 4
    "bessemer_converter": "blow", "bayer_digester": "boil", "hall_heroult_cell": "electro",
    "chlor_alkali_cell": "electro", "solvay_tower": "gasjar", "contact_converter": "gas", "air_liquefier": "frost",
    "spectroscope_bench": "spectrum", "electrolytic_refinery": "electro", "mond_reactor": "gas",
    "moissan_cell": "electro", "refinery_still": "distill", "autoclave": "boil", "air_compressor": "press",
    # Level 5
    "electric_arc_furnace": "arc", "high_pressure_reactor": "gas", "ostwald_converter": "gauze",
    "flotation_cell": "froth", "sintering_furnace": "glow", "fractional_crystallizer": "crystals",
    "van_arkel_reactor": "filament", "cracking_unit": "distill", "prilling_tower": "drip",
    # Level 6
    "chlorinator": "gas", "kroll_reactor": "melt", "ion_exchange_column": "drip", "crystal_puller": "spin",
    "zone_refiner": "zone", "diamond_press": "press", "enrichment_cascade": "gas", "hot_cell": "glow",
    "cyclotron": "beam", "deep_tank_fermenter": "ferment",
    # Level 7
    "calutron": "beam", "chip_fab": "scan", "heavy_ion_accelerator": "beam", "bioreactor": "ferment",
}

# Processes that look unlike their machine's usual work.
REACTION_KIND = {
    ("enrichment_cascade", "heavy_water_enrichment"): "boil",
    ("alembic", "gold_dissolution"): "boil", ("alembic", "platinum_dissolution"): "boil",
    ("crystallizer", "saltpeter_leaching"): "crystals",
    ("electric_arc_furnace", "*"): "arc",
}

# Processes that are only the machine warming up: they show the machine's first look.
TRIVIAL = {"wood_drying"}

VANILLA_COLOURS = {
    "charcoal": (60, 50, 44), "coal": (40, 40, 40), "copper_ingot": (226, 120, 80), "iron_ingot": (216, 216, 216),
    "gold_ingot": (250, 214, 60), "glass": (200, 230, 236), "brick": (170, 86, 60), "netherite_ingot": (70, 60, 64),
    "diamond": (100, 230, 220), "quartz": (236, 230, 220), "redstone": (220, 30, 20), "slime_ball": (110, 200, 90),
    "terracotta": (160, 90, 66), "leather": (160, 100, 60), "lapis_lazuli": (40, 70, 180), "emerald": (40, 200, 90),
    "sugar": (240, 240, 240), "string": (240, 240, 240), "bone_meal": (230, 230, 220), "gunpowder": (90, 90, 90),
}


def _hex(h):
    h = (h or "").strip().lstrip("#")
    if len(h) != 6:
        return None
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def _load_colours():
    cols = {}
    with open(ROOT / "data" / "elements.csv", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            c = _hex(r["color"])
            if c:
                cols[r["name"].lower()] = c
                cols[r["symbol"].lower()] = cols.get(r["symbol"].lower(), c)
    with open(ROOT / "data" / "substances.csv", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            c = _hex(r["color"])
            if c:
                cols[r["id"]] = c
    return cols


_COLOURS = None


def _texture_colour(item):
    """Average colour of the mod item's texture (opaque pixels), or None."""
    p = ROOT / "src" / "main" / "resources" / "assets" / "periodic_elements" / "textures" / "item" / f"{item}.png"
    if not p.exists():
        return None
    img = Image.open(p).convert("RGBA")
    px = [c for c in img.getdata() if c[3] > 128]
    if not px:
        return None
    return tuple(sum(c[i] for c in px) // len(px) for i in range(3))


def _parse(part):
    """'item:ns:id:12.5@slag' -> ('ns', 'id'); 'calcium_oxide:56' -> ('periodic_elements', 'calcium_oxide')."""
    part = part.strip().lstrip("~").split("@")[0]
    head, _, tail = part.rpartition(":")
    if head and tail.replace(".", "").isdigit():
        part = head
    if part.startswith("item:"):
        part = part[5:]
    ns, _, item = part.rpartition(":")
    return ns or "periodic_elements", item


def _item_colour(ns, item):
    global _COLOURS
    if _COLOURS is None:
        _COLOURS = _load_colours()
        import machines
        _COLOURS.update({k: v for k, v in machines.INGOT_COLORS.items()})
        _COLOURS.update({k: v for k, v in getattr(machines, "POWDERS", {}).items()})
    if item in _COLOURS:
        return _COLOURS[item]
    if ns == "minecraft" and item in VANILLA_COLOURS:
        return VANILLA_COLOURS[item]
    for suffix in ("_ingot", "_nugget", "_block", "_sample", "_wafer", "_crystal", "_wire", "_magnet", "_dust",
                   "_concentrate", "_flue_dust", "_chlorides", "_sulfates", "_alloy", "_pellet"):
        if item.endswith(suffix):
            stem = item[:-len(suffix)]
            for cand in (stem, stem.split("_")[-1], stem.split("_")[0]):
                if cand in _COLOURS:
                    return _COLOURS[cand]
    for prefix in ("crude_", "synthetic_", "depleted_", "rare_earth_", "heavy_rare_earth_", "uranium_fuel_"):
        if item.startswith(prefix) and item[len(prefix):] in _COLOURS:
            return _COLOURS[item[len(prefix):]]
    return EXTRA_COLOURS.get(item) or _texture_colour(item)


# Products that are neither an element, a substance nor an ingot with a colour of its own.
EXTRA_COLOURS = {
    "porcelain": (240, 240, 245), "coke": (70, 70, 76), "smokeless_powder": (200, 180, 120),
    "cemented_carbide": (110, 112, 120), "rare_earth_sulfates": (220, 200, 210), "heavy_rare_earth_chlorides": (210, 220, 200),
    "pitchblende_residue": (150, 160, 120), "silicon_wafer": (90, 100, 130), "germanium_crystal": (150, 150, 160),
    "synthetic_diamond": (180, 240, 250), "uranium_fuel_pellet": (60, 66, 60), "integrated_circuit": (60, 140, 90),
    "gallium_arsenide_wafer": (100, 90, 140), "neodymium_magnet": (150, 150, 160), "niobium_titanium_wire": (160, 160, 170),
    "type_metal": (121, 125, 133), "coal_gas": (200, 200, 150), "ammonia_water": (180, 200, 230), "water": (110, 160, 220),
}


def product_colour(reaction):
    """The colour of a reaction's main product: the first product that is kept; else the first in slag; else the
    gas it gives off; else the feed."""
    kept, slag, vented = [], [], []
    for part in reaction["products"].split(";"):
        part = part.strip()
        if not part:
            continue
        (vented if part.startswith("~") else slag if "@" in part else kept).append(_parse(part))
    for ns, item in kept + slag + vented + [_parse(reaction["feed"])]:
        c = _item_colour(ns, item)
        if c:
            return c
    return None


def _vivid(c, min_l=0.35, max_l=0.8, min_s=0.25):
    """Keeps a colour readable as a light: not too dark, not white, a little saturated."""
    if c is None:
        return None
    h, l, s = colorsys.rgb_to_hls(*(v / 255 for v in c))
    l = max(min_l, min(max_l, l))
    s = max(min_s, s) if s > 0.05 else s
    return tuple(int(v * 255) for v in colorsys.hls_to_rgb(h, l, s))


def _quant(c, step):
    if c is None:
        return None
    return tuple(min(255, (v // step) * step + step // 2) for v in c)


def flame_tint(t_max):
    """Flame colour by the top of the process's temperature range."""
    if t_max < 800:
        return (230, 90, 30)
    if t_max < 1150:
        return (250, 140, 40)
    if t_max < 1400:
        return (255, 190, 70)
    return (255, 235, 170)


# --------------------------------------------------------------------------------------------- drawing helpers

def _put(px, x, y, c, a=255):
    if a >= 255:
        px[x, y] = tuple(int(v) for v in c[:3]) + (255,)
    else:
        o = px[x, y]
        k = a / 255
        px[x, y] = tuple(int(o[i] * (1 - k) + c[i] * k) for i in range(3)) + (255,)


def _mix(a, b, k):
    return tuple(int(a[i] * (1 - k) + b[i] * k) for i in range(3))


def _dark(c, k=0.45):
    return tuple(int(v * k) for v in c)


def _light(c, k=0.5):
    return tuple(int(v + (255 - v) * k) for v in c)


def _frame(img, r, edge=(40, 34, 30), inside=(18, 16, 15)):
    x0, y0, x1, y1 = r
    px = img.load()
    for y in range(max(0, y0 - 1), min(15, y1) + 1):
        for x in range(max(0, x0 - 1), min(15, x1 + 1) + 1):
            _put(px, x, y, edge)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            _put(px, x, y, inside)


def _glow_above(img, r, col, k):
    """Light from the window falling on the wall just above it."""
    x0, y0, x1, y1 = r
    if y0 - 2 < 0:
        return
    px = img.load()
    for x in range(max(0, x0 - 1), min(15, x1 + 1) + 1):
        _put(px, x, y0 - 2, col, int(110 * k))


# --------------------------------------------------------------------------------------------- front renderers
# Each draws one frame into the window r of a 16x16 face. col = process colour (never None), t = top temperature.

def front_flame(img, r, i, n, col, t):
    x0, y0, x1, y1 = r
    _frame(img, r, inside=(26, 18, 14))
    px = img.load()
    tint = flame_tint(t)
    h = y1 - y0 + 1
    a = 2 * math.pi * i / n
    for x in range(x0, x1 + 1):
        c = x - x0
        tall = 0.45 + 0.3 * math.sin(a + c * 1.7) + 0.2 * math.sin(2 * a - c * 0.9)
        top = y1 - int(round(max(0.2, min(1.0, tall)) * (h - 1)))
        for y in range(top, y1 + 1):
            d = (y - top) / max(1, y1 - top)
            base = _dark(tint, 0.85) if d < 0.25 else tint if d < 0.6 else _light(tint, 0.5)
            _put(px, x, y, base)
    # The charge glowing in the process colour along the bottom.
    for x in range(x0, x1 + 1):
        k = 0.5 + 0.5 * math.sin(2 * a + x)
        _put(px, x, y1, _mix(_light(col, 0.2), tint, 0.4 * k))
    _glow_above(img, r, tint, 0.4 + 0.2 * math.sin(3 * a))


def front_embers(img, r, i, n, col, t):
    x0, y0, x1, y1 = r
    _frame(img, r)
    px = img.load()
    rnd = random.Random(11)
    phase = 2 * math.pi * i / n
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            k = (y - y0) / max(1, y1 - y0)
            f = 0.5 + 0.5 * math.sin(phase + rnd.random() * 6.28)
            heat = 0.35 + 0.65 * k * (0.6 + 0.4 * f)
            _put(px, x, y, (90 + 165 * heat, 20 + 150 * heat * heat * (0.7 + 0.3 * f), 10 + 40 * heat ** 3))
    for k in range(2):
        sx = x0 + (k * 3 + 1) % (x1 - x0 + 1)
        sy = y1 - (i + k * 3) % (y1 - y0 + 1)
        _put(px, sx, sy, (255, 236, 150))
    _glow_above(img, r, (255, 120, 40), 0.3 + 0.15 * math.sin(phase))


def front_melt(img, r, i, n, col, t):
    """A crucible of glowing melt in the process colour, flame licking round it, a bright surface rippling."""
    x0, y0, x1, y1 = r
    _frame(img, r, inside=(30, 20, 16))
    px = img.load()
    a = 2 * math.pi * i / n
    hot = _mix(_vivid(col, 0.45, 0.75), (255, 170, 60), 0.35 if t >= 900 else 0.15)
    surf = y0 + (y1 - y0) // 2
    for y in range(surf, y1 + 1):
        for x in range(x0, x1 + 1):
            w = 0.5 + 0.5 * math.sin(a + x * 1.3 + y * 0.7)
            c = _light(hot, 0.35 * w) if y == surf else _mix(hot, _dark(hot, 0.7), (y - surf) / max(1, y1 - surf) * 0.6)
            _put(px, x, y, c)
    # Shimmering heat above the melt.
    for x in range(x0, x1 + 1):
        if (x + i) % 3 == 0:
            _put(px, x, surf - 1, _light(hot, 0.6), 160)
        if (x + i) % 4 == 1 and surf - 2 >= y0:
            _put(px, x, surf - 2, hot, 90)
    _glow_above(img, r, hot, 0.35 + 0.15 * math.sin(a))


def front_blow(img, r, i, n, col, t):
    """Bessemer blow: melt with a shower of sparks."""
    front_melt(img, r, i, n, col, t)
    x0, y0, x1, y1 = r
    px = img.load()
    rnd = random.Random(i * 7 + 3)
    for _ in range(4):
        _put(px, rnd.randint(x0, x1), rnd.randint(y0, y0 + (y1 - y0) // 2), (255, 245, 180))


def _liquid(img, r, col, level=0.45):
    x0, y0, x1, y1 = r
    px = img.load()
    surf = y0 + int((y1 - y0) * level)
    liq = _vivid(col, 0.3, 0.6, 0.2)
    for y in range(surf, y1 + 1):
        for x in range(x0, x1 + 1):
            _put(px, x, y, _mix(liq, _dark(liq, 0.6), (y - surf) / max(1, y1 - surf) * 0.5))
    return surf, liq


def front_bubbles(img, r, i, n, col, t, rate=3, big=False, heat=False):
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(70, 74, 80), inside=(30, 34, 38))
    surf, liq = _liquid(img, r, col)
    px = img.load()
    h = y1 - surf + 1
    for k in range(rate):
        x = x0 + (k * 5 + 2) % (x1 - x0 + 1)
        y = y1 - ((i * (1 + k % 2) + k * 3) % h)
        _put(px, x, y, _light(liq, 0.7))
        if big and y + 1 <= y1:
            _put(px, x, y + 1, _light(liq, 0.45))
    # The surface heaves where the bubbles burst.
    for x in range(x0, x1 + 1):
        if (x + i) % 4 == 0:
            _put(px, x, surf, _light(liq, 0.5))
    if heat:
        for x in range(x0, x1 + 1):
            _put(px, x, y1, (220, 90, 30), 120 + int(60 * math.sin(2 * math.pi * i / n + x)))


def front_boil(img, r, i, n, col, t):
    front_bubbles(img, r, i, n, col, t, rate=4, big=True, heat=t > 60)
    # Steam curling off the surface.
    x0, y0, x1, y1 = r
    px = img.load()
    for k in range(2):
        x = x0 + (k * 3 + i) % (x1 - x0 + 1)
        y = y0 + (k + i // 2) % 2
        _put(px, x, y, (220, 224, 228), 150)


def front_ferment(img, r, i, n, col, t):
    front_bubbles(img, r, i // 2, n, col, t, rate=2)


def front_froth(img, r, i, n, col, t):
    """Flotation: air bubbles carry a mineral froth to the top, and it spills over."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(70, 74, 80), inside=(30, 34, 38))
    surf, liq = _liquid(img, r, (90, 110, 120), 0.5)
    px = img.load()
    froth = _vivid(col, 0.5, 0.8)
    for y in range(y0, surf + 1):
        for x in range(x0, x1 + 1):
            if (x * 3 + y * 5 + i) % 4 != 0:
                _put(px, x, y, _light(froth, 0.2 if (x + y + i) % 2 else 0.5))
    for k in range(3):
        x = x0 + (k * 2 + 1) % (x1 - x0 + 1)
        y = y1 - ((i + k * 2) % (y1 - surf + 1))
        _put(px, x, y, (235, 240, 245))


def front_stir(img, r, i, n, col, t):
    """A vat being stirred: a paddle turning in the liquid, a swirl on its surface."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(86, 62, 36), inside=(40, 30, 22))
    surf, liq = _liquid(img, r, col, 0.35)
    px = img.load()
    cx = (x0 + x1) / 2
    a = 2 * math.pi * i / n
    # The paddle seen side-on swings left and right.
    px_x = int(round(cx + math.sin(a) * (x1 - x0) / 2.5))
    for y in range(y0, y1):
        _put(px, max(x0, min(x1, px_x)), y, (150, 112, 70))
    for x in range(x0, x1 + 1):
        if (x - i) % 3 == 0:
            for y in range(surf + 1, y1 + 1, 2):
                _put(px, x, y, _light(liq, 0.25))


def front_distill(img, r, i, n, col, t):
    """A still: the pot bubbling below, drops of the distillate falling from the condenser."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(80, 70, 60), inside=(26, 22, 20))
    px = img.load()
    a = 2 * math.pi * i / n
    # Pot of hot liquid at the bottom.
    pot_top = y1 - 2
    for y in range(pot_top, y1 + 1):
        for x in range(x0, x1 + 1):
            w = 0.5 + 0.5 * math.sin(a * 2 + x)
            _put(px, x, y, _mix((150, 70, 30), (230, 120, 40), w if y == y1 else 0.2))
    # Condenser pipe across the top and drops of distillate falling into a receiver.
    for x in range(x0, x1 + 1):
        _put(px, x, y0, (170, 120, 70))
    dc = _vivid(col, 0.5, 0.8)
    xd = x1 - 1
    fall = (pot_top - 1) - (y0 + 1)
    for k in range(2):
        y = y0 + 1 + ((i + k * (n // 2)) * (fall + 1) // n) % (fall + 1)
        _put(px, xd, y, dc)
    # Vapour rising from the pot.
    for k in range(2):
        x = x0 + 1 + k * 2
        y = pot_top - 1 - ((i + k * 2) % max(1, fall))
        _put(px, x, y, (200, 200, 205), 120)


def front_crystals(img, r, i, n, col, t):
    """Crystals growing on the floor of a pan of brine, glinting."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(110, 90, 70), inside=(40, 46, 54))
    surf, liq = _liquid(img, r, (90, 130, 160), 0.3)
    px = img.load()
    cc = _vivid(col, 0.6, 0.85, 0.1)
    rnd = random.Random(5)
    seeds = [(rnd.randint(x0, x1), rnd.randint(surf + 1, y1)) for _ in range(6)]
    grow = 1 + i * 5 // n
    for k, (x, y) in enumerate(seeds[:grow + 1]):
        _put(px, x, y, cc)
        if k < grow - 1 and y - 1 > surf:
            _put(px, x, y - 1, _light(cc, 0.3))
    gx, gy = seeds[i % len(seeds)]
    _put(px, gx, gy, (255, 255, 255))


def front_gas(img, r, i, n, col, t):
    """A pipe with gas pulsing through it and a pressure gauge whose needle trembles."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(80, 84, 92), inside=(34, 38, 44))
    px = img.load()
    gc = _vivid(col, 0.5, 0.75)
    my = (y0 + y1) // 2
    for x in range(x0, x1 + 1):
        _put(px, x, my, (120, 126, 136))
        _put(px, x, my + 1, (100, 106, 116))
    w = x1 - x0 + 1
    head = x0 + i * w // n
    for x in range(x0, x1 + 1):
        d = (head - x) % w
        if d < 3:
            k = 1 - d / 3
            _put(px, x, my, _light(gc, 0.3 * k), int(140 + 115 * k))
            _put(px, x, my + 1, gc, int(100 + 100 * k))
    # Gauge above the pipe: a dial and a needle.
    gx = x0 + 1
    if my - 2 >= y0:
        _put(px, gx, my - 2, (220, 220, 210))
        _put(px, gx + 1, my - 2, (220, 220, 210))
        nx = gx + (1 if i % 4 < 2 else 0)
        _put(px, nx, my - 3 if my - 3 >= y0 else my - 2, (200, 40, 30))
    # Lamp.
    _put(px, x1, y0, (120, 255, 120) if i % 4 < 2 else (40, 100, 50))


def front_gasjar(img, r, i, n, col, t):
    """Gas bubbling up through liquid into an upturned jar (pneumatic trough, Solvay tower)."""
    x0, y0, x1, y1 = r
    front_bubbles(img, r, i, n, (80, 120, 160), t, rate=3)
    px = img.load()
    gc = _vivid(col, 0.55, 0.8)
    for y in range(y0, y0 + 2):
        for x in range(x0 + 1, x1):
            _put(px, x, y, gc, 120 + 40 * ((x + i) % 2))


def front_gauze(img, r, i, n, col, t):
    """A red-hot platinum gauze with gas streaming through it (Ostwald converter)."""
    front_gas(img, r, i, n, col, t)
    x0, y0, x1, y1 = r
    px = img.load()
    gx = (x0 + x1) // 2
    for y in range(y0, y1 + 1):
        k = 0.5 + 0.5 * math.sin(2 * math.pi * i / n + y)
        _put(px, gx, y, _mix((200, 60, 20), (255, 200, 90), k))


def front_electro(img, r, i, n, col, t):
    """An electrolysis cell: two electrodes in the bath, bubbles streaming off them, a spark of current."""
    x0, y0, x1, y1 = r
    front_bubbles(img, r, 0, n, col, t, rate=0)
    px = img.load()
    ea, eb = x0 + 1, x1 - 1
    for y in range(y0, y1):
        _put(px, ea, y, (90, 90, 96))
        _put(px, eb, y, (200, 120, 60))
    surf = y0 + int((y1 - y0) * 0.45)
    h = y1 - surf
    for k, x in enumerate((ea + 1, eb - 1)):
        for j in range(2):
            y = y1 - 1 - ((i + j * 3 + k) % max(1, h))
            _put(px, x, y, (235, 240, 245))
    if i % 4 == 0:
        _put(px, ea, y0, (255, 255, 160))
        _put(px, eb, y0, (255, 255, 160))


def front_arc(img, r, i, n, col, t):
    """Arc furnace: a white-blue arc jumping between the electrodes and the melt."""
    x0, y0, x1, y1 = r
    front_melt(img, r, i, n, col, t)
    px = img.load()
    rnd = random.Random(i * 13 + 1)
    surf = y0 + (y1 - y0) // 2
    for ex in (x0 + 1, x1 - 1):
        for y in range(y0, surf - 2):
            _put(px, ex, y, (70, 70, 76))
        x = ex
        for y in range(surf - 2, surf):
            x = max(x0, min(x1, x + rnd.choice((-1, 0, 1))))
            _put(px, x, y, (200, 230, 255) if i % 3 else (255, 255, 255))


def front_glow(img, r, i, n, col, t):
    """A sealed chamber glowing inside (sintering furnace, hot cell): a slow pulse of the process colour."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(90, 94, 100), inside=(20, 22, 26))
    px = img.load()
    gc = _vivid(col, 0.45, 0.75)
    a = 2 * math.pi * i / n
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            d = math.hypot(x - cx, y - cy) / max(1, (x1 - x0) / 2)
            k = max(0, 1 - d) * (0.6 + 0.4 * math.sin(a))
            _put(px, x, y, _mix((20, 22, 26), gc, k))
    # Glass reflection.
    _put(px, x0, y0, (180, 200, 210), 120)


def front_filament(img, r, i, n, col, t):
    """Van Arkel: a white-hot tungsten wire in a glass bulb, the metal growing on it."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(150, 160, 170), inside=(40, 44, 52))
    px = img.load()
    cx = (x0 + x1) // 2
    k = 0.75 + 0.25 * math.sin(2 * math.pi * i / n)
    for y in range(y0 + 1, y1):
        _put(px, cx, y, _mix((255, 180, 90), (255, 255, 230), k))
        if (y + i) % 3 == 0:
            _put(px, cx - 1, y, _vivid(col, 0.6, 0.85), 200)
            _put(px, cx + 1, y, (120, 110, 160), 120)
    for x in range(x0, x1 + 1):
        if (x + i) % 4 == 0:
            _put(px, x, y0, (150, 110, 200), 140)   # iodide vapour


def front_beam(img, r, i, n, col, t):
    """A particle beam: a bright spot spiralling out between the dees (cyclotron) with a glowing trail."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(110, 116, 126), inside=(14, 16, 30))
    px = img.load()
    gc = _vivid(col, 0.6, 0.8, 0.6)
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    rr = min(x1 - x0, y1 - y0) / 2 + 0.4
    # The dees glow faintly with the field.
    k = 0.5 + 0.5 * math.sin(2 * math.pi * i / n * 2)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if x != int(cx + 0.5):
                _put(px, x, y, _mix((14, 16, 30), (60, 70, 140), 0.35 + 0.25 * k))
    for trail in range(6):
        a = 2 * math.pi * (i - trail * 0.6) / n
        rad = rr * (0.45 + 0.1 * ((i - trail * 0.6) % n) / n * 5)
        x = int(round(cx + math.cos(a) * min(rr, rad)))
        y = int(round(cy + math.sin(a) * min(rr, rad)))
        if x0 <= x <= x1 and y0 <= y <= y1:
            _put(px, x, y, (255, 255, 255) if trail == 0 else _mix(gc, (60, 70, 140), trail / 7))


def front_spectrum(img, r, i, n, col, t):
    """Spectroscope: a flame and the spectrum it throws, with the element's lines flickering."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(110, 90, 60), inside=(12, 12, 16))
    px = img.load()
    for x in range(x0, x1 + 1):
        h = (x - x0) / max(1, x1 - x0) * 0.8
        c = tuple(int(v * 255) for v in colorsys.hsv_to_rgb(h, 0.9, 0.35))
        _put(px, x, y0 + 1, c)
    lines = [x0 + (sum(col) // (k + 7)) % (x1 - x0 + 1) for k in range(3)]
    for k, x in enumerate(lines):
        if (i + k) % 3 != 0:
            for y in range(y0, y0 + 3):
                _put(px, x, y, (255, 255, 255) if y == y0 + 1 else _vivid(col, 0.6, 0.85))
    fc = _vivid(col, 0.5, 0.75)
    for y in range(y1 - 2, y1 + 1):
        w = 1 if y < y1 else 2
        for x in range((x0 + x1) // 2 - w + 1, (x0 + x1) // 2 + w):
            _put(px, x, y + (0 if i % 2 else -0), _mix(fc, (255, 230, 150), 0.3 if (i + y) % 2 else 0.6))


def front_scan(img, r, i, n, col, t):
    """Chip fab: a wafer under a scanning beam, the pattern filling in row by row."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(200, 204, 210), inside=(40, 46, 70))
    px = img.load()
    rows = y1 - y0 + 1
    row = y0 + i * rows // n
    for y in range(y0, row):
        for x in range(x0, x1 + 1):
            if (x + y) % 2 == 0:
                _put(px, x, y, (90, 140, 200))
    for x in range(x0, x1 + 1):
        _put(px, x, row, (180, 120, 255))


def front_spin(img, r, i, n, col, t):
    """Czochralski puller: a crystal boule turning as it is drawn up out of the melt."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(150, 156, 166), inside=(20, 22, 28))
    px = img.load()
    melt = y1
    for x in range(x0, x1 + 1):
        _put(px, x, melt, (255, 160, 70) if (x + i) % 3 else (255, 220, 140))
    bc = _vivid(col, 0.35, 0.6, 0.1)
    cx = (x0 + x1) // 2
    for y in range(y0 + 1, melt):
        half = 1 if y < y0 + 3 else 2
        for x in range(cx - half + 1, cx + half + 1):
            shade = 0.25 if (x - cx + i) % 4 == 0 else 0
            _put(px, x, y, _light(bc, shade))
    _put(px, cx, y0, (170, 170, 176))


def front_zone(img, r, i, n, col, t):
    """Zone refining: a molten band slowly travelling down a bar."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(150, 156, 166), inside=(20, 22, 28))
    px = img.load()
    bc = _vivid(col, 0.35, 0.6, 0.1)
    cx = (x0 + x1) // 2
    zone = y0 + i * (y1 - y0 + 1) // n
    for y in range(y0, y1 + 1):
        for x in (cx, cx + 1):
            if abs(y - zone) <= 0:
                _put(px, x, y, (255, 200, 110))
            else:
                _put(px, x, y, bc)
        if abs(y - zone) <= 1:
            _put(px, cx - 1, y, (220, 90, 40))
            _put(px, cx + 2, y, (220, 90, 40))


def front_press(img, r, i, n, col, t):
    """A press: the ram comes down on the charge and lifts again."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(90, 94, 100), inside=(30, 32, 36))
    px = img.load()
    travel = [0, 1, 2, 3, 3, 2, 1, 0][i % 8]
    ram_bottom = y0 + 1 + travel
    for y in range(y0, ram_bottom + 1):
        for x in range(x0 + 1, x1):
            _put(px, x, y, (150, 154, 160) if y == ram_bottom else (110, 114, 120))
    cc = _vivid(col, 0.4, 0.7)
    for y in range(y1 - 1, y1 + 1):
        for x in range(x0 + 1, x1):
            _put(px, x, y, cc if travel < 3 else _light(cc, 0.3))
    if travel == 3:
        _put(px, x0, y1 - 1, (240, 240, 200))
        _put(px, x1, y1 - 1, (240, 240, 200))


def front_drip(img, r, i, n, col, t):
    """Droplets falling down a column (prilling tower, ion-exchange column)."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(150, 156, 166), inside=(36, 40, 48))
    px = img.load()
    dc = _vivid(col, 0.55, 0.85)
    h = y1 - y0 + 1
    for k in range(3):
        x = x0 + 1 + (k * 2) % (x1 - x0)
        y = y0 + ((i + k * 3) * h // n) % h
        _put(px, x, y, dc)
        if y - 1 >= y0:
            _put(px, x, y - 1, dc, 110)
    for x in range(x0, x1 + 1):
        _put(px, x, y1, _dark(dc, 0.8))


def front_frost(img, r, i, n, col, t):
    """Air liquefier: frost on the glass and pale blue liquid air dripping into a flask."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(170, 190, 210), inside=(30, 50, 70))
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if (x * 7 + y * 3) % 5 == 0:
                _put(px, x, y, (220, 240, 255), 160)
    dc = _mix(_vivid(col, 0.55, 0.8), (150, 200, 255), 0.5)
    cx = (x0 + x1) // 2
    y = y0 + (i * (y1 - y0 - 1) // n)
    _put(px, cx, y, dc)
    for x in range(x0 + 1, x1):
        _put(px, x, y1, dc)
    for k in range(2):
        mx = x0 + (i + k * 3) % (x1 - x0 + 1)
        _put(px, mx, y1 - 1, (230, 240, 250), 140)


FRONT = {
    "flame": front_flame, "embers": front_embers, "melt": front_melt, "blow": front_blow, "bubbles": front_bubbles,
    "boil": front_boil, "ferment": front_ferment, "froth": front_froth, "stir": front_stir, "distill": front_distill,
    "crystals": front_crystals, "gas": front_gas, "gasjar": front_gasjar, "gauze": front_gauze,
    "electro": front_electro, "arc": front_arc, "glow": front_glow, "filament": front_filament, "beam": front_beam,
    "spectrum": front_spectrum, "scan": front_scan, "spin": front_spin, "zone": front_zone, "press": front_press,
    "drip": front_drip, "frost": front_frost,
}

# Kinds whose frames are distinct moving objects: blending between frames would smear them.
NO_BLEND = {"gas", "gasjar", "gauze", "beam", "scan", "spin", "zone", "press", "drip", "frost", "stir", "distill",
            "electro", "arc", "blow", "spectrum", "froth", "bubbles", "boil", "ferment", "crystals"}
FRAMETIME = {"ferment": 4, "crystals": 4, "glow": 3, "embers": 3, "spin": 3, "zone": 6, "press": 3, "scan": 3}


# --------------------------------------------------------------------------------------------- top renderers

def top_anim(img, r, i, n, kind, col, t):
    """The top face: a flue, hatch or vent showing the same process from above."""
    x0, y0, x1, y1 = r
    px = img.load()
    a = 2 * math.pi * i / n
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    if kind in ("flame", "embers", "melt", "blow", "arc", "filament", "glow"):
        base = flame_tint(t) if kind in ("flame", "embers") else _vivid(col, 0.45, 0.75)
        if kind == "arc":
            base = (210, 230, 255)
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                edge = x in (x0, x1) or y in (y0, y1)
                k = (0.55 + 0.45 * math.sin(a + (x + y) * 0.8)) * (0.6 if edge else 1.0)
                _put(px, x, y, _mix(_dark(base, 0.35), base, k))
        if kind in ("blow", "arc") and i % 2 == 0:
            rnd = random.Random(i)
            _put(px, rnd.randint(x0, x1), rnd.randint(y0, y1), (255, 255, 220))
    elif kind in ("bubbles", "boil", "ferment", "froth", "stir", "electro", "gasjar", "crystals", "drip"):
        liq = _vivid(col if kind not in ("gasjar",) else (80, 120, 160), 0.3, 0.6, 0.2)
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                _put(px, x, y, liq)
        if kind == "stir":
            for k in range(6):
                ang = a + k * 0.9
                rr = 0.6 + k * 0.45
                x, y = int(round(cx + math.cos(ang) * rr)), int(round(cy + math.sin(ang) * rr))
                if x0 <= x <= x1 and y0 <= y <= y1:
                    _put(px, x, y, _light(liq, 0.45))
        elif kind == "crystals":
            rnd = random.Random(3)
            for k in range(5):
                x, y = rnd.randint(x0, x1), rnd.randint(y0, y1)
                _put(px, x, y, _vivid(col, 0.7, 0.9, 0.1) if (k + i) % 4 else (255, 255, 255))
        else:
            rnd = random.Random(7)
            spots = [(rnd.randint(x0, x1), rnd.randint(y0, y1)) for _ in range(6)]
            step = 1 if kind == "ferment" else 2
            for k, (x, y) in enumerate(spots):
                ph = (i * step + k * 3) % 6
                if ph < 2:
                    _put(px, x, y, _light(liq, 0.6 if ph == 0 else 0.35))
            if kind == "froth":
                for y in range(y0, y1 + 1):
                    for x in range(x0, x1 + 1):
                        if (x * 3 + y * 5 + i) % 3 == 0:
                            _put(px, x, y, _vivid(col, 0.6, 0.85))
    elif kind in ("gas", "gauze", "press", "frost"):
        # A fan turning under a grille.
        blades = 4
        a0 = a / blades
        ring = _vivid(col, 0.5, 0.75)
        if kind == "frost":
            ring = (200, 230, 255)
        rmax = (x1 - x0) / 2 + 0.3
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                dx, dy = x - cx, y - cy
                d = math.hypot(dx, dy)
                if d > rmax:
                    _put(px, x, y, (60, 64, 70))
                elif d > rmax - 0.9:
                    _put(px, x, y, _mix(_dark(ring, 0.5), ring, 0.5 + 0.5 * math.sin(a + math.atan2(dy, dx))))
                elif d < 0.8:
                    _put(px, x, y, (90, 94, 100))
                else:
                    ang = math.atan2(dy, dx) - a0 - d * 0.3
                    _put(px, x, y, (196, 202, 210) if math.cos(blades * ang) > 0.2 else (40, 44, 50))
    elif kind == "distill":
        # The condenser's cooling water flowing round a coil.
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                k = 0.5 + 0.5 * math.sin(a + (x - y) * 1.1)
                _put(px, x, y, _mix((60, 110, 150), (140, 190, 220), k))
        for y in range(y0 + 1, y1, 2):
            for x in range(x0 + 1, x1):
                _put(px, x, y, (170, 120, 70))
    else:
        # beam, scan, spin, zone, spectrum: a status ring of lamps chasing round.
        lamps = [(x, y0) for x in range(x0, x1 + 1)] + [(x1, y) for y in range(y0 + 1, y1 + 1)] + \
                [(x, y1) for x in range(x1 - 1, x0 - 1, -1)] + [(x0, y) for y in range(y1 - 1, y0, -1)]
        gc = _vivid(col, 0.5, 0.75, 0.5)
        for k, (x, y) in enumerate(lamps):
            d = (i * len(lamps) // n - k) % len(lamps)
            _put(px, x, y, (255, 255, 255) if d == 0 else gc if d < 3 else _dark(gc, 0.35))
        for y in range(y0 + 1, y1):
            for x in range(x0 + 1, x1):
                k = 0.4 + 0.3 * math.sin(a * 2)
                _put(px, x, y, _mix((16, 18, 26), gc, k * max(0, 1 - math.hypot(x - cx, y - cy) / 3)))


# --------------------------------------------------------------------------------------------- choosing modes

def kind_for(machine, reaction_id):
    return REACTION_KIND.get((machine, reaction_id)) or REACTION_KIND.get((machine, "*")) or MACHINE_KIND.get(machine, "glow")


def plan(machines_rows, reactions_rows):
    """machine -> (looks, modes): looks = list of (kind, colour, t_max, [reaction ids]); modes = {reaction id: index}."""
    by = {}
    for r in reactions_rows:
        for m in r["machines"].split(";"):
            by.setdefault(m, []).append(r)
    out = {}
    for mrow in machines_rows:
        mid = mrow["id"]
        rs = by.get(mid, [])
        default_kind = MACHINE_KIND.get(mid, "glow")
        step = 32
        while True:
            looks, modes = [], {}
            for r in rs:
                if r["id"] in TRIVIAL:
                    continue
                kind = kind_for(mid, r["id"])
                col = _quant(_vivid(product_colour(r)) or (180, 180, 180), step)
                t = float(r["t_max"] or 0)
                tband = 0 if kind not in ("flame", "embers") else (0 if t < 800 else 1 if t < 1150 else 2 if t < 1400 else 3)
                key = (kind, col, tband)
                for j, look in enumerate(looks):
                    if look[4] == key:
                        look[3].append(r["id"])
                        modes[r["id"]] = j
                        break
                else:
                    modes[r["id"]] = len(looks)
                    looks.append([kind, col, t, [r["id"]], key])
            if len(looks) <= MAX_MODES or step >= 256:
                break
            step *= 2
        if not looks:
            looks = [[default_kind, (180, 180, 180), float(mrow["max_c"] or 0), [], None]]
        # Mode 0 should be the machine's own kind; put the first look of that kind first.
        first = next((j for j, l in enumerate(looks) if l[0] == default_kind), 0)
        if first:
            order = [first] + [j for j in range(len(looks)) if j != first]
            remap = {old: new for new, old in enumerate(order)}
            looks = [looks[j] for j in order]
            modes = {k: remap[v] for k, v in modes.items()}
        out[mid] = ([tuple(l[:4]) for l in looks[:MAX_MODES]], {k: min(v, MAX_MODES - 1) for k, v in modes.items()})
    return out


def render(mid, textures, look, front_window=None, top_window=None):
    """Frames for one look: (front frames, top frames, frametime, interpolate)."""
    kind, col, t, _ = look
    fw = front_window or FRONT_WINDOW.get(mid, DEFAULT_FRONT)
    tw = top_window or TOP_WINDOW.get(mid, DEFAULT_TOP)
    base_front = textures.get("front", textures["side"])
    fronts, tops = [], []
    for i in range(FRAMES):
        f = base_front.copy()
        FRONT[kind](f, fw, i, FRAMES, col, t)
        fronts.append(f)
        tp = textures["top"].copy()
        top_anim(tp, tw, i, FRAMES, kind, col, t)
        tops.append(tp)
    return fronts, tops, FRAMETIME.get(kind, 2), kind not in NO_BLEND


def viewport(side, window, lit):
    """The idle front of an electric machine: an inspection window, dark or faintly lit, instead of a firebox."""
    img = side.copy()
    _frame(img, window, edge=(90, 94, 100), inside=(48, 70, 80) if lit else (24, 28, 34))
    px = img.load()
    _put(px, window[0], window[1], (170, 190, 200), 140)
    if lit:
        _put(px, window[2], window[1] - 1 if window[1] > 0 else window[1], (120, 255, 120))
    return img


# --------------------------------------------------------------------------------------------- power blocks (3.33.1)
# Power blocks have no reactions: they show one animation while they run (block state lit=true).

def _fan(img, r, i, n, ring, blades=4, speed=1.0):
    x0, y0, x1, y1 = r
    px = img.load()
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    a0 = 2 * math.pi * i / n / blades * speed
    rmax = min(x1 - x0, y1 - y0) / 2 + 0.4
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            dx, dy = x - cx, y - cy
            d = math.hypot(dx, dy)
            if d > rmax:
                _put(px, x, y, (40, 44, 50))
            elif d < 0.8:
                _put(px, x, y, (90, 94, 100))
            else:
                ang = math.atan2(dy, dx) - a0
                _put(px, x, y, ring if math.cos(blades * ang) > 0.3 else (40, 44, 50))


def front_rotor(img, r, i, n, col, t):
    """A dynamo's armature turning, with blue sparks at the brushes."""
    _frame(img, r, edge=(90, 94, 100), inside=(30, 32, 36))
    _fan(img, r, i, n, (200, 120, 70), blades=3)
    x0, y0, x1, y1 = r
    px = img.load()
    if i % 2 == 0:
        _put(px, x0, (y0 + y1) // 2, (180, 220, 255))
        _put(px, x1, (y0 + y1) // 2, (180, 220, 255))


def front_fan(img, r, i, n, col, t):
    _frame(img, r, edge=(200, 204, 210), inside=(120, 170, 220))
    _fan(img, r, i, n, (235, 238, 242), blades=3)


def front_charge(img, r, i, n, col, t):
    """A battery's charge meter: a column of green bars filling up, then starting again."""
    x0, y0, x1, y1 = r
    _frame(img, r, edge=(90, 94, 100), inside=(20, 22, 26))
    px = img.load()
    h = y1 - y0 + 1
    lit = (i * h) // n + 1
    for j in range(h):
        y = y1 - j
        on = j < lit
        for x in range(x0 + 1, x1):
            _put(px, x, y, (90, 230, 110) if on and j < h - 1 else (230, 210, 80) if on else (40, 60, 44))


def front_piston(img, r, i, n, col, t):
    """An engine: the firebox below and the piston rod and crank going round above."""
    x0, y0, x1, y1 = r
    mid = (y0 + y1) // 2
    front_flame(img, (x0, mid + 1, x1, y1), i, n, col, t)
    px = img.load()
    for y in range(y0, mid + 1):
        for x in range(x0, x1 + 1):
            _put(px, x, y, (30, 30, 34))
    a = 2 * math.pi * i / n
    cx, cy = (x0 + x1) / 2, y0 + 1
    crank = (int(round(cx + math.cos(a) * 1.5)), int(round(cy + math.sin(a) * 1)))
    for y in range(min(crank[1], mid), mid + 1):
        _put(px, crank[0], y, (180, 184, 190))
    _put(px, crank[0], crank[1], (230, 230, 236))


FRONT.update({"rotor": front_rotor, "fan": front_fan, "charge": front_charge, "piston": front_piston})
NO_BLEND.update({"rotor", "fan", "charge", "piston"})
FRAMETIME.update({"charge": 4, "fan": 2, "rotor": 1})

# Power block -> (front kind, top kind, colour, temperature for flame tints).
POWER_LOOKS = {
    "voltaic_pile": ("electro", "electro", (180, 200, 210), 20),
    "steam_engine": ("piston", "steam", (200, 120, 60), 900),
    "dynamo": ("rotor", "gas", (200, 120, 70), 20),
    "solar_panel": ("charge", "solar", (60, 80, 160), 20),
    "nuclear_reactor": ("glow", "glow", (110, 180, 255), 300),
    "radioisotope_generator": ("glow", "glow", (255, 120, 60), 600),
    "diesel_generator": ("piston", "exhaust", (120, 120, 120), 700),
    "wind_turbine": ("fan", "gas", (235, 238, 242), 20),
    # 3.36.0: helium bubbling up past a faint yellow glow (the helium line).
    "helium_collector": ("glow", "gas", (250, 220, 120), 20),
}


def top_power(img, r, i, n, kind, col, t):
    x0, y0, x1, y1 = r
    px = img.load()
    if kind in ("steam", "exhaust"):
        # 3.41.0: grey smoke puffing out of the steam engine's stack, black from the diesel's exhaust.
        puff = (150, 150, 156) if kind == "steam" else (28, 28, 30)
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                edge = x in (x0, x1) or y in (y0, y1)
                _put(px, x, y, (120, 122, 128) if edge else (70, 72, 78))
        k = (i % 4) / 3
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                if math.hypot(x - (x0 + x1) / 2, y - (y0 + y1) / 2) <= 0.5 + 2.2 * k:
                    _put(px, x, y, puff, int(220 * (1 - 0.5 * k)))
    elif kind == "solar":
        # Sunlight glinting across the cells.
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                d = (x + y - (i * (x1 - x0 + y1 - y0 + 4)) // n - x0 - y0) % 14
                _put(px, x, y, (180, 200, 255) if d == 0 else (110, 130, 220) if d == 1 else (40, 60, 140))
    else:
        top_anim(img, r, i, n, kind, col, t)


def render_power(pid, textures):
    """(front frames, top frames, ticks per frame, blend) for a power block while it runs."""
    front_kind, top_kind, col, t = POWER_LOOKS[pid]
    fw, tw = FRONT_WINDOW.get(pid, DEFAULT_FRONT), TOP_WINDOW.get(pid, DEFAULT_TOP)
    fronts, tops = [], []
    for i in range(FRAMES):
        f = textures.get("front", textures["side"]).copy()
        FRONT[front_kind](f, fw, i, FRAMES, col, t)
        fronts.append(f)
        tp = textures["top"].copy()
        top_power(tp, tw, i, FRAMES, top_kind, col, t)
        tops.append(tp)
    return fronts, tops, FRAMETIME.get(front_kind, 2), front_kind not in NO_BLEND


# --------------------------------------------------------------------------------------------- battery (3.41.0)
# Not animated: the Lithium-Ion Battery shows its charge. Block state level (0..12 bar rows) and band (colour).
BATTERY_ROWS = 12
BATTERY_COLOURS = [(20, 20, 22), (220, 50, 40), (240, 210, 50), (80, 210, 90), (70, 140, 255)]


def battery_front(side, level, band):
    """The charge bar: a tall gauge on the front, filled to `level` rows in the band's colour."""
    img = side.copy()
    px = img.load()
    _frame(img, (5, 2, 10, 13), edge=(150, 156, 166), inside=(24, 26, 30))
    col = BATTERY_COLOURS[band]
    for j in range(level):
        y = 13 - j
        top_row = j == level - 1
        for x in range(5, 11):
            c = _light(col, 0.3) if top_row and band else col
            if x in (5, 10):
                c = _dark(c, 0.8)
            _put(px, x, y, c)
    # Tick marks at a quarter, half and full.
    for frac in (0.25, 0.5, 0.75):
        y = 13 - int(round(frac * BATTERY_ROWS))
        _put(px, 11, y, (200, 204, 210))
    return img


def battery_top(top, band):
    """The button on top, lit in the charge's colour (black when empty)."""
    img = top.copy()
    px = img.load()
    col = BATTERY_COLOURS[band]
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 3.6:
                c = col if d <= 2.6 else (150, 156, 166)
                if d <= 1.2 and band:
                    c = _light(col, 0.45)
                _put(px, x, y, c)
    return img
