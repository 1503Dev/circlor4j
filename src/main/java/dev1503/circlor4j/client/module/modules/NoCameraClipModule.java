package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/** NoCameraClip: disables third-person camera clipping (TrollHack style, via CameraMixin). */
public class NoCameraClipModule extends Module {
	public static final String ID = "no_camera_clip";

	public NoCameraClipModule(StatusManager status) {
		super(status, ID, "NoCameraClip", "Disables third-person camera clipping", ModuleCategory.RENDER);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
