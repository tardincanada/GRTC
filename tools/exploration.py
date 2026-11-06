"""Exploration gear (3.47.0): textures, models, blockstates, loot, recipes, the trap_small entity tag, strings and the
Field Guide's Exploration chapter, in 10 languages.

Java: com.periodicelements.explore (Explore registers everything and runs the worn gear; HeadGearItem, ExplorerLightBlock,
StreetLampBlock(+Entity), TentBlock, BackpackItem(+ScreenHandler), ExplorerCompassItem, TrapItem, TrapEntity),
client/ExploreClient, mixins HostileEntityMixin (no spawns by lit street lamps), MinecraftClientMixin (goggle outlines),
ItemEntityMixin (backpack pickup) and CraftingResultSlotMixin (crafting needs the research). The ten research projects
are rows of data/research.csv. generate.py calls generate_exploration and merges translations(code); guidebook.py adds
the chapter from GUIDE and T.
"""
import json
import math

from PIL import Image, ImageDraw

from exploration_i18n import TR, GUIDE_I18N, T_I18N

ITEMS = ["electric_cap_lamp", "led_spotlight_helmet", "night_vision_goggles", "leather_backpack", "nylon_backpack",
         "explorer_compass", "rope_net", "steel_cage"]
BLOCKS = ["tent", "sodium_lamp", "led_lamp"]
ITEM_UNLOCKS = set(ITEMS) | set(BLOCKS)

SMALL_ANIMALS = ["chicken", "rabbit", "pig", "sheep", "cat", "ocelot", "fox", "parrot", "bee", "frog", "axolotl",
                 "armadillo", "turtle"]

LANG = {
    "item.periodic_elements.electric_cap_lamp": "Electric Cap Lamp",
    "item.periodic_elements.electric_cap_lamp.tip": "Wear it: a tungsten bulb gives light 15 that follows you",
    "item.periodic_elements.led_spotlight_helmet": "LED Spotlight Helmet",
    "item.periodic_elements.led_spotlight_helmet.tip": "Wear it: light 15 around you and a beam that lights the spot you look at, up to 32 blocks",
    "item.periodic_elements.night_vision_goggles": "Night Vision Goggles",
    "item.periodic_elements.night_vision_goggles.tip": "Wear them: you see in the dark, and monsters within 32 blocks are outlined",
    "item.periodic_elements.head_gear.draw": "Uses %s kJ a second from a battery pack in your pack",
    "item.periodic_elements.head_gear.on": "Charged and on",
    "block.periodic_elements.sodium_lamp": "Sodium Lamp",
    "block.periodic_elements.sodium_lamp.desc": "Set it on a fence or wall and feed it by copper wire: yellow light, no monster spawns within 24 blocks",
    "block.periodic_elements.led_lamp": "LED Lamp",
    "block.periodic_elements.led_lamp.desc": "Set it on a fence or wall and feed it by copper wire: white light, no monster spawns within 40 blocks",
    "block.periodic_elements.tent": "Tent",
    "block.periodic_elements.tent.desc": "Sleep in it to pass the night; it never moves your spawn point. Sneak and use it to strike it",
    "block.periodic_elements.explorer_light": "Lamp Light",
    "item.periodic_elements.leather_backpack": "Leather Backpack",
    "item.periodic_elements.nylon_backpack": "Nylon Backpack",
    "item.periodic_elements.backpack.tip": "Holds anything but backpacks and shulker boxes, %s places. Use it to open, or press B with it in your pack; sneak and use to switch picking up",
    "item.periodic_elements.backpack.pickup_on": "Picks up the plant and mineral specimens you walk over",
    "item.periodic_elements.backpack.pickup_off": "Does not pick up (sneak and use to switch on)",
    "message.periodic_elements.backpack.on": "Backpack: picks up specimens",
    "message.periodic_elements.backpack.off": "Backpack: does not pick up",
    "key.periodic_elements.backpack": "Open backpack",
    "item.periodic_elements.explorer_compass": "Explorer's Compass",
    "item.periodic_elements.explorer_compass.tip": "Sneak and use to choose a biome; use it to point to the nearest one",
    "item.periodic_elements.explorer_compass.biome": "Biome: %s",
    "item.periodic_elements.explorer_compass.target": "Points to %s, %s",
    "message.periodic_elements.explorer_compass.chosen": "Explorer's Compass: %s",
    "message.periodic_elements.explorer_compass.choose": "Sneak and use it to choose a biome first",
    "message.periodic_elements.explorer_compass.none": "No %s within %s blocks",
    "message.periodic_elements.explorer_compass.found": "%s: %s blocks away",
    "item.periodic_elements.rope_net": "Rope Net",
    "item.periodic_elements.rope_net.tip": "Throw it at a small animal to catch it; throw it again to let it out",
    "item.periodic_elements.steel_cage": "Steel Cage",
    "item.periodic_elements.steel_cage.tip": "Throw it at any animal to catch it; throw it again to let it out",
    "item.periodic_elements.trap.full_name": "%s (%s)",
    "item.periodic_elements.trap.holds": "Holds: %s",
    "message.periodic_elements.tent.no_sleep": "You cannot sleep here",
    "entity.periodic_elements.thrown_trap": "Thrown Trap",
}

