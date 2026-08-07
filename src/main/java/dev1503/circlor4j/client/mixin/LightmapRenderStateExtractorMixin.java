package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.FullBrightModule;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {

	@Inject(method = "extract", at = @At("TAIL"))
	private void circlor4jFullBright(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
		if (FullBrightModule.isNightVisionActive()) {
			renderState.nightVisionEffectIntensity = 1.0F;
		}
		if (FullBrightModule.isGammaActive()) {
			renderState.brightness = (float) FullBrightModule.getGammaValue();
		}
	}
}
