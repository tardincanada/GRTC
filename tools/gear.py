"""Tools, armor and weapons made from process-gated materials. Called by generate.py.

Each material needs an alloy that only one historical process makes (bronze from the Crucible, blister steel from
the Cementation Furnace), so finding the ore is not enough. Stats use vanilla's scale: iron tools are 250
durability, speed 6, +2 damage; an iron chestplate is 6 armor.

Writes:
  periodic_elements/gear.json      runtime table (materials, stats, item ids)
  assets: item models and 16x16 textures, armor layer textures
  data: crafting recipes, item tags for enchanting and repair
"""
import json
import random

from PIL import Image

# id: name, ingot item, tools (durability, speed, damage bonus, enchantability, tag of blocks it can't mine),
#     armor (helmet, chest, legs, boots, toughness, knockback resistance, durability multiplier, enchantability),
#     colours (main, light, dark)
MATERIALS = {
    "bronze": ("Bronze", "periodic_elements:bronze_ingot",
               (200, 5.5, 1.5, 12, "minecraft:incorrect_for_stone_tool"),
               (2, 5, 4, 1, 0.0, 0.0, 12, 12),
               ((196, 132, 64), (232, 178, 110), (120, 74, 34))),
    "wrought_iron": ("Wrought Iron", "minecraft:iron_ingot",
                     (300, 6.0, 2.0, 14, "minecraft:incorrect_for_iron_tool"),
                     (2, 6, 5, 2, 0.0, 0.0, 17, 9),
                     ((150, 148, 146), (206, 204, 200), (84, 82, 80))),
    "crucible_steel": ("Crucible Steel", "periodic_elements:crucible_steel_ingot",
                       (900, 7.0, 3.0, 14, "minecraft:incorrect_for_diamond_tool"),
                       (3, 7, 5, 2, 1.0, 0.0, 28, 12),
                       ((150, 166, 190), (210, 222, 240), (78, 88, 110))),
    "steel": ("Steel", "periodic_elements:steel_ingot",
              (600, 6.5, 2.5, 14, "minecraft:incorrect_for_iron_tool"),
              (2, 6, 5, 2, 1.0, 0.0, 22, 12),
              ((160, 166, 178), (214, 220, 230), (88, 92, 104))),
    "manganese_steel": ("Manganese Steel", "periodic_elements:manganese_steel_ingot",
                        (1000, 6.5, 3.0, 12, "minecraft:incorrect_for_diamond_tool"),
                        (3, 7, 6, 2, 2.0, 0.05, 30, 10),
                        ((128, 120, 136), (190, 184, 200), (70, 64, 78))),
    "tungsten_steel": ("Tungsten Steel", "periodic_elements:tungsten_steel_ingot",
                       (1300, 8.5, 3.0, 10, "minecraft:incorrect_for_diamond_tool"),
                       None,
                       ((104, 112, 124), (170, 180, 196), (56, 60, 70))),
    "aluminium": ("Aluminium", "periodic_elements:aluminium",
                  (400, 7.0, 2.0, 16, "minecraft:incorrect_for_iron_tool"),
                  (2, 6, 5, 2, 0.0, 0.0, 15, 18),
                  ((206, 210, 216), (240, 242, 246), (140, 146, 156))),
    # Level 5: each beats netherite in one stat and pays for it in another.
    "stainless_steel": ("Stainless Steel", "periodic_elements:stainless_steel_ingot",
                        (1800, 8.0, 3.0, 12, "minecraft:incorrect_for_diamond_tool"),
                        (3, 8, 6, 3, 2.0, 0.0, 33, 12),
                        ((200, 206, 214), (240, 244, 248), (130, 136, 146))),
    "high_speed_steel": ("High-Speed Steel", "periodic_elements:high_speed_steel_ingot",
                         (1600, 10.0, 3.0, 10, "minecraft:incorrect_for_netherite_tool"),
                         None,
                         ((120, 116, 130), (186, 182, 198), (66, 62, 74))),
    "cemented_carbide": ("Cemented Carbide", "periodic_elements:cemented_carbide",
                         (1200, 12.0, 2.0, 8, "minecraft:incorrect_for_netherite_tool"),
                         None,
                         ((80, 84, 92), (150, 156, 168), (44, 46, 52))),
    "beryllium_copper": ("Beryllium Copper", "periodic_elements:beryllium_copper_ingot",
                         (450, 6.5, 2.0, 18, "minecraft:incorrect_for_iron_tool"),
                         None,
                         ((214, 120, 86), (246, 170, 130), (140, 70, 46))),
    # Level 6.
    "titanium_alloy": ("Titanium Alloy", "periodic_elements:titanium_alloy_ingot",
                       (2100, 8.5, 3.5, 16, "minecraft:incorrect_for_netherite_tool"),
                       (3, 8, 6, 3, 3.0, 0.0, 37, 16),
                       ((170, 176, 190), (220, 226, 238), (100, 106, 120))),
    "synthetic_diamond": ("Diamond-Tipped", "periodic_elements:synthetic_diamond",
                          (2500, 13.0, 4.0, 10, "minecraft:incorrect_for_netherite_tool"),
                          None,
                          ((150, 220, 230), (220, 250, 255), (70, 130, 150))),
    # Level 7: armour only; slightly better than netherite overall.
    "composite": ("Composite", "periodic_elements:composite_plate",
                  None,
                  (3, 9, 7, 3, 4.0, 0.15, 40, 12),
                  ((232, 200, 64), (250, 230, 130), (48, 52, 58))),
}
# Extra movement speed per armor piece (aluminium armor is light: +5% for the full set).
ARMOR_SPEED = {"aluminium": 0.0125, "titanium_alloy": 0.0125}

