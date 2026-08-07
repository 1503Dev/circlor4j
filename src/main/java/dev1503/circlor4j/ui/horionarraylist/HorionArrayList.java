package dev1503.circlor4j.ui.horionarraylist;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.client.module.ModuleManager;
import dev1503.circlor4j.client.module.modules.ArraylistModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Faithful port of the Android Horion array list (from openhal4a). Rows are laid out per the
 * selected gravity, sorted longest-first, with translucent backgrounds, optional edge/outline
 * borders (with width-change connectors), per-row rainbow/category/custom colours, text shadows,
 * and simultaneous slide-in / slide-out / smooth vertical movement.
 */
public final class HorionArrayList {
	public static final int GRAVITY_TOP_LEFT = 0;
	public static final int GRAVITY_TOP_RIGHT = 1;
	public static final int GRAVITY_BOTTOM_LEFT = 2;
	public static final int GRAVITY_BOTTOM_RIGHT = 3;

	private static final int EDGE_PADDING = 0;
	private static final int MODE_TEXT_COLOR = 0xFFC5C5C5;
	private static final float RAINBOW_STEP = 8.0F;
	private static final float HUE_SPEED = 0.04F;
	private static final float SLIDE_DURATION = 400.0F;
	private static final float EXIT_DURATION = 475.0F;
	private static final float LAYOUT_SMOOTH_RATE = 10.0F;
	private static final float GRAVITY_SLIDE_DURATION = 300.0F;

	private static final Map<Integer, ArrayListItem> items = new HashMap<>();
	private static final Map<Integer, Float> slideProgress = new HashMap<>();
	private static final Map<Integer, Float> exitProgress = new HashMap<>();
	private static final Map<Integer, Float> currentY = new HashMap<>();
	private static final Map<Integer, Float> currentX = new HashMap<>();
	private static float baseHue;
	private static long lastFrameTime;
	private static long lastLayoutTime;

	private static int gravityFrom;
	private static float gravitySlideProgress = 1.0F;
	private static int lastGravity = GRAVITY_TOP_RIGHT;
	private static float gravityAnimatorTime;

	private HorionArrayList() {
	}

	public static void setItem(int id, ArrayListItem item) {
		if (exitProgress.containsKey(id)) {
			exitProgress.remove(id);
		}
		items.put(id, item);
		if (!slideProgress.containsKey(id)) {
			slideProgress.put(id, 0.0F);
		}
	}

	public static void removeItem(int id) {
		if (!items.containsKey(id)) {
			exitProgress.remove(id);
			currentY.remove(id);
			currentX.remove(id);
			return;
		}
		exitProgress.putIfAbsent(id, 0.0F);
	}

