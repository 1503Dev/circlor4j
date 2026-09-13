package dev1503.circlor4j.client.mixin;

import com.mojang.logging.LogUtils;
import dev1503.circlor4j.client.module.modules.AntiPacketKickModule;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.network.Connection;
import net.minecraft.network.SkipPacketException;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

	private static final Logger circlor4j$LOGGER = LogUtils.getLogger();

	@Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
	private void swallowPacketException(ChannelHandlerContext ctx, Throwable cause, CallbackInfo ci) {
		if (cause instanceof TimeoutException || cause instanceof SkipPacketException) {
			return;
		}
		if (AntiPacketKickModule.isActive()) {
			circlor4j$LOGGER.warn("Suppressed connection exception (AntiPacketKick)", cause);
			ci.cancel();
		}
	}
}
