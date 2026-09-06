package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.NoParticlesModule;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class NoParticlesParticleEngineMixin {

	// Do NOT cancel createParticle to return null: vanilla callers such as
	// FireworkParticles$Starter.createParticle dereference the result without a
	// null check (sparkParticle.setTrail) and crash. add(Particle) is the single
	// chokepoint every particle must pass through to be ticked or rendered.
	@Inject(method = "add", at = @At("HEAD"), cancellable = true)
	private void cancelAddParticle(Particle particle, CallbackInfo ci) {
		if (NoParticlesModule.isActive()) {
			ci.cancel();
		}
	}
}
