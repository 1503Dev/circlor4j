package dev1503.circlor4j.ui.horionarraylist;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A minimal canvas abstraction that mirrors the Android {@code Canvas} draw calls used by the
 * original openhal4a array list (drawRect, drawLine, drawText). The array list builds its frame
 * through these primitives instead of Mojang's high-level GUI helpers.
 */
public final class CanvasRender {
	private CanvasRender() {
	}

	public static void fillRect(GuiGraphicsExtractor graphics, float x, float y, float w, float h, int color) {
		if (w <= 0.0F || h <= 0.0F) {
			return;
		}
		graphics.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color);
	}

	public static void drawLine(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, int color) {
		if (x1 == x2) {
			graphics.fill((int) x1, (int) Math.min(y1, y2), (int) x1 + 1, (int) Math.max(y1, y2) + 1, color);
		} else if (y1 == y2) {
			graphics.fill((int) Math.min(x1, x2), (int) y1, (int) Math.max(x1, x2) + 1, (int) y1 + 1, color);
		}
	}

	public static void drawText(GuiGraphicsExtractor graphics, Font font, String text, float x, float y, int color, float scale) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		graphics.pose().translate(-x, -y);
		graphics.text(font, text, (int) x, (int) y, color);
		graphics.pose().popMatrix();
	}
}