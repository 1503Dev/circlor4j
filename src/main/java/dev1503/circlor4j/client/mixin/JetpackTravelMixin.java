package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.JetpackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class JetpackTravelMixin {

	@Inject(method = "travel", at = @At("RETURN"))
	private void onTravelReturn(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof LocalPlayer) || Minecraft.getInstance().player != self) {
			return;
		}
		if (!JetpackModule.isActive()) {
			return;
		}
		self.setDeltaMovement(self.getDeltaMovement().x, self.getDeltaMovement().y + 0.08, self.getDeltaMovement().z);
	}
}
