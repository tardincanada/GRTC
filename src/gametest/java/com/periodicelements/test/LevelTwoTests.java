package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** The 0.5.0 machines (level 1 and 2): alloys, zinc, arsenic, antimony, cobalt, bismuth, silver, phosphorus, iron. */
public class LevelTwoTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static MachineBlockEntity run(TestContext ctx, String machine, List<String> projects, ItemStack input,
										  ItemStack reagent, ItemStack fuel, int limit) {
		ctx.setBlockState(POS, block(machine));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(mod("glass_flask"), 32));
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, projects);
		be.setStack(MachineBlockEntity.INPUT, input);
		be.setStack(MachineBlockEntity.REAGENT, reagent);
		be.setStack(MachineBlockEntity.FUEL, fuel);
		BlockPos abs = ctx.getAbsolutePos(POS);
		double peak = 0;
		int t = 0;
		for (; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			peak = Math.max(peak, be.temperature());
			if (be.getStack(MachineBlockEntity.RESIDUE).getCount() >= 60) {
				be.setStack(MachineBlockEntity.RESIDUE, ItemStack.EMPTY);
			}
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& (be.status() == MachineBlockEntity.Status.IDLE || be.status() == MachineBlockEntity.Status.NEEDS_REAGENT)) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks, peak {} °C, status {}, outputs {} {} {} | residue {} | reagent {} | buffers {}",
				machine, t, String.format(Locale.ROOT, "%.0f", peak), be.status(), be.getStack(2), be.getStack(3),
				be.getStack(4), be.getStack(5), be.getStack(6), be.outputBuffers());
		return be;
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

	private static ItemStack charcoal() {
		return new ItemStack(Items.CHARCOAL, 64);
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void crucibleMakesBronze(TestContext ctx) {
		// 9 copper (999.9 units) + 1 tin (111.1 units) -> 1111.0 units of bronze: 9 ingots, 111.09 units waiting.
		MachineBlockEntity be = run(ctx, "crucible", List.of("bronze_alloying", "bellows"), new ItemStack(Items.COPPER_INGOT, 9),
				new ItemStack(mod("tin"), 1), charcoal(), 40_000);
		expect(ctx, count(be, mod("bronze_ingot")), 9, "bronze ingots");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void crucibleWaitsForTin(TestContext ctx) {
		MachineBlockEntity be = run(ctx, "crucible", List.of("bronze_alloying"), new ItemStack(Items.COPPER_INGOT, 4),
				ItemStack.EMPTY, charcoal(), 2_000);
		expect(ctx, be.getStack(MachineBlockEntity.INPUT).getCount(), 4, "copper left in the input");
		if (be.status() != MachineBlockEntity.Status.NEEDS_REAGENT) {
			ctx.throwGameTestException("status " + be.status());
		}
		expect(ctx, be.getStack(MachineBlockEntity.FUEL).getCount(), 64, "charcoal (none burned)");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void crucibleMakesBrass(TestContext ctx) {
		// 7 copper (777.7) + 3 zinc (333.3): 333.3 zinc covers 777.7 copper -> 1111 units -> 10 brass.
		MachineBlockEntity be = run(ctx, "crucible", List.of("bronze_alloying", "zinc_distillation", "bellows"),
				new ItemStack(Items.COPPER_INGOT, 7), new ItemStack(mod("zinc"), 3), charcoal(), 40_000);
		expect(ctx, count(be, mod("brass_ingot")), 10, "brass ingots");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void zincRetortDistilsZinc(TestContext ctx) {
		// 9 ZnS (999.9) -> 835.1 ZnO -> 670.9 Zn -> 6 zinc.
		MachineBlockEntity be = run(ctx, "zinc_retort", List.of("zinc_distillation", "bellows"),
				new ItemStack(mod("zinc_sulfide"), 9), ItemStack.EMPTY, charcoal(), 60_000);
		expect(ctx, count(be, mod("zinc")), 6, "zinc");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void reverberatoryWorksCobaltOre(TestContext ctx) {
		// 16 raw cobaltite: 1244 CoAsS -> 562 CoO -> 442 Co (3); 742 As2O3 -> 562 As (5); 142 Bi (1).
		MachineBlockEntity be = run(ctx, "reverberatory_furnace",
				List.of("arsenic_sublimation", "bismuth", "cobalt_smelting", "copper_smelting", "bellows"),
				new ItemStack(mod("raw_cobaltite"), 16), ItemStack.EMPTY, charcoal(), 120_000);
		expect(ctx, count(be, mod("cobalt")), 3, "cobalt");
		expect(ctx, count(be, mod("arsenic")), 5, "arsenic");
		expect(ctx, count(be, mod("bismuth")), 1, "bismuth");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void reverberatorySmeltsAntimony(TestContext ctx) {
		// 8 raw stibnite: 666.7 Sb2S3 -> 572.1 Sb2O3 -> 477.9 Sb -> 4 antimony.
		MachineBlockEntity be = run(ctx, "reverberatory_furnace", List.of("arsenic_sublimation", "antimony_smelting", "bellows"),
				new ItemStack(mod("raw_stibnite"), 8), ItemStack.EMPTY, charcoal(), 120_000);
		expect(ctx, count(be, mod("antimony")), 4, "antimony");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void whiteArsenicWithoutReduction(TestContext ctx) {
		// Cobalt research without arsenic reduction is impossible (it requires it), so check arsenopyrite roasting
		// alone gives arsenic metal: 8 raw -> 622 FeAsS -> 378 As2O3 -> 286 As -> 2 arsenic.
		MachineBlockEntity be = run(ctx, "reverberatory_furnace", List.of("arsenic_sublimation", "copper_smelting", "bellows"),
				new ItemStack(mod("raw_arsenopyrite"), 8), ItemStack.EMPTY, charcoal(), 120_000);
		expect(ctx, count(be, mod("arsenic")), 2, "arsenic");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void cupelParts24RawGold(TestContext ctx) {
		// 24 raw gold hold 133 units of silver -> 1 silver bead; the rarity-corrected gold (37 units) stays in the buffer.
		MachineBlockEntity be = run(ctx, "cupel", List.of("silver_cupellation", "copper_smelting", "bellows"),
				new ItemStack(Items.RAW_GOLD, 24), ItemStack.EMPTY, charcoal(), 60_000);
		expect(ctx, count(be, mod("silver")), 1, "silver");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void amalgamationTakesSilverWithMercury(TestContext ctx) {
		MachineBlockEntity be = run(ctx, "amalgamation_pan", List.of("patio_process"), new ItemStack(Items.RAW_GOLD, 24),
				new ItemStack(mod("mercury"), 1), new ItemStack(Items.OAK_PLANKS, 64), 60_000);
		expect(ctx, count(be, mod("silver")), 1, "silver");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void phosphorusFromBones(TestContext ctx) {
		// 13 bone meal: 1155 units of calcium phosphate -> 230.7 P -> 2 phosphorus.
		MachineBlockEntity be = run(ctx, "phosphorus_retort", List.of("phosphorus_isolation", "bellows"),
				new ItemStack(Items.BONE_MEAL, 13), ItemStack.EMPTY, charcoal(), 60_000);
		expect(ctx, count(be, mod("phosphorus")), 2, "phosphorus");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void blastFurnaceCastsIron(TestContext ctx) {
		// 9 raw iron: 700 Fe2O3 -> 510 cast iron; 150 Fe3O4 -> 113 -> 623 units -> 5 cast iron.
		MachineBlockEntity be = run(ctx, "charcoal_blast_furnace", List.of("blast_furnace", "bellows"),
				new ItemStack(Items.RAW_IRON, 9), ItemStack.EMPTY, charcoal(), 60_000);
		expect(ctx, count(be, mod("cast_iron_ingot")), 5, "cast iron");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void cementationMakesSteel(TestContext ctx) {
		// 9 iron ingots (1000 units) + 10 units of carbon -> 1010 units -> 9 steel.
		MachineBlockEntity be = run(ctx, "cementation_furnace", List.of("steel_cementation"),
				new ItemStack(Items.IRON_INGOT, 9), ItemStack.EMPTY, charcoal(), 120_000);
		expect(ctx, count(be, mod("steel_ingot")), 9, "steel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void alembicDistilsOilOfVitriol(TestContext ctx) {
		// 9 green vitriol (999.9) + water -> 322.8 sulfuric acid (2) and 525.5 iron oxide (4); the bucket comes back.
		MachineBlockEntity be = run(ctx, "alembic", List.of("alembic_distillation", "mineral_acids"),
				new ItemStack(mod("iron_sulfate"), 9), new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.OAK_PLANKS, 64),
				60_000);
		expect(ctx, count(be, mod("sulfuric_acid")), 2, "sulfuric acid");
		expect(ctx, count(be, mod("iron_oxide")), 4, "iron oxide");
		expect(ctx, count(be, Items.BUCKET), 1, "empty bucket");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void alembicMakesAquaFortisWithoutOverheating(TestContext ctx) {
		// 2 saltpetre (222.2) + 3 sulfuric acid (333.3, needs 215.5) -> 138.5 nitric acid -> 1; stays under 200 °C.
		MachineBlockEntity be = run(ctx, "alembic", List.of("alembic_distillation", "mineral_acids"),
				new ItemStack(mod("potassium_nitrate"), 2), new ItemStack(mod("sulfuric_acid"), 3),
				new ItemStack(Items.OAK_PLANKS, 64), 60_000);
		expect(ctx, count(be, mod("nitric_acid")), 1, "nitric acid");
		if (be.temperature() > 205) {
			ctx.throwGameTestException("alembic overheated: " + be.temperature());
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void crystallizerLeachesSaltpetre(TestContext ctx) {
		// 18 dirt: 90 units of ammonium nitrate with the dirt's own potash -> 113.7 saltpetre -> 1.
		MachineBlockEntity be = run(ctx, "crystallizer", List.of("saltpeter_refining"), new ItemStack(Items.DIRT, 18),
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS, 64), 60_000);
		expect(ctx, count(be, mod("potassium_nitrate")), 1, "saltpetre");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void gearHasItsStats(TestContext ctx) {
		ItemStack pick = new ItemStack(mod("bronze_pickaxe"));
		expect(ctx, pick.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0), 200, "bronze pickaxe durability");
		expect(ctx, new ItemStack(mod("steel_sword")).getOrDefault(DataComponentTypes.MAX_DAMAGE, 0), 600, "steel sword durability");
		expect(ctx, new ItemStack(mod("steel_chestplate")).getOrDefault(DataComponentTypes.MAX_DAMAGE, 0), 16 * 22,
				"steel chestplate durability");
		if (mod("hand_cannon") == Items.AIR) {
			ctx.throwGameTestException("no hand cannon");
		}
		ctx.complete();
	}
}
