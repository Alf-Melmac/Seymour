package de.webalf.seymour.util;

import de.webalf.seymour.model.annotations.ModalInteraction;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.atteo.classindex.ClassIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * @author Alf
 * @since 12.12.2022
 */
@UtilityClass
public final class ModalInteractionUtils {
	public static final Map<String, Class<?>> idToClassMap = new HashMap<>();

	static {
		final Iterable<Class<?>> classIterable = ClassIndex.getAnnotated(ModalInteraction.class);
		StreamSupport.stream(classIterable.spliterator(), false)
				.forEach(command -> idToClassMap.put(command.getAnnotation(ModalInteraction.class).value(), command));
	}

	/**
	 * Searches for the given modal id the matching class annotated with {@link ModalInteraction}
	 *
	 * @param modalId to search
	 * @return matching class or null if not found
	 */
	public static Class<?> get(@NonNull String modalId) {
		return idToClassMap.get(modalId);
	}

	/**
	 * Returns the string value of the given not null {@link ModalMapping}
	 *
	 * @param mapping to get input from
	 * @return string
	 */
	public static String getStringValue(@NonNull ModalMapping mapping) {
		return mapping.getAsString();
	}
}
