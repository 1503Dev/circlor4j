package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class AntiDebuffModule extends Module {
	public AntiDebuffModule(StatusManager status) {
		super(status, "anti_debuff", "AntiDebuff", "Disables nausea, darkness and blindness effects", ModuleCategory.RENDER);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("anti_debuff/enabled", false);
	}
}
