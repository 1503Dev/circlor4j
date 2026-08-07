package dev1503.circlor4j.ui.clickgui;

import dev1503.circlor4j.client.config.ModStorage;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.client.module.modules.ClickGuiModule;
import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.StatusManager;
import dev1503.circlor4j.ui.component.CategoryWindow;
import dev1503.circlor4j.ui.component.ColorPicker;
import dev1503.circlor4j.ui.component.Dropdown;
import dev1503.circlor4j.ui.component.Slider;
import dev1503.circlor4j.ui.component.UiText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
	private final List<CategoryWindow> windows = new ArrayList<>();
	private Dropdown activeMenu;
	private ColorPicker activePicker;
	private String renderedLang;
	private int lastMouseX;
	private int lastMouseY;

	public ClickGuiScreen() {
		super(Component.literal("Circlor4J ClickGUI"));
	}

	@Override
	protected void init() {
		super.init();
		this.buildWindows(ModStorage.loadLayout());
	}

	/** Rebuilds all category windows (re-resolving localisation) when the language changes. */
	private void rebuildWindows() {
		Map<String, ModStorage.WindowLayout> current = new HashMap<>();
		for (CategoryWindow window : this.windows) {
			current.put(
				window.getCategory().name(),
				new ModStorage.WindowLayout(
					window.getX(),
					window.getY(),
					window.getWidth(),
					window.getHeight(),
					window.isCollapsed(),
					window.getExpandedPaths()
				)
			);
		}
		StatusManager.getInstance().clearWidgets();
		this.activeMenu = null;
		this.activePicker = null;
		this.buildWindows(current);
	}

	private void buildWindows(Map<String, ModStorage.WindowLayout> layout) {
		this.windows.clear();
		ModuleCategory[] categories = ModuleCategory.values();
		int startX = 10;
		int startY = 10;
		for (int i = 0; i < categories.length; i++) {
			ModuleCategory category = categories[i];
			ModStorage.WindowLayout saved = layout != null ? layout.get(category.name()) : null;
			int windowX = saved != null ? saved.x() : startX + i * (CategoryWindow.WIDTH + 6);
			int windowY = saved != null ? saved.y() : startY;
			CategoryWindow window = new CategoryWindow(StatusManager.getInstance(), category, windowX, windowY);
			if (saved != null) {
				window.setSize(saved.width(), saved.height());
				window.setCollapsed(saved.collapsed());
				window.setExpandedPaths(saved.expanded());
			}
			window.setScreenSize(this.width, this.height);
			window.clampToScreen();
			this.windows.add(window);
		}
	}

	@Override
	protected void repositionElements() {
		// Keep window positions when the window is resized.
	}

	@Override
	public void removed() {
		super.removed();
		ClickGuiModule.setClosing(true);
		try {
			StatusManager.getInstance().setValue(ClickGuiModule.ID + "/enabled", 0.0);
		} finally {
			ClickGuiModule.setClosing(false);
		}
		StatusManager.getInstance().clearWidgets();
		this.saveLayout();
		StatusManager.getInstance().save();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, ClickGuiModule.getBackgroundColor());
	}

	private void saveLayout() {
		Map<String, ModStorage.WindowLayout> layout = new HashMap<>();
		for (CategoryWindow window : this.windows) {
			layout.put(
				window.getCategory().name(),
				new ModStorage.WindowLayout(
					window.getX(),
					window.getY(),
					window.getWidth(),
					window.getHeight(),
					window.isCollapsed(),
					window.getExpandedPaths()
				)
			);
		}
		ModStorage.saveLayout(layout);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;
		if (this.renderedLang != null && !this.renderedLang.equals(I18n.lang)) {
			this.rebuildWindows();
		}
		this.renderedLang = I18n.lang;

		this.activeMenu = this.findOpenDropdown();
		this.activePicker = this.findOpenColorPicker();
		boolean menuCovers = this.activeMenu != null && this.activeMenu.containsMenu(this.font, mouseX, mouseY);
		boolean pickerCovers = this.activePicker != null && this.activePicker.containsWindow(mouseX, mouseY);
		boolean popupCovers = menuCovers || pickerCovers;
		for (CategoryWindow window : this.windows) {
			window.setScreenSize(this.width, this.height);
			window.clampToScreen();
			window.setSuppressHover(popupCovers);
			window.render(graphics, this.font, mouseX, mouseY);
		}
		if (this.activeMenu != null) {
			this.activeMenu.renderMenu(graphics, this.font, mouseX, mouseY);
		}
		if (this.activePicker != null) {
			this.activePicker.renderWindow(graphics, this.font, mouseX, mouseY);
		}

		if (!popupCovers) {
			for (int i = this.windows.size() - 1; i >= 0; i--) {
				String description = this.windows.get(i).getHoveredDescription(mouseX, mouseY);
				if (description != null) {
					UiText.scaledText(graphics, this.font, description, 2, this.height - 10, 0xFFAAAAAA);
					break;
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int mx = (int) event.x();
		int my = (int) event.y();
		if (this.activePicker != null) {			if (this.activePicker.mouseClickedWindow(event, this.font)) {
				this.activePicker = this.activePicker.isWindowOpen() ? this.activePicker : null;
				return true;
			}
			if (this.activePicker.isRow(mx, my)) {
				this.activePicker.closeWindow();
				this.activePicker = null;
				return true;
			}
			this.activePicker.closeWindow();
			this.activePicker = null;
		}
		if (this.activeMenu != null) {
			if (this.activeMenu.mouseClickedMenu(event, this.font)) {
				this.activeMenu = null;
				return true;
			}
			if (this.activeMenu.isRow(mx, my)) {
				this.activeMenu.closeMenu();
				this.activeMenu = null;
				return true;
			}
			this.activeMenu.closeMenu();
			this.activeMenu = null;
		}
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			if (this.windows.get(i).mouseClicked(event)) {
				this.activeMenu = this.findOpenDropdown();
				this.activePicker = this.findOpenColorPicker();
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private Dropdown findOpenDropdown() {
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			Dropdown dropdown = this.windows.get(i).findOpenDropdown();
			if (dropdown != null) {
				return dropdown;
			}
		}
		return null;
	}

	private ColorPicker findOpenColorPicker() {
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			ColorPicker picker = this.windows.get(i).findOpenColorPicker();
			if (picker != null) {
				return picker;
			}
		}
		return null;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.activePicker != null && this.activePicker.mouseDraggedWindow((int) event.x(), (int) event.y())) {
			return true;
		}
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			if (this.windows.get(i).mouseDragged(event)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.activePicker != null) {
			this.activePicker.mouseReleasedWindow();
		}
		for (CategoryWindow window : this.windows) {
			window.mouseReleased();
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			if (this.windows.get(i).mouseScrolled((int) x, (int) y, scrollY)) {
				return true;
			}
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.isLeft() || event.isRight()) {
			for (int i = this.windows.size() - 1; i >= 0; i--) {
				Slider slider = this.windows.get(i).getHoveredSlider(this.lastMouseX, this.lastMouseY);
				if (slider != null) {
					slider.stepBy(event.isRight());
					return true;
				}
			}
		}
		return super.keyPressed(event);
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
