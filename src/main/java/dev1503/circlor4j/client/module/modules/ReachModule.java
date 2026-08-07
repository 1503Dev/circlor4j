package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Extends the player's interaction reach by overriding the block and entity
 * interaction range attributes while the module is enabled. The value is set by
 * the "Reach" slider; the original values are restored on disable.
 */
public class ReachModule extends Module {
	private static final String REACH = "reach";
	private double originalBlockReach = 4.5;
	private double originalEntityReach = 3.0;

	public ReachModule(StatusManager status) {
		super(status, "reach", "Reach", "Extends your interaction reach", ModuleCategory.PLAYER);
		this.registerSlider(REACH, "Reach", 3.0, 8.0, 0.02, 3.0);
	}

	@Override
	public void onEnable() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		AttributeInstance block = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		if (block != null) {
			this.originalBlockReach = block.getBaseValue();
		}
		AttributeInstance entity = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (entity != null) {
			this.originalEntityReach = entity.getBaseValue();
		}
	}

	@Override
	public void onDisable() {
		this.restore();
	}

	@Override
	public void onTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		double reach = this.getStatus().getDouble(REACH + "/" + REACH, 3.0);
		AttributeInstance block = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		if (block != null) {
			block.setBaseValue(reach);
		}
		AttributeInstance entity = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (entity != null) {
			entity.setBaseValue(reach);
		}
	}

	private void restore() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		AttributeInstance block = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		if (block != null) {
			block.setBaseValue(this.originalBlockReach);
		}
		AttributeInstance entity = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (entity != null) {
			entity.setBaseValue(this.originalEntityReach);
		}
	}
}