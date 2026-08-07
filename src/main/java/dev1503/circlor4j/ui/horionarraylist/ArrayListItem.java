package dev1503.circlor4j.ui.horionarraylist;

/** An entry in the array list: module text, optional keybind, optional mode text and category id. */
public class ArrayListItem {
	public String text;
	public String keybind;
	public String modeText;
	public int category;

	public ArrayListItem(String text, String keybind, String modeText, int category) {
		this.text = text;
		this.keybind = keybind;
		this.modeText = modeText;
		this.category = category;
	}

	public String getFullText() {
		StringBuilder builder = new StringBuilder(this.text);
		if (this.keybind != null) {
			builder.append(" [").append(this.keybind).append(']');
		}
		if (this.modeText != null) {
			builder.append(' ').append(this.modeText);
		}
		return builder.toString();
	}
}