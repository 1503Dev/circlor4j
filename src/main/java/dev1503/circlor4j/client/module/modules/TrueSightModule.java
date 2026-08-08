package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class TrueSightModule extends Module {
	public static final String ID = "true_sight";
	private static final String ENTITIES = "entities";
	private static final String BLOCKS = "blocks";

	public TrueSightModule(StatusManager status) {
		super(status, ID, "TrueSight", "Reveals invisible entities and hidden blocks", ModuleCategory.RENDER);
		this.registerToggle(ENTITIES, "Entities", true);
		this.registerToggle(BLOCKS, "Blocks", true);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isEntitiesEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + ENTITIES + "/enabled", false);
	}

	public static boolean isBlocksEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + BLOCKS + "/enabled", false);
	}
}
