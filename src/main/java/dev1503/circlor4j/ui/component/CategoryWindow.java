package dev1503.circlor4j.ui.component;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.client.module.ModuleManager;
import dev1503.circlor4j.client.module.modules.LanguageModule;
import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.StatusManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

/**
 * A window holding one category's module toggles; each toggle may carry sub-rows
 * (sliders) shown when expanded. Clicking the header collapses/expands the window.
 * Draggable by the header, resizable by the right/bottom edges, scrollable.
 */
public class CategoryWindow {
	public static final int WIDTH = 72;
	public static final int HEADER_HEIGHT = 9;
	public static final int ROW_HEIGHT = 8;
	public static final int MAX_VISIBLE_ROWS = 8;

	private static final int EDGE_SIZE = 4;
	private static final int MIN_WIDTH = 50;
	private static final int BOTTOM_PADDING = 2;
	private static final int CHILD_INDENT = 2;

	public static final String KEYBINDS_ACTION_PATH = "keybinds/action";
	private static final float BORDER_THICKNESS = 0.5F;
	private static final int MIN_HEIGHT = HEADER_HEIGHT + ROW_HEIGHT + BOTTOM_PADDING;
	private static final int DEFAULT_HEIGHT = (HEADER_HEIGHT + MAX_VISIBLE_ROWS * ROW_HEIGHT + BOTTOM_PADDING) * 2;

	private static final int BODY_COLOR = 0xD0101010;
	private static final int HEADER_COLOR = 0xE0202020;
	private static final int HEADER_ACTIVE_COLOR = 0xE0505050;
	private static final int OUTLINE_COLOR = 0xFF3A3A3A;
	private static final int EXPANDED_OUTLINE_COLOR = 0xFFAAAAAA;
	private static final int SCROLLBAR_COLOR = 0xFF9A9A9A;
	private static final int HEADER_TEXT_COLOR = 0xFFFFFFFF;

	private static final CursorType RESIZE_NWSE = CursorType.createStandardCursor(
		GLFW.GLFW_RESIZE_NWSE_CURSOR, "resize_nwse", CursorTypes.RESIZE_ALL
	);

	private enum ResizeMode { NONE, RIGHT, BOTTOM, BOTTOM_RIGHT }

	private final StatusManager status;
	private final ModuleCategory category;
	private final List<StatusWidget> rootRows = new ArrayList<>();
	private final List<Toggle> toggles = new ArrayList<>();
	private final List<StatusWidget> rows = new ArrayList<>();
	private final List<Integer> rowIndent = new ArrayList<>();
	private int x;
	private int y;
	private int width = WIDTH;
	private int height;
	private int screenWidth = 640;
	private int screenHeight = 480;
	private int scroll;
	private boolean dragging;
	private boolean collapsed;
	private int dragOffsetX;
	private int dragOffsetY;
	private ResizeMode resizeMode = ResizeMode.NONE;
	private int resizeStartX;
	private int resizeStartY;
	private int resizeStartWidth;
	private int resizeStartHeight;
	private boolean headerPressed;
	private boolean headerMoved;
	private int pressX;
	private int pressY;
	private boolean suppressHover;

