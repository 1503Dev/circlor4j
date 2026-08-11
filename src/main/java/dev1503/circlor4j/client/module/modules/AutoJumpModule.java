package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class AutoJumpModule extends Module {
	public static final String ID = "auto_jump";
	private static final String MOVING = "moving";

	public AutoJumpModule(StatusManager status) {
		super(status, ID, "AutoJump", "Automatically jumps when on ground", ModuleCategory.MOVEMENT);
		StatusManager.getInstance().setValueOnly(ID + "/" + MOVING + "/enabled", 1.0);
		this.registerToggle(MOVING, "Moving", true);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isMovingEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + MOVING + "/enabled", true);
	}
}
