package dev1503.circlor4j.client.render;

import dev1503.circlor4j.client.module.modules.TracerModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Draws the Tracer screen-space lines. The camera state (position + view/projection matrices) is
 * captured during the world render by {@code LevelRendererMixin}; {@code HudMixin} then projects
 * each selected entity's box centre to screen and draws a line from the screen centre to it.
 */
public final class TracerRenderer {
	private static Vec3 cameraPos;
	private static Matrix4f viewRotation;
	private static Matrix4f projection;

	private TracerRenderer() {
	}

	public static void capture(CameraRenderState camera) {
		cameraPos = camera.pos;
		viewRotation = camera.viewRotationMatrix;
		projection = camera.projectionMatrix;
	}

	public static void render(GuiGraphicsExtractor graphics) {
		if (!TracerModule.isActive()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || cameraPos == null || viewRotation == null || projection == null) {
			return;
		}

		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();
		float cx = screenWidth / 2.0F;
		float cy = screenHeight / 2.0F;
		float thickness = TracerModule.getThickness();

		for (Entity entity : level.entitiesForRendering()) {
			int color;
			if (entity instanceof Player) {
				if (!TracerModule.isPlayersEnabled() || entity == mc.player) {
					continue;
				}
				color = TracerModule.getPlayersColor();
			} else if (entity instanceof ItemEntity) {
				if (!TracerModule.isItemsEnabled()) {
					continue;
				}
				color = TracerModule.getItemsColor();
			} else if (entity instanceof LivingEntity) {
				if (!TracerModule.isMobsEnabled()) {
					continue;
				}
				color = TracerModule.getMobsColor();
			} else {
				continue;
			}

			Vec3 screen = worldToScreen(entity.getBoundingBox().getCenter(), screenWidth, screenHeight);
			if (screen == null) {
				continue;
			}
			drawLine(graphics, cx, cy, (float) screen.x, (float) screen.y, thickness, color);
		}
	}

	private static Vec3 worldToScreen(Vec3 world, int screenWidth, int screenHeight) {
		Vector4f p = new Vector4f(
			(float) (world.x - cameraPos.x),
			(float) (world.y - cameraPos.y),
			(float) (world.z - cameraPos.z),
			1.0F
		);
		p.mul(viewRotation);
		p.mul(projection);
		if (p.w <= 0.0001F) {
			return null;
		}
		float ndcX = p.x / p.w;
		float ndcY = p.y / p.w;
		return new Vec3((ndcX + 1.0F) * 0.5F * screenWidth, (1.0F - ndcY) * 0.5F * screenHeight, 0.0);
	}

	private static void drawLine(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, float thickness, int color) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		float length = (float) Math.sqrt(dx * dx + dy * dy);
		if (length < 0.5F) {
			return;
		}
		float angle = (float) Math.atan2(dy, dx);
		float t = Math.max(0.01F, thickness);
		graphics.pose().pushMatrix();
		graphics.pose().translate(x1, y1);
		graphics.pose().rotate(angle);
		graphics.pose().scale(t, t);
		graphics.pose().translate(0.0F, -0.5F);
		graphics.fill(0, 0, (int) Math.ceil(length / t) + 1, 1, color);
		graphics.pose().popMatrix();
	}
}