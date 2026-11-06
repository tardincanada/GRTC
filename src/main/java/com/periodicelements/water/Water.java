package com.periodicelements.water;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Salt water and fresh water (3.45.0). The sea is salt water: every water block of an ocean, beach or stony shore
 * column is made salt water once a chunk's features are placed (lakes in ocean biomes too); rivers and lakes
 * elsewhere stay fresh, vanilla water. Fresh water is pure, like distilled water. Rare brine springs dot the land.
 *
 * <p>Infinite water: two fresh sources make fresh water and two salt sources salt water, but a fresh and a salt
 * source always make salt water, and the fresh sources that took part turn salt too (FlowableFluidMixin). A fresh
 * source poured with salt water on three or four sides is sea water at once (FluidBlockMixin). Brine is not
 * infinite. All three look and behave like water; only their buckets tell them apart.
 */
public final class Water {
	public static FlowableFluid SALT_WATER, FLOWING_SALT_WATER, BRINE, FLOWING_BRINE;
	public static Block SALT_WATER_BLOCK, BRINE_BLOCK;
	public static Item SALT_WATER_BUCKET, BRINE_BUCKET;
	/** One chunk in this many has a brine spring. */
	public static final int BRINE_SPRING_CHANCE = 500;
	/** Sources made salt by mixing pass no salt on for this long (stops a chain of conversions up a river). */
	static final long CHAIN_TICKS = 1200;
	private static final java.util.Map<World, it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap> CONVERTED =
			java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

	private Water() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	/** A bucket with a grey tooltip line from "&lt;key&gt;.tip". */
	public static class TippedBucket extends BucketItem {
		public TippedBucket(Fluid fluid, Settings settings) {
			super(fluid, settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		}
	}

