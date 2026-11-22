"""Level 8, stage 1 (3.61.0): rockets. Textures, models, blockstates, loot, recipes, strings and the Field Guide's
Space chapter, in 10 languages.

Java: com.periodicelements.space (Space registers everything and runs the countdown and the weather report;
Propellant, Rocket (reads a rocket off its pad and works out its delta-v), RocketEngineBlock, PropellantTankBlock(+Entity),
PayloadFairingBlock(+Entity), LaunchConsoleBlock, RocketEntity, SpaceState), client/SpaceClient; the six research
projects of level 8 are rows of data/research.csv, the propellants rows of substances.csv and reactions.csv. generate.py
calls generate_space and merges translations(code); guidebook.py adds the chapter from GUIDE and T.
"""
import json

from PIL import Image

from space_i18n import TR, GUIDE_I18N, T_I18N

ENGINES = ["kerolox_engine", "hydrolox_engine", "hypergolic_engine", "methalox_engine",
           "metallic_hydrogen_engine", "antimatter_engine", "fusion_drive"]   # the last three: 3.67.0, fiction
BLOCKS = ENGINES + ["propellant_tank", "payload_fairing", "launch_console"]
ITEMS = ["landing_legs", "weather_satellite", "communication_satellite"]
ITEM_UNLOCKS = set(ITEMS) | set(BLOCKS)