	public CategoryWindow(StatusManager status, ModuleCategory category, int x, int y) {
		this.status = status;
		this.category = category;
		this.x = x;
		this.y = y;
		if (category == ModuleCategory.CIRCLOR) {
			Dropdown language = this.createLanguageDropdown(status, x, y);
			this.rootRows.add(language);
			status.addWidget(language);
			ActionButton keybinds = new ActionButton(status, KEYBINDS_ACTION_PATH, tr("ui.keybinds.name", "Keybinds"), x, y, WIDTH, ROW_HEIGHT);
			this.rootRows.add(keybinds);
			status.addWidget(keybinds);
		}
		for (Module module : ModuleManager.byCategory(category)) {
			if (!module.isShownInGui()) {
				continue;
			}
			Toggle toggle = new Toggle(status, module.getEnabledPath(), module.getName(), x, y, WIDTH, ROW_HEIGHT);
			for (Module.Setting setting : module.getSettings()) {
				if (setting instanceof Module.DoubleSetting s) {
					Slider slider = new Slider(
						status, s.path(), tr(s.labelKey(), s.labelFallback()), s.min(), s.max(), s.step(), x, y, WIDTH, ROW_HEIGHT
					);
					slider.setShowCondition(s.showCondition());
					slider.setHideCondition(s.hideCondition());
					if (s.parentOption() != null) {
						Toggle parent = findChildToggle(toggle, module.getId() + "/" + s.parentOption() + "/enabled");
						if (parent != null) {
							parent.addChild(slider);
						} else {
							toggle.addChild(slider);
						}
					} else {
						toggle.addChild(slider);
					}
				} else if (setting instanceof Module.RangeSetting s) {
					RangeSlider range = new RangeSlider(
						status, s.minPath(), s.maxPath(), tr(s.labelKey(), s.labelFallback()), s.min(), s.max(), s.step(),
						s.defaultMin(), s.defaultMax(), x, y, WIDTH, ROW_HEIGHT
					);
					toggle.addChild(range);
				} else if (setting instanceof Module.DropdownSetting s) {
					String[] items = new String[s.itemKeys().length];
					for (int i = 0; i < items.length; i++) {
						items[i] = s.itemKeys()[i] != null ? tr(s.itemKeys()[i], s.itemFallbacks()[i]) : s.itemFallbacks()[i];
					}
					Dropdown dropdown = new Dropdown(
						status, s.path(), tr(s.labelKey(), s.labelFallback()), items, s.defaultIndex(), x, y, WIDTH, ROW_HEIGHT
					);
					dropdown.setShowCondition(s.showCondition());
					toggle.addChild(dropdown);
				} else if (setting instanceof Module.ToggleSetting s) {
					Toggle subToggle = new Toggle(status, s.path(), tr(s.labelKey(), s.labelFallback()), x, y, WIDTH, ROW_HEIGHT);
					if (s.parentOption() != null) {
						Toggle parent = findChildToggle(toggle, module.getId() + "/" + s.parentOption() + "/enabled");
						if (parent != null) {
							parent.addChild(subToggle);
							continue;
						}
					}
					toggle.addChild(subToggle);
				} else if (setting instanceof Module.ColorSetting s) {
					ColorPicker picker = new ColorPicker(
						status, s.path(), tr(s.labelKey(), s.labelFallback()), s.defaultColor(), x, y, WIDTH, ROW_HEIGHT
					);
					picker.setShowCondition(s.showCondition());
					Toggle parent = findChildToggle(toggle, module.getId() + "/" + s.parentOption() + "/enabled");
					if (parent != null) {
						parent.addChild(picker);
					} else {
						toggle.addChild(picker);
					}
				}
			}
			this.toggles.add(toggle);
			status.addWidget(toggle);
			for (StatusWidget child : toggle.getChildren()) {
				status.addWidget(child);
			}
		}
		this.height = DEFAULT_HEIGHT;
	}

	public ModuleCategory getCategory() {
		return this.category;
	}

	public List<Toggle> getToggles() {
		return new ArrayList<>(this.toggles);
	}

	public List<String> getExpandedPaths() {
		List<String> paths = new ArrayList<>();
		for (Toggle toggle : this.toggles) {
			this.collectExpandedPaths(toggle, paths);
		}
		return paths;
	}

	private void collectExpandedPaths(Toggle toggle, List<String> paths) {
		if (toggle.isExpanded()) {
			paths.add(toggle.getPath());
		}
		for (StatusWidget child : toggle.getChildren()) {
			if (child instanceof Toggle childToggle) {
				this.collectExpandedPaths(childToggle, paths);
			}
		}
	}

	public void setExpandedPaths(List<String> paths) {
		if (paths == null) {
			return;
		}
		for (Toggle toggle : this.toggles) {
			this.applyExpandedPaths(toggle, paths);
		}
	}

	private void applyExpandedPaths(Toggle toggle, List<String> paths) {
		toggle.setExpanded(paths.contains(toggle.getPath()));
		for (StatusWidget child : toggle.getChildren()) {
			if (child instanceof Toggle childToggle) {
				this.applyExpandedPaths(childToggle, paths);
			}
		}
	}

	/** When true, rows do not highlight on hover (e.g. while a dropdown menu covers them). */
	public void setSuppressHover(boolean suppressHover) {
		this.suppressHover = suppressHover;
	}

	/** Hovered module's localised description, or null when none applies. */
	public String getHoveredDescription(int mouseX, int mouseY) {
		if (this.suppressHover) {
			return null;
		}
		for (int i = 0; i < this.visibleRows(); i++) {
			StatusWidget row = this.rows.get(this.scroll + i);
			if (row instanceof Toggle toggle && toggle.contains(mouseX, mouseY)) {
				return moduleDescription(toggle.getPath());
			}
		}
		return null;
	}

