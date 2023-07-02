package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.ContextMenu;
import de.webalf.seymour.model.annotations.DiscordLocalization;
import de.webalf.seymour.model.annotations.ModalInteraction;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.modals.Modal;

import static de.webalf.seymour.service.command.PostMessage.buildMessageModal;
import static de.webalf.seymour.util.InteractionUtils.*;
import static de.webalf.seymour.util.ModalInteractionUtils.getStringValue;
import static net.dv8tion.jda.api.interactions.DiscordLocale.GERMAN;

/**
 * This requires the following permissions to edit messages
 * <ul>
 *     <li>{@link Permission#VIEW_CHANNEL}</li>
 *     <li>{@link Permission#MESSAGE_SEND}</li>
 * </ul>
 *
 * @author Alf
 * @since 28.10.2021
 */
@Slf4j
@ContextMenu(name = "Edit message",
		localizedNames = {
				@DiscordLocalization(locale = GERMAN, name = "Nachricht bearbeiten")
		},
		type = Command.Type.MESSAGE,
		authorization = Permission.MESSAGE_MANAGE)
@ModalInteraction("editMessageModal")
public class EditMessage implements DiscordMessageContext, DiscordModal {
	private static final String MESSAGE_CONTENT = "messageContent";

	@Override
	public void perform(@NonNull MessageContextInteractionEvent event) {
		log.trace("Message context: editMessage");
		final boolean isGerman = isGerman(event);

		final Message message = event.getTarget();
		if (!message.getAuthor().equals(event.getJDA().getSelfUser())) {
			replyNonDeferred(event, isGerman ? "Keine Nachricht von mir." : "Not a message from me.");
			return;
		}
		if (!event.getMessageChannel().canTalk()) {
			replyNonDeferred(event, isGerman ? "Ich muss diesen Kanal sehen und Nachrichten senden können, um Nachrichten bearbeiten zu dürfen." : "I must be able to see this channel and send messages in order to edit messages.");
			return;
		}

		final Modal modal = buildMessageModal(isGerman, isGerman ? "Nachricht bearbeiten" : "Edit message", getClass().getAnnotation(ModalInteraction.class).value() + "-" + message.getId());

		replyModal(event, modal);
	}

	@Override
	public void handle(@NonNull ModalInteractionEvent event) {
		log.trace("Modal: editMessageModal");

		@SuppressWarnings("ConstantConditions") //Required option
		final String message = getStringValue(event.getValue(MESSAGE_CONTENT));

		event.getMessageChannel()
				.editMessageById(event.getModalId().split("-")[1], message)
				.queue();

		finishedInteraction(event);
	}
}
