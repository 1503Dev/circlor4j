package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A colour picker row that opens a standalone popup window (coordinated by the ClickGuiScreen,
 * like the dropdown menu). The window contains:
 * <ul>
 *   <li>a square palette selecting saturation (X) and value/brightness (Y),</li>
 *   <li>a vertical hue strip on its right,</li>
 *   <li>a horizontal alpha strip below the square,</li>
 *   <li>a final-colour preview in the bottom-right corner,</li>
 *   <li>and a confirm button that writes the chosen colour to the bound path.</li>
 * </ul>
 * The colour is stored as a packed ARGB int on path "{id}/{option}/color".
 */
public class ColorPicker extends Component {
	private static final int PAD = 4;
	private static final int SQUARE = 70;
	private static final int HUE_W = 10;
	private static final int ALPHA_H = 10;
	private static final int BUTTON_H = 9;
	private static final int WIN_W = PAD + SQUARE + PAD + HUE_W + PAD;
	private static final int WIN_H = PAD + SQUARE + PAD + ALPHA_H + PAD + BUTTON_H + PAD;

	private static final int HOVER_COLOR = 0x40FFFFFF;
	private static final int WINDOW_BG_COLOR = 0xE0101010;
	private static final int WINDOW_BORDER_COLOR = 0xFF3A3A3A;
	private static final int LABEL_COLOR = 0xFFAAAAAA;
	private static final int CURSOR_DARK = 0xFF000000;
	private static final int CURSOR_LIGHT = 0xFFFFFFFF;

	private enum DragRegion { NONE, SQUARE, HUE, ALPHA }

	private final StatusManager status;
	private final String label;
	private final int defaultColor;
	private final Button confirmButton;
	private int x;
	private int y;
	private int width;
	private int height;
	private boolean windowOpen;
	private float hue;
	private float sat;
	private float val;
	private int alpha;
	private DragRegion dragRegion = DragRegion.NONE;

