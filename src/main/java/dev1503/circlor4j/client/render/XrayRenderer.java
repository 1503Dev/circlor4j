package dev1503.circlor4j.client.render;

import dev1503.circlor4j.client.module.modules.XrayModule;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Block;
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

	private record Outline(VoxelShape shape, int color) {
	}

	private static final List<Outline> cachedOutlines = new ArrayList<>();
	private static int cachedVersion = -1;
	private static int cachedCenterX;
	private static int cachedCenterY;
	private static int cachedCenterZ;
	private static int cachedRadius;
	private static int cachedVerticalRadius;

	private XrayRenderer() {
	}

	private static boolean isDirty(Minecraft mc, BlockPos center, int radius, int verticalRadius, int version) {
		return cachedVersion != version
			|| cachedRadius != radius
			|| cachedVerticalRadius != verticalRadius
			|| cachedCenterX != center.getX()
			|| cachedCenterY != center.getY()
			|| cachedCenterZ != center.getZ();
	}

	private static void rebuild(Minecraft mc, BlockPos center, int radius, int verticalRadius, int version) {
		cachedOutlines.clear();
		cachedVersion = version;
		cachedRadius = radius;
		cachedVerticalRadius = verticalRadius;
		cachedCenterX = center.getX();
		cachedCenterY = center.getY();
		cachedCenterZ = center.getZ();

		int chunkRadius = radius * 2 - 1;
		int halfBlocks = (chunkRadius * 16) / 2;
		int halfVerticalBlocks = (verticalRadius * 16) / 2;
		int minX = center.getX() - halfBlocks;
		int maxX = center.getX() + halfBlocks;
		int minZ = center.getZ() - halfBlocks;
		int maxZ = center.getZ() + halfBlocks;
		int minY = center.getY() - halfVerticalBlocks;
		int maxY = center.getY() + halfVerticalBlocks;

		LongSet orePositions = new LongOpenHashSet();
		Map<Long, Block> positionToBlock = new HashMap<>();

		int minChunkX = SectionPos.blockToSectionCoord(minX);
		int maxChunkX = SectionPos.blockToSectionCoord(maxX);
		int minChunkZ = SectionPos.blockToSectionCoord(minZ);
		int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);
		int minSectionY = minY >> 4;
		int maxSectionY = maxY >> 4;

		for (int cx = minChunkX; cx <= maxChunkX; cx++) {
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
				LevelChunk chunk = mc.level.getChunk(cx, cz);
				if (chunk == null) {
					continue;
				}
				int baseX = chunk.getPos().getMinBlockX();
				int baseZ = chunk.getPos().getMinBlockZ();
				for (int si = 0; si < chunk.getSections().length; si++) {
					int sectionY = mc.level.getSectionYFromSectionIndex(si);
					if (sectionY < minSectionY || sectionY > maxSectionY) {
						continue;
					}
					LevelChunkSection section = chunk.getSections()[si];
					if (section == null || section.hasOnlyAir()) {
						continue;
					}
					int baseY = sectionY * 16;
					int lyStart = sectionY == minSectionY ? (minY & 15) : 0;
					int lyEnd = sectionY == maxSectionY ? (maxY & 15) : 15;
					int lxStart = (cx == minChunkX) ? (minX - baseX) : 0;
					int lxEnd = (cx == maxChunkX) ? (maxX - baseX) : 15;
					int lzStart = (cz == minChunkZ) ? (minZ - baseZ) : 0;
					int lzEnd = (cz == maxChunkZ) ? (maxZ - baseZ) : 15;
					for (int lx = lxStart; lx <= lxEnd; lx++) {
						int wx = baseX + lx;
						for (int lz = lzStart; lz <= lzEnd; lz++) {
							int wz = baseZ + lz;
							for (int ly = lyStart; ly <= lyEnd; ly++) {
								int wy = baseY + ly;
								BlockState state = section.getBlockState(lx, ly, lz);
								if (XrayModule.isOre(state)) {
									long posLong = BlockPos.asLong(wx, wy, wz);
									orePositions.add(posLong);
									positionToBlock.put(posLong, state.getBlock());
								}
							}
						}
					}
				}
			}
		}

		LongSet visited = new LongOpenHashSet();

		for (long startLong : orePositions) {
			if (visited.contains(startLong)) {
				continue;
			}
			Block startBlock = positionToBlock.get(startLong);

			List<VoxelShape> blockShapes = new ArrayList<>();
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
				}

				for (int[] offset : NEIGHBORS) {
					long neighbor = BlockPos.asLong(pos.getX() + offset[0], pos.getY() + offset[1], pos.getZ() + offset[2]);
					if (orePositions.contains(neighbor) && !visited.contains(neighbor)) {
						if (positionToBlock.get(neighbor) == startBlock) {
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

			cachedOutlines.add(new Outline(merged, XrayModule.getBlockColor(startBlock)));
		}
	}

	public static void render(com.mojang.blaze3d.vertex.PoseStack poseStack,
							  net.minecraft.client.renderer.SubmitNodeCollector collector,
							  Vec3 cameraPos) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || !XrayModule.isActive()) {
			return;
		}
		int radius = XrayModule.getRadius();
		int verticalRadius = XrayModule.getVerticalRadius();
		int version = XrayModule.getBlockCacheVersion();
		BlockPos center = mc.player.blockPosition();

		if (isDirty(mc, center, radius, verticalRadius, version)) {
			rebuild(mc, center, radius, verticalRadius, version);
		}

		for (Outline outline : cachedOutlines) {
			collector.submitShapeOutline(poseStack, outline.shape, XrayRenderType.XRAY, outline.color, 2.0f, true);
		}
	}
}
