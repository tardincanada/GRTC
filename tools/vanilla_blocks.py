"""Compositions for the vanilla blocks no recipe reaches (3.35.0).

compositions.csv covers rocks, ores, soils, logs and a few items; crafted.py derives every block a recipe makes. What
is left is listed here, in three kinds:

ROWS     natural things and the raw items many recipes need (flowers, corals, fungi, oxidised copper, prismarine
         shards, string, honeycomb, dyes ...). Once these exist crafted.py also reaches candles, banners, beds,
         stained glass, concrete powder, bamboo wood, crimson and warped wood, purpur, sea lanterns and more.
SAME     blocks that are another block or item in a different state, with optional extras: a wall sign is its sign, a
         potted poppy is a flower pot and a poppy, an infested stone is stone. crafted.py treats them as recipes.
NONE     blocks with no sensible composition: air, fire, portals, command and structure blocks, unbreakable blocks.
         The test CraftedCompositionTests.everyVanillaBlockIsCovered checks that every vanilla block has a
         composition or is in this list.

Fictional materials get a real stand-in and say so in the note: prismarine is read as aquamarine (beryl), netherite
as a tungsten alloy, ender pearls as a magnetite-bearing silica, and so on.
"""
import json

COLOURS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan",
           "purple", "blue", "brown", "green", "red", "black"]
WOODS = ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped"]

PLANT = "water:75;cellulose:15;lignin:3;protein:3;glucose:3;chlorophyll:1"
FLOWER = "water:76;cellulose:14;lignin:2;protein:3;glucose:4;chlorophyll:1"
CROP = "water:70;cellulose:20;protein:4;lignin:4;glucose:2"
FUNGUS = "water:85;protein:5;cellulose:7;glucose:3"
FUNGAL_WOOD = "cellulose:50;protein:15;lignin:10;water:25"
WART = "water:70;protein:20;cellulose:10"
LIVE_CORAL = "calcium_carbonate:88;water:7;protein:5"
DEAD_CORAL = "calcium_carbonate:98;water:2"
SCULK = "protein:40;water:45;calcium_carbonate:7;tricalcium_phosphate:8"
FLESH_HEAD = "water:60;protein:30;tallow:10"
SKULL = "tricalcium_phosphate:70;protein:20;water:10"
GLOW = "protein:40;water:40;zinc_sulfide:20"

