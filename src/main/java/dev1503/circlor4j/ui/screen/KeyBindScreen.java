package dev1503.circlor4j.ui.screen;

import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.component.KeyBindPanel;
import dev1503.circlor4j.ui.component.UiText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Standalone full-screen keybind editor. The list itself is rendered by {@link KeyBindPanel};
 * this screen only adds the background and title, and returns to the previous screen on close.
 */
public class KeyBindScreen extends Screen {
	private static final int LIST_TOP = 48;

	private static final int BODY_COLOR = 0xC0101010;
	private static final int TITLE_COLOR = 0xFFFFFFFF;

	private final Screen returnScreen;
	private final KeyBindPanel panel;

	public KeyBindScreen(Screen returnScreen) {
		super(Component.literal("Circlor4j KeyBinds"));
		this.returnScreen = returnScreen;
		this.panel = new KeyBindPanel(this);
	}

	@Override
	public void onClose() {
		if (this.returnScreen != null) {
			this.minecraft.gui.setScreen(this.returnScreen);
		} else {
			super.onClose();
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (this.panel.mouseClicked(event, this.width, this.height, LIST_TOP)) {
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.panel.keyPressed(event)) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		this.panel.mouseScrolled(scrollY, this.width, this.height, LIST_TOP);
		return true;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, BODY_COLOR);
		String title = I18n.t("ui.keybinds.name");
		if (title.isEmpty() || title.equals("ui.keybinds.name")) {
			title = "Keybinds";
		}
		UiText.scaledText(graphics, this.font, title, this.width / 2 - UiText.scaledWidth(this.font, title) / 2, 10, TITLE_COLOR);
		this.panel.render(graphics, this.font, this.width, this.height, LIST_TOP, mouseX, mouseY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}
}
