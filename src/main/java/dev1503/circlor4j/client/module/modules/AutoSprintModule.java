package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class AutoSprintModule extends Module {
	public AutoSprintModule(StatusManager status) {
		super(status, "auto_sprint", "AutoSprint", "Automatically sprints while moving forward", ModuleCategory.MOVEMENT);
	}

	@Override
	public void onTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if (!player.isSprinting() && !player.isShiftKeyDown() && player.input.hasForwardImpulse()) {
			player.setSprinting(true);
		}
	}
}
