package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.creative.CreativeBlockEntity;
import com.periodicelements.creative.CreativeItems;
import com.periodicelements.creative.CreativeTabs;
import com.periodicelements.creative.CreativeTools;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.multiblock.Structures;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

/** 3.51.0: the creative tabs and the nine creative testing blocks and items. */
public class CreativeTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static Item item(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void everyItemHasATab(TestContext ctx) {
		expect(ctx, CreativeTabs.unplaced().isEmpty(), "items with no creative tab: " + CreativeTabs.unplaced());
		expect(ctx, CreativeTabs.tabs().size() == 18, "18 tabs expected, got " + CreativeTabs.tabs());
		expect(ctx, CreativeTabs.tabOf(item("carbon")).equals("elements"), "carbon should be in Elements");
		expect(ctx, CreativeTabs.tabOf(item("chlorine")).equals("elements"), "chlorine (a gas) stays in Elements");
		expect(ctx, CreativeTabs.tabOf(item("sodium_chloride")).equals("solid_substances"), "salt is a solid");
		expect(ctx, CreativeTabs.tabOf(item("water")).equals("liquid_substances"), "water is a liquid at 25 C");
		expect(ctx, CreativeTabs.tabOf(item("methane")).equals("gas_substances"), "methane is a gas at 25 C");
		expect(ctx, CreativeTabs.tabOf(item("methane_cylinder")).equals("gas_substances"), "a methane cylinder goes with the gases");
		expect(ctx, CreativeTabs.tabOf(item("capybara_spawn_egg")).equals("fauna"), "spawn eggs go with the animals");
		expect(ctx, CreativeTabs.tabOf(item("progress_book")).equals("creative"), "the Book of Progress is a creative test item");
		for (Item i : CreativeItems.ITEMS) {
			expect(ctx, CreativeTabs.tabOf(i).equals("creative"), i + " should be in Creative Testing");
		}
		boolean crafted = ctx.getWorld().getServer().getRecipeManager().values().stream().anyMatch(r -> CreativeItems.ITEMS.stream()
				.anyMatch(i -> r.value().getResult(ctx.getWorld().getRegistryManager()).isOf(i)));
		expect(ctx, !crafted, "no recipe may make a creative test item");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void generatorPowersThroughWire(TestContext ctx) {
		BlockPos gen = new BlockPos(0, 1, 0);
		ctx.setBlockState(gen, CreativeItems.GENERATOR);
		ctx.setBlockState(gen.east(), Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "copper_wire")));
		ctx.setBlockState(gen.east(2), Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "copper_wire")));
		var sources = MachineBlockEntity.powerSources(ctx.getWorld(), ctx.getAbsolutePos(gen.east(3)));
		expect(ctx, sources.size() == 1, "the generator should be reached through the wire, found " + sources.size());
		PowerBlockEntity p = sources.get(0);
		expect(ctx, p.draw(5000) == 5000 && p.draw(5000) == 5000, "the generator should never run down");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 100)
	public void itemSourceFeedsAndVoidSwallows(TestContext ctx) {
		BlockPos src = new BlockPos(0, 2, 0);
		ctx.setBlockState(src, CreativeItems.ITEM_SOURCE);
		((CreativeBlockEntity) ctx.getBlockEntity(src)).setTemplate(new ItemStack(Items.COAL));
		ctx.setBlockState(src.down(), Blocks.HOPPER);
		BlockPos voidPos = new BlockPos(2, 1, 0);
		ctx.setBlockState(voidPos, CreativeItems.VOID);
		ctx.setBlockState(voidPos.up(), Blocks.HOPPER);
		HopperBlockEntity in = (HopperBlockEntity) ctx.getBlockEntity(voidPos.up());
		in.setStack(0, new ItemStack(Items.DIRT, 64));
		ctx.runAtTick(80, () -> {
			HopperBlockEntity out = (HopperBlockEntity) ctx.getBlockEntity(src.down());
			int coal = 0;
			for (int i = 0; i < out.size(); i++) {
				coal += out.getStack(i).isOf(Items.COAL) ? out.getStack(i).getCount() : 0;
			}
			expect(ctx, coal >= 5, "the hopper should have pulled coal from the source, got " + coal);
			expect(ctx, in.getStack(0).getCount() < 64, "the void should have taken dirt from the hopper above it");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void fluidSourceNeverRunsDry(TestContext ctx) {
		BlockPos at = new BlockPos(0, 1, 0);
		ctx.setBlockState(at, CreativeItems.FLUID_SOURCE);
		CreativeBlockEntity be = (CreativeBlockEntity) ctx.getBlockEntity(at);
		expect(ctx, be.fluid() == null && be.drain(100, false) == 0, "an unset source gives nothing");
		Item methane = CreativeBlockEntity.fluidOf(new ItemStack(item("methane_cylinder")));
		expect(ctx, methane == item("methane"), "a cylinder picks its gas");
		be.setFluid(methane);
		expect(ctx, be.drain(1_000_000, false) == 1_000_000 && be.units() > 1e8, "the source should never run dry");
		expect(ctx, be.fillContainer(new ItemStack(item("steel_cylinder"))).isOf(item("methane_cylinder")), "fills cylinders");
		expect(ctx, be.fillContainer(new ItemStack(item("glass_flask"))).isEmpty(), "a gas does not go in a flask");
		ctx.setBlockState(at.east(), CreativeItems.VOID);
		CreativeBlockEntity v = (CreativeBlockEntity) ctx.getBlockEntity(at.east());
		expect(ctx, v.fill(methane, 5000, Double.NaN, false) == 5000 && v.units() == 0, "the void swallows any fluid");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 60)
	public void acceleratorAgesAnimals(TestContext ctx) {
		ctx.setBlockState(new BlockPos(0, 1, 0), CreativeItems.ACCELERATOR);
		CowEntity calf = ctx.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));
		calf.setBreedingAge(-24000);
		ctx.runAtTick(40, () -> {
			int age = calf.getBreedingAge();
			// 40 ticks: 40 by itself plus 9 x 40 from the clock
			expect(ctx, age >= -24000 + 300, "the calf should age about 10x as fast, age " + age);
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void growthWandRipensCrop(TestContext ctx) {
		ctx.setBlockState(new BlockPos(0, 0, 0), Blocks.FARMLAND);
		ctx.setBlockState(new BlockPos(0, 1, 0), Blocks.WHEAT);
		CreativeTools.GrowthWand.grow(ctx.getWorld(), ctx.getAbsolutePos(new BlockPos(0, 1, 0)));
		var s = ctx.getWorld().getBlockState(ctx.getAbsolutePos(new BlockPos(0, 1, 0)));
		expect(ctx, s.isOf(Blocks.WHEAT) && ((CropBlock) Blocks.WHEAT).isMature(s), "the wheat should be ripe");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void amuletBlocksHarm(TestContext ctx) {
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		DamageSource fire = ctx.getWorld().getDamageSources().onFire();
		expect(ctx, !CreativeItems.shielded(player), "no amulet yet");
		player.getInventory().insertStack(new ItemStack(CreativeItems.AMULET));
		expect(ctx, CreativeItems.shielded(player), "the amulet anywhere in the inventory shields");
		float before = player.getHealth();
		player.damage(fire, 4);
		expect(ctx, player.getHealth() == before, "fire should not hurt a player with the amulet");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void builderBuildsValidStructure(TestContext ctx) {
		BlockPos c = ctx.getAbsolutePos(new BlockPos(1, 1, 0));
		CreativeTools.Builder.build(ctx.getWorld(), "lead_chamber", c, Direction.NORTH, null);
		var r = Structures.detect(Structures.Kind.LEAD_CHAMBER, ctx.getWorld(), c, Direction.NORTH);
		expect(ctx, r != null && r.valid(), "the built lead chamber should be a valid structure");
		ctx.complete();
	}
}
