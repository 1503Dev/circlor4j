package dev1503.circlor4j.client.module;

import dev1503.circlor4j.i18n.I18n;
import java.util.Locale;

public enum ModuleCategory {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	RENDER("Render"),
	PLAYER("Player"),
	MISC("Misc"),
	CIRCLOR("Circlor");

	private final String displayName;

	ModuleCategory(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getLocalizedName() {
		String key = "category." + this.name().toLowerCase(Locale.ROOT) + ".name";
		String value = I18n.t(key);
		return key.equals(value) ? this.displayName : value;
	}
}
