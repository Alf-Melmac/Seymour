package de.webalf.seymour.service.listener;

import de.webalf.seymour.model.annotations.ModalInteraction;
import de.webalf.seymour.util.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static de.webalf.seymour.util.InteractionUtils.*;

/**
 * @author Alf
 * @since 15.07.2021
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InteractionListener extends ListenerAdapter {
	private final CommandClassHelper commandClassHelper;

	@Override
	public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) {
		final String commandName = event.getName();
		log.debug("Received slash command: {} from {}", commandName, event.getUser().getId());

		final Class<?> commandClass = SlashCommandUtils.get(commandName);
		if (commandClass == null) {
			log.error("Received not known slash command: {}", commandName);
			return;
		}

		if (!commandClass.isAnnotationPresent(ModalInteraction.class)) {
			ephemeralDeferReply(event);
		}

		try {
			commandClass.getMethod("execute", SlashCommandInteractionEvent.class).invoke(commandClassHelper.getConstructor(commandClass), event);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			unknownException(event, commandClass, e);
		}
	}

	private void unknownException(@NonNull GenericCommandInteractionEvent event, @NonNull Class<?> commandClass, ReflectiveOperationException e) {
		final String errorCode = getErrorCode(e);
		log.error("Failed to execute command interaction {} with options {} - {}", commandClass.getName(), event.getOptions(), errorCode, e);
		failedInteraction(event, "Sorry. Error Code: `" + errorCode + "`");
	}

	@Override
	public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent event) {
		final String componentId = event.getComponentId();
		log.debug("Received selection menu event: {} from {}", componentId, event.getUser().getId());

		final Class<?> aClass = StringSelectUtils.get(componentId);
		if (aClass == null) {
			log.error("Received not known selection menu: {}", componentId);
			return;
		}

		try {
			aClass.getMethod("process", StringSelectInteractionEvent.class).invoke(commandClassHelper.getConstructor(aClass), event);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			unknownException(event, aClass, e);
		}
	}

	private void unknownException(@NonNull StringSelectInteractionEvent event, @NonNull Class<?> commandClass, ReflectiveOperationException e) {
		final String errorCode = getErrorCode(e);
		log.error("Failed to process string selection menu selection {} with id {} - {}", commandClass.getName(), event.getComponentId(), errorCode, e);
		replyAndRemoveComponents(event, "Sorry. Error Code: `" + errorCode + "`");
	}

	@Override
	public void onMessageContextInteraction(@NonNull MessageContextInteractionEvent event) {
		final String commandName = event.getName();
		log.debug("Received message context interaction event: {} from {}", commandName, event.getUser().getId());

		final Class<?> commandClass = ContextMenuUtils.get(commandName);
		if (commandClass == null) {
			log.error("Received not known context menu: {}", commandName);
			return;
		}

		if (!commandClass.isAnnotationPresent(ModalInteraction.class)) {
			ephemeralDeferReply(event);
		}

		try {
			commandClass.getMethod("perform", MessageContextInteractionEvent.class).invoke(commandClassHelper.getConstructor(commandClass), event);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			unknownException(event, commandClass, e);
		}
	}

	@Override
	public void onModalInteraction(@NonNull ModalInteractionEvent event) {
		final String modalId = event.getModalId().split("-")[0];
		log.debug("Received modal interaction event: {} from {}", modalId, event.getUser().getId());

		final Class<?> aClass = ModalInteractionUtils.get(modalId);
		if (aClass == null) {
			log.error("Received not known modal: {}", modalId);
			return;
		}

		ephemeralDeferReply(event);

		try {
			aClass.getMethod("handle", ModalInteractionEvent.class).invoke(commandClassHelper.getConstructor(aClass), event);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			unknownException(event, aClass, e);
		}
	}

	private void unknownException(@NonNull ModalInteractionEvent event, @NonNull Class<?> commandClass, ReflectiveOperationException e) {
		final String errorCode = getErrorCode(e);
		log.error("Failed to process modal interaction {} with values {} - {}", commandClass.getName(), event.getValues(), errorCode, e);
		failedInteraction(event, "Sorry. Error Code: `" + errorCode + "`");
	}

	private String getErrorCode(ReflectiveOperationException e) {
		final String message = e.getMessage();
		return message != null ? UUID.nameUUIDFromBytes(message.getBytes()).toString() : UUID.randomUUID().toString();
	}
}
