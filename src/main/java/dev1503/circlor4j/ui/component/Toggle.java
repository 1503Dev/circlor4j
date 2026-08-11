package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A toggle row that also acts as a container for sub-rows (e.g. a module's sliders).
 * Left-click toggles the bound status, right-click expands/collapses the children.
 * The frame around an expanded toggle is drawn by the owning CategoryWindow.
 */
public class Toggle extends Component {
	public static final int HIGHLIGHT_COLOR = 0xFF2697F3;

	private static final int HOVER_COLOR = 0x40FFFFFF;
	private static final int ENABLED_BG_COLOR = 0x2A2697F3;
	private static final int TEXT_COLOR = 0xFFAAAAAA;
	private static final int ENABLED_TEXT_COLOR = 0xFF2697F3;

	private final StatusManager status;
	private final String label;
	private final int height;
	private final List<StatusWidget> children = new ArrayList<>();
	private int x;
	private int y;
	private int width;
	private boolean checked;
	private boolean expanded;
	private int groupStart;
	private int groupEnd;

	public Toggle(StatusManager status, String path, String label, int x, int y, int width, int height) {
		super(path);
		this.status = status;
		this.label = label;
		this.height = height;
		this.x = x;
		this.y = y;
		this.width = width;
		this.checked = status.getBoolean(path, false);
	}

	@Override
	public void syncStatus(double value) {
		this.checked = value >= 1.0;
	}

	public boolean isChecked() {
		return this.checked;
	}

	public void addChild(StatusWidget child) {
		this.children.add(child);
	}

	public List<StatusWidget> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public boolean isExpanded() {
		return this.expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	void setGroupRange(int start, int end) {
		this.groupStart = start;
		this.groupEnd = end;
	}

	int getGroupStart() {
		return this.groupStart;
	}

	int getGroupEnd() {
		return this.groupEnd;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean contains(int mx, int my) {
		return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
	}

	public boolean mouseClicked(MouseButtonEvent event) {
		int mx = (int) event.x();
		int my = (int) event.y();
		if (mx < this.x || mx >= this.x + this.width || my < this.y || my >= this.y + this.height) {
			return false;
		}
		if (event.button() == 1) {
			if (this.hasChildren()) {
				this.expanded = !this.expanded;
			}
			return true;
		}
		if (event.button() == 0) {
			this.checked = !this.checked;
			this.status.setValue(this, this.getPath(), this.checked ? 1.0 : 0.0);
			return true;
		}
		return false;
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		boolean hovered = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
		if (hovered) {
			graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, HOVER_COLOR);
		} else if (this.checked) {
			graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, ENABLED_BG_COLOR);
		}
		String text = UiText.fit(font, this.label, Math.max(0, this.width - 4));
		UiText.scaledText(graphics, font, text, this.x + 2, UiText.centerY(this.y, this.height), this.checked ? ENABLED_TEXT_COLOR : TEXT_COLOR);
	}
}
