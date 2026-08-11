package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;

/** Overrides the camera FOV (integer 15-150) via CameraMixin. */
public class ZoomModule extends Module {
	private static final String FOV = "fov";
	private static final String BALANCE_SENSITIVITY = "balance_sensitivity";
	private static final double DEFAULT_FOV = 70.0;

	private double originalSensitivity;

	public ZoomModule(StatusManager status) {
		super(status, "zoom", "Zoom", "Overrides the FOV", ModuleCategory.RENDER);
		this.registerSlider(FOV, "FOV", 15.0, 150.0, 1.0, 30.0);
		this.registerToggle(BALANCE_SENSITIVITY, "Balance Sensitivity", true);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("zoom/enabled", false);
	}

	public static int getFov() {
		return (int) StatusManager.getInstance().getDouble("zoom/fov", 30);
	}

	public static boolean isBalanceSensitivityEnabled() {
		return StatusManager.getInstance().getBoolean("zoom/" + BALANCE_SENSITIVITY + "/enabled", true);
	}

	@Override
	public void onEnable() {
		this.originalSensitivity = Minecraft.getInstance().options.sensitivity().get();
	}

	@Override
	public void onTick() {
		if (!isActive() || !isBalanceSensitivityEnabled()) {
			return;
		}
		double ratio = getFov() / DEFAULT_FOV;
		Minecraft.getInstance().options.sensitivity().set(this.originalSensitivity * ratio);
	}

	@Override
	public void onDisable() {
		Minecraft.getInstance().options.sensitivity().set(this.originalSensitivity);
	}
}
