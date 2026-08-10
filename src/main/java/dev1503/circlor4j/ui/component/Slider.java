package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;
import java.math.BigDecimal;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * Slider row: left-aligned label, right-aligned value text, slider on the far right.
 * Supports a step, minimum, maximum and a default value (read from the StatusManager store).
 */
public class Slider extends Component {
	private static final int TRACK_HEIGHT = 2;
	private static final int THUMB_WIDTH = 2;
	private static final int RIGHT_INSET = 3;
	private static final int LEFT_INSET = 2;

	private static final int TRACK_COLOR = 0xFF555555;
	private static final int THUMB_COLOR = 0xFFCCCCCC;
	private static final int LABEL_COLOR = 0xFFAAAAAA;
	private static final int VALUE_COLOR = 0xFFFFFFFF;

	private final StatusManager status;
	private final String label;
	private final double min;
	private final double max;
	private final double step;
	private final int height;
	private int x;
	private int y;
	private int width;
	private double value;
	private boolean dragging;

	public Slider(StatusManager status, String path, String label, double min, double max, double step, int x, int y, int width, int height) {
		super(path);
		this.status = status;
		this.label = label;
		this.min = min;
		this.max = max;
		this.step = step;
		this.height = height;
		this.x = x;
		this.y = y;
		this.width = width;
		this.value = this.clamp(status.getDouble(path, min));
	}

	@Override
	public void syncStatus(double value) {
		this.value = this.clamp(value);
	}

	public boolean isDragging() {
		return this.dragging;
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

	/** Adjusts the value by one step (used by left/right keyboard input while hovered). */
	public void stepBy(boolean forward) {
		double v = this.clamp(this.value + (forward ? this.step : -this.step));
		if (v != this.value) {
			this.value = v;
			this.status.setValue(this, this.getPath(), v);
		}
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	private int trackRight() {
		return this.x + this.width - RIGHT_INSET;
	}

	private int trackStart() {
		return this.x + LEFT_INSET;
	}

	private int trackWidth() {
		return this.trackRight() - this.trackStart();
	}

	private boolean inTrack(int mx, int my) {
		return mx >= this.trackStart() && mx < this.trackRight() && my >= this.y && my < this.y + this.height;
	}

	public boolean mouseClicked(MouseButtonEvent event) {
		if (event.button() != 0) {
			return false;
		}
		int mx = (int) event.x();
		int my = (int) event.y();
		if (!this.inTrack(mx, my)) {
			return false;
		}
		this.dragging = true;
		this.updateFromMouse(mx);
		return true;
	}

	public boolean mouseDragged(int mx) {
		if (!this.dragging) {
			return false;
		}
		this.updateFromMouse(mx);
		return true;
	}

	public boolean mouseReleased() {
		boolean wasDragging = this.dragging;
		this.dragging = false;
		return wasDragging;
	}

	private void updateFromMouse(int mx) {
		double t = (mx - this.trackStart()) / (double) this.trackWidth();
		t = Math.max(0.0, Math.min(1.0, t));
		double v = this.min + t * (this.max - this.min);
		v = this.min + Math.round((v - this.min) / this.step) * this.step;
		v = this.clamp(v);
		if (v != this.value) {
			this.value = v;
			this.status.setValue(this, this.getPath(), v);
		}
	}

	private double clamp(double v) {
		return Math.max(this.min, Math.min(this.max, v));
	}

	private String formatValue() {
		return String.format(Locale.ROOT, "%." + decimalsFromStep(this.step) + "f", this.value);
	}

	private static int decimalsFromStep(double step) {
		if (step == Math.floor(step)) {
			return 0;
		}
		String plain = BigDecimal.valueOf(step).stripTrailingZeros().toPlainString();
		int dot = plain.indexOf('.');
		return dot < 0 ? 0 : plain.length() - dot - 1;
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		int halfHeight = this.height / 2;

		String valueText = this.formatValue();
		int valueWidth = UiText.scaledWidth(font, valueText);
		int valueX = this.x + this.width - RIGHT_INSET - valueWidth;
		int labelMax = Math.max(0, valueX - (this.x + LEFT_INSET) - 2);
		String labelText = UiText.fit(font, this.label, labelMax);
		int textY = this.y + Math.max(1, (halfHeight - 5) / 2);
		UiText.scaledText(graphics, font, labelText, this.x + LEFT_INSET, textY, LABEL_COLOR);
		UiText.scaledText(graphics, font, valueText, valueX, textY, VALUE_COLOR);

		int trackY = this.y + halfHeight + (halfHeight - TRACK_HEIGHT) / 2;
		graphics.fill(this.trackStart(), trackY, this.trackRight(), trackY + TRACK_HEIGHT, TRACK_COLOR);

		double t = (this.value - this.min) / (this.max - this.min);
		int thumbCenter = this.trackStart() + (int) Math.round(t * this.trackWidth());
		int thumbLeft = Math.max(this.trackStart(), Math.min(this.trackRight() - THUMB_WIDTH, thumbCenter - THUMB_WIDTH / 2));
		graphics.fill(thumbLeft, trackY, thumbLeft + THUMB_WIDTH, trackY + TRACK_HEIGHT, THUMB_COLOR);
	}
}