TOOLS = {  # kind: (name, recipe pattern, base attack damage, attack speed)
    "sword": ("Sword", ["X", "X", "#"], 3.0, -2.4),
    "pickaxe": ("Pickaxe", ["XXX", " # ", " # "], 1.0, -2.8),
    "axe": ("Axe", ["XX", "X#", " #"], 6.0, -3.1),
    "shovel": ("Shovel", ["X", "#", "#"], 1.5, -3.0),
    "hoe": ("Hoe", ["XX", " #", " #"], -1.5, -1.0),
}
ARMOR = {
    "helmet": ("Helmet", ["XXX", "X X"]),
    "chestplate": ("Chestplate", ["X X", "XXX", "XXX"]),
    "leggings": ("Leggings", ["XXX", "X X", "X X"]),
    "boots": ("Boots", ["X X", "X X"]),
}
ENCHANT_TAGS = {
    "sword": ["swords", "sharp_weapon", "weapon", "fire_aspect", "durability"],
    "pickaxe": ["mining", "mining_loot", "durability"],
    "axe": ["axes", "mining", "mining_loot", "sharp_weapon", "weapon", "durability"],
    "shovel": ["mining", "mining_loot", "durability"],
    "hoe": ["mining", "mining_loot", "durability"],
    "helmet": ["head_armor", "armor", "equippable", "durability"],
    "chestplate": ["chest_armor", "armor", "equippable", "durability"],
    "leggings": ["leg_armor", "armor", "equippable", "durability"],
    "boots": ["foot_armor", "armor", "equippable", "durability"],
}
# 2.1.0 area tools (mining tools plan): 3x3, slowed to x0.3 by player.block_break_speed, for every material from
# level 2 steel on. kind: (name, pattern, base attack damage, attack speed).
AREA_TOOLS = {
    "sledgehammer": ("Sledgehammer", ["XXX", "X#X", " # "], 5.0, -3.4),
    "excavator": ("Excavator", [" X ", "X#X", " # "], 2.0, -3.2),
}
NO_AREA_TOOLS = {"bronze", "wrought_iron"}
ENCHANT_TAGS_AREA = ["mining", "mining_loot", "durability"]
VANILLA_TAGS = {"sword": "swords", "pickaxe": "pickaxes", "axe": "axes", "shovel": "shovels", "hoe": "hoes"}

# Iron from the bloomery is wrought iron, so its gear replaces vanilla's iron tools and armor.
REPLACED_VANILLA = ["iron_sword", "iron_pickaxe", "iron_axe", "iron_shovel", "iron_hoe",
                    "iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots"]

