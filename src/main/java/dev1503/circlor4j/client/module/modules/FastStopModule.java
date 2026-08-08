package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class FastStopModule extends Module {
	public static final String ID = "fast_stop";

	public FastStopModule(StatusManager status) {
		super(status, ID, "FastStop", "Stops movement immediately when keys are released", ModuleCategory.MOVEMENT);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
