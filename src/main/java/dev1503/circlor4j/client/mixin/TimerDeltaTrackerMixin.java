package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.TimerModule;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
public abstract class TimerDeltaTrackerMixin {

	@Shadow
	private float deltaTicks;

	@Inject(method = "advanceGameTime", at = @At(value = "FIELD", target = "Lnet/minecraft/client/DeltaTracker$Timer;deltaTicks:F", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
	private void onAdvanceGameTime(long currentMs, CallbackInfoReturnable<Integer> cir) {
		float speed = TimerModule.getSpeed();
		if (speed != 1.0F) {
			this.deltaTicks *= speed;
		}
	}
}
