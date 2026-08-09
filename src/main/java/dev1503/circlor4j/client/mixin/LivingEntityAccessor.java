package dev1503.circlor4j.client.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor("noJumpDelay")
	int getNoJumpDelay();

	@Accessor("noJumpDelay")
	void setNoJumpDelay(int delay);
}
