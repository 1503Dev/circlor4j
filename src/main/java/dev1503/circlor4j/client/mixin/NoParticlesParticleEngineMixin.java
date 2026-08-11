package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoParticlesModule;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class NoParticlesParticleEngineMixin {

	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void cancelCreateParticle(ParticleOptions options, double x, double y, double z, double xa, double ya, double za, CallbackInfoReturnable<Particle> cir) {
		if (NoParticlesModule.isActive()) {
			cir.setReturnValue(null);
		}
	}
}
