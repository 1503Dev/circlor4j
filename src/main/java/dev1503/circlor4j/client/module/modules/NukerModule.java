package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/**
 * Nuker: when the player destroys a block, every block within the radius box around it
 * is also destroyed at once. Radius 1 = 3x3x3 (see MultiPlayerGameModeMixin).
 */
public class NukerModule extends Module {
	public static final String ID = "nuker";
	private static final String RADIUS = "radius";

	public NukerModule(StatusManager status) {
		super(status, ID, "Nuker", "Breaks surrounding blocks when you break one", ModuleCategory.PLAYER);
		this.registerSlider(RADIUS, "Radius", 1, 5, 1, 1);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static int getRadius() {
		return (int) StatusManager.getInstance().getDouble(ID + "/" + RADIUS, 1);
	}
}
