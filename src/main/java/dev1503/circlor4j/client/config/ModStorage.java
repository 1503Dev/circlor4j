package dev1503.circlor4j.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import dev1503.circlor4j.client.keybind.KeyBind;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;

/** Files under <mc run dir>/circlor4j. */
public final class ModStorage {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type MAP_STRING_DOUBLE = new TypeToken<LinkedHashMap<String, Double>>() {}.getType();
	private static final Type MAP_STRING_LAYOUT = new TypeToken<LinkedHashMap<String, WindowLayout>>() {}.getType();
	private static final Type LIST_KEYBIND_DATA = new TypeToken<List<KeyBind.Data>>() {}.getType();

	public record WindowLayout(int x, int y, int width, int height, boolean collapsed, List<String> expanded) {
		public WindowLayout(int x, int y, int width, int height, boolean collapsed) {
			this(x, y, width, height, collapsed, null);
		}
	}

	private ModStorage() {
	}

	private static Path path(String relative) {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.gameDirectory == null) {
			return null;
		}
		return mc.gameDirectory.toPath().resolve("circlor4j").resolve(relative);
	}

	public static Map<String, WindowLayout> loadLayout() {
		return read("_layout.json", MAP_STRING_LAYOUT);
	}

	public static void saveLayout(Map<String, WindowLayout> layout) {
		write("_layout.json", layout);
	}

	public static List<KeyBind.Data> readKeybinds(Path file) {
		if (file == null || !Files.exists(file)) {
			return null;
		}
		try {
			return GSON.fromJson(Files.readString(file), LIST_KEYBIND_DATA);
		} catch (IOException | JsonParseException e) {
			return null;
		}
	}

	public static void writeKeybinds(Path file, List<KeyBind.Data> keybinds) {
		if (file == null) {
			return;
		}
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(keybinds));
		} catch (IOException e) {
			// ignore write failures
		}
	}

	public static Map<String, Double> loadStatus() {
		return read("status/default.json", MAP_STRING_DOUBLE);
	}

	public static void saveStatus(Map<String, Double> status) {
		write("status/default.json", status);
	}

	private static <T> T read(String relative, Type type) {
		Path file = path(relative);
		if (file == null || !Files.exists(file)) {
			return null;
		}
		try {
			return GSON.fromJson(Files.readString(file), type);
		} catch (IOException | JsonParseException e) {
			return null;
		}
	}

	private static void write(String relative, Object data) {
		Path file = path(relative);
		if (file == null) {
			return;
		}
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(data));
		} catch (IOException e) {
			// ignore write failures
		}
	}
}