LANG = {
    "block.periodic_elements.kerolox_engine": "Kerosene Rocket Engine",
    "block.periodic_elements.kerolox_engine.desc": "Burns kerosene with liquid oxygen (Isp 300 s); lifts up to 6 Propellant Tanks stacked on it",
    "block.periodic_elements.hydrolox_engine": "Hydrogen Rocket Engine",
    "block.periodic_elements.hydrolox_engine.desc": "Burns liquid hydrogen with liquid oxygen (Isp 430 s); lifts up to 6 Propellant Tanks stacked on it",
    "block.periodic_elements.hypergolic_engine": "Hypergolic Rocket Engine",
    "block.periodic_elements.hypergolic_engine.desc": "Burns hydrazine with dinitrogen tetroxide (Isp 310 s): small and light, for upper stages; lifts up to 3 tanks",
    "block.periodic_elements.propellant_tank": "Propellant Tank",
    "block.periodic_elements.propellant_tank.desc": "Stack it on an engine, then use the engine's fuel and oxidizer on it (sneak to pour a whole stack)",
    "block.periodic_elements.payload_fairing": "Payload Fairing",
    "block.periodic_elements.payload_fairing.desc": "The rocket's nose: use a satellite on it to load it",
    "block.periodic_elements.launch_console": "Launch Console",
    "block.periodic_elements.launch_console.desc": "Use it near a Launch Pad to check the rocket and count down; use it again to abort",
    "item.periodic_elements.landing_legs": "Landing Legs",
    "item.periodic_elements.landing_legs.tip": "Use them on the first stage's engine: the stage flies back to the pad, keeping 15% of its propellant to land",
    "item.periodic_elements.weather_satellite": "Weather Satellite",
    "item.periodic_elements.weather_satellite.tip": "Put it in orbit: each morning it tells you when rain and storms will come",
    "item.periodic_elements.communication_satellite": "Communication Satellite",
    "item.periodic_elements.communication_satellite.tip": "Put it in orbit: your Radio Transmitters reach every player in the world",
    "entity.periodic_elements.rocket": "Rocket",
    "message.periodic_elements.rocket.needs": "Research %s first",
    "message.periodic_elements.rocket.no_pad": "No Launch Pad with an engine on it within 8 blocks",
    "message.periodic_elements.rocket.no_engine": "The bottom of the rocket must be an engine",
    "message.periodic_elements.rocket.no_fairing": "The top of the rocket must be a Payload Fairing",
    "message.periodic_elements.rocket.empty_fairing": "The fairing is empty: load a satellite into it",
    "message.periodic_elements.rocket.no_tanks": "Stage %s: the engine has no tanks",
    "message.periodic_elements.rocket.too_many": "Stage %s: this engine lifts at most %s tanks",
    "message.periodic_elements.rocket.mixed": "Stage %s: a tank holds another engine's propellant",
    "message.periodic_elements.rocket.worn": "Stage %s: the engine is worn; use a titanium alloy ingot on it",
    "message.periodic_elements.rocket.sky": "The sky above the rocket is not clear",
    "message.periodic_elements.rocket.overworld": "Rockets can only be launched from the Overworld for now",
    "message.periodic_elements.rocket.short": "%s km/s short: %s of the %s km/s orbit needs",
    "message.periodic_elements.rocket.stage": "Stage %s: %s, %s tanks, %s t of propellant, %s km/s",
    "message.periodic_elements.rocket.stage_legs": "Stage %s (flies back): %s, %s tanks, %s t of propellant, %s km/s",
    "message.periodic_elements.rocket.total": "Total %s km/s of the %s km/s orbit needs",
    "message.periodic_elements.rocket.ready": "Go for launch. Counting down; use the console again to abort",
    "message.periodic_elements.rocket.ready_reusable": "Go for launch; the first stage will fly back to the pad. Counting down; use the console again to abort",
    "message.periodic_elements.rocket.countdown": "T-%s",
    "message.periodic_elements.rocket.abort": "Launch aborted",
    "message.periodic_elements.rocket.liftoff": "Lift-off!",
    "message.periodic_elements.rocket.separation": "Stage %s separation",
    "message.periodic_elements.rocket.orbit": "%s is in orbit",
    "message.periodic_elements.rocket.landed": "The first stage has landed on the pad: refuel it to fly again",
    "message.periodic_elements.rocket.landed_worn": "The first stage has landed on the pad, but its engine is worn: use a titanium alloy ingot on it",
    "message.periodic_elements.rocket.legs_fitted": "Landing Legs fitted: as a first stage this engine flies back to the pad",
    "message.periodic_elements.rocket.repaired": "Engine repaired",
    "message.periodic_elements.tank.no_engine": "Stack the tank on an engine (or on tanks on an engine) before you fill it",
    "message.periodic_elements.tank.mixed": "This tank holds another engine's propellant: break it to empty it",
    "message.periodic_elements.tank.wrong": "This stage burns %s with %s",
    "message.periodic_elements.tank.full": "That side of the tank is full",
    "message.periodic_elements.tank.empty": "Empty tank",
    "message.periodic_elements.tank.contents": "%s %s/%s · %s %s/%s",
    "message.periodic_elements.fairing.loaded": "Fairing loaded: %s",
    "message.periodic_elements.fairing.full": "The fairing already holds %s",
    "message.periodic_elements.fairing.empty": "Empty fairing: use a satellite on it",
    "message.periodic_elements.weather.rain_in": "Weather satellite: rain in about %s minutes",
    "message.periodic_elements.weather.storm_in": "Weather satellite: a thunderstorm in about %s minutes",
    "message.periodic_elements.weather.rain_now": "Weather satellite: it is raining; it stops in about %s minutes",
    "message.periodic_elements.weather.storm_now": "Weather satellite: thunderstorm; it passes in about %s minutes",
    "message.periodic_elements.weather.clear": "Weather satellite: clear skies for at least %s minutes",
    "message.periodic_elements.weather.dry": "Weather satellite: no rain today; the next in about %s minutes",
}

