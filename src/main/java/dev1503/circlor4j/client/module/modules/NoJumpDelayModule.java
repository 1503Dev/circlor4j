package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class NoJumpDelayModule extends Module {
	public NoJumpDelayModule(StatusManager status) {
		super(status, "no_jump_delay", "NoJumpDelay", "Removes the delay between jumps", ModuleCategory.MOVEMENT);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("no_jump_delay/enabled", false);
	}
}