	private static String tr(String key, String fallback) {
		String value = I18n.t(key);
		return key.equals(value) ? fallback : value;
	}

	private static Toggle findChildToggle(Toggle root, String path) {
		for (StatusWidget child : root.getChildren()) {
			if (child instanceof Toggle toggle && toggle.getPath().equals(path)) {
				return toggle;
			}
		}
		return null;
	}

	private static String trIn(String lang, String key, String fallback) {
		String value = I18n.tIn(lang, key);
		return key.equals(value) ? fallback : value;
	}

	private Dropdown createLanguageDropdown(StatusManager status, int x, int y) {
		String[] items = new String[LanguageModule.LANG_IDS.length];
		for (int i = 0; i < items.length; i++) {
			items[i] = trIn(LanguageModule.LANG_IDS[i], "language.name", LanguageModule.LANG_FALLBACKS[i]);
		}
		return new Dropdown(
			status,
			LanguageModule.OPTION_PATH,
			tr("module.language.name", "Language"),
			items,
			LanguageModule.indexOfCurrentLanguage(),
			x,
			y,
			WIDTH,
			ROW_HEIGHT
		);
	}

	private static String moduleDescription(String enabledPath) {
		if (enabledPath == null || !enabledPath.endsWith("/enabled")) {
			return null;
		}
		String id = enabledPath.substring(0, enabledPath.length() - "/enabled".length());
		String key = "module." + id + ".description";
		String value = I18n.t(key);
		return key.equals(value) ? null : value;
	}

	/** Closes any open dropdown menus and colour picker windows owned by this window. */
	public void closeMenus() {
		for (StatusWidget root : this.rootRows) {
			if (root instanceof Dropdown dropdown) {
				dropdown.closeMenu();
			}
		}
		for (Toggle toggle : this.toggles) {
			this.closeMenusIn(toggle);
		}
	}

	private void closeMenusIn(Toggle toggle) {
		for (StatusWidget child : toggle.getChildren()) {
			if (child instanceof Dropdown dropdown) {
				dropdown.closeMenu();
			} else if (child instanceof ColorPicker picker) {
				picker.closeWindow();
			} else if (child instanceof Toggle childToggle) {
				this.closeMenusIn(childToggle);
			}
		}
	}

	/** The open dropdown menu owned by this window, if any. */
	public Dropdown findOpenDropdown() {
		for (StatusWidget root : this.rootRows) {
			if (root instanceof Dropdown dropdown && dropdown.isMenuOpen()) {
				return dropdown;
			}
		}
		for (Toggle toggle : this.toggles) {
			for (StatusWidget child : toggle.getChildren()) {
				if (child instanceof Dropdown dropdown && dropdown.isMenuOpen()) {
					return dropdown;
				}
			}
		}
		return null;
	}

	/** The open colour picker window owned by this window, if any (searches nested sub-toggles too). */
	public ColorPicker findOpenColorPicker() {
		for (Toggle toggle : this.toggles) {
			ColorPicker picker = this.findOpenColorPickerIn(toggle);
			if (picker != null) {
				return picker;
			}
		}
		return null;
	}

	/** The slider currently under the mouse, or null. */
	public Slider getHoveredSlider(int mx, int my) {
		for (int i = 0; i < this.visibleRows(); i++) {
			StatusWidget row = this.rows.get(this.scroll + i);
			if (row instanceof Slider slider
				&& mx >= slider.getX()
				&& mx < slider.getX() + slider.getWidth()
				&& my >= slider.getY()
				&& my < slider.getY() + slider.getHeight()) {
				return slider;
			}
		}
		return null;
	}

