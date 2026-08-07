package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/** AirJump: lets you jump again while airborne by holding the jump key (see LocalPlayerMixin). */
public class AirJumpModule extends Module {
	public static final String ID = "air_jump";

	public AirJumpModule(StatusManager status) {
		super(status, ID, "AirJump", "Allows you to jump while in the air", ModuleCategory.MOVEMENT);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
