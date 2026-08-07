package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import dev1503.circlor4j.ui.clickgui.ClickGuiScreen;
import net.minecraft.client.Minecraft;

/**
 * Toggles the ClickGUI screen. The screen's visibility is bound to "clickgui/enabled":
 * enabling the module opens the screen, disabling it (or pressing Esc) closes it.
 * The "Background" colour picker controls the screen's backdrop.
 */
public class ClickGuiModule extends Module {
	public static final String ID = "clickgui";
	private static final String BACKGROUND = "background";

	public static final int DEFAULT_BACKGROUND = 0x40000000;

	private static boolean closing;

	public ClickGuiModule(StatusManager status) {
		super(status, ID, "ClickGUI", "Toggles the ClickGUI", ModuleCategory.CIRCLOR);
		this.registerColor(BACKGROUND, "Background", DEFAULT_BACKGROUND);
	}

	/** Whether the ClickGuiScreen is being removed right now (suppresses recursive close). */
	public static boolean isClosing() {
		return closing;
	}

	public static void setClosing(boolean closing) {
		ClickGuiModule.closing = closing;
	}

	@Override
	public void onEnable() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui.screen() == null) {
			mc.gui.setScreen(new ClickGuiScreen());
		}
	}

	@Override
	public void onDisable() {
		if (isClosing()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui.screen() instanceof ClickGuiScreen) {
			mc.gui.setScreen(null);
		}
	}

	public static int getBackgroundColor() {
		return StatusManager.getInstance().getInt(ID + "/" + BACKGROUND + "/color", DEFAULT_BACKGROUND);
	}
}