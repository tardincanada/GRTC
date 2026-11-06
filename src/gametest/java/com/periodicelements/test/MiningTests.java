package com.periodicelements.test;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.MiningChargeBlock;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.mining.AreaMining;
import com.periodicelements.mining.BatteryPackItem;
import com.periodicelements.mining.ChargingBenchBlockEntity;
import com.periodicelements.mining.DrillItem;
import com.periodicelements.mining.Mining;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 2.1.0 Mining tools: hammers, excavators, the Blasting Charge, the drills and their bits, packs and the bench. */
public class MiningTests implements FabricGameTest {
	private static final BlockPos WALL = new BlockPos(4, 3, 2);

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

	private static ServerPlayerEntity player(TestContext ctx, String... projects) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.getInventory().clear();
		player.getInventory().selectedSlot = 8; // packs and cylinders go in from slot 0, clear of the hand
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), player.getUuid(), List.of(projects));
		return player;
	}

	/** A wall of {@code fill} (2r+1 square) facing north, centred on WALL. */
	private static void wall(TestContext ctx, Block fill, int r) {
		for (int a = -r; a <= r; a++) {
			for (int b = -r; b <= r; b++) {
				ctx.setBlockState(WALL.add(a, b, 0), fill);
			}
		}
	}

	private static int count(ServerPlayerEntity player, Item item) {
		int n = 0;
		for (int i = 0; i < player.getInventory().size(); i++) {
			if (player.getInventory().getStack(i).isOf(item)) {
				n += player.getInventory().getStack(i).getCount();
			}
		}
		return n;
	}

	private static int mine(TestContext ctx, ServerPlayerEntity player, ItemStack tool, BlockPos at, Direction side) {
		player.setStackInHand(Hand.MAIN_HAND, tool);
		return AreaMining.harvest(player, tool, ctx.getAbsolutePos(at), side);
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void sledgehammer(TestContext ctx) {
		ServerPlayerEntity player = player(ctx, "quarry_tools");
		check(ctx, AreaMining.knows(player, "item:sledgehammers"), "research lets the hammer work 3x3");
		check(ctx, !com.periodicelements.research.Research.allows(java.util.Set.of(), "item:sledgehammers"), "without research it mines one block");
		wall(ctx, Blocks.STONE, 1);
		ctx.setBlockState(WALL.add(1, 1, 0), Blocks.DEEPSLATE);   // harder than the aimed stone: stays
		ctx.setBlockState(WALL.add(-1, -1, 0), Blocks.FURNACE);   // a block entity: stays
		ItemStack hammer = new ItemStack(mod("steel_sledgehammer"));
		int broke = mine(ctx, player, hammer, WALL, Direction.NORTH);
		check(ctx, broke == 7, "7 of 9 broken, got " + broke);
		check(ctx, hammer.getDamage() == 7, "one wear per block: " + hammer.getDamage());
		check(ctx, count(player, Items.COBBLESTONE) == 7, "drops in the pack: " + count(player, Items.COBBLESTONE));
		check(ctx, ctx.getBlockState(WALL.add(1, 1, 0)).isOf(Blocks.DEEPSLATE), "deepslate spared");
		check(ctx, ctx.getBlockState(WALL.add(-1, -1, 0)).isOf(Blocks.FURNACE), "furnace spared");
		// A sledgehammer is slowed to 0.3 by the break-speed attribute.
		double speed = hammer.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, null).modifiers().stream()
				.filter(m -> m.attribute().matchesKey(net.minecraft.entity.attribute.EntityAttributes.PLAYER_BLOCK_BREAK_SPEED.getKey().orElseThrow()))
				.mapToDouble(m -> m.modifier().value()).sum();
		check(ctx, Math.abs(speed + 0.7) < 1e-6, "x0.3 break speed: " + speed);
		// The Excavator: 3x3 of earth.
		wall(ctx, Blocks.DIRT, 1);
		ItemStack excavator = new ItemStack(mod("steel_excavator"));
		check(ctx, mine(ctx, player, excavator, WALL, Direction.NORTH) == 9, "excavator 3x3");
		check(ctx, count(player, Items.DIRT) == 9, "nine dirt");
		// Through the real break path (the block-break event): a 3x3 floor goes at once; sneaking takes one block.
		BlockPos floor = new BlockPos(4, 1, 6);
		for (BlockPos p : BlockPos.iterate(floor.add(-1, 0, -1), floor.add(1, 0, 1))) {
			ctx.setBlockState(p, Blocks.STONE);
		}
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(mod("steel_sledgehammer")));
		player.setSneaking(true);
		player.interactionManager.tryBreakBlock(ctx.getAbsolutePos(floor.add(1, 0, 1)));
		check(ctx, ctx.getBlockState(floor).isOf(Blocks.STONE), "sneaking: one block");
		player.setSneaking(false);
		player.interactionManager.tryBreakBlock(ctx.getAbsolutePos(floor));
		check(ctx, ctx.getBlockState(floor).isAir() && ctx.getBlockState(floor.add(-1, 0, -1)).isAir(), "the event breaks the floor 3x3");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void blastingCharge(TestContext ctx) {
		BlockPos c = new BlockPos(4, 3, 4);
		for (BlockPos p : BlockPos.iterate(c.add(-2, -2, -2), c.add(2, 2, 2))) {
			ctx.setBlockState(p, Blocks.STONE);
		}
		ctx.setBlockState(c.add(1, 0, 0), Blocks.FURNACE);
		ctx.setBlockState(c.add(0, 1, 0), Blocks.DIRT);
		ctx.setBlockState(c, Mining.BLASTING_CHARGE);
		MiningChargeBlock.detonate(ctx.getWorld(), ctx.getAbsolutePos(c));
		check(ctx, ctx.getBlockState(c.add(1, 1, 1)).isAir() && ctx.getBlockState(c.add(-1, -1, -1)).isAir(), "3x3x3 broken");
		check(ctx, ctx.getBlockState(c.add(2, 0, 0)).isOf(Blocks.STONE), "radius 1 only");
		check(ctx, ctx.getBlockState(c.add(1, 0, 0)).isOf(Blocks.FURNACE), "machines are spared");
		check(ctx, ctx.getBlockState(c.add(0, 1, 0)).isOf(Blocks.DIRT), "only stone-type blocks break");
		Box box = new Box(ctx.getAbsolutePos(c)).expand(4);
		int cobble = ctx.getWorld().getEntitiesByClass(ItemEntity.class, box, e -> e.getStack().isOf(Items.COBBLESTONE))
				.stream().mapToInt(e -> e.getStack().getCount()).sum();
		check(ctx, cobble == 24, "every block drops: " + cobble);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void airCompressor(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, block("air_compressor"));
		MachineBlockEntity be = ctx.getBlockEntity(pos);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, new ArrayList<>(List.of("rock_drill")));
		be.setStack(MachineBlockEntity.CYLINDERS, new ItemStack(Fluids.STEEL_CYLINDER, 4));
		List<PowerBlockEntity> plant = powerPlant(ctx);
		BlockPos abs = ctx.getAbsolutePos(pos);
		Item air = DrillItem.airCylinder();
		int t = 0;
		for (; t < 20_000; t++) {
			for (PowerBlockEntity p : plant) {
				p.tick(ctx.getWorld(), p.getPos(), ctx.getWorld().getBlockState(p.getPos()));
			}
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			int filled = 0;
			for (int s = MachineBlockEntity.PRODUCT_FIRST; s <= MachineBlockEntity.PRODUCT_LAST; s++) {
				if (be.getStack(s).isOf(air)) {
					filled += be.getStack(s).getCount();
				}
			}
			if (filled >= 2) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] air_compressor: {} ticks, status {}, outputs {} {} {} | cylinders {} | buffers {}",
				t, be.status(), be.getStack(2), be.getStack(3), be.getStack(4), be.getStack(MachineBlockEntity.CYLINDERS), be.outputBuffers());
		check(ctx, t < 20_000, "two cylinders of compressed air, no Compressor upgrade");
		ctx.complete();
	}

	private static List<PowerBlockEntity> powerPlant(TestContext ctx) {
		ctx.setBlockState(new BlockPos(2, 1, 1), block("copper_wire"));
		ctx.setBlockState(new BlockPos(3, 1, 1), block("copper_wire"));
		ctx.setBlockState(new BlockPos(4, 1, 1), block("dynamo"));
		ctx.setBlockState(new BlockPos(5, 1, 1), block("steam_engine"));
		PowerBlockEntity engine = ctx.getBlockEntity(new BlockPos(5, 1, 1));
		engine.setStack(0, new ItemStack(Items.COAL, 64));
		engine.setStack(1, new ItemStack(Items.COAL, 64));
		for (int i = 2; i < 9; i++) {
			engine.setStack(i, new ItemStack(Items.WATER_BUCKET));
		}
		return List.of(engine, ctx.getBlockEntity(new BlockPos(4, 1, 1)));
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pneumaticDrill(TestContext ctx) {
		ServerPlayerEntity player = player(ctx, "rock_drill");
		ItemStack drill = new ItemStack(Mining.PNEUMATIC_ROCK_DRILL);
		DrillItem item = (DrillItem) drill.getItem();
		check(ctx, item.getMiningSpeed(drill, Blocks.STONE.getDefaultState()) == 1f, "no bit, no air: hand speed");
		DrillItem.setBit(drill, new ItemStack(Mining.TUNGSTEN_STEEL_BIT));
		player.getInventory().setStack(0, new ItemStack(DrillItem.airCylinder(), 2));
		player.setStackInHand(Hand.MAIN_HAND, drill);
		item.refill(drill, player);
		check(ctx, DrillItem.energy(drill) == DrillItem.CYLINDER_KJ, "one cylinder fills the drill: " + DrillItem.energy(drill));
		check(ctx, count(player, DrillItem.airCylinder()) == 1 && count(player, Fluids.STEEL_CYLINDER) == 1, "the empty cylinder comes back");
		check(ctx, item.getMiningSpeed(drill, Blocks.STONE.getDefaultState()) == 8.5f, "tungsten bit speed 8.5");
		check(ctx, item.isCorrectForDrops(drill, Blocks.OBSIDIAN.getDefaultState()), "tungsten mines obsidian");
		check(ctx, item.slows(player), "the rock drill is heavy");
		item.setMode(drill, DrillItem.Mode.TUNNEL);
		wall(ctx, Blocks.STONE, 1);
		check(ctx, mine(ctx, player, drill, WALL, Direction.NORTH) == 3, "1x3 tunnel");
		item.setMode(drill, DrillItem.Mode.AREA3);
		wall(ctx, Blocks.STONE, 1);
		check(ctx, mine(ctx, player, drill, WALL, Direction.NORTH) == 9, "3x3");
		double used = DrillItem.CYLINDER_KJ - DrillItem.energy(drill);
		check(ctx, Math.abs(used - 12 * 1.5 * 1.5) < 1e-6, "1.5 kJ per hardness point: " + used);
		check(ctx, DrillItem.bit(drill).getDamage() == 2, "one wear per swing: " + DrillItem.bit(drill).getDamage());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void chargingBench(TestContext ctx) {
		List<PowerBlockEntity> plant = powerPlant(ctx);
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, Mining.CHARGING_BENCH);
		ChargingBenchBlockEntity bench = ctx.getBlockEntity(pos);
		ItemStack pack = new ItemStack(Mining.LEAD_ACID_PACK);
		bench.setStack(0, pack);
		for (int t = 0; t < 600; t++) {
			for (PowerBlockEntity p : plant) {
				p.tick(ctx.getWorld(), p.getPos(), ctx.getWorld().getBlockState(p.getPos()));
			}
			bench.tick(ctx.getWorld(), ctx.getAbsolutePos(pos));
		}
		double e = BatteryPackItem.energy(bench.getStack(0));
		PeriodicElements.LOGGER.info("[gametest] charging_bench: lead-acid pack {} kJ after 600 ticks", e);
		check(ctx, e > 0 && e <= 600 * ChargingBenchBlockEntity.RATE + 1e-6, "charged at up to 5 kJ a tick: " + e);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void rotaryHammerAndBits(TestContext ctx) {
		ServerPlayerEntity player = player(ctx, "rotary_hammer");
		ItemStack pack = new ItemStack(Mining.LEAD_ACID_PACK);
		BatteryPackItem.setEnergy(pack, 1500);
		player.getInventory().setStack(0, pack);
		ItemStack drill = new ItemStack(Mining.ROTARY_HAMMER);
		DrillItem item = (DrillItem) drill.getItem();
		var enchants = ctx.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
		ItemStack bit = new ItemStack(Mining.CEMENTED_CARBIDE_BIT);
		bit.addEnchantment(enchants.getOrThrow(Enchantments.FORTUNE), 3);
		bit.addEnchantment(enchants.getOrThrow(Enchantments.MENDING), 1);
		DrillItem.setBit(drill, bit);
		ItemEnchantmentsComponent lent = drill.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
		check(ctx, lent.getLevel(enchants.getOrThrow(Enchantments.FORTUNE)) == 3, "the bit lends Fortune");
		check(ctx, lent.getLevel(enchants.getOrThrow(Enchantments.MENDING)) == 0, "but not Mending");
		player.setStackInHand(Hand.MAIN_HAND, drill);
		item.refill(drill, player);
		check(ctx, DrillItem.energy(drill) == item.capacity() && BatteryPackItem.energy(pack) == 1500 - item.capacity(),
				"the pack tops up the hammer");
		check(ctx, !item.slows(player), "the rotary hammer never slows you");
		item.setMode(drill, DrillItem.Mode.AREA3);
		wall(ctx, Blocks.DEEPSLATE, 1);
		check(ctx, mine(ctx, player, drill, WALL, Direction.NORTH) == 9, "3x3 deepslate");
		check(ctx, Math.abs(item.capacity() - DrillItem.energy(drill) - 9 * 4.5) < 1e-6, "4.5 kJ a deepslate");
		check(ctx, count(player, Items.COBBLED_DEEPSLATE) == 9, "drops in the pack");
		// Fortune III on iron ore: more raw iron than ore, on average.
		wall(ctx, Blocks.IRON_ORE, 1);
		mine(ctx, player, drill, WALL, Direction.NORTH);
		check(ctx, count(player, Items.RAW_IRON) > 9, "Fortune from the bit: " + count(player, Items.RAW_IRON));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void heavyDrill(TestContext ctx) {
		ServerPlayerEntity player = player(ctx, "heavy_drill", "ore_seeking");
		ItemStack lead = new ItemStack(Mining.LEAD_ACID_PACK);
		BatteryPackItem.setEnergy(lead, 1500);
		player.getInventory().setStack(0, lead);
		ItemStack drill = new ItemStack(Mining.HEAVY_MINING_DRILL);
		DrillItem item = (DrillItem) drill.getItem();
		DrillItem.setBit(drill, new ItemStack(Mining.SYNTHETIC_DIAMOND_BIT));
		check(ctx, item.slows(player), "on lead-acid the heavy drill slows you");
		ItemStack li = new ItemStack(Mining.LI_ION_PACK);
		BatteryPackItem.setEnergy(li, 6000);
		player.getInventory().setStack(1, li);
		check(ctx, !item.slows(player), "a Li-ion pack lifts the slowness");
		player.setStackInHand(Hand.MAIN_HAND, drill);
		item.refill(drill, player);
		check(ctx, BatteryPackItem.energy(li) < 6000 && BatteryPackItem.energy(lead) == 1500, "Li-ion is drawn first");
		item.setMode(drill, DrillItem.Mode.AREA5);
		wall(ctx, Blocks.STONE, 2);
		check(ctx, mine(ctx, player, drill, WALL, Direction.NORTH) == 25, "5x5");
		// Vein mode: ten joined iron ores, one apart, stone round them.
		item.setMode(drill, DrillItem.Mode.VEIN);
		BlockPos v = new BlockPos(2, 2, 6);
		for (int i = 0; i < 10; i++) {
			ctx.setBlockState(v.add(i % 5, i / 5, 0), Blocks.IRON_ORE);
		}
		ctx.setBlockState(v.add(7, 0, 0), Blocks.IRON_ORE);
		ctx.setBlockState(v.add(5, 0, 0), Blocks.STONE);
		int seen = DrillItem.survey(player, ctx.getWorld(), ctx.getAbsolutePos(v));
		check(ctx, seen == 10, "ore-seeking reads the vein: " + seen);
		int broke = mine(ctx, player, drill, v, Direction.NORTH);
		check(ctx, broke == 10, "vein mode follows the ore: " + broke);
		check(ctx, ctx.getBlockState(v.add(7, 0, 0)).isOf(Blocks.IRON_ORE), "a separate ore stays");
		check(ctx, ctx.getBlockState(v.add(5, 0, 0)).isOf(Blocks.STONE), "stone stays");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void chainsaw(TestContext ctx) {
		ServerPlayerEntity player = player(ctx, "chainsaw");
		ItemStack pack = new ItemStack(Mining.LEAD_ACID_PACK);
		BatteryPackItem.setEnergy(pack, 1500);
		player.getInventory().setStack(0, pack);
		ItemStack saw = new ItemStack(Mining.CHAINSAW);
		DrillItem item = (DrillItem) saw.getItem();
		check(ctx, item.mode(saw) == DrillItem.Mode.TREE, "the chainsaw starts on whole trees");
		BlockPos base = new BlockPos(4, 1, 4);
		for (int y = 0; y < 6; y++) {
			ctx.setBlockState(base.up(y), Blocks.OAK_LOG);
		}
		ctx.setBlockState(base.add(1, 4, 0), Blocks.OAK_LOG);
		ctx.setBlockState(base.add(2, 5, 1), Blocks.OAK_LOG);
		ctx.setBlockState(base.up(6), Blocks.OAK_LEAVES);
		player.setStackInHand(Hand.MAIN_HAND, saw);
		item.refill(saw, player);
		int broke = mine(ctx, player, saw, base, Direction.NORTH);
		check(ctx, broke == 8, "the whole tree: " + broke);
		check(ctx, ctx.getBlockState(base.up(6)).isOf(Blocks.OAK_LEAVES), "leaves stay");
		check(ctx, count(player, Items.OAK_LOG) == 8, "eight logs in the pack");
		check(ctx, Math.abs(item.capacity() - DrillItem.energy(saw) - 8 * 3) < 1e-6, "3 kJ a log");
		ctx.complete();
	}
}
