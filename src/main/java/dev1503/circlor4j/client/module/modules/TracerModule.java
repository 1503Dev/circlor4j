package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/**
 * Draws tracer lines from the screen-centre crosshair to the centre of each selected entity's
 * collision box. Sub-components mirror ESP (Thickness, Mobs, Players) plus an extra Items toggle
 * for dropped items. The drawing is applied by {@code TracerRenderer} / {@code HudMixin}.
 */
public class TracerModule extends Module {
	public static final String ID = "tracer";
	private static final String MOBS = "mobs";
	private static final String PLAYERS = "players";
	private static final String ITEMS = "items";
	private static final String THICKNESS = "thickness";

	public static final int DEFAULT_COLOR = 0xFFFFFFFF;

	public TracerModule(StatusManager status) {
		super(status, ID, "Tracer", "Draws lines from the crosshair to entities", ModuleCategory.RENDER);
		this.registerSlider(THICKNESS, "Thickness", 0.1, 3.0, 0.05, 0.3);
		this.registerToggle(MOBS, "Mobs");
		this.registerColor(MOBS, "Color", DEFAULT_COLOR);
		this.registerToggle(PLAYERS, "Players", true);
		this.registerColor(PLAYERS, "Color", DEFAULT_COLOR);
		this.registerToggle(ITEMS, "Items");
		this.registerColor(ITEMS, "Color", DEFAULT_COLOR);
	}

	@Override
	public boolean isShownInGui() {
		return true;
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isMobsEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + MOBS + "/enabled", false);
	}

	public static boolean isPlayersEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + PLAYERS + "/enabled", false);
	}

	public static boolean isItemsEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + ITEMS + "/enabled", false);
	}

	public static int getMobsColor() {
		return StatusManager.getInstance().getInt(ID + "/" + MOBS + "/color", DEFAULT_COLOR);
	}

	public static int getPlayersColor() {
		return StatusManager.getInstance().getInt(ID + "/" + PLAYERS + "/color", DEFAULT_COLOR);
	}

	public static int getItemsColor() {
		return StatusManager.getInstance().getInt(ID + "/" + ITEMS + "/color", DEFAULT_COLOR);
	}

	public static float getThickness() {
		return (float) StatusManager.getInstance().getDouble(ID + "/" + THICKNESS, 1.0);
	}
}