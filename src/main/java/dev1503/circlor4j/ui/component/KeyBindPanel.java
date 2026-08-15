package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.client.keybind.KeyBind;
import dev1503.circlor4j.client.keybind.KeyBindManager;
import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.screen.AddKeyBindDialog;
import dev1503.circlor4j.ui.screen.FilePickerScreen;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * Embeddable keybind editor list. Renders the bind list with Add / Load / Save / Save As buttons
 * and a right-click context menu (Edit / Delete). Used by {@code KeyBindScreen} as a standalone
 * screen and by {@code ClickGuiScreen} as an inline tab. Dialogs opened from here return to the
 * host screen.
 */
public class KeyBindPanel {
	private static final int SIDE = 10;
	private static final int ROW_HEIGHT = 24;
	private static final int MAX_CONTENT_WIDTH = 360;

	private static final int MENU_W = 90;
	private static final int MENU_ROW_H = 14;

	private static final int ADD_W = 90;
	private static final int ADD_H = 14;
	private static final int FILE_W = 64;
	private static final int FILE_H = 12;
	private static final int BTN_GAP = 8;

	private static final int ROW_HOVER = 0x40FFFFFF;
	private static final int KEY_COLOR = 0xFF2697F3;
	private static final int CONTENT_COLOR = 0xFFAAAAAA;

	private final Screen host;
	private final Button addButton;
	private final Button loadButton;
	private final Button saveButton;
	private final Button saveAsButton;
	private int scroll;
	private KeyBind contextBind;
	private int contextMenuX;
	private int contextMenuY;

	public KeyBindPanel(Screen host) {
		this.host = host;
		this.addButton = new TextButton(tr("ui.keybinds.add", "Add"), 0, 0, ADD_W, ADD_H,
			() -> Minecraft.getInstance().gui.setScreen(new AddKeyBindDialog(this.host)));
		this.loadButton = new TextButton(tr("ui.keybinds.load", "Load"), 0, 0, FILE_W, FILE_H,
			() -> Minecraft.getInstance().gui.setScreen(new FilePickerScreen(this.host, FilePickerScreen.Mode.LOAD)));
		this.saveButton = new TextButton(tr("ui.keybinds.save", "Save"), 0, 0, FILE_W, FILE_H,
			KeyBindManager::saveDefault);
		this.saveAsButton = new TextButton(tr("ui.keybinds.save_as", "Save As"), 0, 0, FILE_W, FILE_H,
			() -> Minecraft.getInstance().gui.setScreen(new FilePickerScreen(this.host, FilePickerScreen.Mode.SAVE)));
	}

	// ---- geometry ----

	private int contentWidth(int width) {
		return Math.min(width, MAX_CONTENT_WIDTH);
	}

	private int contentLeft(int width) {
		return (width - this.contentWidth(width)) / 2;
	}

	private int contentRight(int width) {
		return this.contentLeft(width) + this.contentWidth(width);
	}

	private int centerX(int width) {
		return width / 2;
	}

	private int addY(int height) {
		return height - 66;
	}

	private int fileY(int height) {
		return height - 44;
	}

	private int addX(int width) {
		return this.centerX(width) - ADD_W / 2;
	}

	private int filesStartX(int width) {
		return this.centerX(width) - (3 * FILE_W + 2 * BTN_GAP) / 2;
	}

	private int loadX(int width) {
		return this.filesStartX(width);
	}

	private int saveX(int width) {
		return this.filesStartX(width) + FILE_W + BTN_GAP;
	}

	private int saveAsX(int width) {
		return this.filesStartX(width) + 2 * (FILE_W + BTN_GAP);
	}

	private int contextMenuX(int width) {
		return Math.max(2, Math.min(this.contextMenuX, width - MENU_W - 2));
	}

	private int contextMenuY(int height) {
		return Math.max(2, Math.min(this.contextMenuY, height - 2 * MENU_ROW_H - 2));
	}

	private int rowAt(int my, int listTop) {
		return (my - listTop) / ROW_HEIGHT;
	}

	private int maxScroll(int width, int height, int listTop) {
		int count = KeyBindManager.all().size();
		int visible = Math.max(1, (this.fileY(height) - listTop - 4) / ROW_HEIGHT);
		return Math.max(0, count - visible);
	}

	// ---- input ----