# (id, amount, composition, note)
ROWS = [
    # Raw items many recipes need.
    ("minecraft:string", 13.89, "protein:100", "Spider silk (fibroin); one nylon makes 8 string, so a string is 1/8 of an item"),
    ("minecraft:cobweb", 111.1, "protein:100", "Spider silk"),
    ("minecraft:flint", 111.1, "silicon_dioxide:98;calcium_carbonate:1;water:1", "Microcrystalline quartz"),
    ("minecraft:honeycomb", 111.1, "vegetable_wax:90;glucose:5;water:5", "Beeswax (counted as vegetable wax)"),
    ("minecraft:gunpowder", 111.1, "potassium_nitrate:75;carbon:15;sulfur:10", "Black powder"),
    ("minecraft:slime_ball", 111.1, "water:85;protein:10;glycerol:5", "A protein gel"),
    ("minecraft:ink_sac", 111.1, "water:50;protein:30;carbon:20", "Squid ink: melanin, counted as carbon"),
    ("minecraft:glow_ink_sac", 111.1, "water:50;protein:30;carbon:10;zinc_sulfide:10", "Ink with a glowing pigment"),
    ("minecraft:prismarine_shard", 111.1, "beryl:70;silicon_dioxide:30", "Prismarine read as aquamarine (beryl)"),
    ("minecraft:prismarine_crystals", 111.1, "beryl:60;silicon_dioxide:30;zinc_sulfide:10", "Aquamarine with a glowing sulfide"),
    ("minecraft:chorus_fruit", 111.1, "water:80;glucose:10;cellulose:10", ""),
    ("minecraft:popped_chorus_fruit", 111.1, "cellulose:60;glucose:30;water:10", "Dried by heat; smelting it loses water"),
    ("minecraft:blaze_rod", 111.1, "sulfur:60;iron_oxide:20;carbon:20", "Read as a sulfur-iron rod"),
    ("minecraft:ender_pearl", 111.1, "magnetite:60;monazite:25;silicon_dioxide:15",
     "Read as a natural magnet: magnetite with monazite, a rare-earth source for neodymium magnets (3.46.0)"),
    ("minecraft:nautilus_shell", 111.1, "calcium_carbonate:95;protein:5", "Aragonite shell"),
    ("minecraft:heart_of_the_sea", 111.1, "calcium_carbonate:90;protein:10", "Read as a pearl"),
    ("minecraft:shulker_shell", 111.1, "calcium_carbonate:60;protein:40", "Read as a shell"),
    ("minecraft:nether_star", 111.1, "iridium:40;osmium:35;platinum:20;ruthenium:5",
     "Read as an osmiridium crystal: the platinum metals, freed like anode slime (3.46.0)"),
    ("minecraft:ghast_tear", 111.1, "titanium_dioxide:70;ruthenium:30",
     "Read as the ruthenium-titanium oxide coating of a DSA anode (Beer 1965); in an upgrade slot it saves power (3.46.0)"),
    ("minecraft:netherite_scrap", 111.1, "tungsten:80;iron:20", "Netherite read as a tungsten alloy"),
    ("minecraft:nether_wart", 111.1, WART, "A fungus"),
    ("minecraft:bamboo", 111.1, "cellulose:50;lignin:25;water:20;silicon_dioxide:5", "Nine make a block of bamboo"),
    ("minecraft:white_dye", 3.47, "titanium_dioxide:100", "Titanium white"),
    ("minecraft:black_dye", 3.47, "carbon:100", "Carbon black"),
    ("minecraft:green_dye", 3.47, "chlorophyll:100", ""),
    ("minecraft:lime_dye", 3.47, "chlorophyll:50;titanium_dioxide:50", ""),
    ("minecraft:pink_dye", 3.47, "alizarin:50;titanium_dioxide:50", ""),
    ("minecraft:cyan_dye", 3.47, "prussian_blue:50;chlorophyll:50", ""),
    ("minecraft:light_blue_dye", 3.47, "prussian_blue:50;titanium_dioxide:50", ""),
    ("minecraft:gray_dye", 3.47, "carbon:50;titanium_dioxide:50", ""),
    ("minecraft:light_gray_dye", 3.47, "carbon:25;titanium_dioxide:75", ""),
    ("minecraft:purple_dye", 3.47, "fuchsine:50;prussian_blue:50", ""),
    # Plants.
    ("minecraft:short_grass", 111.1, PLANT, ""),
    ("minecraft:tall_grass", 222.2, PLANT, "Two blocks tall"),
    ("minecraft:fern", 111.1, PLANT, ""),
    ("minecraft:large_fern", 222.2, PLANT, "Two blocks tall"),
    ("minecraft:dead_bush", 111.1, "cellulose:60;lignin:30;water:10", "Dry stems"),
    ("minecraft:seagrass", 111.1, "water:78;cellulose:14;sodium_chloride:4;protein:3;chlorophyll:1", ""),
    ("minecraft:vine", 111.1, PLANT, ""),
    ("minecraft:glow_lichen", 111.1, "water:70;cellulose:20;protein:5;zinc_sulfide:5", "Lichen with a glowing pigment"),
    ("minecraft:hanging_roots", 111.1, "cellulose:50;lignin:20;water:30", ""),
    ("minecraft:moss_block", 1000, "water:70;cellulose:24;protein:3;lignin:2;chlorophyll:1", ""),
    ("minecraft:lily_pad", 111.1, PLANT, ""),
    ("minecraft:azalea", 444.4, "water:55;cellulose:25;lignin:15;chlorophyll:1;protein:4", "A shrub"),
    ("minecraft:flowering_azalea", 444.4, "water:55;cellulose:25;lignin:14;chlorophyll:1;protein:4;glucose:1", "A shrub"),
    ("minecraft:big_dripleaf", 111.1, PLANT, ""),
    ("minecraft:small_dripleaf", 111.1, PLANT, ""),
    ("minecraft:spore_blossom", 111.1, FLOWER, ""),
    ("minecraft:cave_vines", 111.1, PLANT, ""),
    ("minecraft:chorus_plant", 111.1, PLANT, ""),
    ("minecraft:chorus_flower", 111.1, FLOWER, ""),
    ("minecraft:sea_pickle", 111.1, "water:80;protein:15;calcium_carbonate:5", "A colony of tunicates"),
    ("minecraft:pink_petals", 111.1, FLOWER, ""),
    ("minecraft:lilac", 222.2, FLOWER, "Two blocks tall"),
    ("minecraft:rose_bush", 222.2, FLOWER, "Two blocks tall"),
    ("minecraft:peony", 222.2, FLOWER, "Two blocks tall"),
    ("minecraft:pitcher_plant", 222.2, FLOWER, "Two blocks tall"),
    ("minecraft:mangrove_propagule", 111.1, PLANT, ""),
    ("minecraft:mangrove_roots", 1000, "cellulose:58;lignin:28;water:12;sodium_chloride:1;calcium_oxide:1", "Mangrove wood"),
    ("minecraft:melon_stem", 111.1, PLANT, ""),
    ("minecraft:pumpkin_stem", 111.1, PLANT, ""),
    ("minecraft:carrots", 100, CROP, "The plant, not the carrots"),
    ("minecraft:potatoes", 100, CROP, "The plant, not the potatoes"),
    ("minecraft:beetroots", 100, CROP, "The plant, not the beetroots"),
    ("minecraft:torchflower_crop", 100, CROP, ""),
    ("minecraft:pitcher_crop", 100, CROP, ""),
    ("minecraft:sweet_berry_bush", 100, CROP, ""),
    ("minecraft:cocoa", 111.1, "item:minecraft:cocoa_beans:70;cellulose:20;water:10", "A cocoa pod"),
    ("minecraft:bee_nest", 1000, "cellulose:45;lignin:20;vegetable_wax:25;glucose:10", "Wood with comb inside"),
    ("minecraft:sponge", 1000, "protein:60;silicon_dioxide:30;water:10", "Spongin and silica spicules"),
    ("minecraft:wet_sponge", 1000, "block:minecraft:sponge:60;water:40", "Drying it in a furnace loses the water"),
    # Fungi and the Nether's fungal woods.
    ("minecraft:brown_mushroom", 111.1, FUNGUS, ""),
    ("minecraft:red_mushroom", 111.1, FUNGUS, ""),
    ("minecraft:brown_mushroom_block", 1000, FUNGUS, ""),
    ("minecraft:red_mushroom_block", 1000, FUNGUS, ""),
    ("minecraft:mushroom_stem", 1000, FUNGUS, ""),
    ("minecraft:crimson_fungus", 111.1, FUNGUS, ""),
    ("minecraft:warped_fungus", 111.1, FUNGUS, ""),
    ("minecraft:crimson_roots", 111.1, FUNGUS, ""),
    ("minecraft:warped_roots", 111.1, FUNGUS, ""),
    ("minecraft:nether_sprouts", 111.1, FUNGUS, ""),
    ("minecraft:weeping_vines", 111.1, FUNGUS, ""),
    ("minecraft:twisting_vines", 111.1, FUNGUS, ""),
    ("minecraft:crimson_stem", 1000, FUNGAL_WOOD, "A giant fungus's stem: chitin and glucans counted as cellulose"),
    ("minecraft:warped_stem", 1000, FUNGAL_WOOD, "A giant fungus's stem: chitin and glucans counted as cellulose"),
    ("minecraft:nether_wart_block", 1000, WART, ""),
    ("minecraft:warped_wart_block", 1000, WART, ""),
    ("minecraft:shroomlight", 1000, "water:55;protein:20;cellulose:15;zinc_sulfide:10", "Fungal flesh with a glowing pigment"),
    ("minecraft:crimson_nylium", 1000, "block:minecraft:netherrack:95;protein:3;cellulose:2", ""),
    ("minecraft:warped_nylium", 1000, "block:minecraft:netherrack:95;protein:3;cellulose:2", ""),
    # Sea.
    *[(f"minecraft:{c}_coral_block", 1000, LIVE_CORAL, "Aragonite with living polyps") for c in ["tube", "brain", "bubble", "fire", "horn"]],
    *[(f"minecraft:{c}_coral", 111.1, LIVE_CORAL, "") for c in ["tube", "brain", "bubble", "fire", "horn"]],
    *[(f"minecraft:{c}_coral_fan", 111.1, LIVE_CORAL, "") for c in ["tube", "brain", "bubble", "fire", "horn"]],
    *[(f"minecraft:dead_{c}_coral_block", 1000, DEAD_CORAL, "Bare aragonite") for c in ["tube", "brain", "bubble", "fire", "horn"]],
    *[(f"minecraft:dead_{c}_coral", 111.1, DEAD_CORAL, "") for c in ["tube", "brain", "bubble", "fire", "horn"]],
    *[(f"minecraft:dead_{c}_coral_fan", 111.1, DEAD_CORAL, "") for c in ["tube", "brain", "bubble", "fire", "horn"]],
    ("minecraft:frogspawn", 111.1, "water:90;protein:10", ""),
    ("minecraft:turtle_egg", 111.1, "water:66;protein:12;tallow:10;calcium_carbonate:12", "Like a hen's egg"),
    ("minecraft:sniffer_egg", 333.3, "water:66;protein:12;tallow:10;calcium_carbonate:12", "A large egg"),
    # Minerals.
    ("minecraft:pointed_dripstone", 250, "calcium_carbonate:98;iron_oxide:1;silicon_dioxide:1", "Four make a dripstone block"),
    ("minecraft:small_amethyst_bud", 111.1, "silicon_dioxide:99.9;iron_oxide:0.1", ""),
    ("minecraft:medium_amethyst_bud", 111.1, "silicon_dioxide:99.9;iron_oxide:0.1", ""),
    ("minecraft:large_amethyst_bud", 111.1, "silicon_dioxide:99.9;iron_oxide:0.1", ""),
    ("minecraft:amethyst_cluster", 444.4, "silicon_dioxide:99.9;iron_oxide:0.1", "Breaks into four shards"),
    ("minecraft:ancient_debris", 1000, "block:minecraft:netherrack:75;wolframite:25", "Netherite read as a tungsten ore"),
    ("minecraft:crying_obsidian", 1000, "block:minecraft:obsidian:95;block:minecraft:glowstone:5", ""),
    ("minecraft:gilded_blackstone", 1000, "block:minecraft:blackstone:95;gold:5", ""),
    ("minecraft:heavy_core", 1000, "tungsten:100", "A dense core read as tungsten"),
    ("minecraft:bell", 1000, "copper:78;tin:22", "Bell bronze"),
    *[(f"minecraft:{s}copper", 1000, f"copper:{cu};copper_sulfate:{cs};water:{w}", f"Copper with a {kind} patina (brochantite counted as copper sulfate)")
      for s, cu, cs, w, kind in [("exposed_", 95, 4, 1, "thin"), ("weathered_", 88, 10, 2, "thicker"), ("oxidized_", 80, 16, 4, "full green")]],
    # The deep dark.
    ("minecraft:sculk", 1000, SCULK, "Read as living tissue"),
    ("minecraft:sculk_vein", 111.1, SCULK, ""),
    ("minecraft:sculk_sensor", 1000, SCULK, ""),
    ("minecraft:sculk_shrieker", 1000, SCULK, ""),
    ("minecraft:sculk_catalyst", 1000, "protein:35;water:40;tricalcium_phosphate:20;calcium_carbonate:5", "Sculk with bone"),
    *[(f"minecraft:{c}_froglight", 1000, GLOW, "Read as glowing tissue") for c in ["ochre", "verdant", "pearlescent"]],
    # Heads.
    ("minecraft:skeleton_skull", 333.3, SKULL, ""),
    ("minecraft:wither_skeleton_skull", 333.3, "tricalcium_phosphate:65;protein:20;carbon:10;water:5", "A charred skull"),
    *[(f"minecraft:{m}_head", 333.3, FLESH_HEAD, "") for m in ["zombie", "creeper", "piglin", "player", "dragon"]],
]

