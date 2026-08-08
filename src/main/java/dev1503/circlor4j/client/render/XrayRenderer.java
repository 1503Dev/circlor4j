package dev1503.circlor4j.client.render;

import dev1503.circlor4j.client.module.modules.XrayModule;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class XrayRenderer {

	private static final int[][] NEIGHBORS = {
		{1, 0, 0}, {-1, 0, 0},
		{0, 1, 0}, {0, -1, 0},
		{0, 0, 1}, {0, 0, -1}
	};

	private XrayRenderer() {
	}

	public static void render(com.mojang.blaze3d.vertex.PoseStack poseStack,
							  net.minecraft.client.renderer.SubmitNodeCollector collector,
							  Vec3 cameraPos) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			return;
		}
		int radius = XrayModule.getRadius();
		int chunkRadius = radius * 2 - 1;
		int halfBlocks = (chunkRadius * 16) / 2;
		BlockPos center = mc.player.blockPosition();
		int minX = center.getX() - halfBlocks;
		int maxX = center.getX() + halfBlocks;
		int minZ = center.getZ() - halfBlocks;
		int maxZ = center.getZ() + halfBlocks;
		int minY = mc.level.getMinY();
		int maxY = mc.level.getMaxY();

		LongSet orePositions = new LongOpenHashSet();
		Map<Long, Integer> positionToOreType = new HashMap<>();

		int minChunkX = SectionPos.blockToSectionCoord(minX);
		int maxChunkX = SectionPos.blockToSectionCoord(maxX);
		int minChunkZ = SectionPos.blockToSectionCoord(minZ);
		int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);

		for (int cx = minChunkX; cx <= maxChunkX; cx++) {
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
				LevelChunk chunk = mc.level.getChunk(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (int si = 0; si < chunk.getSections().length; si++) {
					LevelChunkSection section = chunk.getSections()[si];
					if (section == null || section.hasOnlyAir()) {
						continue;
					}
					int baseY = mc.level.getSectionYFromSectionIndex(si) * 16;
					int baseX = chunk.getPos().getMinBlockX();
					int baseZ = chunk.getPos().getMinBlockZ();
					for (int lx = 0; lx < 16; lx++) {
						for (int lz = 0; lz < 16; lz++) {
							for (int ly = 0; ly < 16; ly++) {
								int wx = baseX + lx;
								int wy = baseY + ly;
								int wz = baseZ + lz;
								if (wx < minX || wx > maxX || wz < minZ || wz > maxZ || wy < minY || wy > maxY) {
									continue;
								}
								BlockState state = section.getBlockState(lx, ly, lz);
								if (XrayModule.isOre(state)) {
									long posLong = BlockPos.asLong(wx, wy, wz);
									orePositions.add(posLong);
									positionToOreType.put(posLong, XrayModule.getOreType(state));
								}
							}
						}
					}
				}
			}
		}

		LongSet visited = new LongOpenHashSet();
		LongSet used = new LongOpenHashSet();

		for (long startLong : orePositions) {
			if (visited.contains(startLong)) {
				continue;
			}
			int startOreType = positionToOreType.get(startLong);

			List<VoxelShape> blockShapes = new ArrayList<>();
			List<Long> groupBlocks = new ArrayList<>();
			Deque<Long> queue = new ArrayDeque<>();
			queue.add(startLong);
			visited.add(startLong);

			while (!queue.isEmpty()) {
				long currentLong = queue.poll();
				BlockPos pos = BlockPos.of(currentLong);

				boolean hasExposedFace = false;
				for (int[] offset : NEIGHBORS) {
					long neighbor = BlockPos.asLong(pos.getX() + offset[0], pos.getY() + offset[1], pos.getZ() + offset[2]);
					if (!orePositions.contains(neighbor)) {
						hasExposedFace = true;
						break;
					}
				}

				if (hasExposedFace) {
					blockShapes.add(Shapes.box(pos.getX(), pos.getY(), pos.getZ(),
						pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
					used.add(currentLong);
				}
				groupBlocks.add(currentLong);

				for (int[] offset : NEIGHBORS) {
					long neighbor = BlockPos.asLong(pos.getX() + offset[0], pos.getY() + offset[1], pos.getZ() + offset[2]);
					if (orePositions.contains(neighbor) && !visited.contains(neighbor)) {
						if (positionToOreType.get(neighbor) == startOreType) {
							visited.add(neighbor);
							queue.add(neighbor);
						}
					}
				}
			}

			if (blockShapes.isEmpty()) {
				continue;
			}

			VoxelShape merged = blockShapes.get(0);
			for (int i = 1; i < blockShapes.size(); i++) {
				merged = Shapes.joinUnoptimized(merged, blockShapes.get(i), net.minecraft.world.phys.shapes.BooleanOp.OR);
			}

			int color = XrayModule.getOreColorByType(startOreType);
			collector.submitShapeOutline(poseStack, merged, XrayRenderType.XRAY, color, 2.0f, true);
		}
	}
}
