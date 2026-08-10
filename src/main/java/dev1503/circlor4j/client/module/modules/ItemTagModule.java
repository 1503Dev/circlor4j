package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class ItemTagModule extends Module {
	public static final String ID = "item_tag";
	private static final String TEXT_SIZE = "text_size";
	private static final String SCALE_BY_DISTANCE = "scale_by_distance";

	public ItemTagModule(StatusManager status) {
		super(status, ID, "ItemTag", "Shows item name tags above dropped items", ModuleCategory.RENDER);
		this.registerSlider(TEXT_SIZE, "Text Size", 0.5, 3.0, 0.1, 1.0);
		this.registerToggle(SCALE_BY_DISTANCE, "Scale By Distance", true);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static float getTextSize() {
		return StatusManager.getInstance().getFloat(ID + "/" + TEXT_SIZE, 1.0F);
	}

	public static boolean isScaleByDistance() {
		return StatusManager.getInstance().getBoolean(ID + "/" + SCALE_BY_DISTANCE, true);
	}
}
