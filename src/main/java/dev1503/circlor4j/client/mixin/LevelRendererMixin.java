package dev1503.circlor4j.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev1503.circlor4j.client.module.modules.EspModule;
import dev1503.circlor4j.client.render.EspRenderType;
import dev1503.circlor4j.client.render.TracerRenderer;
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

/**
 * Draws the ESP wireframe boxes. Injects into {@code LevelRenderer.submitFeatures} (the
 * entity/feature submit pass) and submits a {@link EspRenderType#LINES} shape outline for
 * each selected entity's interpolated collision box, translated to be camera-relative like
 * the vanilla block outline. The custom render type has depth testing disabled, so the boxes
 * are never occluded by terrain.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Inject(method = "submitFeatures", at = @At("TAIL"))
	private void circlor4jEsp(
		LevelRenderState levelRenderState,
		SubmitNodeCollector submitNodeCollector,
		boolean renderOutline,
		CallbackInfo ci
	) {
		TracerRenderer.capture(levelRenderState.cameraRenderState);
		if (!EspModule.isActive()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		Entity cameraEntity = minecraft.player;
		if (level == null) {
			return;
		}

		boolean mobs = EspModule.isMobsEnabled();
		boolean players = EspModule.isPlayersEnabled();
		boolean items = EspModule.isItemsEnabled();
		if (!mobs && !players && !items) {
			return;
		}

		float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		PoseStack poseStack = new PoseStack();
		poseStack.translate(
			-(float) levelRenderState.cameraRenderState.pos.x,
			-(float) levelRenderState.cameraRenderState.pos.y,
			-(float) levelRenderState.cameraRenderState.pos.z
		);

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

	/** Moves the collision box by the entity's interpolated movement so it renders smoothly between ticks. */
	private static AABB interpolatedBox(Entity entity, float partialTicks) {
		Vec3 pos = entity.getPosition(partialTicks);
		return entity.getBoundingBox().move(pos.x - entity.getX(), pos.y - entity.getY(), pos.z - entity.getZ());
	}
}