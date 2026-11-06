"""Multiblock structures (3.3.0, stage 1 of the multiblock plan): the shell blocks, their textures, recipes and text.

Java: com.periodicelements.multiblock (Multiblock registers the blocks, Structures measures the structures). The
recipes go into machines.RECIPES so the Field Guide draws them; the "Structures" chapter is built in guidebook.py from
the hand-written text below and the gains computed here (same formulas as Structures.java).
"""
import json
import random

from PIL import Image

EXPONENT = 0.85

BLOCKS = {
    "refractory_brick": ("Refractory Brick", "Fireclay brick for the shell of a Blast Furnace stack; takes 1,600 °C"),
    "tuyere": ("Tuyere", "Water-cooled copper nozzle for the bottom of a Blast Furnace stack: the blast goes in here"),
    "structure_port": ("Structure Port", "Set it in the shell of a structure: hoppers reach the controller through it"),
    "silica_brick": ("Silica Brick", "Quartz-sand brick for a Coke Oven Battery: stays strong while the ovens glow"),
    # 3.8.0
    "lead_sheet": ("Lead Sheet", "Thick lead plate for the walls of a Lead Chamber: oil of vitriol does not eat it"),
    "gay_lussac_tower": ("Gay-Lussac Tower", "Lead tower packed with coke: three or more against a Lead Chamber catch its fumes, 25% faster"),
    "distillation_tray": ("Distillation Tray", "Steel tray with bubble caps: stack them on an Oil Refinery Still to make a column"),
    # 3.13.0
    "diffuser_stage": ("Diffuser Stage", "A pump and a porous nickel barrier: a block of them behind an Enrichment Cascade makes it a real cascade"),
    "graphite_block": ("Graphite Block", "Pure graphite that slows neutrons without catching them: the core of a Nuclear Reactor"),
    "fuel_channel": ("Fuel Channel", "A Zircaloy tube through the graphite core where the fuel burns: each one adds power"),
    "reactor_shielding": ("Reactor Shielding", "Steel and lead round a reactor core: keeps the neutrons in and the radiation out"),
}
# Which research unlocks each block (also listed in data/research.csv).
UNLOCKS = {"refractory_brick": "blast_furnace", "tuyere": "blast_furnace", "structure_port": "blast_furnace",
           "silica_brick": "coking", "lead_sheet": "lead_chamber", "gay_lussac_tower": "lead_chamber",
           "distillation_tray": "oil_refining", "diffuser_stage": "isotope_separation", "graphite_block": "nuclear_reactor",
           "fuel_channel": "nuclear_reactor", "reactor_shielding": "nuclear_reactor"}

SHAPED = {
    "refractory_brick": {"pattern": ["BCB", "CSC", "BCB"], "count": 4,
                         "key": {"B": "minecraft:brick", "C": "minecraft:clay_ball", "S": "minecraft:sand"}},
    "tuyere": {"pattern": ["RCR", "R R", "RCR"],
               "key": {"R": "periodic_elements:refractory_brick", "C": "minecraft:copper_ingot"}},
    "structure_port": {"pattern": ["RIR", "IHI", "RIR"],
                       "key": {"R": "periodic_elements:refractory_brick", "I": "minecraft:iron_ingot", "H": "minecraft:hopper"}},
    "silica_brick": {"pattern": ["SSS", "SLS", "SSS"], "count": 4,
                     "key": {"S": "minecraft:sand", "L": "periodic_elements:calcium_oxide"}},
    "lead_sheet": {"pattern": ["LLL"], "count": 3, "key": {"L": "periodic_elements:lead"}},
    "gay_lussac_tower": {"pattern": ["SCS", "SCS", "SCS"], "count": 3,
                         "key": {"S": "periodic_elements:lead_sheet", "C": "periodic_elements:coke"}},
    "distillation_tray": {"pattern": ["SSS", "B B", "SSS"], "count": 2,
                          "key": {"S": "periodic_elements:steel_ingot", "B": "minecraft:iron_bars"}},
    "diffuser_stage": {"pattern": ["SNS", "NPN", "SNS"], "count": 2,
                       "key": {"S": "periodic_elements:steel_ingot", "N": "periodic_elements:nickel", "P": "minecraft:piston"}},
    "graphite_block": {"pattern": ["CC", "CC"], "count": 2, "key": {"C": "periodic_elements:coke"}},
    "fuel_channel": {"pattern": ["Z Z", "Z Z", "Z Z"], "count": 3, "key": {"Z": "periodic_elements:zircaloy_ingot"}},
    "reactor_shielding": {"pattern": ["SLS", "LSL", "SLS"], "count": 4,
                          "key": {"S": "periodic_elements:steel_ingot", "L": "periodic_elements:lead"}},
}