	private ColorPicker findOpenColorPickerIn(Toggle toggle) {
		for (StatusWidget child : toggle.getChildren()) {
			if (child instanceof ColorPicker picker && picker.isWindowOpen()) {
				return picker;
			}
			if (child instanceof Toggle childToggle) {
				ColorPicker nested = this.findOpenColorPickerIn(childToggle);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
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
		return this.collapsed ? HEADER_HEIGHT : this.height;
	}

	public boolean isCollapsed() {
		return this.collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public boolean isDragging() {
		return this.dragging;
	}

	public void setSize(int width, int height) {
		this.width = Math.max(MIN_WIDTH, width);
		this.height = Math.max(MIN_HEIGHT, height);
	}

	public void setScreenSize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
	}

	/** Pulls the window back so the title bar stays within the MC screen. */
	public void clampToScreen() {
		this.x = Math.max(0, Math.min(this.x, Math.max(0, this.screenWidth - this.width)));
		this.y = Math.max(0, Math.min(this.y, Math.max(0, this.screenHeight - HEADER_HEIGHT)));
	}

	private int contentHeight() {
		return this.getHeight() - HEADER_HEIGHT - BOTTOM_PADDING;
	}

	private void rebuildRows() {
		this.rows.clear();
		this.rowIndent.clear();
		for (StatusWidget root : this.rootRows) {
			this.rows.add(root);
			this.rowIndent.add(0);
		}
		for (Toggle toggle : this.toggles) {
			this.addToggleRows(toggle, 0);
		}
	}

	/** Adds a toggle and, when expanded, recursively adds its children (including nested sub-toggles). */
	private void addToggleRows(Toggle toggle, int depth) {
		int start = this.rows.size();
		this.rows.add(toggle);
		this.rowIndent.add(depth * CHILD_INDENT);
		if (toggle.isExpanded()) {
			for (StatusWidget child : toggle.getChildren()) {
				if (!(child instanceof Component component) || component.isVisible(this.status)) {
					if (child instanceof Toggle childToggle) {
						this.addToggleRows(childToggle, depth + 1);
					} else {
						this.rows.add(child);
						this.rowIndent.add((depth + 1) * CHILD_INDENT);
					}
				}
			}
		}
		toggle.setGroupRange(start, this.rows.size());
	}

	/** Draws group borders for this toggle and, recursively, any expanded nested sub-toggles. */
	private void drawExpandedBorders(Toggle toggle, GuiGraphicsExtractor graphics) {
		if (toggle.isExpanded()) {
			int blockHeight = (toggle.getGroupEnd() - toggle.getGroupStart()) * ROW_HEIGHT;
			int topY = this.y + HEADER_HEIGHT + (toggle.getGroupStart() - this.scroll) * ROW_HEIGHT;
			drawBorder(
				graphics,
				this.x,
				topY,
				this.width,
				blockHeight,
				toggle.isChecked() ? Toggle.HIGHLIGHT_COLOR : EXPANDED_OUTLINE_COLOR
			);
			for (StatusWidget child : toggle.getChildren()) {
				if (child instanceof Toggle childToggle) {
					this.drawExpandedBorders(childToggle, graphics);
				}
			}
		}
	}

	private int visibleRows() {
		int byHeight = Math.max(0, this.contentHeight() / ROW_HEIGHT);
		return Math.min(this.rows.size() - this.scroll, byHeight);
	}

	private int maxScroll() {
		int byHeight = Math.max(1, this.contentHeight() / ROW_HEIGHT);
		return Math.max(0, this.rows.size() - byHeight);
	}

	private void layoutVisibleRows() {
		for (int i = 0; i < this.visibleRows(); i++) {
			StatusWidget row = this.rows.get(this.scroll + i);
			int rowY = this.y + HEADER_HEIGHT + i * ROW_HEIGHT;
			int indent = this.rowIndent.get(this.scroll + i);
			int rowX = this.x + indent;
			int rowWidth = this.width - indent;
			if (row instanceof Toggle toggle) {
				toggle.setPosition(rowX, rowY);
				toggle.setWidth(rowWidth);
			} else if (row instanceof Slider slider) {
				slider.setPosition(rowX, rowY);
				slider.setWidth(rowWidth);
			} else if (row instanceof RangeSlider range) {
				range.setPosition(rowX, rowY);
				range.setWidth(rowWidth);
			} else if (row instanceof Dropdown dropdown) {
				dropdown.setPosition(rowX, rowY);
				dropdown.setWidth(rowWidth);
				dropdown.setAnchorRight(rowX + rowWidth);
			} else if (row instanceof ColorPicker picker) {
				picker.setPosition(rowX, rowY);
				picker.setWidth(rowWidth);
			} else if (row instanceof ActionButton action) {
				action.setPosition(rowX, rowY);
				action.setWidth(rowWidth);
			}
		}
	}

	private ResizeMode detectResize(int mx, int my) {
		boolean nearRight = mx >= this.x + this.width - EDGE_SIZE && mx < this.x + this.width;
		boolean nearBottom = !this.collapsed && my >= this.y + this.getHeight() - EDGE_SIZE && my < this.y + this.getHeight();
		if (nearRight && nearBottom) {
			return ResizeMode.BOTTOM_RIGHT;
		}
		if (nearRight) {
			return ResizeMode.RIGHT;
		}
		if (nearBottom) {
			return ResizeMode.BOTTOM;
		}
		return ResizeMode.NONE;
	}

	/** Draws a border stroke at {@link #BORDER_THICKNESS} thickness (half of the default 1px). */
	private static void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		float t = BORDER_THICKNESS;
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(1.0F, t);
		graphics.pose().translate(-x, -y);
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.pose().popMatrix();

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y + height);
		graphics.pose().scale(1.0F, t);
		graphics.pose().translate(-x, -(y + height));
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.pose().popMatrix();

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(t, 1.0F);
		graphics.pose().translate(-x, -y);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.pose().popMatrix();

		graphics.pose().pushMatrix();
		graphics.pose().translate(x + width, y);
		graphics.pose().scale(t, 1.0F);
		graphics.pose().translate(-(x + width), -y);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
		graphics.pose().popMatrix();
	}

