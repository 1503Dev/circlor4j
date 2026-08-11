package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoTotemModule extends Module {
	private static final int OFFHAND_SLOT = 45;

	public AutoTotemModule(StatusManager status) {
		super(status, "auto_totem", "AutoTotem", "Automatically ensures a totem is in the offhand", ModuleCategory.MISC);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("auto_totem/enabled", false);
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.gameMode == null || mc.gui.screen() != null) {
			return;
		}

		if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
			return;
		}

		int slot = findTotem(mc.player);
		if (slot == -1) {
			return;
		}

		moveTotemToOffhand(mc, slot);
	}

	private int findTotem(net.minecraft.world.entity.player.Player player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			if (player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
				return i;
			}
		}
		return -1;
	}

	private void moveTotemToOffhand(Minecraft mc, int invSlot) {
		int containerId = mc.player.inventoryMenu.containerId;
		int stateId = mc.player.inventoryMenu.getStateId();

		mc.player.connection.send(new ServerboundContainerClickPacket(
			containerId, stateId, (short) invSlot, (byte) 0, ContainerInput.PICKUP,
			it.unimi.dsi.fastutil.ints.Int2ObjectMaps.emptyMap(),
			net.minecraft.network.HashedStack.EMPTY
		));
		mc.player.connection.send(new ServerboundContainerClickPacket(
			containerId, stateId, (short) OFFHAND_SLOT, (byte) 0, ContainerInput.PICKUP,
			it.unimi.dsi.fastutil.ints.Int2ObjectMaps.emptyMap(),
			net.minecraft.network.HashedStack.EMPTY
		));
		mc.player.connection.send(new ServerboundContainerClickPacket(
			containerId, stateId, (short) invSlot, (byte) 0, ContainerInput.PICKUP,
			it.unimi.dsi.fastutil.ints.Int2ObjectMaps.emptyMap(),
			net.minecraft.network.HashedStack.EMPTY
		));
	}
}
