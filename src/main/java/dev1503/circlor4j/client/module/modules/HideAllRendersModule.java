package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/**
 * HideAllRenders: while active, every Render-category feature is implicitly disabled
 * (tick handlers are skipped and render-driven entry points report inactive) without
 * touching the modules' own enabled toggles.
 */
public class HideAllRendersModule extends Module {
	public static final String ID = "hide_all_renders";

	public HideAllRendersModule(StatusManager status) {
		super(status, ID, "HideAllRenders", "Hides all rendering", ModuleCategory.CIRCLOR);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}