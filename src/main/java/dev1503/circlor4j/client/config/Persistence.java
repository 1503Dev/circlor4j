package dev1503.circlor4j.client.config;

import dev1503.circlor4j.client.keybind.KeyBindManager;
import dev1503.circlor4j.client.module.ModuleManager;
import dev1503.circlor4j.client.module.modules.LanguageModule;
import dev1503.circlor4j.ui.StatusManager;

/** One-time deferred load of persisted status/keybinds, once the Minecraft instance exists. */
public final class Persistence {
	private static boolean loaded;

	private Persistence() {
	}

	public static void ensureLoaded() {
		if (loaded) {
			return;
		}
		loaded = true;
		StatusManager.getInstance().load();
		ModuleManager.syncEnabledFromStatus();
		LanguageModule.applyFromStatus();
		KeyBindManager.load();
	}
}
