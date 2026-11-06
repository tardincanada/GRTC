package com.periodicelements.test;

import com.periodicelements.ElementItem;
import com.periodicelements.SubstanceItem;
import com.periodicelements.samplecase.SampleCase;
import com.periodicelements.samplecase.SampleCaseScreenHandler;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/** The Sample Case (3.31.0): holds only elements and substances, picks them up while switched on. */
public class SampleCaseTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static Item element() {
		return Registries.ITEM.stream().filter(i -> i instanceof ElementItem).findFirst().orElseThrow();
	}

	private static Item substance() {
		return Registries.ITEM.stream().filter(i -> i instanceof SubstanceItem).findFirst().orElseThrow();
	}

	private static int count(ItemStack caseStack, Item item) {
		return SampleCase.contents(caseStack).stream().filter(s -> s.isOf(item)).mapToInt(ItemStack::getCount).sum();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "sample_case")
	public void takesOnlySamples(TestContext ctx) {
		expect(ctx, SampleCase.accepts(new ItemStack(element())), "an element should go in the case");
		expect(ctx, SampleCase.accepts(new ItemStack(substance())), "a substance should go in the case");
		expect(ctx, !SampleCase.accepts(new ItemStack(Items.COBBLESTONE)), "cobblestone should not go in the case");
		expect(ctx, !SampleCase.accepts(new ItemStack(SampleCase.CASE)), "a case should not go in a case");
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.getInventory().clear();
		ItemStack caseStack = new ItemStack(SampleCase.CASE);
		player.getInventory().setStack(3, caseStack);
		ItemStack stone = new ItemStack(Items.COBBLESTONE, 10);
		expect(ctx, SampleCase.absorb(player, stone) == 0 && stone.getCount() == 10, "the case took cobblestone");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "sample_case")
	public void absorbsAndStacks(TestContext ctx) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.getInventory().clear();
		ItemStack caseStack = new ItemStack(SampleCase.CASE);
		player.getInventory().setStack(5, caseStack);
		Item el = element();
		ItemStack a = new ItemStack(el, 40);
		expect(ctx, SampleCase.absorb(player, a) == 40 && a.isEmpty(), "40 samples should all go in");
		ItemStack b = new ItemStack(el, 40);
		SampleCase.absorb(player, b);
		ItemStack inCase = player.getInventory().getStack(5);
		expect(ctx, count(inCase, el) == 80, "case should hold 80, holds " + count(inCase, el));
		long used = SampleCase.contents(inCase).stream().filter(s -> !s.isEmpty()).count();
		expect(ctx, used == 2, "80 of a 64-stack item should use 2 places, used " + used);
		// A full case leaves the rest for the pack.
		ItemStack many = new ItemStack(substance(), 64);
		for (int i = 0; i < SampleCase.SLOTS; i++) {
			many.setCount(64);
			SampleCase.absorb(player, many);
		}
		expect(ctx, !many.isEmpty(), "a full case should refuse more");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "sample_case")
	public void pickupSwitchAndWalkingOver(TestContext ctx) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.getInventory().clear();
		ItemStack caseStack = new ItemStack(SampleCase.CASE);
		caseStack.set(SampleCase.PICKUP, false);
		player.getInventory().setStack(0, caseStack);
		Item el = element();
		ItemStack s = new ItemStack(el, 5);
		expect(ctx, SampleCase.absorb(player, s) == 0, "a switched-off case should not pick up");
		player.getInventory().getStack(0).set(SampleCase.PICKUP, true);
		// Walking over an element item puts it in the case, not the pack.
		ItemEntity entity = new ItemEntity(ctx.getWorld(), player.getX(), player.getY(), player.getZ(), new ItemStack(el, 7));
		entity.setPickupDelay(0);
		ctx.getWorld().spawnEntity(entity);
		entity.onPlayerCollision(player);
		expect(ctx, count(player.getInventory().getStack(0), el) == 7, "walking over 7 samples should fill the case");
		expect(ctx, entity.isRemoved(), "the picked-up item should be gone");
		for (int i = 1; i < player.getInventory().size(); i++) {
			expect(ctx, !player.getInventory().getStack(i).isOf(el), "samples landed in the pack, slot " + i);
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "sample_case")
	public void screenSavesToTheCase(TestContext ctx) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.getInventory().clear();
		player.getInventory().setStack(2, new ItemStack(SampleCase.CASE));
		SampleCaseScreenHandler handler = new SampleCaseScreenHandler(1, player.getInventory(), 2);
		expect(ctx, !handler.getSlot(0).canInsert(new ItemStack(Items.DIRT)), "case place should refuse dirt");
		handler.getSlot(0).setStack(new ItemStack(element(), 12));
		handler.onClosed(player);
		expect(ctx, count(player.getInventory().getStack(2), element()) == 12, "the case should keep what was put in");
		ctx.complete();
	}
}
