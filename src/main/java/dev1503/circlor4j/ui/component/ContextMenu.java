package dev1503.circlor4j.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A tiny vertical popup menu of labelled actions, rendered above everything else and dismissed by
 * clicking an item or clicking away. Used for right-click context menus inside other popups.
 */
public class ContextMenu {
	private static final int ITEM_H = 9;
	private static final int PAD = 2;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int HOVER_COLOR = 0x40FFFFFF;
	private static final int BG_COLOR = 0xE8101010;
	private static final int BORDER_COLOR = 0xFF3A3A3A;

	public final List<String> labels = new ArrayList<>();
	public final List<Runnable> actions = new ArrayList<>();
	private int x;
	private int y;
	private int width;

	public boolean isEmpty() {
		return this.labels.isEmpty();
	}

	public void add(String label, Runnable action) {
		this.labels.add(label);
		this.actions.add(action);
	}

	public void open(int x, int y) {
		this.width = 20;
		Font font = Minecraft.getInstance().font;
		for (String label : this.labels) {
			this.width = Math.max(this.width, UiText.scaledWidth(font, label) + PAD * 2);
		}
		int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		int height = PAD * 2 + this.labels.size() * ITEM_H;
		this.x = Math.max(0, Math.min(x, screenW - this.width));
		this.y = Math.max(0, Math.min(y, screenH - height));
	}

	public void close() {
		this.labels.clear();
		this.actions.clear();
	}

	public boolean isOpen() {
		return !this.labels.isEmpty();
	}

	public boolean contains(int mx, int my) {
		int height = PAD * 2 + this.labels.size() * ITEM_H;
		return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + height;
	}

	private int itemAt(int my) {
		int idx = (my - this.y - PAD) / ITEM_H;
		return (idx >= 0 && idx < this.labels.size()) ? idx : -1;
	}

	public boolean mouseClicked(MouseButtonEvent event) {
		if (!this.isOpen() || event.button() != 0) {
			return false;
		}
		int mx = (int) event.x();
		int my = (int) event.y();
		if (!this.contains(mx, my)) {
			this.close();
			return false;
		}
		int idx = this.itemAt(my);
		if (idx >= 0) {
			this.actions.get(idx).run();
		}
		this.close();
		return true;
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		if (!this.isOpen()) {
			return;
		}
		int height = PAD * 2 + this.labels.size() * ITEM_H;
		graphics.fill(this.x, this.y, this.x + this.width, this.y + height, BG_COLOR);
		graphics.outline(this.x, this.y, this.width, height, BORDER_COLOR);
		for (int i = 0; i < this.labels.size(); i++) {
			int itemY = this.y + PAD + i * ITEM_H;
			boolean hover = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= itemY && mouseY < itemY + ITEM_H;
			if (hover) {
				graphics.fill(this.x, itemY, this.x + this.width, itemY + ITEM_H, HOVER_COLOR);
			}
			UiText.scaledText(graphics, font, this.labels.get(i), this.x + PAD, UiText.centerY(itemY, ITEM_H), TEXT_COLOR);
		}
	}
}
