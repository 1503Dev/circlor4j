package dev1503.circlor4j.client.module;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();

	private ModuleManager() {
	}

	public static Module register(Module module) {
		MODULES.add(module);
		return module;
	}

	public static List<Module> all() {
		return new ArrayList<>(MODULES);
	}

	public static List<Module> byCategory(ModuleCategory category) {
		List<Module> result = new ArrayList<>();
		for (Module module : MODULES) {
			if (module.getCategory() == category) {
				result.add(module);
			}
		}
		return result;
	}

	public static Module get(String id) {
		for (Module module : MODULES) {
			if (module.getId().equalsIgnoreCase(id)) {
				return module;
			}
		}
		return null;
	}

	/** StatusManager listener entry point. Enabled-paths flip modules; other paths go to onStatusChange. */
	public static void applyStatus(String path, double value) {
		for (Module module : MODULES) {
			if (path.equals(module.getEnabledPath())) {
				module.applyEnabledStatus(value >= 1.0);
			} else {
				module.onStatusChange(path, value);
			}
		}
	}

	/** Applies persisted enabled states (loaded into the store) to the modules. */
	public static void syncEnabledFromStatus() {
		for (Module module : MODULES) {
			boolean enabled = module.getStatus().getBoolean(module.getEnabledPath(), false);
			if (enabled != module.isEnabled()) {
				module.applyEnabledStatus(enabled);
			}
		}
	}

	public static void tick() {
		for (Module module : MODULES) {
			if (module.isEnabled()) {
				module.onTick();
			}
		}
	}
}
