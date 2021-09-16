package de.webalf.seymour.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Alf
 * @since 15.01.2021
 */
@Getter
@AllArgsConstructor
public enum Emojis {
	//Codepoint notation
	THUMBS_UP("U+1F44D"),
	THUMBS_DOWN("U+1F44E"),

	//Standard discord notation
	CHECKBOX(":ballot_box_with_check:");

	private final String notation;
}
