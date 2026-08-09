package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class XrayModule extends Module {
	public static final String ID = "xray";
	public static final String BLOCKS = "blocks";
	public static final String COLORS = "colors";
	private static final String RADIUS = "radius";
	private static final String VERTICAL_RADIUS = "vertical_radius";

	private static final String[] DEFAULT_ORES = {
		"minecraft:coal_ore",
		"minecraft:deepslate_coal_ore",
		"minecraft:iron_ore",
		"minecraft:deepslate_iron_ore",
		"minecraft:gold_ore",
		"minecraft:deepslate_gold_ore",
		"minecraft:diamond_ore",
		"minecraft:deepslate_diamond_ore",
		"minecraft:redstone_ore",
		"minecraft:deepslate_redstone_ore",
		"minecraft:lapis_ore",
		"minecraft:deepslate_lapis_ore",
		"minecraft:emerald_ore",
		"minecraft:deepslate_emerald_ore",
		"minecraft:copper_ore",
		"minecraft:deepslate_copper_ore",
		"minecraft:nether_gold_ore",
		"minecraft:nether_quartz_ore",
		"minecraft:ancient_debris"
	};

	private static final Map<String, Integer> DEFAULT_COLORS = new HashMap<>();

	static {
		DEFAULT_COLORS.put("minecraft:coal_ore", 0xFF1E1E1E);
		DEFAULT_COLORS.put("minecraft:deepslate_coal_ore", 0xFF1E1E1E);
		DEFAULT_COLORS.put("minecraft:iron_ore", 0xFFD8AF93);
		DEFAULT_COLORS.put("minecraft:deepslate_iron_ore", 0xFFD8AF93);
		DEFAULT_COLORS.put("minecraft:copper_ore", 0xFFC67E5B);
		DEFAULT_COLORS.put("minecraft:deepslate_copper_ore", 0xFFC67E5B);
		DEFAULT_COLORS.put("minecraft:gold_ore", 0xFFF0D05A);
		DEFAULT_COLORS.put("minecraft:deepslate_gold_ore", 0xFFF0D05A);
		DEFAULT_COLORS.put("minecraft:redstone_ore", 0xFFC53C3C);
		DEFAULT_COLORS.put("minecraft:deepslate_redstone_ore", 0xFFC53C3C);
		DEFAULT_COLORS.put("minecraft:lapis_ore", 0xFF1F4F8B);
		DEFAULT_COLORS.put("minecraft:deepslate_lapis_ore", 0xFF1F4F8B);
		DEFAULT_COLORS.put("minecraft:diamond_ore", 0xFF47E0E0);
		DEFAULT_COLORS.put("minecraft:deepslate_diamond_ore", 0xFF47E0E0);
		DEFAULT_COLORS.put("minecraft:emerald_ore", 0xFF3CBF6E);
		DEFAULT_COLORS.put("minecraft:deepslate_emerald_ore", 0xFF3CBF6E);
		DEFAULT_COLORS.put("minecraft:nether_quartz_ore", 0xFFE8DCCC);
		DEFAULT_COLORS.put("minecraft:nether_gold_ore", 0xFFE8DCCC);
		DEFAULT_COLORS.put("minecraft:ancient_debris", 0xFF5A4A3E);
	}

	private static final Set<Block> cachedEnabledBlocks = new HashSet<>();
	private static final Map<Block, Integer> cachedBlockColors = new HashMap<>();
	private static int cacheTick = 0;
	private static int cacheVersion = 0;

	public XrayModule(StatusManager status) {
		super(status, ID, "Xray", "Reveals ores through walls", ModuleCategory.RENDER);
		this.registerBlockList(BLOCKS, "Blocks", DEFAULT_ORES);
		this.registerColorList(COLORS, "Render Color", BLOCKS);
		this.registerSlider(RADIUS, "Radius", 1.0, 6.0, 1.0, 2.0);
		this.registerSlider(VERTICAL_RADIUS, "Vertical Radius", 1.0, 12.0, 1.0, 8.0);
	}

	@Override
	public void onEnable() {
		this.refreshBlockCache();
		this.refreshColorCache();
		cacheTick = 0;
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static int getRadius() {
		return (int) StatusManager.getInstance().getDouble(ID + "/" + RADIUS, 2.0);
	}

	public static int getVerticalRadius() {
		return (int) StatusManager.getInstance().getDouble(ID + "/" + VERTICAL_RADIUS, 8.0);
	}

	private static String blocksPrefix() {
		return ID + "/" + BLOCKS + "/";
	}

	private static String colorPath(String blockId) {
		return ID + "/" + COLORS + "/" + blockId;
	}

	public static void refreshBlockCache() {
		Set<Block> previous = new HashSet<>(cachedEnabledBlocks);
		cachedEnabledBlocks.clear();
		StatusManager status = StatusManager.getInstance();
		Map<String, Double> entries = status.entriesWithPrefix(blocksPrefix());
		for (Map.Entry<String, Double> entry : entries.entrySet()) {
			if (entry.getValue() < 1.0) {
				continue;
			}
			String blockId = entry.getKey().substring(blocksPrefix().length());
			Block block = BuiltInRegistries.BLOCK.getValue(Identifier.tryParse(blockId));
			if (block != null && block != Blocks.AIR) {
				cachedEnabledBlocks.add(block);
			}
		}
		if (!cachedEnabledBlocks.equals(previous)) {
			cacheVersion++;
		}
	}

	public static void refreshColorCache() {
		Map<Block, Integer> previous = new HashMap<>(cachedBlockColors);
		cachedBlockColors.clear();
		for (Block block : cachedEnabledBlocks) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			String key = id.toString();
			int defaultColor = DEFAULT_COLORS.getOrDefault(key, 0xFFFFFFFF);
			int color = StatusManager.getInstance().getInt(colorPath(key), defaultColor);
			cachedBlockColors.put(block, color);
		}
		if (!cachedBlockColors.equals(previous)) {
			cacheVersion++;
		}
	}

	public static int getBlockCacheVersion() {
		return cacheVersion;
	}

	@Override
	public void onTick() {
		if (!isActive()) {
			return;
		}
		cacheTick++;
		if (cacheTick % 2 == 0) {
			refreshBlockCache();
			refreshColorCache();
		}
	}

	public static boolean isOre(BlockState state) {
		return cachedEnabledBlocks.contains(state.getBlock());
	}

	public static int getBlockColor(Block block) {
		Integer color = cachedBlockColors.get(block);
		if (color != null) {
			return color;
		}
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		return DEFAULT_COLORS.getOrDefault(id.toString(), 0xFFFFFFFF);
	}

	public static int defaultColorFor(String blockId) {
		return DEFAULT_COLORS.getOrDefault(blockId, 0xFFFFFFFF);
	}
}
