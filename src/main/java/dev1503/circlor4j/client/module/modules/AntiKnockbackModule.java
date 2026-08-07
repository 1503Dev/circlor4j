package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;

/** AntiKnockback: cancels the velocity packet applied to the player (see ClientPacketListenerMixin). */
public class AntiKnockbackModule extends Module {
	public static final String ID = "anti_knockback";

	public AntiKnockbackModule(StatusManager status) {
		super(status, ID, "AntiKnockback", "Prevents knockback from attacks", ModuleCategory.COMBAT);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}
}
