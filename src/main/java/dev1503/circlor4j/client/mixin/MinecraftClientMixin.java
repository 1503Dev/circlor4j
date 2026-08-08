package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.config.Persistence;
import dev1503.circlor4j.client.module.ModuleManager;
import dev1503.circlor4j.client.module.modules.FreecamModule;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	private void circlor4jOnTick(CallbackInfo ci) {
		Persistence.ensureLoaded();
		ModuleManager.tick();
	}

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void circlor4jFreecamAttack(CallbackInfoReturnable<Boolean> cir) {
		if (FreecamModule.isActive()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void circlor4jFreecamContinueAttack(boolean down, CallbackInfo ci) {
		if (FreecamModule.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void circlor4jFreecamUseItem(CallbackInfo ci) {
		if (FreecamModule.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	private void circlor4jWorldChange(net.minecraft.client.multiplayer.ClientLevel level, CallbackInfo ci) {
		if (FreecamModule.isActive()) {
			FreecamModule.disable();
		}
	}
}
