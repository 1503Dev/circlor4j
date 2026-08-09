package dev1503.circlor4j.client.module;

import dev1503.circlor4j.i18n.I18n;
import dev1503.circlor4j.ui.StatusManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
	/** Marker for a module option (a {@link DoubleSetting}, {@link DropdownSetting}, {@link ToggleSetting}, {@link ColorSetting} or {@link RangeSetting}). */
	public interface Setting {
	}

	/**
	 * A numeric option exposed to the UI as a Slider, bound to path "{id}/{option}".
	 * {@code parentOption} names the sub-toggle it should be nested under, if any.
	 */
	public record DoubleSetting(
		String path,
		String labelKey,
		String labelFallback,
		double min,
		double max,
		double step,
		double defaultValue,
		String showCondition,
		String hideCondition,
		String parentOption
	) implements Setting {
	}

	/**
	 * A range option exposed to the UI as a two-thumb RangeSlider, bound to paths
	 * "{id}/{option}/min" and "{id}/{option}/max".
	 */
	public record RangeSetting(
		String minPath,
		String maxPath,
		String labelKey,
		String labelFallback,
		double min,
		double max,
		double step,
		double defaultMin,
		double defaultMax
	) implements Setting {
	}

	/** An indexed option exposed to the UI as a Dropdown, bound to path "{id}/{option}". */
	public record DropdownSetting(
		String path,
		String labelKey,
		String labelFallback,
		String[] itemKeys,
		String[] itemFallbacks,
		int defaultIndex,
		String showCondition
	) implements Setting {
	}

	/**
	 * An on/off option exposed to the UI as a nested Toggle, bound to path "{id}/{option}/enabled".
	 * {@code parentOption} names the sub-toggle it should be nested under, if any.
	 */
	public record ToggleSetting(String path, String labelKey, String labelFallback, String parentOption) implements Setting {
	}

	/**
	 * An RGBA colour option exposed to the UI as a colour picker, bound to path "{id}/{option}/color".
	 * The stored value is a packed ARGB int. {@code parentOption} names the {@link ToggleSetting}
	 * sub-toggle the picker should be nested under. {@code showCondition} optionally gates visibility.
	 */
	public record ColorSetting(
		String path,
		String labelKey,
		String labelFallback,
		int defaultColor,
		String parentOption,
		String showCondition
	) implements Setting {
	}

	/**
	 * A block-list option exposed to the UI as a {@link dev1503.circlor4j.ui.component.BlockList}:
	 * a collapsible row that opens a popup for adding/toggling/removing individual blocks.
	 * Each block's enabled state is stored at "{path}/{blockId}" as a double (1.0 = enabled).
	 * {@code defaultBlockIds} seeds the list on first creation. {@code parentOption} names the
	 * sub-toggle the list should be nested under. {@code showCondition} optionally gates visibility.
	 */
	public record BlockListSetting(
		String path,
		String labelKey,
		String labelFallback,
		String[] defaultBlockIds,
		String parentOption,
		String showCondition
	) implements Setting {
	}

	/**
	 * A colour-editor option exposed to the UI as a {@link dev1503.circlor4j.ui.component.ColorList}:
	 * a collapsible row that opens a popup listing the blocks from {@code syncBlocksOption} (read-only,
	 * synced with that block list). Each item renders a coloured border and a left-click opens a colour
	 * picker while a right-click restores the default colour. Per-block colours are stored as packed
	 * ARGB ints at "{path}/{blockId}". {@code parentOption} names the sub-toggle to nest under.
	 */
	public record ColorListSetting(
		String path,
		String labelKey,
		String labelFallback,
		String syncBlocksOption,
		String parentOption,
		String showCondition
	) implements Setting {
	}

	private final StatusManager status;
	private final String id;
	private final String name;
	private final String description;
	private final ModuleCategory category;
	private final List<Setting> settings = new ArrayList<>();
	private boolean enabled;

	protected Module(StatusManager status, String id, String name, String description, ModuleCategory category) {
		this.status = status;
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		status.setValueOnly(this.id + "/enabled", 0.0);
	}

	public StatusManager getStatus() {
		return this.status;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return tr("module." + this.id + ".name", this.name);
	}

	/** The raw (English) module name, used by the Arraylist HUD. */
	public String getRawName() {
		return this.name;
	}

	public String getDescription() {
		return tr("module." + this.id + ".description", this.description);
	}

	public ModuleCategory getCategory() {
		return this.category;
	}

	/** Whether this module's toggle appears in the category window (false for hidden helper modules). */
	public boolean isShownInGui() {
		return true;
	}

	public String getEnabledPath() {
		return this.id + "/enabled";
	}

	/** Options in registration order (sliders and dropdowns interleaved as registered). */
	public List<Setting> getSettings() {
		return Collections.unmodifiableList(this.settings);
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void toggle() {
		this.status.setValue(this.id + "/enabled", this.enabled ? 0.0 : 1.0);
	}

	public void setEnabled(boolean enabled) {
		this.status.setValue(this.id + "/enabled", enabled ? 1.0 : 0.0);
	}

	protected void registerSlider(String option, String label, double min, double max, double step, double defaultValue) {
		this.registerSlider(option, label, min, max, step, defaultValue, null, null);
	}

	protected void registerSlider(String option, String label, double min, double max, double step, double defaultValue, String showCondition) {
		this.registerSlider(option, label, min, max, step, defaultValue, showCondition, null);
	}

	/** Registers a slider on path "{id}/{parentOption}/{option}", nested under the matching sub-toggle. */
	protected void registerSlider(String parentOption, String option, String label, double min, double max, double step, double defaultValue) {
		this.registerSlider(option, label, min, max, step, defaultValue, null, null, parentOption);
	}

	protected void registerSlider(
		String option,
		String label,
		double min,
		double max,
		double step,
		double defaultValue,
		String showCondition,
		String hideCondition
	) {
		this.registerSlider(option, label, min, max, step, defaultValue, showCondition, hideCondition, null);
	}

	private void registerSlider(
		String option,
		String label,
		double min,
		double max,
		double step,
		double defaultValue,
		String showCondition,
		String hideCondition,
		String parentOption
	) {
		String path = parentOption != null ? this.id + "/" + parentOption + "/" + option : this.id + "/" + option;
		double value = Math.max(min, Math.min(max, defaultValue));
		this.status.setValueOnly(path, value);
		this.settings.add(
			new DoubleSetting(path, "module." + this.id + "." + option + ".name", label, min, max, step, value, showCondition, hideCondition, parentOption)
		);
	}

	protected void registerDropdown(String option, String label, String[] items, int defaultIndex) {
		String[] keys = new String[items.length];
		this.registerDropdown(option, label, items, keys, defaultIndex);
	}

	protected void registerDropdown(String option, String label, String[] items, String[] itemKeys, int defaultIndex) {
		this.registerDropdown(option, label, items, itemKeys, defaultIndex, null);
	}

	protected void registerDropdown(String option, String label, String[] items, String[] itemKeys, int defaultIndex, String showCondition) {
		String path = this.id + "/" + option;
		int index = Math.max(0, Math.min(items.length - 1, defaultIndex));
		this.status.setValueOnly(path, index);
		String labelKey = "mode".equals(option) ? "mode" : "module." + this.id + "." + option + ".name";
		this.settings.add(new DropdownSetting(path, labelKey, label, itemKeys.clone(), items.clone(), index, showCondition));
	}

	/** Registers an on/off toggle as a sub-option on path "{id}/{option}/enabled" (e.g. "esp/mobs/enabled"). */
	protected void registerToggle(String option, String label) {
		this.registerToggle(option, label, false);
	}

	protected void registerToggle(String option, String label, boolean defaultEnabled) {
		String path = this.id + "/" + option + "/enabled";
		this.status.setValueOnly(path, defaultEnabled ? 1.0 : 0.0);
		this.settings.add(new ToggleSetting(path, "module." + this.id + "." + option + ".name", label, null));
	}

	/** Registers an on/off toggle on path "{id}/{parentOption}/{option}/enabled", nested under the matching sub-toggle. */
	protected void registerToggleIn(String parentOption, String option, String label) {
		this.registerToggleIn(parentOption, option, label, false);
	}

	protected void registerToggleIn(String parentOption, String option, String label, boolean defaultEnabled) {
		String path = this.id + "/" + parentOption + "/" + option + "/enabled";
		this.status.setValueOnly(path, defaultEnabled ? 1.0 : 0.0);
		this.settings.add(new ToggleSetting(path, "module." + this.id + "." + option + ".name", label, parentOption));
	}

	/** Registers a range slider on paths "{id}/{option}/min" and "{id}/{option}/max". */
	protected void registerRangeSlider(String option, String label, double min, double max, double step, double defaultMin, double defaultMax) {
		String minPath = this.id + "/" + option + "/min";
		String maxPath = this.id + "/" + option + "/max";
		double lo = Math.max(min, Math.min(max, Math.min(defaultMin, defaultMax)));
		double hi = Math.max(min, Math.min(max, Math.max(defaultMin, defaultMax)));
		this.status.setValueOnly(minPath, lo);
		this.status.setValueOnly(maxPath, hi);
		this.settings.add(new RangeSetting(minPath, maxPath, "module." + this.id + "." + option + ".name", label, min, max, step, lo, hi));
	}

	/** Registers a colour picker on path "{id}/{option}/color", nested under the matching sub-toggle. */
	protected void registerColor(String option, String label, int defaultColor) {
		this.registerColor(option, label, defaultColor, null);
	}

	protected void registerColor(String option, String label, int defaultColor, String showCondition) {
		String path = this.id + "/" + option + "/color";
		this.status.setValueOnly(path, defaultColor);
		this.settings.add(new ColorSetting(path, "module." + this.id + "." + option + ".color.name", label, defaultColor, option, showCondition));
	}

	/**
	 * Registers a block-list option on path "{id}/{option}". Each block's enabled state is stored at
	 * "{path}/{blockId}" as a double (1.0 = enabled). {@code defaultBlockIds} are seeded as enabled
	 * entries (only where no saved value already exists), so the feature works before the GUI is ever
	 * opened; the user's own edits then persist across launches. {@code parentOption} names the
	 * sub-toggle the list should be nested under. {@code showCondition} optionally gates visibility.
	 */
	protected void registerBlockList(String option, String label, String[] defaultBlockIds) {
		this.registerBlockList(option, label, defaultBlockIds, null, null);
	}

	protected void registerBlockList(String option, String label, String[] defaultBlockIds, String parentOption) {
		this.registerBlockList(option, label, defaultBlockIds, parentOption, null);
	}

	protected void registerBlockList(String option, String label, String[] defaultBlockIds, String parentOption, String showCondition) {
		String path = this.id + "/" + option;
		String prefix = path + "/";
		for (String blockId : defaultBlockIds) {
			if (!this.status.contains(prefix + blockId)) {
				this.status.setValueOnly(prefix + blockId, 1.0);
			}
		}
		this.settings.add(
			new BlockListSetting(path, "module." + this.id + "." + option + ".name", label, defaultBlockIds.clone(), parentOption, showCondition)
		);
	}

	protected void registerColorList(String option, String label, String syncBlocksOption) {
		this.registerColorList(option, label, syncBlocksOption, null, null);
	}

	protected void registerColorList(String option, String label, String syncBlocksOption, String parentOption) {
		this.registerColorList(option, label, syncBlocksOption, parentOption, null);
	}

	protected void registerColorList(String option, String label, String syncBlocksOption, String parentOption, String showCondition) {
		String path = this.id + "/" + option;
		this.settings.add(
			new ColorListSetting(path, "module." + this.id + "." + option + ".name", label, syncBlocksOption, parentOption, showCondition)
		);
	}

	/** Called by the StatusManager listener for any status path change that is not an enabled path. */
	public void onStatusChange(String path, double value) {
	}

	protected static String tr(String key, String fallback) {
		String value = I18n.t(key);
		return key.equals(value) ? fallback : value;
	}

	/**
	 * Applies an enabled-path status change. Called by the StatusManager listener,
	 * so {@link #onEnable()}/{@link #onDisable()} fire exactly once per flip.
	 */
	void applyEnabledStatus(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			if (enabled) {
				this.onEnable();
			} else {
				this.onDisable();
			}
		}
	}

	public void onEnable() {
	}

	public void onDisable() {
	}

	public void onTick() {
	}
}
