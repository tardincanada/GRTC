package com.periodicelements.fluid;

import com.mojang.serialization.MapCodec;
import com.periodicelements.Fluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/** 3.38.0: the blocks of the multiblock tank. */
public final class TankBlocks {
	private TankBlocks() {
	}

	/** The controller a tank block reaches: itself, or the controller of the tank a valve or gauge stands in. */
	@Nullable
	static TankControllerBlockEntity controller(WorldAccess world, BlockPos pos) {
		if (world.getBlockEntity(pos) instanceof TankControllerBlockEntity c) {
			return c;
		}
		BlockPos at = TankStructure.controllerOf(world, pos);
		return at != null && world.getBlockEntity(at) instanceof TankControllerBlockEntity c ? c : null;
	}

	/** Pour a filled container in, or fill an empty one, through a controller or valve; same as the Storage Tank. */
	static ItemActionResult useWith(ItemStack stack, World world, BlockPos pos, PlayerEntity player, Hand hand) {
		boolean filled = Fluids.inFlask(stack.getItem()) || Fluids.isCylinder(stack.getItem());
		if (!filled && !Fluids.isEmptyContainer(stack)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}
		TankControllerBlockEntity tank = controller(world, pos);
		if (tank == null) {
			player.sendMessage(Text.translatable("gui.periodic_elements.big_tank.no_controller"), true);
			return ItemActionResult.SUCCESS;
		}
		if (filled) {
			if (tank.canPour(stack)) {
				give(player, hand, stack, tank.pour(stack));
			} else if (tank.fluid() != null && tank.fluid() != TankControllerBlockEntity.contentOf(stack)) {
				player.sendMessage(Text.translatable("gui.periodic_elements.tank.wrong", Text.translatable(tank.fluid().getTranslationKey())), true);
				return ItemActionResult.SUCCESS;
			} else if (tank.valid()) {
				player.sendMessage(Text.translatable("gui.periodic_elements.tank.full"), true);
				return ItemActionResult.SUCCESS;
			}
		} else {
			ItemStack out = tank.draw(stack);
			if (!out.isEmpty()) {
				stack.decrement(1);
				give(player, hand, stack, out);
			}
		}
		player.sendMessage(tank.describe(), true);
		return ItemActionResult.SUCCESS;
	}

	/** An empty hand reads the tank; sneaking with an empty hand lets a gas out. */
	static ActionResult use(World world, BlockPos pos, PlayerEntity player) {
		if (!world.isClient) {
			TankControllerBlockEntity tank = controller(world, pos);
			if (tank == null) {
				player.sendMessage(Text.translatable("gui.periodic_elements.big_tank.no_controller"), true);
			} else if (player.isSneaking()) {
				FluidContent.vent(player, tank.fluid(), tank::vent);
			} else {
				player.sendMessage(tank.describe(), true);
			}
		}
		return ActionResult.success(world.isClient);
	}

	static void give(PlayerEntity player, Hand hand, ItemStack held, ItemStack back) {
		if (back.isEmpty()) {
			return;
		}
		if (held.isEmpty()) {
			player.setStackInHand(hand, back);
		} else {
			player.getInventory().offerOrDrop(back);
		}
	}

	/**
	 * Tank Plate and Tank Compressor: shell blocks with nothing to do of their own. A Tank Gauge used on a plate of a
	 * finished tank takes its place, the tank keeping what it holds; the plate comes back. One gauge per tank.
	 */
	public static class Part extends Block {
		public Part(Settings settings) {
			super(settings);
		}

