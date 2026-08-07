package dev1503.circlor4j.i18n;

import java.util.Map;

public class I18n {
	public static final String DEFAULT_LANG = "en";
	public static String lang = DEFAULT_LANG;

	/** Minecraft first, then the current language, then the default language, finally the key. */
	public static String t(String key, Object... formats) {
		String mcKey = "circlor4j." + key;
		String mcValue = net.minecraft.client.resources.language.I18n.get(mcKey, formats);
		if (!mcKey.equals(mcValue)) {
			return mcValue;
		}
		String value = lookup(lang, key, formats);
		if (value != null) {
			return value;
		}
		value = lookup(DEFAULT_LANG, key, formats);
		return value != null ? value : key;
	}

	/** Looks up a key in a specific language's table (its own name), falling back to the default language. */
	public static String tIn(String lang, String key, Object... formats) {
		String value = lookup(lang, key, formats);
		if (value != null) {
			return value;
		}
		value = lookup(DEFAULT_LANG, key, formats);
		return value != null ? value : key;
	}

	private static String lookup(String lang, String key, Object... formats) {
		Map<String, String> table = Lang.languages.get(lang);
		if (table != null && table.containsKey(key)) {
			String text = table.get(key);
			if (text == null || text.isEmpty()) {
				return null; // empty value means not translated yet
			}
			if (formats != null && formats.length > 0) {
				text = String.format(text, formats);
			}
			return text;
		}
		return null;
	}
}
