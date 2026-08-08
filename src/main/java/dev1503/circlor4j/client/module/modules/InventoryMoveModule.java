package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class InventoryMoveModule extends Module {
	public static final String ID = "inventory_move";

	public InventoryMoveModule(StatusManager status) {
		super(status, ID, "InventoryMove", "Allows movement while in inventory screens", ModuleCategory.MOVEMENT);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
