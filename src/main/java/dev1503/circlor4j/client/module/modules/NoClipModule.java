package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/** NoClip: allows the local player to fly through blocks (LiquidBounce ModuleNoClip style). */
public class NoClipModule extends Module {
	public static final String ID = "no_clip";
	private static final String SPEED = "speed";

	public NoClipModule(StatusManager status) {
		super(status, ID, "NoClip", "Allows you to fly through blocks", ModuleCategory.PLAYER);
		this.registerSlider(SPEED, "Speed", 0.1, 0.4, 0.01, 0.32);
	}

	@Override
	public void onEnable() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.noPhysics = true;
		}
	}

	@Override
	public void onDisable() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.noPhysics = false;
		}
	}

	@Override
	public void onTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		player.noPhysics = true;
		player.fallDistance = 0.0F;
		player.setOnGround(false);

		double speed = this.getStatus().getDouble(ID + "/" + SPEED, 0.32);
		Minecraft mc = Minecraft.getInstance();
		int forward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
		int strafe = (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0);

		double dx = 0.0;
		double dz = 0.0;
		if (forward != 0 || strafe != 0) {
			float moveYaw = player.getYRot();
			double multiplier = 1.0;
			if (forward < 0) {
				moveYaw += 180.0F;
				multiplier = -0.5;
			} else if (forward > 0) {
				multiplier = 0.5;
			}
			if (strafe < 0) {
				moveYaw -= 90.0F * multiplier;
			} else if (strafe > 0) {
				moveYaw += 90.0F * multiplier;
			}

			double angle = Math.toRadians(moveYaw);
			dx = -Math.sin(angle) * speed;
			dz = Math.cos(angle) * speed;
		}

		double dy = 0.0;
		if (mc.options.keyJump.isDown()) {
			dy += speed;
		}
		if (mc.options.keyShift.isDown()) {
			dy -= speed;
		}

		player.setDeltaMovement(dx, dy, dz);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
