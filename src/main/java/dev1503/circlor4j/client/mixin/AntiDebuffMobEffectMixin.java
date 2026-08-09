package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.AntiDebuffModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public abstract class AntiDebuffMobEffectMixin {

	@Inject(method = "getBlendFactor", at = @At("HEAD"), cancellable = true)
	private void onGetBlendFactor(LivingEntity livingEntity, float partialTickTime, CallbackInfoReturnable<Float> cir) {
		if (!(livingEntity instanceof LocalPlayer) || Minecraft.getInstance().player != livingEntity) {
			return;
		}
		if (AntiDebuffModule.isActive() && ((MobEffectInstance) (Object) this).getEffect() == MobEffects.DARKNESS) {
			cir.setReturnValue(0.0F);
		}
	}
}
