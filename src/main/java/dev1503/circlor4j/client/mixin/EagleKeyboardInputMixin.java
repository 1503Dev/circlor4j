package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.EagleModule;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class EagleKeyboardInputMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	private void onTick(CallbackInfo ci) {
		if (!EagleModule.isActive()) {
			return;
		}
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		boolean originalSneak = mc.options.keyShift.isDown();
		boolean conditionsMet = mc.player.onGround();
		boolean isActive = EagleModule.shouldActivateEagle(mc.player);

		EagleModule.updateSneakCapture(originalSneak, isActive);
		boolean controlsSneak = EagleModule.shouldOverrideSneak(conditionsMet, isActive);

		boolean newSneak = controlsSneak ? isActive : (originalSneak || isActive);

		if (newSneak != clientInput().keyPresses.shift()) {
			Input current = clientInput().keyPresses;
			clientInput().keyPresses = new Input(
				current.forward(), current.backward(),
				current.left(), current.right(),
				current.jump(), newSneak, current.sprint()
			);
		}

		EagleModule.updateSneakState(newSneak);
	}

	private ClientInput clientInput() {
		return (ClientInput)(Object) this;
	}
}
