package dev1503.circlor4j.client.module.modules;

import com.mojang.authlib.GameProfile;
import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.ui.StatusManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * AntiBot identifies fake players ("bots") spawned by practice/anti-cheat servers so that every
 * other module can skip them through {@link #isBot(Entity)}. Detection is modelled after the
 * LiquidBounce module of the same name (Custom + Horizon modes), adapted to this client:
 * <ul>
 *   <li>LiteralNPC - the player has no entry in the online player list at all.</li>
 *   <li>NotInTabList - the player is not present in the currently listed (tab) players.</li>
 *   <li>Custom mode - configurable conditions: Duplicate, NoGameMode, IllegalPitch,
 *       FakeEntityID, IllegalHealth, InvalidGround, Age and Name.</li>
 *   <li>Horizon mode - players added to the player list without a game mode, or players
 *       that never show up in the tab list.</li>
 * </ul>
 */
public class AntiBotModule extends Module {
	public static final String ID = "antibot";
	private static final String MODE = "mode";
	private static final int MODE_CUSTOM = 0;
	private static final int MODE_HORIZON = 1;
	private static final String LITERAL_NPC = "literal_npc";
	private static final String NOT_IN_TAB_LIST = "not_in_tab_list";
	private static final String DUPLICATE = "duplicate";
	private static final String NO_GAME_MODE = "no_game_mode";
	private static final String ILLEGAL_PITCH = "illegal_pitch";
	private static final String FAKE_ID = "fake_id";
	private static final String ILLEGAL_HEALTH = "illegal_health";
	private static final String INVALID_GROUND = "invalid_ground";
	private static final String INVALID_GROUND_VL = "vl";
	private static final String AGE = "age";
	private static final String AGE_TICKS = "min_ticks";
	private static final String NAME = "name";
	private static final int MAX_VALID_ENTITY_ID = 1_000_000_000;
	private static final double Y_EPSILON = 1.0E-4;

	private static AntiBotModule instance;

	private final Set<UUID> horizonBots = new HashSet<>();
	private final Map<Integer, Integer> invalidGroundVls = new HashMap<>();
	private final Map<Integer, Double> lastY = new HashMap<>();

	public AntiBotModule(StatusManager status) {
		super(status, ID, "AntiBot", "Filters out fake players (bots)", ModuleCategory.MISC);
		this.registerDropdown(
			MODE,
			"Mode",
			new String[] {"Custom", "Horizon"},
			new String[] {"module.antibot.mode.custom.name", "module.antibot.mode.horizon.name"},
			MODE_CUSTOM
		);

		this.registerToggle(LITERAL_NPC, "LiteralNPC");
		this.registerToggle(NOT_IN_TAB_LIST, "NotInTabList");

		String customMode = ID + "/" + MODE + " == " + MODE_CUSTOM;
		this.registerToggle(DUPLICATE, "Duplicate", false, customMode);
		this.registerToggle(NO_GAME_MODE, "NoGameMode", false, customMode);
		this.registerToggle(ILLEGAL_PITCH, "IllegalPitch", false, customMode);
		this.registerToggle(FAKE_ID, "FakeEntityID", false, customMode);
		this.registerToggle(ILLEGAL_HEALTH, "IllegalHealth", false, customMode);
		this.registerToggle(INVALID_GROUND, "InvalidGround", false, customMode);
		this.registerSlider(INVALID_GROUND, INVALID_GROUND_VL, "VL", 1.0, 50.0, 1.0, 10.0);
		this.registerToggle(AGE, "Age", false, customMode);
		this.registerSlider(AGE, AGE_TICKS, "Min Ticks", 1.0, 120.0, 1.0, 20.0);
		this.registerToggle(NAME, "Name", false, customMode);

		AntiBotModule.instance = this;
	}

	public static boolean isActive() {
		return StatusManager.getInstance().getBoolean(ID + "/enabled", false);
	}

	/** Whether the given entity is currently considered a bot (module must be enabled). */
	public static boolean isBot(Entity entity) {
		AntiBotModule inst = AntiBotModule.instance;
		if (inst == null || !isActive() || !(entity instanceof Player player)) {
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || player == mc.player) {
			return false;
		}
		StatusManager status = StatusManager.getInstance();
		if (status.getBoolean(ID + "/" + LITERAL_NPC + "/enabled", false) && inst.isLiteralNpc(player)) {
			return true;
		}
		if (status.getBoolean(ID + "/" + NOT_IN_TAB_LIST + "/enabled", false) && inst.isMissingFromTabList(player)) {
			return true;
		}
		if ((int) status.getDouble(ID + "/" + MODE, MODE_CUSTOM) == MODE_HORIZON) {
			return inst.horizonBots.contains(player.getUUID()) || inst.isMissingFromTabList(player);
		}
		return inst.isCustomBot(status, player);
	}

	/** Called from the ClientPacketListener mixin for every player-info update packet. */
	public static void onPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket packet) {
		AntiBotModule inst = AntiBotModule.instance;
		if (inst == null || !isActive()) {
			return;
		}
		if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
			return;
		}
		for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
			if (entry.gameMode() == GameType.DEFAULT_MODE) {
				inst.horizonBots.add(entry.profileId());
			}
		}
	}

	/** Called from the ClientPacketListener mixin for every player-info remove packet. */
	public static void onPlayerInfoRemove(ClientboundPlayerInfoRemovePacket packet) {
		AntiBotModule inst = AntiBotModule.instance;
		if (inst == null) {
			return;
		}
		inst.horizonBots.removeAll(packet.profileIds());
	}

	@Override
	public void onDisable() {
		this.horizonBots.clear();
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			return;
		}
		if (mc.getConnection() == null) {
			this.horizonBots.clear();
		}
		boolean trackGround = this.getStatus().getBoolean(ID + "/" + INVALID_GROUND + "/enabled", false);

		Set<Integer> present = new HashSet<>();
		for (Entity entity : mc.level.entitiesForRendering()) {
			if (!(entity instanceof Player) || entity == mc.player) {
				continue;
			}
			int id = entity.getId();
			present.add(id);
			if (trackGround) {
				double y = entity.getY();
				double prevY = this.lastY.getOrDefault(id, y);
				int vl = this.invalidGroundVls.getOrDefault(id, 0);
				if (entity.onGround()) {
					if (Math.abs(prevY - y) > Y_EPSILON) {
						this.invalidGroundVls.put(id, vl + 1);
					}
				} else if (vl > 0) {
					int decayed = vl / 2;
					if (decayed > 0) {
						this.invalidGroundVls.put(id, decayed);
					} else {
						this.invalidGroundVls.remove(id);
					}
				}
				this.lastY.put(id, y);
			}
		}
		this.invalidGroundVls.keySet().removeIf(id -> !present.contains(id));
		this.lastY.keySet().removeIf(id -> !present.contains(id));
	}

	private boolean isCustomBot(StatusManager status, Player player) {
		if (status.getBoolean(ID + "/" + DUPLICATE + "/enabled", false) && this.isADuplicate(player.getGameProfile())) {
			return true;
		}
		if (status.getBoolean(ID + "/" + NO_GAME_MODE + "/enabled", false) && this.getPlayerInfo(player) == null) {
			return true;
		}
		if (status.getBoolean(ID + "/" + ILLEGAL_PITCH + "/enabled", false) && Math.abs(player.getXRot()) > 90.0F) {
			return true;
		}
		if (status.getBoolean(ID + "/" + FAKE_ID + "/enabled", false)
			&& (player.getId() < 0 || player.getId() > MAX_VALID_ENTITY_ID)) {
			return true;
		}
		if (status.getBoolean(ID + "/" + ILLEGAL_HEALTH + "/enabled", false)) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null && player.getHealth() > mc.player.getMaxHealth()) {
				return true;
			}
		}
		if (status.getBoolean(ID + "/" + INVALID_GROUND + "/enabled", false)
			&& this.invalidGroundVls.getOrDefault(player.getId(), 0)
				>= (int) status.getDouble(ID + "/" + INVALID_GROUND + "/" + INVALID_GROUND_VL, 10.0)) {
			return true;
		}
		if (status.getBoolean(ID + "/" + AGE + "/enabled", false)
			&& player.tickCount < (int) status.getDouble(ID + "/" + AGE + "/" + AGE_TICKS, 20.0)) {
			return true;
		}
		if (status.getBoolean(ID + "/" + NAME + "/enabled", false) && hasInvalidName(player)) {
			return true;
		}
		return false;
	}

	/** A second player with the same name but a different UUID is present in the online list. */
	private boolean isADuplicate(GameProfile profile) {
		ClientPacketListener connection = getConnection();
		if (connection == null) {
			return false;
		}
		int matches = 0;
		for (PlayerInfo info : connection.getOnlinePlayers()) {
			if (Objects.equals(info.getProfile().name(), profile.name())
				&& !Objects.equals(info.getProfile().id(), profile.id())) {
				matches++;
			}
		}
		return matches == 1;
	}

	private PlayerInfo getPlayerInfo(Player player) {
		ClientPacketListener connection = getConnection();
		return connection == null ? null : connection.getPlayerInfo(player.getUUID());
	}

	/** The player has no entry in the online player list (playerInfoMap) at all. */
	private boolean isLiteralNpc(Player player) {
		ClientPacketListener connection = getConnection();
		return connection != null && !connection.getOnlinePlayerIds().contains(player.getUUID());
	}

	/** The player is not present in the currently listed (tab) players. */
	private boolean isMissingFromTabList(Player player) {
		ClientPacketListener connection = getConnection();
		if (connection == null) {
			return false;
		}
		for (PlayerInfo info : connection.getListedOnlinePlayers()) {
			if (info.getProfile().id().equals(player.getUUID())) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasInvalidName(Player player) {
		String name = player.getName().getString();
		if (name.length() < 3 || name.length() > 16) {
			return true;
		}
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if ((c < '0' || c > '9') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && c != '_') {
				return true;
			}
		}
		return false;
	}

	private static ClientPacketListener getConnection() {
		return Minecraft.getInstance().getConnection();
	}
}