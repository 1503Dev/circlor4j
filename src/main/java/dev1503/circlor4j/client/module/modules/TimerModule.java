package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class TimerModule extends Module {
	private static final String TPS = "tps";

	public TimerModule(StatusManager status) {
		super(status, "timer", "Timer", "Changes the client tick speed", ModuleCategory.MISC);
		this.registerSlider(TPS, "TPS", 4.0, 100.0, 2.0, 20.0);
	}

	public static float getSpeed() {
		if (!StatusManager.getInstance().getBoolean("timer/enabled", false)) {
			return 1.0F;
		}
		int tps = (int) StatusManager.getInstance().getDouble("timer/" + TPS, 20.0);
		return tps / 20.0F;
	}
}