# The Field Guide's Exploration chapter ("@N " sets a paragraph's level; a heading takes the next paragraph's).
GUIDE = [
    "@1 # Camping",
    "@1 Hide Tent (level 1): six leather over three sticks make a Tent. Sleep in it as in a bed to pass the night, with "
    "no monsters near; it never moves your spawn point. Sneak and use it to strike it and take it with you.",
    "@1 # Backpacks",
    "@1 Leather Backpack (level 1): 27 places for anything but backpacks and shulker boxes. Use it to open it, or "
    "press B (Controls) with it anywhere in your pack. While picking up is on (sneak and use to switch), the plant "
    "specimens (saplings, seeds, flowers, leaves, mushrooms) and minerals (ores, raw ores, gems, crystals) you walk "
    "over go into it first.",
    "@6 Nylon Backpack (level 6): the same with 54 places.",
    "@1 # Traps",
    "@1 Hunting Nets (level 1): five string make a Rope Net. Throw it at a small animal (chicken, rabbit, pig, sheep, "
    "cat, fox, parrot, bee, frog and the like) to catch it, and throw the full net to let it out where it lands; the "
    "net drops there either way. It wears a little with every catch.",
    "@3 Animal Cage (level 3): the Steel Cage holds any animal, a cow or a horse too, and never wears out. No trap takes "
    "monsters, villagers, bosses, ridden animals or another player's pet.",
    "@2 # Finding your way",
    "@2 Explorer's Compass (level 2): sneak and use it to choose a biome, one after another; use it to find the "
    "nearest within 6400 blocks. The needle then points there and the tooltip gives the place.",
    "@5 # Lights",
    "@5 Electric Cap Lamp (level 5): wear it and it gives light 15 that follows you, on 0.25 kJ a second from a "
    "battery pack in your pack. Without charge it is dark.",
    "@5 Sodium Street Lighting (level 5): set a Sodium Lamp on a fence or wall and run copper wire to the lamp or to "
    "the foot of its post. Lit, it gives light 15 and no monster spawns by itself within 24 blocks; it takes 1 kJ a "
    "second.",
    "@6 Night Vision (level 6): Night Vision Goggles give night vision and outline monsters within 32 blocks, even "
    "through walls, on 0.1 kJ a second.",
    "@7 White LED (level 7): the LED Lamp keeps monsters away within 40 blocks on 0.4 kJ a second, and the LED "
    "Spotlight Helmet adds a beam that lights the spot you look at, up to 32 blocks away.",
]

T = {"exploration": "Exploration"}

# Translated paragraphs carry the English "@N " level prefixes, so headings and levels line up.
for _code, _paras in GUIDE_I18N.items():
    assert len(_paras) == len(GUIDE), (_code, len(_paras), len(GUIDE))
    GUIDE_I18N[_code] = [(en.split(" ", 1)[0] + " " if en.startswith("@") else "") + t for en, t in zip(GUIDE, _paras)]
assert set(LANG.values()) <= set(__import__("exploration_i18n").EN), set(LANG.values()) - set(__import__("exploration_i18n").EN)


def translations(code):
    """English -> translation for these strings (and the ten research projects) in one language."""
    return dict(TR.get(code, {}))


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _img():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def _rect(img, x0, y0, x1, y1, c):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(c) + (255,) if len(c) == 3 else tuple(c)


def _helmet(shell, rim, lamp, glow):
    img = _img()
    _rect(img, 4, 4, 11, 5, shell)
    _rect(img, 3, 6, 12, 10, shell)
    _rect(img, 2, 11, 13, 12, rim)
    _rect(img, 5, 5, 10, 5, tuple(min(255, v + 40) for v in shell))
    _rect(img, 6, 6, 9, 9, (40, 40, 44))
    _rect(img, 7, 7, 8, 8, lamp)
    for x, y in [(6, 6), (9, 6), (6, 9), (9, 9)]:
        img.load()[x, y] = tuple(glow) + (255,)
    return img