	public static void render(GuiGraphicsExtractor graphics) {
		if (!ArraylistModule.isActive()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}
		Font font = mc.font;
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		syncItems();
		if (items.isEmpty()) {
			return;
		}

		updateRainbow();

		long now = System.currentTimeMillis();
		float dtMs = lastLayoutTime == 0L ? 0.0F : Math.min(Math.max(now - lastLayoutTime, 0), 50);
		lastLayoutTime = now;

		int gravity = ArraylistModule.getGravity();
		if (gravity != lastGravity) {
			gravityFrom = lastGravity;
			gravitySlideProgress = 0.0F;
			gravityAnimatorTime = 0.0F;
			lastGravity = gravity;
		}
		if (gravitySlideProgress < 1.0F) {
			gravityAnimatorTime = Math.min(gravityAnimatorTime + dtMs, GRAVITY_SLIDE_DURATION);
			gravitySlideProgress = Math.min(1.0F, gravityAnimatorTime / GRAVITY_SLIDE_DURATION);
			gravitySlideProgress = 1.0F - (1.0F - gravitySlideProgress) * (1.0F - gravitySlideProgress);
		}

		List<RowLayout> layouts = computeLayout(gravity, screenWidth, screenHeight);
		List<RowLayout> oldLayout = gravitySlideProgress < 1.0F ? computeLayout(gravityFrom, screenWidth, screenHeight) : null;
		Map<Integer, RowLayout> oldById = new HashMap<>();
		if (oldLayout != null) {
			for (RowLayout l : oldLayout) {
				oldById.put(l.id, l);
			}
		}

		int count = layouts.size();
		int[] xs = new int[count];
		int[] ys = new int[count];
		int[] widths = new int[count];

		float alpha = Math.min(1.0F, dtMs / 1000.0F * LAYOUT_SMOOTH_RATE);
		for (int i = 0; i < count; i++) {
			RowLayout layout = layouts.get(i);
			int id = layout.id;
			float progress = slideProgress.getOrDefault(id, 1.0F);
			float slideDir = isRightGravity(gravity) ? 1.0F : -1.0F;

			float targetX = layout.x;
			float targetY = layout.y;
			if (gravitySlideProgress < 1.0F) {
				RowLayout old = oldById.get(id);
				if (old != null) {
					float t = gravitySlideProgress;
					targetX = old.x + (layout.x - old.x) * t;
					targetY = old.y + (layout.y - old.y) * t;
				}
			}

			float x = targetX + slideDir * screenWidth * (1.0F - progress);

			Float cur = currentY.get(id);
			float rowY;
			if (cur == null) {
				rowY = targetY;
			} else {
				rowY = cur + (targetY - cur) * alpha;
				if (Math.abs(targetY - rowY) < 0.5F) {
					rowY = targetY;
				}
			}
			currentY.put(id, rowY);
			currentX.put(id, x);

			xs[i] = (int) x;
			ys[i] = (int) rowY;
			widths[i] = (int) layout.width;
		}

		for (int i = 0; i < count; i++) {
			drawRowBackground(graphics, xs[i], ys[i], widths[i]);
		}
		for (int i = 0; i < count; i++) {
			ArrayListItem item = items.get(layouts.get(i).id);
			drawRowText(graphics, font, item, xs[i], ys[i], widths[i]);
		}
		for (int i = 0; i < count; i++) {
			ArrayListItem item = items.get(layouts.get(i).id);
			Integer prevBorderX = i > 0 ? borderXOf(xs[i - 1], widths[i - 1], gravity) : null;
			Integer nextBorderX = i < count - 1 ? borderXOf(xs[i + 1], widths[i + 1], gravity) : null;
			Integer prevWidth = i > 0 ? widths[i - 1] : null;
			Integer nextWidth = i < count - 1 ? widths[i + 1] : null;
			drawRowBorder(graphics, font, item, xs[i], ys[i], widths[i], i, count, prevBorderX, nextBorderX, prevWidth, nextWidth);
		}

		// exiting rows slide out from their frozen position without holding a layout slot
		for (Map.Entry<Integer, Float> entry : exitProgress.entrySet()) {
			int id = entry.getKey();
			ArrayListItem item = items.get(id);
			if (item == null) {
				continue;
			}
			float exit = Math.min(1.0F, entry.getValue());
			float slideDir = isRightGravity(gravity) ? 1.0F : -1.0F;
			float y = currentY.getOrDefault(id, 0.0F);
			float x = currentX.getOrDefault(id, isRightGravity(gravity) ? screenWidth : 0.0F) + slideDir * screenWidth * exit;
			int rowH = rowHeightPx(font);
			int width = (int) measureItemWidth(font, item);
			drawRowBackground(graphics, (int) x, (int) y, width);
			drawRowText(graphics, font, item, x, y, width);
		}

		// advance animations and remove finished exits
		Set<Integer> ids = new HashSet<>(items.keySet());
		for (int id : ids) {
			if (slideProgress.containsKey(id)) {
				float p = Math.min(1.0F, slideProgress.get(id) + dtMs / SLIDE_DURATION);
				slideProgress.put(id, p);
			}
			if (exitProgress.containsKey(id)) {
				float e = Math.min(1.0F, exitProgress.get(id) + dtMs / EXIT_DURATION);
				exitProgress.put(id, e);
				if (e >= 1.0F) {
					items.remove(id);
					slideProgress.remove(id);
					exitProgress.remove(id);
					currentY.remove(id);
					currentX.remove(id);
				}
			}
		}
	}

