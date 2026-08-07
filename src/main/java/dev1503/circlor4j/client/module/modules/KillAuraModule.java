package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;

/**
 * Automatically attacks eligible entities within reach.
 * <ul>
 *   <li>MaxReach - the maximum attack distance; the actual range is capped by the current
 *       entity interaction range (the Reach module's value when enabled, vanilla otherwise).</li>
 *   <li>Mode - AutoInterval attacks whenever the held weapon's cooldown finishes; FixedCPS
 *       attacks at a steady rate set by CPS.</li>
 *   <li>MultiTarget - when enabled, attacks every eligible entity in range each swing;
 *       otherwise only the nearest one.</li>
 *   <li>Players/Mobs - which target types to attack.</li>
 *   <li>Mobs filters - FilterVillager skips villagers, FilterBaby skips baby mobs.</li>
 * </ul>
 */
public class KillAuraModule extends Module {
	public static final String ID = "killaura";
	private static final String MAX_REACH = "max_reach";
	private static final String MODE = "mode";
	private static final String CPS = "cps";
	private static final String MULTI_TARGET = "multi_target";
	private static final String PLAYERS = "players";
	private static final String MOBS = "mobs";
	private static final String FILTER_VILLAGER = "filter_villager";
	private static final String FILTER_BABY = "filter_baby";
	private static final int MODE_AUTO_INTERVAL = 0;
	private static final int MODE_FIXED_CPS = 1;

	private double cpsAccumulator;

	public KillAuraModule(StatusManager status) {
		super(status, ID, "KillAura", "Automatically attacks nearby entities", ModuleCategory.COMBAT);
		this.registerSlider(MAX_REACH, "MaxReach", 1.0, 8.0, 0.05, 3.0);
		this.registerDropdown(
			MODE,
			"Mode",
			new String[] {"AutoInterval", "FixedCPS"},
			new String[] {"module.killaura.mode.auto_interval.name", "module.killaura.mode.fixed_cps.name"},
			MODE_AUTO_INTERVAL
		);
		this.registerSlider(CPS, "CPS", 0.5, 20.0, 0.5, 5.0, ID + "/" + MODE + " == " + MODE_FIXED_CPS);
		this.registerToggle(MULTI_TARGET, "MultiTarget");
		this.registerToggle(PLAYERS, "Players");
		this.registerToggle(MOBS, "Mobs");
		this.registerToggleIn(MOBS, FILTER_VILLAGER, "FilterVillager");
		this.registerToggleIn(MOBS, FILTER_BABY, "FilterBaby");
	}

	@Override
	public void onTick() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.gameMode == null || minecraft.isPaused() || player.isSpectator()) {
			return;
		}
		int mode = (int) this.getStatus().getDouble(ID + "/" + MODE, MODE_AUTO_INTERVAL);
		if (mode == MODE_AUTO_INTERVAL) {
			if (player.getAttackStrengthScale(0.0F) >= 1.0F) {
				this.attack(minecraft, player);
			}
		} else {
			double cps = this.getStatus().getDouble(ID + "/" + CPS, 5.0);
			this.cpsAccumulator += cps / 20.0;
			if (this.cpsAccumulator >= 1.0) {
				this.cpsAccumulator -= 1.0;
				this.attack(minecraft, player);
			}
		}
	}

	private void attack(Minecraft minecraft, LocalPlayer player) {
		List<Entity> targets = this.collectTargets(player);
		if (targets.isEmpty()) {
			return;
		}
		boolean multiTarget = this.getStatus().getBoolean(ID + "/" + MULTI_TARGET + "/enabled", false);
		if (multiTarget) {
			for (Entity target : targets) {
				minecraft.gameMode.attack(player, target);
			}
		} else {
			minecraft.gameMode.attack(player, targets.get(0));
		}
		player.swing(InteractionHand.MAIN_HAND);
	}

	/** All eligible living entities within reach, nearest first. */
	private List<Entity> collectTargets(LocalPlayer player) {
		StatusManager status = this.getStatus();
		boolean playersEnabled = status.getBoolean(ID + "/" + PLAYERS + "/enabled", false);
		boolean mobsEnabled = status.getBoolean(ID + "/" + MOBS + "/enabled", false);
		if (!playersEnabled && !mobsEnabled) {
			return List.of();
		}
		boolean filterVillager = status.getBoolean(ID + "/" + MOBS + "/" + FILTER_VILLAGER + "/enabled", false);
		boolean filterBaby = status.getBoolean(ID + "/" + MOBS + "/" + FILTER_BABY + "/enabled", false);

		double maxReach = status.getDouble(ID + "/" + MAX_REACH, 3.0);
		double reachLimit = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
		double reach = Math.min(maxReach, reachLimit);
		double reachSq = reach * reach;

		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return List.of();
		}
		List<Entity> targets = new ArrayList<>();
		for (Entity entity : level.entitiesForRendering()) {
			if (entity == player || !entity.isAlive() || !(entity instanceof LivingEntity living)) {
				continue;
			}
			if (entity instanceof Player) {
				if (!playersEnabled || entity.isSpectator()) {
					continue;
				}
			} else {
				if (!mobsEnabled) {
					continue;
				}
				if (filterVillager && entity instanceof AbstractVillager) {
					continue;
				}
				if (filterBaby && living.isBaby()) {
					continue;
				}
			}
			if (player.distanceToSqr(entity) <= reachSq) {
				targets.add(entity);
			}
		}
		targets.sort(Comparator.comparingDouble(player::distanceToSqr));
		return targets;
	}
}