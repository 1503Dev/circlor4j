package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.CriticalsModule;
import dev1503.circlor4j.client.module.modules.NukerModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Nuker + Criticals hooks on the client game mode. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

	@Unique
	private static boolean circlor4j$nuking;

	/** Criticals: fake a fall before attacking so the server registers a critical hit. */
	@Inject(method = "attack", at = @At("HEAD"))
	private void circlor4jCriticals(Player player, Entity entity, CallbackInfo ci) {
		if (!CriticalsModule.isActive()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer localPlayer = mc.player;
		if (localPlayer == null || !localPlayer.onGround() || localPlayer.isInWater() || localPlayer.isInLava()) {
			return;
		}
		ClientPacketListener connection = mc.getConnection();
		if (connection == null) {
			return;
		}
		double x = localPlayer.getX();
		double y = localPlayer.getY();
		double z = localPlayer.getZ();
		boolean horizontalCollision = localPlayer.horizontalCollision;
		connection.send(new ServerboundMovePlayerPacket.Pos(x, y + 0.03125, z, false, horizontalCollision));
		connection.send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, horizontalCollision));
		connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, horizontalCollision));
	}

	/** Nuker: after the player destroys a block, destroys every reachable block in the radius box around it. */
	@Inject(method = "destroyBlock", at = @At("RETURN"))
	private void circlor4jNuker(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (!NukerModule.isActive() || circlor4j$nuking) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		if (level == null) {
			return;
		}

		int radius = NukerModule.getRadius();
		MultiPlayerGameMode gameMode = (MultiPlayerGameMode) (Object) this;
		circlor4j$nuking = true;
		try {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dy = -radius; dy <= radius; dy++) {
					for (int dz = -radius; dz <= radius; dz++) {
						if (dx == 0 && dy == 0 && dz == 0) {
							continue;
						}
						BlockPos target = pos.offset(dx, dy, dz);
						BlockState state = level.getBlockState(target);
						if (state.isAir() || state.getDestroySpeed(level, target) < 0.0F) {
							continue;
						}
						gameMode.startDestroyBlock(target, Direction.UP);
					}
				}
			}
		} finally {
			circlor4j$nuking = false;
		}
	}
}
