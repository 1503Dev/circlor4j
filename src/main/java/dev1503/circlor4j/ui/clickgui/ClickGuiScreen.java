package dev1503.circlor4j.ui.clickgui;

import dev1503.circlor4j.ModStatic;
import dev1503.circlor4j.client.config.ModStorage;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.client.module.modules.ClickGuiModule;
import dev1503.circlor4j.client.update.UpdateChecker;
import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.StatusManager;
import dev1503.circlor4j.ui.component.BlockList;
import dev1503.circlor4j.ui.component.Button;
import dev1503.circlor4j.ui.component.CategoryWindow;
import dev1503.circlor4j.ui.component.ColorList;
import dev1503.circlor4j.ui.component.ColorPicker;
import dev1503.circlor4j.ui.component.Dropdown;
import dev1503.circlor4j.ui.component.KeyBindPanel;
import dev1503.circlor4j.ui.component.Slider;
import dev1503.circlor4j.ui.component.TextButton;
import dev1503.circlor4j.ui.component.UiText;
import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
	private static final int TAB_TOP = 0;
	private static final int TAB_HEIGHT = 14;
	private static final int TAB_GAP = 0;
	private static final int TAB_PAD_X = 12;
	private static final int TAB_LINE_HEIGHT = 1;
	private static final int LIST_TOP = TAB_TOP + TAB_HEIGHT + 8;
	private static final ModuleCategory[] DEFAULT_CATEGORY_ORDER = {
		ModuleCategory.MOVEMENT,
		ModuleCategory.PLAYER,
		ModuleCategory.RENDER,
		ModuleCategory.COMBAT,
		ModuleCategory.MISC,
		ModuleCategory.CIRCLOR
	};

	private static final int TAB_HIGHLIGHT = 0xFF2697F3;
	private static final int TAB_TEXT_IDLE = 0xFFAAAAAA;
	private static final int TAB_TEXT_HOVER = 0xFFDDDDDD;

	private final List<CategoryWindow> windows = new ArrayList<>();
	private final KeyBindPanel keyBindPanel = new KeyBindPanel(this);
	private int activeTab;
	private Dropdown activeMenu;
	private ColorPicker activePicker;
	private BlockList activeBlockList;
	private ColorList activeColorList;
	private String renderedLang;
	private int lastMouseX;
	private int lastMouseY;
	private Button updateButton;

	public ClickGuiScreen() {
		super(Component.literal("Circlor4J ClickGUI"));
	}

	@Override
	protected void init() {
		super.init();
		this.buildWindows(ModStorage.loadLayout());
		this.updateButton = new TextButton(
			I18n.t("clickgui.update_available", "New version is ready"),
			0,
			0,
			10,
			10,
			this::openUpdateUrl
		);
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
					window.getContentHeight(),
					window.isCollapsed(),
					window.getExpandedPaths()
				)
			);
		}
		StatusManager.getInstance().clearWidgets();
		this.activeMenu = null;
		this.activePicker = null;
		this.activeBlockList = null;
		this.activeColorList = null;
		this.buildWindows(current);
	}

	private void buildWindows(Map<String, ModStorage.WindowLayout> layout) {
		this.windows.clear();
		ModuleCategory[] categories = DEFAULT_CATEGORY_ORDER;
		int startX = 10;
		int startY = TAB_TOP + TAB_HEIGHT + 6;
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
			window.setMinY(TAB_TOP + TAB_HEIGHT);
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

	private String tabLabel(int tab) {
		if (tab == 1) {
			String value = I18n.t("ui.keybinds.name");
			return value.equals("ui.keybinds.name") ? "Keybinds" : value;
		}
		String value = I18n.t("clickgui.tab.modules");
		return value.equals("clickgui.tab.modules") ? "Modules" : value;
	}

	private int[] tabRects() {
		int label0W = UiText.scaledWidth(this.font, this.tabLabel(0));
		int label1W = UiText.scaledWidth(this.font, this.tabLabel(1));
		int w = Math.max(label0W, label1W) + TAB_PAD_X * 2;
		int total = w * 2 + TAB_GAP;
		int startX = (this.width - total) / 2;
		return new int[] {startX, startX + w + TAB_GAP, w, w};
	}

	private void renderTabbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int[] r = this.tabRects();
		this.renderTab(graphics, this.tabLabel(0), r[0], r[2], mouseX, mouseY, this.activeTab == 0);
		this.renderTab(graphics, this.tabLabel(1), r[1], r[3], mouseX, mouseY, this.activeTab == 1);
	}

	private void renderTab(GuiGraphicsExtractor graphics, String label, int x, int w, int mouseX, int mouseY, boolean active) {
		boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= TAB_TOP && mouseY < TAB_TOP + TAB_HEIGHT;
		int textColor = active ? TAB_HIGHLIGHT : (hovered ? TAB_TEXT_HOVER : TAB_TEXT_IDLE);
		int textX = x + w / 2 - UiText.scaledWidth(this.font, label) / 2;
		UiText.scaledText(graphics, this.font, label, textX, UiText.centerY(TAB_TOP, TAB_HEIGHT), textColor);
		if (active) {
			graphics.fill(x, TAB_TOP + TAB_HEIGHT - TAB_LINE_HEIGHT, x + w, TAB_TOP + TAB_HEIGHT, TAB_HIGHLIGHT);
		}
	}

	private void setTab(int tab) {
		if (this.activeTab == tab) {
			return;
		}
		if (tab == 0) {
			for (CategoryWindow window : this.windows) {
				window.closeMenus();
			}
			this.activeMenu = null;
			this.activePicker = null;
			this.activeBlockList = null;
			this.activeColorList = null;
		}
		this.activeTab = tab;
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
					window.getContentHeight(),
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

		if (this.activeTab == 1) {
			this.keyBindPanel.render(graphics, this.font, this.width, this.height, LIST_TOP, mouseX, mouseY);
		} else {
			this.renderModulesTab(graphics, mouseX, mouseY);
		}

		this.renderTabbar(graphics, mouseX, mouseY);

		String version = ModStatic.VERSION;
		String versionText = "Circlor4J v" + version;
		int versionWidth = UiText.scaledWidth(this.font, versionText);
		UiText.scaledText(graphics, this.font, versionText, this.width / 2 - versionWidth / 2, this.height - 16, 0xFFAAAAAA);

		String updateText = I18n.t("clickgui.update_available", "New version is ready");
		int textW = this.font.width(updateText);
		int textH = this.font.lineHeight;
		int padding = 2;
		int boxW = textW + padding * 2;
		int boxH = textH + padding * 2;
		int boxX = this.width - boxW - 4;
		int boxY = this.height - boxH - 6;
		this.updateButton.setPosition(boxX, boxY);
		this.updateButton.setSize(boxW, boxH);
		if (UpdateChecker.hasUpdate()) {
			this.updateButton.render(graphics, this.font, mouseX, mouseY);
		}
	}

	private void renderModulesTab(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		this.activeMenu = this.findOpenDropdown();
		this.activePicker = this.findOpenColorPicker();
		this.activeBlockList = this.findOpenBlockList();
		this.activeColorList = this.findOpenColorList();
		boolean menuCovers = this.activeMenu != null && this.activeMenu.containsMenu(this.font, mouseX, mouseY);
		boolean pickerCovers = this.activePicker != null && this.activePicker.containsWindow(mouseX, mouseY);
		boolean blockListCovers = this.activeBlockList != null && this.activeBlockList.containsWindow(mouseX, mouseY);
		boolean colorListCovers = this.activeColorList != null && this.activeColorList.containsWindow(mouseX, mouseY);
		boolean popupCovers = menuCovers || pickerCovers || blockListCovers || colorListCovers;
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
		if (this.activeBlockList != null) {
			this.activeBlockList.renderWindow(graphics, this.font, mouseX, mouseY);
		}
		if (this.activeColorList != null) {
			this.activeColorList.renderWindow(graphics, this.font, mouseX, mouseY);
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
		if (this.updateButton != null && UpdateChecker.hasUpdate() && this.updateButton.mouseClicked(event)) {
			return true;
		}
		if (event.button() == 0 && my >= TAB_TOP && my < TAB_TOP + TAB_HEIGHT) {
			int[] r = this.tabRects();
			if (mx >= r[0] && mx < r[0] + r[2]) {
				this.setTab(0);
				return true;
			}
			if (mx >= r[1] && mx < r[1] + r[3]) {
				this.setTab(1);
				return true;
			}
		}
		if (this.activeTab == 1) {
			return this.keyBindPanel.mouseClicked(event, this.width, this.height, LIST_TOP);
		}
		if (this.activePicker != null) {
			if (this.activePicker.mouseClickedWindow(event, this.font)) {
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
		if (this.activeBlockList != null) {
			if (this.activeBlockList.isContextMenuOpen()) {
				this.activeBlockList.contextMenuClicked(event);
				return true;
			}
			if (this.activeBlockList.mouseClickedWindow(event, this.font)) {
				this.activeBlockList = this.activeBlockList.isWindowOpen() ? this.activeBlockList : null;
				return true;
			}
			if (this.activeBlockList.isRow(mx, my)) {
				this.activeBlockList.closeWindow();
				this.activeBlockList = null;
				return true;
			}
			this.activeBlockList.closeWindow();
			this.activeBlockList = null;
		}
		if (this.activeColorList != null) {
			if (this.activeColorList.isContextMenuOpen()) {
				this.activeColorList.contextMenuClicked(event);
				return true;
			}
			if (this.activeColorList.mouseClickedWindow(event, this.font)) {
				this.activeColorList = this.activeColorList.isWindowOpen() ? this.activeColorList : null;
				return true;
			}
			if (this.activeColorList.isRow(mx, my)) {
				this.activeColorList.closeWindow();
				this.activeColorList = null;
				return true;
			}
			this.activeColorList.closeWindow();
			this.activeColorList = null;
		}
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			if (this.windows.get(i).mouseClicked(event)) {
				this.activeMenu = this.findOpenDropdown();
				this.activePicker = this.findOpenColorPicker();
				this.activeBlockList = this.findOpenBlockList();
				this.activeColorList = this.findOpenColorList();
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

	private BlockList findOpenBlockList() {
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			BlockList blockList = this.windows.get(i).findOpenBlockList();
			if (blockList != null) {
				return blockList;
			}
		}
		return null;
	}

	private ColorList findOpenColorList() {
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			ColorList colorList = this.windows.get(i).findOpenColorList();
			if (colorList != null) {
				return colorList;
			}
		}
		return null;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.activeTab == 1) {
			return false;
		}
		if (this.activePicker != null && this.activePicker.mouseDraggedWindow((int) event.x(), (int) event.y())) {
			return true;
		}
		if (this.activeColorList != null && this.activeColorList.mouseDraggedWindow((int) event.x(), (int) event.y())) {
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
		if (this.activeTab == 1) {
			return false;
		}
		if (this.activePicker != null) {
			this.activePicker.mouseReleasedWindow();
		}
		if (this.activeColorList != null) {
			this.activeColorList.mouseReleasedWindow();
		}
		for (CategoryWindow window : this.windows) {
			window.mouseReleased();
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		int mx = (int) x;
		int my = (int) y;
		if (this.activeTab == 1) {
			this.keyBindPanel.mouseScrolled(scrollY, this.width, this.height, LIST_TOP);
			return true;
		}
		if (this.activeBlockList != null && this.activeBlockList.containsWindow(mx, my)) {
			this.activeBlockList.mouseScrolled(mx, my, scrollY);
			return true;
		}
		if (this.activeColorList != null && this.activeColorList.containsWindow(mx, my)) {
			this.activeColorList.mouseScrolled(mx, my, scrollY);
			return true;
		}
		for (int i = this.windows.size() - 1; i >= 0; i--) {
			if (this.windows.get(i).mouseScrolled(mx, my, scrollY)) {
				return true;
			}
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.activeTab == 1) {
			return this.keyBindPanel.keyPressed(event) || super.keyPressed(event);
		}
		if (this.activeBlockList != null && this.activeBlockList.isInputActive()) {
			return this.activeBlockList.keyPressed(event);
		}
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
	public boolean charTyped(CharacterEvent event) {
		if (this.activeBlockList != null && this.activeBlockList.isInputActive()) {
			return this.activeBlockList.charTyped(event);
		}
		return super.charTyped(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	private void openUpdateUrl() {
		System.out.println("[Circlor4J] openUpdateUrl() called");
		if (!UpdateChecker.hasUpdate()) {
			System.out.println("[Circlor4J] No update, returning");
			return;
		}
		String url = UpdateChecker.getUpdateUrl();
		System.out.println("[Circlor4J] Update URL: " + url);
		if (url == null || url.isEmpty()) {
			return;
		}
		try {
			URI uri = URI.create(url);
			if (uri.getScheme() == null) {
				uri = URI.create("https://" + url);
			}
			final String finalUrl = uri.toString();
			System.out.println("[Circlor4J] Opening: " + finalUrl);
			new Thread(() -> {
				String os = System.getProperty("os.name", "").toLowerCase();
				System.out.println("[Circlor4J] OS: " + os);
				try {
					if (os.contains("win")) {
						System.out.println("[Circlor4J] Running rundll32");
						Process p = Runtime.getRuntime().exec(new String[] {"rundll32", "url.dll,FileProtocolHandler", finalUrl});
						System.out.println("[Circlor4J] rundll32 exit: " + p.waitFor());
					} else if (os.contains("mac")) {
						Runtime.getRuntime().exec(new String[] {"open", finalUrl});
					} else if (os.contains("linux")) {
						Runtime.getRuntime().exec(new String[] {"xdg-open", finalUrl});
					} else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
						Desktop.getDesktop().browse(URI.create(finalUrl));
					}
				} catch (Exception e) {
					System.out.println("[Circlor4J] exec failed: " + e);
					try {
						if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
							System.out.println("[Circlor4J] Falling back to Desktop.browse");
							Desktop.getDesktop().browse(URI.create(finalUrl));
						} else {
							System.out.println("[Circlor4J] Desktop.browse not supported");
						}
					} catch (Exception e2) {
						System.out.println("[Circlor4J] Desktop.browse failed: " + e2);
					}
				}
			}, "circlor4j-open-url").start();
		} catch (Exception e) {
		}
	}
}