GUNS = {
    "hand_cannon": {
        "name": "Hand Cannon", "pattern": ["CCF", "  S"],
        "key": {"C": "periodic_elements:cast_iron_ingot", "F": "minecraft:flint", "S": "minecraft:stick"},
        "durability": 150, "cooldown": 60, "velocity": 4.5, "damage": 2.2, "project": "gunpowder",
        "powder": "periodic_elements:black_powder", "shot": "periodic_elements:lead",
        "tip": "Loads 1 black powder and 1 lead as shot; needs Black Powder research",
        "colors": ((70, 70, 76), (120, 120, 128), (40, 40, 44))},
    "flintlock_musket": {
        "name": "Flintlock Musket", "pattern": ["IIF", " PP"],
        "key": {"I": "periodic_elements:crucible_steel_ingot", "F": "minecraft:flint", "P": "#minecraft:planks"},
        "durability": 300, "cooldown": 40, "velocity": 6.0, "damage": 2.4, "project": "flintlock",
        "powder": "periodic_elements:black_powder", "shot": "periodic_elements:lead",
        "tip": "Loads 1 black powder and 1 lead ball; needs Flintlock Musket research",
        "colors": ((120, 84, 48), (150, 166, 190), (70, 50, 30))},
    "breech_loading_rifle": {
        "name": "Breech-Loading Rifle", "pattern": ["MMB", " PP"],
        "key": {"M": "periodic_elements:manganese_steel_ingot", "B": "periodic_elements:brass_ingot", "P": "#minecraft:planks"},
        "durability": 600, "cooldown": 20, "velocity": 8.0, "damage": 3.0, "project": "breech_loading",
        "powder": "periodic_elements:smokeless_powder", "shot": "periodic_elements:lead",
        "tip": "Loads 1 smokeless powder and 1 lead bullet; needs Breech-Loading Rifle research",
        "colors": ((110, 76, 44), (128, 120, 136), (64, 44, 26))},
}

# 16x16 masks: X = material, L = highlight, D = shade, S = stick, s = stick shade.
MASKS = {
    "sword": ["..............LX", ".............LXD", "............LXD.", "...........LXD..", "..........LXD...",
              ".........LXD....", "........LXD.....", ".......LXD......", "..D...LXD.......", "..DD.LXD........",
              "...DDXD.........", "....sD..........", "...sSDD.........", "..sS...D........", ".sS.............",
              "DD.............."],
    "pickaxe": ["......XXXXX.....", "....XXLLLLLXX...", "...XLD....DXXX..", "..XD.......SDXX.", "..X.......sS.DX.",
                "..........sS..X.", ".........sS...X.", "........sS......", ".......sS.......", "......sS........",
                ".....sS.........", "....sS..........", "...sS...........", "..sS............", ".sS.............",
                "................"],
    "axe": ["........XX......", ".......XLLX.....", "......XLLLXD....", ".....XLLLXSD....", ".....XLLXsSXD...",
            "......XXsS.XD...", ".......sS..XD...", "......sS........", ".....sS.........", "....sS..........",
            "...sS...........", "..sS............", ".sS.............", "................", "................",
            "................"],
    "shovel": ["...........XXX..", "..........XLLXD.", ".........XLLLXD.", ".........XLLXD..", "........sXXDD...",
               ".......sS.D.....", "......sS........", ".....sS.........", "....sS..........", "...sS...........",
               "..sS............", ".sS.............", "................", "................", "................",
               "................"],
    "hoe": ["........XXXX....", ".......XLLLXD...", "..........sSD...", ".........sS.....", "........sS......",
            ".......sS.......", "......sS........", ".....sS.........", "....sS..........", "...sS...........",
            "..sS............", ".sS.............", "................", "................", "................",
            "................"],
    "helmet": ["................", "................", "...XXXXXXXXXX...", "..XLLLLLLLLLLX..", "..XLXXXXXXXXLX..",
               "..XLX......XLX..", "..XDX......XDX..", "..XDX......XDX..", "..DDD......DDD..", "................",
               "................", "................", "................", "................", "................",
               "................"],
    "chestplate": ["................", "..XXX......XXX..", ".XLLXX....XXLLX.", ".XLLLXXXXXXLLLX.", ".XXLLLLLLLLLLXX.",
                   "..XXLLLLLLLLXX..", "...XLLLLLLLLX...", "...XLLLLLLLLX...", "...XLLLLLLLLX...", "...XDLLLLLLDX...",
                   "...XDDLLLLDDX...", "...XDDDDDDDDX...", "....XXXXXXXX....", "................", "................",
                   "................"],
    "leggings": ["................", "...XXXXXXXXXX...", "...XLLLLLLLLX...", "...XLLXXXXLLX...", "...XLLX..XLLX...",
                 "...XLLX..XLLX...", "...XLLX..XLLX...", "...XLDX..XDLX...", "...XLDX..XDLX...", "...XDDX..XDDX...",
                 "...XDDX..XDDX...", "...XXXX..XXXX...", "................", "................", "................",
                 "................"],
    "boots": ["................", "................", "................", "................", "................",
              "...XXX....XXX...", "...XLX....XLX...", "...XLX....XLX...", "...XLX....XLX...", "..XXLX....XLXX..",
              ".XLLDX....XDLLX.", ".XDDDX....XDDDX.", ".XXXXX....XXXXX.", "................", "................",
              "................"],
    "sledgehammer": ["....XXXXXX......", "...XLLLLLLX.....", "...XLLLLLLXD....", "...XXXXXXXXD....", "....DDDsSDDD....",
                     ".......sS.......", "......sS........", ".....sS.........", "....sS..........", "...sS...........",
                     "..sS............", ".sS.............", "sS..............", "................", "................",
                     "................"],
    "excavator": ["..........XXXXX.", ".........XLLLLXD", "........XLLLLLXD", "........XLLLLXD.", ".......XLLLLXD..",
                  "......sXXXXDD...", ".....sS.DDD.....", "....sS..........", "...sS...........", "..sS............",
                  ".sS.............", "sS..............", "................", "................", "................",
                  "................"],
    "hand_cannon": ["................", "................", "................", "...........DD...", "..XXXXXXXXXLLD..",
                    ".XLLLLLLLLLLLXD.", ".XXXXXXXXXXXXXD.", "..DDDDDDDDsSDD..", ".........sS.....", "........sS......",
                    ".......sS.......", "......sS........", ".....sS.........", "....sS..........", "...sS...........",
                    "................"],
    "flintlock_musket": ["................", "................", "..............D.", "XXXXXXXXXXXXXLD.",
                         "LLLLLLLLLLLLLXD.", "XXXXXXXXXXXXSSs.", "..........SSSSs.", ".........SSSSs..",
                         "........SSSs....", "................", "................", "................",
                         "................", "................", "................", "................"],
    "breech_loading_rifle": ["................", "................", "...............D", "XXXXXXXXXXXXXXLD",
                             "LLLLLLLLLLLLLLXD", "XXXXXXXXXXXSSSs.", "...........SSs..", ".........SSSSs..",
                             "........SSSs....", "................", "................", "................",
                             "................", "................", "................", "................"],
}
STICK, STICK_DARK = (137, 103, 39), (88, 63, 22)