	private void requestResizeCursor(GuiGraphicsExtractor graphics, ResizeMode mode) {
		CursorType cursor = switch (mode) {
			case RIGHT -> CursorTypes.RESIZE_EW;
			case BOTTOM -> CursorTypes.RESIZE_NS;
			case BOTTOM_RIGHT -> RESIZE_NWSE;
			case NONE -> CursorTypes.ARROW;
		};
		graphics.requestCursor(cursor);
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		int h = this.getHeight();
		graphics.fill(this.x, this.y, this.x + this.width, this.y + h, BODY_COLOR);
		graphics.fill(this.x, this.y, this.x + this.width, this.y + HEADER_HEIGHT, this.dragging ? HEADER_ACTIVE_COLOR : HEADER_COLOR);
		drawBorder(graphics, this.x, this.y, this.width, h, OUTLINE_COLOR);

		String title = this.category.getLocalizedName();
		int titleX = this.x + this.width / 2 - UiText.scaledWidth(font, title) / 2;
		int titleY = this.y + Math.max(1, (HEADER_HEIGHT - 5) / 2);
		UiText.scaledText(graphics, font, title, titleX, titleY, HEADER_TEXT_COLOR);

		ResizeMode hover = this.resizeMode != ResizeMode.NONE ? this.resizeMode : this.detectResize(mouseX, mouseY);
		if (hover != ResizeMode.NONE) {
			this.requestResizeCursor(graphics, hover);
		}

		if (this.collapsed) {
			return;
		}

		this.scroll = Math.min(this.scroll, this.maxScroll());
		this.rebuildRows();

		graphics.enableScissor(this.x, this.y + HEADER_HEIGHT, this.x + this.width, this.y + h - BOTTOM_PADDING);

		this.layoutVisibleRows();
		int effX = this.suppressHover ? -1 : mouseX;
		int effY = this.suppressHover ? -1 : mouseY;
		for (int i = 0; i < this.visibleRows(); i++) {
			StatusWidget row = this.rows.get(this.scroll + i);
			if (row instanceof Toggle toggle) {
				toggle.render(graphics, font, effX, effY);
			} else if (row instanceof Slider slider) {
				slider.render(graphics, font, effX, effY);
			} else if (row instanceof RangeSlider range) {
				range.render(graphics, font, effX, effY);
			} else if (row instanceof Dropdown dropdown) {
				dropdown.renderRow(graphics, font, effX, effY);
			} else if (row instanceof ColorPicker picker) {
				picker.renderRow(graphics, font, effX, effY);
			} else if (row instanceof ActionButton action) {
				action.render(graphics, font, effX, effY);
			}
		}

		for (Toggle toggle : this.toggles) {
			this.drawExpandedBorders(toggle, graphics);
		}

		graphics.disableScissor();

		int contentH = this.contentHeight();
		if (this.rows.size() * ROW_HEIGHT > contentH) {
			int thumbHeight = Math.max(8, contentH * contentH / (this.rows.size() * ROW_HEIGHT));
			int thumbY = this.y + HEADER_HEIGHT + (contentH - thumbHeight) * this.scroll / Math.max(1, this.maxScroll());
			graphics.fill(this.x + this.width - 2, thumbY, this.x + this.width, thumbY + thumbHeight, SCROLLBAR_COLOR);
		}
	}

