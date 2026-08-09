package dev1503.circlor4j.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev1503.circlor4j.client.module.modules.LowFireModule;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.ScreenEffectRenderer.class)
public abstract class LowFireScreenEffectMixin {

	@Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
	private static void onSubmitFire(PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, TextureAtlasSprite sprite, CallbackInfo ci) {
		if (LowFireModule.isActive()) {
			submitNodeCollector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.fireScreenEffect(sprite.atlasLocation()), (basePose, builder) -> {
				Matrix4f pose = new Matrix4f();
				pose.set(basePose.pose());
				pose.translate(0.24F, -0.3F, 0.0F);
				pose.rotateY((float) (-Math.PI / 18));
				buildFireQuad(sprite, builder, pose);
				pose = new Matrix4f();
				pose.set(basePose.pose());
				pose.translate(-0.24F, -0.3F, 0.0F);
				pose.rotateY((float) (Math.PI / 18));
				buildFireQuad(sprite, builder, pose);
			});
			ci.cancel();
		}
	}

	@Unique
	private static void buildFireQuad(TextureAtlasSprite sprite, VertexConsumer builder, Matrix4f pose) {
		float y1 = LowFireModule.isActive() ? 0.0F : 0.5F;
		quad(builder, pose, sprite, -0.5F, -0.5F, 0.5F, y1, -0.5F);
	}

	@Unique
	private static void quad(VertexConsumer builder, Matrix4f pose, TextureAtlasSprite sprite, float x0, float y0, float x1, float y1, float z) {
		builder.addVertex(pose, x0, y0, z).setUv(sprite.getU1(), sprite.getV1()).setColor(-436207617);
		builder.addVertex(pose, x1, y0, z).setUv(sprite.getU0(), sprite.getV1()).setColor(-436207617);
		builder.addVertex(pose, x1, y1, z).setUv(sprite.getU0(), sprite.getV0()).setColor(-436207617);
		builder.addVertex(pose, x0, y1, z).setUv(sprite.getU1(), sprite.getV0()).setColor(-436207617);
	}
}
