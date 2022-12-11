package de.webalf.seymour.util;

import de.webalf.seymour.model.annotations.ContextMenu;
import de.webalf.seymour.model.annotations.SlashCommand;
import de.webalf.seymour.service.command.DiscordSlashCommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Alf
 * @since 04.01.2021
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommandClassHelper {
	/**
	 * Tries to create a new constructor instance for the given {@link DiscordSlashCommand}
	 *
	 * @param commandClass command to get constructor for
	 * @return a new instance of the declared constructor
	 * @throws IllegalArgumentException if construct couldn't be found
	 */
	public Object getConstructor(@NonNull Class<?> commandClass) throws IllegalArgumentException {
		Object constructor = null;

		for (Constructor<?> declaredConstructor : commandClass.getDeclaredConstructors()) {
			Class<?>[] parameterTypes = declaredConstructor.getParameterTypes();

			if (parameterTypes.length == 0) {
				//Admin, Copy, EditMessage, PostMessage, Vote
				try {
					constructor = declaredConstructor.newInstance();
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
					log.error("Failed to create new constructor instance without parameters for type {}", commandClass.getName(), e);
				}
				break;
			}
		}

		if (constructor == null) {
			throw new IllegalArgumentException("Couldn't find constructor for " + commandClass.getName());
		}

		return constructor;
	}

	public static SlashCommand getSlashCommand(@NonNull Class<?> commandClass) {
		return commandClass.getAnnotation(SlashCommand.class);
	}

	public static ContextMenu getContextMenu(@NonNull Class<?> commandClass) {
		return commandClass.getAnnotation(ContextMenu.class);
	}
}
