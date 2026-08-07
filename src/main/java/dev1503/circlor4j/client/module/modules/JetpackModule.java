package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/** Jetpack: while enabled, applies momentum in the direction you are looking. */
public class JetpackModule extends Module {
	public JetpackModule(StatusManager status) {
		super(status, "jetpack", "Jetpack", "Applies momentum in the direction you are looking", ModuleCategory.MOVEMENT);
	}

	@Override
	public void onTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		Vec3 movement = player.getDeltaMovement();
		Vec3 look = player.getLookAngle();
		player.setDeltaMovement(movement.add(look.scale(0.15)));
	}
}