# The Field Guide's Space chapter ("@N " sets a paragraph's level; a heading takes the next paragraph's).
GUIDE = [
    "@8 # The Space Centre",
    "@8 Level 8, the Space Age, is researched at the Space Centre, made from the International Research Centre. Its "
    "first projects build rockets: the Moon and Mars come with later updates.",
    "@8 # Propellants",
    "@8 Liquid oxygen comes from oxygen in the Air Liquefier (Orbital Rocket); liquid hydrogen from hydrogen in the same "
    "machine, at ten times the energy (Liquid Hydrogen Rocket). Hydrazine is made from ammonia and sodium hypochlorite in "
    "the High-Pressure Reactor, the hypochlorite from chlorine and lye in the Chlor-Alkali Cell; dinitrogen tetroxide "
    "comes from ammonia in the Ostwald Converter (Hypergolic Propellants).",
    "@8 # Building a rocket",
    "@8 A rocket stands on a Launch Pad. From the bottom up: an engine, the Propellant Tanks it lifts, then the next "
    "stage's engine and its tanks, and on top a Payload Fairing with a satellite inside. Every engine starts a new stage.",
    "@8 Fill each tank by using its stage's fuel and oxidizer on it; sneak to pour a whole stack. One item counts as one "
    "tonne. A tank of kerosene holds 16 kerosene and 40 liquid oxygen (56 t); of hydrogen, 3 liquid hydrogen and 17 "
    "liquid oxygen (20 t, because hydrogen is so light); of hypergolics, 28 hydrazine and 32 dinitrogen tetroxide (60 t).",
    "@8 Empty, an engine weighs 3 t (hypergolic 1.5 t), a tank 2 t, the fairing and a satellite 1 t each.",
    "@8 # Delta-v",
    "@8 Each stage's speed gain comes from the rocket equation: delta-v = exhaust velocity × ln(mass at ignition / mass "
    "at burnout), where everything above the stage counts as its load. The exhaust velocity is the engine's Isp × 9.8: "
    "kerosene 300 s, hydrogen 430 s, hypergolic 310 s.",
    "@8 Low orbit needs 9.4 km/s (7.8 km/s of orbital speed plus losses to gravity and air). One stage cannot reach "
    "it: the best kerosene rocket with one stage makes 8.9 km/s. Two stages can, for example a kerosene engine with 4 "
    "full tanks under a kerosene engine with 2.",
    "@8 # Launch",
    "@8 Use the Launch Console within 8 blocks of the pad. It lists each stage with its delta-v and says what is wrong; "
    "if the rocket can reach orbit, it counts down from 10. Use it again to abort. Each stage burns, separates and falls "
    "away downrange; the last one leaves the payload in orbit.",
    "@8 # Satellites",
    "@8 The Atlas Satellite maps for your Tracking Stations, as before (the old Launch Pad shortcut still works for it). "
    "A Weather Satellite tells you each morning when rain and thunderstorms will come. A Communication Satellite lets "
    "your Radio Transmitters share your Atlas markers with every player in the world, not only within 512 blocks.",
    "@8 # Reusable rockets",
    "@8 After Propulsive Landing, use Landing Legs on the first stage's engine. That stage keeps 15% of its propellant "
    "for the way home, so the rocket loses some delta-v; after separation it flies back and lands upright on the pad, "
    "empty, ready to be refuelled. One landing in ten wears the engine: use a titanium alloy ingot on it before it flies "
    "again.",
]

T = {"space": "Space"}

# 3.62.0, stage 2: meteorites and the Space Telescope join the Space chapter
import meteorites as _met  # noqa: E402
import fusion as _fus  # noqa: E402
import moon as _moon  # noqa: E402
import moon2 as _moon2  # noqa: E402
import mars as _mars  # noqa: E402
import fiction as _fic  # noqa: E402
import inner as _inner  # noqa: E402
GUIDE = GUIDE + _met.GUIDE + _fus.GUIDE + _moon.GUIDE + _moon2.GUIDE + _mars.GUIDE + _fic.GUIDE + _inner.GUIDE
for _code in list(GUIDE_I18N):
    GUIDE_I18N[_code] = list(GUIDE_I18N[_code]) + list(_met.GUIDE_I18N[_code]) + list(_fus.GUIDE_I18N[_code]) \
        + list(_moon.GUIDE_I18N[_code]) + list(_moon2.GUIDE_I18N[_code]) + list(_mars.GUIDE_I18N[_code]) \
        + list(_fic.GUIDE_I18N[_code]) + list(_inner.GUIDE_I18N[_code])
