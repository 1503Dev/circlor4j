package dev1503.circlor4j.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Locale;

/** A keybind: a key combination (with optional modifiers), a target function, and a mode. */
public class KeyBind {
	public enum Mode { TOGGLE, HOLD }

	private InputConstants.Key key;
	private boolean shift;
	private boolean ctrl;
	private boolean alt;
	private String function;
	private Mode mode;
	private boolean down;

	public KeyBind(InputConstants.Key key, boolean shift, boolean ctrl, boolean alt, String function, Mode mode) {
		this.key = key;
		this.shift = shift;
		this.ctrl = ctrl;
		this.alt = alt;
		this.function = function;
		this.mode = mode;
	}

	public InputConstants.Key getKey() {
		return this.key;
	}

	public boolean hasShift() {
		return this.shift;
	}

	public boolean hasControl() {
		return this.ctrl;
	}

	public boolean hasAlt() {
		return this.alt;
	}

	public String getFunction() {
		return this.function;
	}

	public Mode getMode() {
		return this.mode;
	}

	public boolean isDown() {
		return this.down;
	}

	public void setDown(boolean down) {
		this.down = down;
	}

	/** Matches by base key only (used for release handling). */
	public boolean matchesKey(InputConstants.Key key) {
		return this.key.equals(key);
	}

	/** Matches the full combination. A modifier bit is ignored when the bound key is that modifier itself. */
	public boolean matches(InputConstants.Key key, boolean shift, boolean ctrl, boolean alt) {
		if (!this.key.equals(key)) {
			return false;
		}
		boolean shiftOk = isShiftKey(this.key) || this.shift == shift;
		boolean ctrlOk = isControlKey(this.key) || this.ctrl == ctrl;
		boolean altOk = isAltKey(this.key) || this.alt == alt;
		return shiftOk && ctrlOk && altOk;
	}

	private static boolean isShiftKey(InputConstants.Key key) {
		int v = key.getValue();
		return v == InputConstants.KEY_LSHIFT || v == InputConstants.KEY_RSHIFT;
	}

	private static boolean isControlKey(InputConstants.Key key) {
		int v = key.getValue();
		return v == InputConstants.KEY_LCONTROL || v == InputConstants.KEY_RCONTROL;
	}

	private static boolean isAltKey(InputConstants.Key key) {
		int v = key.getValue();
		return v == InputConstants.KEY_LALT || v == InputConstants.KEY_RALT;
	}

	/** Human-readable combination, e.g. "Ctrl+Shift+K". */
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		if (this.ctrl) {
			sb.append("Ctrl+");
		}
		if (this.alt) {
			sb.append("Alt+");
		}
		if (this.shift) {
			sb.append("Shift+");
		}
		sb.append(this.key.getName());
		return sb.toString();
	}

	public String getModeName() {
		return this.mode.name().toLowerCase(Locale.ROOT);
	}

	/** Serialization form. */
	public record Data(String key, boolean shift, boolean ctrl, boolean alt, String function, String mode) {
	}

	public Data toData() {
		return new Data(this.key.getName(), this.shift, this.ctrl, this.alt, this.function, this.getModeName());
	}

	public static KeyBind fromData(Data data) {
		if (data == null || data.key() == null || data.function() == null || data.function().isBlank()) {
			return null;
		}
		try {
			InputConstants.Key key = InputConstants.getKey(data.key());
			Mode mode = "hold".equalsIgnoreCase(data.mode()) ? Mode.HOLD : Mode.TOGGLE;
			return new KeyBind(key, data.shift(), data.ctrl(), data.alt(), data.function(), mode);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}