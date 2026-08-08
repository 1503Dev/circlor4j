package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoWebModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WebBlock.class)
public abstract class WebBlockMixin {

	@Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
	private void circlor4jNoWeb(
		BlockState state,
		Level level,
		BlockPos pos,
		Entity entity,
		InsideBlockEffectApplier effectApplier,
		boolean isPrecise,
		CallbackInfo ci
	) {
		if (NoWebModule.isActive() && entity == Minecraft.getInstance().player) {
			ci.cancel();
		}
	}
}
