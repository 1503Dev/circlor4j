package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoJumpDelayModule;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class NoJumpDelayLocalPlayerMixin {

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void onAiStep(CallbackInfo ci) {
		if (NoJumpDelayModule.isActive()) {
			((LivingEntityAccessor) this).setNoJumpDelay(0);
		}
	}
}
