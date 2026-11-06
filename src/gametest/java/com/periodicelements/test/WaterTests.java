package com.periodicelements.test;

import com.periodicelements.ModContent;
import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.research.ResearchState;
import com.periodicelements.water.Water;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.UUID;

/** 3.45.0: salt water and fresh water, brine, the Seawater Still. */
public class WaterTests implements FabricGameTest {
	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	/** A stone trough three blocks long at y 2 (x 1..3, z 1), closed on every side. */
	private static void trough(TestContext ctx) {
		for (int x = 0; x <= 4; x++) {
			for (int z = 0; z <= 2; z++) {
				ctx.setBlockState(new BlockPos(x, 1, z), Blocks.STONE);
				if (x == 0 || x == 4 || z != 1) ctx.setBlockState(new BlockPos(x, 2, z), Blocks.STONE);
			}
		}
	}

	private static void expectFluid(TestContext ctx, BlockPos pos, Fluid fluid, boolean still, String what) {
		FluidState f = ctx.getWorld().getFluidState(ctx.getAbsolutePos(pos));
		if (f.getFluid() != fluid || f.isStill() != still) {
			ctx.throwGameTestException(what + ": expected " + Registries.FLUID.getId(fluid) + (still ? " source" : " flow")
					+ ", got " + Registries.FLUID.getId(f.getFluid()) + (f.isStill() ? " source" : " flow"));
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void freshAndSaltMakeSaltWater(TestContext ctx) {
		trough(ctx);
		ctx.setBlockState(new BlockPos(1, 2, 1), Blocks.WATER);
		ctx.setBlockState(new BlockPos(3, 2, 1), Water.SALT_WATER_BLOCK);
		ctx.waitAndRun(60, () -> {
			expectFluid(ctx, new BlockPos(2, 2, 1), Water.SALT_WATER, true, "the new source between fresh and salt");
			expectFluid(ctx, new BlockPos(1, 2, 1), Water.SALT_WATER, true, "the fresh source that took part");
			expectFluid(ctx, new BlockPos(3, 2, 1), Water.SALT_WATER, true, "the salt source");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void twoFreshSourcesMakeFreshWater(TestContext ctx) {
		trough(ctx);
		ctx.setBlockState(new BlockPos(1, 2, 1), Blocks.WATER);
		ctx.setBlockState(new BlockPos(3, 2, 1), Blocks.WATER);
		ctx.waitAndRun(60, () -> {
			expectFluid(ctx, new BlockPos(2, 2, 1), Fluids.WATER, true, "the new source between two fresh ones");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void twoSaltSourcesMakeSaltWater(TestContext ctx) {
		trough(ctx);
		ctx.setBlockState(new BlockPos(1, 2, 1), Water.SALT_WATER_BLOCK);
		ctx.setBlockState(new BlockPos(3, 2, 1), Water.SALT_WATER_BLOCK);
		ctx.waitAndRun(60, () -> {
			expectFluid(ctx, new BlockPos(2, 2, 1), Water.SALT_WATER, true, "the new source between two salt ones");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void brineMakesNoNewSource(TestContext ctx) {
		trough(ctx);
		ctx.setBlockState(new BlockPos(1, 2, 1), Water.BRINE_BLOCK);
		ctx.setBlockState(new BlockPos(3, 2, 1), Water.BRINE_BLOCK);
		ctx.waitAndRun(60, () -> {
			expectFluid(ctx, new BlockPos(2, 2, 1), Water.FLOWING_BRINE, false, "between two brine sources");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 100)
	public void freshWaterPouredIntoTheSeaTurnsSalt(TestContext ctx) {
		for (int x = 0; x <= 2; x++) {
			for (int z = 0; z <= 2; z++) {
				ctx.setBlockState(new BlockPos(x, 1, z), Blocks.STONE);
				ctx.setBlockState(new BlockPos(x, 2, z), Water.SALT_WATER_BLOCK);
			}
		}
		ctx.setBlockState(new BlockPos(1, 2, 1), Blocks.WATER);
		ctx.waitAndRun(5, () -> {
			expectFluid(ctx, new BlockPos(1, 2, 1), Water.SALT_WATER, true, "fresh water in the middle of the sea");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bucketsTellTheWatersApart(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		ctx.setBlockState(new BlockPos(1, 1, 1), Blocks.STONE);
		ctx.setBlockState(pos, Water.SALT_WATER_BLOCK);
		BlockPos abs = ctx.getAbsolutePos(pos);
		BlockState state = ctx.getWorld().getBlockState(abs);
		ItemStack got = ((FluidBlock) Water.SALT_WATER_BLOCK).tryDrainFluid(null, ctx.getWorld(), abs, state);
		if (!got.isOf(ModContent.SEAWATER_BUCKET)) ctx.throwGameTestException("salt water fills a " + got);
		ctx.setBlockState(pos, Blocks.WATER);
		got = ((FluidBlock) Blocks.WATER).tryDrainFluid(null, ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		if (!got.isOf(Items.WATER_BUCKET)) ctx.throwGameTestException("fresh water fills a " + got);
		ctx.setBlockState(pos, Water.BRINE_BLOCK);
		got = ((FluidBlock) Water.BRINE_BLOCK).tryDrainFluid(null, ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		if (!got.isOf(mod("brine_bucket"))) ctx.throwGameTestException("brine fills a " + got);
		if (!Water.SALT_WATER.getDefaultState().isIn(FluidTags.WATER) || !Water.BRINE.getDefaultState().isIn(FluidTags.WATER)) {
			ctx.throwGameTestException("salt water and brine must behave as water (fluid tag minecraft:water)");
		}
		if (Water.SALT_WATER.getBucketItem() != ModContent.SEAWATER_BUCKET) ctx.throwGameTestException("seawater bucket");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void brineSpringsAreRare(TestContext ctx) {
		int springs = 0;
		for (int x = -100; x < 100; x++) {
			for (int z = -100; z < 100; z++) {
				if (Water.hasBrineSpring(12345L, new ChunkPos(x, z))) springs++;
			}
		}
		// 40,000 chunks, one in 500: about 80 (only those on dry, flat land get a spring)
		if (springs < 50 || springs > 115) ctx.throwGameTestException("brine spring chunks: " + springs);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void seawaterStillMakesFreshWater(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "desalination_still")));
		MachineBlockEntity be = ctx.getBlockEntity(pos);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(mod("glass_flask"), 64));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner,
				List.of("salt_evaporation", "alembic_distillation", "desalination"));
		BlockPos abs = ctx.getAbsolutePos(pos);
		int buckets = 0;
		for (int t = 0; t < 60_000; t++) {
			if (be.getStack(MachineBlockEntity.INPUT).isEmpty() && buckets < 5) {
				be.setStack(MachineBlockEntity.INPUT, new ItemStack(ModContent.SEAWATER_BUCKET));
				buckets++;
			}
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (buckets == 5 && t > 200 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		int water = 0, salt = 0, empty = 0;
		for (int i = MachineBlockEntity.PRODUCT_FIRST; i <= MachineBlockEntity.RESIDUE; i++) {
			ItemStack s = be.getStack(i);
			if (s.isOf(mod("water"))) water += s.getCount();
			if (s.isOf(mod("sodium_chloride"))) salt += s.getCount();
			if (s.isOf(Items.BUCKET)) empty += s.getCount();
		}
		PeriodicElements.LOGGER.info("[gametest] desalination_still: water {} salt {} buckets {} buffers {}", water, salt, empty, be.outputBuffers());
		// 5 buckets: 4825 units of water -> 43 flasks; 136 units of salt -> 1 salt
		if (water != 43) ctx.throwGameTestException("expected 43 flasks of fresh water, got " + water);
		if (salt != 1) ctx.throwGameTestException("expected 1 salt, got " + salt);
		if (empty != 5) ctx.throwGameTestException("expected 5 empty buckets, got " + empty);
		ctx.complete();
	}
}
