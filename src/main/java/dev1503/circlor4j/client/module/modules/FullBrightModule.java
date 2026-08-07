package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/**
 * Brightness module with two modes:
 * <ul>
 *   <li>NightVision - forces night-vision lightmap intensity without an actual potion effect (no icon/overlay).</li>
 *   <li>Gamma - overrides the lightmap brightness with a configurable gamma value (1-16).</li>
 * </ul>
 * Rendering is applied by {@code LightmapRenderStateExtractorMixin}; this class only owns the settings.
 */
public class FullBrightModule extends Module {
	public static final String ID = "full_bright";
	private static final String MODE = "mode";
	private static final String GAMMA = "gamma";
	private static final int MODE_NIGHT_VISION = 0;
	private static final int MODE_GAMMA = 1;

	public FullBrightModule(StatusManager status) {
		super(status, ID, "FullBright", "Maximises world brightness", ModuleCategory.RENDER);
		this.registerDropdown(
			MODE,
			"Mode",
			new String[] {"NightVision", "Gamma"},
			new String[] {
				"module." + ID + "." + MODE + ".night_vision.name",
				"module." + ID + "." + MODE + ".gamma_override.name"
			},
			MODE_NIGHT_VISION
		);
		this.registerSlider(GAMMA, "Gamma", 1.0, 16.0, 0.5, 16.0, ID + "/" + MODE + " == " + MODE_GAMMA);
	}

	public static boolean isNightVisionActive() {
		StatusManager status = StatusManager.getInstance();
		return status.getBoolean(ID + "/enabled", false) && (int) status.getDouble(ID + "/" + MODE, MODE_NIGHT_VISION) == MODE_NIGHT_VISION;
	}

	public static boolean isGammaActive() {
		StatusManager status = StatusManager.getInstance();
		return status.getBoolean(ID + "/enabled", false) && (int) status.getDouble(ID + "/" + MODE, MODE_NIGHT_VISION) == MODE_GAMMA;
	}

	public static double getGammaValue() {
		return StatusManager.getInstance().getDouble(ID + "/" + GAMMA, 16.0);
	}
}
