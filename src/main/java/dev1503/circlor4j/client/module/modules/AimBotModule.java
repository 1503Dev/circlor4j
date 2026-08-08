package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AimBotModule extends Module {
	public static final String ID = "aimbot";
	private static final String FOV = "fov";
	private static final String RANGE = "range";
	private static final String SPEED = "speed";
	private static final String AIM_LOCK = "aim_lock";
	private static final String PLAYERS = "players";
	private static final String MOBS = "mobs";
	private static final String INVENTORY = "inventory";
	private static final String RAYCAST = "raycast";

	private static AimBotModule instance;

	private boolean initialized;
	private LivingEntity currentTarget;

	public AimBotModule(StatusManager status) {
		super(status, ID, "Aimbot", "Automatically aims at nearby entities", ModuleCategory.COMBAT);
		this.registerSlider(FOV, "FOV", 15.0, 180.0, 1.0, 60.0);
		this.registerSlider(RANGE, "Range", 3.0, 10.0, 0.1, 4.5);
		this.registerToggle(AIM_LOCK, "AimLock");
		this.registerSlider(SPEED, "Speed", 0.01, 0.25, 0.01, 0.08, ID + "/" + AIM_LOCK + " == 0");
		this.registerToggle(PLAYERS, "Players", true);
		this.registerToggle(MOBS, "Mobs");
		this.registerToggle(INVENTORY, "Inventory");
		this.registerToggle(RAYCAST, "Wall Check", true);
		AimBotModule.instance = this;
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static float getFov() {
		return (float) StatusManager.getInstance().getDouble(ID + "/" + FOV, 60.0);
	}

	public static double getRange() {
		return StatusManager.getInstance().getDouble(ID + "/" + RANGE, 4.5);
	}

	public static float getSpeed() {
		return (float) StatusManager.getInstance().getDouble(ID + "/" + SPEED, 0.3);
	}

	public static boolean isAimLockEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + AIM_LOCK + "/enabled", false);
	}

	public static boolean isPlayersEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + PLAYERS + "/enabled", false);
	}

	public static boolean isMobsEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + MOBS + "/enabled", false);
	}

	public static boolean isInventoryEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + INVENTORY + "/enabled", false);
	}

	public static boolean isRaycastEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + RAYCAST + "/enabled", false);
	}

	@Override
	public void onTick() {
		this.selectTarget();
	}

	private void selectTarget() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return;
		}
		if (!isInventoryEnabled() && mc.gui.screen() != null) {
			this.initialized = false;
			return;
		}
		LivingEntity target = this.findTarget(mc);
 		this.currentTarget = target;
		if (target == null) {
			this.initialized = false;
			return;
		}
		this.initialized = true;
	}

	public static boolean applyAim(double mouseXo, double mouseYo) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return false;
		}
		AimBotModule inst = instance;
		if (inst == null) {
			return false;
		}
		return inst.tickAim(mc, mouseXo, mouseYo);
	}

	private boolean tickAim(Minecraft mc, double mouseXo, double mouseYo) {
		if (this.currentTarget == null || !this.currentTarget.isAlive()) {
			this.initialized = false;
			return false;
		}
		Vec3 targetPos = this.getTargetCenter(this.currentTarget);
		float[] rotations = this.getRotations(mc, targetPos);
		float yawDiff = this.angleDifference(rotations[0], mc.player.getYRot());
		float pitchDiff = rotations[1] - mc.player.getXRot();
		if (Math.abs(yawDiff) > getFov()) {
			this.initialized = false;
			return false;
		}
		if (isAimLockEnabled()) {
			float newYaw = rotations[0];
			float newPitch = rotations[1];
			mc.player.setYRot(newYaw);
			mc.player.setXRot(Math.max(-90.0F, Math.min(90.0F, newPitch)));
			mc.player.yHeadRot = newYaw;
		} else {
			float speed = getSpeed();
			float aimDeltaYaw = yawDiff * speed;
			float aimDeltaPitch = pitchDiff * speed;
			float blendedYaw = mc.player.getYRot() + (float) (mouseXo * 0.15) + aimDeltaYaw;
			float blendedPitch = mc.player.getXRot() + (float) (mouseYo * 0.15) + aimDeltaPitch;
			blendedPitch = Math.max(-90.0F, Math.min(90.0F, blendedPitch));
			mc.player.setYRot(blendedYaw);
			mc.player.setXRot(blendedPitch);
			mc.player.yHeadRot = blendedYaw;
		}
		return true;
	}

	private LivingEntity findTarget(Minecraft mc) {
		double range = getRange();
		double rangeSq = range * range;
		LivingEntity bestTarget = null;
		double bestScore = Double.MAX_VALUE;
		float playerYaw = mc.player.getYRot();
		float playerPitch = mc.player.getXRot();
		for (Entity entity : mc.level.entitiesForRendering()) {
			if (entity == mc.player || entity == mc.player.getVehicle()) {
				continue;
			}
			if (entity instanceof ArmorStand || entity instanceof ItemEntity) {
				continue;
			}
			if (!(entity instanceof LivingEntity living)) {
				continue;
			}
			if (!living.isAlive()) {
				continue;
			}
			if (entity instanceof Player && !isPlayersEnabled()) {
				continue;
			}
			if (!(entity instanceof Player) && !isMobsEnabled()) {
				continue;
			}
			double distSq = mc.player.distanceToSqr(entity);
			if (distSq > rangeSq) {
				continue;
			}
			if (isRaycastEnabled() && !mc.player.hasLineOfSight(entity)) {
				continue;
			}
			Vec3 targetPos = this.getTargetCenter(living);
			float[] rot = this.getRotations(mc, targetPos);
			float yawDiff = Math.abs(this.angleDifference(rot[0], playerYaw));
			float pitchDiff = Math.abs(rot[1] - playerPitch);
			if (yawDiff > getFov()) {
				continue;
			}
			double score = distSq + yawDiff * 2.0 + pitchDiff;
			if (score < bestScore) {
				bestScore = score;
				bestTarget = living;
			}
		}
		return bestTarget;
	}

	private Vec3 getTargetCenter(LivingEntity entity) {
		AABB box = entity.getBoundingBox();
		return new Vec3(
			(box.minX + box.maxX) / 2.0,
			(box.minY + box.maxY) / 2.0,
			(box.minZ + box.maxZ) / 2.0
		);
	}

	private float[] getRotations(Minecraft mc, Vec3 target) {
		Vec3 eyes = mc.player.getEyePosition();
		double dx = target.x - eyes.x;
		double dy = target.y - eyes.y;
		double dz = target.z - eyes.z;
		double dist = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
		return new float[] {this.normalizeAngle(yaw), Math.max(-90.0F, Math.min(90.0F, pitch))};
	}

	private float angleDifference(float a, float b) {
		float diff = (a - b) % 360.0F;
		if (diff > 180.0F) {
			diff -= 360.0F;
		}
		if (diff < -180.0F) {
			diff += 360.0F;
		}
		return diff;
	}

	private float normalizeAngle(float angle) {
		float a = angle % 360.0F;
		if (a > 180.0F) {
			a -= 360.0F;
		}
		if (a < -180.0F) {
			a += 360.0F;
		}
		return a;
	}
}
