package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.AirJumpModule;
import dev1503.circlor4j.client.module.modules.FreecamModule;
import dev1503.circlor4j.client.module.modules.FreelookModule;
import dev1503.circlor4j.client.module.modules.HitboxModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	/** Feeds the mouse look into the Freecam/Freelook camera state instead of turning the frozen player. */
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	private void circlor4jCameraTurn(double xo, double yo, CallbackInfo ci) {
		if ((Object) this != Minecraft.getInstance().player) {
			return;
		}
		if (FreelookModule.isActive()) {
			FreelookModule.updateRotation(xo * 0.15, yo * 0.15);
			ci.cancel();
		} else if (FreecamModule.isActive()) {
			FreecamModule.updateRotation(xo * 0.15, yo * 0.15);
			ci.cancel();
		}
	}

	/**
	 * AirJump: the local player is reported as always standing on the ground while the module is
	 * active, so the vanilla jump logic keeps firing even when airborne (infinite jumps).
	 */
	@Inject(method = "onGround", at = @At("RETURN"), cancellable = true)
	private void circlor4jAirJumpGround(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this == Minecraft.getInstance().player && AirJumpModule.isActive()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getPickRadius", at = @At("RETURN"), cancellable = true)
	private void circlor4jHitbox(CallbackInfoReturnable<Float> cir) {
		if (!HitboxModule.isActive()) {
			return;
		}
		Entity entity = (Entity) (Object) this;
		if (entity instanceof Player p && p.isLocalPlayer()) {
			return;
		}
		double scale;
		if (entity instanceof Player) {
			scale = HitboxModule.getPlayersHorizon();
		} else if (entity instanceof LivingEntity) {
			scale = HitboxModule.getMobsHorizon();
		} else {
			return;
		}
		if (scale <= 1.0) {
			return;
		}
		float base = cir.getReturnValue();
		float width = entity.getBbWidth();
		float expand = (width / 2.0F) * (float)(scale - 1.0);
		cir.setReturnValue(base + expand);
	}
}
