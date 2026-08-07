package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A button-style row that looks like a Toggle but has no on/off state. Clicking it fires a
 * status action via {@link StatusManager#trigger(String)} so the owning screen can act on it.
 */
public class ActionButton extends Component {
	private static final int HOVER_COLOR = 0x40FFFFFF;
	private static final int TEXT_COLOR = 0xFFAAAAAA;

	private final StatusManager status;
	private final String label;
	private final int height;
	private int x;
	private int y;
	private int width;

	public ActionButton(StatusManager status, String path, String label, int x, int y, int width, int height) {
		super(path);
		this.status = status;
		this.label = label;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void syncStatus(double value) {
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean contains(int mx, int my) {
		return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
	}

	/** Row click: fires the bound action. */
	public boolean mouseClicked(MouseButtonEvent event) {
		if (event.button() != 0 || !this.contains((int) event.x(), (int) event.y())) {
			return false;
		}
		this.status.trigger(this.getPath());
		return true;
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		if (this.contains(mouseX, mouseY)) {
			graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, HOVER_COLOR);
		}
		String text = UiText.fit(font, this.label, Math.max(0, this.width - 4));
		UiText.scaledText(graphics, font, text, this.x + 2, UiText.centerY(this.y, this.height), TEXT_COLOR);
	}
}