package com.periodicelements.test;

import com.periodicelements.crops.Crops;
import com.periodicelements.shrubs.ShrubBlock;
import com.periodicelements.shrubs.Shrubs;
import com.periodicelements.trees.Trees;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Dev-only (./gradlew runBoneMealCheck): a real client right-clicks mod plants with bone meal and logs what grew. */
public class BoneMealClientCheck implements ClientModInitializer {
	private int tick = -1, worldTick = -1;
	private final List<BlockPos> targets = new ArrayList<>();
	private final List<String> names = new ArrayList<>();
	private final List<BlockState> before = new ArrayList<>();
	private final List<String> biomes = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.bonemealcheck")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
		net.minecraft.util.Identifier early = net.minecraft.util.Identifier.of("pe_test", "early");
		net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.addPhaseOrdering(early, net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE);
		net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register(early, (p, w, h, hit) -> {
			if (!w.isClient) {
				System.out.println("[BONEMEAL] server got the click at " + hit.getBlockPos().toShortString() + " holding " + p.getStackInHand(h));
			}
			return net.minecraft.util.ActionResult.PASS;
		});
	}

	private void step(MinecraftClient client) {
		if (client.world == null) {
			if (client.getOverlay() != null || client.currentScreen == null) {
				return;
			}
			tick++;
			if (tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("bonemeal" + System.currentTimeMillis());
				wc.setGameMode(WorldCreator.Mode.SURVIVAL);
				wc.setCheatsEnabled(true);
				press(client, net.minecraft.text.Text.translatable("selectWorld.create").getString());
			}
			return;
		}
		if (client.player == null) {
			return;
		}
		worldTick++;
		BlockPos p0 = BlockPos.ofFloored(client.player.getPos()).withY(150);
		if (worldTick == 40) {
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "gamerule randomTickSpeed 0");
			cmd(client, "time set 6000");
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:air", p0.getX() - 8, 140, p0.getZ() - 8, p0.getX() + 8, 190, p0.getZ() + 8));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:grass_block", p0.getX() - 8, 149, p0.getZ() - 8, p0.getX() + 8, 149, p0.getZ() + 8));
			cmd(client, String.format("tp @s %d 150 %d", p0.getX(), p0.getZ()));
		}
		if (worldTick == 60) {
			var server = client.getServer();
			BlockPos c = p0;
			server.execute(() -> {
				var w = server.getOverworld();
				int i = 0;
				for (var e : Crops.BLOCKS.entrySet()) {
					if (i >= 3) break;
					BlockPos p = c.add(-2 + i * 2, 0, 2);
					w.setBlockState(p.down(), Blocks.FARMLAND.getDefaultState());
					w.setBlockState(p, e.getValue().getDefaultState());
					add(p, "crop " + e.getKey(), w.getBlockState(p), null);
					i++;
				}
				ShrubBlock bush = Shrubs.ALL.get(0);
				BlockPos sp = c.add(2, 0, -2);
								w.setBlockState(sp, bush.getDefaultState().with(ShrubBlock.AGE, 0));
				add(sp, "shrub " + Registries.BLOCK.getId(bush), w.getBlockState(sp), bushBiome(bush));
				BlockPos wp = c.add(-3, 0, 1);
				w.setBlockState(wp, Trees.ALL.get(0).sapling().getDefaultState());
				add(wp, "sapling " + Trees.ALL.get(0).id() + " OUT of its biome (plains)", w.getBlockState(wp), "plains");
				BlockPos ws = c.add(3, 0, 0);
				w.setBlockState(ws, bush.getDefaultState().with(ShrubBlock.AGE, 0));
				add(ws, "shrub OUT of its biome (snowy_plains)", w.getBlockState(ws), "snowy_plains");
				for (int k = 0; k < 2; k++) {
					Trees.Species s = Trees.ALL.get(k * 40);
					BlockPos tp = k == 0 ? c.add(-3, 0, -2) : c.add(0, 0, -3);
										w.setBlockState(tp, s.sapling().getDefaultState());
					add(tp, "sapling " + s.id() + " in " + s.biomes().get(0), w.getBlockState(tp), s.biomes().get(0));
				}
			});
		}
		int t = worldTick - 80;
		if (t >= 0 && !targets.isEmpty() && t / 80 < targets.size()) {
			int k = t / 80, o = t % 80;
			BlockPos p = targets.get(k);
			var server = client.getServer();
			if (o == 0 && biomes.get(k) != null) {
				String b = biomes.get(k);
				server.execute(() -> fillbiome(server, p, b));
			}
			if (o >= 10 && o % 5 == 0) {
				client.player.getInventory().setStack(client.player.getInventory().selectedSlot, new ItemStack(Items.BONE_MEAL, 64));
				cmd(client, "item replace entity @s weapon.mainhand with minecraft:bone_meal 64");
				var r = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND,
						new BlockHitResult(Vec3d.ofCenter(p), Direction.UP, p, false));
				if (o == 10) {
					System.out.println("[BONEMEAL] click " + names.get(k) + " -> client " + r);
				}
			}
			if (o == 79) {
				server.execute(() -> System.out.println("[BONEMEAL] " + names.get(k) + ": before " + before.get(k) + " after "
						+ server.getOverworld().getBlockState(p) + " biome " + server.getOverworld().getBiome(p).getIdAsString()));
			}
			return;
		}
		if (t > 0 && !targets.isEmpty() && t / 80 >= targets.size() && t % 80 == 20) {
			client.scheduleStop();
		}
	}

	private void add(BlockPos p, String name, BlockState st, String biome) {
		biomes.add(biome);
		targets.add(p);
		names.add(name);
		before.add(st);
	}

	@SuppressWarnings("unchecked")
	private static String bushBiome(ShrubBlock bush) {
		try {
			var f = ShrubBlock.class.getDeclaredField("biomeIds");
			f.setAccessible(true);
			return ((List<String>) f.get(bush)).get(0);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private static void fillbiome(net.minecraft.server.MinecraftServer server, BlockPos p, String biome) {
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent().withLevel(4),
				String.format("fillbiome %d %d %d %d %d %d minecraft:%s", p.getX() - 6, p.getY() - 2, p.getZ() - 6,
						p.getX() + 6, p.getY() + 30, p.getZ() + 6, biome));
		com.periodicelements.shrubs.Greenhouse.forget();
	}

	private static void press(MinecraftClient client, String label) {
		client.currentScreen.children().stream().filter(c -> c instanceof ButtonWidget b && b.getMessage().getString().equals(label))
				.findFirst().map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
	}

	private static void cmd(MinecraftClient client, String command) {
		var server = client.getServer();
		UUID id = client.player.getUuid();
		server.execute(() -> {
			var player = server.getPlayerManager().getPlayer(id);
			server.getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(4), command);
		});
	}
}
