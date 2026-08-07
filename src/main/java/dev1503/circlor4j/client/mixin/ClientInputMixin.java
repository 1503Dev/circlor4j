package dev1503.circlor4j.client.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientInput.class)
public abstract class ClientInputMixin {

	@Shadow
	public Input keyPresses;

	@Shadow
	protected Vec2 moveVector;
}
