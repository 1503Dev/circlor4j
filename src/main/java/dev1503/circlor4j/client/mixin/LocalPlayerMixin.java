package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.FastStopModule;
import dev1503.circlor4j.client.module.modules.JetpackModule;
import dev1503.circlor4j.client.module.modules.NoFallModule;
import dev1503.circlor4j.client.module.modules.NoSlowDownModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void circlor4jNoFall(CallbackInfo ci) {
		if (NoFallModule.isActive()) {
			((LocalPlayer) (Object) this).fallDistance = 0.0;
		}
	}

	// Both redirects hook a unique call in modifyInput rather than Vec2.scale with an ordinal:
	// ViaFabricPlus redirects Vec2.scale(..., ordinal = 0) there too, and consuming that call
	// first would shift every later ordinal (and make ours fail if it applies first).
	@Redirect(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
	private boolean circlor4jNoSlowDownItemUse(LocalPlayer instance) {
		if (NoSlowDownModule.isActive()) {
			return false;
		}
		return instance.isUsingItem();
	}

	@Redirect(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
	private double circlor4jNoSlowDownSneak(LocalPlayer instance, Holder<Attribute> attribute) {
		if (NoSlowDownModule.isActive()) {
			return 1.0;
		}
		return instance.getAttributeValue(attribute);
	}

	@Inject(method = "isSlowDueToUsingItem", at = @At("RETURN"), cancellable = true)
	private void circlor4jNoSlowDownSprint(CallbackInfoReturnable<Boolean> cir) {
		if (NoSlowDownModule.isActive()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void circlor4jFastStop(CallbackInfo ci) {
		if (!FastStopModule.isActive()) {
			return;
		}
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (player.isInWater() || player.isInLava()) {
			return;
		}
		if (JetpackModule.isActive()) {
			return;
		}
		if (player.input.keyPresses.forward() || player.input.keyPresses.backward()
			|| player.input.keyPresses.left() || player.input.keyPresses.right()) {
			return;
		}
		player.setDeltaMovement(0.0, player.getDeltaMovement().y, 0.0);
	}
}
