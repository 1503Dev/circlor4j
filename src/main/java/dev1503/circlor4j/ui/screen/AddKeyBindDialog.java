package dev1503.circlor4j.ui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev1503.circlor4j.client.keybind.KeyBind;
import dev1503.circlor4j.client.keybind.KeyBindManager;
import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.component.Button;
import dev1503.circlor4j.ui.component.TextButton;
import dev1503.circlor4j.ui.component.UiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Modal dialog for adding a keybind: a key-capture box (listens for a key combination), a
 * function name text field, a mode dropdown (toggle/hold) and Add/Cancel buttons.
 */
public class AddKeyBindDialog extends Screen {
	private static final int DIALOG_W = 250;
	private static final int DIALOG_H = 170;
	private static final int LABEL_W = 62;
	private static final int FIELD_H = 14;
	private static final int FIELD_X = 74;
	private static final int FIELD_W = DIALOG_W - 20 - FIELD_X;
	private static final int BTN_W = 70;
	private static final int BTN_H = 13;

	private static final int BG_COLOR = 0xD0101010;
	private static final int BORDER_COLOR = 0xFF3A3A3A;
	private static final int FIELD_COLOR = 0xFF222222;
	private static final int FIELD_ACTIVE_COLOR = 0xFF3A6EA5;
	private static final int HOVER_COLOR = 0x40FFFFFF;
	private static final int TITLE_COLOR = 0xFFFFFFFF;
	private static final int LABEL_COLOR = 0xFFAAAAAA;
	private static final int VALUE_COLOR = 0xFFFFFFFF;

	private final Screen returnScreen;
	private final KeyBind editTarget;
	private final Button addButton;
	private final Button cancelButton;
	private boolean capturing;
	private InputConstants.Key capturedKey;
	private boolean capturedShift;
	private boolean capturedCtrl;
	private boolean capturedAlt;
	private String function = "";
	private boolean functionFocused;
	private KeyBind.Mode mode = KeyBind.Mode.TOGGLE;
	private boolean modeDropdownOpen;

	public AddKeyBindDialog(Screen returnScreen) {
		this(returnScreen, null);
	}

	public AddKeyBindDialog(Screen returnScreen, KeyBind editTarget) {
		super(Component.literal("Add Keybind"));
		this.returnScreen = returnScreen;
		this.editTarget = editTarget;
		this.addButton = new TextButton(tr("ui.keybinds.add", "Add"), 0, 0, BTN_W, BTN_H, this::addBind);
		this.cancelButton = new TextButton(tr("ui.keybinds.cancel", "Cancel"), 0, 0, BTN_W, BTN_H, this::onClose);
		if (editTarget != null) {
			this.capturedKey = editTarget.getKey();
			this.capturedShift = editTarget.hasShift();
			this.capturedCtrl = editTarget.hasControl();
			this.capturedAlt = editTarget.hasAlt();
			this.function = editTarget.getFunction();
			this.mode = editTarget.getMode();
		}
	}

	/** Whether the dialog is currently awaiting a key to capture (so keybinds don't fire). */
	public static boolean isCapturing() {
		return Minecraft.getInstance().gui.screen() instanceof AddKeyBindDialog dialog && dialog.capturing;
	}

	@Override
	public void onClose() {
		if (this.returnScreen != null) {
			this.minecraft.gui.setScreen(this.returnScreen);
		} else {
			super.onClose();
		}
	}

	// ---- geometry ----

	private int dialogX() {
		return this.width / 2 - DIALOG_W / 2;
	}

	private int dialogY() {
		return this.height / 2 - DIALOG_H / 2;
	}

	private int fieldX() {
		return this.dialogX() + FIELD_X;
	}

	private int captureY() {
		return this.dialogY() + 30;
	}

	private int functionY() {
		return this.dialogY() + 54;
	}

	private int modeY() {
		return this.dialogY() + 78;
	}

	private int addX() {
		return this.dialogX() + (DIALOG_W / 2 - BTN_W - 4);
	}

	private int cancelX() {
		return this.dialogX() + (DIALOG_W / 2 + 4);
	}