		@Override
		protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
												 Hand hand, BlockHitResult hit) {
			if (!state.isOf(FluidContent.TANK_WALL) || !stack.isOf(FluidContent.TANK_GAUGE.asItem())) {
				return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			}
			if (world.isClient) {
				return ItemActionResult.SUCCESS;
			}
			String k = "gui.periodic_elements.big_tank.";
			if (!player.isCreative() && !com.periodicelements.research.Research.allows(
					com.periodicelements.research.ResearchAccess.known(player), "item:tank_gauge")) {
				player.sendMessage(Text.translatable("message.periodic_elements.needs_research",
						com.periodicelements.research.Research.get("gas_mains").name()).formatted(net.minecraft.util.Formatting.RED), true);
				return ItemActionResult.SUCCESS;
			}
			TankControllerBlockEntity tank = controller(world, pos);
			if (tank == null || !tank.valid()) {
				player.sendMessage(Text.translatable(k + "gauge_needs_tank"), true);
				return ItemActionResult.SUCCESS;
			}
			if (!tank.structure().gauges().isEmpty()) {
				player.sendMessage(Text.translatable(k + "one_gauge"), true);
				return ItemActionResult.SUCCESS;
			}
			net.minecraft.util.math.Direction face = hit.getSide().getAxis().isHorizontal() ? hit.getSide()
					: player.getHorizontalFacing().getOpposite();
			world.setBlockState(pos, FluidContent.TANK_GAUGE.getDefaultState().with(Gauge.FACING, face), 3);
			world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_METAL_PLACE, net.minecraft.sound.SoundCategory.BLOCKS, 1f, 1f);
			if (!player.isCreative()) {
				stack.decrement(1);
				player.getInventory().offerOrDrop(new ItemStack(FluidContent.TANK_WALL));
			}
			tank.measure();
			player.sendMessage(tank.describe(), true);
			return ItemActionResult.SUCCESS;
		}
	}

	/** Tank Valve: where pipes and hoppers meet the tank, and players too. */
	public static class Valve extends Block implements InventoryProvider {
		public Valve(Settings settings) {
			super(settings);
		}

		@Override
		public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
			return controller(world, pos);
		}

		@Override
		protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
												 Hand hand, BlockHitResult hit) {
			return useWith(stack, world, pos, player, hand);
		}

		@Override
		protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
			return use(world, pos, player);
		}

		@Override
		protected boolean hasComparatorOutput(BlockState state) {
			return true;
		}

		@Override
		protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
			TankControllerBlockEntity c = controller(world, pos);
			return c == null ? 0 : c.comparatorOutput();
		}
	}

	/** Tank Controller: holds the tank's contents and measures it. Breaking it loses what the tank held. */
	public static class Controller extends BlockWithEntity {
		public static final MapCodec<Controller> CODEC = createCodec(Controller::new);

		public Controller(Settings settings) {
			super(settings);
		}

		@Override
		protected MapCodec<? extends BlockWithEntity> getCodec() {
			return CODEC;
		}

		@Override
		protected BlockRenderType getRenderType(BlockState state) {
			return BlockRenderType.MODEL;
		}

		@Nullable
		@Override
		public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
			return new TankControllerBlockEntity(pos, state);
		}

		@Nullable
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
			return world.isClient ? null : validateTicker(type, FluidContent.CONTROLLER_ENTITY, (w, p, s, be) -> be.tick());
		}

		@Override
		protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
												 Hand hand, BlockHitResult hit) {
			return useWith(stack, world, pos, player, hand);
		}

		@Override
		protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
			return use(world, pos, player);
		}

		@Override
		protected boolean hasComparatorOutput(BlockState state) {
			return true;
		}

		@Override
		protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
			return world.getBlockEntity(pos) instanceof TankControllerBlockEntity c ? c.comparatorOutput() : 0;
		}

		@Override
		protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
			ItemScatterer.onStateReplaced(state, newState, world, pos);
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}

	/**
	 * Tank Gauge: an optional dial in the shell that shows how full the tank is, in percent, on its face. It also reads a
	 * Storage Tank it is set against (the tank behind its face).
	 */
	public static class Gauge extends BlockWithEntity {
		public static final MapCodec<Gauge> CODEC = createCodec(Gauge::new);
		public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

		public Gauge(Settings settings) {
			super(settings);
			setDefaultState(getStateManager().getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH));
		}

		@Override
		protected MapCodec<? extends BlockWithEntity> getCodec() {
			return CODEC;
		}

		@Override
		protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
			builder.add(FACING);
		}

		@Override
		public BlockState getPlacementState(ItemPlacementContext ctx) {
			return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
		}

		@Override
		protected BlockState rotate(BlockState state, BlockRotation rotation) {
			return state.with(FACING, rotation.rotate(state.get(FACING)));
		}

		@Override
		protected BlockState mirror(BlockState state, BlockMirror mirror) {
			return state.rotate(mirror.getRotation(state.get(FACING)));
		}

		@Override
		protected BlockRenderType getRenderType(BlockState state) {
			return BlockRenderType.MODEL;
		}

		@Nullable
		@Override
		public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
			return new TankGaugeBlockEntity(pos, state);
		}

		@Nullable
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
			return world.isClient ? null : validateTicker(type, FluidContent.GAUGE_ENTITY, (w, p, s, be) -> be.tick());
		}

		@Override
		protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
			if (!world.isClient && world.getBlockEntity(pos) instanceof TankGaugeBlockEntity g) {
				FluidHolder h = g.holder();
				if (h instanceof TankControllerBlockEntity c) {
					player.sendMessage(c.describe(), true);
				} else if (h instanceof com.periodicelements.machine.StorageTankBlockEntity t) {
					player.sendMessage(t.describe(), true);
				} else {
					player.sendMessage(Text.translatable("gui.periodic_elements.big_tank.gauge_alone"), true);
				}
			}
			return ActionResult.success(world.isClient);
		}

		@Override
		protected boolean hasComparatorOutput(BlockState state) {
			return true;
		}

		@Override
		protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
			if (world.getBlockEntity(pos) instanceof TankGaugeBlockEntity g && g.holder() != null && g.holder().fluid() != null) {
				return Math.max(1, (int) Math.floor(14 * g.holder().fraction()) + 1);
			}
			return 0;
		}
	}
}
