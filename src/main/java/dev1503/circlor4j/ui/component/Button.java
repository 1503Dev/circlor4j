package dev1503.circlor4j.ui.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * Base class for all clickable buttons in the mod UI. Renders a filled background with a
 * hover highlight and centred text; no border. Subclasses implement {@link #onPress()}.
 */
public abstract class Button {
	protected static final int BG_COLOR = 0xFF222222;
	protected static final int BG_HOVER = 0xFF3A6EA5;
	protected static final int TEXT_COLOR = 0xFFFFFFFF;

	protected String label;
	protected int x;
	protected int y;
	protected int width;
	protected int height;

	protected Button(String label, int x, int y, int width, int height) {
		this.label = label;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public boolean contains(int mx, int my) {
		return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
	}

	/** Left-click within bounds: fires the button. */
	public boolean mouseClicked(MouseButtonEvent event) {
		if (event.button() != 0 || !this.contains((int) event.x(), (int) event.y())) {
			return false;
		}
		this.onPress();
		return true;
	}

	protected abstract void onPress();

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		boolean hovered = this.contains(mouseX, mouseY);
		graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, hovered ? BG_HOVER : BG_COLOR);
		int textX = this.x + this.width / 2 - UiText.scaledWidth(font, this.label) / 2;
		UiText.scaledText(graphics, font, this.label, textX, UiText.centerY(this.y, this.height), TEXT_COLOR);
	}
}
