package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A dropdown row. Clicking the row opens a separate top-layer popup whose right edge hugs the
 * owning category window's right side; its width adapts to the item texts and items are right-aligned.
 * Picking an item writes its index to the bound path. The menu is rendered/coordinated by the
 * ClickGuiScreen so it always appears above the category windows.
 */
public class Dropdown extends Component {
	private static final int MENU_PADDING = 8;
	private static final int HOVER_COLOR = 0x40FFFFFF;
	private static final int MENU_BG_COLOR = 0xE0101010;
	private static final int MENU_BORDER_COLOR = 0xFF3A3A3A;
	private static final int LABEL_COLOR = 0xFFAAAAAA;
	private static final int VALUE_COLOR = 0xFFFFFFFF;
	private static final int SELECTED_COLOR = 0xFF2697F3;

	private final StatusManager status;
	private final String label;
	private final String[] items;
	private final int height;
	private int x;
	private int y;
	private int width;
	private int anchorRight;
	private int selected;
	private boolean menuOpen;

	public Dropdown(StatusManager status, String path, String label, String[] items, int defaultIndex, int x, int y, int width, int height) {
		super(path);
		this.status = status;
		this.label = label;
		this.items = items.clone();
		this.height = height;
		this.x = x;
		this.y = y;
		this.width = width;
		this.anchorRight = x + width;
		this.selected = this.clampIndex((int) Math.round(status.getDouble(path, defaultIndex)));
	}

	@Override
	public void syncStatus(double value) {
		this.selected = this.clampIndex((int) Math.round(value));
	}

	public boolean isMenuOpen() {
		return this.menuOpen;
	}

	public void closeMenu() {
		this.menuOpen = false;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setAnchorRight(int right) {
		this.anchorRight = right;
	}

	private int clampIndex(int index) {
		return Math.max(0, Math.min(this.items.length - 1, index));
	}

	private boolean containsRow(int mx, int my) {
		return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
	}

	private int menuWidth(Font font) {
		int maxItem = 0;
		for (String item : this.items) {
			maxItem = Math.max(maxItem, UiText.scaledWidth(font, item));
		}
		return maxItem + MENU_PADDING;
	}

	private int menuX(Font font) {
		return Math.max(0, this.anchorRight - this.menuWidth(font));
	}

	private int menuY() {
		return this.y;
	}

	private int menuHeight() {
		return this.items.length * this.height;
	}

	public boolean containsMenu(Font font, int mx, int my) {
		return mx >= this.menuX(font) && mx < this.menuX(font) + this.menuWidth(font) && my >= this.menuY() && my < this.menuY() + this.menuHeight();
	}

	/** Row click (called by the owning CategoryWindow): toggles the menu. */
	public boolean mouseClickedRow(MouseButtonEvent event) {
		if (event.button() != 0 || !this.containsRow((int) event.x(), (int) event.y())) {
			return false;
		}
		this.menuOpen = !this.menuOpen;
		return true;
	}

	/** Menu item click (called by the ClickGuiScreen, top layer). */
	public boolean mouseClickedMenu(MouseButtonEvent event, Font font) {
		if (!this.menuOpen || event.button() != 0) {
			return false;
		}
		int mx = (int) event.x();
		int my = (int) event.y();
		if (!this.containsMenu(font, mx, my)) {
			return false;
		}
		int index = (my - this.menuY()) / this.height;
		if (index >= 0 && index < this.items.length) {
			this.selected = index;
			this.status.setValue(this, this.getPath(), index);
			this.menuOpen = false;
			return true;
		}
		return false;
	}

	/** Row bounds check for the open-menu reconciliation (a row click should close it). */
	public boolean isRow(int mx, int my) {
		return this.containsRow(mx, my);
	}

	public void renderRow(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		boolean hovered = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
		if (hovered) {
			graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, HOVER_COLOR);
		}

		String indicator = ">";
		int indicatorWidth = UiText.scaledWidth(font, indicator);
		String valueText = UiText.fit(font, this.items[this.selected], Math.max(0, this.width - 6 - indicatorWidth));
		int valueWidth = UiText.scaledWidth(font, valueText);
		int valueX = this.x + this.width - 2 - indicatorWidth - 2 - valueWidth;
		int textY = UiText.centerY(this.y, this.height);

		String labelText = UiText.fit(font, this.label, Math.max(0, valueX - (this.x + 2) - 2));
		UiText.scaledText(graphics, font, labelText, this.x + 2, textY, LABEL_COLOR);
		UiText.scaledText(graphics, font, valueText, Math.max(this.x + 2, valueX), textY, VALUE_COLOR);
		UiText.scaledText(graphics, font, indicator, this.x + this.width - 2 - indicatorWidth, textY, VALUE_COLOR);
	}

	public void renderMenu(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		if (!this.menuOpen) {
			return;
		}
		int menuX = this.menuX(font);
		int menuY = this.menuY();
		int menuW = this.menuWidth(font);
		int menuH = this.menuHeight();

		graphics.fill(menuX, menuY, menuX + menuW, menuY + menuH, MENU_BG_COLOR);
		for (int i = 0; i < this.items.length; i++) {
			int rowY = menuY + i * this.height;
			boolean hovered = mouseX >= menuX && mouseX < menuX + menuW && mouseY >= rowY && mouseY < rowY + this.height;
			if (hovered) {
				graphics.fill(menuX, rowY, menuX + menuW, rowY + this.height, HOVER_COLOR);
			}
			int color = i == this.selected ? SELECTED_COLOR : LABEL_COLOR;
			String itemText = UiText.fit(font, this.items[i], Math.max(0, menuW - 4));
			int textX = menuX + menuW - 2 - UiText.scaledWidth(font, itemText);
			UiText.scaledText(graphics, font, itemText, textX, UiText.centerY(rowY, this.height), color);
		}

		graphics.fill(menuX, menuY, menuX + menuW, menuY + 1, MENU_BORDER_COLOR);
		graphics.fill(menuX, menuY + menuH - 1, menuX + menuW, menuY + menuH, MENU_BORDER_COLOR);
		graphics.fill(menuX, menuY, menuX + 1, menuY + menuH, MENU_BORDER_COLOR);
		graphics.fill(menuX + menuW - 1, menuY, menuX + menuW, menuY + menuH, MENU_BORDER_COLOR);
	}
}
