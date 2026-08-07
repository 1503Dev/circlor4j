package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * Freelook: temporarily switches to third-person and lets the mouse move the camera
 * without rotating the player's head/body. The camera orbits the frozen player.
 */
public class FreelookModule extends Module {
	public static final String ID = "freelook";

	private static CameraType savedCameraType;
	private static boolean available;
	private static float rotYaw;
	private static float rotPitch;

	public FreelookModule(StatusManager status) {
		super(status, ID, "Freelook", "Look around without turning your player", ModuleCategory.RENDER);
	}

	@Override
	public void onEnable() {
		Minecraft mc = Minecraft.getInstance();
		savedCameraType = mc.options.getCameraType();
		mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		LocalPlayer player = mc.player;
		if (player != null) {
			rotYaw = player.getYRot();
			rotPitch = player.getXRot();
		}
		available = true;
	}

	@Override
	public void onDisable() {
		available = false;
		Minecraft mc = Minecraft.getInstance();
		if (savedCameraType != null) {
			mc.options.setCameraType(savedCameraType);
			savedCameraType = null;
		}
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	/** Called by EntityMixin from {@code Entity.turn}; updates the camera look while the player stays still. */
	public static void updateRotation(double yawDelta, double pitchDelta) {
		if (!available) {
			return;
		}
		rotYaw += (float) yawDelta;
		rotPitch = Mth.clamp(rotPitch + (float) pitchDelta, -90.0F, 90.0F);
	}

	public static float[] getRotation() {
		if (!available) {
			return null;
		}
		return new float[] { rotYaw, rotPitch };
	}
}
