package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.AntiDebuffModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class AntiDebuffLocalPlayerMixin {

	@Inject(method = "getEffectBlendFactor", at = @At("HEAD"), cancellable = true)
	private void onGetEffectBlendFactor(Holder<MobEffect> effect, float partialTicks, CallbackInfoReturnable<Float> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof LocalPlayer) || Minecraft.getInstance().player != self) {
			return;
		}
		if (AntiDebuffModule.isActive() && (effect == MobEffects.NAUSEA || effect == MobEffects.DARKNESS)) {
			cir.setReturnValue(0.0F);
		}
	}

	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	private void onHasEffect(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof LocalPlayer) || Minecraft.getInstance().player != self) {
			return;
		}
		if (AntiDebuffModule.isActive() && effect == MobEffects.BLINDNESS) {
			cir.setReturnValue(false);
		}
	}
}
