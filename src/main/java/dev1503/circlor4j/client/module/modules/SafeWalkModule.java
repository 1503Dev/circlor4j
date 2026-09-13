package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class SafeWalkModule extends Module {
	public static final String ID = "safe_walk";
	public static final String DISABLE_WHEN_LANDING = "disable_when_landing";

	public SafeWalkModule(StatusManager status) {
		super(status, ID, "SafeWalk", "Prevents you from walking off block edges", ModuleCategory.MOVEMENT);
		this.registerToggle(DISABLE_WHEN_LANDING, "Disable When Landing Possible");
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isDisableWhenLanding() {
		return StatusManager.getInstance().getBoolean(ID + "/" + DISABLE_WHEN_LANDING + "/enabled", false);
	}
}
