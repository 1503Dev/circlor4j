package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class NoWebModule extends Module {
	public static final String ID = "no_web";

	public NoWebModule(StatusManager status) {
		super(status, ID, "NoWeb", "Prevents spiderweb slowdown", ModuleCategory.MOVEMENT);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
