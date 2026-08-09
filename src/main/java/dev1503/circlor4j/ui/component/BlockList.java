package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A collapsible block-list option. Collapsed it reads like an {@link ActionButton}: the localised
 * label on the left and the number of enabled blocks on the right. Clicking the row opens a
 * standalone popup (coordinated by the {@link dev1503.circlor4j.ui.clickgui.ClickGuiScreen}, like a
 * colour picker window) that lists the added blocks with a 3D icon, name and id; left-click toggles
 * a block, right-click removes it, and a text field at the top adds new ones by id.
 *
 * <p>Each block's enabled state is persisted as a double under "{path}/{blockId}" (1.0 = enabled).
 * The component's effective value is the array of block ids that are currently enabled.
 */
public class BlockList extends Component {
	private static final int WIN_W = 140;
	private static final int HEADER_H = 14;
	private static final int ITEM_H = 18;
	private static final int ICON = 16;
	private static final int MAX_VISIBLE = 8;
	private static final int SCROLLBAR_W = 2;

	private static final int ROW_HOVER_COLOR = 0x40FFFFFF;
	private static final int ROW_TEXT_COLOR = 0xFFAAAAAA;
	private static final int COUNT_COLOR = 0xFFFFFFFF;

	private static final int WIN_BG_COLOR = 0xE0101010;
	private static final int WIN_BORDER_COLOR = 0xFF3A3A3A;
	private static final int LABEL_COLOR = 0xFFAAAAAA;
	private static final int FIELD_BG_COLOR = 0xFF222222;
	private static final int FIELD_ACTIVE_COLOR = 0xFF3A6EA5;
	private static final int ADD_COLOR = 0xFF2697F3;
	private static final int ADD_HOVER_COLOR = 0xFF4EA3FF;
	private static final int ITEM_BG_COLOR = 0x20FFFFFF;
	private static final int ITEM_HOVER_COLOR = 0x40FFFFFF;
	private static final int ITEM_ENABLED_COLOR = 0x332697F3;
	private static final int ITEM_NAME_COLOR = 0xFFFFFFFF;
	private static final int ITEM_ID_COLOR = 0xFF777777;
	private static final int SCROLLBAR_COLOR = 0xFF9A9A9A;
	private static final int CURSOR_COLOR = 0xFF2697F3;

	private static final int PAD = 2;

	private final StatusManager status;
	private final String label;
	private int x;
	private int y;
	private int width;
	private int height = CategoryWindow.ROW_HEIGHT;

	private boolean windowOpen;
	private boolean inputActive;
	private String inputBuffer = "";
	private int scroll;
	private int cursorBlink;

	private final ContextMenu contextMenu = new ContextMenu();

	private final List<String> members = new ArrayList<>();

	public BlockList(StatusManager status, String path, String label, String[] defaultBlockIds, int x, int y, int width, int height) {
		super(path);
		this.status = status;
		this.label = label;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.rebuildMembers();
	}

	private String blockPath(String blockId) {
		return this.getPath() + "/" + blockId;
	}

	private void rebuildMembers() {
		this.members.clear();
		String prefix = this.getPath() + "/";
		for (Map.Entry<String, Double> entry : this.status.entriesWithPrefix(prefix).entrySet()) {
			String rel = entry.getKey().substring(prefix.length());
			if (!rel.isEmpty() && !rel.contains("/")) {
				this.members.add(rel);
			}
		}
	}

	@Override
	public void syncStatus(double value) {
		this.rebuildMembers();
	}

	public int enabledCount() {
		int count = 0;
		for (String id : this.members) {
			if (this.status.getBoolean(this.blockPath(id), false)) {
				count++;
			}
		}
		return count;
	}

	public boolean isWindowOpen() {
		return this.windowOpen;
	}

	public void closeWindow() {
		this.windowOpen = false;
		this.inputActive = false;
		this.inputBuffer = "";
		this.contextMenu.close();
	}

	public boolean isInputActive() {
		return this.inputActive;
	}

	public boolean isContextMenuOpen() {
		return this.contextMenu.isOpen();
	}

