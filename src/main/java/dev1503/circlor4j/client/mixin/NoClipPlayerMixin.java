package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoClipModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NoClip: {@link Player#tick()} resets {@code noPhysics} to {@code isSpectator()} at its start,
 * so we re-apply it right after that reset and before the rest of the tick's physics runs.
 * (Same approach as LiquidBounce's MixinPlayer.hookNoClip.)
 * <p>
 * In singleplayer the integrated {@code ServerPlayer} is a separate entity from the client
 * {@code LocalPlayer}, yet shares the same UUID and JVM. {@code ServerGamePacketListenerImpl}
 * only accepts positions when the server-side player has {@code noPhysics == true}, otherwise it
 * teleports the player back out of the wall. We therefore also flag the matching server entity.
 */
@Mixin(Player.class)
public abstract class NoClipPlayerMixin {

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z",
			ordinal = 1,
			shift = At.Shift.BEFORE
		)
	)
	private void circlor4jNoClip(CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || !NoClipModule.isActive()) {
			return;
		}
		Player self = (Player) (Object) this;
		if (self == mc.player || self.getUUID().equals(mc.player.getUUID())) {
			self.noPhysics = true;
		}
	}
}