def _mask_texture(mask, colors):
    assert len(mask) == 16 and all(len(r) == 16 for r in mask), "masks are 16x16"
    main, light, dark = colors
    pal = {"X": main, "L": light, "D": dark, "S": STICK, "s": STICK_DARK}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(mask):
        for x, ch in enumerate(row):
            if ch in pal:
                px[x, y] = pal[ch] + (255,)
    return img


def _armor_layer(colors, layer, seed):
    """64x32 armor texture in vanilla's layout. Layer 1: helmet, chestplate, boots; layer 2: leggings."""
    main, light, dark = colors
    rnd = random.Random(seed)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    px = img.load()

    def fill(x0, y0, x1, y1, base=main):
        for y in range(y0, y1):
            for x in range(x0, x1):
                v = rnd.randint(-10, 10)
                edge = x in (x0, x1 - 1) or y in (y0, y1 - 1)
                c = dark if edge else base
                px[x, y] = tuple(max(0, min(255, k + v)) for k in c) + (255,)

    if layer == 1:
        fill(0, 0, 32, 16)                    # helmet, every face
        for y in range(12, 16):               # open face below the brow
            for x in range(9, 15):
                px[x, y] = (0, 0, 0, 0)
        fill(16, 16, 40, 32, light)           # chestplate body
        fill(40, 16, 56, 32)                  # shoulders and arms
        fill(0, 27, 16, 32, dark)             # boots: the bottom of each leg
    else:
        fill(0, 20, 16, 32)                   # legs
        fill(16, 26, 40, 32, dark)            # belt around the waist
    return img


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def generate_gear(assets, data_dir, runtime_dir, lang, mod_id, research):
    tex_item = assets / "textures" / "item"
    tex_armor = assets / "textures" / "models" / "armor"
    tex_armor.mkdir(parents=True, exist_ok=True)
    item_tags = {}
    materials = []
    for i, (mid, (name, ingot, tool, armor, colors)) in enumerate(MATERIALS.items()):
        dur, speed, dmg, ench, incorrect = tool or (1, 1.0, 0.0, 1, "minecraft:incorrect_for_netherite_tool")
        items = []
        for kind, (kname, pattern, base, aspeed) in (TOOLS.items() if tool else ()):
            iid = f"{mid}_{kind}"
            items.append({"id": iid, "kind": kind, "base": base, "speed": aspeed})
            lang[f"item.{mod_id}.{iid}"] = f"{name} {kname}"
            _mask_texture(MASKS[kind], colors).save(tex_item / f"{iid}.png")
            _dump(assets / "models" / "item" / f"{iid}.json",
                  {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{mod_id}:item/{iid}"}})
            _dump(data_dir / "recipe" / f"{iid}.json", {
                "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
                "key": {"X": {"item": ingot}, "#": {"item": "minecraft:stick"}},
                "result": {"id": f"{mod_id}:{iid}", "count": 1}})
            for t in ENCHANT_TAGS[kind]:
                item_tags.setdefault(f"enchantable/{t}", []).append(f"{mod_id}:{iid}")
            item_tags.setdefault(VANILLA_TAGS[kind], []).append(f"{mod_id}:{iid}")
        for kind, (kname, pattern, base, aspeed) in (AREA_TOOLS.items() if tool and mid not in NO_AREA_TOOLS else ()):
            iid = f"{mid}_{kind}"
            items.append({"id": iid, "kind": kind, "base": base, "speed": aspeed})
            lang[f"item.{mod_id}.{iid}"] = f"{name} {kname}"
            _mask_texture(MASKS[kind], colors).save(tex_item / f"{iid}.png")
            _dump(assets / "models" / "item" / f"{iid}.json",
                  {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{mod_id}:item/{iid}"}})
            _dump(data_dir / "recipe" / f"{iid}.json", {
                "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
                "key": {"X": {"item": ingot}, "#": {"item": "minecraft:stick"}},
                "result": {"id": f"{mod_id}:{iid}", "count": 1}})
            for tg in ENCHANT_TAGS_AREA:
                item_tags.setdefault(f"enchantable/{tg}", []).append(f"{mod_id}:{iid}")
        for kind, (kname, pattern) in (ARMOR.items() if armor else ()):
            iid = f"{mid}_{kind}"
            items.append({"id": iid, "kind": kind})
            lang[f"item.{mod_id}.{iid}"] = f"{name} {kname}"
            _mask_texture(MASKS[kind], colors).save(tex_item / f"{iid}.png")
            _dump(assets / "models" / "item" / f"{iid}.json",
                  {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{iid}"}})
            _dump(data_dir / "recipe" / f"{iid}.json", {
                "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
                "key": {"X": {"item": ingot}}, "result": {"id": f"{mod_id}:{iid}", "count": 1}})
            for t in ENCHANT_TAGS[kind]:
                item_tags.setdefault(f"enchantable/{t}", []).append(f"{mod_id}:{iid}")
            item_tags.setdefault("trimmable_armor", []).append(f"{mod_id}:{iid}")
        m = {"id": mid, "ingot": ingot, "durability": dur, "speed": speed, "damage": dmg, "enchantability": ench,
             "incorrect": incorrect, "items": items}
        if armor:
            helmet, chest, legs, boots, tough, kb, mult, aench = armor
            for layer in (1, 2):
                _armor_layer(colors, layer, 50 + i * 2 + layer).save(tex_armor / f"{mid}_layer_{layer}.png")
            m.update({"armor": [boots, legs, chest, helmet], "toughness": tough, "knockback": kb,
                      "durabilityMultiplier": mult, "armorEnchantability": aench,
                      "armorSpeed": ARMOR_SPEED.get(mid, 0.0)})
        materials.append(m)

    guns = []
    for gid, spec in GUNS.items():
        g = dict(spec)
        assert g["project"] in research, g["project"]
        lang[f"item.{mod_id}.{gid}"] = g.pop("name")
        lang[f"item.{mod_id}.{gid}.tip"] = g.pop("tip")
        _mask_texture(MASKS[gid], g.pop("colors")).save(tex_item / f"{gid}.png")
        _dump(assets / "models" / "item" / f"{gid}.json",
              {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{mod_id}:item/{gid}"}})
        _dump(data_dir / "recipe" / f"{gid}.json", {
            "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": g.pop("pattern"),
            "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in g.pop("key").items()},
            "result": {"id": f"{mod_id}:{gid}", "count": 1}})
        item_tags.setdefault("enchantable/durability", []).append(f"{mod_id}:{gid}")
        guns.append(dict(g, id=gid))
    lang[f"message.{mod_id}.hand_cannon.empty"] = "Needs 1 black powder and 1 lead in your inventory"
    lang[f"message.{mod_id}.gun.empty"] = "Needs 1 %s and 1 %s in your inventory"

    disabled = [{"condition": "fabric:not", "value": {"condition": "fabric:true"}}]
    for rid in REPLACED_VANILLA:
        _dump(data_dir.parent / "minecraft" / "recipe" / f"{rid}.json", {"fabric:load_conditions": disabled})

    mc_tags = data_dir.parent / "minecraft" / "tags" / "item"
    for tag, values in item_tags.items():
        _dump(mc_tags / f"{tag}.json", {"replace": False, "values": values})
    _dump(runtime_dir / "gear.json", {"materials": materials, "guns": guns})
    return materials
