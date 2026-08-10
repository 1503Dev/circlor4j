package dev1503.circlor4j.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev1503.circlor4j.client.module.modules.EspModule;
import dev1503.circlor4j.client.module.modules.XrayModule;
import dev1503.circlor4j.client.render.EspRenderType;
import dev1503.circlor4j.client.render.ItemTagRenderer;
import dev1503.circlor4j.client.render.TracerRenderer;
import dev1503.circlor4j.client.render.XrayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Inject(method = "submitFeatures", at = @At("TAIL"))
	private void circlor4jEsp(
		LevelRenderState levelRenderState,
		SubmitNodeCollector submitNodeCollector,
		boolean renderOutline,
		CallbackInfo ci
	) {
		Minecraft minecraft = Minecraft.getInstance();
		TracerRenderer.capture(levelRenderState.cameraRenderState);
		ItemTagRenderer.capture(levelRenderState.cameraRenderState);
		ClientLevel level = minecraft.level;
		if (level == null) {
			return;
		}

		PoseStack poseStack = new PoseStack();
		poseStack.translate(
			-(float) levelRenderState.cameraRenderState.pos.x,
			-(float) levelRenderState.cameraRenderState.pos.y,
			-(float) levelRenderState.cameraRenderState.pos.z
		);

		if (XrayModule.isActive()) {
			XrayRenderer.render(poseStack, submitNodeCollector, levelRenderState.cameraRenderState.pos);
		}

		if (!EspModule.isActive()) {
			return;
		}
		Entity cameraEntity = minecraft.player;

		boolean mobs = EspModule.isMobsEnabled();
		boolean players = EspModule.isPlayersEnabled();
		boolean items = EspModule.isItemsEnabled();
		if (!mobs && !players && !items) {
			return;
		}

		float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		for (Entity entity : level.entitiesForRendering()) {
			if (entity == cameraEntity) {
				continue;
			}
			int color;
			if (entity instanceof Player) {
				if (!players) {
					continue;
				}
				color = EspModule.getPlayersColor();
			} else if (entity instanceof ItemEntity) {
				if (!items) {
					continue;
				}
				color = EspModule.getItemsColor();
			} else if (entity instanceof LivingEntity) {
				if (!mobs) {
					continue;
				}
				color = EspModule.getMobsColor();
			} else {
				continue;
			}

			AABB box = interpolatedBox(entity, partialTicks);
			VoxelShape shape = Shapes.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
			submitNodeCollector.submitShapeOutline(poseStack, shape, EspRenderType.LINES, color, EspModule.getThickness(), false);
		}
	}

	private static AABB interpolatedBox(Entity entity, float partialTicks) {
		Vec3 pos = entity.getPosition(partialTicks);
		return entity.getBoundingBox().move(pos.x - entity.getX(), pos.y - entity.getY(), pos.z - entity.getZ());
	}
}
