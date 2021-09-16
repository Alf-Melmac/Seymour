package de.webalf.seymour.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

/**
 * @author Alf
 * @since 16.09.2021
 */
@UtilityClass
public final class ArrayUtils {
	/**
	 * Adds the given element to given array
	 *
	 * @param array to element to
	 * @param el    element to add
	 * @param <T>   Array type
	 * @return array with added element
	 */
	public static <T> T[] add(T[] array, T el) {
		array = Arrays.copyOf(array, array.length + 1);
		array[array.length - 1] = el;
		return array;
	}
}
