package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoFogModule;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * NoFog: {@code GameRenderer.renderLevel()} binds the WORLD fog buffer (render-distance, water,
 * lava, blindness and darkness fog all end up here). Swapping the mode to NONE makes
 * {@link FogRenderer#getBuffer} return the empty buffer, so the shaders render without any fog.
 */
@Mixin(GameRenderer.class)
public abstract class NoFogGameRendererMixin {

	@ModifyArg(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/fog/FogRenderer;getBuffer(Lnet/minecraft/client/renderer/fog/FogRenderer$FogMode;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;",
			ordinal = 0
		),
		index = 0
	)
	private FogRenderer.FogMode circlor4jNoFog(FogRenderer.FogMode mode) {
		if (NoFogModule.isActive()) {
			return FogRenderer.FogMode.NONE;
		}
		return mode;
	}
}