LANG = {
    "gui.periodic_elements.structure.blast_furnace": "Furnace stack %sx%sx%s: speed x%s, heat per item %s%%",
    "gui.periodic_elements.structure.coke_battery": "Battery of %s ovens: speed x%s, heat per item %s%%",
    "gui.periodic_elements.structure.coke_battery.gas": "Battery of %s ovens: speed x%s, fired by its own gas",
    "gui.periodic_elements.structure.missing": "Structure: %s missing in layer %s",
    "gui.periodic_elements.structure.missing.floor": "Structure: %s missing in the floor",
    "gui.periodic_elements.structure.too_small.blast_furnace": "Furnace stack too low: the inside must be 3 to 10 high",
    "gui.periodic_elements.structure.too_small.coke_battery": "Battery too small",
    "gui.periodic_elements.structure.no_tuyere": "Furnace stack needs a Tuyere in its bottom layer",
    "gui.periodic_elements.structure.decimal": ".",
    "gui.periodic_elements.guide.layer": "Layer %s of %s",
    "gui.periodic_elements.guide.floor": "Floor",
    "gui.periodic_elements.guide.smallest": "Smallest",
    "gui.periodic_elements.guide.largest": "Largest",
    "gui.periodic_elements.guide.drag": "Drag to turn",
    "gui.periodic_elements.structure.lead_chamber": "Lead chamber %sx%sx%s: speed x%s, heat per item %s%%",
    "gui.periodic_elements.structure.lead_chamber.tower": "Lead chamber %sx%sx%s and tower: speed x%s, heat per item %s%%",
    "gui.periodic_elements.structure.distillation_column": "Column %sx%s, %s trays: speed x%s, %s%% of the bitumen to light cuts",
    "gui.periodic_elements.structure.too_small.lead_chamber": "Lead chamber too small: the inside must be at least 3x3x3",
    "gui.periodic_elements.structure.too_small.distillation_column": "Column too low: it needs at least 4 trays",
    "gui.periodic_elements.structure.enrichment_cascade": "Cascade of %s stages: speed x%s, heat per item %s%%, %s%% of the feed enriched",
    "gui.periodic_elements.structure.too_small.enrichment_cascade": "Cascade too small: it needs at least 4 Diffuser Stages",
    "gui.periodic_elements.structure.too_small.nuclear_reactor": "Reactor core too small: it must be at least 3x3x3",
    "gui.periodic_elements.structure.no_channel": "Reactor core needs at least one Fuel Channel",
    "gui.periodic_elements.power.reactor_core": "Reactor core %sx%sx%s, %s fuel channels: %s of %s kJ stored; %s; k %s; %s",
    "gui.periodic_elements.power.reactor.subcritical": "subcritical: the core is too small for this fuel",
    "gui.periodic_elements.power.reactor.runaway": "SCRAM: it ran away with no Boron Control Rod in its slots",
}


# ---- Gains, the same numbers Structures.java uses. ----

def furnace(a, h):
    """(shell blocks, speed, heat lost per item) for a blast furnace with interior a x a x h."""
    s = a + 2
    blocks = s * s + (s * s - a * a) * h
    speed = (a * a * h / 3) ** EXPONENT
    return blocks, speed, (4 * a * h + a * a) / 13 / speed


