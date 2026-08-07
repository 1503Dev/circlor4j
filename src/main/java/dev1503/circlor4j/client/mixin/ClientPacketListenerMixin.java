package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.AntiKnockbackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** AntiKnockback: cancels the velocity packet that would knock the local player back. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

	@Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
	private void circlor4jAntiKnockback(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
		if (AntiKnockbackModule.isActive()
			&& Minecraft.getInstance().player != null
			&& packet.id() == Minecraft.getInstance().player.getId()) {
			ci.cancel();
		}
	}
}
