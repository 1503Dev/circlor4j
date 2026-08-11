package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ScaffoldModule extends Module {
	public static final String ID = "scaffold";

	private static final String FAST_TOWER = "fast_tower";
	private static final String TOWER_SPEED = "tower_speed";
	private static final String WHILE_MOVING = "while_moving";
	private static final String ONLY_ON_CLICK = "only_on_click";
	private static final String AUTO_SWITCH = "auto_switch";
	private static final String ROTATE = "rotate";
	private static final String AIR_PLACE = "air_place";
	private static final String AHEAD_DISTANCE = "ahead_distance";
	private static final String PLACE_RANGE = "place_range";
	private static final String RADIUS = "radius";
	private static final String BLOCKS_PER_TICK = "blocks_per_tick";
	private static final String SWING = "swing";
	private static final String BLOCKS = "blocks";
	private static final String BLOCKS_FILTER = "blocks_filter";

	private static final String[] DEFAULT_BLOCKS = new String[]{
		"minecraft:obsidian",
		"minecraft:cobblestone",
		"minecraft:netherite_block",
		"minecraft:ender_chest"
	};

	private int placeDelayCounter;

	public ScaffoldModule(StatusManager status) {
		super(status, ID, "Scaffold", "Automatically places blocks beneath you", ModuleCategory.PLAYER);
		this.registerToggle(FAST_TOWER, "Fast Tower", false);
		this.registerSlider(FAST_TOWER, TOWER_SPEED, "Tower Speed", 0.0, 1.0, 0.05, 0.5);
		this.registerToggleIn(FAST_TOWER, WHILE_MOVING, "While Moving", false);
		this.registerToggle(ONLY_ON_CLICK, "Only On Click", false);
		this.registerToggle(AUTO_SWITCH, "Auto Switch", true);
		this.registerToggle(ROTATE, "Rotate", true);
		this.registerToggle(AIR_PLACE, "Air Place", false);
		this.registerSlider(AHEAD_DISTANCE, "Ahead Distance", 0.0, 1.0, 0.05, 0.0, "scaffold/air_place/enabled == 0");
		this.registerSlider(PLACE_RANGE, "Place Range", 0.0, 8.0, 0.5, 4.0, "scaffold/air_place/enabled == 0");
		this.registerSlider(RADIUS, "Radius", 0.0, 6.0, 1.0, 0.0, "scaffold/air_place/enabled == 1");
		this.registerSlider(BLOCKS_PER_TICK, "Blocks Per Tick", 1.0, 10.0, 1.0, 3.0, "scaffold/air_place/enabled == 1");
		this.registerToggle(SWING, "Swing", true);
		this.registerBlockList(BLOCKS, "Blocks", DEFAULT_BLOCKS);
		this.registerDropdown(BLOCKS_FILTER, "Blocks Filter", new String[]{"Blacklist", "Whitelist"}, 0);
	}

	private static String blocksPrefix() {
		return ID + "/" + BLOCKS + "/";
	}

	private static List<Block> getSelectedBlocks() {
		List<Block> result = new ArrayList<>();
		StatusManager sm = StatusManager.getInstance();
		Map<String, Double> entries = sm.entriesWithPrefix(blocksPrefix());
		for (Map.Entry<String, Double> entry : entries.entrySet()) {
			if (entry.getValue() < 1.0) continue;
			String blockId = entry.getKey().substring(blocksPrefix().length());
			Block block = BuiltInRegistries.BLOCK.getValue(Identifier.tryParse(blockId));
			if (block != null) result.add(block);
		}
		return result;
	}

	private static boolean isBlocksFilterWhitelist() {
		return StatusManager.getInstance().getInt(ID + "/" + BLOCKS_FILTER, 0) == 1;
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static boolean isFastTowerEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + FAST_TOWER + "/enabled", false);
	}

	public static double getTowerSpeed() {
		return StatusManager.getInstance().getDouble(ID + "/" + TOWER_SPEED, 0.5);
	}

	public static boolean isWhileMovingEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + FAST_TOWER + "/" + WHILE_MOVING + "/enabled", false);
	}

	public static boolean isOnlyOnClickEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + ONLY_ON_CLICK + "/enabled", false);
	}

	public static boolean isAutoSwitchEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + AUTO_SWITCH + "/enabled", true);
	}

	public static boolean isRotateEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + ROTATE + "/enabled", true);
	}

	public static boolean isAirPlaceEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + AIR_PLACE + "/enabled", false);
	}

	public static double getAheadDistance() {
		return StatusManager.getInstance().getDouble(ID + "/" + AHEAD_DISTANCE, 0.0);
	}

	public static double getPlaceRange() {
		return StatusManager.getInstance().getDouble(ID + "/" + PLACE_RANGE, 4.0);
	}

	public static int getRadius() {
		return (int) StatusManager.getInstance().getDouble(ID + "/" + RADIUS, 0.0);
	}

	public static int getBlocksPerTick() {
		return (int) StatusManager.getInstance().getDouble(ID + "/" + BLOCKS_PER_TICK, 3.0);
	}

	public static boolean isSwingEnabled() {
		return StatusManager.getInstance().getBoolean(ID + "/" + SWING + "/enabled", true);
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.gameMode == null) return;
		if (isOnlyOnClickEnabled() && !mc.options.keyUse.isDown()) return;
		if (this.placeDelayCounter > 0) {
			this.placeDelayCounter--;
			return;
		}

		Vec3 playerPos = mc.player.position();
		Vec3 playerVel = mc.player.getDeltaMovement();
		Vec3 vec = playerPos.add(playerVel).add(0, -0.75, 0);

		BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
		if (isAirPlaceEnabled()) {
			bp.set(vec.x(), vec.y(), vec.z());
		} else {
			Vec3 pos = playerPos;
			if (getAheadDistance() != 0 && !towering() && !mc.level.getBlockState(mc.player.blockPosition().below()).getCollisionShape(mc.level, mc.player.blockPosition()).isEmpty()) {
				Vec3 dir = getDirectionFromYaw(mc.player.getYRot()).multiply(getAheadDistance(), 0, getAheadDistance());
				if (mc.options.keyUp.isDown()) pos = pos.add(dir.x, 0, dir.z);
				if (mc.options.keyDown.isDown()) pos = pos.add(-dir.x, 0, -dir.z);
				if (mc.options.keyLeft.isDown()) pos = pos.add(dir.z, 0, -dir.x);
				if (mc.options.keyRight.isDown()) pos = pos.add(-dir.z, 0, dir.x);
			}
			bp.set(pos.x, vec.y, pos.z);
		}

		if (mc.options.keyShift.isDown() && !mc.options.keyJump.isDown() && mc.player.getY() + vec.y > -1) {
			bp.setY(bp.getY() - 1);
		}
		if (bp.getY() >= mc.player.blockPosition().getY()) {
			bp.setY(mc.player.blockPosition().getY() - 1);
		}
		BlockPos targetBlock = bp.immutable();

		if (!isAirPlaceEnabled() && getPlaceSide(bp) == null) {
			Vec3 pos = playerPos.add(0, -0.98f, 0).add(playerVel);
			List<BlockPos> blockPosArray = new ArrayList<>();
			double range = getPlaceRange();
			for (int x = (int) (mc.player.getX() - range); x < mc.player.getX() + range; x++) {
				for (int z = (int) (mc.player.getZ() - range); z < mc.player.getZ() + range; z++) {
					for (int y = (int) Math.max(mc.level.getMinY(), mc.player.getY() - range); y < Math.min(mc.level.getHeight(), mc.player.getY() + range); y++) {
						bp.set(x, y, z);
						if (getPlaceSide(bp) == null) continue;
						if (!canPlace(bp)) continue;
						if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(bp.relative(getClosestPlaceSide(bp)))) > 36) continue;
						blockPosArray.add(new BlockPos(bp));
					}
				}
			}
			if (blockPosArray.isEmpty()) return;
			blockPosArray.sort(Comparator.comparingDouble(blockPos -> blockPos.distSqr(targetBlock)));
			bp.set(blockPosArray.getFirst());
		}

		if (isAirPlaceEnabled()) {
			List<BlockPos> blocks = new ArrayList<>();
			int radius = getRadius();
			for (int x = (int) (bp.getX() - radius); x <= bp.getX() + radius; x++) {
				for (int z = (int) (bp.getZ() - radius); z <= bp.getZ() + radius; z++) {
					BlockPos blockPos = BlockPos.containing(x, bp.getY(), z);
					if (mc.player.position().distanceTo(Vec3.atCenterOf(blockPos)) <= radius || (x == bp.getX() && z == bp.getZ())) {
						blocks.add(blockPos);
					}
				}
			}
			if (!blocks.isEmpty()) {
				blocks.sort(Comparator.comparingDouble(this::squaredDistanceTo));
				int counter = 0;
				for (BlockPos block : blocks) {
					if (place(mc, block)) counter++;
					if (counter >= getBlocksPerTick()) break;
				}
			}
		} else {
			place(mc, bp);
		}

		if (isFastTowerEnabled() && mc.options.keyJump.isDown() && !mc.options.keyShift.isDown() && hasBlocks(mc) && (isAutoSwitchEnabled() || getHand() != null)) {
			Vec3 velocity = mc.player.getDeltaMovement();
			AABB playerBox = mc.player.getBoundingBox();
			if (mc.level.getBlockCollisions(mc.player, playerBox.move(0, 1, 0)).iterator().hasNext()) {
				mc.player.setDeltaMovement(velocity.x, Math.ceil(mc.player.getY()) - mc.player.getY(), velocity.z);
				mc.player.setOnGround(true);
			} else {
				if (isWhileMovingEnabled() || !isMoving(mc.player)) {
					velocity = new Vec3(velocity.x, getTowerSpeed(), velocity.z);
				}
				mc.player.setDeltaMovement(velocity);
			}
		}
	}

	public boolean scaffolding() {
		Minecraft mc = Minecraft.getInstance();
		return isActive() && (!isOnlyOnClickEnabled() || mc.options.keyUse.isDown());
	}

	public boolean towering() {
		Minecraft mc = Minecraft.getInstance();
		return scaffolding() && isFastTowerEnabled() && mc.options.keyJump.isDown() && !mc.options.keyShift.isDown() &&
			(isWhileMovingEnabled() || !isMoving(mc.player)) && hasBlocks(mc) && (isAutoSwitchEnabled() || getHand() != null);
	}

	private boolean hasBlocks(Minecraft mc) {
		for (int i = 0; i < 9; i++) {
			ItemStack stack = mc.player.getInventory().getItem(i);
			if (!stack.isEmpty() && validItem(stack, BlockPos.ZERO)) {
				return true;
			}
		}
		return false;
	}

	private InteractionHand getHand() {
		Minecraft mc = Minecraft.getInstance();
		ItemStack mainHand = mc.player.getMainHandItem();
		if (!mainHand.isEmpty() && mainHand.getItem() instanceof BlockItem) {
			return InteractionHand.MAIN_HAND;
		}
		return null;
	}

	private boolean validItem(ItemStack itemStack, BlockPos pos) {
		if (!(itemStack.getItem() instanceof BlockItem)) return false;

		Block block = ((BlockItem) itemStack.getItem()).getBlock();

		List<Block> selectedBlocks = getSelectedBlocks();
		boolean inList = selectedBlocks.contains(block);
		if (isBlocksFilterWhitelist()) {
			if (!inList) return false;
		} else {
			if (inList) return false;
		}

		VoxelShape collision = block.defaultBlockState().getCollisionShape(Minecraft.getInstance().level, pos);
		if (collision.isEmpty()) return false;

		if (block instanceof FallingBlock) {
			BlockState state = Minecraft.getInstance().level.getBlockState(pos);
			return !FallingBlock.isFree(state);
		}
		return true;
	}

	private boolean place(Minecraft mc, BlockPos pos) {
		Direction direction = getPlaceSide(pos);
		if (direction == null) return false;
		BlockPos adjacent = pos.relative(direction);
		int slot = findBlockSlot(mc, pos);
		if (slot == -1) return false;
		if (slot == -2 && !isAutoSwitchEnabled()) return false;

		Vec3 hitVec = Vec3.atCenterOf(adjacent).add(
			direction.getStepX() * 0.5,
			direction.getStepY() * 0.5,
			direction.getStepZ() * 0.5
		);
		BlockHitResult hitResult = new BlockHitResult(hitVec, direction.getOpposite(), adjacent, false);
		int prevSlot = mc.player.getInventory().getSelectedSlot();
		boolean switched = false;
		if (prevSlot != slot) {
			mc.player.getInventory().setSelectedSlot(slot);
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
			switched = true;
		}
		if (isRotateEnabled()) {
			float[] rotations = getRotations(hitVec);
			mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(rotations[0], rotations[1], mc.player.onGround(), mc.player.horizontalCollision));
		}
		mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
		if (isSwingEnabled()) {
			mc.player.swing(InteractionHand.MAIN_HAND);
		}
		if (switched) {
			mc.player.getInventory().setSelectedSlot(prevSlot);
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(prevSlot));
		}
		return true;
	}

	private int findBlockSlot(Minecraft mc, BlockPos pos) {
		for (int i = 0; i < 9; i++) {
			ItemStack stack = mc.player.getInventory().getItem(i);
			if (!stack.isEmpty() && validItem(stack, pos)) {
				return i;
			}
		}
		return -1;
	}

	private Direction getPlaceSide(BlockPos pos) {
		Minecraft mc = Minecraft.getInstance();
		Direction best = null;
		double bestDist = Double.MAX_VALUE;
		Vec3 eyes = mc.player.getEyePosition();
		for (Direction dir : Direction.values()) {
			BlockPos adjacent = pos.relative(dir);
			if (!mc.level.isLoaded(adjacent)) continue;
			BlockState adjState = mc.level.getBlockState(adjacent);
			if (adjState.isAir()) continue;
			VoxelShape collision = adjState.getCollisionShape(mc.level, adjacent);
			if (collision.isEmpty()) continue;
			Vec3 faceCenter = Vec3.atCenterOf(adjacent).add(dir.getStepX() * 0.5, dir.getStepY() * 0.5, dir.getStepZ() * 0.5);
			double dist = eyes.distanceToSqr(faceCenter);
			if (dist < bestDist) {
				bestDist = dist;
				best = dir;
			}
		}
		return best;
	}

	private Direction getClosestPlaceSide(BlockPos pos) {
		return getPlaceSide(pos);
	}

	private boolean canPlace(BlockPos pos) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.level.isLoaded(pos)) return false;
		BlockState state = mc.level.getBlockState(pos);
		return state.isAir() || state.canBeReplaced();
	}

	private double squaredDistanceTo(BlockPos pos) {
		Minecraft mc = Minecraft.getInstance();
		return mc.player.position().distanceToSqr(Vec3.atCenterOf(pos));
	}

	private boolean isMoving(net.minecraft.client.player.LocalPlayer player) {
		return player.input.keyPresses.forward() || player.input.keyPresses.backward() || player.input.keyPresses.left() || player.input.keyPresses.right();
	}

	private Vec3 getDirectionFromYaw(float yaw) {
		float rad = (float) Math.toRadians(yaw);
		return new Vec3(-Math.sin(rad), 0, Math.cos(rad));
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
}
