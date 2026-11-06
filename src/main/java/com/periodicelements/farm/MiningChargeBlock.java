package com.periodicelements.farm;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

/**
 * Mining Charge (1.8.0): four sticks of dynamite. Lit with flint and steel or a redstone signal, it goes off after
 * {@link #FUSE} ticks and breaks every block in a 5x5x5 cube round it that is weaker than obsidian, dropping all of
 * them (no explosion loss). With mobGriefing off it breaks nothing but still hurts what stands close.
 */
public class MiningChargeBlock extends Block {
	public static final MapCodec<MiningChargeBlock> CODEC = createCodec(MiningChargeBlock::new);
	public static final BooleanProperty LIT = Properties.LIT;
	public static final int FUSE = 80;
	public static final int RADIUS = 2;
	/** Obsidian's blast resistance: anything this strong or stronger stops the charge. */
	public static final float STOPS_AT = 1200f;

	/** Half the cube's side, less one: 2 for the Mining Charge's 5x5x5. */
	public int radius() {
		return RADIUS;
	}

	/** The blocks this charge breaks, from its centre (a cube for the Mining Charge). */
	public Iterable<BlockPos> targets(BlockState state, BlockPos center) {
		int r = radius();
		return BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r));
	}

	/** Share of the broken blocks that drop as items. */
	public float dropChance() {
		return 1f;
	}

	/** Strength of the blast that hurts what stands near (it breaks no blocks itself). */
	public float blastPower() {
		return 3f;
	}

	/** Can this charge break the block? The Mining Charge stops at obsidian; nothing breaks bedrock. */
	public boolean breaks(BlockState s, ServerWorld world, BlockPos p) {
		return s.getHardness(world, p) >= 0 && s.getBlock().getBlastResistance() < STOPS_AT;
	}

	public MiningChargeBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(LIT, false));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	public static void light(World world, BlockPos pos, BlockState state) {
		if (state.get(LIT)) {
			return;
		}
		world.setBlockState(pos, state.with(LIT, true), 3);
		world.scheduleBlockTick(pos, state.getBlock(), FUSE);
		world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1f, 1f);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (!stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient) {
			light(world, pos, state);
			player.sendMessage(Text.translatable("gui.periodic_elements.charge.lit"), true);
			if (stack.isOf(Items.FLINT_AND_STEEL)) {
				stack.damage(1, player, net.minecraft.entity.LivingEntity.getSlotForHand(hand));
			} else {
				stack.decrementUnlessCreative(1, player);
			}
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		if (!world.isClient && world.isReceivingRedstonePower(pos)) {
			light(world, pos, state);
		}
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.isOf(this) && !world.isClient && world.isReceivingRedstonePower(pos)) {
			light(world, pos, state);
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(LIT)) {
			detonate(world, pos);
		}
	}

	/** Breaks the cube (dropping every block) and makes a harmless-to-blocks blast that hurts mobs and players. */
	public static void detonate(ServerWorld world, BlockPos center) {
		BlockState self = world.getBlockState(center);
		MiningChargeBlock charge = self.getBlock() instanceof MiningChargeBlock c ? c : (MiningChargeBlock) Farm.MINING_CHARGE;
		world.removeBlock(center, false);
		// The blast first (it would destroy dropped items), then the rock comes loose.
		world.createExplosion(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, charge.blastPower(),
				World.ExplosionSourceType.NONE);
		if (world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
			for (BlockPos p : charge.targets(self, center)) {
				BlockState s = world.getBlockState(p);
				if (s.isAir() || !s.getFluidState().isEmpty() && s.getBlock() instanceof net.minecraft.block.FluidBlock) {
					continue;
				}
				if (!charge.breaks(s, world, p)) {
					continue;
				}
				if (s.getBlock() instanceof MiningChargeBlock) {
					light(world, p.toImmutable(), s); // sets off its neighbours a moment later
					continue;
				}
				world.breakBlock(p, charge.dropChance() >= 1 || world.random.nextFloat() < charge.dropChance());
			}
		}
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(LIT)) {
			world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 0, 0.02, 0);
			world.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.02, pos.getZ() + 0.5, 0, 0, 0);
		}
	}
}
