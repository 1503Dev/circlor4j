package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.client.module.modules.XrayModule;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A read-only, colour-editor popup synced with a block list. It lists the blocks from the sync
 * prefix (no add/delete/toggle), renders each item with a coloured border, opens an inline RGB
 * editor on left-click and a [Restore Default] context menu on right-click. Per-block colours are
 * stored as packed ARGB ints at "{colorsPath}/{blockId}".
 */
public class ColorList extends Component {
	private static final int WIN_W = 140;
	private static final int ITEM_H = 18;
	private static final int ICON = 16;
	private static final int BORDER_W = 3;
	private static final int MAX_VISIBLE = 8;
	private static final int SCROLLBAR_W = 2;
	private static final int EDIT_H = 30;
	private static final int SLIDER_W = 80;
	private static final int TRACK_H = 3;

	private static final int ROW_HOVER_COLOR = 0x40FFFFFF;
	private static final int ROW_TEXT_COLOR = 0xFFAAAAAA;

	private static final int WIN_BG_COLOR = 0xE0101010;
	private static final int WIN_BORDER_COLOR = 0xFF3A3A3A;
	private static final int ITEM_BG_COLOR = 0x20FFFFFF;
	private static final int ITEM_HOVER_COLOR = 0x40FFFFFF;
	private static final int ITEM_NAME_COLOR = 0xFFFFFFFF;
	private static final int ITEM_ID_COLOR = 0xFF777777;
	private static final int SCROLLBAR_COLOR = 0xFF9A9A9A;
	private static final int EDIT_BG_COLOR = 0x30FFFFFF;
	private static final int SLIDER_TRACK_COLOR = 0xFF444444;
	private static final int SLIDER_LABEL_COLOR = 0xFFCCCCCC;
	private static final int PREVIEW_BORDER = 0xFF000000;

	private static final int PAD = 2;

	private enum EditChannel { RED, GREEN, BLUE }

	private final StatusManager status;
	private final String label;
	private final String syncPrefix;
	private final String colorsPath;
	private int x;
	private int y;
	private int width;
	private int height = CategoryWindow.ROW_HEIGHT;

	private boolean windowOpen;
	private int scroll;
	private int editingIndex = -1;
	private EditChannel dragChannel = null;

	private final List<String> members = new ArrayList<>();
	private final ContextMenu contextMenu = new ContextMenu();

	public ColorList(StatusManager status, String path, String label, String syncBlocksOption, int x, int y, int width, int height) {
		super(path);
		this.status = status;
		this.label = label;
		this.syncPrefix = XrayModule.ID + "/" + syncBlocksOption + "/";
		this.colorsPath = path + "/";
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.rebuildMembers();
	}

	private String colorPath(String blockId) {
		return this.colorsPath + blockId;
	}

	private void rebuildMembers() {
		this.members.clear();
		for (Map.Entry<String, Double> entry : this.status.entriesWithPrefix(this.syncPrefix).entrySet()) {
			String rel = entry.getKey().substring(this.syncPrefix.length());
			if (!rel.isEmpty() && !rel.contains("/")) {
				this.members.add(rel);
			}
		}
	}

	@Override
	public void syncStatus(double value) {
		this.rebuildMembers();
	}

	public boolean isWindowOpen() {
		return this.windowOpen;
	}

