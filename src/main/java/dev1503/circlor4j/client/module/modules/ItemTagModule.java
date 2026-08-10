package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class ItemTagModule extends Module {
	public static final String ID = "item_tag";

	public ItemTagModule(StatusManager status) {
		super(status, ID, "ItemTag", "Shows item name tags above dropped items", ModuleCategory.RENDER);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
