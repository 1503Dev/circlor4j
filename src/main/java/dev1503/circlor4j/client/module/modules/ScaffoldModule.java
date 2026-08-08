package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ScaffoldModule extends Module {
	public static final String ID = "scaffold";
	private static final String SWING = "swing";
	private static final String ROTATE = "rotate";

	private int placeDelayCounter;

	public ScaffoldModule(StatusManager status) {
		super(status, ID, "Scaffold", "Automatically places blocks beneath you", ModuleCategory.PLAYER);
		this.registerToggle(SWING, "Swing", true);
		this.registerToggle(ROTATE, "Rotate", true);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isRotateEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + ROTATE + "/enabled", false);
	}

	public static boolean isSwingEnabled() {
		return isActive() && StatusManager.getInstance().getBoolean(ID + "/" + SWING + "/enabled", false);
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.gameMode == null) {
			return;
		}
		if (this.placeDelayCounter > 0) {
			this.placeDelayCounter--;
			return;
		}
		BlockPos below = mc.player.blockPosition().below();
		if (!isAir(mc, below)) {
			return;
		}
		Direction direction = this.getPlaceDirection(mc, below);
		if (direction == null) {
			return;
		}
		int slot = this.findBlockSlot(mc);
		if (slot == -1) {
			return;
		}
		this.placeBlock(mc, below, direction, slot);
		this.placeDelayCounter = 1;
	}

	private Direction getPlaceDirection(Minecraft mc, BlockPos pos) {
		Direction best = null;
		double bestDist = Double.MAX_VALUE;
		Vec3 eyes = mc.player.getEyePosition();
		for (Direction dir : Direction.values()) {
			BlockPos adjacent = pos.relative(dir);
			if (isAir(mc, adjacent)) {
				continue;
			}
			Vec3 faceCenter = Vec3.atCenterOf(adjacent).add(dir.getStepX() * 0.5, dir.getStepY() * 0.5, dir.getStepZ() * 0.5);
			double dist = eyes.distanceToSqr(faceCenter);
			if (dist < bestDist) {
				bestDist = dist;
				best = dir;
			}
		}
		return best;
	}

	private void placeBlock(Minecraft mc, BlockPos pos, Direction direction, int slot) {
		BlockPos adjacent = pos.relative(direction);
		Vec3 hitVec = Vec3.atCenterOf(adjacent).add(
			direction.getStepX() * 0.5,
			direction.getStepY() * 0.5,
			direction.getStepZ() * 0.5
		);
		BlockHitResult hitResult = new BlockHitResult(hitVec, direction.getOpposite(), adjacent, false);
		int prevSlot = mc.player.getInventory().getSelectedSlot();
		if (prevSlot != slot) {
			mc.player.getInventory().setSelectedSlot(slot);
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
		}
		if (isRotateEnabled()) {
			float[] rotations = getRotations(hitVec);
			mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(rotations[0], rotations[1], mc.player.onGround(), mc.player.horizontalCollision));
		}
		mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
		if (isSwingEnabled()) {
			mc.player.swing(InteractionHand.MAIN_HAND);
		}
		if (prevSlot != slot) {
			mc.player.getInventory().setSelectedSlot(prevSlot);
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(prevSlot));
		}
	}

	private static float[] getRotations(Vec3 target) {
		Vec3 eyes = Minecraft.getInstance().player.getEyePosition();
		double dx = target.x - eyes.x;
		double dy = target.y - eyes.y;
		double dz = target.z - eyes.z;
		double dist = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
		pitch = Math.max(-90.0F, Math.min(90.0F, pitch));
		return new float[] {yaw, pitch};
	}

	private int findBlockSlot(Minecraft mc) {
		for (int i = 0; i < 9; i++) {
			ItemStack stack = mc.player.getInventory().getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
				return i;
			}
		}
		return -1;
	}

	private static boolean isAir(Minecraft mc, BlockPos pos) {
		if (!mc.level.isLoaded(pos)) {
			return false;
		}
		BlockState state = mc.level.getBlockState(pos);
		return state.isAir();
	}
}
