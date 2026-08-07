package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * CrystalAura: finds a good spot near the nearest enemy, places an end crystal there and
 * detonates it (also breaks existing crystals that hurt the target). Ported from TrollHack's
 * crystal logic, simplified for this mod's module system.
 */
public class CrystalAuraModule extends Module {
	public static final String ID = "crystal_aura";
	private static final String PLACE_RANGE = "place_range";
	private static final String BREAK_RANGE = "break_range";
	private static final String TARGET_RANGE = "target_range";
	private static final String MIN_DAMAGE = "min_damage";
	private static final String SELF_DAMAGE = "self_damage";
	private static final String PLACE_DELAY = "place_delay";
	private static final String BREAK_DELAY = "break_delay";

	private static final float EXPLOSION_POWER = 6.0F;

	private int placeTicks;
	private int breakTicks;

	public CrystalAuraModule(StatusManager status) {
		super(status, ID, "CrystalAura", "Places and detonates end crystals", ModuleCategory.COMBAT);
		this.registerSlider(PLACE_RANGE, "Place Range", 1.0, 6.0, 0.1, 4.5);
		this.registerSlider(BREAK_RANGE, "Break Range", 1.0, 6.0, 0.1, 4.5);
		this.registerSlider(TARGET_RANGE, "Target Range", 1.0, 16.0, 0.5, 12.0);
		this.registerSlider(MIN_DAMAGE, "Min Damage", 0.0, 20.0, 0.5, 6.0);
		this.registerSlider(SELF_DAMAGE, "Self Damage", 0.0, 20.0, 0.5, 4.0);
		this.registerSlider(PLACE_DELAY, "Place Delay", 0.0, 20.0, 1.0, 2.0);
		this.registerSlider(BREAK_DELAY, "Break Delay", 0.0, 20.0, 1.0, 2.0);
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.level == null || mc.gameMode == null || mc.isPaused() || player.isSpectator()) {
			return;
		}

		double targetRange = this.getStatus().getDouble(ID + "/" + TARGET_RANGE, 12.0);
		Player target = this.findTarget(player, targetRange);
		if (target == null) {
			return;
		}

		double breakRange = this.getStatus().getDouble(ID + "/" + BREAK_RANGE, 4.5);
		double placeRange = this.getStatus().getDouble(ID + "/" + PLACE_RANGE, 4.5);
		double minDamage = this.getStatus().getDouble(ID + "/" + MIN_DAMAGE, 6.0);
		double selfDamage = this.getStatus().getDouble(ID + "/" + SELF_DAMAGE, 4.0);
		int breakDelay = Math.max(1, (int) this.getStatus().getDouble(ID + "/" + BREAK_DELAY, 2.0));
		int placeDelay = Math.max(1, (int) this.getStatus().getDouble(ID + "/" + PLACE_DELAY, 2.0));

		this.breakTicks--;
		if (this.breakTicks <= 0 && this.breakCrystal(mc, player, target, breakRange)) {
			this.breakTicks = breakDelay;
		}