def battery(n):
    blocks = (2 * n + 1) * 5 * 4 - 6 * n
    speed = n ** EXPONENT
    return blocks, speed, (n + 1) / 2 / speed


FURNACE_ROWS = [(1, 3), (2, 5), (2, 8), (3, 10)]
BATTERY_ROWS = [2, 4, 8, 12]


def chamber(a, b, h, tower=False):
    blocks = (a + 2) * (b + 2) * (h + 2) - a * b * h
    speed = (a * b * h / 27) ** EXPONENT * (1.25 if tower else 1)
    return blocks, speed, 2 * (a * b + a * h + b * h) / 54 / speed


def column(w, h):
    """(trays, speed, heat per item, share of the bitumen sent up as light cuts)."""
    speed = (w * w) ** EXPONENT
    return w * w * h, speed, (16 * w + w * w) / 17 / speed, 0.4 * max(0, min(1, (h - 4) / 12))


def cascade(w, h, l):
    """(stages, speed, heat per item, enriched UF6 per 100 natural) for w x h rows of l Diffuser Stages."""
    n = min(64, w * h * l)
    speed = (n / 4) ** EXPONENT
    loss = (n / 4) ** 0.7
    return n, speed, loss / speed, 14.5 + 3.5 * max(0, min(1, (n - 4) / 60))


CASCADE_ROWS = [(1, 1, 4), (1, 1, 16), (2, 2, 8), (4, 4, 4)]

MIGRATION, K_ENRICHED, K_NATURAL, K_RUNAWAY, CHANNEL_BLOCKS = 0.3, 1.6, 1.15, 1.3, 3


def non_leakage(a, b, h):
    import math
    b2 = (math.pi / (a + 1)) ** 2 + (math.pi / (b + 1)) ** 2 + (math.pi / (h + 1)) ** 2
    return 1 / (1 + MIGRATION * b2)


