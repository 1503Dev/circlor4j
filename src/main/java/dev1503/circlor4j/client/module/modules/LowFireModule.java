package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class LowFireModule extends Module {
	public LowFireModule(StatusManager status) {
		super(status, "low_fire", "LowFire", "Lowers the on-fire screen overlay to half height", ModuleCategory.RENDER);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("low_fire/enabled", false);
	}
}
