package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.FreecamModule;
import dev1503.circlor4j.client.module.modules.FreelookModule;
import dev1503.circlor4j.client.module.modules.NoCameraClipModule;
import dev1503.circlor4j.client.module.modules.ZoomModule;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	protected abstract void setPosition(double x, double y, double z);

	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	@Shadow
	private boolean detached;

	@Shadow
	private Entity entity;

	@Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
	private void circlor4jZoom(float partialTicks, CallbackInfoReturnable<Float> cir) {
		if (ZoomModule.isActive()) {
			cir.setReturnValue((float) ZoomModule.getFov());
		}
	}

	@Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", shift = At.Shift.AFTER))
	private void circlor4jCameraRotation(float partialTicks, CallbackInfo ci) {
		if (this.entity == Minecraft.getInstance().player && FreelookModule.isActive()) {
			float[] rot = FreelookModule.getRotation();
			if (rot != null) {
				this.setRotation(rot[0], rot[1]);
			}
		} else if (this.isFreecamCamera()) {
			float[] rot = FreecamModule.getInterpolatedRotation(partialTicks);
			if (rot != null) {
				this.setRotation(rot[0], rot[1]);
			}
		}
	}

	@Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
	private void circlor4jFreecamPosition(float partialTicks, CallbackInfo ci) {
		if (this.isFreecamCamera()) {
			Vec3 pos = FreecamModule.getInterpolatedPosition(partialTicks);
			if (pos != null) {
				this.setPosition(pos.x, pos.y, pos.z);
			}
		}
	}

	@Inject(method = "alignWithEntity", at = @At("TAIL"))
	private void circlor4jFreecamDetached(float partialTicks, CallbackInfo ci) {
		if (this.isFreecamCamera()) {
			this.detached = true;
		}
	}

	@Redirect(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;"))
	private CameraType circlor4jFreecamPerspective(Options options) {
		CameraType original = options.getCameraType();
		return this.isFreecamCamera() ? CameraType.FIRST_PERSON : original;
	}

	@Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
	private void circlor4jNoCameraClip(float cameraDist, CallbackInfoReturnable<Float> cir) {
		if (NoCameraClipModule.isActive()) {
			cir.setReturnValue(cameraDist);
		}
	}

	@Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
	private boolean circlor4jFreecamSmartCull(LocalPlayer player) {
		return player.isSpectator() || FreecamModule.isActive();
	}

	@Unique
	private boolean isFreecamCamera() {
		return FreecamModule.isActive() && this.entity == Minecraft.getInstance().player;
	}
}
