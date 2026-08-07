package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.FreecamModule;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the local player stationary while Freecam is active (LiquidBounce MovementInputEvent cancel). */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInputMixin {

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void circlor4jFreecamInput(CallbackInfo ci) {
		if (FreecamModule.isActive()) {
			this.keyPresses = Input.EMPTY;
			this.moveVector = Vec2.ZERO;
			ci.cancel();
		}
	}
}
