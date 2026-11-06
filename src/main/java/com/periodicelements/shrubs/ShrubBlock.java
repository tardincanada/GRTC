package com.periodicelements.shrubs;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * A shrub (3.16.0): one block, ages 0 to 3, that grows only in its biomes or in a {@link Greenhouse} warm enough for it.
 * Ripe (age 3), a right-click picks its product and it goes back to age 1; shears on a bush of age 2 or more cut a
 * cutting (its item) and it goes back to age 1. Thorny kinds prick whoever walks through them (3.22.0, Guilherme:
 * a little damage like the sweet berry bush, without slowing them down).
 */
public class ShrubBlock extends PlantBlock implements Fertilizable {
	public static final IntProperty AGE = Properties.AGE_3;
	private static final VoxelShape SMALL = Block.createCuboidShape(3, 0, 3, 13, 8, 13);
	private static final VoxelShape LARGE = Block.createCuboidShape(1, 0, 1, 15, 16, 15);

	public final String id;
	private final TagKey<Biome> biomes;
	private final List<String> biomeIds;
	private final String soil;
	public final String climate;
	public final boolean thorny;
	private final Supplier<Item> product;
	private Item cutting = Items.AIR;

	public ShrubBlock(String id, List<String> biomeIds, String soil, String climate, boolean thorny, Supplier<Item> product,
					  Settings settings) {
		super(settings);
		this.id = id;
		this.biomes = TagKey.of(RegistryKeys.BIOME, Identifier.of("periodic_elements", "shrubs/" + id));
		this.biomeIds = List.copyOf(biomeIds);
		this.soil = soil;
		this.climate = climate;
		this.thorny = thorny;
		this.product = product;
		setDefaultState(getStateManager().getDefaultState().with(AGE, 0));
	}

	void setCutting(Item cutting) {
		this.cutting = cutting;
	}

	public Item cutting() {
		return cutting;
	}

	@Nullable
	public Item product() {
		return product.get();
	}

	@Override
	protected MapCodec<? extends PlantBlock> getCodec() {
		return MapCodec.unit(() -> this);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	/** In its biomes, or in a greenhouse of the right climate. */
	public boolean growsAt(WorldView world, BlockPos pos) {
		return world.getBiome(pos).isIn(biomes) || Greenhouse.allows(world, pos, climate);
	}

	public Text biomeNames() {
		return Texts.join(biomeIds.stream().map(b -> (Text) Text.translatable("biome.minecraft." + b)).toList(), Text.literal(", "));
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		if (floor.isIn(BlockTags.DIRT) || floor.isOf(net.minecraft.block.Blocks.FARMLAND)) {
			return true;
		}
		return switch (soil) {
			case "sand" -> floor.isIn(BlockTags.SAND) || floor.isIn(BlockTags.TERRACOTTA);
			// 3.22.0: shrubs of sea cliffs, gravel hills and mountain tops.
			case "rock" -> floor.isIn(BlockTags.BASE_STONE_OVERWORLD) || floor.isOf(net.minecraft.block.Blocks.GRAVEL);
			case "alpine" -> floor.isIn(BlockTags.BASE_STONE_OVERWORLD) || floor.isOf(net.minecraft.block.Blocks.GRAVEL)
					|| floor.isOf(net.minecraft.block.Blocks.SNOW_BLOCK) || floor.isOf(net.minecraft.block.Blocks.POWDER_SNOW)
					|| floor.isOf(net.minecraft.block.Blocks.PACKED_ICE) || floor.isIn(BlockTags.SAND);
			default -> false;
		};
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(AGE) == 0 ? SMALL : LARGE;
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
		return new ItemStack(cutting);
	}

	@Override
	protected boolean hasRandomTicks(BlockState state) {
		return state.get(AGE) < 3;
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int age = state.get(AGE);
		if (age < 3 && random.nextInt(5) == 0 && world.getBaseLightLevel(pos.up(), 0) >= 9 && growsAt(world, pos)) {
			world.setBlockState(pos, state.with(AGE, age + 1), Block.NOTIFY_LISTENERS);
			world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
		}
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!thorny || !(entity instanceof LivingEntity) || entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) {
			return;
		}
		if (!world.isClient && state.get(AGE) > 0 && (entity.lastRenderX != entity.getX() || entity.lastRenderZ != entity.getZ())) {
			double dx = Math.abs(entity.getX() - entity.lastRenderX), dz = Math.abs(entity.getZ() - entity.lastRenderZ);
			if (dx >= 0.003 || dz >= 0.003) {
				entity.damage(world.getDamageSources().sweetBerryBush(), 1.0f);
			}
		}
	}

	/** Picks the ripe harvest: one or two of the product, and the bush goes back to age 1. */
	public boolean harvest(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player) {
		Item item = product();
		if (state.get(AGE) != 3 || item == null) {
			return false;
		}
		if (!world.isClient) {
			dropStack(world, pos, new ItemStack(item, 1 + world.random.nextInt(2)));
			world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0f,
					0.8f + world.random.nextFloat() * 0.4f);
			BlockState after = state.with(AGE, 1);
			world.setBlockState(pos, after, Block.NOTIFY_LISTENERS);
			world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, after));
		}
		return true;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (stack.isOf(Items.SHEARS) && state.get(AGE) >= 2) {
			if (!world.isClient) {
				dropStack(world, pos, new ItemStack(cutting));
				world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS, 1.0f, 1.2f);
				world.setBlockState(pos, state.with(AGE, 1), Block.NOTIFY_LISTENERS);
				stack.damage(1, player, LivingEntity.getSlotForHand(hand));
			}
			return ItemActionResult.success(world.isClient);
		}
		if (stack.isOf(Items.BONE_MEAL) && state.get(AGE) < 3) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		return harvest(state, world, pos, player) ? ItemActionResult.success(world.isClient)
				: ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		return harvest(state, world, pos, player) ? ActionResult.success(world.isClient) : super.onUse(state, world, pos, player, hit);
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return state.get(AGE) < 3 && growsAt(world, pos);
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		world.setBlockState(pos, state.with(AGE, Math.min(3, state.get(AGE) + 1)), Block.NOTIFY_LISTENERS);
	}
}
