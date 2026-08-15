package dev1503.circlor4j.ui.component;

/**
 * A labelled button that runs a fixed action when pressed.
 */
public class TextButton extends Button {
	private final Runnable action;

	public TextButton(String label, int x, int y, int width, int height, Runnable action) {
		super(label, x, y, width, height);
		this.action = action;
	}

	@Override
	protected void onPress() {
		this.action.run();
	}
}
