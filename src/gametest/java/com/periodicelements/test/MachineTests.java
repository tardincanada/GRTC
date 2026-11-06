package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlock;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
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

/**
 * Runs each machine on real inputs by calling its tick directly (so minutes of game time run in one test tick),
 * and checks yields against the stoichiometry in data/reactions.csv.
 */
public class MachineTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private record Run(MachineBlockEntity be, int ticks, int fuelUsed, double peak) {
	}

	/** Places the machine, gives its owner the projects, loads it, and ticks until idle or the limit. */
	private static Run run(TestContext ctx, String machine, List<String> projects, ItemStack input, ItemStack fuel, int limit) {
		ctx.setBlockState(POS, block(machine));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(mod("glass_flask"), 32));
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, projects);
		be.setStack(MachineBlockEntity.INPUT, input);
		be.setStack(MachineBlockEntity.FUEL, fuel);
		int fuelStart = fuel.getCount();
		BlockPos abs = ctx.getAbsolutePos(POS);
		double peak = 0;
		int t = 0;
		for (; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			peak = Math.max(peak, be.temperature());
			if (be.getStack(MachineBlockEntity.RESIDUE).getCount() >= 60) {
				be.setStack(MachineBlockEntity.RESIDUE, ItemStack.EMPTY); // a hopper would take the slag away
			}
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		int used = fuelStart - be.getStack(MachineBlockEntity.FUEL).getCount();
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks ({} s), peak {} °C, fuel used {}, status {}, outputs {} + {} {} {} | residue {} | buffers {}",
				machine, t, t / 20, String.format(Locale.ROOT, "%.0f", peak), used, be.status(),
				be.getStack(2), be.getStack(3), be.getStack(4), "", be.getStack(5), be.outputBuffers());
		return new Run(be, t, used, peak);
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

	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void limeKilnBurnsCalcite(TestContext ctx) {
		Run r = run(ctx, "lime_kiln", List.of("fired_pottery", "lime_burning"),
				new ItemStack(Items.CALCITE, 2), new ItemStack(Items.CHARCOAL, 32), 40_000);
		// 2 blocks of CaCO3 = 2000 units -> 1120.6 units CaO -> 10 items (111.1 each) with 9.5 units left over.
		int lime = count(r.be(), mod("calcium_oxide"));
		expect(ctx, lime == 10, "expected 10 quicklime, got " + lime);
		expect(ctx, r.peak() >= 900, "kiln never reached 900 °C");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void limeKilnNeedsResearch(TestContext ctx) {
		Run r = run(ctx, "lime_kiln", List.of(), new ItemStack(Items.CALCITE, 1), new ItemStack(Items.CHARCOAL, 8), 3_000);
		expect(ctx, count(r.be(), mod("calcium_oxide")) == 0, "made quicklime without Lime Burning");
		expect(ctx, r.be().status() == MachineBlockEntity.Status.NEEDS_RESEARCH, "status " + r.be().status());
		expect(ctx, r.fuelUsed() == 0, "burned fuel with nothing to do");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void charcoalPitCharsLogs(TestContext ctx) {
		Run r = run(ctx, "charcoal_pit", List.of(), new ItemStack(Items.OAK_LOG, 4), new ItemStack(Items.OAK_LOG, 8), 40_000);
		// Each log: 620 cellulose * 22% + 240 lignin * 40% = 232.4 units of charcoal -> 4 logs = 929.6 = 8 charcoal.
		int charcoal = count(r.be(), Items.CHARCOAL);
		expect(ctx, charcoal == 8, "expected 8 charcoal, got " + charcoal);
		expect(ctx, r.peak() >= 400 && r.peak() <= 700, "pit temperature out of range: " + r.peak());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void charcoalPitShowsActiveOnlyWhileCharring(TestContext ctx) {
		// 3.28.0: the "active" block state switches the pit to its animated model while a reaction runs.
		ctx.setBlockState(POS, block("charcoal_pit"));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		be.setOwner(UUID.randomUUID());
		BlockPos abs = ctx.getAbsolutePos(POS);
		for (int t = 0; t < 40; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		expect(ctx, !ctx.getWorld().getBlockState(abs).get(MachineBlock.ACTIVE), "empty pit shows active");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.OAK_LOG, 2));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.OAK_LOG, 8));
		int activeTicks = 0, t = 0;
		for (; t < 40_000; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (ctx.getWorld().getBlockState(abs).get(MachineBlock.ACTIVE)) {
				activeTicks++;
			}
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		expect(ctx, activeTicks > 100, "pit was active for only " + activeTicks + " ticks while charring");
		expect(ctx, count(be, Items.CHARCOAL) > 0, "pit made no charcoal");
		for (int i = 0; i < 30; i++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		expect(ctx, !ctx.getWorld().getBlockState(abs).get(MachineBlock.ACTIVE), "pit still active after the charge ran out");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bloomeryNeedsBellows(TestContext ctx) {
		// Iron Smelting without Bellows: charcoal alone tops out at 1100 °C, below the 1150 °C reduction.
		Run r = run(ctx, "bloomery", List.of("iron_smelting"), new ItemStack(Items.RAW_IRON, 9),
				new ItemStack(Items.CHARCOAL, 64), 12_000);
		expect(ctx, count(r.be(), Items.IRON_INGOT) == 0, "reduced iron without bellows");
		expect(ctx, r.peak() <= 1100.5, "bloomery passed the charcoal flame limit: " + r.peak());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bloomeryReducesIron(TestContext ctx) {
		Run r = run(ctx, "bloomery", List.of("iron_smelting", "bellows"), new ItemStack(Items.RAW_IRON, 9),
				new ItemStack(Items.CHARCOAL, 64), 60_000);
		// Raw iron: 77.77 Fe2O3 * 0.6994 + 16.665 Fe3O4 * 0.7236 = 66.45 units Fe each; 9 -> 598 -> 5 iron.
		int iron = count(r.be(), Items.IRON_INGOT);
		expect(ctx, iron == 5, "expected 5 iron ingots, got " + iron);
		expect(ctx, count(r.be(), mod("slag")) == 1, "expected 1 slag (150 units of silica)");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void smeltingFurnaceMeltsGold(TestContext ctx) {
		Run r = run(ctx, "smelting_furnace", List.of("copper_smelting"), new ItemStack(Items.RAW_GOLD_BLOCK, 64),
				new ItemStack(Items.CHARCOAL, 64), 120_000);
		// A raw gold block: 500 units gold * 0.028 rarity = 13.9 units; 64 blocks -> 888 units -> 7 gold ingots.
		int gold = count(r.be(), Items.GOLD_INGOT);
		expect(ctx, gold == 7, "expected 7 gold ingots, got " + gold);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void vanillaFurnaceNoLongerSmeltsOre(TestContext ctx) {
		var recipes = ctx.getWorld().getRecipeManager();
		for (String id : List.of("iron_ingot_from_smelting_raw_iron", "copper_ingot_from_blasting_raw_copper",
				"gold_ingot_from_smelting_gold_ore")) {
			expect(ctx, recipes.get(Identifier.ofVanilla(id)).isEmpty(), id + " is still loaded");
		}
		expect(ctx, recipes.get(Identifier.ofVanilla("glass")).isPresent(), "glass smelting should still work");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void smeltingFurnaceMakesCopper(TestContext ctx) {
		Run r = run(ctx, "smelting_furnace", List.of("copper_smelting"), new ItemStack(Items.RAW_COPPER, 32),
				new ItemStack(Items.CHARCOAL, 64), 60_000);
		// Raw copper holds 77.77 units of chalcopyrite (copper has no rarity correction): 77.77 * 0.4333 * 0.7989
		// = 26.9 units Cu each; 32 -> 861 units -> 7 copper.
		int copper = count(r.be(), Items.COPPER_INGOT);
		expect(ctx, copper == 7, "expected 7 copper ingots, got " + copper);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void smeltingFurnaceMakesTin(TestContext ctx) {
		Run r = run(ctx, "smelting_furnace", List.of("copper_smelting"), new ItemStack(mod("raw_cassiterite"), 16),
				new ItemStack(Items.CHARCOAL, 64), 120_000);
		// Raw cassiterite: 77.77 units SnO2 (tin has no rarity correction) * 0.7877 = 61.3 Sn each; 16 -> 980 -> 8 tin.
		int tin = count(r.be(), mod("tin"));
		expect(ctx, tin == 8, "expected 8 tin, got " + tin);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void smeltingFurnaceMakesLead(TestContext ctx) {
		Run r = run(ctx, "smelting_furnace", List.of("copper_smelting", "lead_smelting"),
				new ItemStack(mod("raw_galena"), 64), new ItemStack(Items.CHARCOAL, 64), 120_000);
		// Raw galena: 83.3 units PbS (no rarity correction since 1.4.1, like tin), about 1.6 raw galena per lead; 64 -> 41.
		int lead = count(r.be(), mod("lead"));
		expect(ctx, lead == 41, "expected 41 lead, got " + lead);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void smeltingFurnaceRoastsCinnabar(TestContext ctx) {
		Run r = run(ctx, "smelting_furnace", List.of("copper_smelting", "mercury_roasting"),
				new ItemStack(Items.REDSTONE, 64), new ItemStack(Items.CHARCOAL, 32), 60_000);
		// Redstone as cinnabar: 10% HgS (11.1 units) * 0.8622 = 9.58 Hg each, about 12 redstone per mercury;
		// 64 -> 613 -> 5 mercury.
		int mercury = count(r.be(), mod("mercury"));
		expect(ctx, mercury == 5, "expected 5 mercury, got " + mercury);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void limeKilnMakesGreenVitriolOnWood(TestContext ctx) {
		Run r = run(ctx, "lime_kiln", List.of("fired_pottery", "lime_burning", "copper_smelting", "green_vitriol"),
				new ItemStack(Items.COBBLED_DEEPSLATE, 16), new ItemStack(Items.OAK_LOG, 32), 60_000);
		// Deepslate holds 20 units of pyrite: 20 * 1.266 = 25.3 FeSO4 each; 16 -> 405 -> 3 green vitriol.
		int vitriol = count(r.be(), mod("iron_sulfate"));
		expect(ctx, vitriol == 3, "expected 3 iron sulfate, got " + vitriol);
		expect(ctx, r.peak() <= 650.5, "a wood fire should keep the kiln under 650 °C: " + r.peak());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void hotLimeKilnBurnsPyriteAndKeepsGoing(TestContext ctx) {
		Run r = run(ctx, "lime_kiln", List.of("fired_pottery", "lime_burning", "copper_smelting", "green_vitriol"),
				new ItemStack(Items.COBBLED_DEEPSLATE, 4), new ItemStack(Items.CHARCOAL, 64), 60_000);
		// On charcoal the kiln passes 650 °C, so pyrite roasts to iron oxide (slag) instead; the kiln must not get stuck.
		expect(ctx, r.be().getStack(MachineBlockEntity.INPUT).isEmpty(), "kiln stopped with input left");
		expect(ctx, r.be().status() == MachineBlockEntity.Status.IDLE, "status " + r.be().status());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void goldBlockMeltsWithoutLoss(TestContext ctx) {
		Run r = run(ctx, "smelting_furnace", List.of("copper_smelting"), new ItemStack(Items.GOLD_BLOCK, 1),
				new ItemStack(Items.CHARCOAL, 32), 60_000);
		// Refined gold is not rarity-corrected again: 1000 units -> 9 ingots.
		int gold = count(r.be(), Items.GOLD_INGOT);
		expect(ctx, gold == 9, "expected 9 gold ingots, got " + gold);
		ctx.complete();
	}
}
