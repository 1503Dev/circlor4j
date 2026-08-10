package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;
import java.math.BigDecimal;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A slider with two draggable thumbs (min and max) on a shared track. Thumbs may overlap or
 * cross; the row's value text shows the current "min..max" range. Values are stored on the
 * "{id}/{option}/min" and "{id}/{option}/max" paths.
 */
public class RangeSlider extends Component {
	private static final int TRACK_HEIGHT = 2;
	private static final int THUMB_WIDTH = 2;
	private static final int RIGHT_INSET = 3;
	private static final int LEFT_INSET = 2;
	private static final int GRAB_DISTANCE = 3;

	private static final int TRACK_COLOR = 0xFF555555;
	private static final int THUMB_COLOR = 0xFFCCCCCC;
	private static final int LABEL_COLOR = 0xFFAAAAAA;
	private static final int VALUE_COLOR = 0xFFFFFFFF;

	private enum DragThumb { NONE, MIN, MAX }

	private final StatusManager status;
	private final String label;
	private final String minPath;
	private final String maxPath;
	private final double min;
	private final double max;
	private final double step;
	private final int height;
	private int x;
	private int y;
	private int width;
	private double valueMin;
	private double valueMax;
	private DragThumb dragging = DragThumb.NONE;

	public RangeSlider(
		StatusManager status,
		String minPath,
		String maxPath,
		String label,
		double min,
		double max,
		double step,
		double defaultMin,
		double defaultMax,
		int x,
		int y,
		int width,
		int height
	) {
		super(minPath);
		this.status = status;
		this.label = label;
		this.minPath = minPath;
		this.maxPath = maxPath;
		this.min = min;
		this.max = max;
		this.step = step;
		this.height = height;
		this.x = x;
		this.y = y;
		this.width = width;
		this.valueMin = this.clamp(defaultMin);
		this.valueMax = this.clamp(defaultMax);
	}

	@Override
	public void syncStatus(double value) {
		this.valueMin = this.clamp(this.status.getDouble(this.minPath, this.min));
		this.valueMax = this.clamp(this.status.getDouble(this.maxPath, this.max));
	}

	public int getHeight() {
		return this.height;
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

	private int trackLength() {
		return this.trackRight() - this.trackStart() - THUMB_WIDTH;
	}

	private double clamp(double v) {
		return Math.max(this.min, Math.min(this.max, v));
	}

	private int valueToX(double v) {
		double t = (this.clamp(v) - this.min) / (this.max - this.min);
		return this.trackStart() + (int) Math.round(t * this.trackLength());
	}

	private double xToValue(int px) {
		double t = (px - this.trackStart()) / (double) this.trackLength();
		t = Math.max(0.0, Math.min(1.0, t));
		double v = this.min + t * (this.max - this.min);
		return this.min + Math.round((v - this.min) / this.step) * this.step;
	}

	private boolean inTrack(int mx, int my) {
		return mx >= this.trackStart() && mx < this.trackRight() && my >= this.y && my < this.y + this.height;
	}

	private boolean nearThumb(int mx, double value) {
		return Math.abs(mx - this.valueToX(value)) <= GRAB_DISTANCE;
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
		boolean nearMin = this.nearThumb(mx, this.valueMin);
		boolean nearMax = this.nearThumb(mx, this.valueMax);
		if (nearMin && nearMax) {
			this.dragging = Math.abs(mx - this.valueToX(this.valueMin)) <= Math.abs(mx - this.valueToX(this.valueMax))
				? DragThumb.MIN
				: DragThumb.MAX;
		} else if (nearMin) {
			this.dragging = DragThumb.MIN;
		} else if (nearMax) {
			this.dragging = DragThumb.MAX;
		} else {
			return false;
		}
		this.updateFromMouse(mx);
		return true;
	}

	public boolean mouseDragged(int mx) {
		if (this.dragging == DragThumb.NONE) {
			return false;
		}
		this.updateFromMouse(mx);
		return true;
	}

	public boolean mouseReleased() {
		boolean wasDragging = this.dragging != DragThumb.NONE;
		this.dragging = DragThumb.NONE;
		return wasDragging;
	}

	private void updateFromMouse(int mx) {
		double v = this.clamp(this.xToValue(mx));
		if (this.dragging == DragThumb.MIN) {
			if (v > this.valueMax) {
				this.dragging = DragThumb.MAX;
				double newMin = this.valueMax;
				this.valueMax = v;
				this.valueMin = newMin;
				this.status.setValue(this, this.minPath, newMin);
				this.status.setValue(this, this.maxPath, v);
			} else if (v != this.valueMin) {
				this.valueMin = v;
				this.status.setValue(this, this.minPath, v);
			}
		} else if (this.dragging == DragThumb.MAX) {
			if (v < this.valueMin) {
				this.dragging = DragThumb.MIN;
				double newMax = this.valueMin;
				this.valueMin = v;
				this.valueMax = newMax;
				this.status.setValue(this, this.minPath, v);
				this.status.setValue(this, this.maxPath, newMax);
			} else if (v != this.valueMax) {
				this.valueMax = v;
				this.status.setValue(this, this.maxPath, v);
			}
		}
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		this.valueMin = this.clamp(this.status.getDouble(this.minPath, this.min));
		this.valueMax = this.clamp(this.status.getDouble(this.maxPath, this.max));

		int halfHeight = this.height / 2;

		String valueText = format(this.valueMin) + ".." + format(this.valueMax);
		int valueWidth = UiText.scaledWidth(font, valueText);
		int valueX = this.x + this.width - RIGHT_INSET - valueWidth;
		int labelMax = Math.max(0, valueX - (this.x + LEFT_INSET) - 2);
		String labelText = UiText.fit(font, this.label, labelMax);
		int textY = this.y + Math.max(1, (halfHeight - 5) / 2);
		UiText.scaledText(graphics, font, labelText, this.x + LEFT_INSET, textY, LABEL_COLOR);
		UiText.scaledText(graphics, font, valueText, valueX, textY, VALUE_COLOR);

		int trackY = this.y + halfHeight + (halfHeight - TRACK_HEIGHT) / 2;
		graphics.fill(this.trackStart(), trackY, this.trackRight(), trackY + TRACK_HEIGHT, TRACK_COLOR);

		int minX = Math.max(this.trackStart(), Math.min(this.trackRight() - THUMB_WIDTH, this.valueToX(this.valueMin)));
		int maxX = Math.max(this.trackStart(), Math.min(this.trackRight() - THUMB_WIDTH, this.valueToX(this.valueMax)));
		graphics.fill(minX, trackY, minX + THUMB_WIDTH, trackY + TRACK_HEIGHT, THUMB_COLOR);
		graphics.fill(maxX, trackY, maxX + THUMB_WIDTH, trackY + TRACK_HEIGHT, THUMB_COLOR);
	}

	private String format(double value) {
		return String.format(Locale.ROOT, "%." + decimalsFromStep(this.step) + "f", value);
	}

	private static int decimalsFromStep(double step) {
		if (step == Math.floor(step)) {
			return 0;
		}
		String plain = BigDecimal.valueOf(step).stripTrailingZeros().toPlainString();
		int dot = plain.indexOf('.');
		return dot < 0 ? 0 : plain.length() - dot - 1;
	}
}