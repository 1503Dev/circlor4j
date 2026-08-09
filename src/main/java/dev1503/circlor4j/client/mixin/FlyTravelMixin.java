package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.FlyModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class FlyTravelMixin {

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void onTravel(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof LocalPlayer) || Minecraft.getInstance().player != self) {
			return;
		}
		if (FlyModule.isActive() && FlyModule.isVanillaPositionMode()) {
			ci.cancel();
		}
	}
}
