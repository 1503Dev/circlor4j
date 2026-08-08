package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class XrayModule extends Module {
	public static final String ID = "xray";
	private static final String RADIUS = "radius";

	public XrayModule(StatusManager status) {
		super(status, ID, "Xray", "Reveals ores through walls", ModuleCategory.RENDER);
		this.registerSlider(RADIUS, "Radius", 1.0, 6.0, 1.0, 2.0);
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	public static int getRadius() {
		return (int) StatusManager.getInstance().getDouble(ID + "/" + RADIUS, 2.0);
	}

	public static boolean isOre(BlockState state) {
		return getOreType(state) > 0;
	}

	public static int getOreType(BlockState state) {
		if (state.is(Blocks.COAL_ORE)) return 1;
		if (state.is(Blocks.DEEPSLATE_COAL_ORE)) return 2;
		if (state.is(Blocks.IRON_ORE)) return 3;
		if (state.is(Blocks.DEEPSLATE_IRON_ORE)) return 4;
		if (state.is(Blocks.GOLD_ORE)) return 5;
		if (state.is(Blocks.DEEPSLATE_GOLD_ORE)) return 6;
		if (state.is(Blocks.DIAMOND_ORE)) return 7;
		if (state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) return 8;
		if (state.is(Blocks.REDSTONE_ORE)) return 9;
		if (state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) return 10;
		if (state.is(Blocks.LAPIS_ORE)) return 11;
		if (state.is(Blocks.DEEPSLATE_LAPIS_ORE)) return 12;
		if (state.is(Blocks.EMERALD_ORE)) return 13;
		if (state.is(Blocks.DEEPSLATE_EMERALD_ORE)) return 14;
		if (state.is(Blocks.COPPER_ORE)) return 15;
		if (state.is(Blocks.DEEPSLATE_COPPER_ORE)) return 16;
		if (state.is(Blocks.NETHER_GOLD_ORE)) return 17;
		if (state.is(Blocks.NETHER_QUARTZ_ORE)) return 18;
		if (state.is(Blocks.ANCIENT_DEBRIS)) return 19;
		return 0;
	}

	public static int getOreColorByType(int oreType) {
		switch (oreType) {
			case 1: return 0xFF1A1A1A;
			case 2: return 0xFF2A2A2A;
			case 3: return 0xFFD4A574;
			case 4: return 0xFFC8B89A;
			case 5: return 0xFFFFD700;
			case 6: return 0xFFE8C84A;
			case 7: return 0xFF00E5E5;
			case 8: return 0xFF44DDDD;
			case 9: return 0xFFFF0000;
			case 10: return 0xFFDD2222;
			case 11: return 0xFF1E5AE0;
			case 12: return 0xFF3366CC;
			case 13: return 0xFF00CC22;
			case 14: return 0xFF33DD55;
			case 15: return 0xFFFF7722;
			case 16: return 0xFFDD8844;
			case 17: return 0xFFFFDD44;
			case 18: return 0xFFEEEEEE;
			case 19: return 0xFF8B5E3C;
			default: return 0xFFFFFFFF;
		}
	}
}