	/** Registers the fluids, their blocks and buckets (the salt water bucket keeps the old id seawater_bucket). */
	public static void register(List<Item> items) {
		SALT_WATER = Registry.register(Registries.FLUID, id("salt_water"), new SeaFluid.Still() {
			protected boolean salt() {
				return true;
			}
		});
		FLOWING_SALT_WATER = Registry.register(Registries.FLUID, id("flowing_salt_water"), new SeaFluid.Flowing() {
			protected boolean salt() {
				return true;
			}
		});
		BRINE = Registry.register(Registries.FLUID, id("brine"), new SeaFluid.Still() {
			protected boolean salt() {
				return false;
			}
		});
		FLOWING_BRINE = Registry.register(Registries.FLUID, id("flowing_brine"), new SeaFluid.Flowing() {
			protected boolean salt() {
				return false;
			}
		});
		SALT_WATER_BLOCK = Registry.register(Registries.BLOCK, id("salt_water"),
				new FluidBlock(SALT_WATER, AbstractBlock.Settings.copy(Blocks.WATER)) {
				});
		BRINE_BLOCK = Registry.register(Registries.BLOCK, id("brine"),
				new FluidBlock(BRINE, AbstractBlock.Settings.copy(Blocks.WATER)) {
				});
		SALT_WATER_BUCKET = Registry.register(Registries.ITEM, id("seawater_bucket"),
				new TippedBucket(SALT_WATER, new Item.Settings().maxCount(1).recipeRemainder(Items.BUCKET)));
		BRINE_BUCKET = Registry.register(Registries.ITEM, id("brine_bucket"),
				new TippedBucket(BRINE, new Item.Settings().maxCount(1).recipeRemainder(Items.BUCKET)));
		items.add(SALT_WATER_BUCKET);
		items.add(BRINE_BUCKET);
		// A glass bottle does not fill from salt water or brine: drinking water has to be fresh.
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isOf(Items.GLASS_BOTTLE) || player.isSpectator()) {
				return TypedActionResult.pass(stack);
			}
			Vec3d eye = player.getEyePos();
			Vec3d end = eye.add(player.getRotationVec(1f).multiply(player.getBlockInteractionRange()));
			BlockHitResult hit = world.raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.OUTLINE,
					RaycastContext.FluidHandling.SOURCE_ONLY, player));
			if (hit.getType() == HitResult.Type.BLOCK && salty(world.getFluidState(hit.getBlockPos()).getFluid())) {
				return TypedActionResult.fail(stack);
			}
			return TypedActionResult.pass(stack);
		});
	}

	public static boolean isSaltWater(Fluid f) {
		return f == SALT_WATER || f == FLOWING_SALT_WATER;
	}

	public static boolean isBrine(Fluid f) {
		return f == BRINE || f == FLOWING_BRINE;
	}

	/** Salt water or brine. */
	public static boolean salty(Fluid f) {
		return isSaltWater(f) || isBrine(f);
	}

	public static boolean isFresh(Fluid f) {
		return f == net.minecraft.fluid.Fluids.WATER || f == net.minecraft.fluid.Fluids.FLOWING_WATER;
	}

	/** Fresh water and salt water flow together and make infinite sources together. */
	public static boolean freshOrSalt(Fluid f) {
		return isFresh(f) || isSaltWater(f);
	}

	/** Ocean, deep ocean, beach and stony shore: the sea. */
	public static boolean isSea(RegistryEntry<Biome> biome) {
		return biome.isIn(BiomeTags.IS_OCEAN) || biome.isIn(BiomeTags.IS_DEEP_OCEAN) || biome.isIn(BiomeTags.IS_BEACH)
				|| biome.matchesKey(BiomeKeys.STONY_SHORE);
	}

	// ---- infinite sources ----------------------------------------------------------------------------------

	/**
	 * A water or salt water flow at pos has just been made a new source by vanilla's rule (two sources beside it).
	 * Returns the source it becomes: salt if any source beside it is salt, else fresh; with salt, the fresh
	 * sources beside it turn salt as well.
	 */
	public static FluidState newSource(World world, BlockPos pos, FluidState made) {
		if (world.getFluidState(pos).isStill()) {
			return made; // already a source: nothing new forms
		}
		boolean salt = false, sea = false;
		List<BlockPos> fresh = new ArrayList<>();
		long now = world.getTime();
		var recent = CONVERTED.computeIfAbsent(world, w -> new it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap());
		for (Direction d : Direction.Type.HORIZONTAL) {
			BlockPos n = pos.offset(d);
			FluidState f = world.getFluidState(n);
			if (!f.isStill()) continue;
			if (isSaltWater(f.getFluid())) {
				salt = true;
				// salt water that was itself fresh a moment ago does not pass the salt on: no chain up a river
				sea |= now - recent.getOrDefault(n.asLong(), Long.MIN_VALUE / 2) > CHAIN_TICKS;
			}
			else if (isFresh(f.getFluid()) && world.getBlockState(n).isOf(Blocks.WATER)) fresh.add(n);
		}
		if (!salt) {
			return net.minecraft.fluid.Fluids.WATER.getStill(false);
		}
		if (!world.isClient && sea) {
			if (recent.size() > 4096) recent.long2LongEntrySet().removeIf(e -> now - e.getLongValue() > CHAIN_TICKS);
			recent.put(pos.asLong(), now);
			for (BlockPos n : fresh) {
				world.setBlockState(n, SALT_WATER_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
				recent.put(n.asLong(), now);
			}
		}
		return SALT_WATER.getStill(false);
	}

	/** A fresh source with salt water sources on three or four sides is part of the sea: it turns salt. */
	public static boolean drownedInSea(WorldView world, BlockPos pos) {
		int salt = 0;
		for (Direction d : Direction.Type.HORIZONTAL) {
			FluidState f = world.getFluidState(pos.offset(d));
			if (f.isStill() && isSaltWater(f.getFluid())) salt++;
		}
		return salt >= 3;
	}

	// ---- world generation ----------------------------------------------------------------------------------

	/** After a chunk's features: the sea's water becomes salt water, and now and then a brine spring is dug. */
	public static void afterFeatures(StructureWorldAccess world, Chunk chunk) {
		int sea = world.getSeaLevel();
		ChunkPos cp = chunk.getPos();
		ChunkSection[] sections = chunk.getSectionArray();
		BlockState salt = SALT_WATER_BLOCK.getDefaultState();
		boolean any = false;
		boolean[] column = new boolean[256];
		// In the Geological world a river valley low enough can get an ocean biome: its river stays fresh.
		net.minecraft.world.gen.noise.NoiseConfig geo = null;
		if (com.periodicelements.worldgen.GeoWorld.active() && world.toServerWorld().getRegistryKey() == World.OVERWORLD) {
			geo = world.toServerWorld().getChunkManager().getNoiseConfig();
		}
		for (int lx = 0; lx < 16; lx++) {
			for (int lz = 0; lz < 16; lz++) {
				int x = cp.getStartX() + lx, z = cp.getStartZ() + lz;
				RegistryEntry<Biome> biome = chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(x),
						BiomeCoords.fromBlock(Math.max(chunk.getBottomY(), sea)), BiomeCoords.fromBlock(z));
				column[lx * 16 + lz] = isSea(biome) && (geo == null || !com.periodicelements.worldgen.GeoWorld.riverColumn(geo, x, z));
				any |= column[lx * 16 + lz];
			}
		}
		if (any) {
			for (int i = 0; i < sections.length; i++) {
				ChunkSection s = sections[i];
				if (s.isEmpty() || !s.hasAny(st -> st.isOf(Blocks.WATER))) continue;
				for (int lx = 0; lx < 16; lx++) {
					for (int lz = 0; lz < 16; lz++) {
						if (!column[lx * 16 + lz]) continue;
						for (int ly = 0; ly < 16; ly++) {
							BlockState st = s.getBlockState(lx, ly, lz);
							if (st.isOf(Blocks.WATER)) {
								s.setBlockState(lx, ly, lz, salt.with(FluidBlock.LEVEL, st.get(FluidBlock.LEVEL)), false);
							}
						}
					}
				}
			}
		}
		brineSpring(world, chunk, column);
	}

	/** Whether this chunk holds a brine spring (about one chunk in BRINE_SPRING_CHANCE, decided by the seed). */
	public static boolean hasBrineSpring(long seed, ChunkPos cp) {
		long h = seed ^ 0x5DEECE66DL;
		h = h * 6364136223846793005L + cp.x * 341873128712L;
		h = h * 6364136223846793005L + cp.z * 132897987541L;
		h ^= h >>> 29;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 32;
		return Math.floorMod(h, BRINE_SPRING_CHANCE) == 0;
	}

	/**
	 * A brine spring: a round pool of brine about five blocks across with a shaft six deep at its middle, ringed
	 * with a white salt crust (calcite). Only on dry land above the sea, away from the sea and rivers.
	 */
	static void brineSpring(StructureWorldAccess world, Chunk chunk, boolean[] seaColumn) {
		ChunkPos cp = chunk.getPos();
		if (!hasBrineSpring(world.getSeed(), cp)) return;
		int cx = cp.getStartX() + 8, cz = cp.getStartZ() + 8;
		BlockPos.Mutable p = new BlockPos.Mutable();
		int top = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, 8, 8);
		int sea = world.getSeaLevel();
		if (top <= sea + 1) return;
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				int lx = 8 + dx, lz = 8 + dz;
				if (seaColumn[lx * 16 + lz]) return;
				int h = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, lx, lz);
				if (Math.abs(h - top) > 2) return; // too steep
				p.set(cx + dx, h + 1, cz + dz);
				if (!chunk.getFluidState(p).isEmpty()) return; // water or lava on top
				p.set(cx + dx, h, cz + dz);
				if (!chunk.getBlockState(p).isSolid()) return;
				RegistryEntry<Biome> biome = chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(cx + dx),
						BiomeCoords.fromBlock(h), BiomeCoords.fromBlock(cz + dz));
				if (biome.isIn(BiomeTags.IS_RIVER)) return;
			}
		}
		BlockState brine = BRINE_BLOCK.getDefaultState();
		BlockState crust = Blocks.CALCITE.getDefaultState();
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				int d2 = dx * dx + dz * dz;
				int x = cx + dx, z = cz + dz;
				if (d2 <= 5) {
					int depth = d2 == 0 ? 6 : d2 <= 2 ? 3 : 2;
					for (int y = top - depth + 1; y <= top; y++) {
						p.set(x, y, z);
						chunk.setBlockState(p, brine, false);
					}
					// walls and floor stay closed: anything not solid around the brine becomes the rock below
					BlockState rock = chunk.getBlockState(p.set(x, top - depth - 1, z));
					if (!rock.isSolid() || !rock.getFluidState().isEmpty()) rock = Blocks.STONE.getDefaultState();
					p.set(x, top - depth, z);
					if (!chunk.getBlockState(p).isSolid()) chunk.setBlockState(p, rock, false);
					for (Direction d : Direction.Type.HORIZONTAL) {
						int nx = dx + d.getOffsetX(), nz = dz + d.getOffsetZ();
						if (nx * nx + nz * nz <= 5) continue;
						for (int y = top - depth + 1; y <= top; y++) {
							p.set(x + d.getOffsetX(), y, z + d.getOffsetZ());
							BlockState wall = chunk.getBlockState(p);
							if (!wall.isSolid() || !wall.getFluidState().isEmpty()) chunk.setBlockState(p, rock, false);
						}
					}
					clearAbove(chunk, p, x, top, z);
				} else if (d2 <= 10) {
					p.set(x, chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, 8 + dx, 8 + dz), z);
					BlockState ground = chunk.getBlockState(p);
					if (ground.isIn(BlockTags.DIRT) || ground.isIn(BlockTags.SAND) || ground.isOf(Blocks.GRAVEL)) {
						chunk.setBlockState(p, crust, false);
						clearAbove(chunk, p, x, p.getY(), z);
					}
				}
			}
		}
	}

	/** Plants and snow on top of the spring go; logs and leaves of trees stay. */
	private static void clearAbove(Chunk chunk, BlockPos.Mutable p, int x, int top, int z) {
		for (int y = top + 1; y <= top + 2; y++) {
			p.set(x, y, z);
			BlockState above = chunk.getBlockState(p);
			if (!above.isAir() && above.isReplaceable() && above.getFluidState().isEmpty()) {
				chunk.setBlockState(p, Blocks.AIR.getDefaultState(), false);
			}
		}
	}
}
