package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class NoHurtCamModule extends Module {
	public NoHurtCamModule(StatusManager status) {
		super(status, "no_hurt_cam", "NoHurtCam", "Disables the camera shake when taking damage", ModuleCategory.RENDER);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("no_hurt_cam/enabled", false);
	}
}
