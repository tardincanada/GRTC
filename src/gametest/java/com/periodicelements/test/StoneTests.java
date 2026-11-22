package com.periodicelements.test;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * 3.68.2: in the Geological world a craton is granite, diorite and gneiss and a volcanic plain is basalt and tuff, with no
 * plain stone near the surface. Any of these rough stones crafts into cobblestone, which a furnace turns into stone.
 */
public class StoneTests implements FabricGameTest {
	private static final String[] ROUGH = {"minecraft:granite", "minecraft:diorite", "minecraft:andesite", "minecraft:tuff",
			"minecraft:basalt", "minecraft:smooth_basalt", "minecraft:calcite", "periodic_elements:cobbled_shale",
			"periodic_elements:cobbled_gneiss", "periodic_elements:cobbled_peridotite", "periodic_elements:cobbled_kimberlite",
			"periodic_elements:cobbled_laterite"};

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void roughStoneMakesCobblestone(TestContext ctx) {
		var world = ctx.getWorld();
		var rm = world.getRecipeManager();
		for (String id : ROUGH) {
			Item item = Registries.ITEM.get(Identifier.of(id));
			if (item == Items.AIR) {
				ctx.throwGameTestException("no item " + id);
			}
			CraftingRecipeInput in = CraftingRecipeInput.create(1, 1, List.of(new ItemStack(item)));
			ItemStack out = rm.getFirstMatch(RecipeType.CRAFTING, in, world)
					.map(h -> h.value().craft(in, world.getRegistryManager())).orElse(ItemStack.EMPTY);
			if (!out.isOf(Items.COBBLESTONE) || out.getCount() != 1) {
				ctx.throwGameTestException(id + " crafts into " + out + ", not 1 cobblestone");
			}
		}
		ItemStack stone = rm.getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(new ItemStack(Items.COBBLESTONE)), world)
				.map(h -> h.value().getResult(world.getRegistryManager())).orElse(ItemStack.EMPTY);
		if (!stone.isOf(Items.STONE)) {
			ctx.throwGameTestException("cobblestone smelts into " + stone);
		}
		ctx.complete();
	}
}
