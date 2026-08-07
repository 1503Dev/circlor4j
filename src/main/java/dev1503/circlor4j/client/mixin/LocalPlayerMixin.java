package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoFallModule;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void circlor4jNoFall(CallbackInfo ci) {
		if (NoFallModule.isActive()) {
			((LocalPlayer) (Object) this).fallDistance = 0.0;
		}
	}
}