ITEM_UNLOCKS = ITEM_UNLOCKS | _met.ITEM_UNLOCKS | _fus.ITEM_UNLOCKS | _moon.ITEM_UNLOCKS | _mars.ITEM_UNLOCKS | _fic.ITEM_UNLOCKS | _inner.ITEM_UNLOCKS

for _code, _paras in GUIDE_I18N.items():
    assert len(_paras) == len(GUIDE), (_code, len(_paras), len(GUIDE))
    GUIDE_I18N[_code] = [(en.split(" ", 1)[0] + " " if en.startswith("@") else "") + t for en, t in zip(GUIDE, _paras)]


def translations(code):
    """English -> translation for these strings, the level 8 research, propellants and reactions in one language."""
    return {**TR.get(code, {}), **_met.translations(code), **_fus.translations(code), **_moon.translations(code), **_moon2.translations(code), **_mars.translations(code), **_fic.translations(code), **_inner.translations(code)}


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _img(c=(0, 0, 0, 0)):
    return Image.new("RGBA", (16, 16), c)


def _rect(img, x0, y0, x1, y1, c):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(c) + (255,) if len(c) == 3 else tuple(c)


def _noise(base, var, seed):
    import random
    rnd = random.Random(seed)
    img = _img()
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = rnd.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


# engine id -> (bell colour, throat colour, plate colour)
ENGINE_COLOURS = {"kerolox_engine": ((70, 62, 58), (190, 110, 60), (196, 198, 204)),
                  "hydrolox_engine": ((150, 156, 166), (220, 225, 235), (222, 224, 228)),
                  "hypergolic_engine": ((60, 56, 50), (214, 170, 70), (170, 150, 110)),
                  "methalox_engine": ((54, 52, 56), (150, 170, 220), (176, 180, 188)),   # 3.66.0: steel bell, blue methane flame
                  # 3.67.0, fiction: silvery metallic hydrogen, a violet antimatter throat, a pink fusion plasma
                  "metallic_hydrogen_engine": ((150, 156, 170), (230, 236, 255), (200, 204, 214)),
                  "antimatter_engine": ((40, 40, 52), (255, 190, 255), (150, 120, 190)),
                  "fusion_drive": ((60, 50, 80), (255, 140, 230), (120, 100, 150))}


def block_textures():
    out = {}
    for eid, (bell, throat, plate) in ENGINE_COLOURS.items():
        for worn in (False, True):
            soot = 0.55 if worn else 1.0
            nozzle = _img()
            for y in range(16):
                f = y / 15
                c = tuple(int((throat[i] * (1 - f) + bell[i] * f) * soot) for i in range(3))
                _rect(nozzle, 0, y, 15, y, c)
            for x in (3, 7, 11):   # regenerative cooling tubes
                for y in range(16):
                    r, g, b, a = nozzle.getpixel((x, y))
                    nozzle.putpixel((x, y), (max(0, r - 25), max(0, g - 25), max(0, b - 25), 255))
            out[f"{eid}_nozzle{'_worn' if worn else ''}"] = nozzle
            p = _noise(tuple(int(c * soot) for c in plate), 6, hash(eid) % 997)
            _rect(p, 0, 0, 15, 1, tuple(int(c * 0.7 * soot) for c in plate))
            for x, y in ((2, 4), (13, 4), (2, 11), (13, 11)):
                _rect(p, x, y, x, y, (90, 90, 96))
            out[f"{eid}_plate{'_worn' if worn else ''}"] = p
    pump = _noise((120, 124, 132), 10, 77)
    _rect(pump, 5, 3, 10, 12, (90, 94, 100))
    _rect(pump, 6, 6, 9, 9, (200, 140, 60))   # turbopump
    out["rocket_turbopump"] = pump
    legs = _noise((40, 40, 44), 4, 78)
    _rect(legs, 0, 0, 15, 1, (200, 200, 205))
    out["landing_leg"] = legs
    tank = _noise((236, 236, 238), 3, 79)
    _rect(tank, 0, 0, 15, 0, (150, 152, 158))
    _rect(tank, 0, 15, 15, 15, (150, 152, 158))
    _rect(tank, 0, 6, 7, 9, (24, 24, 28))   # the black-and-white roll pattern of early rockets
    out["propellant_tank_side"] = tank
    top = _noise((180, 182, 188), 5, 80)
    _rect(top, 6, 6, 9, 9, (120, 122, 128))
    out["propellant_tank_top"] = top
    out["payload_fairing"] = _noise((240, 240, 242), 3, 81)
    console = _noise((70, 76, 86), 5, 82)
    _rect(console, 1, 2, 14, 8, (14, 16, 22))
    for i, x in enumerate(range(2, 14, 3)):
        _rect(console, x, 3, x + 1, 4, [(80, 220, 120), (240, 200, 70), (230, 70, 60), (90, 160, 240)][i])
    _rect(console, 2, 6, 13, 6, (80, 220, 120))
    _rect(console, 3, 11, 5, 13, (230, 50, 40))   # the big red button
    _rect(console, 8, 11, 13, 12, (160, 166, 176))
    out["launch_console_front"] = console
    out["launch_console_side"] = _noise((70, 76, 86), 5, 83)
    out["launch_console_top"] = _noise((90, 96, 106), 5, 84)
    return out


