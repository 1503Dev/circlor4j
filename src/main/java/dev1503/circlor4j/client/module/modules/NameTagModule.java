package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class NameTagModule extends Module {
	public static final String ID = "name_tag";
	private static final String SHOW_HEALTH = "show_health";

	public NameTagModule(StatusManager status) {
		super(status, ID, "NameTag", "Shows player name tags and health above heads", ModuleCategory.RENDER);
		this.registerToggle(SHOW_HEALTH, "ShowHealth", true);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isShowHealth() {
		return StatusManager.getInstance().getBoolean(ID + "/" + SHOW_HEALTH + "/enabled", true);
	}
}
