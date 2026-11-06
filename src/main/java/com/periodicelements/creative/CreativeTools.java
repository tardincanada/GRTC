package com.periodicelements.creative;

import com.periodicelements.BlockComposition;
import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.multiblock.Structures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 3.51.0: the four creative test items that are not blocks. */
public final class CreativeTools {
	private CreativeTools() {
	}

	/** Protection Amulet: anywhere in the inventory, it keeps the player safe (see {@link CreativeItems#protect}). */
	public static class Amulet extends Item {
		public Amulet(Settings settings) {
			super(settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			CreativeItems.tip(this, tooltip);
		}
	}

	/** Growth Wand: grows a crop, sapling or shrub at once (sneaking: everything within 3 blocks); a young animal grows up. */
	public static class GrowthWand extends Item {
		public static final int AREA = 3;
		private static final int STEPS = 48;

		public GrowthWand(Settings settings) {
			super(settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			CreativeItems.tip(this, tooltip);
		}

		@Override
		public ActionResult useOnBlock(ItemUsageContext ctx) {
			if (!(ctx.getWorld() instanceof ServerWorld world)) {
				return ActionResult.SUCCESS;
			}
			BlockPos center = ctx.getBlockPos();
			int r = ctx.getPlayer() != null && ctx.getPlayer().isSneaking() ? AREA : 0;
			int grown = 0;
			for (BlockPos p : BlockPos.iterate(center.add(-r, -1 - r / 3, -r), center.add(r, 1 + r / 3, r))) {
				if (grow(world, p.toImmutable())) {
					grown++;
				}
			}
			if (ctx.getPlayer() != null) {
				ctx.getPlayer().sendMessage(Text.translatable("message.periodic_elements.growth_wand.grown", grown), true);
			}
			return ActionResult.CONSUME;
		}

		/** Grows one block as far as it goes; returns whether it changed. */
		public static boolean grow(ServerWorld world, BlockPos p) {
			BlockState start = world.getBlockState(p);
			if (start.isAir() || start.isOf(Blocks.GRASS_BLOCK) || start.isOf(Blocks.MOSS_BLOCK)) {
				return false; // bonemeal on these scatters plants everywhere: not what a test wants
			}
			boolean changed = false;
			for (int i = 0; i < STEPS; i++) {
				BlockState s = world.getBlockState(p);
				if (s.getBlock() instanceof Fertilizable f && f.isFertilizable(world, p, s)) {
					f.grow(world, world.random, p, s);
				} else if (s.hasRandomTicks() && s.getBlock() == start.getBlock()) {
					s.randomTick(world, p, world.random);
				} else {
					break;
				}
				changed |= world.getBlockState(p) != s;
			}
			if (changed) {
				world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, p.getX() + 0.5, p.getY() + 0.7, p.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0);
			}
			return changed;
		}

