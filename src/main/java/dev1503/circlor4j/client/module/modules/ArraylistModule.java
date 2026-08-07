package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/**
 * HUD Arraylist that lists the enabled modules. The drawing is applied by
 * {@code dev1503.circlor4j.ui.horionarraylist.HorionArrayList} (port of the openhal4a array list).
 */
public class ArraylistModule extends Module {
	public static final String ID = "arraylist";
	private static final String COLOR = "color";
	private static final String RAINBOW = "rainbow";
	private static final String CUSTOM = "custom";
	private static final String GRAVITY = "gravity";
	private static final String BACKGROUND = "background";
	private static final String TEXT_SIZE = "text_size";
	private static final String SHOW_MODES = "show_modes";
	private static final String SHOW_KEYBINDS = "show_keybinds";
	private static final String ROW_PADDING = "row_padding";

	public static final int MODE_NONE = 0;

	public static final int COLOR_RAINBOW = 0;
	public static final int COLOR_CATEGORIZED = 1;
	public static final int COLOR_CUSTOM = 2;

	public static final int RAINBOW_PASTEL = 0;
	public static final int RAINBOW_SATURATED = 1;

	public static final int GRAVITY_TOP_LEFT = 0;
	public static final int GRAVITY_TOP_RIGHT = 1;
	public static final int GRAVITY_BOTTOM_LEFT = 2;
	public static final int GRAVITY_BOTTOM_RIGHT = 3;

	public static final int DEFAULT_COLOR = 0xFFFFFFFF;
	public static final int DEFAULT_BACKGROUND = 0x9A000000;

	public ArraylistModule(StatusManager status) {
		super(status, ID, "Arraylist", "Shows enabled modules on screen", ModuleCategory.CIRCLOR);
		status.setValueOnly(ID + "/enabled", 1.0);
		this.registerDropdown(COLOR, "Color", new String[] {"Rainbow", "Categorized", "Custom"}, new String[] {
			"module.arraylist.color.rainbow.name",
			"module.arraylist.color.categorized.name",
			"module.arraylist.color.custom.name"
		}, COLOR_RAINBOW);
		this.registerDropdown(RAINBOW, "Rainbow", new String[] {"Pastel", "Saturated"}, new String[] {
			"module.arraylist.rainbow.pastel.name",
			"module.arraylist.rainbow.saturated.name"
		}, RAINBOW_PASTEL, ID + "/" + COLOR + " == " + COLOR_RAINBOW);
		this.registerColor(CUSTOM, "Custom Color", DEFAULT_COLOR, ID + "/" + COLOR + " == " + COLOR_CUSTOM);
		this.registerDropdown(GRAVITY, "Gravity", new String[] {"TopLeft", "TopRight", "BottomLeft", "BottomRight"}, new String[] {
			"module.arraylist.gravity.top_left.name",
			"module.arraylist.gravity.top_right.name",
			"module.arraylist.gravity.bottom_left.name",
			"module.arraylist.gravity.bottom_right.name"
		}, GRAVITY_TOP_RIGHT);
		this.registerColor(BACKGROUND, "Background", DEFAULT_BACKGROUND);
		this.registerSlider(TEXT_SIZE, "Text Size", 0.5, 2.0, 0.05, 1.0);
		this.registerToggle(SHOW_MODES, "Show Modes");
		this.registerToggle(SHOW_KEYBINDS, "Show Keybinds");
		this.registerSlider(ROW_PADDING, "Row Padding", 0.0, 12.0, 1.0, 3.0);
	}

	@Override
	public boolean isShownInGui() {
		return true;
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", true);
	}

	public static int getMode() {
		return MODE_NONE;
	}

	public static int getColorMode() {
		return StatusManager.getInstance().getInt(ID + "/" + COLOR, COLOR_RAINBOW);
	}

	public static int getRainbowMode() {
		return StatusManager.getInstance().getInt(ID + "/" + RAINBOW, RAINBOW_PASTEL);
	}

	public static int getCustomColor() {
		return StatusManager.getInstance().getInt(ID + "/" + CUSTOM + "/color", DEFAULT_COLOR);
	}

	public static int getGravity() {
		return StatusManager.getInstance().getInt(ID + "/" + GRAVITY, GRAVITY_TOP_RIGHT);
	}

	public static int getBackgroundColor() {
		return StatusManager.getInstance().getInt(ID + "/" + BACKGROUND + "/color", DEFAULT_BACKGROUND);
	}

	public static float getTextSize() {
		return (float) StatusManager.getInstance().getDouble(ID + "/" + TEXT_SIZE, 1.0);
	}

	public static boolean isShowModes() {
		return StatusManager.getInstance().getBoolean(ID + "/" + SHOW_MODES + "/enabled", false);
	}

	public static boolean isShowKeybinds() {
		return StatusManager.getInstance().getBoolean(ID + "/" + SHOW_KEYBINDS + "/enabled", false);
	}

	public static boolean isTextShadow() {
		return false;
	}

	public static float getRowPadding() {
		return (float) StatusManager.getInstance().getDouble(ID + "/" + ROW_PADDING, 3.0);
	}

	public static float getBorderThickness() {
		return 1.0F;
	}
}