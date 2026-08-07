package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/** Overrides the camera FOV (integer 15-150) via CameraMixin. */
public class ZoomModule extends Module {
	private static final String FOV = "fov";

	public ZoomModule(StatusManager status) {
		super(status, "zoom", "Zoom", "Overrides the FOV", ModuleCategory.RENDER);
		this.registerSlider(FOV, "FOV", 15.0, 150.0, 1.0, 30.0);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("zoom/enabled", false);
	}

	public static int getFov() {
		return (int) StatusManager.getInstance().getDouble("zoom/fov", 30);
	}
}
