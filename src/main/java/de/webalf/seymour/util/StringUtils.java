package de.webalf.seymour.util;

import lombok.experimental.UtilityClass;

/**
 * @author Alf
 * @since 22.12.2020
 */
@UtilityClass
public final class StringUtils {
	private static final String NON_DIGIT_REGEX = "\\D";

	public static String removeNonDigitCharacters(String str) {
		return str.replaceAll(NON_DIGIT_REGEX, "");
	}
}