def reactor(a, b, h):
    """For the guide's lattice core (a channel column at every other block): (channel blocks, kJ per tick, rod life
    factor, k with enriched rods, k with natural rods and heavy water)."""
    channels = ((a + 1) // 2) * ((b + 1) // 2) * h
    p = non_leakage(a, b, h)
    return channels, 30 * channels / CHANNEL_BLOCKS, p / non_leakage(3, 3, 3), K_ENRICHED * p, K_NATURAL * p


REACTOR_ROWS = [(3, 3, 3), (5, 5, 5), (7, 7, 7), (9, 9, 7)]

CHAMBER_ROWS = [(3, 3, 3, False), (3, 3, 3, True), (6, 6, 4, True), (9, 9, 6, True)]
COLUMN_ROWS = [(1, 4), (1, 10), (1, 16), (3, 16)]


# ---- Textures. ----

def _noise(base, var, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = rnd.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


def _rect(img, x0, y0, x1, y1, color):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(color) + (255,)


def _bricks(base, mortar, seed):
    """Running-bond bricks: four courses of 4 pixels, joints offset every other course, each brick its own shade."""
    rnd = random.Random(seed)
    img = _noise(base, 6, seed)
    px = img.load()
    for row in range(4):
        off = 0 if row % 2 == 0 else 4
        for x0 in range(off - 8, 16, 8):
            shade = rnd.randint(-18, 18)
            for y in range(row * 4, row * 4 + 3):
                for x in range(max(0, x0 + 1), min(16, x0 + 8)):
                    r, g, b_, a = px[x, y]
                    px[x, y] = (max(0, min(255, r + shade)), max(0, min(255, g + shade)), max(0, min(255, b_ + shade)), a)
            if 0 <= x0 < 16:
                _rect(img, x0, row * 4, x0, row * 4 + 2, mortar)
        _rect(img, 0, row * 4 + 3, 15, row * 4 + 3, mortar)
    return img


def textures():
    out = {}
    out["refractory_brick"] = _bricks((222, 196, 150), (118, 100, 84), 3201)
    out["silica_brick"] = _bricks((232, 230, 222), (140, 138, 132), 3202)
    tuyere = _bricks((222, 196, 150), (118, 100, 84), 3203)
    _rect(tuyere, 4, 4, 11, 11, (184, 108, 62))   # copper nozzle
    _rect(tuyere, 5, 5, 10, 10, (214, 134, 80))
    _rect(tuyere, 6, 6, 9, 9, (40, 28, 22))        # the bore
    _rect(tuyere, 7, 7, 8, 8, (255, 150, 60))      # the glow inside
    out["tuyere"] = tuyere
    port = _noise((120, 124, 132), 6, 3204)
    _rect(port, 0, 0, 15, 1, (78, 80, 88))
    _rect(port, 0, 14, 15, 15, (78, 80, 88))
    _rect(port, 0, 0, 1, 15, (78, 80, 88))
    _rect(port, 14, 0, 15, 15, (78, 80, 88))
    _rect(port, 4, 4, 11, 11, (54, 56, 62))        # hatch
    _rect(port, 5, 5, 10, 10, (92, 96, 104))
    _rect(port, 7, 4, 8, 11, (54, 56, 62))
    _rect(port, 2, 2, 2, 2, (200, 204, 210))       # rivets
    _rect(port, 13, 2, 13, 2, (200, 204, 210))
    _rect(port, 2, 13, 2, 13, (200, 204, 210))
    _rect(port, 13, 13, 13, 13, (200, 204, 210))
    out["structure_port"] = port
    # Lead: dull blue-grey plate with hammered seams.
    lead = _noise((118, 122, 136), 7, 3205)
    _rect(lead, 0, 7, 15, 7, (92, 96, 110))
    _rect(lead, 7, 0, 7, 6, (92, 96, 110))
    _rect(lead, 11, 8, 11, 15, (92, 96, 110))
    out["lead_sheet"] = lead
    # Gay-Lussac tower: lead frame, coke lumps seen through a grille.
    tower = _noise((118, 122, 136), 7, 3206)
    _rect(tower, 3, 2, 12, 13, (34, 34, 38))
    rnd = random.Random(3207)
    for _ in range(14):
        x, y = rnd.randint(3, 11), rnd.randint(2, 12)
        _rect(tower, x, y, x + 1, y + 1, (70, 70, 78))
    for y in (5, 9):
        _rect(tower, 3, y, 12, y, (118, 122, 136))
    out["gay_lussac_tower"] = tower
    # Distillation tray: riveted steel shell, a band of bubble caps.
    tray = _noise((150, 156, 166), 5, 3208)
    _rect(tray, 0, 0, 15, 1, (110, 114, 124))
    _rect(tray, 0, 14, 15, 15, (110, 114, 124))
    for x in (2, 6, 10):
        _rect(tray, x, 6, x + 3, 9, (96, 100, 110))
        _rect(tray, x + 1, 7, x + 2, 8, (190, 196, 206))
    for x in (1, 5, 9, 13):
        _rect(tray, x, 3, x, 3, (210, 214, 222))
        _rect(tray, x, 12, x, 12, (210, 214, 222))
    out["distillation_tray"] = tray
    # 3.13.0. Diffuser stage: a steel drum with a pump flange and pipe stubs.
    stage = _noise((168, 172, 180), 5, 3209)
    _rect(stage, 0, 0, 15, 0, (120, 124, 132))
    _rect(stage, 0, 15, 15, 15, (120, 124, 132))
    _rect(stage, 3, 3, 12, 12, (138, 142, 150))
    _rect(stage, 5, 5, 10, 10, (96, 100, 108))
    _rect(stage, 6, 6, 9, 9, (184, 188, 196))
    _rect(stage, 0, 7, 2, 8, (110, 114, 122))
    _rect(stage, 13, 7, 15, 8, (110, 114, 122))
    out["diffuser_stage"] = stage
    # Graphite: dark grey with a faint sheen and machined joints.
    graphite = _noise((52, 52, 56), 6, 3210)
    _rect(graphite, 0, 0, 15, 0, (36, 36, 40))
    _rect(graphite, 0, 0, 0, 15, (36, 36, 40))
    _rect(graphite, 2, 2, 5, 2, (78, 78, 84))
    out["graphite_block"] = graphite
    # Fuel channel: graphite round a Zircaloy tube end with the fuel's glow.
    channel = _noise((52, 52, 56), 6, 3211)
    _rect(channel, 0, 0, 15, 0, (36, 36, 40))
    _rect(channel, 0, 0, 0, 15, (36, 36, 40))
    _rect(channel, 4, 4, 11, 11, (170, 172, 160))
    _rect(channel, 5, 5, 10, 10, (120, 122, 112))
    _rect(channel, 6, 6, 9, 9, (80, 140, 210))
    _rect(channel, 7, 7, 8, 8, (150, 200, 255))
    out["fuel_channel"] = channel
    # Reactor shielding: heavy steel plate with bolts and a yellow-black band.
    shield = _noise((112, 116, 124), 5, 3212)
    for x in range(16):
        _rect(shield, x, 13, x, 15, (230, 190, 40) if (x // 2) % 2 == 0 else (30, 30, 30))
    for x in (2, 13):
        for y in (2, 9):
            _rect(shield, x, y, x, y, (190, 194, 200))
    out["reactor_shielding"] = shield
    return out


def _put(img, x, y, c, a=255):
    px = img.load()
    if a >= 255:
        px[x, y] = tuple(int(v) for v in c[:3]) + (255,)
    else:
        o = px[x, y]
        k = a / 255
        px[x, y] = tuple(int(o[i] * (1 - k) + c[i] * k) for i in range(3)) + (255,)


def _glow_joints(base, mortar, hot, i, n, seed):
    """Brickwork in a hot furnace: the mortar joints glow and the glow wanders."""
    import math
    img = base.copy()
    px = img.load()
    rnd = random.Random(seed)
    phase = [rnd.random() * 6.28 for _ in range(256)]
    for y in range(16):
        for x in range(16):
            if px[x, y][:3] == mortar:
                k = 0.45 + 0.35 * math.sin(2 * math.pi * i / n + phase[y * 16 + x])
                _put(img, x, y, hot, int(255 * k))
    return img


def active_frames(bid, base):
    """3.33.0: the frames a structure block shows while its structure works -> (frames, ticks per frame, blend)."""
    import math
    n = 8
    frames = []
    for i in range(n):
        a = 2 * math.pi * i / n
        img = base.copy()
        if bid == "refractory_brick":
            img = _glow_joints(base, (118, 100, 84), (255, 130, 40), i, n, 1)
        elif bid == "silica_brick":
            img = _glow_joints(base, (140, 138, 132), (255, 170, 80), i, n, 2)
        elif bid == "tuyere":
            k = 0.5 + 0.5 * math.sin(a)
            for y in range(6, 10):
                for x in range(6, 10):
                    inner = 7 <= x <= 8 and 7 <= y <= 8
                    _put(img, x, y, (255, 240, 170) if inner and k > 0.5 else (255, 150 + int(80 * k), 60), 255 if inner else int(120 + 120 * k))
            # The blast roaring in: a flicker round the bore.
            for x, y in ((5, 7), (10, 8), (7, 5), (8, 10)):
                _put(img, x, y, (255, 120, 40), 80 + int(100 * ((i + x + y) % 3 == 0)))
        elif bid == "structure_port":
            # Goods moving through the hatch and a green lamp blinking.
            y = 4 + (i * 8 // n)
            _put(img, 7, y, (230, 230, 200))
            _put(img, 8, y, (230, 230, 200))
            _put(img, 2, 2, (90, 255, 90) if i % 4 < 2 else (40, 110, 40))
        elif bid == "lead_sheet":
            # Drops of chamber acid running down the seams.
            for k, x in enumerate((7, 11, 3)):
                y = (i * 2 + k * 5) % 16
                _put(img, x, y, (200, 210, 150), 200)
                if y > 0:
                    _put(img, x, y - 1, (180, 190, 140), 110)
        elif bid == "gay_lussac_tower":
            # Acid trickling down through the coke.
            for k in range(3):
                x = 4 + k * 3
                y = 2 + (i + k * 3) * 11 // n % 11
                _put(img, x, y, (210, 220, 160))
        elif bid == "distillation_tray":
            # The bubble caps bubbling in turn, vapour rising between them.
            for k, x in enumerate((2, 6, 10)):
                on = (i // 2 + k) % 3 == 0
                _put(img, x + 1, 7, (240, 244, 250) if on else (150, 156, 166))
                _put(img, x + 2, 8, (240, 244, 250) if (i // 2 + k) % 3 == 1 else (150, 156, 166))
                vy = 5 - (i + k * 2) % 3
                _put(img, x + 1 + (i % 2), vy, (220, 224, 230), 140)
        elif bid == "diffuser_stage":
            # The pump rotor turning behind the flange, gas pulsing in the pipe stubs.
            cx, cy = 7.5, 7.5
            for y in range(6, 10):
                for x in range(6, 10):
                    ang = math.atan2(y - cy, x - cx) - a / 4
                    _put(img, x, y, (210, 214, 222) if math.cos(4 * ang) > 0.2 else (70, 74, 82))
            k = 0.5 + 0.5 * math.sin(a)
            for x in (0, 1, 2, 13, 14, 15):
                _put(img, x, 7, (220, 230, 140), int(80 + 140 * (k if x < 8 else 1 - k)))
        elif bid == "graphite_block":
            # Faint warmth moving through the moderator.
            for y in range(16):
                for x in range(16):
                    k = max(0, math.sin(a + (x + y) * 0.35))
                    _put(img, x, y, (120, 60, 40), int(40 * k))
        elif bid == "fuel_channel":
            # Cherenkov blue pulsing in the tube.
            k = 0.5 + 0.5 * math.sin(a)
            for y in range(5, 11):
                for x in range(5, 11):
                    d = max(abs(x - 7.5), abs(y - 7.5))
                    _put(img, x, y, (120 + 100 * k, 190 + 50 * k, 255), int(255 * max(0.2, 1 - d / 3.5)))
        elif bid == "reactor_shielding":
            # The hazard band crawling and a red lamp turning.
            for x in range(16):
                _put(img, x, 13, (230, 190, 40) if ((x + i) // 2) % 2 == 0 else (30, 30, 30))
                _put(img, x, 14, (230, 190, 40) if ((x + i) // 2) % 2 == 0 else (30, 30, 30))
                _put(img, x, 15, (230, 190, 40) if ((x + i) // 2) % 2 == 0 else (30, 30, 30))
            lamp = (255, 60, 40) if i % 4 in (0, 1) else (110, 30, 24)
            for x, y in ((7, 5), (8, 5), (7, 6), (8, 6)):
                _put(img, x, y, lamp)
        frames.append(img)
    blend = bid in ("refractory_brick", "silica_brick", "tuyere", "graphite_block", "fuel_channel")
    return frames, 2 if bid != "distillation_tray" else 3, blend


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def generate_multiblock(assets, data_root, lang, mod_id):
    data_dir = data_root / mod_id
    m = lambda n: f"{mod_id}:block/{n}"
    for bid, img in textures().items():
        img.save(assets / "textures" / "block" / f"{bid}.png")
        _dump(assets / "models" / "block" / f"{bid}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m(bid)}})
        # 3.33.0: an animated model while the structure works (AnimatedPartBlock.ACTIVE).
        frames, ft, blend = active_frames(bid, img)
        strip = Image.new("RGBA", (16, 16 * len(frames)))
        for i, f in enumerate(frames):
            strip.paste(f, (0, 16 * i))
        strip.save(assets / "textures" / "block" / f"{bid}_active.png")
        (assets / "textures" / "block" / f"{bid}_active.png.mcmeta").write_text(
            json.dumps({"animation": {"frametime": ft, "interpolate": blend}}, indent=2) + "\n", encoding="utf-8")
        _dump(assets / "models" / "block" / f"{bid}_active.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m(f"{bid}_active")}})
        _dump(assets / "blockstates" / f"{bid}.json", {"variants": {"active=false": {"model": m(bid)}, "active=true": {"model": m(f"{bid}_active")}}})
        _dump(assets / "models" / "item" / f"{bid}.json", {"parent": m(bid)})
        _dump(data_dir / "loot_table" / "blocks" / f"{bid}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": f"{mod_id}:{bid}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    path = data_root / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json"
    values = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
    values += [f"{mod_id}:{b}" for b in BLOCKS if f"{mod_id}:{b}" not in values]
    _dump(path, {"replace": False, "values": values})
    for bid, (name, desc) in BLOCKS.items():
        lang[f"block.{mod_id}.{bid}"] = name
        lang[f"block.{mod_id}.{bid}.desc"] = desc
    lang.update(LANG)


# ---- The Field Guide's "Structures" chapter. ----
# Hand-written paragraphs; each language has the same paragraphs in tools/multiblock_i18n.py. "@2 " / "@3 " mark the
# level a paragraph belongs to. guidebook.py adds the recipe grids, gains lines, layer views and 3D views after them.

TITLE = "Structures"
GUIDE_INTRO = [
    "# How structures work",
    "@2 Some machines grow into structures. Build a shell of casing blocks round an empty inside and set the machine in "
    "its wall, facing out: it becomes the controller and works with the size of the whole structure. Alone it works as "
    "before. The bigger the structure, the faster it works and the less heat it loses per item: speed = (inside "
    "volume / smallest volume)^0.85, so doubling the volume gives x1.8, not x2.",
    "@2 The controller measures its structure once a second and shows it on its screen. If a block is missing, the "
    "screen names it and its layer, and red sparks mark the spot while you hold the Field Guide. Structure Ports set "
    "anywhere in the shell let hoppers in and out as the machine's own sides do: from above into the input, from "
    "below out of the products, from the sides into fuel and the other slots.",
]
GUIDE_FURNACE = [
    "# Blast Furnace stack",
    "@2 A Charcoal Blast Furnace set in the wall of a column of Refractory Brick. The inside is 1x1 to 3x3 blocks and 3 "
    "to 10 high, open at the top; the floor and every wall block, corners too, are refractory brick, a Tuyere or a "
    "Structure Port. A Tuyere in the bottom layer is required: Bellows or a Steam Engine against it blow the whole "
    "stack. Fired with coke it runs 100 °C hotter than the furnace alone.",
]
GUIDE_BATTERY = [
    "# Coke Oven Battery",
    "@3 A row of narrow ovens in a block of Silica Brick, a Coke Oven set in the front wall of one of them. Each oven "
    "is 1 wide, 3 deep and 2 high, with a one-block heating wall between ovens; 1 to 12 ovens. Shared walls lose less "
    "heat, and from 4 ovens the battery burns its own coke-oven gas and needs no fuel.",
]
GUIDE_OUTLINE = [
    "@2 To build one, sneak and use its controller with the Field Guide in your hand: the smallest structure is drawn "
    "round it in translucent blocks (orange the shell, grey the ports, red the Tuyere, blue the tower) and each block "
    "you place takes its place. Do it again to hide it.",
]
GUIDE_CHAMBER = [
    "# Lead Chamber",
    "@3 A Lead Chamber set in the wall of a closed room of Lead Sheet: inside 3x3x3 to 9x9x6, walls, floor, roof and "
    "corners all lead or Structure Ports. A Gay-Lussac Tower, three or more Gay-Lussac Tower blocks stacked against "
    "the outside of the shell, catches the nitrous fumes and sends them back: the chamber works 25% faster.",
]
GUIDE_COLUMN = [
    "# Distillation Column",
    "@4 Distillation Trays stacked on an Oil Refinery Still, 1x1 or 3x3 (the still under the middle), 4 to 16 high; a "
    "Structure Port may stand in for any tray. Width gives speed; height gives better cuts: from 4 trays up, each "
    "tray sends more of the bitumen up as naphtha and kerosene, 40% of it at 16 trays.",
]
GUIDE_CASCADE = [
    "# Enrichment Cascade",
    "@6 An Enrichment Cascade with a solid block of Diffuser Stages behind it: 1 to 4 wide, 1 to 4 high and up to 16 "
    "long, 4 to 64 stages in all; a Structure Port may stand in for any stage. The gas runs through every stage, so "
    "each one adds speed and strips the tails further: alone the machine turns 100 natural into 14.5 enriched, 64 "
    "stages into 18.",
]
GUIDE_REACTOR = [
    "# Nuclear Reactor",
    "@6 A Nuclear Reactor set in a wall of Reactor Shielding round a core of Graphite Blocks and Fuel Channels: core "
    "3x3x3 to 9x9x7, no gaps, water touching the shielding outside. Every three Fuel Channels burn rods and make "
    "current like one reactor alone; a bigger core loses fewer neutrons at its edge, so each rod lasts longer.",
    "@6 The core must be critical (k 1 or more). Enriched rods are critical in any core. Natural rods, with heavy "
    "water in the slots, only in a big one: 7x7x7, 8x8x6, 9x9x5 and larger, like the CANDU reactors. Above k 1.3 the "
    "chain reaction runs away unless a Boron Control Rod is in the slots. If it runs away, or the water goes, the "
    "reactor scrams: the rod is lost and radiation leaks round it for half a day. It never explodes.",
]
GUIDE_T = {
    "structures": TITLE,
    "gain_furnace": "Inside {a}x{a}x{h}: {blocks} blocks, speed x{speed}, heat per item {loss}%",
    "gain_battery": "{n} ovens: {blocks} blocks, speed x{speed}, heat per item {loss}%",
    "gain_battery_gas": "{n} ovens: {blocks} blocks, speed x{speed}, no fuel",
    "assembly": "Layer by layer (the smallest; repeat the middle layers to build it higher):",
    "assembly_battery": "Layer by layer (one oven; repeat the oven and its wall to add more):",
    "view": "The structure in 3D:",
    "blocks_head": "Blocks",
    "gain_chamber": "Inside {a}x{b}x{h}: {blocks} blocks, speed x{speed}, heat per item {loss}%",
    "gain_chamber_tower": "Inside {a}x{b}x{h} with the tower: speed x{speed}, heat per item {loss}%",
    "gain_column": "{w}x{w}, {h} trays: speed x{speed}, heat per item {loss}%, {cut}% of the bitumen to light cuts",
    "assembly_chamber": "Layer by layer (the smallest; a bigger room just has longer walls):",
    "assembly_column": "Layer by layer (the smallest; add trays on top to go higher):",
    "gain_cascade": "{n} stages: speed x{speed}, heat per item {loss}%, {enriched} enriched per 100",
    "gain_reactor": "Core {a}x{b}x{h}, {ch} channels: {kj} kJ per tick, rods last x{life}, k {ke} enriched, {kn} natural",
    "assembly_cascade": "Layer by layer (the smallest; make the block wider, higher or longer for more stages):",
    "assembly_reactor": "Layer by layer (the smallest; channels in every other column, graphite between):",
}


def translations(code):
    """English -> translation for the blocks and screen lines, in one language."""
    from multiblock_i18n import BLOCKS as TB, LANG as TL
    out = {}
    for bid, (name, desc) in BLOCKS.items():
        out[name], out[desc] = TB[code][bid]
    for key, en in LANG.items():
        out[en] = TL[code][key]
    return out
