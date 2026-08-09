package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.FlyModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class FlyLocalPlayerMixin {

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void onAiStep(CallbackInfo ci) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (!FlyModule.isActive()) {
			if (player.getAbilities().flying || player.getAbilities().mayfly) {
				player.getAbilities().flying = false;
				player.getAbilities().mayfly = false;
			}
			return;
		}
		if (FlyModule.isVanillaFlyMode()) {
			player.getAbilities().flying = true;
		} else if (FlyModule.isVanillaPositionMode()) {
			Minecraft mc = Minecraft.getInstance();
			double speed = 1.08;

			int forward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
			int strafe = (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0);

			float yaw = player.getYRot();
			double dx = 0.0;
			double dz = 0.0;
			if (forward != 0 || strafe != 0) {
				double angle = Math.toRadians(yaw);
				dx = -Math.sin(angle) * forward * speed - Math.cos(angle) * strafe * speed;
				dz = Math.cos(angle) * forward * speed - Math.sin(angle) * strafe * speed;
			}

			double dy = 0.0;
			if (mc.options.keyJump.isDown()) {
				dy += speed;
			}
			if (mc.options.keyShift.isDown()) {
				dy -= speed;
			}

			Vec3 newPos = player.position().add(dx, dy, dz);
			player.setPos(newPos);
			player.setDeltaMovement(Vec3.ZERO);
		}
	}
}
