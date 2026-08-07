package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/**
 * ESP (Extra Sensory Perception) module. When enabled, renders a white
 * wireframe box around the collision box of the selected entity types:
 * <ul>
 *   <li>Mobs - any living creature that is not a player.</li>
 *   <li>Players - other player entities.</li>
 * </ul>
 * The actual box drawing is applied by {@code LevelRendererMixin}; this class only owns the settings.
 */
	public class EspModule extends Module {
	public static final String ID = "esp";
	private static final String MOBS = "mobs";
	private static final String PLAYERS = "players";
	private static final String ITEMS = "items";
	private static final String THICKNESS = "thickness";

	public static final int DEFAULT_COLOR = 0xFFFFFFFF;

	public EspModule(StatusManager status) {
		super(status, ID, "ESP", "Highlights entities with a coloured bounding box", ModuleCategory.RENDER);
		this.registerSlider(THICKNESS, "Thickness", 0.5, 5.0, 0.5, 1.0);
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