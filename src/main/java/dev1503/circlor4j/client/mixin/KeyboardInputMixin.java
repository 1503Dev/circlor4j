package dev1503.circlor4j.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev1503.circlor4j.client.module.modules.FreecamModule;
import dev1503.circlor4j.client.module.modules.InventoryMoveModule;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

	@Inject(method = "tick", at = @At("TAIL"))
	private void circlor4jInventoryMove(CallbackInfo ci) {
		if (!InventoryMoveModule.isActive()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || !(mc.gui.screen() instanceof AbstractContainerScreen)) {
			return;
		}
		InputConstants.Key up = ((KeyMappingAccessor) (Object) mc.options.keyUp).circlor4jKey();
		InputConstants.Key down = ((KeyMappingAccessor) (Object) mc.options.keyDown).circlor4jKey();
		InputConstants.Key left = ((KeyMappingAccessor) (Object) mc.options.keyLeft).circlor4jKey();
		InputConstants.Key right = ((KeyMappingAccessor) (Object) mc.options.keyRight).circlor4jKey();
		InputConstants.Key jump = ((KeyMappingAccessor) (Object) mc.options.keyJump).circlor4jKey();
		InputConstants.Key shift = ((KeyMappingAccessor) (Object) mc.options.keyShift).circlor4jKey();
		InputConstants.Key sprint = ((KeyMappingAccessor) (Object) mc.options.keySprint).circlor4jKey();
		this.keyPresses = new Input(
			isKeyDown(mc, up),
			isKeyDown(mc, down),
			isKeyDown(mc, left),
			isKeyDown(mc, right),
			isKeyDown(mc, jump),
			isKeyDown(mc, shift),
			isKeyDown(mc, sprint)
		);
		float forwardImpulse = impulse(this.keyPresses.forward(), this.keyPresses.backward());
		float leftImpulse = impulse(this.keyPresses.left(), this.keyPresses.right());
		this.moveVector = new Vec2(leftImpulse, forwardImpulse).normalized();
	}

	private static boolean isKeyDown(Minecraft mc, InputConstants.Key key) {
		return InputConstants.isKeyDown(mc.getWindow(), key.getValue());
	}

	private static float impulse(boolean positive, boolean negative) {
		if (positive == negative) {
			return 0.0F;
		}
		return positive ? 1.0F : -1.0F;
	}
}
