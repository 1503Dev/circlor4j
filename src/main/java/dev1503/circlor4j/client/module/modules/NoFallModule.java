package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Items;

/**
 * Prevents fall damage by sending an "on ground" status packet to the server while falling
 * fast, which resets the server-side fall distance before landing (reference: TrollHack NoFall).
 * The LocalPlayerMixin additionally keeps the client-side fall distance near zero.
 */
public class NoFallModule extends Module {
	public NoFallModule(StatusManager status) {
		super(status, "no_fall", "NoFall", "Prevents fall damage", ModuleCategory.PLAYER);
	}

	@Override
	public void onTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || isHoldingMace(player)) {
			return;
		}
		if (player.getDeltaMovement().y() < -0.5) {
			player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, player.horizontalCollision));
		}
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean("no_fall/enabled", false);
	}

	private static boolean isHoldingMace(LocalPlayer player) {
		return player.getMainHandItem().is(Items.MACE);
	}
}
