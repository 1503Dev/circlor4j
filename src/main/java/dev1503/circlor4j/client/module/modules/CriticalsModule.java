package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/**
 * Criticals: before attacking, sends fake-fall position packets so the server registers
 * every attack as a critical hit (TrollHack Criticals style).
 */
public class CriticalsModule extends Module {
	public static final String ID = "criticals";

	public CriticalsModule(StatusManager status) {
		super(status, ID, "Criticals", "Makes every attack a critical hit", ModuleCategory.COMBAT);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
