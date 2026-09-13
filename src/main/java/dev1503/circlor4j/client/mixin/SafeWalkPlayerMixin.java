package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.SafeWalkModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SafeWalk: while enabled, make the local player count as "staying on the ground surface"
 * so vanilla's edge clip ({@link Player#maybeBackOffFromEdge}) zeroes any movement component
 * that would walk off a ledge — the same code path used when actually sneaking.
 */
@Mixin(Player.class)
public abstract class SafeWalkPlayerMixin {

	@Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
	private void circlor4jSafeWalk(CallbackInfoReturnable<Boolean> cir) {
		if (!SafeWalkModule.isActive()) {
			return;
		}
		Player self = (Player) (Object) this;
		if (self == Minecraft.getInstance().player) {
			cir.setReturnValue(true);
		}
	}

	// "Disable when landing possible": raise the edge-clip fall threshold from maxUpStep
	// (0.6) to 1.0 so vanilla's back-off loop lets the player move whenever the footprint
	// has ground support within one block below, and still clips at a real drop.
	@Redirect(
		method = "maybeBackOffFromEdge",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;maxUpStep()F")
	)
	private float circlor4jSafeWalkLandingDepth(Player instance) {
		if (SafeWalkModule.isActive()
			&& SafeWalkModule.isDisableWhenLanding()
			&& (Object) this == Minecraft.getInstance().player) {
			return 1.0F;
		}
		return instance.maxUpStep();
	}
}
