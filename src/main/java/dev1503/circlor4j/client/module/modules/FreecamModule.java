package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Freecam: allows you to move out of your body (LiquidBounce ModuleFreeCam style).
 *
 * The camera position and rotation are tracked independently of the player via a
 * PositionState, updated each tick from WASD/Space/Shift input and from mouse
 * deltas. While active the player's own input and look are frozen (see
 * KeyboardInputMixin / EntityMixin) and the camera is detached at the end of
 * {@code Camera.alignWithEntity} so the player model can be rendered.
 */
public class FreecamModule extends Module {
	public static final String ID = "freecam";
	private static final String SPEED = "speed";
	private static final String HIDE_PLAYER = "hide_player";

	private static boolean available;
	private static Vec3 pos = Vec3.ZERO;
	private static Vec3 lastPos = Vec3.ZERO;
	private static float rotYaw;
	private static float rotPitch;
	private static float lastYaw;
	private static float lastPitch;
	private static long lastRotationTime;

	public FreecamModule(StatusManager status) {
		super(status, ID, "Freecam", "Allows you to move out of your body", ModuleCategory.RENDER);
		this.registerSlider(SPEED, "Speed", 0.5, 3.0, 0.05, 1.5);
		this.registerToggle(HIDE_PLAYER, "Hide Player", false);
	}

	@Override
	public void onEnable() {
		available = true;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			pos = lastPos = player.position().add(0.0, player.getEyeHeight(), 0.0);
			rotYaw = lastYaw = player.getYRot();
			rotPitch = lastPitch = player.getXRot();
		}
	}

	@Override
	public void onDisable() {
		available = false;
		pos = lastPos = Vec3.ZERO;
		rotYaw = rotPitch = lastYaw = lastPitch = 0.0F;
	}

	public static void disable() {
		if (available) {
			StatusManager.getInstance().setValue(ID + "/enabled", 0.0);
			available = false;
			pos = lastPos = Vec3.ZERO;
			rotYaw = rotPitch = lastYaw = lastPitch = 0.0F;
		}
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (!available || player == null || mc.gui.screen() != null) {
			return;
		}

		double speed = this.getSpeed();
		int forward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
		int strafe = (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0);

		double dx = 0.0;
		double dy = 0.0;
		double dz = 0.0;
		if (forward != 0 || strafe != 0) {
			// Movement direction relative to the camera yaw (same math as LiquidBounce's
			// getMovementDirectionOfInput + withStrafe).
			float moveYaw = rotYaw;
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
		if (mc.options.keyJump.isDown()) {
			dy += speed;
		}
		if (mc.options.keyShift.isDown()) {
			dy -= speed;
		}

		// Sync every tick (even when idle) so lastPos/pos stay equal when not moving,
		// otherwise the frame interpolation wobbles the camera while stationary.
		lastPos = pos;
		pos = pos.add(dx, dy, dz);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isHidePlayer() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + HIDE_PLAYER + "/enabled", false);
	}

	/** Called by EntityMixin from {@code Entity.turn}; keeps the freecam look separate from the player. */
	public static void updateRotation(double yawDelta, double pitchDelta) {
		if (!available) {
			return;
		}
		lastYaw = rotYaw;
		lastPitch = rotPitch;
		rotYaw += (float) yawDelta;
		rotPitch = Mth.clamp(rotPitch + (float) pitchDelta, -90.0F, 90.0F);
		lastRotationTime = System.currentTimeMillis();
	}

	/** Interpolated camera position for the current render frame, or null when freecam is off. */
	public static Vec3 getInterpolatedPosition(float partialTicks) {
		if (!available) {
			return null;
		}
		return lastPos.lerp(pos, partialTicks);
	}

	/**
	 * Interpolated camera rotation {yaw, pitch} for the current render frame, or null when freecam is off.
	 * When the mouse has been idle for more than a few frames (e.g. after opening a GUI mid-move) the
	 * last/live rotation pair is left desynced and the frame interpolation would shake the camera, so the
	 * live target is returned directly instead.
	 */
	public static float[] getInterpolatedRotation(float partialTicks) {
		if (!available) {
			return null;
		}
		if (System.currentTimeMillis() - lastRotationTime > 50L) {
			return new float[] { rotYaw, rotPitch };
		}
		return new float[] { lerpAngle(partialTicks, lastYaw, rotYaw), Mth.lerp(partialTicks, lastPitch, rotPitch) };
	}

	private static float lerpAngle(float delta, float from, float to) {
		float diff = ((to - from + 180.0F) % 360.0F + 360.0F) % 360.0F - 180.0F;
		return from + diff * delta;
	}

	private double getSpeed() {
		return this.getStatus().getDouble(ID + "/" + SPEED, 1.5);
	}
}
