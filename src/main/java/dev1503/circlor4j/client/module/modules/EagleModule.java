package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class EagleModule extends Module {
	public static final String ID = "eagle";

	public EagleModule(StatusManager status) {
		super(status, ID, "Eagle", "Automatically sneaks at block edges", ModuleCategory.PLAYER);
		StatusManager.getInstance().setValueOnly(ID + "/edge_distance", 0.5);
		StatusManager.getInstance().setValueOnly(ID + "/was_sneaking", 0.0);
		StatusManager.getInstance().setValueOnly(ID + "/sneak_captured", 0.0);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static float getCurrentEdgeDistance() {
		return StatusManager.getInstance().getFloat(ID + "/edge_distance", 0.5F);
	}

	public static void setCurrentEdgeDistance(float value) {
		StatusManager.getInstance().setValueOnly(ID + "/edge_distance", value);
	}

	public static boolean wasSneaking() {
		return StatusManager.getInstance().getBoolean(ID + "/was_sneaking", false);
	}

	public static void setWasSneaking(boolean value) {
		StatusManager.getInstance().setValueOnly(ID + "/was_sneaking", value ? 1.0 : 0.0);
	}

	public static boolean isSneakCaptured() {
		return StatusManager.getInstance().getBoolean(ID + "/sneak_captured", false);
	}

	public static void setSneakCaptured(boolean value) {
		StatusManager.getInstance().setValueOnly(ID + "/sneak_captured", value ? 1.0 : 0.0);
	}

	public static boolean shouldActivateEagle(net.minecraft.world.entity.player.Player player) {
		if (player.getAbilities().flying) {
			return false;
		}
		return isCloseToEdge(player);
	}

	public static boolean isCloseToEdge(net.minecraft.world.entity.player.Player player) {
		Vec3 pos = player.position();
		float edgeDistance = getCurrentEdgeDistance();

		BlockPos below = BlockPos.containing(pos.x, pos.y - 0.1, pos.z);
		if (!player.level().getBlockState(below).isSolidRender()) {
			return false;
		}

		double localX = pos.x - below.getX();
		double localZ = pos.z - below.getZ();

		if (localX < (double) edgeDistance || localX > 1.0 - (double) edgeDistance) {
			return false;
		}
        return !(localZ < (double) edgeDistance) && !(localZ > 1.0 - (double) edgeDistance);
    }

	public static void updateSneakCapture(boolean originalSneak, boolean active) {
		if (!isSneakCaptured() && active && originalSneak) {
			setSneakCaptured(true);
		} else if (isSneakCaptured() && !originalSneak) {
			setSneakCaptured(false);
		}
	}

	public static boolean shouldOverrideSneak(boolean conditionsMet, boolean active) {
		return conditionsMet && (active || isSneakCaptured());
	}

	public static void updateSneakState(boolean isSneaking) {
		if (isSneaking) {
			setWasSneaking(true);
		} else if (wasSneaking()) {
			setWasSneaking(false);
		}
	}

	@Override
	public void onDisable() {
		setWasSneaking(false);
		setSneakCaptured(false);
		super.onDisable();
	}
}
