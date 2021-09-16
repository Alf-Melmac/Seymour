package de.webalf.seymour.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * @author Alf
 * @since 16.09.2021
 */
@UtilityClass
public final class MentionUtils {
	private static final Pattern SNOWFLAKE = Pattern.compile("^\\d{17,19}$");

	public static boolean isSnowflake(String arg) {
		return SNOWFLAKE.matcher(arg).matches();
	}
}