def item_texture(iid):
    img = _img()
    if iid == "landing_legs":
        for x0, d in ((3, 1), (12, -1)):
            for k in range(10):
                _rect(img, x0 + d * (k // 3), 3 + k, x0 + d * (k // 3) + 1, 3 + k, (40, 40, 46))
            _rect(img, x0 - 2 + 2 * (d < 0), 13, x0 + 2 + 2 * (d < 0) - 2, 14, (200, 200, 205))
        _rect(img, 5, 2, 10, 4, (180, 182, 190))
        return img
    if iid in ("weather_satellite", "communication_satellite"):
        panel = (40, 60, 140)
        for x0 in (0, 11):
            _rect(img, x0, 6, x0 + 4, 9, panel)
            _rect(img, x0 + 2, 6, x0 + 2, 9, (120, 140, 200))
        _rect(img, 5, 7, 10, 8, (150, 150, 156))
        _rect(img, 6, 4, 9, 11, (220, 190, 80) if iid == "weather_satellite" else (210, 210, 216))
        if iid == "weather_satellite":
            _rect(img, 7, 12, 8, 13, (30, 30, 34))   # the camera
        else:
            _rect(img, 6, 1, 9, 2, (230, 230, 236))   # the dish
            _rect(img, 7, 3, 8, 3, (150, 150, 156))
        return img
    raise KeyError(iid)


def _faces(tex, faces=("north", "south", "east", "west", "up", "down")):
    return {f: {"texture": tex} for f in faces}


def _engine_model(mod, eid, legs, worn):
    w = "_worn" if worn else ""
    els = [{"from": [0, 12, 0], "to": [16, 16, 16], "faces": _faces("#plate")},
           {"from": [4, 7, 4], "to": [12, 12, 12], "faces": _faces("#pump", ("north", "south", "east", "west"))},
           {"from": [3, 3, 3], "to": [13, 7, 13], "faces": _faces("#nozzle", ("north", "south", "east", "west", "down"))},
           {"from": [1, 0, 1], "to": [15, 3, 15], "faces": _faces("#nozzle", ("north", "south", "east", "west", "down"))}]
    if legs:
        for x0, z0 in ((-4, 7), (18, 7), (7, -4), (7, 18)):
            els.append({"from": [x0, 0, z0], "to": [x0 + 2, 13, z0 + 2], "faces": _faces("#leg")})
        els += [{"from": [-4, 11, 7], "to": [0, 13, 9], "faces": _faces("#leg")},
                {"from": [16, 11, 7], "to": [20, 13, 9], "faces": _faces("#leg")},
                {"from": [7, 11, -4], "to": [9, 13, 0], "faces": _faces("#leg")},
                {"from": [7, 11, 16], "to": [9, 13, 20], "faces": _faces("#leg")}]
    return {"parent": "minecraft:block/block",
            "textures": {"particle": f"{mod}:block/{eid}_plate{w}", "plate": f"{mod}:block/{eid}_plate{w}",
                         "nozzle": f"{mod}:block/{eid}_nozzle{w}", "pump": f"{mod}:block/rocket_turbopump",
                         "leg": f"{mod}:block/landing_leg"},
            "elements": els}


def _fairing_model(mod):
    tex = f"{mod}:block/payload_fairing"
    els = []
    for (a, y0, y1) in ((1, 0, 6), (3, 6, 11), (5, 11, 14), (7, 14, 16)):
        els.append({"from": [a, y0, a], "to": [16 - a, y1, 16 - a], "faces": _faces("#f")})
    return {"parent": "minecraft:block/block", "textures": {"particle": tex, "f": tex}, "elements": els}


def _shaped(pattern, key, result, count=1):
    return {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
            "key": {k: {"item": v} for k, v in key.items()}, "result": {"id": f"periodic_elements:{result}", "count": count}}


P = "periodic_elements:"
RECIPES = {
    "kerolox_engine": (["SIS", "SCS", "N N"], {"S": P + "stainless_steel_ingot", "I": P + "integrated_circuit",
                                               "C": "minecraft:copper_block", "N": P + "niobium"}, 1),
    "hydrolox_engine": (["AIA", "SCS", "N N"], {"A": P + "aluminium", "S": P + "stainless_steel_ingot",
                                                "I": P + "integrated_circuit", "C": "minecraft:copper_block", "N": P + "niobium"}, 1),
    "methalox_engine": (["SIS", "SCS", "TNT"], {"S": P + "stainless_steel_ingot", "I": P + "integrated_circuit",
                                                "C": "minecraft:copper_block", "T": P + "titanium_alloy_ingot", "N": P + "niobium"}, 1),
    "metallic_hydrogen_engine": (["TIT", "SCS", "N N"], {"T": P + "titanium_alloy_ingot", "I": P + "integrated_circuit",
                                                         "S": P + "stainless_steel_ingot", "C": "minecraft:diamond",
                                                         "N": P + "niobium"}, 1),
    "antimatter_engine": (["PIP", "SMS", "N N"], {"P": P + "room_temperature_superconductor", "I": P + "integrated_circuit",
                                                  "S": P + "stability_alloy_ingot", "M": P + "magnetic_shield",
                                                  "N": P + "niobium"}, 1),
    "fusion_drive": (["PIP", "STS", "N N"], {"P": P + "room_temperature_superconductor", "I": P + "integrated_circuit",
                                             "S": P + "stability_alloy_ingot", "T": P + "tokamak", "N": P + "niobium"}, 1),
    "hypergolic_engine": (["TIT", " N "], {"T": P + "titanium_alloy_ingot", "I": P + "integrated_circuit", "N": P + "niobium"}, 1),
    "propellant_tank": (["AAA", "A A", "AAA"], {"A": P + "aluminium"}, 2),
    "payload_fairing": ([" C ", "CSC"], {"C": P + "composite_plate", "S": P + "titanium_alloy_ingot"}, 1),
    "launch_console": (["GGG", "IWI", "SSS"], {"G": "minecraft:glass_pane", "I": P + "integrated_circuit",
                                               "W": P + "copper_wire", "S": P + "stainless_steel_ingot"}, 1),
    "landing_legs": (["T T", "T T", "A A"], {"T": P + "titanium_alloy_ingot", "A": P + "aluminium"}, 1),
    "weather_satellite": (["PCP", "TST", "T T"], {"P": P + "solar_panel", "C": P + "integrated_circuit",
                                                  "S": P + "silicon_wafer", "T": P + "titanium_alloy_ingot"}, 1),
    "communication_satellite": (["PGP", "TCT", "T T"], {"P": P + "solar_panel", "G": P + "gallium_arsenide_wafer",
                                                        "C": P + "integrated_circuit", "T": P + "titanium_alloy_ingot"}, 1),
}


def generate_space(assets, data_root, lang, mod):
    tex_item = assets / "textures" / "item"
    tex_block = assets / "textures" / "block"
    models = assets / "models"
    states = assets / "blockstates"
    for iid in ITEMS:
        item_texture(iid).save(tex_item / f"{iid}.png")
        _dump(models / "item" / f"{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod}:item/{iid}"}})
    for name, img in block_textures().items():
        img.save(tex_block / f"{name}.png")
    for eid in ENGINES:
        variants = {}
        for legs in (False, True):
            for worn in (False, True):
                name = eid + ("_legs" if legs else "") + ("_worn" if worn else "")
                _dump(models / "block" / f"{name}.json", _engine_model(mod, eid, legs, worn))
                variants[f"legs={str(legs).lower()},worn={str(worn).lower()}"] = {"model": f"{mod}:block/{name}"}
        _dump(states / f"{eid}.json", {"variants": variants})
        _dump(models / "item" / f"{eid}.json", {"parent": f"{mod}:block/{eid}"})
    _dump(models / "block" / "propellant_tank.json", {"parent": "minecraft:block/cube_column", "textures": {
        "side": f"{mod}:block/propellant_tank_side", "end": f"{mod}:block/propellant_tank_top"}})
    _dump(states / "propellant_tank.json", {"variants": {"": {"model": f"{mod}:block/propellant_tank"}}})
    _dump(models / "item" / "propellant_tank.json", {"parent": f"{mod}:block/propellant_tank"})
    _dump(models / "block" / "payload_fairing.json", _fairing_model(mod))
    _dump(states / "payload_fairing.json", {"variants": {"": {"model": f"{mod}:block/payload_fairing"}}})
    _dump(models / "item" / "payload_fairing.json", {"parent": f"{mod}:block/payload_fairing"})
    _dump(models / "block" / "launch_console.json", {"parent": "minecraft:block/orientable", "textures": {
        "front": f"{mod}:block/launch_console_front", "side": f"{mod}:block/launch_console_side",
        "top": f"{mod}:block/launch_console_top"}})
    _dump(states / "launch_console.json", {"variants": {
        f"facing={f}": ({"model": f"{mod}:block/launch_console", "y": y} if y else {"model": f"{mod}:block/launch_console"})
        for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    _dump(models / "item" / "launch_console.json", {"parent": f"{mod}:block/launch_console"})

    data_dir = data_root / mod
    for b in BLOCKS:
        pools = [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{b}"}],
                  "conditions": [{"condition": "minecraft:survives_explosion"}]}]
        if b in ENGINES:
            pools.append({"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:landing_legs"}],
                          "conditions": [{"condition": "minecraft:block_state_property", "block": f"{mod}:{b}",
                                          "properties": {"legs": "true"}},
                                         {"condition": "minecraft:survives_explosion"}]})
        _dump(data_dir / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": pools})
    for rid, (pattern, key, count) in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", _shaped(pattern, key, rid, count))
    # 3.62.0: the rocket blocks were missing from the pickaxe tag, so they dropped nothing when broken
    tag = data_root / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json"
    values = json.loads(tag.read_text(encoding="utf-8"))["values"] if tag.exists() else []
    values += [f"{mod}:{b}" for b in BLOCKS if f"{mod}:{b}" not in values]
    _dump(tag, {"replace": False, "values": values})
    lang.update(LANG)
    _met.generate_meteorites(assets, data_root, lang, mod)
    _fus.generate_fusion(assets, data_root, lang, mod)
    _moon.generate_moon(assets, data_root, lang, mod)
    _moon2.generate_moon2(assets, data_root, lang, mod)
    _mars.generate_mars(assets, data_root, lang, mod)
    _fic.generate_fiction(assets, data_root, lang, mod)
    _inner.generate_inner(assets, data_root, lang, mod)
