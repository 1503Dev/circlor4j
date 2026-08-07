package dev1503.circlor4j.client.module.modules;

import dev1503.circlor4j.client.module.Module;
import dev1503.circlor4j.client.module.ModuleCategory;
import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.StatusManager;

/**
 * Hidden helper module backing the root Language dropdown in the Circlor category.
 * Each dropdown option shows its own language's "language.name" (endonym).
 */
public class LanguageModule extends Module {
	public static final String OPTION_PATH = "language/option";
	public static final String[] LANG_IDS = {"en", "zh-hans"};
	public static final String[] LANG_FALLBACKS = {"English", "简体中文"};

	public LanguageModule(StatusManager status) {
		super(status, "language", "Language", "Switches the UI language", ModuleCategory.CIRCLOR);
	}

	@Override
	public boolean isShownInGui() {
		return false;
	}

	@Override
	public void onStatusChange(String path, double value) {
		if (path.equals(OPTION_PATH)) {
			I18n.lang = LANG_IDS[clampIndex((int) Math.round(value))];
		}
	}

	public static void applyFromStatus() {
		int index = (int) StatusManager.getInstance().getDouble(OPTION_PATH, 0);
		I18n.lang = LANG_IDS[clampIndex(index)];
	}

	public static int indexOfCurrentLanguage() {
		for (int i = 0; i < LANG_IDS.length; i++) {
			if (LANG_IDS[i].equals(I18n.lang)) {
				return i;
			}
		}
		return 0;
	}

	private static int clampIndex(int index) {
		return Math.max(0, Math.min(LANG_IDS.length - 1, index));
	}
}
