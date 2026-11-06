package com.periodicelements;

import com.periodicelements.machine.Reactions;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchCommand;
import com.periodicelements.research.ResearchState;
import com.periodicelements.research.ResearchSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PeriodicElements implements ModInitializer {
	public static final String MOD_ID = "periodic_elements";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** All element items in atomic-number order. */
	public static final List<ElementItem> ELEMENTS = new ArrayList<>();
	/** All substance items, simplest first. */
	public static final List<SubstanceItem> SUBSTANCES = new ArrayList<>();

	@Override
	public void onInitialize() {
		Fluids.registerContainers();
		for (Element element : ElementTable.load()) {
			Item.Settings settings = Fluids.settings(element.id());
			ElementItem item = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, element.id()),
					new ElementItem(element, settings));
			ELEMENTS.add(item);
		}


		for (Substance substance : ElementTable.loadSubstances()) {
			SubstanceItem item = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, substance.id()),
					new SubstanceItem(substance, Fluids.settings(substance.id())));
			SUBSTANCES.add(item);
		}


		Fluids.registerCylinders();
		BlockComposition.load();
		Research.load();
		Reactions.load();
		ModContent.register();
		com.periodicelements.worldgen.GeoWorld.register();
		com.periodicelements.radiation.Radiation.register();
		com.periodicelements.radiation.Decay.register();
		com.periodicelements.radiation.Decay.load();
		com.periodicelements.radiation.Isotopes.register();
		com.periodicelements.radiation.Isotopes.load();

		PayloadTypeRegistry.playS2C().register(ResearchSyncPayload.ID, ResearchSyncPayload.CODEC);
		com.periodicelements.atlas.Atlas.init();
		com.periodicelements.compendium.Compendium.init();
		com.periodicelements.research.ProgressBook.init();
		com.periodicelements.creative.CreativeItems.init();
		// 3.51.0: last, once every item is registered.
		com.periodicelements.creative.CreativeTabs.register();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				ResearchState.get(server).sync(handler.getPlayer()));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				{ ResearchCommand.register(dispatcher); com.periodicelements.compendium.CompendiumCommand.register(dispatcher); });

		LOGGER.info("Registered {} elements and {} substances; {} block compositions",
				ELEMENTS.size(), SUBSTANCES.size(), BlockComposition.size());
		LOGGER.info("Loaded {} research projects, {} reactions, {} machines, {} research stations and {} ores",
				Research.all().size(), Reactions.all().size(), ModContent.MACHINES.size(), ModContent.STATIONS.size(), OreContent.ORES.size());
	}
}
