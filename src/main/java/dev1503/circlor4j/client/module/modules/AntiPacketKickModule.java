package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class AntiPacketKickModule extends Module {
	public static final String ID = "anti_packet_kick";

	public AntiPacketKickModule(StatusManager status) {
		super(status, ID, "AntiPacketKick", "Prevents disconnection caused by packet handling errors", ModuleCategory.MISC);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
