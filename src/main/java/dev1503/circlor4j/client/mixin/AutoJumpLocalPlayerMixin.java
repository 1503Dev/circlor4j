package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.AutoJumpModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class AutoJumpLocalPlayerMixin {

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void onAiStep(CallbackInfo ci) {
		if (!AutoJumpModule.isActive()) {
			return;
		}
		LocalPlayer self = (LocalPlayer)(Object) this;
		if (!self.onGround()) {
			return;
		}
		if (AutoJumpModule.isMovingEnabled()) {
			Input input = self.input.keyPresses;
			if (!input.forward() && !input.backward() && !input.left() && !input.right()) {
				return;
			}
		}
		self.jumpFromGround();
	}
}