	public boolean mouseClicked(MouseButtonEvent event) {
		if (event.button() != 0 && event.button() != 1) {
			return false;
		}
		int mx = (int) event.x();
		int my = (int) event.y();
		int h = this.getHeight();
		if (mx < this.x || mx >= this.x + this.width || my < this.y || my >= this.y + h) {
			return false;
		}

		ResizeMode mode = this.detectResize(mx, my);
		if (mode != ResizeMode.NONE) {
			this.resizeMode = mode;
			this.resizeStartX = mx;
			this.resizeStartY = my;
			this.resizeStartWidth = this.width;
			this.resizeStartHeight = this.height;
			return true;
		}

		if (my >= this.y && my < this.y + HEADER_HEIGHT) {
			this.headerPressed = true;
			this.headerMoved = false;
			this.pressX = mx;
			this.pressY = my;
			this.dragging = true;
			this.dragOffsetX = mx - this.x;
			this.dragOffsetY = my - this.y;
			return true;
		}

		if (this.collapsed) {
			return true;
		}

		for (int i = 0; i < this.visibleRows(); i++) {
			StatusWidget row = this.rows.get(this.scroll + i);
			if (row instanceof Toggle toggle && toggle.mouseClicked(event)) {
				return true;
			}
			if (row instanceof Slider slider && slider.mouseClicked(event)) {
				return true;
			}
			if (row instanceof RangeSlider range && range.mouseClicked(event)) {
				return true;
			}
			if (row instanceof Dropdown dropdown && dropdown.mouseClickedRow(event)) {
				return true;
			}
			if (row instanceof ColorPicker picker && picker.mouseClickedRow(event)) {
				return true;
			}
			if (row instanceof ActionButton action && action.mouseClicked(event)) {
				return true;
			}
		}
		return true;
	}

	public boolean mouseDragged(MouseButtonEvent event) {
		int mx = (int) event.x();
		int my = (int) event.y();
		if (this.resizeMode != ResizeMode.NONE) {
			int deltaW = mx - this.resizeStartX;
			int deltaH = my - this.resizeStartY;
			if (this.resizeMode == ResizeMode.RIGHT || this.resizeMode == ResizeMode.BOTTOM_RIGHT) {
				this.width = Math.max(MIN_WIDTH, this.resizeStartWidth + deltaW);
			}
			if (this.resizeMode == ResizeMode.BOTTOM || this.resizeMode == ResizeMode.BOTTOM_RIGHT) {
				this.height = Math.max(MIN_HEIGHT, this.resizeStartHeight + deltaH);
			}
			this.clampToScreen();
			return true;
		}
		if (this.dragging) {
			this.x = mx - this.dragOffsetX;
			this.y = my - this.dragOffsetY;
			this.clampToScreen();
			if (this.headerPressed && Math.abs(mx - this.pressX) + Math.abs(my - this.pressY) > 3) {
				this.headerMoved = true;
			}
			return true;
		}
		for (int i = 0; i < this.visibleRows(); i++) {
			StatusWidget row = this.rows.get(this.scroll + i);
			if (row instanceof Slider slider && slider.mouseDragged(mx)) {
				return true;
			}
			if (row instanceof RangeSlider range && range.mouseDragged(mx)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseReleased() {
		boolean any = this.dragging || this.resizeMode != ResizeMode.NONE;
		if (this.headerPressed) {
			if (!this.headerMoved) {
				this.collapsed = !this.collapsed;
				this.closeMenus();
			}
			this.headerPressed = false;
			this.headerMoved = false;
		}
		this.dragging = false;
		this.resizeMode = ResizeMode.NONE;
		for (int i = 0; i < this.visibleRows(); i++) {
			StatusWidget row = this.rows.get(this.scroll + i);
			if (row instanceof Slider slider && slider.mouseReleased()) {
				any = true;
			}
			if (row instanceof RangeSlider range && range.mouseReleased()) {
				any = true;
			}
		}
		return any;
	}

	public boolean mouseScrolled(int mx, int my, double scrollDelta) {
		if (mx < this.x || mx >= this.x + this.width || my < this.y || my >= this.y + this.getHeight()) {
			return false;
		}
		this.closeMenus();
		if (this.collapsed || this.rows.size() * ROW_HEIGHT <= this.contentHeight()) {
			return true;
		}
		if (scrollDelta > 0.0) {
			this.scroll = Math.max(0, this.scroll - 1);
		} else if (scrollDelta < 0.0) {
			this.scroll = Math.min(this.maxScroll(), this.scroll + 1);
		}
		return true;
	}
}
