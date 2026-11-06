package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

/** The new ores: what they drop, which tool they need, and that their world generation is registered. */
public class OreTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	private static Identifier mod(String id) {
		return Identifier.of(PeriodicElements.MOD_ID, id);
	}

	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static int drops(TestContext ctx, String ore, ItemStack tool, Item expected) {
		Block block = Registries.BLOCK.get(mod(ore));
		ctx.setBlockState(POS, block);
		BlockPos abs = ctx.getAbsolutePos(POS);
		BlockState state = ctx.getWorld().getBlockState(abs);
		List<ItemStack> stacks = Block.getDroppedStacks(state, ctx.getWorld(), abs, null, null, tool);
		int n = 0;
		for (ItemStack s : stacks) {
			expect(ctx, s.isOf(expected), ore + " dropped " + s);
			n += s.getCount();
		}
		return n;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void oresDropTheirMinerals(TestContext ctx) {
		Map<String, String> drops = Map.ofEntries(
				Map.entry("cobaltite_ore", "raw_cobaltite"), Map.entry("deepslate_cobaltite_ore", "raw_cobaltite"),
				Map.entry("stibnite_ore", "raw_stibnite"), Map.entry("deepslate_stibnite_ore", "raw_stibnite"),
				Map.entry("arsenopyrite_ore", "raw_arsenopyrite"), Map.entry("deepslate_arsenopyrite_ore", "raw_arsenopyrite"),
				Map.entry("cassiterite_ore", "raw_cassiterite"), Map.entry("deepslate_cassiterite_ore", "raw_cassiterite"),
				Map.entry("galena_ore", "raw_galena"), Map.entry("deepslate_galena_ore", "raw_galena"),
				Map.entry("sphalerite_ore", "raw_sphalerite"), Map.entry("deepslate_sphalerite_ore", "raw_sphalerite"),
				Map.entry("nether_sulfur_ore", "sulfur"), Map.entry("deepslate_sulfur_ore", "sulfur"),
				Map.entry("saltpetre_crust", "potassium_nitrate"),
				Map.entry("barite_ore", "raw_barite"), Map.entry("spodumene_ore", "raw_spodumene"),
				Map.entry("borax_crust", "raw_borax"), Map.entry("pitchblende_ore", "raw_pitchblende"),
				Map.entry("deepslate_pitchblende_ore", "raw_pitchblende"));
		Map<String, String> unused = Map.of(
				"cassiterite_ore", "raw_cassiterite", "deepslate_cassiterite_ore", "raw_cassiterite",
				"galena_ore", "raw_galena", "deepslate_galena_ore", "raw_galena",
				"sphalerite_ore", "raw_sphalerite", "deepslate_sphalerite_ore", "raw_sphalerite",
				"nether_sulfur_ore", "sulfur", "deepslate_sulfur_ore", "sulfur",
				"saltpetre_crust", "potassium_nitrate");
		for (var e : drops.entrySet()) {
			int n = drops(ctx, e.getKey(), new ItemStack(Items.IRON_PICKAXE), Registries.ITEM.get(mod(e.getValue())));
			expect(ctx, n >= 1 && n <= 4, e.getKey() + " dropped " + n);
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void metalOresNeedAStonePickaxe(TestContext ctx) {
		BlockState tin = Registries.BLOCK.get(mod("cassiterite_ore")).getDefaultState();
		expect(ctx, !new ItemStack(Items.WOODEN_PICKAXE).isSuitableFor(tin), "wooden pickaxe can mine cassiterite");
		expect(ctx, new ItemStack(Items.STONE_PICKAXE).isSuitableFor(tin), "stone pickaxe can't mine cassiterite");
		BlockState sulfur = Registries.BLOCK.get(mod("nether_sulfur_ore")).getDefaultState();
		expect(ctx, new ItemStack(Items.WOODEN_PICKAXE).isSuitableFor(sulfur), "wooden pickaxe can't mine sulfur");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void worldGenerationIsRegistered(TestContext ctx) {
		var placed = ctx.getWorld().getRegistryManager().get(RegistryKeys.PLACED_FEATURE);
		for (String f : List.of("ore_cassiterite", "ore_cassiterite_mountains", "ore_galena", "ore_sphalerite",
				"ore_sulfur_nether", "ore_sulfur_deltas", "ore_sulfur_deep", "ore_saltpetre_desert", "ore_saltpetre_caves",
				"ore_cobaltite", "ore_cobaltite_mountains", "ore_stibnite", "ore_stibnite_mountains", "ore_arsenopyrite")) {
			expect(ctx, placed.containsId(mod(f)), "missing placed feature " + f);
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void ingotsAndElementsConvert(TestContext ctx) {
		var recipes = ctx.getWorld().getRecipeManager();
		for (String id : List.of("copper_from_ingot", "copper_ingot_from_copper", "iron_from_ingot",
				"iron_ingot_from_iron", "gold_from_ingot", "gold_ingot_from_gold")) {
			expect(ctx, recipes.get(mod(id)).isPresent(), "missing recipe " + id);
		}
		ctx.complete();
	}
}