	private static void syncItems() {
		Set<Integer> active = new HashSet<>();
		for (Module module : ModuleManager.all()) {
			if (!module.isEnabled() || module.getCategory() == ModuleCategory.CIRCLOR) {
				continue;
			}
			int id = module.getId().hashCode();
			active.add(id);
			ArrayListItem item = items.get(id);
			if (item == null) {
				setItem(id, new ArrayListItem(module.getRawName(), keybindFor(module), modeTextFor(module), module.getCategory().ordinal()));
			} else {
				item.keybind = keybindFor(module);
				item.modeText = modeTextFor(module);
			}
		}
		Set<Integer> toRemove = new HashSet<>();
		for (int id : items.keySet()) {
			if (!active.contains(id)) {
				toRemove.add(id);
			}
		}
		for (int id : toRemove) {
			removeItem(id);
		}
	}

	private static String keybindFor(Module module) {
		if (!ArraylistModule.isShowKeybinds()) {
			return null;
		}
		for (dev1503.circlor4j.client.keybind.KeyBind bind : dev1503.circlor4j.client.keybind.KeyBindManager.all()) {
			if (dev1503.circlor4j.client.keybind.KeyBindManager.resolveFunction(bind.getFunction()) == module) {
				return bind.getDisplayName();
			}
		}
		return null;
	}

	private static String modeTextFor(Module module) {
		if (!ArraylistModule.isShowModes()) {
			return null;
		}
		String mode = null;
		for (Module.Setting setting : module.getSettings()) {
			if (setting instanceof Module.DropdownSetting dropdown) {
				if (dropdown.path().endsWith("/mode")) {
					int index = (int) module.getStatus().getDouble(dropdown.path(), dropdown.defaultIndex());
					if (index >= 0 && index < dropdown.itemFallbacks().length) {
						mode = dropdown.itemFallbacks()[index];
					}
				}
			}
		}
		String value = null;
		for (Module.Setting setting : module.getSettings()) {
			if (setting instanceof Module.DoubleSetting slider) {
				double v = module.getStatus().getDouble(slider.path(), slider.defaultValue());
				value = formatNumber(v, slider.step());
				break;
			}
		}
		if (mode != null && value != null) {
			return mode + " " + value;
		}
		return mode != null ? mode : value;
	}

	private static String formatNumber(double value, double step) {
		if (step == Math.floor(step)) {
			return Long.toString(Math.round(value));
		}
		int decimals = 0;
		String plain = java.math.BigDecimal.valueOf(step).stripTrailingZeros().toPlainString();
		int dot = plain.indexOf('.');
		if (dot >= 0) {
			decimals = plain.length() - dot - 1;
		}
		return String.format(java.util.Locale.ROOT, "%." + decimals + "f", value);
	}

	private static float rowHeight(Font font) {
		return font.lineHeight * ArraylistModule.getTextSize() + ArraylistModule.getRowPadding();
	}

	private static int rowHeightPx(Font font) {
		return Math.max(1, (int) Math.round(rowHeight(font)));
	}

	private static float measureItemWidth(Font font, ArrayListItem item) {
		String mainText = mainTextFor(item);
		String modeText = modeTextFor(item);
		String fullText = modeText != null ? mainText + modeText : mainText;
		return font.width(fullText) * ArraylistModule.getTextSize() + ArraylistModule.getRowPadding() * 2.0F;
	}

	private static String mainTextFor(ArrayListItem item) {
		StringBuilder builder = new StringBuilder(item.text);
		if (ArraylistModule.isShowKeybinds() && item.keybind != null) {
			builder.append(" [").append(item.keybind).append(']');
		}
		return builder.toString();
	}

