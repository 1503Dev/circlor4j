package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoParticlesModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class NoParticlesClientLevelMixin {

	@Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void cancelParticle(ParticleOptions particle, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
		if (NoParticlesModule.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void cancelParticleWithFlags(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
		if (NoParticlesModule.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void cancelAlwaysVisibleParticle(ParticleOptions particle, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
		if (NoParticlesModule.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void cancelAlwaysVisibleParticleWithFlag(ParticleOptions particle, boolean overrideLimiter, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
		if (NoParticlesModule.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
	private void cancelDestroyBlockEffect(BlockPos pos, BlockState blockState, CallbackInfo ci) {
		if (NoParticlesModule.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
	private void cancelBreakingBlockEffect(BlockPos pos, Direction direction, CallbackInfo ci) {
		if (NoParticlesModule.isActive()) {
			ci.cancel();
		}
	}
}
