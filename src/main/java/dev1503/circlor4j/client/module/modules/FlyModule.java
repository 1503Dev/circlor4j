package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

public class FlyModule extends Module {
	public static final String ID = "fly";
	private static final String MODE = "mode";
	private static final int MODE_VANILLA = 0;
	private static final int MODE_VANILLA_POSITION = 1;

	public FlyModule(StatusManager status) {
		super(status, ID, "Fly", "Enables flight", ModuleCategory.MOVEMENT);
		this.registerDropdown(
			MODE,
			"Mode",
			new String[] {"VanillaFly", "VanillaPosition"},
			new String[] {"module.fly.mode.vanilla.name", "module.fly.mode.vanilla_position.name"},
			MODE_VANILLA
		);
	}

	@Override
	public void onDisable() {
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		if (mc.player != null) {
			mc.player.getAbilities().flying = false;
			mc.player.getAbilities().mayfly = false;
		}
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isVanillaFlyMode() {
		return (int) StatusManager.getInstance().getDouble(ID + "/" + MODE, MODE_VANILLA) == MODE_VANILLA;
	}

	public static boolean isVanillaPositionMode() {
		return (int) StatusManager.getInstance().getDouble(ID + "/" + MODE, MODE_VANILLA) == MODE_VANILLA_POSITION;
	}
}
