package dev1503.circlor4j.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import dev1503.circlor4j.client.config.ModStorage;
import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleManager;
import dev1503.circlor4j.client.module.modules.ClickGuiModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Holds the registered keybinds, dispatches key input by mode, and persists them as JSON. */
public final class KeyBindManager {
	private static final List<KeyBind> BINDS = new ArrayList<>();

	private KeyBindManager() {
	}

	/** Seeds the default ClickGUI bind when nothing is registered yet. */
	public static void init() {
		if (BINDS.isEmpty()) {
			BINDS.add(defaultClickGuiBind());
		}
	}

	private static KeyBind defaultClickGuiBind() {
		return new KeyBind(
			InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_RSHIFT),
			false,
			false,
			false,
			"clickgui",
			KeyBind.Mode.TOGGLE
		);
	}

	public static List<KeyBind> all() {
		return new ArrayList<>(BINDS);
	}

	public static void add(KeyBind bind) {
		BINDS.add(bind);
	}

	public static void remove(KeyBind bind) {
		BINDS.remove(bind);
	}

	/** Replaces an existing bind in place (or appends it if it is not present). */
	public static void update(KeyBind oldBind, KeyBind newBind) {
		int index = BINDS.indexOf(oldBind);
		if (index >= 0) {
			BINDS.set(index, newBind);
		} else {
			BINDS.add(newBind);
		}
	}

	/** Whether any other bind already uses the same combination. */
	public static boolean isUsed(KeyBind probe) {
		for (KeyBind bind : BINDS) {
			if (bind != probe && bind.matches(probe.getKey(), probe.hasShift(), probe.hasControl(), probe.hasAlt())) {
				return true;
			}
		}
		return false;
	}

	/** Whether the given function name maps to a known module (underscores/case-insensitive). */
	public static boolean isKnownFunction(String function) {
		return resolveFunction(function) != null;
	}

	/**
	 * Dispatches a raw keyboard event to every registered bind.
	 * While ANY screen is open (chat, inventory, ClickGUI, ...) only the ClickGUI
	 * toggle bind fires, so module keybinds never trigger outside the game screen.
	 * Returns true if the event was consumed.
	 */
	public static boolean onKeyInput(InputConstants.Key key, boolean shift, boolean ctrl, boolean alt, int action) {
		boolean inGui = Minecraft.getInstance().gui.screen() != null;
		boolean consumed = false;
		for (KeyBind bind : BINDS) {
			if (inGui && !isClickGuiBind(bind)) {
				continue;
			}
			if (action == GLFW.GLFW_RELEASE) {
				if (bind.matchesKey(key) && bind.isDown()) {
					if (bind.getMode() == KeyBind.Mode.HOLD) {
						setFunctionEnabled(bind.getFunction(), false);
					}
					bind.setDown(false);
					consumed = true;
				}
			} else if (action == GLFW.GLFW_PRESS) {
				if (!bind.matches(key, shift, ctrl, alt) || bind.isDown()) {
					continue;
				}
				bind.setDown(true);
				consumed = true;
				if (bind.getMode() == KeyBind.Mode.HOLD) {
					setFunctionEnabled(bind.getFunction(), true);
				} else {
					toggleFunction(bind.getFunction());
				}
			}
		}
		return consumed;
	}

	private static boolean isClickGuiBind(KeyBind bind) {
		Module module = resolveFunction(bind.getFunction());
		return module != null && ClickGuiModule.ID.equals(module.getId());
	}

	public static Module resolveFunction(String function) {
		if (function == null) {
			return null;
		}
		String normalized = function.replace("_", "");
		for (Module module : ModuleManager.all()) {
			if (module.getId().replace("_", "").equalsIgnoreCase(normalized)) {
				return module;
			}
		}
		return null;
	}

	private static void toggleFunction(String function) {
		Module module = resolveFunction(function);
		if (module != null) {
			module.toggle();
		}
	}

	private static void setFunctionEnabled(String function, boolean enabled) {
		Module module = resolveFunction(function);
		if (module != null) {
			module.setEnabled(enabled);
		}
	}

	// ---- persistence ----

	public static Path keybindsDir() {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.gameDirectory == null) {
			return null;
		}
		Path dir = mc.gameDirectory.toPath().resolve("circlor4j").resolve("keybinds");
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			// ignore
		}
		return dir;
	}

	/** Loads the persisted binds from default.json (called once the Minecraft instance exists). */
	public static void load() {
		Path dir = keybindsDir();
		if (dir == null) {
			return;
		}
		Path file = dir.resolve("default.json");
		if (Files.exists(file)) {
			replaceBinds(ModStorage.readKeybinds(file));
		}
		if (BINDS.isEmpty()) {
			BINDS.add(defaultClickGuiBind());
		}
		ensureClickGuiBind();
	}

	public static void loadFrom(Path file) {
		if (file == null || !Files.exists(file)) {
			return;
		}
		replaceBinds(ModStorage.readKeybinds(file));
		if (BINDS.isEmpty()) {
			BINDS.add(defaultClickGuiBind());
		}
		ensureClickGuiBind();
	}

	/**
	 * Ensures a valid bind opens the ClickGUI; if it is missing or invalid, binds it to Right
	 * Shift. Always writes the result back to default.json so the config stays valid.
	 */
	private static void ensureClickGuiBind() {
		boolean hasValid = false;
		for (KeyBind bind : BINDS) {
			Module module = resolveFunction(bind.getFunction());
			if (module != null
				&& ClickGuiModule.ID.equals(module.getId())
				&& bind.getKey() != null
				&& bind.getKey() != InputConstants.UNKNOWN) {
				hasValid = true;
				break;
			}
		}
		if (!hasValid) {
			BINDS.removeIf(bind -> {
				Module module = resolveFunction(bind.getFunction());
				return module != null && ClickGuiModule.ID.equals(module.getId());
			});
			BINDS.add(defaultClickGuiBind());
		}
		saveDefault();
	}

	public static void saveDefault() {
		Path dir = keybindsDir();
		if (dir != null) {
			saveTo(dir.resolve("default.json"));
		}
	}

	public static void saveTo(Path file) {
		List<KeyBind.Data> data = new ArrayList<>();
		for (KeyBind bind : BINDS) {
			data.add(bind.toData());
		}
		ModStorage.writeKeybinds(file, data);
	}

	private static void replaceBinds(List<KeyBind.Data> data) {
		if (data == null) {
			return;
		}
		List<KeyBind> loaded = new ArrayList<>();
		for (KeyBind.Data d : data) {
			KeyBind bind = KeyBind.fromData(d);
			if (bind != null) {
				loaded.add(bind);
			}
		}
		BINDS.clear();
		BINDS.addAll(loaded);
	}
}