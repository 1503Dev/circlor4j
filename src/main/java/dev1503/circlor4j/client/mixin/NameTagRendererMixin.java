package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NameTagModule;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class NameTagRendererMixin {

	@Inject(method = "extractNameTags", at = @At("RETURN"))
	private void circlor4jHideVanillaNameTag(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
		if (NameTagModule.isActive() && entity instanceof Player) {
			state.nameTag = null;
		}
	}
}
