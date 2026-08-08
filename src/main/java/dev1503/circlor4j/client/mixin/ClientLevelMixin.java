package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.module.modules.TrueSightModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

	@Inject(method = "animateTick", at = @At("TAIL"))
	private void circlor4jTrueSightBlocks(int xt, int yt, int zt, CallbackInfo ci) {
		if (!TrueSightModule.isBlocksEnabled()) {
			return;
		}
		ClientLevel level = (ClientLevel) (Object) this;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		BlockPos center = mc.player.blockPosition();
		int r = 16;
		RandomSource random = RandomSource.createThreadLocalInstance();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Block[] targets = {Blocks.BARRIER, Blocks.STRUCTURE_VOID, Blocks.LIGHT};

		for (int i = 0; i < 200; i++) {
			int x = center.getX() + random.nextInt(r) - random.nextInt(r);
			int y = center.getY() + random.nextInt(r) - random.nextInt(r);
			int z = center.getZ() + random.nextInt(r) - random.nextInt(r);
			pos.set(x, y, z);
			if (!level.isLoaded(pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			Block block = state.getBlock();
			for (Block target : targets) {
				if (block == target) {
					level.addParticle(
						new BlockParticleOption(ParticleTypes.BLOCK_MARKER, state),
						x + 0.5, y + 0.5, z + 0.5,
						0.0, 0.0, 0.0
					);
					break;
				}
			}
		}
	}
}