	private static String modeTextFor(ArrayListItem item) {
		return ArraylistModule.isShowModes() && item.modeText != null ? " " + item.modeText : null;
	}

	private static class RowLayout {
		final int id;
		final float x;
		final float y;
		final float width;

		RowLayout(int id, float x, float y, float width) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.width = width;
		}
	}

	private static List<RowLayout> computeLayout(int gravity, int screenWidth, int screenHeight) {
		List<Map.Entry<Integer, ArrayListItem>> entries = new ArrayList<>();
		for (Map.Entry<Integer, ArrayListItem> entry : items.entrySet()) {
			if (!exitProgress.containsKey(entry.getKey())) {
				entries.add(entry);
			}
		}
		boolean top = gravity == GRAVITY_TOP_LEFT || gravity == GRAVITY_TOP_RIGHT;
		boolean right = gravity == GRAVITY_TOP_RIGHT || gravity == GRAVITY_BOTTOM_RIGHT;
		entries.sort((a, b) -> {
			float wa = measureItemWidth(Minecraft.getInstance().font, a.getValue());
			float wb = measureItemWidth(Minecraft.getInstance().font, b.getValue());
			return Float.compare(top ? wb : wa, top ? wa : wb);
		});

		int rowH = rowHeightPx(Minecraft.getInstance().font);
		int contentHeight = rowH * entries.size();
		boolean bottom = gravity == GRAVITY_BOTTOM_LEFT || gravity == GRAVITY_BOTTOM_RIGHT;
		float startY = bottom && screenHeight - EDGE_PADDING * 2 >= contentHeight
			? screenHeight - EDGE_PADDING - contentHeight
			: EDGE_PADDING;

		List<RowLayout> rows = new ArrayList<>();
		float y = startY;
		for (Map.Entry<Integer, ArrayListItem> entry : entries) {
			float rowWidth = measureItemWidth(Minecraft.getInstance().font, entry.getValue());
			float x = right ? screenWidth - EDGE_PADDING - rowWidth : EDGE_PADDING;
			rows.add(new RowLayout(entry.getKey(), x, y, rowWidth));
			y += rowH;
		}
		return rows;
	}
	private static int borderXOf(float x, float width, int gravity) {
		return (int) x + (isLeftGravity(gravity) ? (int) width : 0);
	}

	private static void drawRowBackground(GuiGraphicsExtractor graphics, int x, int y, int width) {
		CanvasRender.fillRect(graphics, x, y, width, rowHeightPx(Minecraft.getInstance().font), ArraylistModule.getBackgroundColor());
	}

	private static void drawRowText(GuiGraphicsExtractor graphics, Font font, ArrayListItem item, float x, float y, float width) {
		int color = colorFor(item, 0);
		String mainText = mainTextFor(item);
		String modeText = modeTextFor(item);
		float scale = ArraylistModule.getTextSize();
		float mainWidth = font.width(mainText) * scale;
		float modeWidth = modeText != null ? font.width(modeText) * scale : 0.0F;
		float pad = ArraylistModule.getRowPadding();
		float textY = y + (rowHeight(font) - font.lineHeight * scale) / 2.0F;

		float mainX;
		float modeX;
		if (isRightGravity(ArraylistModule.getGravity())) {
			modeX = x + width - pad - modeWidth;
			mainX = modeX - mainWidth;
		} else {
			mainX = x + pad;
			modeX = mainX + mainWidth;
		}

		if (ArraylistModule.isTextShadow()) {
			CanvasRender.drawText(graphics, font, mainText, mainX + 1, textY + 1, shadowColorOf(color), scale);
			if (modeText != null) {
				CanvasRender.drawText(graphics, font, modeText, modeX + 1, textY + 1, shadowColorOf(MODE_TEXT_COLOR), scale);
			}
		}
		CanvasRender.drawText(graphics, font, mainText, mainX, textY, color, scale);
		if (modeText != null) {
			CanvasRender.drawText(graphics, font, modeText, modeX, textY, MODE_TEXT_COLOR, scale);
		}
	}

	private static int shadowColorOf(int color) {
		int alpha = (color >>> 24) & 0xFF;
		int r = Math.min(255, ((color >> 16) & 0xFF) / 4);
		int g = Math.min(255, ((color >> 8) & 0xFF) / 4);
		int b = Math.min(255, (color & 0xFF) / 4);
		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}

	private static void drawRowBorder(
		GuiGraphicsExtractor graphics,
		Font font,
		ArrayListItem item,
		int x,
		int y,
		int width,
		int index,
		int count,
		Integer prevBorderX,
		Integer nextBorderX,
		Integer prevWidth,
		Integer nextWidth
	) {
		int color = colorFor(item, index);
		int mode = ArraylistModule.getMode();
		if (mode == 0) {
			return;
		}
		int ih = rowHeightPx(font);
		int bw = Math.max(1, (int) Math.round(ArraylistModule.getBorderThickness()));
		boolean left = isLeftGravity(ArraylistModule.getGravity());
		int borderX = left ? x + width : x;

		if (mode == 2) {
			CanvasRender.fillRect(graphics, borderX, y, bw, ih, color);
			return;
		}

		CanvasRender.fillRect(graphics, borderX, y, bw, ih, color);
		if (index == 0) {
			CanvasRender.fillRect(graphics, x, y, width, bw, color);
		}
		if (index == count - 1) {
			CanvasRender.fillRect(graphics, x, y + ih - bw, width, bw, color);
		}
		if (index > 0 && prevBorderX != null && prevWidth != null && prevWidth != width) {
			int lo = Math.min(borderX, prevBorderX);
			CanvasRender.fillRect(graphics, lo, y, Math.abs(borderX - prevBorderX), bw, color);
		}
		if (index < count - 1 && nextBorderX != null && nextWidth != null && nextWidth != width) {
			int lo = Math.min(borderX, nextBorderX);
			CanvasRender.fillRect(graphics, lo, y + ih - bw, Math.abs(borderX - nextBorderX), bw, color);
		}
	}

	private static int colorFor(ArrayListItem item, int index) {
		return switch (ArraylistModule.getColorMode()) {
			case 0 -> rainbow(baseHue + index * RAINBOW_STEP);
			case 1 -> categoryColor(item.category);
			default -> ArraylistModule.getCustomColor();
		};
	}

	private static int rainbow(float hue) {
		float sat = ArraylistModule.getRainbowMode() == 1 ? 0.9F : 0.35F;
		return hsvToArgb(hue % 360.0F, sat, 1.0F);
	}

	private static int hsvToArgb(float h, float s, float v) {
		float c = v * s;
		float hp = (h % 360.0F) / 60.0F;
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
		return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
	}

	private static int categoryColor(int category) {
		return switch (category) {
			case 0 -> 0xFFFF5555;
			case 1 -> 0xFF55FF55;
			case 2 -> 0xFF55AAFF;
			case 3 -> 0xFFFFAA55;
			case 4 -> 0xFFAA55FF;
			default -> 0xFFFFFFFF;
		};
	}

	private static void updateRainbow() {
		if (ArraylistModule.getColorMode() != 0) {
			return;
		}
		long now = System.currentTimeMillis();
		if (lastFrameTime == 0L) {
			lastFrameTime = now;
			return;
		}
		float delta = Math.min(Math.max(now - lastFrameTime, 0), 50);
		lastFrameTime = now;
		baseHue = (baseHue + delta * HUE_SPEED) % 360.0F;
	}

	private static boolean isRightGravity(int gravity) {
		return gravity == GRAVITY_TOP_RIGHT || gravity == GRAVITY_BOTTOM_RIGHT;
	}

	private static boolean isLeftGravity(int gravity) {
		return gravity == GRAVITY_TOP_LEFT || gravity == GRAVITY_BOTTOM_LEFT;
	}
}