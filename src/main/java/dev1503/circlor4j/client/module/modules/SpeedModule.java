package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Movement speed boost with two modes sharing the path "speed/speed":
 * <ul>
 *   <li>Velocity - adds extra momentum to the player's horizontal velocity while moving.</li>
 *   <li>Property - overrides the movement speed attribute the game reads.</li>
 * </ul>
 */
public class SpeedModule extends Module {
	private static final String MODE = "mode";
	private static final String SPEED = "speed";
	private static final int MODE_VELOCITY = 0;
	private static final int MODE_PROPERTY = 1;
	private double originalSpeed = 0.1;

	public SpeedModule(StatusManager status) {
		super(status, "speed", "Speed", "Increases movement speed", ModuleCategory.MOVEMENT);
		this.registerDropdown(
			MODE,
			"Mode",
			new String[] {"Velocity", "Property"},
			new String[] {"module.speed.mode.velocity.name", "module.speed.mode.property.name"},
			MODE_VELOCITY
		);
		this.registerSlider(SPEED, "Speed", 1.0, 10.0, 0.2, 5.0);
	}

	@Override
	public void onEnable() {
		LocalPlayer player = Minecraft.getInstance().player;
		AttributeInstance speed = player != null ? player.getAttribute(Attributes.MOVEMENT_SPEED) : null;
		if (speed != null) {
			this.originalSpeed = speed.getBaseValue();
		}
	}

	@Override
	public void onDisable() {
		this.restoreSpeed();
	}

	@Override
	public void onTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		int mode = (int) this.getStatus().getDouble("speed/mode", MODE_VELOCITY);
		double speed = this.getStatus().getDouble("speed/speed", 1.0);
		if (mode == MODE_VELOCITY) {
			this.applyVelocity(player, speed);
			this.restoreSpeed();
		} else {
			AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
			if (attribute != null) {
				attribute.setBaseValue(speed / 7.8);
			}
		}
	}

	/**
	 * While the player has WASD input, sets the horizontal momentum to speed/5 in the actual
	 * input direction (computed from xxa/zza and yaw), not the game's current movement direction.
	 */
	private void applyVelocity(LocalPlayer player, double speed) {
		Vec2 move = player.input.getMoveVector();
		if (player.isSpectator() || move.lengthSquared() <= 0.0F) {
			return;
		}
		float yawRad = player.getYRot() * (float) (Math.PI / 180.0);
		double sin = Math.sin(yawRad);
		double cos = Math.cos(yawRad);
		double dirX = move.x * cos - move.y * sin;
		double dirZ = move.y * cos + move.x * sin;
		double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
		if (length <= 1.0E-4) {
			return;
		}
		double target = speed / 4.8;
		Vec3 delta = player.getDeltaMovement();
		player.setDeltaMovement(dirX / length * target, delta.y, dirZ / length * target);
	}

	private void restoreSpeed() {
		LocalPlayer player = Minecraft.getInstance().player;
		AttributeInstance attribute = player != null ? player.getAttribute(Attributes.MOVEMENT_SPEED) : null;
		if (attribute != null) {
			attribute.setBaseValue(this.originalSpeed);
		}
	}
}
