package com.strefiz.multicore.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu integration: exposes the configuration screen behind the gear button.
 *
 * <p>Mod Menu is an optional dependency ({@code compileOnly} + {@code localRuntime}), so this class
 * is only ever loaded when Mod Menu is present.
 */
public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return MultiCoreConfigScreen::new;
	}
}
