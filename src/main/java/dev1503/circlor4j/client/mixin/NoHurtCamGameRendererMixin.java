package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoHurtCamModule;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class NoHurtCamGameRendererMixin {

	@Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
	private void onBobHurt(CallbackInfo ci) {
		if (NoHurtCamModule.isActive()) {
			ci.cancel();
		}
	}
}
