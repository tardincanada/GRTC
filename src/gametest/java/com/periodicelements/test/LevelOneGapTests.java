package com.periodicelements.test;

import com.periodicelements.ModContent;
import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.research.ResearchStationBlockEntity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** The 0.5.1 fills for level 1 and 2: salt, fermentation, brick, glass, porcelain, acids, recipes, printing. */
public class LevelOneGapTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static MachineBlockEntity place(TestContext ctx, String machine, List<String> projects) {
		ctx.setBlockState(POS, block(machine));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(mod("glass_flask"), 32));
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, projects);
		return be;
	}

	/** Ticks until the input is used up and the machine goes idle (or waits for a reagent). */
	private static int tick(TestContext ctx, MachineBlockEntity be, int limit) {
		BlockPos abs = ctx.getAbsolutePos(POS);
		int t = 0;
		for (; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (be.getStack(MachineBlockEntity.RESIDUE).getCount() >= 60) {
				be.setStack(MachineBlockEntity.RESIDUE, ItemStack.EMPTY);
			}
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& (be.status() == MachineBlockEntity.Status.IDLE || be.status() == MachineBlockEntity.Status.NEEDS_REAGENT)) {
				break;
			}
		}
		return t;
	}

	private static MachineBlockEntity run(TestContext ctx, String machine, List<String> projects, ItemStack input,
										  ItemStack reagent, ItemStack fuel, int limit) {
		MachineBlockEntity be = place(ctx, machine, projects);
		be.setStack(MachineBlockEntity.INPUT, input);
		be.setStack(MachineBlockEntity.REAGENT, reagent);
		be.setStack(MachineBlockEntity.FUEL, fuel);
		int t = tick(ctx, be, limit);
		log(machine, t, be);
		return be;
	}

	private static void log(String machine, int t, MachineBlockEntity be) {
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks, {} °C, status {}, outputs {} {} {} | residue {} | reagent {} | fuel {} | buffers {}",
				machine, t, String.format(Locale.ROOT, "%.0f", be.temperature()), be.status(), be.getStack(2),
				be.getStack(3), be.getStack(4), be.getStack(5), be.getStack(6), be.getStack(MachineBlockEntity.FUEL),
				be.outputBuffers());
	}

	private static int count(MachineBlockEntity be, Item item) {
		int n = 0;
		for (int i = MachineBlockEntity.PRODUCT_FIRST; i <= MachineBlockEntity.RESIDUE; i++) {
			if (be.getStack(i).isOf(item)) {
				n += be.getStack(i).getCount();
			}
		}
		return n;
	}

	private static void expect(TestContext ctx, int got, int want, String what) {
		if (got != want) {
			ctx.throwGameTestException("expected " + want + " " + what + ", got " + got);
		}
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static ItemStack charcoal() {
		return new ItemStack(Items.CHARCOAL, 64);
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void saltPanDriesSeawaterWithoutFuel(TestContext ctx) {
		// 5 buckets x 27.2 units of NaCl = 136 units -> 1 salt; the buckets come back.
		MachineBlockEntity be = place(ctx, "salt_pan", List.of("salt_evaporation"));
		int t = 0;
		for (int i = 0; i < 5; i++) {
			be.setStack(MachineBlockEntity.INPUT, new ItemStack(ModContent.SEAWATER_BUCKET));
			t += tick(ctx, be, 5_000);
		}
		log("salt_pan", t, be);
		expect(ctx, count(be, mod("sodium_chloride")), 1, "salt");
		expect(ctx, count(be, Items.BUCKET), 5, "empty buckets");
		check(ctx, be.temperature() < 21, "the salt pan should stay at air temperature");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void barrelFermentsSugar(TestContext ctx) {
		// 10 sugar (1111 units of sucrose) + water -> 598 units of ethanol -> 5 ethanol, no fuel.
		MachineBlockEntity be = run(ctx, "fermentation_barrel", List.of("fermentation"), new ItemStack(Items.SUGAR, 10),
				new ItemStack(Items.WATER_BUCKET), ItemStack.EMPTY, 20_000);
		expect(ctx, count(be, mod("ethanol")), 5, "ethanol");
		expect(ctx, count(be, Items.BUCKET), 1, "empty bucket");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void vatSoursEthanolToVinegar(TestContext ctx) {
		// 5 ethanol (555.5 units) -> 724 units of acetic acid -> 6.
		MachineBlockEntity be = run(ctx, "vinegar_vat", List.of("fermentation"), new ItemStack(mod("ethanol"), 5),
				ItemStack.EMPTY, ItemStack.EMPTY, 20_000);
		expect(ctx, count(be, mod("acetic_acid")), 6, "acetic acid");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void kilnFiresClayIntoBrick(TestContext ctx) {
		// 8 clay balls: 1200 kaolinite + 400 quartz -> 1432 units of brick -> 12 bricks.
		MachineBlockEntity be = run(ctx, "pottery_kiln", List.of("fired_pottery", "bellows"),
				new ItemStack(Items.CLAY_BALL, 8), ItemStack.EMPTY, charcoal(), 40_000);
		expect(ctx, count(be, Items.BRICK), 12, "bricks");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void kilnShowsActiveWhileFiring(TestContext ctx) {
		// 3.28.1: the kiln switches to its animated model only while the clay is firing.
		MachineBlockEntity be = place(ctx, "pottery_kiln", List.of("fired_pottery", "bellows"));
		be.setStack(MachineBlockEntity.FUEL, charcoal());
		for (int t = 0; t < 200; t++) {
			be.tick(ctx.getWorld(), be.getPos(), ctx.getWorld().getBlockState(be.getPos()));
		}
		if (ctx.getWorld().getBlockState(be.getPos()).get(com.periodicelements.machine.MachineBlock.ACTIVE)) {
			ctx.throwGameTestException("kiln with no clay shows active");
		}
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.CLAY_BALL, 8));
		int active = 0;
		for (int t = 0; t < 40_000 && count(be, Items.BRICK) < 12; t++) {
			be.tick(ctx.getWorld(), be.getPos(), ctx.getWorld().getBlockState(be.getPos()));
			if (ctx.getWorld().getBlockState(be.getPos()).get(com.periodicelements.machine.MachineBlock.ACTIVE)) {
				active++;
			}
		}
		if (active < 100) {
			ctx.throwGameTestException("kiln was active for only " + active + " ticks while firing");
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void glassFurnaceMeltsSandWithAsh(TestContext ctx) {
		// 2 sand + about 19 wood ash: 2190 units of silica -> 3610 units of forest glass -> 3 blocks.
		MachineBlockEntity be = run(ctx, "glass_furnace", List.of("glassmaking", "lime_burning", "fired_pottery", "bellows"),
				new ItemStack(Items.SAND, 2), new ItemStack(mod("wood_ash"), 24), charcoal(), 80_000);
		expect(ctx, count(be, Items.GLASS), 3, "glass");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void porcelainKilnFiresKaolin(TestContext ctx) {
		// 8 clay balls + quartz: 1200 kaolinite + 480 quartz -> 1512 units -> 13 porcelain.
		MachineBlockEntity be = run(ctx, "porcelain_kiln", List.of("porcelain", "bellows"),
				new ItemStack(Items.CLAY_BALL, 8), new ItemStack(Items.QUARTZ, 4), charcoal(), 60_000);
		expect(ctx, count(be, mod("porcelain")), 13, "porcelain");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void alembicMakesSpiritOfSalt(TestContext ctx) {
		// 4 salt (444.4 units) + 746 units of sulfuric acid -> 277 units of HCl -> 2.
		MachineBlockEntity be = run(ctx, "alembic", List.of("alembic_distillation", "mineral_acids"),
				new ItemStack(mod("sodium_chloride"), 4), new ItemStack(mod("sulfuric_acid"), 8), charcoal(), 40_000);
		expect(ctx, count(be, mod("hydrochloric_acid")), 2, "hydrochloric acid");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void aquaRegiaDissolvesGold(TestContext ctx) {
		// 4 gold (444.4 units) take 493 units of aqua regia (4.4 items, 5 given) and come back as 4 pure ingots.
		MachineBlockEntity be = run(ctx, "alembic", List.of("alembic_distillation", "mineral_acids", "aqua_regia"),
				new ItemStack(mod("gold"), 4), new ItemStack(mod("aqua_regia"), 5), ItemStack.EMPTY, 40_000);
		int gold = count(be, Items.GOLD_INGOT);
		check(ctx, gold >= 3 && gold <= 4, "expected 4 gold ingots (3 if rounding), got " + gold);
		check(ctx, be.temperature() < 21, "aqua regia works cold, with no fuel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void recipesReplaceVanillaIronGear(TestContext ctx) {
		var recipes = ctx.getWorld().getRecipeManager();
		for (String id : List.of("iron_pickaxe", "iron_sword", "iron_chestplate")) {
			check(ctx, recipes.get(Identifier.ofVanilla(id)).isEmpty(), "vanilla " + id + " recipe still loaded");
		}
		for (String id : List.of("wrought_iron_pickaxe", "wrought_iron_chestplate", "lime_slaking", "bark_tanning",
				"leavened_bread", "aqua_regia", "iron_gall_ink", "magnifying_lens", "pottery_kiln", "glass_furnace")) {
			check(ctx, recipes.get(Identifier.of(PeriodicElements.MOD_ID, id)).isPresent(), "missing recipe " + id);
		}
		check(ctx, new ItemStack(mod("wrought_iron_pickaxe")).getMaxDamage() == 300, "wrought iron pickaxe durability");
		check(ctx, new ItemStack(mod("iron_gall_ink")).isIn(ResearchStationBlockEntity.INK), "iron-gall ink is ink");
		check(ctx, ModContent.LEAVENED_BREAD.getDefaultStack().get(DataComponentTypes.FOOD).nutrition() == 7, "bread food");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void printingMakesTwoCopies(TestContext ctx) {
		ctx.setBlockState(POS, block("alchemists_laboratory"));
		ResearchStationBlockEntity station = ctx.getBlockEntity(POS);
		ServerPlayerEntity writer = ctx.createMockCreativeServerPlayerInWorld();
		var server = ctx.getWorld().getServer();
		ResearchState.get(server).grant(server, writer.getUuid(), List.of("writing", "printing_press", "fired_pottery", "lime_burning"));
		// 4 projects: 1 type metal + 2 leather + 2 paper + 2 ink, for 2 printed copies.
		station.setStack(0, new ItemStack(mod("type_metal"), 1));
		station.setStack(1, new ItemStack(Items.LEATHER, 2));
		station.setStack(2, new ItemStack(Items.PAPER, 2));
		station.setStack(3, new ItemStack(mod("iron_gall_ink"), 2));
		check(ctx, station.writeTreatise(writer) == null, "could not print the treatise");
		int copies = 0;
		for (int i = 0; i < writer.getInventory().size(); i++) {
			ItemStack s = writer.getInventory().getStack(i);
			if (s.isOf(ModContent.TREATISE_PRINTED) && s.get(ModContent.TREATISE_CONTENTS) != null
					&& s.get(ModContent.TREATISE_CONTENTS).projects().size() == 4) {
				copies += s.getCount();
			}
		}
		expect(ctx, copies, 2, "printed copies");
		ctx.complete();
	}
}
