package com.periodicelements.test;

import com.periodicelements.compendium.Compendium;
import com.periodicelements.compendium.CompendiumData;
import com.periodicelements.compendium.DiscoveryState;
import com.periodicelements.explore.Explore;
import com.periodicelements.fauna.ColonyBlock;
import com.periodicelements.fauna.Fauna;
import com.periodicelements.fauna.FaunaAnimal;
import com.periodicelements.fauna.GuanoBlock;
import com.periodicelements.fauna.WormBinBlock;
import com.periodicelements.fauna.SericultureTrayBlock;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

/** The fauna plan (3.50.0): species, breeding out of climate, products, small animals, the Animal Compendium. */
public class FaunaTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) ctx.throwGameTestException(message);
	}

	private static Item item(String path) {
		return Registries.ITEM.get(Identifier.of("periodic_elements", path));
	}

	private static int count(TestContext ctx, Item item) {
		int n = 0;
		for (ItemEntity e : ctx.getWorld().getEntitiesByClass(ItemEntity.class, new Box(ctx.getAbsolutePos(BlockPos.ORIGIN)).expand(12),
				e -> e.getStack().isOf(item))) {
			n += e.getStack().getCount();
		}
		return n;
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void everySpeciesSpawnsWithItsEggAndLoot(TestContext ctx) {
		expect(ctx, Fauna.SPECIES.size() >= 11, "only " + Fauna.SPECIES.size() + " species");
		for (Fauna.Species s : Fauna.SPECIES.values()) {
			EntityType<?> type = Fauna.TYPES.get(s.id());
			expect(ctx, type != null && Registries.ENTITY_TYPE.getId(type).getPath().equals(s.id()), s.id() + " has no entity type");
			expect(ctx, DefaultAttributeRegistry.hasDefinitionFor(type), s.id() + " has no attributes");
			expect(ctx, item(s.id() + "_spawn_egg") != Items.AIR, s.id() + " has no spawn egg");
			expect(ctx, CompendiumData.animalOf(type) != null, s.id() + " is not in the Animal Compendium");
			var e = type.create(ctx.getWorld());
			expect(ctx, e instanceof MobEntity, s.id() + " does not spawn");
			var loot = ctx.getWorld().getServer().getReloadableRegistries().getLootTable(type.getLootTableId());
			expect(ctx, s.water() || loot != net.minecraft.loot.LootTable.EMPTY, s.id() + " has no loot table");
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void outOfClimateBreedsSlowly(TestContext ctx) {
		// The test world is plains: capybaras are of jungles and mangroves.
		FaunaAnimal a = (FaunaAnimal) ctx.spawnEntity(Fauna.TYPES.get("capybara"), 1, 2, 1);
		FaunaAnimal b = (FaunaAnimal) ctx.spawnEntity(Fauna.TYPES.get("capybara"), 2, 2, 1);
		expect(ctx, !Fauna.inClimate(ctx.getWorld(), a.getBlockPos(), a.species()), "plains should not suit a capybara");
		a.breed(ctx.getWorld(), b);
		expect(ctx, a.getBreedingAge() == Fauna.SLOW_BREEDING && b.getBreedingAge() == Fauna.SLOW_BREEDING,
				"breeding age " + a.getBreedingAge() + ", expected " + Fauna.SLOW_BREEDING);
		expect(ctx, a.isBreedingItem(new ItemStack(Items.SUGAR_CANE)), "capybaras eat sugar cane");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void frogTipsArrowsAndPoisonsBareHands(TestContext ctx) {
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		p.getAbilities().creativeMode = false;
		p.getInventory().clear();
		FaunaAnimal frog = (FaunaAnimal) ctx.spawnEntity(Fauna.TYPES.get("golden_poison_frog"), 1, 2, 1);
		p.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.ARROW, 5));
		frog.interactMob(p, Hand.MAIN_HAND);
		int tipped = 0;
		for (int i = 0; i < p.getInventory().size(); i++) {
			ItemStack s = p.getInventory().getStack(i);
			if (s.isOf(Items.TIPPED_ARROW) && s.get(DataComponentTypes.POTION_CONTENTS).matches(Potions.POISON)) tipped += s.getCount();
		}
		expect(ctx, tipped == 5, tipped + " poison arrows, expected 5");
		p.getInventory().clear();
		frog.interactMob(p, Hand.MAIN_HAND);
		expect(ctx, p.hasStatusEffect(StatusEffects.POISON), "touching the frog should poison");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void scorpionGivesVenomAndAntivenomCures(TestContext ctx) {
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		p.getAbilities().creativeMode = false;
		p.getInventory().clear();
		FaunaAnimal s = (FaunaAnimal) ctx.spawnEntity(Fauna.TYPES.get("deathstalker"), 1, 2, 1);
		p.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
		s.interactMob(p, Hand.MAIN_HAND);
		expect(ctx, p.getMainHandStack().isOf(item("scorpion_venom")), "no venom: " + p.getMainHandStack());
		p.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
		s.interactMob(p, Hand.MAIN_HAND);
		expect(ctx, p.getMainHandStack().isOf(Items.GLASS_BOTTLE), "venom should wait for the cooldown");
		p.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200));
		ItemStack anti = new ItemStack(item("antivenom"));
		ItemStack left = anti.getItem().finishUsing(anti, ctx.getWorld(), p);
		expect(ctx, !p.hasStatusEffect(StatusEffects.POISON), "antivenom should cure poison");
		expect(ctx, left.isOf(Items.GLASS_BOTTLE), "antivenom leaves a bottle");
		expect(ctx, "item:antivenom".equals(Explore.gate(new ItemStack(item("antivenom")))), "antivenom needs research");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void cochinealColonyIsStartedAndScraped(TestContext ctx) {
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		BlockPos cactus = new BlockPos(2, 2, 2);
		ctx.setBlockState(cactus.down(), Blocks.SAND);
		ctx.setBlockState(cactus, Blocks.CACTUS);
		BlockPos at = cactus.north();
		BlockState colony = Fauna.block("cochineal_colony").getDefaultState().with(ColonyBlock.FACING, Direction.NORTH);
		ctx.setBlockState(at, colony.with(ColonyBlock.AGE, 2));
		expect(ctx, ctx.getBlockState(at).canPlaceAt(ctx.getWorld(), ctx.getAbsolutePos(at)), "the colony should hold to the cactus");
		ctx.getBlockState(at).onUse(ctx.getWorld(), p, new net.minecraft.util.hit.BlockHitResult(
				ctx.getAbsolutePos(at).toCenterPos(), Direction.NORTH, ctx.getAbsolutePos(at), false));
		expect(ctx, ctx.getBlockState(at).get(ColonyBlock.AGE) == 0, "scraped colony should start again");
		expect(ctx, count(ctx, item("cochineal")) >= 1, "no cochineal scraped");
		ctx.setBlockState(cactus, Blocks.AIR);
		ctx.getWorld().updateNeighbors(ctx.getAbsolutePos(cactus), Blocks.AIR);
		ctx.runAtTick(2, () -> {
			expect(ctx, !ctx.getBlockState(at).isOf(Fauna.block("cochineal_colony")), "the colony should fall without its cactus");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void wormBinAndSericultureTrayMakeTheirGoods(TestContext ctx) {
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		BlockPos bin = new BlockPos(1, 2, 1), tray = new BlockPos(3, 2, 1);
		ctx.setBlockState(bin, Fauna.block("worm_bin"));
		ctx.setBlockState(tray, Fauna.block("sericulture_tray"));
		var hit = (java.util.function.Function<BlockPos, net.minecraft.util.hit.BlockHitResult>) b ->
				new net.minecraft.util.hit.BlockHitResult(ctx.getAbsolutePos(b).toCenterPos(), Direction.UP, ctx.getAbsolutePos(b), false);
		ctx.getBlockState(bin).onUseWithItem(new ItemStack(item("earthworm")), ctx.getWorld(), p, Hand.MAIN_HAND, hit.apply(bin));
		ctx.getBlockState(bin).onUseWithItem(new ItemStack(Items.OAK_LEAVES), ctx.getWorld(), p, Hand.MAIN_HAND, hit.apply(bin));
		expect(ctx, ctx.getBlockState(bin).get(WormBinBlock.STAGE) == 1 && ctx.getBlockState(bin).get(WormBinBlock.FOOD) == 1,
				"bin should hold worms and food: " + ctx.getBlockState(bin));
		for (int i = 0; i < 200 && ctx.getBlockState(bin).get(WormBinBlock.STAGE) != 2; i++) {
			ctx.getBlockState(bin).randomTick(ctx.getWorld(), ctx.getAbsolutePos(bin), ctx.getWorld().random);
			if (ctx.getBlockState(bin).get(WormBinBlock.FOOD) == 0 && ctx.getBlockState(bin).get(WormBinBlock.STAGE) == 1) {
				ctx.setBlockState(bin, ctx.getBlockState(bin).with(WormBinBlock.FOOD, 1));
			}
		}
		ctx.getBlockState(bin).onUse(ctx.getWorld(), p, hit.apply(bin));
		expect(ctx, count(ctx, item("humus")) == 2, "the bin should give 2 humus");
		ctx.getBlockState(tray).onUseWithItem(new ItemStack(item("silkworm")), ctx.getWorld(), p, Hand.MAIN_HAND, hit.apply(tray));
		ctx.getBlockState(tray).onUseWithItem(new ItemStack(item("white_mulberry_leaves")), ctx.getWorld(), p, Hand.MAIN_HAND, hit.apply(tray));
		expect(ctx, ctx.getBlockState(tray).get(SericultureTrayBlock.STAGE) == 2, "fed tray: " + ctx.getBlockState(tray));
		for (int i = 0; i < 200 && ctx.getBlockState(tray).get(SericultureTrayBlock.STAGE) != 3; i++) {
			ctx.getBlockState(tray).randomTick(ctx.getWorld(), ctx.getAbsolutePos(tray), ctx.getWorld().random);
		}
		ctx.getBlockState(tray).onUse(ctx.getWorld(), p, hit.apply(tray));
		expect(ctx, count(ctx, item("silk_cocoon")) >= 2, "the tray should give cocoons");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void animalsAreNotedInTheCompendium(TestContext ctx) {
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		CowEntity cow = ctx.spawnEntity(EntityType.COW, 1, 2, 1);
		var capy = ctx.spawnEntity(Fauna.TYPES.get("capybara"), 2, 2, 1);
		Compendium.noteAnimal(p, cow);
		Compendium.noteAnimal(p, capy);
		DiscoveryState s = DiscoveryState.get(ctx.getWorld().getServer());
		expect(ctx, s.knows(p.getUuid(), "animal:cow") && s.knows(p.getUuid(), "animal:capybara"), "cow and capybara should be noted");
		expect(ctx, CompendiumData.ANIMALS.size() >= 46, CompendiumData.ANIMALS.size() + " animals in the compendium");
		expect(ctx, CompendiumData.animalOf(Fauna.block("worm_bin")) != null, "the worm bin notes the earthworm");
		expect(ctx, Fauna.TYPES.get("golden_poison_frog").isIn(Explore.SMALL_ANIMALS), "a rope net should hold the frog");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void pennedCowsAreFoundAndManureFertilizes(TestContext ctx) {
		ctx.setBlockState(new BlockPos(1, 1, 1), Blocks.DIRT);
		ctx.setBlockState(new BlockPos(1, 2, 1), Blocks.OAK_SAPLING);
		ctx.setBlockState(new BlockPos(4, 2, 4), Blocks.OAK_FENCE);
		expect(ctx, Fauna.penned(ctx.getWorld(), ctx.getAbsolutePos(new BlockPos(2, 2, 2))), "a fence nearby should make a pen");
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		ItemStack manure = new ItemStack(item("manure"), 64);
		p.setStackInHand(Hand.MAIN_HAND, manure);
		for (int i = 0; i < 64 && ctx.getBlockState(new BlockPos(1, 2, 1)).isOf(Blocks.OAK_SAPLING); i++) {
			manure.useOnBlock(new net.minecraft.item.ItemUsageContext(p, Hand.MAIN_HAND, new net.minecraft.util.hit.BlockHitResult(
					ctx.getAbsolutePos(new BlockPos(1, 2, 1)).toCenterPos(), Direction.UP, ctx.getAbsolutePos(new BlockPos(1, 2, 1)), false)));
		}
		expect(ctx, !ctx.getBlockState(new BlockPos(1, 2, 1)).isOf(Blocks.OAK_SAPLING), "manure should grow the sapling");
		ctx.complete();
	}
	/** 3.50.0-beta.2: boobies' guano piles up to half a block; a beaver dams the shallow water near it. */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void guanoPilesUpAndBeaversBuildDams(TestContext ctx) {
		BlockPos g = new BlockPos(5, 2, 5);
		ctx.setBlockState(g.down(), Blocks.STONE);
		ctx.setBlockState(g, Blocks.AIR);
		for (int i = 0; i < 6; i++) GuanoBlock.addLayer(ctx.getWorld(), ctx.getAbsolutePos(g));
		expect(ctx, ctx.getBlockState(g).isOf(Fauna.block("guano")), "no guano laid");
		expect(ctx, ctx.getBlockState(g).get(net.minecraft.block.SnowBlock.LAYERS) == 4, "guano should stop at 4 layers");
		for (int x = 1; x <= 3; x++) {
			for (int z = 1; z <= 3; z++) {
				ctx.setBlockState(new BlockPos(x, 1, z), Blocks.STONE);
				ctx.setBlockState(new BlockPos(x, 2, z), Blocks.WATER);
			}
		}
		FaunaAnimal beaver = (FaunaAnimal) ctx.spawnEntity(Fauna.TYPES.get("eurasian_beaver"), 2, 2, 2);
		for (int i = 0; i < 20; i++) beaver.buildDam(ctx.getWorld());
		int dams = 0;
		for (int x = 1; x <= 3; x++) {
			for (int z = 1; z <= 3; z++) {
				if (ctx.getBlockState(new BlockPos(x, 2, z)).isOf(Fauna.block("beaver_dam"))) dams++;
			}
		}
		expect(ctx, dams >= 1, "the beaver built no dam");
		expect(ctx, Fauna.item("castoreum") != null && Fauna.item("galalith") != null, "stage 2 items missing");
		ctx.complete();
	}
	/** 3.50.0-beta.3: lichen grows on snow and stone, yaks are sheared and milked, a bear empties a full beehive. */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna", tickLimit = 300)
	public void coldAnimalsGiveWoolMilkAndRaidHives(TestContext ctx) {
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		BlockPos l = new BlockPos(5, 2, 5);
		ctx.setBlockState(l.down(), Blocks.SNOW_BLOCK);
		ctx.setBlockState(l, Fauna.block("reindeer_lichen"));
		expect(ctx, ctx.getBlockState(l).canPlaceAt(ctx.getWorld(), ctx.getAbsolutePos(l)), "lichen should grow on snow");
		FaunaAnimal yak = (FaunaAnimal) ctx.spawnEntity(Fauna.TYPES.get("yak"), 1, 2, 1);
		p.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));
		yak.interactMob(p, Hand.MAIN_HAND);
		p.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BUCKET));
		yak.interactMob(p, Hand.MAIN_HAND);
		expect(ctx, p.getInventory().contains(new ItemStack(Items.MILK_BUCKET)) || p.getMainHandStack().isOf(Items.MILK_BUCKET), "the yak gave no milk");
		BlockPos hive = new BlockPos(3, 2, 3);
		ctx.setBlockState(hive, Blocks.BEEHIVE.getDefaultState().with(net.minecraft.block.BeehiveBlock.HONEY_LEVEL, 5));
		var bear = (FaunaAnimal) ctx.spawnEntity(Fauna.TYPES.get("brown_bear"), 3, 2, 4);
		bear.setAiDisabled(true);
		ctx.runAtTick(15, () -> expect(ctx, count(ctx, item("yak_wool")) >= 1, "no yak wool sheared"));
		ctx.runAtTick(215, () -> {
			expect(ctx, ctx.getBlockState(hive).get(net.minecraft.block.BeehiveBlock.HONEY_LEVEL) == 0, "the bear left the full hive alone");
			ctx.complete();
		});
	}

	/** 3.50.0-beta.4: pearl oysters grow on sand under water and drop shells; the sperm whale cannot be hunted, but a stranded one gives spermaceti; a horseshoe crab is bled into a bottle. */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fauna")
	public void oystersStrandingsAndCrabBlood(TestContext ctx) {
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		BlockPos o = new BlockPos(5, 2, 5);
		ctx.setBlockState(o.down(), Blocks.SAND);
		ctx.setBlockState(o, Fauna.block("pearl_oyster").getDefaultState().with(net.minecraft.state.property.Properties.WATERLOGGED, true));
		expect(ctx, ctx.getBlockState(o).canPlaceAt(ctx.getWorld(), ctx.getAbsolutePos(o)), "the oyster should sit on sand");
		ctx.getWorld().breakBlock(ctx.getAbsolutePos(o), true);
		expect(ctx, count(ctx, item("oyster_shell")) >= 2, "the oyster dropped no shells");
		var crab = (FaunaAnimal) ctx.spawnEntity(Fauna.TYPES.get("horseshoe_crab"), 1, 2, 1);
		p.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
		crab.interactMob(p, Hand.MAIN_HAND);
		expect(ctx, p.getMainHandStack().isOf(item("horseshoe_crab_blood")) || p.getInventory().contains(new ItemStack(item("horseshoe_crab_blood"))),
				"the horseshoe crab gave no blood");
		expect(ctx, crab.isAlive(), "the crab should live on");
		BlockPos w = new BlockPos(3, 2, 3);
		expect(ctx, com.periodicelements.fauna.PearlOysterBlock.strandAt(ctx.getWorld(), ctx.getAbsolutePos(w)), "no whale stranded");
		var whales = ctx.getWorld().getEntitiesByClass(net.minecraft.entity.LivingEntity.class, new Box(ctx.getAbsolutePos(w)).expand(4),
				e -> e.getType() == Fauna.TYPES.get("sperm_whale"));
		expect(ctx, whales.size() == 1, "the stranded whale is missing");
		var whale = whales.get(0);
		expect(ctx, !whale.damage(ctx.getWorld().getDamageSources().playerAttack(p), 1000f), "players must not hurt the sperm whale");
		whale.damage(ctx.getWorld().getDamageSources().dryOut(), 1000f);
		expect(ctx, !whale.isAlive(), "the stranded whale should dry out");
		int sperm = 0;
		for (ItemEntity e : ctx.getWorld().getEntitiesByClass(ItemEntity.class, new Box(ctx.getAbsolutePos(w)).expand(8), e -> true)) {
			if (e.getStack().isOf(item("spermaceti"))) sperm += e.getStack().getCount();
		}
		expect(ctx, sperm >= 4, "the stranded whale left no spermaceti");
		expect(ctx, item("ambergris") != Items.AIR, "stage 4 items missing");
		ctx.complete();
	}
}
