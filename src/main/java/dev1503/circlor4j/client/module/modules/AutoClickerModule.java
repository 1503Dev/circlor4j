package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

/**
 * Automatically clicks the mouse. The click rate randomly fluctuates within the CPS range.
 * Each side (left/right) has a toggle and a nested "hold" toggle: when hold is on, clicks
 * only fire while the corresponding mouse button is physically held down.
 */
public class AutoClickerModule extends Module {
	public static final String ID = "autoclicker";
	private static final String CPS = "cps";
	private static final String LEFT = "left";
	private static final String RIGHT = "right";
	private static final String HOLD = "hold";

	private final Random random = new Random();
	private int leftTicks;
	private int rightTicks;

	public AutoClickerModule(StatusManager status) {
		super(status, ID, "AutoClicker", "Automatically clicks the mouse", ModuleCategory.COMBAT);
		this.registerRangeSlider(CPS, "CPS", 0.5, 20.0, 0.5, 5.0, 10.0);
		this.registerToggle(LEFT, "LeftClicker", true);
		this.registerToggleIn(LEFT, HOLD, "Hold", true);
		this.registerToggle(RIGHT, "RightClicker", false);
		this.registerToggleIn(RIGHT, HOLD, "Hold", false);
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.gameMode == null || mc.gui.screen() != null || mc.isPaused() || player.isSpectator()) {
			return;
		}
		double cpsMin = Math.min(this.getCpsMin(), this.getCpsMax());
		double cpsMax = Math.max(this.getCpsMin(), this.getCpsMax());

		if (this.isClickerActive(LEFT) && (!this.isHoldOn(LEFT) || isMouseDown(0))) {
			this.leftTicks--;
			if (this.leftTicks <= 0) {
				this.leftClick(mc, player);
				this.leftTicks = this.nextTicks(cpsMin, cpsMax);
			}
		}
		if (this.isClickerActive(RIGHT) && (!this.isHoldOn(RIGHT) || isMouseDown(1))) {
			this.rightTicks--;
			if (this.rightTicks <= 0) {
				this.rightClick(mc, player);
				this.rightTicks = this.nextTicks(cpsMin, cpsMax);
			}
		}
	}

	private boolean isClickerActive(String side) {
		return this.getStatus().getBoolean(ID + "/" + side + "/enabled", false);
	}

	private boolean isHoldOn(String side) {
		return this.getStatus().getBoolean(ID + "/" + side + "/" + HOLD + "/enabled", false);
	}

	private double getCpsMin() {
		return this.getStatus().getDouble(ID + "/" + CPS + "/min", 5.0);
	}

	private double getCpsMax() {
		return this.getStatus().getDouble(ID + "/" + CPS + "/max", 10.0);
	}

	private int nextTicks(double cpsMin, double cpsMax) {
		double cps = cpsMin + this.random.nextDouble() * (cpsMax - cpsMin);
		return Math.max(1, (int) Math.round(20.0 / cps));
	}

	private static boolean isMouseDown(int button) {
		long handle = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetMouseButton(handle, button) == GLFW.GLFW_PRESS;
	}

	private void leftClick(Minecraft mc, LocalPlayer player) {
		if (mc.hitResult != null) {
			switch (mc.hitResult.getType()) {
				case ENTITY -> mc.gameMode.attack(player, ((EntityHitResult) mc.hitResult).getEntity());
				case BLOCK -> {
					BlockHitResult blockHit = (BlockHitResult) mc.hitResult;
					mc.gameMode.startDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection());
				}
				default -> {
				}
			}
		}
		player.swing(InteractionHand.MAIN_HAND);
	}

	private void rightClick(Minecraft mc, LocalPlayer player) {
		InteractionHand hand = InteractionHand.MAIN_HAND;
		boolean usedTarget = false;
		if (mc.hitResult != null) {
			switch (mc.hitResult.getType()) {
				case ENTITY -> {
					EntityHitResult entityHit = (EntityHitResult) mc.hitResult;
					if (player.isWithinEntityInteractionRange(entityHit.getEntity(), 0.0)) {
						mc.gameMode.interact(player, entityHit.getEntity(), entityHit, hand);
						usedTarget = true;
					}
				}
				case BLOCK -> {
					mc.gameMode.useItemOn(player, hand, (BlockHitResult) mc.hitResult);
					usedTarget = true;
				}
				default -> {
				}
			}
		}
		if (!usedTarget) {
			mc.gameMode.useItem(player, hand);
		}
		player.swing(hand);
	}
}