# (block, [(source id, count)], how many): the block is its sources in another state. Sources may be anything with a
# composition, including other SAME entries; crafted.py resolves them like recipes.
SAME = [
    *[(f"minecraft:{w}_wall_sign", [(f"minecraft:{w}_sign", 1)], 1) for w in WOODS],
    *[(f"minecraft:{w}_wall_hanging_sign", [(f"minecraft:{w}_hanging_sign", 1)], 1) for w in WOODS],
    *[(f"minecraft:{c}_wall_banner", [(f"minecraft:{c}_banner", 1)], 1) for c in COLOURS],
    *[(f"minecraft:{c}_candle_cake", [("minecraft:cake", 1), (f"minecraft:{c}_candle", 1)], 1) for c in COLOURS],
    ("minecraft:candle_cake", [("minecraft:cake", 1), ("minecraft:candle", 1)], 1),
    *[(f"minecraft:{c}_concrete", [(f"minecraft:{c}_concrete_powder", 1)], 1) for c in COLOURS],
    *[(f"minecraft:{m}_wall_head", [(f"minecraft:{m}_head", 1)], 1) for m in ["zombie", "creeper", "piglin", "player", "dragon"]],
    ("minecraft:skeleton_wall_skull", [("minecraft:skeleton_skull", 1)], 1),
    ("minecraft:wither_skeleton_wall_skull", [("minecraft:wither_skeleton_skull", 1)], 1),
    ("minecraft:wall_torch", [("minecraft:torch", 1)], 1),
    ("minecraft:soul_wall_torch", [("minecraft:soul_torch", 1)], 1),
    ("minecraft:redstone_wall_torch", [("minecraft:redstone_torch", 1)], 1),
    *[(f"minecraft:{c}_coral_wall_fan", [(f"minecraft:{c}_coral_fan", 1)], 1) for c in ["tube", "brain", "bubble", "fire", "horn"]],
    *[(f"minecraft:dead_{c}_coral_wall_fan", [(f"minecraft:dead_{c}_coral_fan", 1)], 1) for c in ["tube", "brain", "bubble", "fire", "horn"]],
    *[(f"minecraft:stripped_{w}_{p}", [(f"minecraft:{w}_{p}", 1)], 1)
      for w in ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry"] for p in ["log", "wood"]],
    *[(f"minecraft:stripped_{w}_{p}", [(f"minecraft:{w}_{p}", 1)], 1) for w in ["crimson", "warped"] for p in ["stem", "hyphae"]],
    ("minecraft:stripped_bamboo_block", [("minecraft:bamboo_block", 1)], 1),
    ("minecraft:crimson_hyphae", [("minecraft:crimson_stem", 1)], 1),
    ("minecraft:warped_hyphae", [("minecraft:warped_stem", 1)], 1),
    *[(f"minecraft:infested_{b}", [(f"minecraft:{b}", 1)], 1)
      for b in ["stone", "cobblestone", "stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks", "deepslate"]],
    ("minecraft:dirt_path", [("minecraft:dirt", 1)], 1),
    ("minecraft:farmland", [("minecraft:dirt", 1)], 1),
    ("minecraft:suspicious_sand", [("minecraft:sand", 1)], 1),
    ("minecraft:suspicious_gravel", [("minecraft:gravel", 1)], 1),
    ("minecraft:water", [("minecraft:water_bucket", 1)], 1),
    ("minecraft:lava", [("minecraft:lava_bucket", 1)], 1),
    ("minecraft:frosted_ice", [("minecraft:ice", 1)], 1),
    ("minecraft:powder_snow", [("minecraft:snow_block", 1)], 1),
    ("minecraft:water_cauldron", [("minecraft:cauldron", 1), ("minecraft:water_bucket", 1)], 1),
    ("minecraft:lava_cauldron", [("minecraft:cauldron", 1), ("minecraft:lava_bucket", 1)], 1),
    ("minecraft:powder_snow_cauldron", [("minecraft:cauldron", 1), ("minecraft:snow_block", 1)], 1),
    ("minecraft:redstone_wire", [("minecraft:redstone", 1)], 1),
    ("minecraft:tripwire", [("minecraft:string", 1)], 1),
    ("minecraft:kelp_plant", [("minecraft:kelp", 1)], 1),
    ("minecraft:tall_seagrass", [("minecraft:seagrass", 2)], 1),
    ("minecraft:cave_vines_plant", [("minecraft:cave_vines", 1)], 1),
    ("minecraft:weeping_vines_plant", [("minecraft:weeping_vines", 1)], 1),
    ("minecraft:twisting_vines_plant", [("minecraft:twisting_vines", 1)], 1),
    ("minecraft:big_dripleaf_stem", [("minecraft:big_dripleaf", 1)], 1),
    ("minecraft:attached_melon_stem", [("minecraft:melon_stem", 1)], 1),
    ("minecraft:attached_pumpkin_stem", [("minecraft:pumpkin_stem", 1)], 1),
    ("minecraft:bamboo_sapling", [("minecraft:bamboo", 1)], 1),
    ("minecraft:flowering_azalea_leaves", [("minecraft:azalea_leaves", 1)], 1),
    ("minecraft:carved_pumpkin", [("minecraft:pumpkin", 1)], 1),
    ("minecraft:chipped_anvil", [("minecraft:anvil", 1)], 1),
    ("minecraft:damaged_anvil", [("minecraft:anvil", 1)], 1),
    ("minecraft:petrified_oak_slab", [("minecraft:oak_slab", 1)], 1),
    ("minecraft:muddy_mangrove_roots", [("minecraft:mangrove_roots", 1), ("minecraft:mud", 1)], 1),
    # Dyeing a shulker box is a special recipe crafted.py cannot read.
    *[(f"minecraft:{c}_shulker_box", [("minecraft:shulker_box", 1), (f"minecraft:{c}_dye", 1)], 1) for c in COLOURS],
    # Copper doors and trapdoors only weather in the world (no recipe): counted as the new door.
    *[(f"minecraft:{s}_copper_{p}", [(f"minecraft:copper_{p}", 1)], 1)
      for s in ["exposed", "weathered", "oxidized"] for p in ["door", "trapdoor"]],
]

FLOWERS = ["dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip", "white_tulip",
           "pink_tulip", "cornflower", "lily_of_the_valley", "torchflower", "wither_rose"]
SAPLINGS = ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "cherry"]
ROWS += [(f"minecraft:{f}", 111.1, FLOWER, "") for f in FLOWERS]
ROWS += [(f"minecraft:{s}_sapling", 111.1, "water:60;cellulose:22;lignin:12;protein:3;chlorophyll:1;glucose:2", "") for s in SAPLINGS]
# Potted plants: the pot and what is in it.
POTTED = {**{f: f for f in FLOWERS + ["oxeye_daisy", "fern", "dead_bush", "cactus", "bamboo", "brown_mushroom", "red_mushroom",
                                      "crimson_fungus", "warped_fungus", "crimson_roots", "warped_roots", "mangrove_propagule"]},
          **{f"{s}_sapling": f"{s}_sapling" for s in SAPLINGS},
          "azalea_bush": "azalea", "flowering_azalea_bush": "flowering_azalea"}
