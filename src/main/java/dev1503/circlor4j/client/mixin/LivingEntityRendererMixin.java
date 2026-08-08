package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.TrueSightModule;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState> {

	@Shadow
	public abstract Identifier getTextureLocation(S state);

	@Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
	private void circlor4jTrueSight(S state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing, CallbackInfoReturnable<RenderType> cir) {
		if (TrueSightModule.isEntitiesEnabled() && state.isInvisible && !isBodyVisible && !forceTransparent && !appearGlowing) {
			state.isInvisible = false;
			cir.setReturnValue(RenderTypes.entityTranslucentCullItemTarget(this.getTextureLocation(state)));
		}
	}
}
