package dev1503.circlor4j.ui.screen;

import dev1503.circlor4j.client.keybind.KeyBindManager;
import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.component.UiText;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * File dialog for keybind JSON files inside <mc>/circlor4j/keybinds/.
 * Load mode: pick a file to load. Save mode: type a name (or pick an existing file) and save.
 */
public class FilePickerScreen extends Screen {
	public enum Mode { LOAD, SAVE }

	private static final int SIDE = 10;
	private static final int LIST_TOP = 22;
	private static final int ROW_HEIGHT = 16;
	private static final int SAVE_FIELD_H = 14;
	private static final int SAVE_FIELD_Y_OFFSET = 40;
	private static final int BTN_W = 70;
	private static final int BTN_H = 13;

	private static final int BODY_COLOR = 0xC0101010;
	private static final int ROW_HOVER = 0x40FFFFFF;
	private static final int TITLE_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR = 0xFFAAAAAA;
	private static final int VALUE_COLOR = 0xFFFFFFFF;
	private static final int FIELD_COLOR = 0xFF222222;
	private static final int FIELD_ACTIVE_COLOR = 0xFF3A6EA5;
	private static final int BORDER_COLOR = 0xFF3A3A3A;

	private final Screen returnScreen;
	private final Mode mode;
	private String filename = "default";
	private boolean filenameFocused;
	private int scroll;

	public FilePickerScreen(Screen returnScreen, Mode mode) {
		super(Component.literal(mode == Mode.LOAD ? "Load Keybinds" : "Save Keybinds"));
		this.returnScreen = returnScreen;
		this.mode = mode;
	}

	@Override
	public void onClose() {
		if (this.returnScreen != null) {
			this.minecraft.gui.setScreen(this.returnScreen);
		} else {
			super.onClose();
		}
	}

	private Path dir() {
		return KeyBindManager.keybindsDir();
	}

	private List<Path> listJsonFiles() {
		Path dir = this.dir();
		if (dir == null || !Files.isDirectory(dir)) {
			return new ArrayList<>();
		}
		try (var stream = Files.list(dir)) {
			return stream
				.filter(p -> p.getFileName().toString().endsWith(".json"))
				.sorted()
				.collect(Collectors.toList());
		} catch (IOException e) {
			return new ArrayList<>();
		}
	}

	private int saveFieldY() {
		return this.height - SAVE_FIELD_Y_OFFSET - SAVE_FIELD_H;
	}

	private int saveFieldX() {
		return SIDE;
	}

	private int saveFieldW() {
		return this.width - 2 * SIDE - BTN_W - 8;
	}

	private int saveBtnX() {
		return this.width - SIDE - BTN_W;
	}

	private int saveBtnY() {
		return this.saveFieldY();
	}

	private boolean inSaveField(int mx, int my) {
		return mx >= this.saveFieldX() && mx < this.saveFieldX() + this.saveFieldW() && my >= this.saveFieldY() && my < this.saveFieldY() + SAVE_FIELD_H;
	}

	private boolean inSaveBtn(int mx, int my) {
		return mx >= this.saveBtnX() && mx < this.saveBtnX() + BTN_W && my >= this.saveBtnY() && my < this.saveBtnY() + BTN_H;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0) {
			return super.mouseClicked(event, doubleClick);
		}
		int mx = (int) event.x();
		int my = (int) event.y();

		if (this.mode == Mode.SAVE && this.inSaveField(mx, my)) {
			this.filenameFocused = true;
			return true;
		}
		if (this.mode == Mode.SAVE && this.inSaveBtn(mx, my)) {
			this.doSave();
			return true;
		}

		List<Path> files = this.listJsonFiles();
		int index = this.scroll + (my - LIST_TOP) / ROW_HEIGHT;
		if (index >= 0 && index < files.size() && mx >= SIDE && mx < this.width - SIDE) {
			Path file = files.get(index);
			if (this.mode == Mode.LOAD) {
				KeyBindManager.loadFrom(file);
				this.onClose();
			} else {
				this.filename = file.getFileName().toString().replace(".json", "");
				this.filenameFocused = true;
			}
			return true;
		}