		@Override
		public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
			if (entity instanceof PassiveEntity animal && animal.isBaby()) {
				if (!user.getWorld().isClient) {
					animal.setBreedingAge(0);
					((ServerWorld) user.getWorld()).spawnParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getBodyY(0.6),
							animal.getZ(), 8, 0.4, 0.4, 0.4, 0);
				}
				return ActionResult.success(user.getWorld().isClient);
			}
			return ActionResult.PASS;
		}
	}

	/** Debug Magnifier: shows in chat the inside of the block or animal it is used on. */
	public static class Magnifier extends Item {
		public static final int MAX_LINES = 32;
		private static final Set<String> BORING = Set.of("Motion", "Rotation", "FallDistance", "Fire", "Air", "OnGround",
				"Invulnerable", "PortalCooldown", "UUID", "Pos", "HurtTime", "HurtByTimestamp", "DeathTime", "FallFlying",
				"AbsorptionAmount", "Brain", "attributes", "LeftHanded", "CanPickUpLoot", "PersistenceRequired",
				"ArmorItems", "HandItems", "ArmorDropChances", "HandDropChances", "body_armor_drop_chance", "id", "x", "y", "z",
				"components");

		public Magnifier(Settings settings) {
			super(settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			CreativeItems.tip(this, tooltip);
		}

		@Override
		public ActionResult useOnBlock(ItemUsageContext ctx) {
			if (!(ctx.getWorld() instanceof ServerWorld world) || ctx.getPlayer() == null) {
				return ActionResult.SUCCESS;
			}
			PlayerEntity player = ctx.getPlayer();
			BlockPos pos = ctx.getBlockPos();
			BlockState state = world.getBlockState(pos);
			Identifier id = Registries.BLOCK.getId(state.getBlock());
			StringBuilder props = new StringBuilder();
			for (Property<?> p : state.getProperties()) {
				props.append(props.isEmpty() ? "[" : ", ").append(p.getName()).append('=').append(state.get(p));
			}
			if (!props.isEmpty()) {
				props.append(']');
			}
			header(player, state.getBlock().getName().copy().append(Text.literal(" (" + id + props + ") @ " + pos.toShortString())));
			composition(player, id.toString());
			BlockEntity be = world.getBlockEntity(pos);
			if (be != null) {
				dump(player, be.createNbt(world.getRegistryManager()));
			}
			return ActionResult.CONSUME;
		}

		@Override
		public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
			if (user.getWorld().isClient) {
				return ActionResult.SUCCESS;
			}
			header(user, entity.getName().copy().append(Text.literal(" (" + Registries.ENTITY_TYPE.getId(entity.getType())
					+ String.format(Locale.ROOT, ") %.1f/%.1f HP", entity.getHealth(), entity.getMaxHealth()))));
			NbtCompound nbt = new NbtCompound();
			entity.writeNbt(nbt);
			dump(user, nbt);
			return ActionResult.CONSUME;
		}

		private static void header(PlayerEntity player, Text text) {
			player.sendMessage(Text.literal("— ").append(text).formatted(Formatting.LIGHT_PURPLE), false);
		}

		/** The composition, each part by its name in the player's language (3.51.1). */
		private static void composition(PlayerEntity player, String id) {
			BlockComposition.Entry e = BlockComposition.of(id);
			if (e == null) {
				return;
			}
			net.minecraft.text.MutableText parts = Text.empty();
			int n = 0;
			for (BlockComposition.Part part : e.parts()) {
				if (n == 8) {
					parts.append(", …");
					break;
				}
				if (n++ > 0) {
					parts.append(", ");
				}
				parts.append(Text.translatable("item." + PeriodicElements.MOD_ID + "." + part.id()))
						.append(String.format(Locale.ROOT, " %.1f", part.units()));
			}
			player.sendMessage(Text.translatable("message.periodic_elements.debug_magnifier.composition",
					String.format(Locale.ROOT, "%.0f", e.amount()), parts).formatted(Formatting.GRAY), false);
		}

		/** One line per top-level key, long values cut short. */
		static void dump(PlayerEntity player, NbtCompound nbt) {
			int lines = 0;
			for (String key : nbt.getKeys().stream().sorted().toList()) {
				if (BORING.contains(key)) {
					continue;
				}
				if (lines++ == MAX_LINES) {
					player.sendMessage(Text.literal("…").formatted(Formatting.DARK_GRAY), false);
					break;
				}
				NbtElement v = nbt.get(key);
				String s = v == null ? "" : v.toString();
				if (s.length() > 120) {
					s = s.substring(0, 117) + "…";
				}
				player.sendMessage(Text.literal(key).formatted(Formatting.AQUA).append(Text.literal(" = " + s).formatted(Formatting.WHITE)), false);
			}
			if (lines == 0) {
				player.sendMessage(Text.translatable("message.periodic_elements.debug_magnifier.nothing").formatted(Formatting.GRAY), false);
			}
		}
	}

	/**
	 * Structure Builder: sneak + use in the air picks a structure (the six multiblocks at their smallest, or a
	 * 3x3x3 tank); use on a block builds it there, the controller facing you and the rest stretching away.
	 */
	public static class Builder extends Item {
		/** The structures, in the order sneak + use goes through them; the last is the tank. */
		public static final List<String> CHOICES = List.of("charcoal_blast_furnace", "coke_oven", "lead_chamber", "refinery_still",
				"enrichment_cascade", "nuclear_reactor", "tank");

		public Builder(Settings settings) {
			super(settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			CreativeItems.tip(this, tooltip);
			tooltip.add(Text.translatable("message.periodic_elements.structure_builder.chosen", name(choice(stack))).formatted(Formatting.YELLOW));
		}

		public static int choice(ItemStack stack) {
			NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
			int c = data == null ? 0 : data.copyNbt().getInt("Structure");
			return Math.floorMod(c, CHOICES.size());
		}

		static Text name(int choice) {
			String c = CHOICES.get(choice);
			return c.equals("tank") ? Text.translatable("message.periodic_elements.structure_builder.tank")
					: Text.translatable("block." + PeriodicElements.MOD_ID + "." + c);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			ItemStack stack = user.getStackInHand(hand);
			if (!user.isSneaking()) {
				return TypedActionResult.pass(stack);
			}
			if (!world.isClient) {
				int next = (choice(stack) + 1) % CHOICES.size();
				NbtCompound nbt = new NbtCompound();
				nbt.putInt("Structure", next);
				stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
				user.sendMessage(Text.translatable("message.periodic_elements.structure_builder.chosen", name(next)), true);
			}
			return TypedActionResult.success(stack, world.isClient);
		}

		@Override
		public ActionResult useOnBlock(ItemUsageContext ctx) {
			PlayerEntity player = ctx.getPlayer();
			if (player == null || player.isSneaking()) {
				return ActionResult.PASS; // sneaking: let use() switch the structure
			}
			if (!(ctx.getWorld() instanceof ServerWorld world)) {
				return ActionResult.SUCCESS;
			}
			BlockPos c = ctx.getBlockPos().offset(ctx.getSide() == Direction.DOWN ? Direction.DOWN : Direction.UP);
			if (ctx.getSide().getAxis().isHorizontal()) {
				c = ctx.getBlockPos().offset(ctx.getSide());
			}
			Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
			int choice = choice(ctx.getStack());
			build(world, CHOICES.get(choice), c, facing, player);
			player.sendMessage(Text.translatable("message.periodic_elements.structure_builder.built", name(choice)), true);
			world.playSound(null, c, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.6f, 1.2f);
			return ActionResult.CONSUME;
		}

		/** Builds a structure with its controller at {@code c} facing {@code facing}. */
		public static void build(ServerWorld world, String what, BlockPos c, Direction facing, PlayerEntity owner) {
			if (what.equals("tank")) {
				buildTank(world, c, facing);
				return;
			}
			Structures.Kind kind = Structures.forMachine(what);
			Structures.Template t = Structures.smallest(kind);
			t.build(world, c, facing);
			for (BlockPos rel : t.parts().keySet()) {
				if (world.getBlockEntity(t.toWorld(rel, c, facing)) instanceof MachineBlockEntity m && owner != null) {
					m.setOwner(owner.getUuid());
				}
			}
		}

		/** A tank of Tank Plates 5x5x5 (inside 3x3x3) standing on the ground: controller in the middle of the front, a valve on each side. */
		static void buildTank(ServerWorld world, BlockPos c, Direction facing) {
			Block wall = block("tank_wall"), controller = block("tank_controller"), valve = block("tank_valve");
			Direction right = facing.rotateYClockwise();
			Direction back = facing.getOpposite();
			for (int x = -2; x <= 2; x++) {
				for (int y = 0; y <= 4; y++) {
					for (int z = 0; z <= 4; z++) {
						boolean shell = Math.abs(x) == 2 || y == 0 || y == 4 || z == 0 || z == 4;
						BlockPos p = c.offset(right, x).up(y).offset(back, z);
						world.setBlockState(p, shell ? wall.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
					}
				}
			}
			world.setBlockState(c.up(2), controller.getDefaultState(), 3);
			world.setBlockState(c.offset(right, 2).up(2).offset(back, 2), valve.getDefaultState(), 3);
			world.setBlockState(c.offset(right, -2).up(2).offset(back, 2), valve.getDefaultState(), 3);
		}

		private static Block block(String id) {
			return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
		}
	}
}
