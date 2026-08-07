package dev1503.circlor4j.ui.screen;

import dev1503.circlor4j.client.keybind.KeyBind;
import dev1503.circlor4j.client.keybind.KeyBindManager;
import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.component.UiText;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Fixed-layout keybind editor. The middle shows the list of binds (title = key combination,
 * content = mode + function); the bottom has an Add button and, below it, Load / Save / Save As.
 */
public class KeyBindScreen extends Screen {
	private static final int SIDE = 10;
	private static final int LIST_TOP = 22;
	private static final int ROW_HEIGHT = 24;
	private static final int MAX_CONTENT_WIDTH = 360;

	private static final int MENU_W = 90;
	private static final int MENU_ROW_H = 14;

	private static final int ADD_W = 90;
	private static final int ADD_H = 14;
	private static final int FILE_W = 64;
	private static final int FILE_H = 12;
	private static final int BTN_GAP = 8;

	private static final int BODY_COLOR = 0xC0101010;
	private static final int ROW_HOVER = 0x40FFFFFF;
	private static final int TITLE_COLOR = 0xFFFFFFFF;
	private static final int KEY_COLOR = 0xFF2697F3;
	private static final int CONTENT_COLOR = 0xFFAAAAAA;
	private static final int BTN_COLOR = 0xFF222222;
	private static final int BTN_HOVER = 0xFF3A6EA5;
	private static final int BTN_TEXT = 0xFFFFFFFF;

	private final Screen returnScreen;
	private int scroll;
	private KeyBind contextBind;
	private int contextMenuX;
	private int contextMenuY;

	public KeyBindScreen(Screen returnScreen) {
		super(Component.literal("Circlor4j KeyBinds"));
		this.returnScreen = returnScreen;
	}

	@Override
	public void onClose() {
		if (this.returnScreen != null) {
			this.minecraft.gui.setScreen(this.returnScreen);
		} else {
			super.onClose();
		}
	}

	private int addY() {
		return this.height - 66;
	}

	private int fileY() {
		return this.height - 44;
	}

	private int contentWidth() {
		return Math.min(this.width, MAX_CONTENT_WIDTH);
	}

	private int contentLeft() {
		return (this.width - this.contentWidth()) / 2;
	}

	private int contentRight() {
		return this.contentLeft() + this.contentWidth();
	}

	private int centerX() {
		return this.width / 2;
	}

	private int addX() {
		return this.centerX() - ADD_W / 2;
	}

	private int filesStartX() {
		return this.centerX() - (3 * FILE_W + 2 * BTN_GAP) / 2;
	}

	private int loadX() {
		return this.filesStartX();
	}

	private int saveX() {
		return this.filesStartX() + FILE_W + BTN_GAP;
	}

	private int saveAsX() {
		return this.filesStartX() + 2 * (FILE_W + BTN_GAP);
	}