		this.filenameFocused = false;
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.mode == Mode.SAVE && this.filenameFocused) {
			if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_BACKSPACE && !this.filename.isEmpty()) {
				this.filename = this.filename.substring(0, this.filename.length() - 1);
				return true;
			}
			if (event.key() == 261) {
				return true;
			}
			if (event.isEscape()) {
				this.filenameFocused = false;
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (this.mode == Mode.SAVE && this.filenameFocused && event.isAllowedChatCharacter()) {
			this.filename = this.filename + event.codepointAsString();
			return true;
		}
		return super.charTyped(event);
	}

	private void doSave() {
		String name = this.filename.trim();
		if (name.isEmpty()) {
			name = "default";
		}
		if (!name.endsWith(".json")) {
			name = name + ".json";
		}
		Path dir = this.dir();
		if (dir != null) {
			KeyBindManager.saveTo(dir.resolve(name));
			this.onClose();
		}
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

	private int maxScroll() {
		int count = this.listJsonFiles().size();
		int visible = Math.max(1, this.bottomLimit() / ROW_HEIGHT);
		return Math.max(0, count - visible);
	}

	private int bottomLimit() {
		return this.saveFieldY() - LIST_TOP - 6;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, BODY_COLOR);
		String title = tr(this.mode == Mode.LOAD ? "ui.keybinds.load" : "ui.keybinds.save_as", this.mode == Mode.LOAD ? "Load" : "Save As");
		UiText.scaledText(graphics, this.font, title, this.width / 2 - UiText.scaledWidth(this.font, title) / 2, 10, TITLE_COLOR);

		this.scroll = Math.min(this.scroll, this.maxScroll());
		List<Path> files = this.listJsonFiles();
		int visible = Math.max(1, this.bottomLimit() / ROW_HEIGHT);
		for (int i = 0; i < visible; i++) {
			int index = this.scroll + i;
			if (index >= files.size()) {
				break;
			}
			Path file = files.get(index);
			int rowY = LIST_TOP + i * ROW_HEIGHT;
			if (mouseX >= SIDE && mouseX < this.width - SIDE && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
				graphics.fill(SIDE, rowY, this.width - SIDE, rowY + ROW_HEIGHT, ROW_HOVER);
			}
			UiText.scaledText(graphics, this.font, file.getFileName().toString(), SIDE + 4, UiText.centerY(rowY, ROW_HEIGHT), TEXT_COLOR);
		}

		if (this.mode == Mode.SAVE) {
			String fieldText = this.filename + (this.filenameFocused ? "_" : "");
			graphics.fill(this.saveFieldX(), this.saveFieldY(), this.saveFieldX() + this.saveFieldW(), this.saveFieldY() + SAVE_FIELD_H, this.filenameFocused ? FIELD_ACTIVE_COLOR : FIELD_COLOR);
			graphics.outline(this.saveFieldX(), this.saveFieldY(), this.saveFieldW(), SAVE_FIELD_H, BORDER_COLOR);
			UiText.scaledText(graphics, this.font, fieldText, this.saveFieldX() + 3, UiText.centerY(this.saveFieldY(), SAVE_FIELD_H), VALUE_COLOR);

			String saveLabel = tr("ui.keybinds.save", "Save");
			boolean hovered = this.inSaveBtn(mouseX, mouseY);
			graphics.fill(this.saveBtnX(), this.saveBtnY(), this.saveBtnX() + BTN_W, this.saveBtnY() + BTN_H, hovered ? 0xFF3A6EA5 : 0xFF222222);
			graphics.outline(this.saveBtnX(), this.saveBtnY(), BTN_W, BTN_H, BORDER_COLOR);
			int textX = this.saveBtnX() + BTN_W / 2 - UiText.scaledWidth(this.font, saveLabel) / 2;
			UiText.scaledText(graphics, this.font, saveLabel, textX, UiText.centerY(this.saveBtnY(), BTN_H), VALUE_COLOR);
		}
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