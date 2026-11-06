package com.periodicelements.test;

import com.periodicelements.BlockComposition;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.ArrayList;
import java.util.List;

/** Crafted vanilla blocks (3.34.0): compositions from recipes, and no recipe makes units from nothing. */
public class CraftedCompositionTests implements FabricGameTest {
	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static double amount(Item item) {
		var e = BlockComposition.of(Registries.ITEM.getId(item).toString());
		return e == null ? Double.NaN : e.amount();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "crafted_compositions")
	public void bricksHaveTheirBricks(TestContext ctx) {
		for (String id : List.of("minecraft:bricks", "minecraft:brick_slab", "minecraft:brick_stairs", "minecraft:brick_wall",
				"minecraft:oak_planks", "minecraft:stone_bricks", "minecraft:glass_pane", "minecraft:chest")) {
			check(ctx, BlockComposition.of(id) != null, id + " has no composition");
		}
		var brick = BlockComposition.of("minecraft:brick");
		var bricks = BlockComposition.of("minecraft:bricks");
		check(ctx, Math.abs(bricks.amount() - 4 * brick.amount()) < 0.01, "bricks should be 4 bricks, is " + bricks.amount());
		check(ctx, Math.abs(BlockComposition.of("minecraft:brick_slab").amount() - bricks.amount() / 2) < 0.01, "a brick slab is half a block");
		check(ctx, bricks.parts().get(0).id().equals(brick.parts().get(0).id()), "bricks are made of what a brick is made of");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "crafted_compositions")
	public void noRecipeMakesUnitsFromNothing(TestContext ctx) {
		var world = ctx.getWorld();
		List<String> bad = new ArrayList<>();
		for (var entry : world.getRecipeManager().values()) {
			var recipe = entry.value();
			var type = recipe.getType();
			if (type != RecipeType.CRAFTING && type != RecipeType.STONECUTTING) {
				continue;
			}
			ItemStack out = recipe.getResult(world.getRegistryManager());
			if (out.isEmpty() || Double.isNaN(amount(out.getItem()))) {
				continue;
			}
			double in = 0;
			boolean known = true;
			for (Ingredient ing : recipe.getIngredients()) {
				if (ing.isEmpty()) {
					continue;
				}
				double cheapest = Double.NaN;
				for (ItemStack s : ing.getMatchingStacks()) {
					double a = amount(s.getItem());
					if (!Double.isNaN(a) && !(a >= cheapest)) {
						cheapest = a;
					}
				}
				if (Double.isNaN(cheapest)) {
					known = false;
					break;
				}
				in += cheapest;
			}
			double made = amount(out.getItem()) * out.getCount();
			if (known && made > in * 1.001 + 0.01) {
				bad.add(entry.id() + String.format(" (%.1f from %.1f)", made, in));
			}
		}
		check(ctx, bad.isEmpty(), bad.size() + " recipes make units: " + String.join(", ", bad.subList(0, Math.min(12, bad.size()))));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "crafted_compositions")
	public void everyVanillaBlockIsCovered(TestContext ctx) throws java.io.IOException {
		java.util.Set<String> none;
		try (var in = CraftedCompositionTests.class.getResourceAsStream("/periodic_elements/no_composition.json")) {
			check(ctx, in != null, "no_composition.json is missing");
			none = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
					.getAsJsonObject().keySet();
		}
		List<String> missing = new ArrayList<>();
		for (var id : Registries.BLOCK.getIds()) {
			String key = id.toString();
			if (!id.getNamespace().equals("minecraft")) {
				continue;
			}
			boolean has = BlockComposition.of(key) != null;
			check(ctx, !(has && none.contains(key)), key + " has a composition but is listed as having none");
			if (!has && !none.contains(key)) {
				missing.add(key);
			}
		}
		check(ctx, missing.isEmpty(), missing.size() + " vanilla blocks have no composition: "
				+ String.join(", ", missing.subList(0, Math.min(15, missing.size()))));
		ctx.complete();
	}
}
