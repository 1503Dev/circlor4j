package dev1503.circlor4j.client.mixin;

import com.mojang.logging.LogUtils;
import dev1503.circlor4j.client.module.modules.AntiPacketKickModule;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.ProtocolInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PacketDecoder.class)
public abstract class PacketDecoderMixin {

	private static final Logger circlor4j$LOGGER = LogUtils.getLogger();

	@Shadow
	@Final
	private ProtocolInfo<?> protocolInfo;

	// Probe-decode the current frame first: on failure, drain the rest of the frame
	// (mirroring the SkipPacketException path) so the stream stays in sync, and drop
	// the packet instead of letting the exception reach Connection.exceptionCaught
	// and disconnect us. On success, rewind the reader index and let vanilla decode.
	@Inject(method = "decode", at = @At("HEAD"), cancellable = true)
	private void circlor4j$probeDecode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out, CallbackInfo ci) {
		if (!AntiPacketKickModule.isActive()) {
			return;
		}

		int readerIndex = input.readerIndex();
		try {
			this.protocolInfo.codec().decode(input);
		} catch (Exception e) {
			try {
				if (input.refCnt() > 0) {
					input.skipBytes(input.readableBytes());
				}
			} catch (Exception ignored) {
			}
			circlor4j$LOGGER.warn("Dropped malformed packet (AntiPacketKick)", e);
			ci.cancel();
			return;
		}

		input.readerIndex(readerIndex);
	}
}