	public boolean mouseClicked(MouseButtonEvent event, int width, int height, int listTop) {
		int mx = (int) event.x();
		int my = (int) event.y();
		if (this.contextBind != null) {
			int menuX = this.contextMenuX(width);
			int menuY = this.contextMenuY(height);
			if (event.button() == 0) {
				if (mx >= menuX && mx < menuX + MENU_W) {
					if (my >= menuY && my < menuY + MENU_ROW_H) {
						Minecraft.getInstance().gui.setScreen(new AddKeyBindDialog(this.host, this.contextBind));
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
			this.layoutButtons(width, height);
			if (this.addButton.mouseClicked(event)) {
				return true;
			}
			if (this.loadButton.mouseClicked(event)) {
				return true;
			}
			if (this.saveButton.mouseClicked(event)) {
				return true;
			}
			if (this.saveAsButton.mouseClicked(event)) {
				return true;
			}
		} else if (event.button() == 1) {
			if (mx >= this.contentLeft(width) + SIDE && mx < this.contentRight(width) - SIDE) {
				List<KeyBind> binds = KeyBindManager.all();
				int index = this.scroll + this.rowAt(my, listTop);
				if (index >= 0 && index < binds.size()) {
					this.contextBind = binds.get(index);
					this.contextMenuX = mx;
					this.contextMenuY = my;
					return true;
				}
			}
		}
		return false;
	}

	public void mouseScrolled(double scrollY, int width, int height, int listTop) {
		int max = this.maxScroll(width, height, listTop);
		if (scrollY > 0.0) {
			this.scroll = Math.max(0, this.scroll - 1);
		} else if (scrollY < 0.0) {
			this.scroll = Math.min(max, this.scroll + 1);
		}
	}

	public boolean keyPressed(KeyEvent event) {
		if (this.contextBind != null && event.isEscape()) {
			this.contextBind = null;
			return true;
		}
		return false;
	}

	// ---- rendering ----

	public void render(GuiGraphicsExtractor graphics, Font font, int width, int height, int listTop, int mouseX, int mouseY) {
		this.scroll = Math.min(this.scroll, this.maxScroll(width, height, listTop));
		int listLeft = this.contentLeft(width) + SIDE;
		int listRight = this.contentRight(width) - SIDE;
		List<KeyBind> binds = KeyBindManager.all();
		int visible = Math.max(1, (this.fileY(height) - listTop - 4) / ROW_HEIGHT);
		for (int i = 0; i < visible; i++) {
			int index = this.scroll + i;
			if (index >= binds.size()) {
				break;
			}
			KeyBind bind = binds.get(index);
			int rowY = listTop + i * ROW_HEIGHT;
			if (mouseX >= listLeft && mouseX < listRight && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
				graphics.fill(listLeft, rowY, listRight, rowY + ROW_HEIGHT, ROW_HOVER);
			}
			UiText.scaledText(graphics, font, bind.getDisplayName(), listLeft + 4, rowY + 2, KEY_COLOR);
			UiText.scaledText(graphics, font, bind.getModeName() + ": " + bind.getFunction(), listLeft + 4, rowY + 12, CONTENT_COLOR);
		}

		this.layoutButtons(width, height);
		this.addButton.render(graphics, font, mouseX, mouseY);
		this.loadButton.render(graphics, font, mouseX, mouseY);
		this.saveButton.render(graphics, font, mouseX, mouseY);
		this.saveAsButton.render(graphics, font, mouseX, mouseY);

		if (this.contextBind != null) {
			int menuX = this.contextMenuX(width);
			int menuY = this.contextMenuY(height);
			graphics.fill(menuX, menuY, menuX + MENU_W, menuY + 2 * MENU_ROW_H, 0xE0101010);
			graphics.outline(menuX, menuY, MENU_W, 2 * MENU_ROW_H, 0xFF3A3A3A);
			String edit = tr("ui.keybinds.edit", "Edit");
			UiText.scaledText(graphics, font, edit, menuX + MENU_W / 2 - UiText.scaledWidth(font, edit) / 2, UiText.centerY(menuY, MENU_ROW_H), CONTENT_COLOR);
			String delete = tr("ui.keybinds.delete", "Delete");
			UiText.scaledText(graphics, font, delete, menuX + MENU_W / 2 - UiText.scaledWidth(font, delete) / 2, UiText.centerY(menuY + MENU_ROW_H, MENU_ROW_H), CONTENT_COLOR);
		}
	}

	private void layoutButtons(int width, int height) {
		this.addButton.setPosition(this.addX(width), this.addY(height));
		this.loadButton.setPosition(this.loadX(width), this.fileY(height));
		this.saveButton.setPosition(this.saveX(width), this.fileY(height));
		this.saveAsButton.setPosition(this.saveAsX(width), this.fileY(height));
	}

	private static String tr(String key, String fallback) {
		String value = I18n.t(key);
		return key.equals(value) ? fallback : value;
	}
}
