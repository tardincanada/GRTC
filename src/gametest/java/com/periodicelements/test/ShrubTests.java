package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.shrubs.Greenhouse;
import com.periodicelements.shrubs.GreenhouseHeaterBlock;
import com.periodicelements.shrubs.GreenhouseHeaterBlockEntity;
import com.periodicelements.shrubs.ShrubBlock;
import com.periodicelements.shrubs.Shrubs;
import com.periodicelements.trees.BiomeSaplingBlock;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.List;

/** 3.16.0 shrubs: harvest, cuttings, biomes, greenhouses (plain and heated) and the Greenhouse Heater. */
public class ShrubTests implements FabricGameTest {
	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static void biome(TestContext ctx, BlockPos abs, int r, String biome) {
		var server = ctx.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent().withLevel(4),
				String.format("fillbiome %d %d %d %d %d %d minecraft:%s", abs.getX() - r, abs.getY() - r, abs.getZ() - r,
						abs.getX() + r, abs.getY() + r, abs.getZ() + r, biome));
		Greenhouse.forget();
	}

	private static int drops(TestContext ctx, BlockPos abs, Item item) {
		List<ItemEntity> list = ctx.getWorld().getEntitiesByClass(ItemEntity.class, new Box(abs).expand(2), e -> e.getStack().isOf(item));
		int n = list.stream().mapToInt(e -> e.getStack().getCount()).sum();
		list.forEach(ItemEntity::discard);
		return n;
	}

	/** A 5x5x5 room at {@code corner}: grass floor, stone walls with an oak door, glass roof; inside is 3x3x3. */
	private static BlockPos room(TestContext ctx, BlockPos corner, Block roof) {
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				for (int z = 0; z < 5; z++) {
					BlockPos p = corner.add(x, y, z);
					boolean edge = x == 0 || x == 4 || z == 0 || z == 4;
					ctx.setBlockState(p, y == 0 ? Blocks.GRASS_BLOCK : y == 4 ? roof : edge ? Blocks.STONE : Blocks.AIR);
				}
			}
		}
		ctx.setBlockState(corner.add(2, 1, 0), Blocks.OAK_DOOR.getDefaultState());
		ctx.setBlockState(corner.add(2, 2, 0), Blocks.OAK_DOOR.getDefaultState().with(net.minecraft.block.DoorBlock.HALF,
				net.minecraft.block.enums.DoubleBlockHalf.UPPER));
		Greenhouse.forget();
		return corner.add(2, 1, 2);
	}

	private static boolean grows(TestContext ctx, BlockPos pos, String shrub) {
		ctx.setBlockState(pos, block(shrub + "_bush"));
		BlockPos abs = ctx.getAbsolutePos(pos);
		ServerWorld w = ctx.getWorld();
		Greenhouse.forget();
		return ((ShrubBlock) block(shrub + "_bush")).isFertilizable(w, abs, w.getBlockState(abs));
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void ripeBushesGiveTheirHarvestAndStay(TestContext ctx) {
		check(ctx, Shrubs.ALL.size() == 62, "62 shrubs in stages 1 to 3, got " + Shrubs.ALL.size());
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(pos, block("annatto_bush").getDefaultState().with(ShrubBlock.AGE, 3));
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		BlockState state = ctx.getWorld().getBlockState(abs);
		ShrubBlock bush = (ShrubBlock) state.getBlock();
		check(ctx, bush.harvest(state, ctx.getWorld(), abs, player), "a ripe annatto should be harvested");
		check(ctx, ctx.getWorld().getBlockState(abs).isOf(bush), "the bush stays planted");
		check(ctx, ctx.getWorld().getBlockState(abs).get(ShrubBlock.AGE) == 1, "the bush goes back to age 1");
		int seeds = drops(ctx, abs, bush.product());
		check(ctx, seeds >= 1 && seeds <= 2, "1 or 2 annatto seeds, got " + seeds);
		check(ctx, !bush.harvest(ctx.getWorld().getBlockState(abs), ctx.getWorld(), abs, player), "nothing to pick at age 1");

		// Shears take a cutting from a grown bush; a bush without a product (sea holly) still gives cuttings.
		ctx.setBlockState(pos, block("holly_mangrove_bush").getDefaultState().with(ShrubBlock.AGE, 2));
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));
		state = ctx.getWorld().getBlockState(abs);
		state.onUseWithItem(player.getMainHandStack(), ctx.getWorld(), player, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
		check(ctx, drops(ctx, abs, mod("holly_mangrove_cutting")) == 1, "shears should cut one holly mangrove cutting");
		check(ctx, ctx.getWorld().getBlockState(abs).get(ShrubBlock.AGE) == 1, "a cut bush goes back to age 1");

		// Sown seed plants the bush.
		check(ctx, bush.product() instanceof net.minecraft.item.BlockItem bi && bi.getBlock() == bush,
				"annatto seeds should plant an annatto bush");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void shrubsGrowOnlyInTheirBiomes(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		biome(ctx, abs, 2, "jungle");
		check(ctx, grows(ctx, pos, "guarana"), "guarana grows in the jungle");
		check(ctx, !grows(ctx, pos, "sagebrush"), "no sagebrush in the jungle");
		biome(ctx, abs, 2, "plains");
		check(ctx, !grows(ctx, pos, "guarana"), "no guarana in the plains");
		ctx.setBlockState(pos.down(), Blocks.SAND);
		check(ctx, block("creosote_bush").getDefaultState().canPlaceAt(ctx.getWorld(), abs), "creosote roots in sand");
		check(ctx, !block("tea_bush").getDefaultState().canPlaceAt(ctx.getWorld(), abs), "tea does not root in sand");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void greenhousesGrowShrubsAndTreesAwayFromHome(TestContext ctx) {
		BlockPos corner = new BlockPos(0, 1, 0);
		BlockPos in = room(ctx, corner, Blocks.GLASS);
		biome(ctx, ctx.getAbsolutePos(in), 6, "snowy_plains");
		check(ctx, Greenhouse.TEMPERATE.equals(Greenhouse.climateAt(ctx.getWorld(), ctx.getAbsolutePos(in))),
				"a closed glass room is a temperate greenhouse, got " + Greenhouse.climateAt(ctx.getWorld(), ctx.getAbsolutePos(in)));
		check(ctx, grows(ctx, in, "tea"), "tea grows in a plain greenhouse in the snow");
		check(ctx, !grows(ctx, in, "guarana"), "guarana needs a warm greenhouse");

		// A tree sapling too: beech (temperate forests) in a greenhouse on the snowy plains.
		ctx.setBlockState(in, block("beech_sapling"));
		BlockPos abs = ctx.getAbsolutePos(in);
		check(ctx, ((BiomeSaplingBlock) block("beech_sapling")).isFertilizable(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs)),
				"a beech sapling grows in a greenhouse");

		// A lit heater in the wall makes it warm.
		BlockPos heater = corner.add(4, 1, 2);
		ctx.setBlockState(heater, Shrubs.HEATER.getDefaultState().with(GreenhouseHeaterBlock.LIT, true));
		check(ctx, grows(ctx, in, "guarana"), "guarana grows in a heated greenhouse");
		check(ctx, grows(ctx, in, "tea"), "tea also grows in a heated greenhouse");
		ctx.setBlockState(heater, Shrubs.HEATER.getDefaultState());
		check(ctx, !grows(ctx, in, "guarana"), "a cold heater does not warm the greenhouse");

		// An open door still counts; a hole in the wall or the roof does not, nor a stone roof.
		ctx.setBlockState(corner.add(2, 1, 0), Blocks.OAK_DOOR.getDefaultState().with(net.minecraft.block.DoorBlock.OPEN, true));
		check(ctx, grows(ctx, in, "tea"), "an open door does not break the greenhouse");
		ctx.setBlockState(corner.add(1, 2, 4), Blocks.AIR);
		check(ctx, !grows(ctx, in, "tea"), "a hole in the wall breaks the greenhouse");
		ctx.setBlockState(corner.add(1, 2, 4), Blocks.STONE);
		ctx.setBlockState(corner.add(3, 4, 3), Blocks.AIR);
		check(ctx, !grows(ctx, in, "tea"), "a hole in the roof breaks the greenhouse");
		room(ctx, corner, Blocks.STONE);
		check(ctx, !grows(ctx, in, "tea"), "a stone roof is not a greenhouse");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void heaterBurnsFuelSlowly(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos, Shrubs.HEATER);
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.COAL, 2));
		ctx.getWorld().getBlockState(abs).onUseWithItem(player.getMainHandStack(), ctx.getWorld(), player, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
		check(ctx, player.getMainHandStack().getCount() == 1, "one coal goes in");
		GreenhouseHeaterBlockEntity be = (GreenhouseHeaterBlockEntity) ctx.getWorld().getBlockEntity(abs);
		// Coal burns 1600 ticks in a furnace, four times as long here: 320 seconds.
		check(ctx, be.secondsLeft() == 320, "one coal is 320 seconds of heat, got " + be.secondsLeft());
		be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		check(ctx, ctx.getWorld().getBlockState(abs).get(GreenhouseHeaterBlock.LIT), "a fuelled heater is lit");
		for (int i = 0; i < 1600 * 4; i++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, !ctx.getWorld().getBlockState(abs).get(GreenhouseHeaterBlock.LIT), "the heater goes out when the coal is spent");
		check(ctx, mod("greenhouse_heater") != Items.AIR, "the heater has an item");
		ctx.complete();
	}

	// ---------- 3.22.0: stages 2 and 3 ----------

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void thornsPrickWithoutSlowing(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(pos, block("blackberry_bush").getDefaultState().with(ShrubBlock.AGE, 2));
		check(ctx, ((ShrubBlock) block("blackberry_bush")).thorny, "brambles are thorny");
		check(ctx, !((ShrubBlock) block("raspberry_bush")).thorny, "raspberries are not");
		var pig = ctx.spawnEntity(net.minecraft.entity.EntityType.PIG, pos);
		pig.lastRenderX = pig.getX() - 0.2;
		pig.setVelocity(0.2, 0, 0);
		float before = pig.getHealth();
		ctx.getWorld().getBlockState(abs).onEntityCollision(ctx.getWorld(), abs, pig);
		check(ctx, pig.getHealth() < before, "a pig walking through brambles is pricked");
		check(ctx, pig.getHealth() >= before - 1.01f, "only a little damage");
		// No slowdown: a sweet berry bush would set the entity's movement multiplier; the bramble must not.
		check(ctx, pig.getVelocity().x == 0.2, "brambles do not slow");
		pig.discard();
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void teasGiveAnEffectAndCureOne(TestContext ctx) {
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.MINING_FATIGUE, 600));
		player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.POISON, 600));
		ItemStack tea = new ItemStack(mod("black_tea"));
		ItemStack rest = tea.finishUsing(ctx.getWorld(), player);
		check(ctx, !player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.MINING_FATIGUE), "black tea cures mining fatigue");
		check(ctx, player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.POISON), "but not poison");
		check(ctx, player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.HASTE), "black tea gives haste");
		check(ctx, rest.isOf(Items.GLASS_BOTTLE), "the bottle comes back, got " + rest);
		check(ctx, Shrubs.TEAS.size() == 8, "eight teas, got " + Shrubs.TEAS.size());

		// Two leaves and a water bottle make it; another potion does not.
		var manager = ctx.getWorld().getRecipeManager();
		ItemStack water = net.minecraft.component.type.PotionContentsComponent.createStack(Items.POTION, net.minecraft.potion.Potions.WATER);
		ItemStack awkward = net.minecraft.component.type.PotionContentsComponent.createStack(Items.POTION, net.minecraft.potion.Potions.AWKWARD);
		var good = net.minecraft.recipe.input.CraftingRecipeInput.create(3, 1, List.of(new ItemStack(mod("tea_leaf")),
				new ItemStack(mod("tea_leaf")), water));
		var bad = net.minecraft.recipe.input.CraftingRecipeInput.create(3, 1, List.of(new ItemStack(mod("tea_leaf")),
				new ItemStack(mod("tea_leaf")), awkward));
		var made = manager.getFirstMatch(net.minecraft.recipe.RecipeType.CRAFTING, good, ctx.getWorld());
		check(ctx, made.isPresent() && made.get().value().craft(good, ctx.getWorld().getRegistryManager()).isOf(mod("black_tea")),
				"tea leaves and a water bottle make black tea");
		check(ctx, manager.getFirstMatch(net.minecraft.recipe.RecipeType.CRAFTING, bad, ctx.getWorld()).isEmpty(),
				"an awkward potion is not water");
		// Raw red elderberries upset the stomach.
		var food = mod("red_elderberries").getComponents().get(net.minecraft.component.DataComponentTypes.FOOD);
		check(ctx, food != null && !food.effects().isEmpty(), "red elderberries carry nausea");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void hydrangeaTurnsBlueWithAluminiumAndPinkWithLime(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(pos, block("hydrangea_bush").getDefaultState().with(ShrubBlock.AGE, 3));
		var blue = com.periodicelements.shrubs.HydrangeaBlock.BLUE;
		check(ctx, !ctx.getWorld().getBlockState(abs).get(blue), "a wild hydrangea is pink");
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(mod("aluminium_sulfate"), 2));
		ctx.getWorld().getBlockState(abs).onUseWithItem(player.getMainHandStack(), ctx.getWorld(), player, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
		check(ctx, ctx.getWorld().getBlockState(abs).get(blue), "aluminium sulfate turns it blue");
		check(ctx, player.getMainHandStack().getCount() == 1, "one aluminium sulfate is used");
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(mod("calcium_hydroxide")));
		ctx.getWorld().getBlockState(abs).onUseWithItem(player.getMainHandStack(), ctx.getWorld(), player, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
		check(ctx, !ctx.getWorld().getBlockState(abs).get(blue), "slaked lime turns it pink again");
		check(ctx, ctx.getWorld().getBlockState(abs).get(ShrubBlock.AGE) == 3, "colouring does not cut it back");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void coldShrubsGrowOnMountainsAndInCooledGreenhouses(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.SNOW_BLOCK);
		check(ctx, block("mountain_avens_bush").getDefaultState().canPlaceAt(ctx.getWorld(), abs), "mountain avens roots in snow");
		ctx.setBlockState(pos.down(), Blocks.STONE);
		check(ctx, block("rosemary_bush").getDefaultState().canPlaceAt(ctx.getWorld(), abs), "rosemary roots in rock");
		check(ctx, !block("raspberry_bush").getDefaultState().canPlaceAt(ctx.getWorld(), abs), "raspberries do not");
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		biome(ctx, abs, 2, "snowy_plains");
		check(ctx, grows(ctx, pos, "cloudberry"), "cloudberries grow on the snowy plains");
		biome(ctx, abs, 2, "desert");
		check(ctx, !grows(ctx, pos, "cloudberry"), "no cloudberries in the desert");

		BlockPos corner = new BlockPos(0, 1, 0);
		BlockPos in = room(ctx, corner, Blocks.GLASS);
		biome(ctx, ctx.getAbsolutePos(in), 6, "desert");
		check(ctx, !grows(ctx, in, "cloudberry"), "an unheated greenhouse is not cold enough for cloudberries");
		check(ctx, grows(ctx, in, "raspberry"), "but raspberries grow in it");
		BlockPos cooler = corner.add(4, 1, 2);
		ctx.setBlockState(cooler, Shrubs.COOLER.getDefaultState().with(com.periodicelements.shrubs.GreenhouseCoolerBlock.LIT, true));
		Greenhouse.forget();
		check(ctx, Greenhouse.COLD.equals(Greenhouse.climateAt(ctx.getWorld(), ctx.getAbsolutePos(in))), "a running cooler makes it cold");
		check(ctx, grows(ctx, in, "cloudberry"), "cloudberries grow in a cooled greenhouse in the desert");
		check(ctx, grows(ctx, in, "raspberry"), "temperate shrubs grow in it too");
		check(ctx, !grows(ctx, in, "guarana"), "tropical ones do not");
		ctx.setBlockState(corner.add(0, 1, 2), Shrubs.HEATER.getDefaultState().with(GreenhouseHeaterBlock.LIT, true));
		Greenhouse.forget();
		check(ctx, Greenhouse.TEMPERATE.equals(Greenhouse.climateAt(ctx.getWorld(), ctx.getAbsolutePos(in))),
				"a heater and a cooler cancel out");
		ctx.setBlockState(corner.add(0, 1, 2), Blocks.STONE);
		ctx.setBlockState(cooler, Shrubs.COOLER.getDefaultState());
		check(ctx, !grows(ctx, in, "cloudberry"), "a cooler that is off does not cool");
		// A dwarf willow (cold-only tree-like shrub) and a cold-only tree sapling would follow the same rule.
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void coolerMeltsIceAndRunsOnCurrent(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos, Shrubs.COOLER);
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.PACKED_ICE, 2));
		ctx.getWorld().getBlockState(abs).onUseWithItem(player.getMainHandStack(), ctx.getWorld(), player, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
		check(ctx, player.getMainHandStack().getCount() == 1, "one packed ice goes in");
		var be = (com.periodicelements.shrubs.GreenhouseCoolerBlockEntity) ctx.getWorld().getBlockEntity(abs);
		check(ctx, be.secondsLeft() == 1080, "packed ice is 18 minutes of cold, got " + be.secondsLeft());
		be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		check(ctx, ctx.getWorld().getBlockState(abs).get(com.periodicelements.shrubs.GreenhouseCoolerBlock.LIT), "with ice it runs");
		for (int i = 0; i < 21600; i++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, !ctx.getWorld().getBlockState(abs).get(com.periodicelements.shrubs.GreenhouseCoolerBlock.LIT),
				"with the ice melted and no current it stops");
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIRT));
		ctx.getWorld().getBlockState(abs).onUseWithItem(player.getMainHandStack(), ctx.getWorld(), player, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
		check(ctx, player.getMainHandStack().getCount() == 1, "dirt is not ice");
		check(ctx, mod("greenhouse_cooler") != Items.AIR, "the cooler has an item");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void coolerRunsOnCurrentFromADynamo(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, Shrubs.COOLER);
		ctx.setBlockState(new BlockPos(2, 1, 1), block("copper_wire"));
		ctx.setBlockState(new BlockPos(3, 1, 1), block("copper_wire"));
		ctx.setBlockState(new BlockPos(4, 1, 1), block("dynamo"));
		ctx.setBlockState(new BlockPos(5, 1, 1), block("steam_engine"));
		com.periodicelements.machine.PowerBlockEntity engine = ctx.getBlockEntity(new BlockPos(5, 1, 1));
		engine.setStack(0, new ItemStack(Items.COAL, 64));
		for (int i = 2; i < 9; i++) {
			engine.setStack(i, new ItemStack(Items.WATER_BUCKET));
		}
		List<com.periodicelements.machine.PowerBlockEntity> plant = List.of(engine, ctx.getBlockEntity(new BlockPos(4, 1, 1)));
		BlockPos abs = ctx.getAbsolutePos(pos);
		var be = (com.periodicelements.shrubs.GreenhouseCoolerBlockEntity) ctx.getWorld().getBlockEntity(abs);
		boolean ran = false;
		for (int t = 0; t < 600 && !ran; t++) {
			for (var p : plant) {
				p.tick(ctx.getWorld(), p.getPos(), ctx.getWorld().getBlockState(p.getPos()));
			}
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			ran = ctx.getWorld().getBlockState(abs).get(com.periodicelements.shrubs.GreenhouseCoolerBlock.LIT);
		}
		check(ctx, ran && be.powered(), "a cooler wired to a running dynamo runs without ice");
		ctx.complete();
	}
}