def item_texture(iid):
    img = _img()
    if iid == "electric_cap_lamp":
        return _helmet((70, 74, 82), (46, 48, 54), (255, 220, 120), (200, 170, 90))
    if iid == "led_spotlight_helmet":
        return _helmet((236, 200, 60), (170, 140, 40), (230, 245, 255), (150, 200, 255))
    if iid == "night_vision_goggles":
        _rect(img, 1, 7, 14, 8, (40, 40, 40))
        for x0 in (3, 9):
            _rect(img, x0, 5, x0 + 4, 10, (60, 64, 60))
            _rect(img, x0 + 1, 6, x0 + 3, 9, (30, 120, 50))
            _rect(img, x0 + 1, 6, x0 + 1, 6, (150, 255, 160))
        _rect(img, 7, 7, 8, 8, (60, 64, 60))
        return img
    if iid in ("leather_backpack", "nylon_backpack"):
        body, dark, strap = ((139, 90, 52), (100, 62, 34), (70, 42, 24)) if iid == "leather_backpack" \
            else ((40, 90, 170), (28, 62, 120), (230, 120, 40))
        _rect(img, 4, 2, 11, 3, dark)
        _rect(img, 3, 3, 12, 14, dark)
        _rect(img, 4, 4, 11, 13, body)
        _rect(img, 4, 4, 11, 6, dark)
        _rect(img, 5, 9, 10, 12, dark)
        _rect(img, 6, 10, 9, 11, body)
        _rect(img, 5, 6, 5, 8, strap)
        _rect(img, 10, 6, 10, 8, strap)
        _rect(img, 7, 7, 8, 7, (222, 180, 80))
        return img
    if iid in ("rope_net", "rope_net_full"):
        rope = (200, 170, 110)
        for i in range(2, 14, 3):
            _rect(img, i, 2, i, 13, rope)
            _rect(img, 2, i, 13, i, rope)
        if iid.endswith("_full"):
            _rect(img, 5, 6, 10, 10, (230, 230, 225))
            _rect(img, 10, 5, 11, 7, (230, 230, 225))
            _rect(img, 11, 6, 11, 6, (40, 40, 40))
            _rect(img, 6, 11, 6, 12, (200, 120, 60))
            _rect(img, 9, 11, 9, 12, (200, 120, 60))
            for i in range(2, 14, 3):
                _rect(img, i, 2, i, 13, rope)
        return img
    if iid in ("steel_cage", "steel_cage_full"):
        bar = (150, 156, 166)
        _rect(img, 2, 3, 13, 3, (110, 114, 122))
        _rect(img, 2, 13, 13, 14, (110, 114, 122))
        if iid.endswith("_full"):
            _rect(img, 4, 7, 11, 11, (120, 80, 50))
            _rect(img, 10, 6, 12, 8, (120, 80, 50))
            _rect(img, 11, 6, 11, 6, (20, 20, 20))
            _rect(img, 5, 12, 5, 12, (80, 50, 30))
            _rect(img, 10, 12, 10, 12, (80, 50, 30))
        for x in range(2, 14, 2):
            _rect(img, x, 4, x, 12, bar)
        _rect(img, 7, 1, 8, 2, (110, 114, 122))
        return img
    raise KeyError(iid)


def compass_frame(a):
    """The Explorer's Compass with its needle turned a (0..1) of a turn clockwise from straight up."""
    big = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    d.ellipse((4, 4, 59, 59), fill=(120, 80, 30, 255))
    d.ellipse((10, 10, 53, 53), fill=(205, 165, 70, 255))
    d.ellipse((14, 14, 49, 49), fill=(40, 96, 72, 255))
    for k in range(4):
        t = k * math.pi / 2
        d.ellipse((31.5 + 15 * math.sin(t) - 2, 31.5 - 15 * math.cos(t) - 2, 31.5 + 15 * math.sin(t) + 2,
                   31.5 - 15 * math.cos(t) + 2), fill=(200, 220, 190, 255))
    t = a * 2 * math.pi
    tip = (31.5 + 15 * math.sin(t), 31.5 - 15 * math.cos(t))
    tail = (31.5 - 10 * math.sin(t), 31.5 + 10 * math.cos(t))
    d.line([tail, (31.5, 31.5)], fill=(230, 230, 230, 255), width=5)
    d.line([(31.5, 31.5), tip], fill=(220, 40, 40, 255), width=5)
    d.ellipse((28, 28, 35, 35), fill=(60, 50, 40, 255))
    return big.resize((16, 16), Image.BOX)