	private boolean inRect(int mx, int my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private boolean inAdd(int mx, int my) {
		return this.inRect(mx, my, this.addX(), this.addY(), ADD_W, ADD_H);
	}

	private boolean inLoad(int mx, int my) {
		return this.inRect(mx, my, this.loadX(), this.fileY(), FILE_W, FILE_H);
	}

	private boolean inSave(int mx, int my) {
		return this.inRect(mx, my, this.saveX(), this.fileY(), FILE_W, FILE_H);
	}

	private boolean inSaveAs(int mx, int my) {
		return this.inRect(mx, my, this.saveAsX(), this.fileY(), FILE_W, FILE_H);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int mx = (int) event.x();
		int my = (int) event.y();
		if (this.contextBind != null) {
			int menuX = this.contextMenuX();
			int menuY = this.contextMenuY();
			if (event.button() == 0) {
				if (mx >= menuX && mx < menuX + MENU_W) {
					if (my >= menuY && my < menuY + MENU_ROW_H) {
						this.minecraft.gui.setScreen(new AddKeyBindDialog(this, this.contextBind));
						this.contextBind = null;
						return true;
					}
					if (my >= menuY + MENU_ROW_H && my < menuY + 2 * MENU_ROW_H) {
						KeyBindManager.remove(this.contextBind);
						KeyBindManager.saveDefault();
						this.contextBind = null;
						return true;
					}
				}
				this.contextBind = null;
				return true;
			}
			this.contextBind = null;
		}
		if (event.button() == 0) {
			if (this.inAdd(mx, my)) {
				this.minecraft.gui.setScreen(new AddKeyBindDialog(this));
				return true;
			}
			if (this.inLoad(mx, my)) {
				this.minecraft.gui.setScreen(new FilePickerScreen(this, FilePickerScreen.Mode.LOAD));
				return true;
			}
			if (this.inSave(mx, my)) {
				KeyBindManager.saveDefault();
				return true;
			}
			if (this.inSaveAs(mx, my)) {
				this.minecraft.gui.setScreen(new FilePickerScreen(this, FilePickerScreen.Mode.SAVE));
				return true;
			}
		} else if (event.button() == 1) {
			if (mx >= this.contentLeft() + SIDE && mx < this.contentRight() - SIDE) {
				List<KeyBind> binds = KeyBindManager.all();
				int index = this.scroll + this.rowAt(my);
				if (index >= 0 && index < binds.size()) {
					this.contextBind = binds.get(index);
					this.contextMenuX = mx;
					this.contextMenuY = my;
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private int contextMenuX() {
		return Math.max(2, Math.min(this.contextMenuX, this.width - MENU_W - 2));
	}

	private int contextMenuY() {
		return Math.max(2, Math.min(this.contextMenuY, this.height - 2 * MENU_ROW_H - 2));
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.contextBind != null && event.isEscape()) {
			this.contextBind = null;
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		int max = this.maxScroll();
		if (scrollY > 0.0) {
			this.scroll = Math.max(0, this.scroll - 1);
		} else if (scrollY < 0.0) {
			this.scroll = Math.min(max, this.scroll + 1);
		}
		return true;
	}

	private int rowAt(int my) {
		return (my - LIST_TOP) / ROW_HEIGHT;
	}

	private int maxScroll() {
		int count = KeyBindManager.all().size();
		int visible = Math.max(1, (this.fileY() - LIST_TOP - 4) / ROW_HEIGHT);
		return Math.max(0, count - visible);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, BODY_COLOR);
		String title = tr("ui.keybinds.name", "Keybinds");
		UiText.scaledText(graphics, this.font, title, this.width / 2 - UiText.scaledWidth(this.font, title) / 2, 10, TITLE_COLOR);

		this.scroll = Math.min(this.scroll, this.maxScroll());
		int listLeft = this.contentLeft() + SIDE;
		int listRight = this.contentRight() - SIDE;
		List<KeyBind> binds = KeyBindManager.all();
		int visible = Math.max(1, (this.fileY() - LIST_TOP - 4) / ROW_HEIGHT);
		for (int i = 0; i < visible; i++) {
			int index = this.scroll + i;
			if (index >= binds.size()) {
				break;
			}
			KeyBind bind = binds.get(index);
			int rowY = LIST_TOP + i * ROW_HEIGHT;
			if (mouseX >= listLeft && mouseX < listRight && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
				graphics.fill(listLeft, rowY, listRight, rowY + ROW_HEIGHT, ROW_HOVER);
			}
			UiText.scaledText(graphics, this.font, bind.getDisplayName(), listLeft + 4, rowY + 2, KEY_COLOR);
			UiText.scaledText(graphics, this.font, bind.getModeName() + ": " + bind.getFunction(), listLeft + 4, rowY + 12, CONTENT_COLOR);
		}

		this.drawButton(graphics, tr("ui.keybinds.add", "Add"), this.addX(), this.addY(), ADD_W, ADD_H, mouseX, mouseY);
		this.drawButton(graphics, tr("ui.keybinds.load", "Load"), this.loadX(), this.fileY(), FILE_W, FILE_H, mouseX, mouseY);
		this.drawButton(graphics, tr("ui.keybinds.save", "Save"), this.saveX(), this.fileY(), FILE_W, FILE_H, mouseX, mouseY);
		this.drawButton(graphics, tr("ui.keybinds.save_as", "Save As"), this.saveAsX(), this.fileY(), FILE_W, FILE_H, mouseX, mouseY);

		if (this.contextBind != null) {
			int menuX = this.contextMenuX();
			int menuY = this.contextMenuY();
			graphics.fill(menuX, menuY, menuX + MENU_W, menuY + 2 * MENU_ROW_H, 0xE0101010);
			graphics.outline(menuX, menuY, MENU_W, 2 * MENU_ROW_H, 0xFF3A3A3A);
			String edit = tr("ui.keybinds.edit", "Edit");
			UiText.scaledText(graphics, this.font, edit, menuX + MENU_W / 2 - UiText.scaledWidth(this.font, edit) / 2, UiText.centerY(menuY, MENU_ROW_H), CONTENT_COLOR);
			String delete = tr("ui.keybinds.delete", "Delete");
			UiText.scaledText(graphics, this.font, delete, menuX + MENU_W / 2 - UiText.scaledWidth(this.font, delete) / 2, UiText.centerY(menuY + MENU_ROW_H, MENU_ROW_H), CONTENT_COLOR);
		}
	}

	private void drawButton(GuiGraphicsExtractor graphics, String label, int x, int y, int w, int h, int mouseX, int mouseY) {
		boolean hovered = this.inRect(mouseX, mouseY, x, y, w, h);
		graphics.fill(x, y, x + w, y + h, hovered ? BTN_HOVER : BTN_COLOR);
		graphics.outline(x, y, w, h, 0xFF3A3A3A);
		int textX = x + w / 2 - UiText.scaledWidth(this.font, label) / 2;
		UiText.scaledText(graphics, this.font, label, textX, UiText.centerY(y, h), BTN_TEXT);
	}

	private static String tr(String key, String fallback) {
		String value = I18n.t(key);
		return key.equals(value) ? fallback : value;
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