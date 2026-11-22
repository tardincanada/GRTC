package com.periodicelements.space;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A Propellant Tank. Stack tanks on an engine, then fill them by using the engine's fuel and oxidizer on them (sneak to
 * pour the whole stack). Use it with an empty hand to read what it holds. Broken, it gives its propellant back.
 */
public class PropellantTankBlock extends BlockWithEntity {
	public static final MapCodec<PropellantTankBlock> CODEC = createCodec(PropellantTankBlock::new);

	public PropellantTankBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PropellantTankBlockEntity(pos, state);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	/** The engine at the foot of the column of tanks this tank stands in, or null. */
	public static RocketEngineBlock engineBelow(World world, BlockPos pos) {
		BlockPos p = pos.down();
		for (int i = 0; i < 64; i++, p = p.down()) {
			BlockState s = world.getBlockState(p);
			if (s.getBlock() instanceof RocketEngineBlock e) {
				return e;
			}
			if (!(s.getBlock() instanceof PropellantTankBlock)) {
				return null;
			}
		}
		return null;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof PropellantTankBlockEntity tank)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		Item item = stack.getItem();
		boolean anyPropellant = false;
		for (Propellant p : Propellant.values()) {
			anyPropellant |= item == p.fuelItem() || item == p.oxidizerItem();
		}
		if (!anyPropellant) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}
		String ns = "message.periodic_elements.tank.";
		RocketEngineBlock engine = engineBelow(world, pos);
		if (engine == null) {
			player.sendMessage(Text.translatable(ns + "no_engine").formatted(Formatting.YELLOW), true);
			return ItemActionResult.SUCCESS;
		}
		Propellant p = engine.propellant;
		if (!tank.empty() && tank.propellant != null && tank.propellant != p) {
			player.sendMessage(Text.translatable(ns + "mixed").formatted(Formatting.RED), true);
			return ItemActionResult.SUCCESS;
		}
		boolean fuel = item == p.fuelItem();
		if (!fuel && item != p.oxidizerItem()) {
			player.sendMessage(Text.translatable(ns + "wrong", p.fuelItem().getName(), p.oxidizerItem().getName())
					.formatted(Formatting.RED), true);
			return ItemActionResult.SUCCESS;
		}
		int room = fuel ? p.fuelCapacity - tank.fuel : p.oxidizerCapacity - tank.oxidizer;
		int n = Math.min(room, player.isSneaking() ? stack.getCount() : 1);
		if (n <= 0) {
			player.sendMessage(Text.translatable(ns + "full").formatted(Formatting.YELLOW), true);
			return ItemActionResult.SUCCESS;
		}
		tank.propellant = p;
		if (fuel) {
			tank.fuel += n;
		} else {
			tank.oxidizer += n;
		}
		tank.markDirty();
		if (!player.isCreative()) {
			stack.decrement(n);
		}
		world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.6f, 0.8f);
		show(player, tank, p);
		return ItemActionResult.SUCCESS;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof PropellantTankBlockEntity tank) {
			RocketEngineBlock engine = engineBelow(world, pos);
			Propellant p = tank.propellant != null ? tank.propellant : engine != null ? engine.propellant : null;
			if (p == null || tank.empty() && engine == null) {
				player.sendMessage(Text.translatable("message.periodic_elements.tank.empty"), true);
			} else {
				show(player, tank, p);
			}
		}
		return ActionResult.SUCCESS;
	}

	static void show(PlayerEntity player, PropellantTankBlockEntity tank, Propellant p) {
		player.sendMessage(Text.translatable("message.periodic_elements.tank.contents", p.fuelItem().getName(), tank.fuel,
				p.fuelCapacity, p.oxidizerItem().getName(), tank.oxidizer, p.oxidizerCapacity), true);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && !Space.lifting
				&& world.getBlockEntity(pos) instanceof PropellantTankBlockEntity tank && tank.propellant != null) {
			ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(tank.propellant.fuelItem(), tank.fuel));
			ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(tank.propellant.oxidizerItem(), tank.oxidizer));
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
