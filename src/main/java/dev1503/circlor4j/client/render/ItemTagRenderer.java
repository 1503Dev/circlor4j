package dev1503.circlor4j.client.render;

import dev1503.circlor4j.client.module.modules.ItemTagModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class ItemTagRenderer {
	private static Vec3 cameraPos;
	private static Matrix4f viewRotation;
	private static Matrix4f projection;

	private ItemTagRenderer() {
	}

	public static void capture(CameraRenderState camera) {
		cameraPos = camera.pos;
		viewRotation = camera.viewRotationMatrix;
		projection = camera.projectionMatrix;
	}

	public static void render(GuiGraphicsExtractor graphics) {
		if (!ItemTagModule.isActive()) {
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
			if (!(entity instanceof ItemEntity itemEntity)) {
				continue;
			}
			Vec3 worldPos = itemEntity.position().add(0.0, itemEntity.getBbHeight() + 0.3, 0.0);
			Vec3 screen = worldToScreen(worldPos, screenWidth, screenHeight);
			if (screen == null) {
				continue;
			}
			ItemStack stack = itemEntity.getItem();
			String text = stack.getHoverName().getString() + " x" + stack.getCount();
			int textWidth = mc.font.width(text);
			float x = (float) screen.x - textWidth / 2.0F;
			float y = (float) screen.y;
			graphics.text(mc.font, text, (int) x, (int) y, 0xFFFFFFFF);
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
