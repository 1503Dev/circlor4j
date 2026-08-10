package dev1503.circlor4j.client.render;

import dev1503.circlor4j.client.module.modules.NameTagModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class NameTagRenderer {
	private static Vec3 cameraPos;
	private static Matrix4f viewRotation;
	private static Matrix4f projection;

	private NameTagRenderer() {
	}

	public static void capture(CameraRenderState camera) {
		cameraPos = camera.pos;
		viewRotation = camera.viewRotationMatrix;
		projection = camera.projectionMatrix;
	}

	public static void render(GuiGraphicsExtractor graphics) {
		if (!NameTagModule.isActive()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || cameraPos == null || viewRotation == null || projection == null) {
			return;
		}

		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		for (net.minecraft.world.entity.Entity entity : level.entitiesForRendering()) {
			if (!(entity instanceof Player player)) {
				continue;
			}
			if (player == mc.player) {
				continue;
			}
			Vec3 worldPos = entity.position().add(0.0, entity.getBbHeight() + 0.5, 0.0);
			Vec3 screen = worldToScreen(worldPos, screenWidth, screenHeight);
			if (screen == null) {
				continue;
			}

			float nameX = (float) screen.x;
			float nameY = (float) screen.y;

			String name = player.getName().getString();
			int nameWidth = mc.font.width(name);
			graphics.text(mc.font, name, (int) (nameX - nameWidth / 2.0F), (int) nameY, 0xFFFFFFFF);

			if (!NameTagModule.isShowHealth()) {
				continue;
			}

			float health = player.getHealth();
			float maxHealth = player.getMaxHealth();
			String healthText = String.format("%.1f", health) + "/" + String.format("%.1f", maxHealth) + " HP";
			int healthWidth = mc.font.width(healthText);
			int healthColor = health > maxHealth * 0.6F ? 0xFF55FF55 : health > maxHealth * 0.3F ? 0xFFFFFF55 : 0xFFFF5555;
			graphics.text(mc.font, healthText, (int) (nameX - healthWidth / 2.0F), (int) nameY + 10, healthColor);
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
}