		this.placeTicks--;
		if (this.placeTicks <= 0 && this.placeCrystal(mc, player, target, placeRange, minDamage, selfDamage)) {
			this.placeTicks = placeDelay;
		}
	}

	private Player findTarget(LocalPlayer player, double range) {
		Player best = null;
		double bestDist = range * range;
		for (Player other : player.level().players()) {
			if (other == player || !other.isAlive() || other.isSpectator()) {
				continue;
			}
			double dist = player.distanceToSqr(other);
			if (dist <= bestDist) {
				bestDist = dist;
				best = other;
			}
		}
		return best;
	}

	// ---- breaking ----

	private boolean breakCrystal(Minecraft mc, LocalPlayer player, Player target, double breakRange) {
		AABB search = target.getBoundingBox().inflate(5.0);
		EndCrystal best = null;
		float bestDamage = 0.0F;
		for (EndCrystal crystal : mc.level.getEntitiesOfClass(EndCrystal.class, search)) {
			float damage = this.estimateDamage(target, crystal.position());
			if (damage > bestDamage && player.distanceTo(crystal) <= breakRange) {
				bestDamage = damage;
				best = crystal;
			}
		}
		if (best == null) {
			return false;
		}
		mc.gameMode.attack(player, best);
		player.swing(InteractionHand.MAIN_HAND);
		return true;
	}

	// ---- placing ----

	private boolean placeCrystal(Minecraft mc, LocalPlayer player, Player target, double placeRange, double minDamage, double selfDamageThreshold) {
		BlockPos base = this.findBestPlacePos(mc, player, target, placeRange, minDamage, selfDamageThreshold);
		if (base == null) {
			return false;
		}
		int slot = this.findEndCrystalSlot(player);
		if (slot < 0) {
			return false;
		}
		player.getInventory().setSelectedSlot(slot);

		Vec3 hitVec = new Vec3(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5);
		BlockHitResult blockHit = new BlockHitResult(hitVec, Direction.UP, base, false);
		mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, blockHit);
		player.swing(InteractionHand.MAIN_HAND);
		return true;
	}

	private BlockPos findBestPlacePos(Minecraft mc, LocalPlayer player, Player target, double placeRange, double minDamage, double selfDamageThreshold) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestScore = -1.0;
		int r = (int) Math.ceil(placeRange);
		Vec3 targetPos = target.position();
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -2; dy <= 2; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					pos.set(targetPos.x + dx, targetPos.y + dy, targetPos.z + dz);
					BlockPos base = pos.immutable();
					if (!this.canPlaceCrystal(mc.level, base)) {
						continue;
					}
					Vec3 crystalPos = new Vec3(base.getX() + 0.5, base.getY() + 1.5, base.getZ() + 0.5);
					if (player.distanceToSqr(crystalPos) > placeRange * placeRange) {
						continue;
					}
					float targetDamage = this.estimateDamage(target, crystalPos);
					if (targetDamage < minDamage) {
						continue;
					}
					float selfDamage = this.estimateDamage(player, crystalPos);
					if (selfDamage > selfDamageThreshold) {
						continue;
					}
					double score = targetDamage - selfDamage;
					if (score > bestScore) {
						bestScore = score;
						best = base;
					}
				}
			}
		}
		return best;
	}

	private boolean canPlaceCrystal(ClientLevel level, BlockPos base) {
		Block block = level.getBlockState(base).getBlock();
		if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
			return false;
		}
		if (!this.isReplaceable(level.getBlockState(base.above())) || !this.isReplaceable(level.getBlockState(base.above(2)))) {
			return false;
		}
		AABB box = new AABB(
			base.getX() + 0.001, base.getY() + 1.0, base.getZ() + 0.001,
			base.getX() + 0.999, base.getY() + 3.0, base.getZ() + 0.999
		);
		return level.getEntities((Entity) null, box).isEmpty();
	}

	private boolean isReplaceable(BlockState state) {
		return !state.liquid() && state.canBeReplaced();
	}

	// ---- damage ----

	private float estimateDamage(Entity target, Vec3 crystalPos) {
		Vec3 eye = target.getEyePosition();
		double dist = eye.distanceTo(crystalPos);
		if (dist >= EXPLOSION_POWER) {
			return 0.0F;
		}
		if (!hasLineOfSight(target.level(), crystalPos, eye, target)) {
			return 0.0F;
		}
		return EXPLOSION_POWER * (float) (1.0 - dist / EXPLOSION_POWER);
	}

	private static boolean hasLineOfSight(Level level, Vec3 from, Vec3 to, Entity ignore) {
		ClipContext context = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, ignore);
		BlockHitResult hit = level.clip(context);
		return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(from) >= to.distanceToSqr(from);
	}

	private static int findEndCrystalSlot(LocalPlayer player) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < 9; i++) {
			if (inventory.getItem(i).is(Items.END_CRYSTAL)) {
				return i;
			}
		}
		return -1;
	}
}