def block_textures():
    out = {}
    tent = Image.new("RGBA", (16, 16))
    px = tent.load()
    for y in range(16):
        for x in range(16):
            v = 8 * ((x * 7 + y * 3) % 5 == 0) - 6 * (y % 4 == 0)
            px[x, y] = (196 + v, 168 + v, 118 + v, 255)
    out["tent"] = tent
    door = tent.copy()
    _rect(door, 6, 4, 9, 15, (90, 66, 40))
    _rect(door, 7, 5, 8, 15, (60, 44, 28))
    out["tent_door"] = door
    for kind, on, off in (("sodium", (255, 176, 40), (120, 100, 60)), ("led", (240, 248, 255), (150, 156, 170))):
        for lit in (False, True):
            img = Image.new("RGBA", (16, 16))
            _rect(img, 0, 0, 15, 15, (70, 72, 78))
            _rect(img, 1, 1, 14, 14, on if lit else off)
            if lit:
                _rect(img, 4, 4, 11, 11, tuple(min(255, c + 30) for c in on))
            out[f"{kind}_lamp{'_lit' if lit else ''}"] = img
    housing = Image.new("RGBA", (16, 16))
    _rect(housing, 0, 0, 15, 15, (70, 72, 78))
    _rect(housing, 1, 1, 14, 14, (96, 100, 108))
    out["lamp_housing"] = housing
    return out


def _lamp_model(mod_id, kind, lit):
    glass = f"{mod_id}:block/{kind}_lamp{'_lit' if lit else ''}"
    house = f"{mod_id}:block/lamp_housing"
    return {"parent": "minecraft:block/block", "textures": {"particle": house, "glass": glass, "house": house},
            "elements": [
                {"from": [2, 10, 2], "to": [14, 13, 14], "faces": {f: {"texture": "#house"} for f in
                                                                   ("north", "south", "east", "west", "up", "down")}},
                {"from": [4, 4, 4], "to": [12, 10, 12], "shade": not lit,
                 "faces": {f: {"texture": "#glass"} for f in ("north", "south", "east", "west", "down")}},
                {"from": [6, 0, 6], "to": [10, 4, 10], "faces": {f: {"texture": "#house"} for f in
                                                                 ("north", "south", "east", "west", "down")}},
            ]}


def _tent_model(mod_id):
    tex = f"{mod_id}:block/tent"
    els = []
    for i in range(7):
        x0, x1 = i, 16 - i
        y0, y1 = 2 * i, 2 * i + 2
        faces = {"up": {"texture": "#tent"}, "east": {"texture": "#tent"}, "west": {"texture": "#tent"},
                 "north": {"texture": "#tent"}, "south": {"texture": "#door" if i < 4 else "#tent"}}
        if i == 0:
            faces["down"] = {"texture": "#tent"}
        els.append({"from": [x0, y0, 0], "to": [x1, y1, 16], "faces": faces})
    els.append({"from": [7, 14, 0], "to": [9, 16, 16], "faces": {f: {"texture": "#tent"} for f in
                                                                 ("north", "south", "east", "west", "up")}})
    return {"parent": "minecraft:block/block", "textures": {"particle": tex, "tent": tex, "door": f"{mod_id}:block/tent_door"},
            "elements": els}


def _shaped(pattern, key, result, count=1):
    return {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
            "key": {k: {"item": v} for k, v in key.items()}, "result": {"id": f"periodic_elements:{result}", "count": count}}


RECIPES = {
    "tent": ([" L ", "LLL", "S S"], {"L": "minecraft:leather", "S": "minecraft:stick"}, 1),
    "leather_backpack": (["SLS", "LCL", "LLL"], {"L": "minecraft:leather", "S": "minecraft:string", "C": "minecraft:chest"}, 1),
    "nylon_backpack": (["SNS", "NCN", "NAN"], {"N": "periodic_elements:nylon", "S": "minecraft:string", "C": "minecraft:chest",
                                               "A": "periodic_elements:aluminium"}, 1),
    "rope_net": (["S S", " S ", "S S"], {"S": "minecraft:string"}, 1),
    "steel_cage": (["IBI", "B B", "IBI"], {"I": "periodic_elements:crucible_steel_ingot", "B": "minecraft:iron_bars"}, 1),
    "explorer_compass": ([" G ", "GCG", " M "], {"G": "minecraft:gold_ingot", "C": "minecraft:compass", "M": "minecraft:map"}, 1),
    "electric_cap_lamp": (["STS", "SGS", " W "], {"S": "periodic_elements:steel_ingot", "T": "periodic_elements:tungsten",
                                                 "G": "minecraft:glass_pane", "W": "periodic_elements:copper_wire"}, 1),
    "led_spotlight_helmet": (["AEA", "AGA", " W "], {"A": "periodic_elements:aluminium", "E": "periodic_elements:gallium_arsenide_wafer",
                                                    "G": "minecraft:glass_pane", "W": "periodic_elements:copper_wire"}, 1),
    "night_vision_goggles": (["LWL", "GCG"], {"L": "minecraft:leather", "W": "periodic_elements:copper_wire",
                                              "G": "minecraft:glass_pane", "C": "periodic_elements:caesium"}, 1),
    "sodium_lamp": (["SSS", "GNG", " W "], {"S": "periodic_elements:steel_ingot", "G": "minecraft:glass",
                                            "N": "periodic_elements:sodium", "W": "periodic_elements:copper_wire"}, 2),
    "led_lamp": (["AAA", "GEG", " W "], {"A": "periodic_elements:aluminium", "G": "minecraft:glass",
                                         "E": "periodic_elements:gallium_arsenide_wafer", "W": "periodic_elements:copper_wire"}, 4),
}