	public boolean contextMenuClicked(MouseButtonEvent event) {
		return this.contextMenu.mouseClicked(event);
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	private boolean containsRow(int mx, int my) {
		return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
	}

	public boolean isRow(int mx, int my) {
		return this.containsRow(mx, my);
	}

	private int winX() {
		int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int right = this.x + this.width + 2;
		return Math.max(0, Math.min(right, screenW - WIN_W));
	}

	private int winY() {
		int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		return Math.max(0, Math.min(this.y, screenH - winH()));
	}

	private int winH() {
		return PAD + HEADER_H + PAD + MAX_VISIBLE * ITEM_H + PAD;
	}

	public boolean containsWindow(int mx, int my) {
		int wx = this.winX();
		int wy = this.winY();
		return mx >= wx && mx < wx + WIN_W && my >= wy && my < wy + winH();
	}

	private boolean inHeaderField(int mx, int my) {
		int wx = this.winX();
		int wy = this.winY();
		int fieldW = WIN_W - PAD - 14;
		return mx >= wx + PAD && mx < wx + PAD + fieldW && my >= wy + PAD && my < wy + PAD + HEADER_H;
	}

	private boolean inAddButton(int mx, int my) {
		int wx = this.winX();
		int wy = this.winY();
		int bx = wx + WIN_W - PAD - 12;
		return mx >= bx && mx < bx + 12 && my >= wy + PAD + 1 && my < wy + PAD + HEADER_H - 1;
	}

	private int listTop() {
		return this.winY() + PAD + HEADER_H + PAD;
	}

	private int visibleCapacity() {
		return Math.min(MAX_VISIBLE, this.members.size());
	}

	private int maxScroll() {
		return Math.max(0, this.members.size() - MAX_VISIBLE);
	}

	private void clampScroll() {
		this.scroll = Math.max(0, Math.min(this.scroll, this.maxScroll()));
	}

	/** Row click (called by the owning CategoryWindow): toggles the popup window. */
	public boolean mouseClickedRow(MouseButtonEvent event) {
		if (event.button() != 0 || !this.containsRow((int) event.x(), (int) event.y())) {
			return false;
		}
		this.windowOpen = !this.windowOpen;
		if (this.windowOpen) {
			this.inputActive = false;
			this.inputBuffer = "";
			this.clampScroll();
		}
		return true;
	}

	/** Window click (called by the ClickGuiScreen, top layer). */
	public boolean mouseClickedWindow(MouseButtonEvent event, Font font) {
		if (!this.windowOpen) {
			return false;
		}
		int mx = (int) event.x();
		int my = (int) event.y();
		if (!this.containsWindow(mx, my)) {
			return false;
		}
		if (this.inHeaderField(mx, my)) {
			this.inputActive = true;
			return true;
		}
		if (this.inAddButton(mx, my)) {
			this.commitAdd();
			return true;
		}
		this.inputActive = false;
		boolean menuWasOpen = this.contextMenu.isOpen();
		if (this.contextMenu.mouseClicked(event)) {
			return true;
		}
		if (menuWasOpen) {
			return true;
		}
		int index = this.itemAt(mx, my);
		if (index >= 0 && index < this.members.size()) {
			if (event.button() == 0) {
				String id = this.members.get(index);
				boolean enabled = this.status.getBoolean(this.blockPath(id), false);
				this.status.setValue(this, this.blockPath(id), enabled ? 0.0 : 1.0);
				this.rebuildMembers();
			} else if (event.button() == 1) {
				this.openContextMenu(index, (int) event.x(), (int) event.y());
			}
			return true;
		}
		return true;
	}

	private void openContextMenu(int index, int mx, int my) {
		this.contextMenu.close();
		String id = this.members.get(index);
		this.contextMenu.add("复制ID", () -> this.copyIdToClipboard(id));
		this.contextMenu.add("删除", () -> {
			this.status.remove(this.blockPath(id));
			this.rebuildMembers();
			this.clampScroll();
		});
		this.contextMenu.open(mx, my);
	}

	private void copyIdToClipboard(String id) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.keyboardHandler != null) {
			mc.keyboardHandler.setClipboard(id);
		}
	}

	private int itemAt(int mx, int my) {
		int listTop = this.listTop();
		if (my < listTop) {
			return -1;
		}
		int rel = (my - listTop) / ITEM_H;
		if (rel >= visibleCapacity()) {
			return -1;
		}
		int index = this.scroll + rel;
		return index < this.members.size() ? index : -1;
	}

	public void mouseScrolled(int mx, int my, double delta) {
		if (!this.windowOpen || !this.containsWindow(mx, my)) {
			return;
		}
		if (delta > 0.0) {
			this.scroll = Math.max(0, this.scroll - 1);
		} else if (delta < 0.0) {
			this.scroll = Math.min(this.maxScroll(), this.scroll + 1);
		}
	}

	private void commitAdd() {
		String raw = this.inputBuffer.trim().toLowerCase(Locale.ROOT);
		this.inputBuffer = "";
		if (raw.isEmpty()) {
			return;
		}
		Identifier id = Identifier.tryParse(raw);
		if (id == null) {
			id = Identifier.tryBuild("minecraft", raw);
		}
		if (id == null) {
			return;
		}
		Block block = BuiltInRegistries.BLOCK.getValue(id);
		if (block == null || block == Blocks.AIR) {
			return;
		}
		String key = id.toString();
		boolean exists = this.members.contains(key);
		this.status.setValueOnly(this.blockPath(key), 1.0);
		if (!exists) {
			this.rebuildMembers();
			this.scroll = this.maxScroll();
		}
	}

	public boolean keyPressed(KeyEvent event) {
		if (!this.inputActive) {
			return false;
		}
		if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_BACKSPACE) {
			if (!this.inputBuffer.isEmpty()) {
				this.inputBuffer = this.inputBuffer.substring(0, this.inputBuffer.length() - 1);
			}
			return true;
		}
		if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_RETURN
			|| event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER) {
			this.commitAdd();
			return true;
		}
		if (event.isEscape()) {
			this.inputActive = false;
			return true;
		}
		return false;
	}

	public boolean charTyped(CharacterEvent event) {
		if (!this.inputActive || !event.isAllowedChatCharacter()) {
			return false;
		}
		this.inputBuffer = this.inputBuffer + event.codepointAsString();
		return true;
	}

	public void renderRow(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		if (this.containsRow(mouseX, mouseY)) {
			graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, ROW_HOVER_COLOR);
		}
		String countText = enabledCount() + "";
		int countW = UiText.scaledWidth(font, countText);
		int countX = this.x + this.width - PAD - countW;
		UiText.scaledText(graphics, font, countText, countX, UiText.centerY(this.y, this.height), COUNT_COLOR);
		String labelText = UiText.fit(font, this.label, Math.max(0, countX - (this.x + PAD) - 2));
		UiText.scaledText(graphics, font, labelText, this.x + PAD, UiText.centerY(this.y, this.height), ROW_TEXT_COLOR);
	}

	public void renderWindow(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		if (!this.windowOpen) {
			return;
		}
		int wx = this.winX();
		int wy = this.winY();
		int wh = winH();
		graphics.fill(wx, wy, wx + WIN_W, wy + wh, WIN_BG_COLOR);
		graphics.outline(wx, wy, WIN_W, wh, WIN_BORDER_COLOR);

		this.renderHeader(graphics, font, mouseX, mouseY, wx, wy);
		this.renderList(graphics, font, mouseX, mouseY, wx, wy);
		this.contextMenu.render(graphics, font, mouseX, mouseY);
	}

	private void renderHeader(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, int wx, int wy) {
		int fieldX = wx + PAD;
		int fieldW = WIN_W - PAD - 14;
		int fieldY = wy + PAD;
		graphics.fill(fieldX, fieldY, fieldX + fieldW, fieldY + HEADER_H, this.inputActive ? FIELD_ACTIVE_COLOR : FIELD_BG_COLOR);
		graphics.outline(fieldX, fieldY, fieldW, HEADER_H, WIN_BORDER_COLOR);

		String display = this.inputBuffer;
		int textX = fieldX + 2;
		int textY = UiText.centerY(fieldY, HEADER_H);
		int maxFontW = Math.max(1, (int) ((fieldW - 4) / UiText.CONTENT_SCALE));
		if (font.width(display) > maxFontW) {
			display = font.plainSubstrByWidth(display, maxFontW);
		}
		if (this.inputActive) {
			this.cursorBlink++;
			if ((this.cursorBlink / 18) % 2 == 0) {
				display = display + "_";
			}
		}
		UiText.scaledText(graphics, font, display, textX, textY, LABEL_COLOR);

		int bx = wx + WIN_W - PAD - 12;
		int by = wy + PAD + 1;
		boolean hover = this.inAddButton(mouseX, mouseY);
		graphics.fill(bx, by, bx + 12, by + HEADER_H - 2, hover ? ADD_HOVER_COLOR : ADD_COLOR);
		graphics.outline(bx, by, 12, HEADER_H - 2, WIN_BORDER_COLOR);
		int plusX = bx + 6 - UiText.scaledWidth(font, "+") / 2;
		UiText.scaledText(graphics, font, "+", plusX, UiText.centerY(by, HEADER_H - 2), COUNT_COLOR);
	}

	private void renderList(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, int wx, int wy) {
		int listTop = this.listTop();
		int listLeft = wx + PAD;
		int listW = WIN_W - PAD * 2 - SCROLLBAR_W;
		graphics.enableScissor(listLeft, listTop, listLeft + listW, listTop + MAX_VISIBLE * ITEM_H);
		this.clampScroll();
		int cap = visibleCapacity();
		for (int i = 0; i < cap; i++) {
			int index = this.scroll + i;
			String id = this.members.get(index);
			int itemY = listTop + i * ITEM_H;
			boolean hover = mouseY >= itemY && mouseY < itemY + ITEM_H && mouseX >= listLeft && mouseX < listLeft + listW;
			boolean enabled = this.status.getBoolean(this.blockPath(id), false);
			if (hover) {
				graphics.fill(listLeft, itemY, listLeft + listW, itemY + ITEM_H, ITEM_HOVER_COLOR);
			} else if (enabled) {
				graphics.fill(listLeft, itemY, listLeft + listW, itemY + ITEM_H, ITEM_ENABLED_COLOR);
			} else {
				graphics.fill(listLeft, itemY, listLeft + listW, itemY + ITEM_H, ITEM_BG_COLOR);
			}
			this.renderBlockIcon(graphics, id, listLeft + 1, itemY + 1);
			int textX = listLeft + ICON + 3;
			int nameY = itemY + 2;
			Block block = BuiltInRegistries.BLOCK.getValue(Identifier.tryParse(id));
			String name = block != null ? block.getName().getString() : id;
			String fittedName = UiText.fit(font, name, Math.max(0, listW - (ICON + 5)));
			UiText.scaledText(graphics, font, fittedName, textX, nameY, enabled ? ITEM_NAME_COLOR : ITEM_ID_COLOR);
			int idY = nameY + 5;
			String fittedId = UiText.fit(font, id, Math.max(0, listW - (ICON + 5)));
			UiText.scaledText(graphics, font, fittedId, textX, idY, ITEM_ID_COLOR);
		}
		graphics.disableScissor();

		if (this.members.size() > MAX_VISIBLE) {
			int trackH = MAX_VISIBLE * ITEM_H;
			int thumbH = Math.max(6, trackH * trackH / (this.members.size() * ITEM_H));
			int thumbY = listTop + (trackH - thumbH) * this.scroll / Math.max(1, this.maxScroll());
			int sbX = wx + WIN_W - PAD - SCROLLBAR_W;
			graphics.fill(sbX, thumbY, sbX + SCROLLBAR_W, thumbY + thumbH, SCROLLBAR_COLOR);
		}
	}

	private void renderBlockIcon(GuiGraphicsExtractor graphics, String id, int x, int y) {
		Identifier identifier = Identifier.tryParse(id);
		if (identifier == null) {
			return;
		}
		Block block = BuiltInRegistries.BLOCK.getValue(identifier);
		if (block == null) {
			return;
		}
		if (block == Blocks.AIR) {
			return;
		}
		graphics.item(new ItemStack(block.asItem()), x, y);
	}
}
