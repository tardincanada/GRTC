package com.periodicelements.farm;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;

import java.util.List;

/**
 * Life sciences (2.0.0): Insulation, the Shaped Charge and ANFO Charge, the four sprayers and Tolerant Seeds, and
 * the ticking of treated ground. Assets and data come from tools/industry.py.
 */
public final class Life {
	/** DDT on the ground: insects there take this much every second. */
	public static final float DDT_DAMAGE = 2f;
	/** A chicken on DDT ground has its egg timer held back this many ticks a second: it lays half as often. */
	public static final int EGG_DELAY = 10;

	public static Block INSULATION;
	public static Block SHAPED_CHARGE;
	public static Block ANFO_CHARGE;
	public static Item DDT_DUSTER;
	public static Item WEED_KILLER;
	public static Item PRE_EMERGENT;
	public static Item FIELD_CLEAR;
	public static Item TOLERANT_SEEDS;

	private Life() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	private static Block gated(String name, Block block, String project, List<Item> items) {
		Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new GatedBlockItem(block, new Item.Settings(),
				known -> Research.allows(known, "item:" + name), Research.get(project).name())));
		return block;
	}

	private static Item item(String name, Item item, List<Item> items) {
		items.add(Registry.register(Registries.ITEM, id(name), item));
		return item;
	}

	public static void register(List<Item> items) {
		INSULATION = Registry.register(Registries.BLOCK, id("insulation"), new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.WHITE).strength(0.6f).sounds(BlockSoundGroup.WOOL)));
		items.add(Registry.register(Registries.ITEM, id("insulation"), new BlockItem(INSULATION, new Item.Settings())));
		SHAPED_CHARGE = gated("shaped_charge", new ShapedChargeBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(1.0f).sounds(BlockSoundGroup.METAL)), "plastic_explosive", items);
		ANFO_CHARGE = gated("anfo_charge", new AnfoChargeBlock(AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW)
				.strength(0.5f).sounds(BlockSoundGroup.WOOL)), "anfo", items);
		DDT_DUSTER = item("ddt_duster", new SprayerItem(new Item.Settings().maxDamage(16), SprayerItem.Kind.DDT), items);
		WEED_KILLER = item("weed_killer", new SprayerItem(new Item.Settings().maxDamage(16), SprayerItem.Kind.WEED_KILLER), items);
		PRE_EMERGENT = item("pre_emergent", new SprayerItem(new Item.Settings().maxDamage(16), SprayerItem.Kind.PRE_EMERGENT), items);
		FIELD_CLEAR = item("field_clear", new SprayerItem(new Item.Settings().maxDamage(16), SprayerItem.Kind.FIELD_CLEAR), items);
		TOLERANT_SEEDS = item("tolerant_seeds", new TolerantSeedsItem(new Item.Settings()), items);

		for (String c : new String[]{"spent_mycelium", "biomass", "polylactic_acid"}) {
			CompostingChanceRegistry.INSTANCE.add(Registries.ITEM.get(id(c)), 0.65f);
		}
		CompostingChanceRegistry.INSTANCE.add(TOLERANT_SEEDS, 0.3f);
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
			if (world instanceof ServerWorld sw && state.isOf(Blocks.WHEAT)) {
				FieldState.of(sw).forget(pos);
			}
		});
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getTime() % 20 == 0) {
				tickFields(world);
			}
		});
	}

	/** Once a second: DDT ground harms insects and slows chickens; tolerant wheat grows a tenth faster. */
	public static void tickFields(ServerWorld world) {
		FieldState fields = FieldState.of(world);
		long now = world.getTime();
		fields.expire(now);
		for (FieldState.Area a : fields.areas()) {
			if (a.kind() != FieldState.Kind.DDT || !world.isChunkLoaded(ChunkPos.toLong(a.center().getX() >> 4, a.center().getZ() >> 4))) {
				continue;
			}
			Box box = new Box(a.center()).expand(a.radius());
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, box, InsectSprayItem::pest)) {
				e.damage(world.getDamageSources().magic(), DDT_DAMAGE);
			}
			for (ChickenEntity c : world.getEntitiesByClass(ChickenEntity.class, box, c -> !c.isBaby())) {
				c.eggLayTime += EGG_DELAY; // Silent Spring: thin shells, fewer eggs
			}
		}
		if (fields.tolerantCrops().isEmpty()) {
			return;
		}
		double chance = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED) / 4096.0 * 20 * TolerantSeedsItem.EXTRA_GROWTH;
		var it = fields.tolerantCrops().iterator();
		while (it.hasNext()) {
			BlockPos p = BlockPos.fromLong(it.next());
			if (!world.isChunkLoaded(ChunkPos.toLong(p.getX() >> 4, p.getZ() >> 4))) {
				continue;
			}
			BlockState s = world.getBlockState(p);
			if (!s.isOf(Blocks.WHEAT)) {
				it.remove();
				fields.markDirty();
			} else if (s.hasRandomTicks() && world.random.nextDouble() < chance) {
				s.randomTick(world, p, world.random);
			}
		}
	}
}
