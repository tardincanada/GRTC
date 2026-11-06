package com.periodicelements.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.List;

/**
 * One of the 20 crops of 3.23.0. Planted on farmland like wheat; the WILD form stands ripe on grass, sand or mud in
 * the biomes where the crop's ancestors grow. A perennial (coffee, alfalfa) is picked with an empty hand and grows
 * back from {@link CropInfo#perennial()}.
 */
public class GeneticCropBlock extends CropBlock {
	public static final BooleanProperty WILD = BooleanProperty.of("wild");
	private CropInfo info;

	public GeneticCropBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(WILD, false));
	}

	void setInfo(CropInfo info) {
		this.info = info;
	}

	public CropInfo info() {
		return info;
	}

	@Override
	public MapCodec<? extends CropBlock> getCodec() {
		return MapCodec.unit(() -> this);
	}

	@Override
	protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(WILD);
	}

	@Override
	protected ItemConvertible getSeedsItem() {
		return info == null ? Blocks.AIR : info.seed().get();
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (state.get(WILD)) {
			BlockState floor = world.getBlockState(pos.down());
			return floor.isIn(BlockTags.DIRT) || floor.isIn(BlockTags.SAND) || floor.isOf(Blocks.MUD) || floor.isOf(Blocks.FARMLAND);
		}
		return super.canPlaceAt(state, world, pos);
	}

	@Override
	protected boolean hasRandomTicks(BlockState state) {
		return !state.get(WILD) && super.hasRandomTicks(state);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		Integer back = info == null ? null : info.perennial();
		if (back == null || !isMature(state) || !player.getMainHandStack().isEmpty()) {
			return super.onUse(state, world, pos, player, hit);
		}
		if (world instanceof ServerWorld sw) {
			List<ItemStack> drops = Genetics.harvestInPlace(state, sw, pos);
			for (ItemStack s : drops) {
				dropStack(world, pos, s);
			}
			world.setBlockState(pos, state.with(AGE, Math.min(back, getMaxAge() - 1)), 2);
			world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1f, 1f);
		}
		return ActionResult.success(world.isClient);
	}

	/** For the record/info: the product item this crop gives when ripe. */
	public Item product() {
		return info.product().get();
	}
}
