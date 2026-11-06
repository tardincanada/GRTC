package com.periodicelements.fauna;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Set;

/**
 * 3.50.0 (fauna stage 4): a pearl oyster on the floor of warm seas. Broken, it gives two shells and sometimes a pearl.
 * Beds of them appear near players in warm and lukewarm oceans.
 */
public class PearlOysterBlock extends Block implements Waterloggable {
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final Set<String> BIOMES = Set.of("warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean");
	private static final VoxelShape SHAPE = Block.createCuboidShape(3, 0, 3, 13, 4, 13);

	public PearlOysterBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(WATERLOGGED, true));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER));
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighbor, WorldAccess world,
												   BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		return direction == Direction.DOWN && !canPlaceAt(state, world, pos) ? Blocks.AIR.getDefaultState()
				: super.getStateForNeighborUpdate(state, direction, neighbor, world, pos, neighborPos);
	}

	/** Near a player in a warm sea, a small bed of oysters may appear on the floor where there is none near. */
	static void seed(ServerWorld world, BlockPos center, Random r) {
		Block oyster = Fauna.block("pearl_oyster");
		if (oyster == null || r.nextInt(3) != 0) return;
		int x = center.getX() + r.nextInt(97) - 48, z = center.getZ() + r.nextInt(97) - 48;
		if (!world.isChunkLoaded(x >> 4, z >> 4)) return;
		BlockPos floor = new BlockPos(x, world.getTopY(Heightmap.Type.OCEAN_FLOOR, x, z), z);
		String biome = world.getBiome(floor).getKey().map(k -> k.getValue().getPath()).orElse("");
		if (!BIOMES.contains(biome)) return;
		for (BlockPos p : BlockPos.iterate(floor.add(-10, -4, -10), floor.add(10, 4, 10))) {
			if (world.getBlockState(p).isOf(oyster)) return;
		}
		for (int i = 0; i < 5; i++) {
			int px = x + r.nextInt(5) - 2, pz = z + r.nextInt(5) - 2;
			BlockPos p = new BlockPos(px, world.getTopY(Heightmap.Type.OCEAN_FLOOR, px, pz), pz);
			if (world.getBlockState(p).isOf(Blocks.WATER) && world.getFluidState(p).isStill()
					&& (world.getBlockState(p.down()).isIn(BlockTags.SAND) || world.getBlockState(p.down()).isOf(Blocks.GRAVEL))) {
				world.setBlockState(p, oyster.getDefaultState());
			}
		}
	}

	/** Now and then a lump of ambergris, from a sperm whale's gut, washes up on a beach near a player. */
	static void ambergris(ServerWorld world, BlockPos center, Random r) {
		var item = Fauna.item("ambergris");
		if (item == null || r.nextInt(40) != 0) return;
		int x = center.getX() + r.nextInt(65) - 32, z = center.getZ() + r.nextInt(65) - 32;
		if (!world.isChunkLoaded(x >> 4, z >> 4)) return;
		BlockPos top = new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z), z);
		String biome = world.getBiome(top).getKey().map(k -> k.getValue().getPath()).orElse("");
		if (!biome.equals("beach") || !world.getBlockState(top.down()).isIn(BlockTags.SAND)) return;
		var e = new net.minecraft.entity.ItemEntity(world, x + 0.5, top.getY() + 0.1, z + 0.5, new net.minecraft.item.ItemStack(item));
		e.setNeverDespawn();
		world.spawnEntity(e);
	}

	/**
	 * Very rarely a sperm whale strands on a beach near a player and dies there: the only way to get spermaceti, since
	 * players cannot hunt it (Guilherme, answer 3a). About once every few hours of play spent near a beach.
	 */
	static void strand(ServerWorld world, BlockPos center, Random r) {
		if (r.nextInt(600) != 0) return;
		int x = center.getX() + r.nextInt(65) - 32, z = center.getZ() + r.nextInt(65) - 32;
		if (!world.isChunkLoaded(x >> 4, z >> 4)) return;
		BlockPos top = new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z), z);
		String biome = world.getBiome(top).getKey().map(k -> k.getValue().getPath()).orElse("");
		if (!biome.equals("beach") || !world.getBlockState(top.down()).isIn(BlockTags.SAND)) return;
		strandAt(world, top);
	}

	/** Puts a stranded sperm whale at a spot; out of water it dries out and dies, leaving spermaceti and blubber. */
	public static boolean strandAt(ServerWorld world, BlockPos pos) {
		for (var entry : Fauna.SPECIES.entrySet()) {
			if (!entry.getValue().has("strand") || !Fauna.TYPES.containsKey(entry.getKey())) continue;
			var e = Fauna.TYPES.get(entry.getKey()).create(world);
			if (e == null) return false;
			e.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360, 0);
			world.spawnEntity(e);
			return true;
		}
		return false;
	}
}
