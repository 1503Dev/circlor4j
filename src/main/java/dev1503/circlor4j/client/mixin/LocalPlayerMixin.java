package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.FastStopModule;
import dev1503.circlor4j.client.module.modules.NoFallModule;
import dev1503.circlor4j.client.module.modules.NoSlowDownModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void circlor4jNoFall(CallbackInfo ci) {
		if (NoFallModule.isActive()) {
			((LocalPlayer) (Object) this).fallDistance = 0.0;
		}
	}

	@Redirect(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal = 1))
	private Vec2 circlor4jNoSlowDownItemUse(Vec2 instance, float factor) {
		if (NoSlowDownModule.isActive()) {
			return instance;
		}
		return instance.scale(factor);
	}

	@Redirect(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal = 2))
	private Vec2 circlor4jNoSlowDownSneak(Vec2 instance, float factor) {
		if (NoSlowDownModule.isActive()) {
			return instance;
		}
		return instance.scale(factor);
	}

	@Inject(method = "isSlowDueToUsingItem", at = @At("RETURN"), cancellable = true)
	private void circlor4jNoSlowDownSprint(CallbackInfoReturnable<Boolean> cir) {
		if (NoSlowDownModule.isActive()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void circlor4jFastStop(CallbackInfo ci) {
		if (!FastStopModule.isActive()) {
			return;
		}
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (player.isInWater() || player.isInLava()) {
			return;
		}
		if (player.input.keyPresses.forward() || player.input.keyPresses.backward()
			|| player.input.keyPresses.left() || player.input.keyPresses.right()) {
			return;
		}
		player.setDeltaMovement(0.0, player.getDeltaMovement().y, 0.0);
	}
}
