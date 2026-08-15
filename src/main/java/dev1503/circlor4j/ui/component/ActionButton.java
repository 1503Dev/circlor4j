package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A button-style row that looks like a Toggle but has no on/off state. Clicking it fires a
 * status action via {@link StatusManager#trigger(String)} so the owning screen can act on it.
 */
public class ActionButton extends Button implements StatusWidget {
	private final StatusManager status;
	private final String path;

	public ActionButton(StatusManager status, String path, String label, int x, int y, int width, int height) {
		super(label, x, y, width, height);
		this.status = status;
		this.path = path;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public void syncStatus(double value) {
	}

	@Override
	protected void onPress() {
		this.status.trigger(this.path);
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		if (this.contains(mouseX, mouseY)) {
			graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, 0x40FFFFFF);
		}
		String text = UiText.fit(font, this.label, Math.max(0, this.width - 4));
		UiText.scaledText(graphics, font, text, this.x + 2, UiText.centerY(this.y, this.height), 0xFFAAAAAA);
	}
}
