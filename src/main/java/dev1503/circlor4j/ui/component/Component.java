package dev1503.circlor4j.ui.component;

import dev1503.circlor4j.ui.StatusManager;

/**
 * Base class for UI components. Supports optional show/hide conditions evaluated
 * against the StatusManager store, e.g. "full_bright/mode == 1".
 */
public abstract class Component implements StatusWidget {
	private final String path;
	private String showCondition;
	private String hideCondition;

	protected Component(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	public void setShowCondition(String showCondition) {
		this.showCondition = showCondition;
	}

	public void setHideCondition(String hideCondition) {
		this.hideCondition = hideCondition;
	}

	public boolean isVisible(StatusManager status) {
		if (this.showCondition != null && !Condition.evaluate(status, this.showCondition)) {
			return false;
		}
		if (this.hideCondition != null && Condition.evaluate(status, this.hideCondition)) {
			return false;
		}
		return true;
	}
}
