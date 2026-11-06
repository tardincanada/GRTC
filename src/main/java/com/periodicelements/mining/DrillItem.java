package com.periodicelements.mining;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.ResearchedToolItem;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * The powered mining tools (2.1.0): Pneumatic Rock Drill (L4, compressed air), Electric Rotary Hammer (L5), Heavy
 * Mining Drill (L6: 5x5 and vein modes) and Chainsaw (L5, whole trees). Each keeps a small charge of its own, topped up
 * while held from Compressed Air cylinders or battery packs in the inventory, and pays
 * {@link #KJ_PER_HARDNESS} kJ per point of hardness of every block it breaks. A drill needs a bit, which sets the tier
 * and speed and wears one point per swing. Sneak and use: with a bit in the other hand, swap bits; otherwise change mode.
 */
public class DrillItem extends Item implements AreaMining.AreaTool {
	public enum Kind { PNEUMATIC, ROTARY, HEAVY, CHAINSAW }

	public enum Mode {
		SINGLE(1.0), TUNNEL(1.0), AREA3(0.5), AREA5(0.3), VEIN(0.5), TREE(0.5);

		public final double speed;

		Mode(double speed) {
			this.speed = speed;
		}
	}

	public static final double KJ_PER_HARDNESS = 1.5;
	/** One Compressed Air cylinder drives the Pneumatic Rock Drill this far: about 400 stone. */
	public static final double CYLINDER_KJ = 600;
	public static final float CHAINSAW_SPEED = 10f;
	private final Kind kind;
	private final String id;

	public DrillItem(Settings settings, Kind kind, String id) {
		super(settings.maxCount(1));
		this.kind = kind;
		this.id = id;
	}

	public Kind kind() {
		return kind;
	}

	/** The drill's own store: what one top-up holds. */
	public double capacity() {
		return kind == Kind.PNEUMATIC ? CYLINDER_KJ + 100 : 200;
	}

	public List<Mode> modes() {
		return switch (kind) {
			case PNEUMATIC, ROTARY -> List.of(Mode.SINGLE, Mode.TUNNEL, Mode.AREA3);
			case HEAVY -> List.of(Mode.SINGLE, Mode.TUNNEL, Mode.AREA3, Mode.AREA5, Mode.VEIN);
			case CHAINSAW -> List.of(Mode.TREE, Mode.SINGLE);
		};
	}

	// ---- stored state ----

	private static NbtComponent data(ItemStack stack) {
		return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
	}

	public static double energy(ItemStack stack) {
		return data(stack).copyNbt().getDouble("Energy");
	}

	public static void setEnergy(ItemStack stack, double kJ) {
		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, n -> n.putDouble("Energy", Math.max(0, kJ)));
	}

	public Mode mode(ItemStack stack) {
		List<Mode> modes = modes();
		int i = data(stack).copyNbt().getInt("Mode");
		return modes.get(Math.floorMod(i, modes.size()));
	}

	public void setMode(ItemStack stack, Mode mode) {
		int i = modes().indexOf(mode);
		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, n -> n.putInt("Mode", Math.max(0, i)));
		stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, modeModifiers(mode));
	}

	/** Wide heads are slower per swing: player.block_break_speed, after Efficiency, as for the hammers. */
	public static AttributeModifiersComponent modeModifiers(Mode mode) {
		AttributeModifiersComponent mods = AttributeModifiersComponent.DEFAULT;
		if (mode.speed < 1) {
			mods = mods.with(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, new EntityAttributeModifier(
					Identifier.of(PeriodicElements.MOD_ID, "drill_mode"), mode.speed - 1,
					EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), AttributeModifierSlot.MAINHAND);
		}
		return mods;
	}

	public static ItemStack bit(ItemStack stack) {
		ContainerComponent c = stack.get(DataComponentTypes.CONTAINER);
		return c == null ? ItemStack.EMPTY : c.copyFirstStack();
	}

	/** Fits a bit (or none) and lends the drill the bit's enchantments, all but Mending. */
	public static void setBit(ItemStack stack, ItemStack bit) {
		if (bit.isEmpty()) {
			stack.remove(DataComponentTypes.CONTAINER);
			stack.remove(DataComponentTypes.ENCHANTMENTS);
			return;
		}
		stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(List.of(bit.copyWithCount(1))));
		ItemEnchantmentsComponent.Builder b = new ItemEnchantmentsComponent.Builder(
				bit.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT));
		b.remove(e -> e.matchesKey(Enchantments.MENDING));
		stack.set(DataComponentTypes.ENCHANTMENTS, b.build());
	}

	// ---- mining ----

	private boolean effective(BlockState state) {
		return kind == Kind.CHAINSAW ? state.isIn(BlockTags.AXE_MINEABLE) : state.isIn(BlockTags.PICKAXE_MINEABLE);
	}

	@Override
	public float getMiningSpeed(ItemStack stack, BlockState state) {
		if (!effective(state) || energy(stack) <= 0) {
			return 1f;
		}
		if (kind == Kind.CHAINSAW) {
			return CHAINSAW_SPEED;
		}
		ItemStack bit = bit(stack);
		return bit.getItem() instanceof DrillBitItem b ? b.speed() : 1f;
	}

	@Override
	public boolean isCorrectForDrops(ItemStack stack, BlockState state) {
		if (!effective(state) || energy(stack) <= 0) {
			return false;
		}
		if (kind == Kind.CHAINSAW) {
			return true;
		}
		return bit(stack).getItem() instanceof DrillBitItem b && !state.isIn(b.incorrect());
	}

	@Override
	public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
		if (world instanceof ServerWorld sw && miner instanceof ServerPlayerEntity p && effective(state)) {
			pay(stack, p, sw, state, state.getHardness(world, pos));
			afterSwing(stack, p, 1);
		}
		return true;
	}

	@Override
	public String unlock() {
		return "item:" + id;
	}

	@Override
	public List<BlockPos> targets(ItemStack stack, ServerPlayerEntity player, ServerWorld world, BlockPos origin, Direction side) {
		BlockState s = world.getBlockState(origin);
		return switch (mode(stack)) {
			case SINGLE -> List.of(origin);
			case TUNNEL -> AreaMining.tunnel(origin, side, player.getHorizontalFacing());
			case AREA3 -> AreaMining.square(origin, side, 1);
			case AREA5 -> AreaMining.square(origin, side, 2);
			case VEIN -> AreaMining.ore(s) ? AreaMining.connected(world, origin, x -> x.isOf(s.getBlock()), AreaMining.VEIN_MAX) : List.of(origin);
			case TREE -> AreaMining.log(s) ? AreaMining.connected(world, origin, AreaMining::log, AreaMining.TREE_MAX) : List.of(origin);
		};
	}

	@Override
	public boolean pay(ItemStack stack, ServerPlayerEntity player, ServerWorld world, BlockState state, float hardness) {
		if (player.getAbilities().creativeMode) {
			return true;
		}
		double cost = KJ_PER_HARDNESS * Math.max(0, hardness);
		double have = energy(stack);
		if (have < cost) {
			refill(stack, player);
			have = energy(stack);
			if (have < cost) {
				return false;
			}
		}
		setEnergy(stack, have - cost);
		return true;
	}

	/** One point of wear on the bit per swing (Unbreaking on the bit counts); a worn-out bit drops out. */
	@Override
	public void afterSwing(ItemStack stack, ServerPlayerEntity player, int broken) {
		if (kind == Kind.CHAINSAW || broken <= 0 || player.getAbilities().creativeMode) {
			return;
		}
		ItemStack bit = bit(stack);
		if (bit.isEmpty()) {
			return;
		}
		int wear = EnchantmentHelper.getItemDamage(player.getServerWorld(), bit, 1);
		if (wear <= 0) {
			return;
		}
		int dmg = bit.getDamage() + wear;
		if (dmg >= bit.getMaxDamage()) {
			setBit(stack, ItemStack.EMPTY);
			player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1f, 1f);
			player.sendMessage(Text.translatable("gui.periodic_elements.drill.bit_broken"), true);
		} else {
			bit.setDamage(dmg);
			stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(List.of(bit)));
		}
	}

	// ---- power ----

	/** Tops the drill's store up from the inventory: a Compressed Air cylinder, or battery packs (Li-ion first). */
	public void refill(ItemStack stack, PlayerEntity player) {
		double have = energy(stack);
		double room = capacity() - have;
		if (kind == Kind.PNEUMATIC) {
			if (room < CYLINDER_KJ) {
				return;
			}
			Item cyl = airCylinder();
			var inv = player.getInventory();
			for (int i = 0; i < inv.size(); i++) {
				ItemStack s = inv.getStack(i);
				if (cyl != null && s.isOf(cyl)) {
					s.decrement(1);
					inv.offerOrDrop(new ItemStack(Fluids.STEEL_CYLINDER));
					setEnergy(stack, have + CYLINDER_KJ);
					return;
				}
			}
			return;
		}
		if (room <= 1e-6) {
			return;
		}
		double got = 0;
		for (ItemStack pack : packs(player)) {
			got += BatteryPackItem.draw(pack, room - got);
			if (got >= room - 1e-9) {
				break;
			}
		}
		if (got > 0) {
			setEnergy(stack, have + got);
		}
	}

	public static Item airCylinder() {
		return Fluids.cylinderOf(Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, "compressed_air")));
	}

	/** The charged packs in the inventory, Lithium-ion first. */
	public static List<ItemStack> packs(PlayerEntity player) {
		var inv = player.getInventory();
		List<ItemStack> out = new java.util.ArrayList<>();
		for (boolean light : new boolean[]{true, false}) {
			for (int i = 0; i < inv.size(); i++) {
				ItemStack s = inv.getStack(i);
				if (s.getItem() instanceof BatteryPackItem p && p.light() == light && BatteryPackItem.energy(s) > 0) {
					out.add(s);
				}
			}
		}
		return out;
	}

	/** Does this drill weigh you down now? The rock drill always; the heavy drill unless a charged Li-ion pack drives it. */
	public boolean slows(PlayerEntity player) {
		return switch (kind) {
			case PNEUMATIC -> true;
			case HEAVY -> packs(player).stream().noneMatch(s -> ((BatteryPackItem) s.getItem()).light());
			default -> false;
		};
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (world.isClient || !selected || !(entity instanceof PlayerEntity player) || world.getTime() % 10 != 0) {
			return;
		}
		// Top up in one step once below half, so the held item is not re-sent every tick.
		if (energy(stack) < capacity() / 2) {
			refill(stack, player);
		}
		if (slows(player)) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 0, true, false, true));
		}
	}

	// ---- use: modes, bits, ore-seeking ----

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (hand != Hand.MAIN_HAND || !user.isSneaking()) {
			return TypedActionResult.pass(stack);
		}
		if (!world.isClient) {
			sneakUse(stack, user);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		PlayerEntity user = ctx.getPlayer();
		if (user == null || ctx.getHand() != Hand.MAIN_HAND) {
			return ActionResult.PASS;
		}
		ItemStack stack = ctx.getStack();
		if (user.isSneaking()) {
			if (!ctx.getWorld().isClient) {
				sneakUse(stack, user);
			}
			return ActionResult.success(ctx.getWorld().isClient);
		}
		if (kind == Kind.HEAVY && mode(stack) == Mode.VEIN) {
			if (ctx.getWorld() instanceof ServerWorld sw && user instanceof ServerPlayerEntity sp) {
				if (!AreaMining.knows(sp, "item:ore_seeking")) {
					sp.sendMessage(Text.translatable("gui.periodic_elements.drill.needs", AreaMining.projectName("item:ore_seeking")), true);
				} else {
					survey(sp, sw, ctx.getBlockPos());
				}
			}
			return ActionResult.success(ctx.getWorld().isClient);
		}
		return ActionResult.PASS;
	}

	private void sneakUse(ItemStack stack, PlayerEntity user) {
		ItemStack off = user.getOffHandStack();
		if (kind != Kind.CHAINSAW && off.getItem() instanceof DrillBitItem) {
			ItemStack old = bit(stack);
			setBit(stack, off.copyWithCount(1));
			off.decrement(1);
			if (!old.isEmpty()) {
				user.getInventory().offerOrDrop(old);
			}
			user.sendMessage(Text.translatable("gui.periodic_elements.drill.bit_fitted", bit(stack).getName()), true);
			return;
		}
		List<Mode> modes = modes();
		Mode next = modes.get((modes.indexOf(mode(stack)) + 1) % modes.size());
		setMode(stack, next);
		user.sendMessage(Text.translatable("gui.periodic_elements.drill.mode",
				Text.translatable("gui.periodic_elements.drill.mode." + next.name().toLowerCase(Locale.ROOT))), true);
	}

	/** Ore-Seeking (L7): the vein's size and the elements in it, read before digging. */
	public static int survey(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
		BlockState s = world.getBlockState(pos);
		if (!AreaMining.ore(s)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.drill.vein.none"), true);
			return 0;
		}
		List<BlockPos> vein = AreaMining.connected(world, pos, x -> x.isOf(s.getBlock()), AreaMining.VEIN_MAX);
		Map<String, Double> total = new TreeMap<>();
		for (BlockPos p : vein) {
			ResearchedToolItem.elementsOfBlock(world.getBlockState(p)).forEach((k, v) -> total.merge(k, v, Double::sum));
		}
		double sum = total.values().stream().mapToDouble(Double::doubleValue).sum();
		MutableText line = Text.empty();
		int i = 0;
		for (var e : total.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).toList()) {
			if (i >= 6 || sum <= 0) {
				break;
			}
			if (i++ > 0) {
				line.append(", ");
			}
			line.append(Text.translatable("item." + PeriodicElements.MOD_ID + "." + e.getKey()))
					.append(String.format(Locale.ROOT, " %.1f%%", 100 * e.getValue() / sum));
		}
		player.sendMessage(Text.translatable("gui.periodic_elements.drill.vein", vein.size(), s.getBlock().getName(), line), false);
		return vein.size();
	}

	// ---- display ----

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		return (int) Math.round(13 * MathHelper.clamp(energy(stack) / capacity(), 0, 1));
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return kind == Kind.PNEUMATIC ? 0x9FC8E8 : 0x50C878;
	}

	@Override
	public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
		return false;
	}

	@Override
	public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("gui.periodic_elements.drill.mode",
				Text.translatable("gui.periodic_elements.drill.mode." + mode(stack).name().toLowerCase(Locale.ROOT))).formatted(Formatting.YELLOW));
		if (kind != Kind.CHAINSAW) {
			ItemStack bit = bit(stack);
			tooltip.add(bit.isEmpty() ? Text.translatable("gui.periodic_elements.drill.no_bit").formatted(Formatting.RED)
					: Text.translatable("gui.periodic_elements.drill.bit", Text.empty().append(bit.getName())
					.append(String.format(Locale.ROOT, " (%d/%d)", bit.getMaxDamage() - bit.getDamage(), bit.getMaxDamage()))).formatted(Formatting.AQUA));
		}
		tooltip.add(Text.translatable(kind == Kind.PNEUMATIC ? "gui.periodic_elements.drill.air" : "gui.periodic_elements.drill.energy",
				(int) energy(stack), (int) capacity()).formatted(Formatting.GREEN));
	}
}