	public void closeWindow() {
		this.windowOpen = false;
		this.editingIndex = -1;
		this.contextMenu.close();
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

	private boolean isEditing() {
		return this.editingIndex >= 0 && this.editingIndex < this.members.size();
	}

	private int editPanelH() {
		return this.isEditing() ? EDIT_H : 0;
	}

	private int winH() {
		return PAD + MAX_VISIBLE * ITEM_H + PAD + this.editPanelH();
	}

	public boolean containsWindow(int mx, int my) {
		int wx = this.winX();
		int wy = this.winY();
		return mx >= wx && mx < wx + WIN_W && my >= wy && my < wy + winH();
	}

	private int listTop() {
		return this.winY() + PAD;
	}

	private int editTop() {
		return this.winY() + PAD + MAX_VISIBLE * ITEM_H + PAD;
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

	public boolean mouseClickedRow(MouseButtonEvent event) {
		if (event.button() != 0 || !this.containsRow((int) event.x(), (int) event.y())) {
			return false;
		}
		this.windowOpen = !this.windowOpen;
		if (this.windowOpen) {
			this.editingIndex = -1;
			this.clampScroll();
		}
		return true;
	}

	public boolean mouseClickedWindow(MouseButtonEvent event, Font font) {
		if (!this.windowOpen) {
			return false;
		}
		int mx = (int) event.x();
		int my = (int) event.y();
		if (!this.containsWindow(mx, my)) {
			return false;
		}
		boolean menuWasOpen = this.contextMenu.isOpen();
		if (this.contextMenu.mouseClicked(event)) {
			return true;
		}
		if (menuWasOpen) {
			return true;
		}
		if (event.button() == 0 && this.isEditing() && this.inEditPanel(mx, my)) {
			EditChannel ch = this.channelAt(mx, my);
			if (ch != null) {
				this.dragChannel = ch;
				this.updateChannelFromMouse(ch, mx);
				return true;
			}
		}
		int index = this.itemAt(mx, my);
		if (index >= 0 && index < this.members.size()) {
			if (event.button() == 0) {
				this.editingIndex = index;
			} else if (event.button() == 1) {
				this.openContextMenu(index, mx, my);
			}
			return true;
		}
		this.editingIndex = -1;
		return true;
	}

	public boolean mouseDraggedWindow(int mx, int my) {
		if (this.dragChannel == null) {
			return false;
		}
		this.updateChannelFromMouse(this.dragChannel, mx);
		return true;
	}

	public void mouseReleasedWindow() {
		this.dragChannel = null;
	}

	private void openContextMenu(int index, int mx, int my) {
		this.contextMenu.close();
		String id = this.members.get(index);
		this.contextMenu.add("恢复默认", () -> {
			this.status.setValue(this, this.colorPath(id), XrayModule.defaultColorFor(id));
		});
		this.contextMenu.open(mx, my);
	}

	public void mouseScrolled(int mx, int my, double scrollDelta) {
		if (!this.windowOpen || !this.containsWindow(mx, my)) {
			return;
		}
		if (scrollDelta > 0.0) {
			this.scroll = Math.max(0, this.scroll - 1);
		} else if (scrollDelta < 0.0) {
			this.scroll = Math.min(this.maxScroll(), this.scroll + 1);
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

	private int editingColor() {
		String id = this.members.get(this.editingIndex);
		return this.status.getInt(this.colorPath(id), XrayModule.defaultColorFor(id));
	}

	private void setEditingColor(int argb) {
		String id = this.members.get(this.editingIndex);
		this.status.setValue(this, this.colorPath(id), argb);
	}

	private int channelValue(int argb, EditChannel ch) {
		return switch (ch) {
			case RED -> (argb >> 16) & 0xFF;
			case GREEN -> (argb >> 8) & 0xFF;
			case BLUE -> argb & 0xFF;
		};
	}

	private int withChannel(int argb, EditChannel ch, int value) {
		value = Math.max(0, Math.min(255, value));
		return switch (ch) {
			case RED -> (argb & 0xFF00FFFF) | (value << 16);
			case GREEN -> (argb & 0xFFFF00FF) | (value << 8);
			case BLUE -> (argb & 0xFFFFFF00) | value;
		};
	}

	private int sliderLeft() {
		return this.winX() + 18;
	}

	private int sliderRight() {
		return this.sliderLeft() + SLIDER_W;
	}

	private int channelRowY(EditChannel ch) {
		return this.editTop() + 3 + ch.ordinal() * 9;
	}

	private boolean inEditPanel(int mx, int my) {
		return this.isEditing() && my >= this.editTop() && my < this.editTop() + EDIT_H;
	}

	private EditChannel channelAt(int mx, int my) {
		if (mx < this.sliderLeft() || mx >= this.sliderRight()) {
			return null;
		}
		for (EditChannel ch : EditChannel.values()) {
			int rowY = this.channelRowY(ch);
			if (my >= rowY && my < rowY + 9) {
				return ch;
			}
		}
		return null;
	}

	private void updateChannelFromMouse(EditChannel ch, int mx) {
		double t = (mx - this.sliderLeft()) / (double) SLIDER_W;
		t = Math.max(0.0, Math.min(1.0, t));
		int value = (int) Math.round(t * 255.0);
		this.setEditingColor(this.withChannel(this.editingColor(), ch, value));
	}

	public void renderRow(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		if (this.containsRow(mouseX, mouseY)) {
			graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, ROW_HOVER_COLOR);
		}
		String labelText = UiText.fit(font, this.label, Math.max(0, this.width - PAD * 2));
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
		this.renderList(graphics, font, mouseX, mouseY, wx, wy);
		if (this.isEditing()) {
			this.renderEditor(graphics, font, mouseX, mouseY, wx);
		}
		this.contextMenu.render(graphics, font, mouseX, mouseY);
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
			boolean selected = index == this.editingIndex;
			if (selected) {
				graphics.fill(listLeft, itemY, listLeft + listW, itemY + ITEM_H, ITEM_HOVER_COLOR);
			} else if (hover) {
				graphics.fill(listLeft, itemY, listLeft + listW, itemY + ITEM_H, ITEM_HOVER_COLOR);
			} else {
				graphics.fill(listLeft, itemY, listLeft + listW, itemY + ITEM_H, ITEM_BG_COLOR);
			}
			int color = this.status.getInt(this.colorPath(id), XrayModule.defaultColorFor(id));
			graphics.fill(listLeft, itemY, listLeft + BORDER_W, itemY + ITEM_H, color);
			this.renderBlockIcon(graphics, id, listLeft + BORDER_W + 1, itemY + 1);
			int textX = listLeft + BORDER_W + ICON + 4;
			Block block = BuiltInRegistries.BLOCK.getValue(Identifier.tryParse(id));
			String name = block != null ? block.getName().getString() : id;
			String fittedName = UiText.fit(font, name, Math.max(0, listW - (BORDER_W + ICON + 6)));
			UiText.scaledText(graphics, font, fittedName, textX, itemY + 2, ITEM_NAME_COLOR);
			String fittedId = UiText.fit(font, id, Math.max(0, listW - (BORDER_W + ICON + 6)));
			UiText.scaledText(graphics, font, fittedId, textX, itemY + 8, ITEM_ID_COLOR);
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

	private void renderEditor(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, int wx) {
		int top = this.editTop();
		int color = this.editingColor();
		graphics.fill(wx + PAD, top, wx + WIN_W - PAD, top + EDIT_H, EDIT_BG_COLOR);
		int previewX = wx + WIN_W - PAD - 12;
		graphics.fill(previewX, top + 3, previewX + 12, top + EDIT_H - 3, color);
		graphics.outline(previewX, top + 3, 12, EDIT_H - 6, PREVIEW_BORDER);

		int labelX = wx + PAD;
		for (EditChannel ch : EditChannel.values()) {
			int rowY = this.channelRowY(ch);
			String tag = switch (ch) {
				case RED -> "R";
				case GREEN -> "G";
				case BLUE -> "B";
			};
			UiText.scaledText(graphics, font, tag, labelX, UiText.centerY(rowY, 9), SLIDER_LABEL_COLOR);
			int value = this.channelValue(color, ch);
			int trackY = rowY + (9 - TRACK_H) / 2;
			int left = this.sliderLeft();
			int right = this.sliderRight();
			graphics.fill(left, trackY, right, trackY + TRACK_H, SLIDER_TRACK_COLOR);
			int filled = left + (int) Math.round((value / 255.0) * SLIDER_W);
			int channelColor = switch (ch) {
				case RED -> 0xFFFF0000;
				case GREEN -> 0xFF00FF00;
				case BLUE -> 0xFF0000FF;
			};
			graphics.fill(left, trackY, filled, trackY + TRACK_H, channelColor);
			String valStr = value + "";
			UiText.scaledText(graphics, font, valStr, right + 2, UiText.centerY(rowY, 9), SLIDER_LABEL_COLOR);
		}
	}

	private void renderBlockIcon(GuiGraphicsExtractor graphics, String id, int x, int y) {
		Identifier identifier = Identifier.tryParse(id);
		if (identifier == null) {
			return;
		}
		Block block = BuiltInRegistries.BLOCK.getValue(identifier);
		if (block == null || block == Blocks.AIR) {
			return;
		}
		graphics.item(new ItemStack(block.asItem()), x, y);
	}
}