def generate_exploration(assets, data_root, lang, mod_id):
    tex_item = assets / "textures" / "item"
    tex_block = assets / "textures" / "block"
    models = assets / "models"
    for iid in ITEMS:
        if iid == "explorer_compass":
            continue
        item_texture(iid).save(tex_item / f"{iid}.png")
        model = {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{iid}"}}
        if iid in ("rope_net", "steel_cage"):
            item_texture(iid + "_full").save(tex_item / f"{iid}_full.png")
            _dump(models / "item" / f"{iid}_full.json",
                  {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{iid}_full"}})
            model["overrides"] = [{"predicate": {f"{mod_id}:full": 1}, "model": f"{mod_id}:item/{iid}_full"}]
        _dump(models / "item" / f"{iid}.json", model)
    # The compass: 32 needle frames, chosen by the vanilla "angle" predicate (0 = the target straight ahead).
    overrides = []
    for j in range(32):
        compass_frame(j / 32).save(tex_item / f"explorer_compass_{j:02d}.png")
        _dump(models / "item" / f"explorer_compass_{j:02d}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/explorer_compass_{j:02d}"}})
        overrides.append({"predicate": {"angle": 0.0 if j == 0 else (j - 0.5) / 32},
                          "model": f"{mod_id}:item/explorer_compass_{j:02d}"})
    overrides.append({"predicate": {"angle": 31.5 / 32}, "model": f"{mod_id}:item/explorer_compass_00"})
    _dump(models / "item" / "explorer_compass.json", {"parent": "minecraft:item/generated",
                                                     "textures": {"layer0": f"{mod_id}:item/explorer_compass_00"},
                                                     "overrides": overrides})

    for name, img in block_textures().items():
        img.save(tex_block / f"{name}.png")
    states = assets / "blockstates"
    for kind in ("sodium", "led"):
        for lit in (False, True):
            _dump(models / "block" / f"{kind}_lamp{'_lit' if lit else ''}.json", _lamp_model(mod_id, kind, lit))
        _dump(states / f"{kind}_lamp.json", {"variants": {
            "lit=false": {"model": f"{mod_id}:block/{kind}_lamp"}, "lit=true": {"model": f"{mod_id}:block/{kind}_lamp_lit"}}})
        _dump(models / "item" / f"{kind}_lamp.json", {"parent": f"{mod_id}:block/{kind}_lamp_lit"})
    _dump(models / "block" / "tent.json", _tent_model(mod_id))
    _dump(states / "tent.json", {"variants": {f"facing={f}": ({"model": f"{mod_id}:block/tent", "y": y} if y else
                                                              {"model": f"{mod_id}:block/tent"})
                                              for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    _dump(models / "item" / "tent.json", {"parent": f"{mod_id}:block/tent"})
    # 3.48.1 (Fiat Lux P1): a transparent 2x2 px quad, and kind=cap (warm cap lamp) / kind=led (cold white LED helmet).
    from industry import light_marker_model
    _dump(models / "block" / "explorer_light.json", light_marker_model(mod_id))
    _dump(states / "explorer_light.json", {"variants": {f"kind={k}": {"model": f"{mod_id}:block/explorer_light"}
                                                        for k in ("cap", "led")}})

    data_dir = data_root / mod_id
    for b in BLOCKS:
        _dump(data_dir / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod_id}:{b}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for rid, (pattern, key, count) in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", _shaped(pattern, key, rid, count))
    _dump(data_dir / "tags" / "entity_type" / "trap_small.json", {"values": [f"minecraft:{a}" for a in SMALL_ANIMALS]})
    lang.update(LANG)
