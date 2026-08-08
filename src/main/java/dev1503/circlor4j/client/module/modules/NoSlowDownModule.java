package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class NoSlowDownModule extends Module {
	public static final String ID = "no_slow_down";

	public NoSlowDownModule(StatusManager status) {
		super(status, ID, "NoSlowDown", "Prevents slowdown from sneaking/eating/using items", ModuleCategory.MOVEMENT);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
