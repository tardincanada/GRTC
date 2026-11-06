package com.periodicelements.test;

import com.periodicelements.explore.BackpackItem;
import com.periodicelements.explore.Explore;
import com.periodicelements.explore.ExplorerLightBlock;
import com.periodicelements.explore.ExplorerCompassItem;
import com.periodicelements.explore.StreetLampBlock;
import com.periodicelements.explore.TrapEntity;
import com.periodicelements.explore.TrapItem;
import com.periodicelements.mining.BatteryPackItem;
import com.periodicelements.research.Research;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/** Exploration gear (3.47.0): research gates, head lamps and goggles, street lamps, tent, backpacks, compass, traps. */
public class ExplorationTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static Item item(String path) {
		return Registries.ITEM.get(Identifier.of("periodic_elements", path));
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void eachItemNeedsItsResearch(TestContext ctx) {
		Map<String, Integer> levels = Map.ofEntries(Map.entry("tent", 1), Map.entry("leather_backpack", 1),
				Map.entry("rope_net", 1), Map.entry("explorer_compass", 2), Map.entry("steel_cage", 3),
				Map.entry("electric_cap_lamp", 5), Map.entry("sodium_lamp", 5), Map.entry("night_vision_goggles", 6),
				Map.entry("nylon_backpack", 6), Map.entry("led_lamp", 7), Map.entry("led_spotlight_helmet", 7));
		for (var e : levels.entrySet()) {
			String gate = Explore.gate(new ItemStack(item(e.getKey())));
			expect(ctx, ("item:" + e.getKey()).equals(gate), e.getKey() + " has gate " + gate);
			String project = Research.unlockedBy(gate);
			expect(ctx, project != null, "no research unlocks " + gate);
			expect(ctx, Research.get(project).level() == e.getValue(), e.getKey() + " should be level " + e.getValue()
					+ ", is " + Research.get(project).level());
		}
		expect(ctx, Explore.gate(new ItemStack(Items.COBBLESTONE)) == null, "cobblestone should need no research");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void capLampLightsAndDrawsTheBattery(TestContext ctx) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false;
		player.getInventory().clear();
		ItemStack pack = new ItemStack(item("lead_acid_pack"));
		BatteryPackItem.setEnergy(pack, 10);
		player.getInventory().setStack(9, pack);
		player.equipStack(EquipmentSlot.HEAD, new ItemStack(Explore.CAP_LAMP));
		Explore.tickPlayer(ctx.getWorld(), player);
		ItemStack lamp = player.getEquippedStack(EquipmentSlot.HEAD);
		expect(ctx, Explore.powered(lamp), "a lamp on a charged pack should be on");
		double left = BatteryPackItem.energy(player.getInventory().getStack(9));
		expect(ctx, Math.abs(left - (10 - Explore.CAP_LAMP_KJ / 2)) < 1e-6, "half a second should take 0.125 kJ, left " + left);
		BlockPos head = BlockPos.ofFloored(player.getEyePos());
		expect(ctx, ctx.getWorld().getBlockState(head).isOf(Explore.LIGHT)
				|| ctx.getWorld().getBlockState(player.getBlockPos()).isOf(Explore.LIGHT), "the lamp should light the player's block");
		// Flat: no charge, no light.
		BatteryPackItem.setEnergy(player.getInventory().getStack(9), 0);
		Explore.tickPlayer(ctx.getWorld(), player);
		expect(ctx, !Explore.powered(player.getEquippedStack(EquipmentSlot.HEAD)), "a lamp on a flat pack should be off");
		ctx.complete();
	}

	/** Fiat Lux P1 (3.48.1): the worn-lamp lights draw a model (shaders see them) and say which lamp lit them. */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void lampLightsAreVisibleToShadersAndTellTheirKind(TestContext ctx) {
		var world = ctx.getWorld();
		expect(ctx, Explore.LIGHT.getDefaultState().getRenderType() == net.minecraft.block.BlockRenderType.MODEL,
				"explorer_light should render a model");
		expect(ctx, com.periodicelements.farm.Industry.LAMP_LIGHT.getDefaultState().getRenderType()
				== net.minecraft.block.BlockRenderType.MODEL, "lamp_light should render a model");
		expect(ctx, Explore.LIGHT.getDefaultState().getLuminance() == 15, "explorer_light should give light 15");
		expect(ctx, Explore.LIGHT.getDefaultState().isReplaceable(), "explorer_light should stay replaceable");
		BlockPos at = ctx.getAbsolutePos(new BlockPos(1, 2, 1));
		ExplorerLightBlock.lightAt(world, at, null, ExplorerLightBlock.Kind.CAP);
		expect(ctx, world.getBlockState(at).get(ExplorerLightBlock.KIND) == ExplorerLightBlock.Kind.CAP, "cap lamp light should be kind=cap");
		ExplorerLightBlock.lightAt(world, at, null, ExplorerLightBlock.Kind.LED);
		expect(ctx, world.getBlockState(at).get(ExplorerLightBlock.KIND) == ExplorerLightBlock.Kind.LED, "LED helmet light should be kind=led");
		ctx.complete();
	}