SAME += [(f"minecraft:potted_{p}", [("minecraft:flower_pot", 1), (f"minecraft:{src}", 1)], 1) for p, src in POTTED.items()]

NONE = {
    "air": "nothing", "cave_air": "nothing", "void_air": "nothing", "structure_void": "nothing",
    "fire": "a flame", "soul_fire": "a flame", "bubble_column": "moving water",
    "nether_portal": "a portal", "end_portal": "a portal", "end_gateway": "a portal",
    "barrier": "creative only", "light": "creative only", "structure_block": "creative only", "jigsaw": "creative only",
    "command_block": "creative only", "chain_command_block": "creative only", "repeating_command_block": "creative only",
    "moving_piston": "a piston in motion", "piston_head": "part of a piston",
    "spawner": "cannot be obtained", "trial_spawner": "cannot be obtained", "vault": "cannot be obtained",
    "bedrock": "unbreakable", "reinforced_deepslate": "unbreakable", "end_portal_frame": "unbreakable",
    "dragon_egg": "a one-of-a-kind trophy",
}


def composition_rows(root=None):
    """(id, amount, composition) for generate.py."""
    return [(i, a, c) for i, a, c, _ in ROWS]


def same_recipes():
    """SAME as recipe-like dicts for crafted.py: (id, {"type", "result", "ingredients"})."""
    out = []
    for block, sources, count in SAME:
        ings = [{"item": s} for s, n in sources for _ in range(n)]
        out.append((f"same:{block}", {"type": "minecraft:crafting_shapeless", "ingredients": ings,
                                      "result": {"id": block, "count": count}}))
    return out


def write_none(runtime_dir):
    (runtime_dir / "no_composition.json").write_text(
        json.dumps({f"minecraft:{k}": v for k, v in sorted(NONE.items())}, indent=1) + "\n", encoding="utf-8")