	private int buttonsY() {
		return this.dialogY() + DIALOG_H - 26;
	}

	private boolean inRect(int mx, int my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private boolean inCapture(int mx, int my) {
		return this.inRect(mx, my, this.fieldX(), this.captureY(), FIELD_W, FIELD_H);
	}

	private boolean inFunction(int mx, int my) {
		return this.inRect(mx, my, this.fieldX(), this.functionY(), FIELD_W, FIELD_H);
	}

	private boolean inMode(int mx, int my) {
		return this.inRect(mx, my, this.fieldX(), this.modeY(), FIELD_W, FIELD_H);
	}

	// ---- input ----

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0) {
			return super.mouseClicked(event, doubleClick);
		}
		int mx = (int) event.x();
		int my = (int) event.y();

		if (this.inCapture(mx, my)) {
			this.capturing = true;
			this.functionFocused = false;
			this.modeDropdownOpen = false;
			return true;
		}
		if (this.inFunction(mx, my)) {
			this.capturing = false;
			this.functionFocused = true;
			this.modeDropdownOpen = false;
			return true;
		}
		if (this.inMode(mx, my)) {
			this.capturing = false;
			this.functionFocused = false;
			this.modeDropdownOpen = !this.modeDropdownOpen;
			return true;
		}
		if (this.modeDropdownOpen) {
			int optionY = this.modeY() + FIELD_H;
			if (this.inRect(mx, my, this.fieldX(), optionY, FIELD_W, FIELD_H)) {
				this.mode = KeyBind.Mode.TOGGLE;
				this.modeDropdownOpen = false;
				return true;
			}
			if (this.inRect(mx, my, this.fieldX(), optionY + FIELD_H, FIELD_W, FIELD_H)) {
				this.mode = KeyBind.Mode.HOLD;
				this.modeDropdownOpen = false;
				return true;
			}
			this.modeDropdownOpen = false;
		}
		if (this.addButton.mouseClicked(event)) {
			return true;
		}
		if (this.cancelButton.mouseClicked(event)) {
			return true;
		}
		this.functionFocused = false;
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.capturing) {
			if (event.isEscape()) {
				this.capturedKey = null;
			} else {
				this.capturedKey = InputConstants.getKey(event);
				this.capturedShift = event.hasShiftDown();
				this.capturedCtrl = event.hasControlDown();
				this.capturedAlt = event.hasAltDown();
			}
			this.capturing = false;
			return true;
		}
		if (this.functionFocused) {
			if (event.key() == InputConstants.KEY_BACKSPACE && !this.function.isEmpty()) {
				this.function = this.function.substring(0, this.function.length() - 1);
				return true;
			}
			if (event.key() == InputConstants.KEY_DELETE) {
				return true;
			}
			if (event.isEscape()) {
				this.functionFocused = false;
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (this.functionFocused && event.isAllowedChatCharacter()) {
			this.function = this.function + event.codepointAsString();
			return true;
		}
		return super.charTyped(event);
	}

	private void addBind() {
		if (this.capturedKey == null || this.function.isBlank()) {
			return;
		}
		if (!KeyBindManager.isKnownFunction(this.function)) {
			return;
		}
		KeyBind bind = new KeyBind(
			this.capturedKey,
			this.capturedShift,
			this.capturedCtrl,
			this.capturedAlt,
			this.function,
			this.mode
		);
		if (this.editTarget != null) {
			KeyBindManager.update(this.editTarget, bind);
		} else {
			if (KeyBindManager.isUsed(bind)) {
				return;
			}
			KeyBindManager.add(bind);
		}
		KeyBindManager.saveDefault();
		this.onClose();
	}

	// ---- rendering ----

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int dx = this.dialogX();
		int dy = this.dialogY();
		graphics.fill(0, 0, this.width, this.height, 0x80000000);
		graphics.fill(dx, dy, dx + DIALOG_W, dy + DIALOG_H, BG_COLOR);
		graphics.outline(dx, dy, DIALOG_W, DIALOG_H, BORDER_COLOR);

		String title = this.editTarget != null
			? tr("ui.keybinds.edit_title", "Edit Keybind")
			: tr("ui.keybinds.add_title", "Add Keybind");
		UiText.scaledText(graphics, this.font, title, dx + DIALOG_W / 2 - UiText.scaledWidth(this.font, title) / 2, dy + 8, TITLE_COLOR);

		this.drawLabel(graphics, tr("ui.keybinds.key", "Key"), this.captureY());
		String keyText = this.capturing ? "..." : (this.capturedKey == null ? "" : this.comboName());
		this.drawField(graphics, this.fieldX(), this.captureY(), FIELD_W, keyText, this.capturing, mouseX, mouseY);

		this.drawLabel(graphics, tr("ui.keybinds.function", "Function"), this.functionY());
		String funcText = this.function + (this.functionFocused ? "_" : "");
		this.drawField(graphics, this.fieldX(), this.functionY(), FIELD_W, funcText, this.functionFocused, mouseX, mouseY);

		this.drawLabel(graphics, tr("ui.keybinds.mode", "Mode"), this.modeY());
		this.drawField(graphics, this.fieldX(), this.modeY(), FIELD_W, this.mode.name().toLowerCase(), this.modeDropdownOpen, mouseX, mouseY);
		if (this.modeDropdownOpen) {
			int optionY = this.modeY() + FIELD_H;
			graphics.fill(this.fieldX(), optionY, this.fieldX() + FIELD_W, optionY + 2 * FIELD_H, FIELD_COLOR);
			graphics.outline(this.fieldX(), optionY, FIELD_W, 2 * FIELD_H, BORDER_COLOR);
			String toggle = tr("ui.keybinds.mode_toggle", "toggle");
			String hold = tr("ui.keybinds.mode_hold", "hold");
			this.drawDropdownOption(graphics, toggle, this.fieldX(), optionY, mouseX, mouseY);
			this.drawDropdownOption(graphics, hold, this.fieldX(), optionY + FIELD_H, mouseX, mouseY);
		}

		String confirmLabel = this.editTarget != null
			? tr("ui.keybinds.save", "Save")
			: tr("ui.keybinds.add", "Add");
		this.addButton.setLabel(confirmLabel);
		this.addButton.setPosition(this.addX(), this.buttonsY());
		this.cancelButton.setPosition(this.cancelX(), this.buttonsY());
		this.addButton.render(graphics, this.font, mouseX, mouseY);
		this.cancelButton.render(graphics, this.font, mouseX, mouseY);
	}

	private String comboName() {
		StringBuilder sb = new StringBuilder();
		if (this.capturedCtrl) {
			sb.append("Ctrl+");
		}
		if (this.capturedAlt) {
			sb.append("Alt+");
		}
		if (this.capturedShift) {
			sb.append("Shift+");
		}
		sb.append(this.capturedKey.getName());
		return sb.toString();
	}

	private void drawLabel(GuiGraphicsExtractor graphics, String label, int y) {
		UiText.scaledText(graphics, this.font, label, this.dialogX() + 10, UiText.centerY(y, FIELD_H), LABEL_COLOR);
	}

	private void drawField(GuiGraphicsExtractor graphics, int x, int y, int w, String text, boolean active, int mouseX, int mouseY) {
		graphics.fill(x, y, x + w, y + FIELD_H, active ? FIELD_ACTIVE_COLOR : FIELD_COLOR);
		graphics.outline(x, y, w, FIELD_H, BORDER_COLOR);
		UiText.scaledText(graphics, this.font, text, x + 3, UiText.centerY(y, FIELD_H), VALUE_COLOR);
	}

	private void drawDropdownOption(GuiGraphicsExtractor graphics, String label, int x, int y, int mouseX, int mouseY) {
		if (this.inRect(mouseX, mouseY, x, y, FIELD_W, FIELD_H)) {
			graphics.fill(x, y, x + FIELD_W, y + FIELD_H, HOVER_COLOR);
		}
		UiText.scaledText(graphics, this.font, label, x + 3, UiText.centerY(y, FIELD_H), VALUE_COLOR);
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