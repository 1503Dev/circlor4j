package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class DerpModule extends Module {
	private float currentYaw = 0.0F;

	public DerpModule(StatusManager status) {
		super(status, "derp", "Derp", "Rotates your player body continuously", ModuleCategory.MISC);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("derp/enabled", false);
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		currentYaw += 30.0F;
		if (currentYaw > 180.0F) {
			currentYaw -= 360.0F;
		}

		mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
			currentYaw, mc.player.getXRot(), mc.player.onGround(), mc.player.horizontalCollision
		));
	}
}
