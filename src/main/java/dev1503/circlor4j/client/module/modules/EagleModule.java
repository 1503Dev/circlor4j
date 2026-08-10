package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class EagleModule extends Module {
	public static final String ID = "eagle";
	private static final String EDGE_DISTANCE = "edge_distance";
	private static final String CURRENT_EDGE_DISTANCE = "current_edge_distance";
	private static final String WAS_SNEAKING = "was_sneaking";
	private static final String SNEAK_CAPTURED = "sneak_captured";
	private static final String CONDITIONAL = "conditional";
	private static final String ON_GROUND = "on_ground";
	private static final String SNEAK = "sneak";
	private static final String LEFT = "left";
	private static final String RIGHT = "right";
	private static final String FORWARDS = "forwards";
	private static final String BACKWARDS = "backwards";
	private static final String HOLDING_BLOCKS = "holding_blocks";
	private static final String PITCH = "pitch";

	private static final Random RANDOM = new Random();

	public EagleModule(StatusManager status) {
		super(status, ID, "Eagle", "Automatically sneaks at block edges", ModuleCategory.PLAYER);
		this.registerRangeSlider(EDGE_DISTANCE, "Edge Distance", 0.01, 1.3, 0.01, 0.4, 0.6);
		this.registerToggle(CONDITIONAL, "Conditional", true);
		this.registerToggleIn(CONDITIONAL, ON_GROUND, "On Ground", true);
		this.registerToggleIn(CONDITIONAL, SNEAK, "Sneak", false);
		this.registerToggleIn(CONDITIONAL, LEFT, "Left", false);
		this.registerToggleIn(CONDITIONAL, RIGHT, "Right", false);
		this.registerToggleIn(CONDITIONAL, FORWARDS, "Forwards", false);
		this.registerToggleIn(CONDITIONAL, BACKWARDS, "Backwards", false);
		this.registerToggleIn(CONDITIONAL, HOLDING_BLOCKS, "Holding Blocks", false);
		this.registerRangeSlider(PITCH, "Pitch", -90.0, 90.0, 1.0, -90.0, 90.0);
		StatusManager.getInstance().setValueOnly(ID + "/" + CURRENT_EDGE_DISTANCE, getRandomEdgeDistance());
		StatusManager.getInstance().setValueOnly(ID + "/" + WAS_SNEAKING, 0.0);
		StatusManager.getInstance().setValueOnly(ID + "/" + SNEAK_CAPTURED, 0.0);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isConditionalEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + CONDITIONAL + "/enabled", true);
	}

	/** Whether the module takes full control of the sneak key (Conditional enabled and the Sneak condition active). */
	public static boolean isControlsSneak() {
		return isConditionalEnabled() && isSneakConditionEnabled();
	}

	public static boolean isOnGroundConditionEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + CONDITIONAL + "/" + ON_GROUND + "/enabled", true);
	}

	public static boolean isSneakConditionEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + CONDITIONAL + "/" + SNEAK + "/enabled", false);
	}

	public static boolean isHoldingBlocksConditionEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + CONDITIONAL + "/" + HOLDING_BLOCKS + "/enabled", false);
	}

	public static boolean isLeftConditionEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + CONDITIONAL + "/" + LEFT + "/enabled", false);
	}

	public static boolean isRightConditionEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + CONDITIONAL + "/" + RIGHT + "/enabled", false);
	}

	public static boolean isForwardsConditionEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + CONDITIONAL + "/" + FORWARDS + "/enabled", false);
	}

	public static boolean isBackwardsConditionEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + CONDITIONAL + "/" + BACKWARDS + "/enabled", false);
	}

	private static float getPitchMin() {
		return StatusManager.getInstance().getFloat(ID + "/" + PITCH + "/min", -90.0F);
	}

	private static float getPitchMax() {
		return StatusManager.getInstance().getFloat(ID + "/" + PITCH + "/max", 90.0F);
	}

	private static float getRandomEdgeDistance() {
		float min = StatusManager.getInstance().getFloat(ID + "/" + EDGE_DISTANCE + "/min", 0.4F);
		float max = StatusManager.getInstance().getFloat(ID + "/" + EDGE_DISTANCE + "/max", 0.6F);
		if (max < min) {
			return min;
		}
		return min + RANDOM.nextFloat() * (max - min);
	}

	public static float getCurrentEdgeDistance() {
		return StatusManager.getInstance().getFloat(ID + "/" + CURRENT_EDGE_DISTANCE, 0.5F);
	}

	public static void setCurrentEdgeDistance(float value) {
		StatusManager.getInstance().setValueOnly(ID + "/" + CURRENT_EDGE_DISTANCE, value);
	}

	public static boolean wasSneaking() {
		return StatusManager.getInstance().getBoolean(ID + "/" + WAS_SNEAKING, false);
	}

	public static void setWasSneaking(boolean value) {
		StatusManager.getInstance().setValueOnly(ID + "/" + WAS_SNEAKING, value ? 1.0 : 0.0);
	}

	public static boolean isSneakCaptured() {
		return StatusManager.getInstance().getBoolean(ID + "/" + SNEAK_CAPTURED, false);
	}

	public static void setSneakCaptured(boolean value) {
		StatusManager.getInstance().setValueOnly(ID + "/" + SNEAK_CAPTURED, value ? 1.0 : 0.0);
	}

	public static boolean shouldActivateEagle(net.minecraft.world.entity.player.Player player, boolean conditionsMet, boolean forward, boolean backward, boolean left, boolean right) {
		if (player.getAbilities().flying || !conditionsMet) {
			return false;
		}
		return isCloseToEdge(player, forward, backward, left, right);
	}

	public static boolean isCloseToEdge(net.minecraft.world.entity.player.Player player, boolean forward, boolean backward, boolean left, boolean right) {
		Vec3 pos = player.position();
		float edgeDistance = getCurrentEdgeDistance();

		BlockPos below = BlockPos.containing(pos.x, pos.y - 0.1, pos.z);
		if (!player.level().getBlockState(below).isSolidRender()) {
			return true;
		}

		int forwardInput = (forward ? 1 : 0) - (backward ? 1 : 0);
		int strafeInput = (right ? 1 : 0) - (left ? 1 : 0);
		if (forwardInput == 0 && strafeInput == 0) {
			return false;
		}

		double angle = Math.toRadians(player.getYRot());
		double dx = -Math.sin(angle) * forwardInput - Math.cos(angle) * strafeInput;
		double dz = Math.cos(angle) * forwardInput - Math.sin(angle) * strafeInput;
		double length = Math.hypot(dx, dz);
		if (length < 1.0E-6) {
			return false;
		}
		dx /= length;
		dz /= length;

		double distToBoundary = Double.MAX_VALUE;
		if (Math.abs(dx) > 1.0E-6) {
			double boundaryX = dx > 0 ? below.getX() + 1.0 : below.getX();
			distToBoundary = Math.min(distToBoundary, (boundaryX - pos.x) / dx);
		}
		if (Math.abs(dz) > 1.0E-6) {
			double boundaryZ = dz > 0 ? below.getZ() + 1.0 : below.getZ();
			distToBoundary = Math.min(distToBoundary, (boundaryZ - pos.z) / dz);
		}
		if (distToBoundary < 0.0 || distToBoundary > edgeDistance) {
			return false;
		}

		double probeX = pos.x + dx * (distToBoundary + 0.05);
		double probeZ = pos.z + dz * (distToBoundary + 0.05);
		BlockPos ahead = BlockPos.containing(probeX, pos.y - 0.1, probeZ);
		if (player.level().getBlockState(ahead).isSolidRender()) {
			return false;
		}
		return !player.level().getBlockState(ahead.below()).isSolidRender();
	}

	public static void updateSneakCapture(boolean originalSneak, boolean active) {
		if (!isControlsSneak()) {
			setSneakCaptured(false);
			return;
		}
		if (!isSneakCaptured() && active && originalSneak) {
			setSneakCaptured(true);
		} else if (isSneakCaptured() && !originalSneak) {
			setSneakCaptured(false);
		}
	}

	public static boolean shouldOverrideSneak(boolean conditionsMet, boolean active) {
		return conditionsMet && isControlsSneak() && (active || isSneakCaptured());
	}

	public static void updateSneakState(boolean isSneaking) {
		if (isSneaking) {
			setWasSneaking(true);
		} else if (wasSneaking()) {
			setCurrentEdgeDistance(getRandomEdgeDistance());
			setWasSneaking(false);
		}
	}

	public static boolean conditionsMet(net.minecraft.world.entity.player.Player player, boolean originalSneak, boolean forward, boolean backward, boolean left, boolean right) {
		if (!isConditionalEnabled()) {
			return true;
		}
		float clampedPitch = Math.max(-90.0F, Math.min(90.0F, player.getXRot()));
		if (clampedPitch < getPitchMin() || clampedPitch > getPitchMax()) {
			return false;
		}
		if (isOnGroundConditionEnabled() && !player.onGround()) {
			return false;
		}
		if (isSneakConditionEnabled() && !originalSneak) {
			return false;
		}
		if (isHoldingBlocksConditionEnabled()) {
			boolean holdingBlock = player.getMainHandItem().getItem() instanceof BlockItem
				|| player.getOffhandItem().getItem() instanceof BlockItem;
			if (!holdingBlock) {
				return false;
			}
		}
		if (isForwardsConditionEnabled() && !forward) {
			return false;
		}
		if (isBackwardsConditionEnabled() && !backward) {
			return false;
		}
		if (isLeftConditionEnabled() && !left) {
			return false;
		}
		if (isRightConditionEnabled() && !right) {
			return false;
		}
		return true;
	}

	@Override
	public void onDisable() {
		setWasSneaking(false);
		setSneakCaptured(false);
		super.onDisable();
	}
}