	/** Fiat Lux P2 (3.49.1): every block state of the mod that gives light is in lights.json, and every entry exists. */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void lightsJsonListsEveryLuminousBlock(TestContext ctx) {
		com.google.gson.JsonObject json;
		try (var in = ExplorationTests.class.getResourceAsStream("/periodic_elements/lights.json")) {
			expect(ctx, in != null, "lights.json is missing");
			json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
					.getAsJsonObject();
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		var kinds = json.getAsJsonObject("kinds");
		java.util.Map<String, java.util.List<String>> blockStates = new java.util.HashMap<>();
		for (var el : json.getAsJsonArray("lights")) {
			var o = el.getAsJsonObject();
			Identifier id = Identifier.of(o.get("id").getAsString());
			expect(ctx, kinds.has(o.get("kind").getAsString()), id + " has unknown kind " + o.get("kind"));
			boolean item = o.has("item") && o.get("item").getAsBoolean();
			if (item) {
				expect(ctx, Registries.ITEM.containsId(id), "lights.json lists missing item " + id);
				continue;
			}
			expect(ctx, Registries.BLOCK.containsId(id), "lights.json lists missing block " + id);
			blockStates.computeIfAbsent(id.toString(), k -> new java.util.ArrayList<>()).add(o.has("state") ? o.get("state").getAsString() : "");
		}
		for (var block : Registries.BLOCK) {
			Identifier id = Registries.BLOCK.getId(block);
			if (!id.getNamespace().equals("periodic_elements")) {
				continue;
			}
			for (var state : block.getStateManager().getStates()) {
				if (state.getLuminance() == 0) {
					continue;
				}
				var listed = blockStates.get(id.toString());
				expect(ctx, listed != null, "block " + id + " gives light but is not in lights.json");
				boolean match = listed.stream().anyMatch(want -> want.isEmpty() || java.util.Arrays.stream(want.split(",")).allMatch(kv -> {
					String[] p = kv.split("=");
					var prop = block.getStateManager().getProperty(p[0]);
					return prop != null && net.minecraft.util.Util.getValueAsString(prop, state.get(prop)).equals(p[1]);
				}));
				expect(ctx, match, "state " + state + " gives light but no lights.json entry covers it");
			}
		}
		ctx.complete();
	}

	/** Fiat Lux P4 (3.49.2) + P5 (3.49.3): rain sound and splashes stop above the shader's top cloud layer, fixed at Y 272. */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void rainFadesAboveTheTopCloudLayer(TestContext ctx) {
		expect(ctx, com.periodicelements.weather.CloudCeiling.top() == 272, "top layer should be Y 272");
		expect(ctx, com.periodicelements.weather.CloudCeiling.volume(272) == 1f, "full volume at the top layer");
		expect(ctx, Math.abs(com.periodicelements.weather.CloudCeiling.volume(284) - 0.5f) < 1e-6, "half volume 12 above");
		expect(ctx, com.periodicelements.weather.CloudCeiling.volume(296) == 0f, "silent 24 above");
		expect(ctx, com.periodicelements.weather.CloudCeiling.splashes(272) && !com.periodicelements.weather.CloudCeiling.splashes(273),
				"no splashes above the top layer");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void gogglesGiveNightVision(TestContext ctx) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.equipStack(EquipmentSlot.HEAD, new ItemStack(Explore.GOGGLES));
		Explore.tickPlayer(ctx.getWorld(), player);
		expect(ctx, player.hasStatusEffect(StatusEffects.NIGHT_VISION), "goggles should give night vision");
		expect(ctx, Explore.worn(player, com.periodicelements.explore.HeadGearItem.Kind.GOGGLES) != null, "goggles should be on");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void streetLampOnAFencePostLightsFromItsFoot(TestContext ctx) {
		BlockPos foot = new BlockPos(1, 1, 1);
		for (int i = 0; i < 3; i++) {
			ctx.setBlockState(foot.up(i), Blocks.OAK_FENCE);
		}
		BlockPos lampRel = foot.up(3);
		ctx.setBlockState(lampRel, Explore.SODIUM_LAMP);
		// A charged battery beside the foot of the post, through one copper wire.
		ctx.setBlockState(foot.east(), Registries.BLOCK.get(Identifier.of("periodic_elements", "copper_wire")));
		ctx.setBlockState(foot.east(2), Registries.BLOCK.get(Identifier.of("periodic_elements", "lithium_ion_battery")));
		NbtCompound nbt = new NbtCompound();
		nbt.putDouble("Stored", 5000);
		ctx.getWorld().getBlockEntity(ctx.getAbsolutePos(foot.east(2))).read(nbt, ctx.getWorld().getRegistryManager());
		BlockPos lamp = ctx.getAbsolutePos(lampRel);
		ctx.waitAndRun(45, () -> {
			expect(ctx, ctx.getWorld().getBlockState(lamp).get(StreetLampBlock.LIT), "the lamp should be lit from the wire at its foot");
			expect(ctx, Explore.LitLamps.covers(ctx.getWorld(), lamp.add(20, -3, 5)), "20 blocks away should be covered");
			expect(ctx, !Explore.LitLamps.covers(ctx.getWorld(), lamp.add(30, 0, 0)), "30 blocks away is past a sodium lamp");
			expect(ctx, !HostileEntity.canSpawnInDark(EntityType.ZOMBIE, ctx.getWorld(), SpawnReason.NATURAL, lamp.add(10, -3, 0),
					ctx.getWorld().getRandom()), "no zombie should spawn by a lit lamp");
			ctx.setBlockState(lampRel, Blocks.AIR);
			expect(ctx, !Explore.LitLamps.covers(ctx.getWorld(), lamp.add(5, 0, 0)), "a broken lamp should cover nothing");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void unpoweredLampStaysDark(TestContext ctx) {
		BlockPos rel = new BlockPos(1, 2, 1);
		ctx.setBlockState(rel.down(), Blocks.COBBLESTONE_WALL);
		ctx.setBlockState(rel, Explore.LED_LAMP);
		ctx.waitAndRun(30, () -> {
			expect(ctx, !ctx.getWorld().getBlockState(ctx.getAbsolutePos(rel)).get(StreetLampBlock.LIT), "a lamp with no wire stays dark");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void tentIsABedThatKeepsTheSpawn(TestContext ctx) {
		BlockPos rel = new BlockPos(1, 1, 1);
		ctx.setBlockState(rel, Explore.TENT);
		BlockPos pos = ctx.getAbsolutePos(rel);
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		var state = ctx.getWorld().getBlockState(pos);
		expect(ctx, EntitySleepEvents.ALLOW_BED.invoker().allowBed(player, pos, state, false) == ActionResult.SUCCESS,
				"a tent should count as a bed");
		expect(ctx, !EntitySleepEvents.ALLOW_SETTING_SPAWN.invoker().allowSettingSpawn(player, pos),
				"sleeping in a tent should not set the spawn");
		expect(ctx, EntitySleepEvents.ALLOW_SETTING_SPAWN.invoker().allowSettingSpawn(player, pos.east(3)),
				"other beds still set the spawn");
		expect(ctx, Explore.TENT.getDefaultState().getCollisionShape(ctx.getWorld(), pos).getMax(net.minecraft.util.math.Direction.Axis.Y) < 0.2,
				"you should walk into a tent");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void backpackTakesSpecimensOnly(TestContext ctx) {
		expect(ctx, ((BackpackItem) Explore.LEATHER_BACKPACK).slots() == 27, "leather backpack: 27 places");
		expect(ctx, ((BackpackItem) Explore.NYLON_BACKPACK).slots() == 54, "nylon backpack: 54 places");
		expect(ctx, BackpackItem.isSpecimen(new ItemStack(Items.OAK_SAPLING)), "a sapling is a specimen");
		expect(ctx, BackpackItem.isSpecimen(new ItemStack(Items.WHEAT_SEEDS)), "seeds are specimens");
		expect(ctx, BackpackItem.isSpecimen(new ItemStack(Items.RAW_IRON)), "raw iron is a specimen");
		expect(ctx, BackpackItem.isSpecimen(new ItemStack(Items.DIAMOND)), "a diamond is a specimen");
		expect(ctx, !BackpackItem.isSpecimen(new ItemStack(Items.COBBLESTONE)), "cobblestone is not a specimen");
		expect(ctx, !BackpackItem.accepts(new ItemStack(Explore.NYLON_BACKPACK)), "no backpack in a backpack");
		expect(ctx, !BackpackItem.accepts(new ItemStack(Items.SHULKER_BOX)), "no shulker box in a backpack");
		expect(ctx, BackpackItem.accepts(new ItemStack(Items.IRON_PICKAXE)), "tools go in a backpack");
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.getInventory().clear();
		player.getInventory().setStack(20, new ItemStack(Explore.LEATHER_BACKPACK));
		ItemStack saplings = new ItemStack(Items.BIRCH_SAPLING, 10);
		expect(ctx, BackpackItem.absorb(player, saplings) == 10 && saplings.isEmpty(), "the backpack should take 10 saplings");
		ItemStack stone = new ItemStack(Items.COBBLESTONE, 10);
		expect(ctx, BackpackItem.absorb(player, stone) == 0, "the backpack should leave cobblestone");
		ItemStack pack = player.getInventory().getStack(20);
		pack.set(Explore.PICKUP, false);
		ItemStack more = new ItemStack(Items.BIRCH_SAPLING, 5);
		expect(ctx, BackpackItem.absorb(player, more) == 0, "a backpack switched off picks up nothing");
		long saplingsIn = BackpackItem.contents(pack).stream().filter(s -> s.isOf(Items.BIRCH_SAPLING)).mapToInt(ItemStack::getCount).sum();
		expect(ctx, saplingsIn == 10, "10 saplings should be inside, " + saplingsIn + " are");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void compassFindsAChosenBiome(TestContext ctx) {
		var world = ctx.getWorld();
		var biomes = ExplorerCompassItem.biomes(world);
		expect(ctx, !biomes.isEmpty(), "the world should have biomes to choose");
		ItemStack compass = new ItemStack(Explore.COMPASS);
		Identifier here = world.getBiome(ctx.getAbsolutePos(BlockPos.ORIGIN)).getKey().orElseThrow().getValue();
		for (int i = 0; i < biomes.size() && !here.toString().equals(compass.get(Explore.COMPASS_BIOME)); i++) {
			ExplorerCompassItem.next(world, compass);
		}
		expect(ctx, here.toString().equals(compass.get(Explore.COMPASS_BIOME)), "the compass should offer " + here);
		BlockPos found = ExplorerCompassItem.locate(world, ctx.getAbsolutePos(BlockPos.ORIGIN), compass);
		expect(ctx, found != null && compass.get(Explore.COMPASS_TARGET) != null, "the compass should find the biome it stands in");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "exploration")
	public void trapsCatchAndRelease(TestContext ctx) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		TrapItem ropeNet = (TrapItem) Explore.ROPE_NET, cage = (TrapItem) Explore.STEEL_CAGE;
		ChickenEntity chicken = ctx.spawnEntity(EntityType.CHICKEN, 1, 2, 1);
		CowEntity cow = ctx.spawnEntity(EntityType.COW, 2, 2, 2);
		Entity zombie = ctx.spawnEntity(EntityType.ZOMBIE, 3, 2, 1);
		Entity villager = ctx.spawnEntity(EntityType.VILLAGER, 1, 2, 3);
		expect(ctx, ropeNet.canHold(chicken, player), "a net holds a chicken");
		expect(ctx, !ropeNet.canHold(cow, player), "a net does not hold a cow");
		expect(ctx, cage.canHold(cow, player), "a cage holds a cow");
		expect(ctx, !cage.canHold(zombie, player) && !cage.canHold(villager, player), "no trap holds a zombie or a villager");
		chicken.setCustomName(net.minecraft.text.Text.literal("Clucky"));
		ItemStack trap = new ItemStack(Explore.ROPE_NET);
		expect(ctx, TrapEntity.tryCatch(trap, chicken, player), "the net should catch the chicken");
		expect(ctx, chicken.isRemoved() && TrapItem.full(trap) && trap.getDamage() == 1, "the chicken is in the net, which wore by one");
		expect(ctx, !TrapEntity.tryCatch(trap, cow, player), "a full trap catches nothing more");
		Vec3d at = Vec3d.ofBottomCenter(ctx.getAbsolutePos(new BlockPos(3, 2, 3)));
		Entity out = TrapEntity.release(ctx.getWorld(), trap, at, 0f);
		expect(ctx, out instanceof ChickenEntity && out.isAlive() && "Clucky".equals(out.getCustomName().getString()),
				"the same chicken should come out");
		expect(ctx, out.getPos().distanceTo(at) < 0.01 && !TrapItem.full(trap), "it comes out where the trap lands, and the trap is empty");
		ItemStack box = new ItemStack(Explore.STEEL_CAGE);
		expect(ctx, TrapEntity.tryCatch(box, cow, player) && box.getDamage() == 0, "the cage catches the cow and does not wear");
		ctx.complete();
	}
}
