package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class NoParticlesModule extends Module {
	public static final String ID = "no_particles";

	public NoParticlesModule(StatusManager status) {
		super(status, ID, "NoParticles", "Disables all particle effects", ModuleCategory.RENDER);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
