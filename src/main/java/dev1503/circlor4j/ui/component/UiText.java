package dev1503.circlor4j.ui.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Small text helpers for UI components: scaled content text and label fitting. */
public final class UiText {
	public static final float CONTENT_SCALE = 2.0F / 3.0F;
	private static final String ELLIPSIS = "...";

	private UiText() {
	}

	/** Draws text at about 2/3 scale, anchored at (x, y). */
	public static void scaledText(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(CONTENT_SCALE, CONTENT_SCALE);
		graphics.pose().translate(-x, -y);
		graphics.text(font, text, x, y, color);
		graphics.pose().popMatrix();
	}

	/** Visual (scaled) width of the given text. */
	public static int scaledWidth(Font font, String text) {
		return (int) Math.ceil(font.width(text) * CONTENT_SCALE);
	}

	/** Vertically centres scaled text inside a row of the given height. */
	public static int centerY(int rowY, int rowHeight) {
		return rowY + Math.max(1, (rowHeight - 5) / 2);
	}

	/** Truncates text with an ellipsis so its scaled width fits maxScreenWidth. */
	public static String fit(Font font, String text, int maxScreenWidth) {
		if (maxScreenWidth <= 0) {
			return "";
		}
		int maxFontWidth = Math.max(1, (int) (maxScreenWidth / CONTENT_SCALE));
		if (font.width(text) <= maxFontWidth) {
			return text;
		}
		int ellipsisWidth = font.width(ELLIPSIS);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (font.width(builder.toString() + text.charAt(i)) + ellipsisWidth > maxFontWidth) {
				break;
			}
			builder.append(text.charAt(i));
		}
		return builder.toString() + ELLIPSIS;
	}
}
