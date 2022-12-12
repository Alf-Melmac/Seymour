package de.webalf.seymour.util;

import de.webalf.seymour.model.annotations.StringSelectInteraction;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.atteo.classindex.ClassIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * @author Alf
 * @since 01.08.2021
 */
@UtilityClass
public final class StringSelectUtils {
	public static final Map<String, Class<?>> idToClassMap = new HashMap<>();

	static {
		final Iterable<Class<?>> classIterable = ClassIndex.getAnnotated(StringSelectInteraction.class);
		StreamSupport.stream(classIterable.spliterator(), false)
				.forEach(command -> idToClassMap.put(command.getAnnotation(StringSelectInteraction.class).value(), command));
	}

	/**
	 * Searches for the given string select id the matching class annotated with {@link StringSelectInteraction}
	 *
	 * @param stringSelectId to search
	 * @return matching class or null if not found
	 */
	public static Class<?> get(@NonNull String stringSelectId) {
		return idToClassMap.get(stringSelectId);
	}

	/**
	 * Builds the label string for a {@link SelectOption} respecting the maximum length
	 *
	 * @param label to use
	 * @return shortened label if needed
	 */
	public static String buildSelectLabel(@NonNull String label) {
		return label.substring(0, Math.min(label.length(), SelectOption.LABEL_MAX_LENGTH));
	}
}
