package dev1503.circlor4j.ui.component;

public interface StatusWidget {
	String getPath();

	/** Called by the StatusManager to reflect a store value without re-notifying. */
	void syncStatus(double value);
}
