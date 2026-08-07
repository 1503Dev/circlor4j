package dev1503.circlor4j.ui;

import dev1503.circlor4j.client.config.ModStorage;
import dev1503.circlor4j.ui.component.StatusWidget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for UI-bound feature values, keyed by path
 * (e.g. "sprint/enabled", "sprint/speed"). UI widgets and feature logic
 * both read/write through here and stay in sync by path.
 */
public final class StatusManager {
	private static final StatusManager INSTANCE = new StatusManager();

	public static StatusManager getInstance() {
		return INSTANCE;
	}

	private final Map<String, Double> store = new HashMap<>();
	private final List<StatusWidget> widgets = new ArrayList<>();
	private Listener listener;

	private StatusManager() {
	}

	public void addWidget(StatusWidget widget) {
		this.widgets.add(widget);
	}

	public void clearWidgets() {
		this.widgets.clear();
	}

	/**
	 * Stores a value, syncs every other widget bound to the same path,
	 * then notifies the listener. The source widget updates itself on user input.
	 */
	public void setValue(StatusWidget source, String path, double value) {
		this.store.put(path, value);
		for (StatusWidget widget : this.widgets) {
			if (widget == source || !path.equals(widget.getPath())) {
				continue;
			}
			widget.syncStatus(value);
		}
		if (this.listener != null) {
			this.listener.onValueChange(path, value);
		}
	}

	public void setValue(String path, double value) {
		this.setValue(null, path, value);
	}

	public void setValueOnly(String path, double value) {
		this.store.put(path, value);
	}

	/** Loads the persisted store without notifying listeners. */
	public void load() {
		Map<String, Double> saved = ModStorage.loadStatus();
		if (saved != null) {
			this.store.putAll(saved);
		}
	}

	public void save() {
		ModStorage.saveStatus(new HashMap<>(this.store));
	}

	public void trigger(String path) {
		if (this.listener != null && path != null) {
			this.listener.onActionTrigger(path);
		}
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public double getDouble(String path, double defaultValue) {
		Double value = this.store.get(path);
		return value != null ? value : defaultValue;
	}

	public double getDouble(String path) {
		return this.getDouble(path, 0.0);
	}

	public int getInt(String path, int defaultValue) {
		return (int) this.getDouble(path, defaultValue);
	}

	public int getInt(String path) {
		return this.getInt(path, 0);
	}

	public boolean getBoolean(String path, boolean defaultValue) {
		return this.getDouble(path, defaultValue ? 1.0 : 0.0) >= 1.0;
	}

	public boolean getBoolean(String path) {
		return this.getBoolean(path, false);
	}

	public float getFloat(String path, float defaultValue) {
		return (float) this.getDouble(path, defaultValue);
	}

	public float getFloat(String path) {
		return this.getFloat(path, 0.0F);
	}

	public interface Listener {
		void onValueChange(String path, double value);

		default void onActionTrigger(String path) {
		}
	}
}
