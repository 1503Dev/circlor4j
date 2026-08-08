package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class HitboxModule extends Module {
	public static final String ID = "hitbox";
	private static final String MOBS = "mobs";
	private static final String PLAYERS = "players";
	private static final String HORIZON = "horizon";
	private static final String VERTICAL = "vertical";

	public HitboxModule(StatusManager status) {
		super(status, ID, "Hitbox", "Expands entity hitboxes for mobs and players", ModuleCategory.COMBAT);
		this.registerToggle(MOBS, "Mobs", true);
		this.registerSlider(MOBS, HORIZON, "Horizon", 0.5, 3.0, 0.05, 1.0);
		this.registerSlider(MOBS, VERTICAL, "Vertical", 0.5, 3.0, 0.05, 1.0);
		this.registerToggle(PLAYERS, "Players", true);
		this.registerSlider(PLAYERS, HORIZON, "Horizon", 0.5, 3.0, 0.05, 1.0);
		this.registerSlider(PLAYERS, VERTICAL, "Vertical", 0.5, 3.0, 0.05, 1.0);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	private static double getScale(String category, String axis) {
		return StatusManager.getInstance().getDouble(ID + "/" + category + "/" + axis, 1.0);
	}

	public static double getMobsHorizon() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + MOBS + "/enabled", false) ? getScale(MOBS, HORIZON) : 1.0;
	}

	public static double getMobsVertical() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + MOBS + "/enabled", false) ? getScale(MOBS, VERTICAL) : 1.0;
	}

	public static double getPlayersHorizon() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + PLAYERS + "/enabled", false) ? getScale(PLAYERS, HORIZON) : 1.0;
	}

	public static double getPlayersVertical() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + PLAYERS + "/enabled", false) ? getScale(PLAYERS, VERTICAL) : 1.0;
	}
}
