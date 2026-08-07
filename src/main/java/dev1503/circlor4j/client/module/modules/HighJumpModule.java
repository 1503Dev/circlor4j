package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Jump height boost with two modes sharing the path "high_jump/height":
 * <ul>
 *   <li>Velocity - adds extra upward momentum on each jump (see LivingEntityMixin).</li>
 *   <li>Property - overrides the jump strength attribute the game reads.</li>
 * </ul>
 */
public class HighJumpModule extends Module {
	public static final String ID = "high_jump";
	private static final String MODE = "mode";
	private static final String HEIGHT = "height";
	private static final int MODE_VELOCITY = 0;
	private static final int MODE_PROPERTY = 1;
	private static final double BASE_JUMP_STRENGTH = 0.42;
	private double originalJump = BASE_JUMP_STRENGTH;

	public HighJumpModule(StatusManager status) {
		super(status, ID, "HighJump", "Increases jump height", ModuleCategory.MOVEMENT);
		this.registerDropdown(
			MODE,
			"Mode",
			new String[] {"Velocity", "Property"},
			new String[] {"module.speed.mode.velocity.name", "module.speed.mode.property.name"},
			MODE_VELOCITY
		);
		this.registerSlider(HEIGHT, "Height", 1.0, 10.0, 0.2, 2.0);
	}

	@Override
	public void onEnable() {
		LocalPlayer player = Minecraft.getInstance().player;
		AttributeInstance jump = player != null ? player.getAttribute(Attributes.JUMP_STRENGTH) : null;
		if (jump != null) {
			this.originalJump = jump.getBaseValue();
		}
	}

	@Override
	public void onDisable() {
		this.restoreHeight();
	}

	@Override
	public void onTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		int mode = (int) this.getStatus().getDouble(ID + "/" + MODE, MODE_VELOCITY);
		double height = this.getStatus().getDouble(ID + "/" + HEIGHT, 2.0);
		if (mode == MODE_VELOCITY) {
			this.restoreHeight();
		} else {
			AttributeInstance attribute = player.getAttribute(Attributes.JUMP_STRENGTH);
			if (attribute != null) {
				attribute.setBaseValue(BASE_JUMP_STRENGTH * (height));
			}
		}
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	/** Whether the Velocity mode is active (used by LivingEntityMixin's jump boost). */
	public static boolean isVelocityMode() {
		return isActive() && StatusManager.getInstance().getDouble(ID + "/" + MODE, MODE_VELOCITY) < 1.0;
	}

	public static double getHeight() {
		return StatusManager.getInstance().getDouble(ID + "/" + HEIGHT, 2.0);
	}

	private void restoreHeight() {
		LocalPlayer player = Minecraft.getInstance().player;
		AttributeInstance attribute = player != null ? player.getAttribute(Attributes.JUMP_STRENGTH) : null;
		if (attribute != null) {
			attribute.setBaseValue(this.originalJump);
		}
	}
}
