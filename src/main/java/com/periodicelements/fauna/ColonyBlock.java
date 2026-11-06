package com.periodicelements.fauna;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Set;

/**
 * A colony of scale insects (cochineal on cactus, lac insects on jungle wood), a crust on the side of its host. It
 * grows through three ages; ripe (age 2) it is scraped with a use for its product and starts again, and now and then
 * it spreads to another side nearby. FACING points away from the host, like a ladder.
 */
public class ColonyBlock extends HorizontalFacingBlock {
	public static final MapCodec<ColonyBlock> CODEC = createCodec(s -> new ColonyBlock(s, Blocks.CACTUS, "cochineal"));
	public static final IntProperty AGE = Properties.AGE_2;
	public static final Set<String> COCHINEAL_BIOMES = Set.of("desert", "badlands", "eroded_badlands", "wooded_badlands");
	public static final Set<String> LAC_BIOMES = Set.of("jungle", "bamboo_jungle", "sparse_jungle");
	private static final VoxelShape[] SHAPES = {
			Block.createCuboidShape(0, 0, 0, 16, 16, 2), Block.createCuboidShape(14, 0, 0, 16, 16, 16),
			Block.createCuboidShape(0, 0, 14, 16, 16, 16), Block.createCuboidShape(0, 0, 0, 2, 16, 16)};
	private final Block host;
	private final String product;

	public ColonyBlock(Settings settings, Block host, String product) {
		super(settings);
		this.host = host;
		this.product = product;
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(AGE, 0));
	}

	@Override
	protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, AGE);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPES[state.get(FACING).getHorizontal()];
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return world.getBlockState(pos.offset(state.get(FACING).getOpposite())).isOf(host);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighbor, WorldAccess world,
												   BlockPos pos, BlockPos neighborPos) {
		return canPlaceAt(state, world, pos) ? state : Blocks.AIR.getDefaultState();
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int age = state.get(AGE);
		if (age < 2) {
			if (random.nextInt(4) == 0) world.setBlockState(pos, state.with(AGE, age + 1));
		} else if (random.nextInt(12) == 0) {
			spread(world, pos, random);
		}
	}

	/** A free side of a host block within 2 blocks gets a young colony. */
	private void spread(ServerWorld world, BlockPos pos, Random random) {
		BlockPos h = pos.add(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
		if (!world.getBlockState(h).isOf(host)) return;
		Direction d = Direction.Type.HORIZONTAL.random(random);
		BlockPos at = h.offset(d);
		if (world.getBlockState(at).isAir()) world.setBlockState(at, getDefaultState().with(FACING, d));
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(AGE) < 2) return ActionResult.PASS;
		if (!world.isClient) {
			Block.dropStack(world, pos, new ItemStack(Registries.ITEM.get(Identifier.of("periodic_elements", product)),
					1 + world.random.nextInt(2)));
			world.setBlockState(pos, state.with(AGE, 0));
			world.playSound(null, pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 0.6f, 1.4f);
		}
		return ActionResult.success(world.isClient);
	}

	/** Near a player, a colony may appear on a cactus (desert, badlands) or a jungle log (jungles) that has none near. */
	static void seed(ServerWorld world, BlockPos center, Random r) {
		if (r.nextInt(4) != 0) return;
		int x = center.getX() + r.nextInt(65) - 32, z = center.getZ() + r.nextInt(65) - 32;
		if (!world.isChunkLoaded(x >> 4, z >> 4)) return;
		int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
		BlockPos topPos = new BlockPos(x, top, z);
		String biome = world.getBiome(topPos).getKey().map(k -> k.getValue().getPath()).orElse("");
		Block colony = COCHINEAL_BIOMES.contains(biome) ? Fauna.block("cochineal_colony")
				: LAC_BIOMES.contains(biome) ? Fauna.block("lac_colony") : null;
		if (!(colony instanceof ColonyBlock c)) return;
		for (int y = top; y > top - 24; y--) {
			BlockPos h = new BlockPos(x, y, z);
			if (!world.getBlockState(h).isOf(c.host)) continue;
			for (BlockPos p : BlockPos.iterate(h.add(-6, -4, -6), h.add(6, 4, 6))) {
				if (world.getBlockState(p).isOf(colony)) return;
			}
			Direction d = Direction.Type.HORIZONTAL.random(r);
			BlockPos at = h.offset(d);
			if (world.getBlockState(at).isAir()) {
				world.setBlockState(at, colony.getDefaultState().with(FACING, d).with(AGE, r.nextInt(3)));
			}
			return;
		}
	}
}
