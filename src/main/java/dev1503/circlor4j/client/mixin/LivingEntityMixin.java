package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.AirJumpModule;
import dev1503.circlor4j.client.module.modules.HighJumpModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	/** HighJump Velocity: after a jump, add extra upward momentum scaled by the height slider. */
	@Inject(method = "jumpFromGround", at = @At("RETURN"))
	private void circlor4jHighJump(CallbackInfo ci) {
		if (!HighJumpModule.isVelocityMode()) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		if (self != Minecraft.getInstance().player) {
			return;
		}
		float jumpPower = (float) self.getAttributeValue(Attributes.JUMP_STRENGTH);
		double height = HighJumpModule.getHeight();
		Vec3 movement = self.getDeltaMovement();
		self.setDeltaMovement(movement.x, movement.y + jumpPower * (height - 1), movement.z);
	}
}
