package de.webalf.seymour.util;

import de.webalf.seymour.model.annotations.ContextMenu;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.atteo.classindex.ClassIndex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * @author Alf
 * @since 11.12.2022
 */
@UtilityClass
public final class ContextMenuUtils {
	private static final Map<String, Class<?>> commandToClassMap = new HashMap<>();

	static {
		final Iterable<Class<?>> commandList = ClassIndex.getAnnotated(ContextMenu.class);
		StreamSupport.stream(commandList.spliterator(), false)
				.forEach(command -> commandToClassMap.put(CommandClassHelper.getContextMenu(command).name().toLowerCase(), command));
	}

	/**
	 * Searches for the given context menu the matching class annotated with {@link ContextMenu}
	 *
	 * @param command to search
	 * @return matching class or null if not found
	 */
	public static Class<?> get(@NonNull String command) {
		return commandToClassMap.get(command.toLowerCase());
	}

	/**
	 * Returns all classes annotated with {@link ContextMenu}
	 *
	 * @return all context menu classes
	 */
	public static Collection<Class<?>> get() {
		return commandToClassMap.values();
	}
}