	public ColorPicker(StatusManager status, String path, String label, int defaultColor, int x, int y, int width, int height) {
		super(path);
		this.status = status;
		this.label = label;
		this.defaultColor = defaultColor;
		String confirm = I18n.t("ui.color_picker.confirm");
		String confirmText = "ui.color_picker.confirm".equals(confirm) ? "OK" : confirm;
		this.confirmButton = new TextButton(confirmText, 0, 0, this.buttonWidth(), BUTTON_H, this::confirm);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void syncStatus(double value) {
		// Row and window read the committed colour from the status store each frame.
	}

	public boolean isWindowOpen() {
		return this.windowOpen;
	}

	public void closeWindow() {
		this.windowOpen = false;
		this.dragRegion = DragRegion.NONE;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	/** Window origin, clamped to the screen so it never goes off-screen. */
	private int winX() {
		int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		return Math.max(0, Math.min(this.x + this.width + 2, screenW - WIN_W));
	}

	private int winY() {
		int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		return Math.max(0, Math.min(this.y, screenH - WIN_H));
	}

	private int squareX() {
		return this.winX() + PAD;
	}

	private int squareY() {
		return this.winY() + PAD;
	}

	private int hueX() {
		return this.winX() + PAD + SQUARE + PAD;
	}

	private int hueY() {
		return this.winY() + PAD;
	}

	private int alphaX() {
		return this.winX() + PAD;
	}

	private int alphaY() {
		return this.winY() + PAD + SQUARE + PAD;
	}

	private int buttonX() {
		return this.winX() + PAD;
	}

	private int buttonY() {
		return this.winY() + PAD + SQUARE + PAD + ALPHA_H + PAD;
	}

	private int buttonWidth() {
		return WIN_W - 2 * PAD;
	}

	private boolean containsRow(int mx, int my) {
		return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
	}

	public boolean isRow(int mx, int my) {
		return this.containsRow(mx, my);
	}

	public boolean containsWindow(int mx, int my) {
		return mx >= this.winX() && mx < this.winX() + WIN_W && my >= this.winY() && my < this.winY() + WIN_H;
	}

	private boolean inSquare(int mx, int my) {
		return mx >= this.squareX() && mx < this.squareX() + SQUARE && my >= this.squareY() && my < this.squareY() + SQUARE;
	}

	private boolean inHue(int mx, int my) {
		return mx >= this.hueX() && mx < this.hueX() + HUE_W && my >= this.hueY() && my < this.hueY() + SQUARE;
	}

	private boolean inAlpha(int mx, int my) {
		return mx >= this.alphaX() && mx < this.alphaX() + SQUARE && my >= this.alphaY() && my < this.alphaY() + ALPHA_H;
	}

	/** Row click (called by the owning CategoryWindow): toggles the popup window. */
	public boolean mouseClickedRow(MouseButtonEvent event) {
		if (event.button() != 0 || !this.containsRow((int) event.x(), (int) event.y())) {
			return false;
		}
		this.windowOpen = !this.windowOpen;
		if (this.windowOpen) {
			this.initFromStatus();
		}
		this.dragRegion = DragRegion.NONE;
		return true;
	}

	/** Window click (called by the ClickGuiScreen, top layer). */
	public boolean mouseClickedWindow(MouseButtonEvent event, Font font) {
		if (!this.windowOpen || event.button() != 0) {
			return false;
		}
		int mx = (int) event.x();
		int my = (int) event.y();
		if (!this.containsWindow(mx, my)) {
			return false;
		}
		if (this.inSquare(mx, my)) {
			this.dragRegion = DragRegion.SQUARE;
			this.applySquare(mx, my);
			return true;
		}
		if (this.inHue(mx, my)) {
			this.dragRegion = DragRegion.HUE;
			this.applyHue(mx, my);
			return true;
		}
		if (this.inAlpha(mx, my)) {
			this.dragRegion = DragRegion.ALPHA;
			this.applyAlpha(mx, my);
			return true;
		}
		if (this.confirmButton.mouseClicked(event)) {
			return true;
		}
		return true;
	}

	public boolean mouseDraggedWindow(int mx, int my) {
		if (this.dragRegion == DragRegion.NONE) {
			return false;
		}
		switch (this.dragRegion) {
			case SQUARE -> this.applySquare(mx, my);
			case HUE -> this.applyHue(mx, my);
			case ALPHA -> this.applyAlpha(mx, my);
			default -> {
			}
		}
		return true;
	}

	public boolean mouseReleasedWindow() {
		boolean wasDragging = this.dragRegion != DragRegion.NONE;
		this.dragRegion = DragRegion.NONE;
		return wasDragging;
	}

	private void initFromStatus() {
		int committed = this.status.getInt(this.getPath(), this.defaultColor);
		this.alpha = (committed >>> 24) & 0xFF;
		float[] hsv = rgbToHsv(committed);
		this.hue = hsv[0];
		this.sat = hsv[1];
		this.val = hsv[2];
	}

	private void applySquare(int mx, int my) {
		float sx = (mx - this.squareX()) / (float) (SQUARE - 1);
		float sy = (my - this.squareY()) / (float) (SQUARE - 1);
		this.sat = Math.max(0.0F, Math.min(1.0F, sx));
		this.val = Math.max(0.0F, Math.min(1.0F, 1.0F - sy));
	}

	private void applyHue(int mx, int my) {
		this.hue = Math.max(0.0F, Math.min(360.0F, (my - this.hueY()) / (float) (SQUARE - 1) * 360.0F));
	}

	private void applyAlpha(int mx, int my) {
		this.alpha = Math.max(0, Math.min(255, Math.round((mx - this.alphaX()) / (float) (SQUARE - 1) * 255.0F)));
	}

	private void confirm() {
		this.status.setValue(this, this.getPath(), this.workingArgb());
		this.windowOpen = false;
		this.dragRegion = DragRegion.NONE;
	}

	private int workingArgb() {
		return hsvToArgb(this.hue, this.sat, this.val, this.alpha);
	}

	private static float[] rgbToHsv(int argb) {
		float r = (float) ((argb >> 16) & 0xFF) / 255.0F;
		float g = (float) ((argb >> 8) & 0xFF) / 255.0F;
		float b = (float) (argb & 0xFF) / 255.0F;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float d = max - min;
		float h;
		if (d == 0.0F) {
			h = 0.0F;
		} else if (max == r) {
			h = 60.0F * (((g - b) / d) % 6.0F);
		} else if (max == g) {
			h = 60.0F * ((b - r) / d + 2.0F);
		} else {
			h = 60.0F * ((r - g) / d + 4.0F);
		}
		if (h < 0.0F) {
			h += 360.0F;
		}
		float s = max == 0.0F ? 0.0F : d / max;
		return new float[] {h, s, max};
	}

	private static int hsvToArgb(float h, float s, float v, int a) {
		float c = v * s;
		float hp = h / 60.0F;
		float x = c * (1.0F - Math.abs(hp % 2.0F - 1.0F));
		float r;
		float g;
		float b;
		switch ((int) hp % 6) {
			case 0 -> {
				r = c;
				g = x;
				b = 0.0F;
			}
			case 1 -> {
				r = x;
				g = c;
				b = 0.0F;
			}
			case 2 -> {
				r = 0.0F;
				g = c;
				b = x;
			}
			case 3 -> {
				r = 0.0F;
				g = x;
				b = c;
			}
			case 4 -> {
				r = x;
				g = 0.0F;
				b = c;
			}
			default -> {
				r = c;
				g = 0.0F;
				b = x;
			}
		}
		float m = v - c;
		int ri = (int) ((r + m) * 255.0F + 0.5F);
		int gi = (int) ((g + m) * 255.0F + 0.5F);
		int bi = (int) ((b + m) * 255.0F + 0.5F);
		return (a << 24) | (ri << 16) | (gi << 8) | bi;
	}

	public void renderRow(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		boolean hovered = this.containsRow(mouseX, mouseY);
		if (hovered) {
			graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, HOVER_COLOR);
		}

		int swatch = 10;
		int swatchH = 5;
		int swatchX = this.x + this.width - 2 - swatch;
		int swatchY = this.y + (this.height - swatchH) / 2;
		int committed = this.status.getInt(this.getPath(), this.defaultColor);
		graphics.fill(swatchX, swatchY, swatchX + swatch, swatchY + swatchH, committed);
		graphics.outline(swatchX, swatchY, swatch, swatchH, CURSOR_DARK);

		String labelText = UiText.fit(font, this.label, Math.max(0, swatchX - (this.x + 2) - 2));
		UiText.scaledText(graphics, font, labelText, this.x + 2, UiText.centerY(this.y, this.height), LABEL_COLOR);
	}

	public void renderWindow(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		if (!this.windowOpen) {
			return;
		}
		int winX = this.winX();
		int winY = this.winY();
		graphics.fill(winX, winY, winX + WIN_W, winY + WIN_H, WINDOW_BG_COLOR);
		graphics.outline(winX, winY, WIN_W, WIN_H, WINDOW_BORDER_COLOR);

		int sqX = this.squareX();
		int sqY = this.squareY();
		for (int cx = 0; cx < SQUARE; cx++) {
			float s = (float) cx / (SQUARE - 1);
			int top = hsvToArgb(this.hue, s, 1.0F, 255);
			graphics.fillGradient(sqX + cx, sqY, sqX + cx + 1, sqY + SQUARE, top, 0xFF000000);
		}
		int cursorX = sqX + Math.round(this.sat * (SQUARE - 1));
		int cursorY = sqY + Math.round((1.0F - this.val) * (SQUARE - 1));
		graphics.outline(cursorX - 2, cursorY - 2, 5, 5, CURSOR_DARK);
		graphics.outline(cursorX - 1, cursorY - 1, 3, 3, CURSOR_LIGHT);

		int hx = this.hueX();
		int hy = this.hueY();
		for (int cy = 0; cy < SQUARE; cy++) {
			int h = Math.round((float) cy / (SQUARE - 1) * 360.0F);
			graphics.fill(hx, hy + cy, hx + HUE_W, hy + cy + 1, hsvToArgb(h, 1.0F, 1.0F, 255));
		}
		int hueCursorY = hy + Math.round(this.hue / 360.0F * (SQUARE - 1));
		graphics.fill(hx - 1, hueCursorY - 1, hx + HUE_W + 1, hueCursorY + 1, CURSOR_DARK);
		graphics.fill(hx, hueCursorY, hx + HUE_W, hueCursorY + 1, CURSOR_LIGHT);

		int ax = this.alphaX();
		int ay = this.alphaY();
		for (int by = 0; by * 4 < ALPHA_H; by++) {
			for (int bx = 0; bx * 4 < SQUARE; bx++) {
				int cell = ((bx + by) & 1) == 0 ? 0xFF333333 : 0xFF555555;
				graphics.fill(ax + bx * 4, ay + by * 4, Math.min(ax + SQUARE, ax + bx * 4 + 4), Math.min(ay + ALPHA_H, ay + by * 4 + 4), cell);
			}
		}
		int rgb = this.workingArgb() & 0xFFFFFF;
		for (int cx = 0; cx < SQUARE; cx++) {
			int a = Math.round((float) cx / (SQUARE - 1) * 255.0F);
			graphics.fill(ax + cx, ay, ax + cx + 1, ay + ALPHA_H, (a << 24) | rgb);
		}
		int alphaCursorX = ax + Math.round(this.alpha / 255.0F * (SQUARE - 1));
		graphics.fill(alphaCursorX - 1, ay - 1, alphaCursorX + 1, ay + ALPHA_H + 1, CURSOR_DARK);
		graphics.fill(alphaCursorX, ay, alphaCursorX + 1, ay + ALPHA_H, CURSOR_LIGHT);

		int pvX = winX + PAD + SQUARE + PAD;
		int pvY = winY + PAD + SQUARE + PAD;
		graphics.fill(pvX, pvY, pvX + HUE_W, pvY + ALPHA_H, this.workingArgb());
		graphics.outline(pvX, pvY, HUE_W, ALPHA_H, CURSOR_DARK);

		int btnX = this.buttonX();
		int btnY = this.buttonY();
		this.confirmButton.setPosition(btnX, btnY);
		this.confirmButton.render(graphics, font, mouseX, mouseY);
	}
}