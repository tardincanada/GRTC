package com.periodicelements.compat.modmenu;

import com.periodicelements.client.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Mod Menu's "Configure" button opens the mod's settings (3.31.0). Loaded only through the "modmenu" entrypoint. */
public class PeModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreen::new;
	}
}
