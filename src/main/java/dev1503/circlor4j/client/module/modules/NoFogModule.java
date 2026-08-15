package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class NoFogModule extends Module {
	public static final String ID = "no_fog";

	public NoFogModule(StatusManager status) {
		super(status, ID, "NoFog", "Disables all fog rendering", ModuleCategory.RENDER);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
