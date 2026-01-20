package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.DiscordLocalization;
import de.webalf.seymour.model.annotations.ModalInteraction;
import de.webalf.seymour.model.annotations.SlashCommand;
import de.webalf.seymour.util.MessageUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

import static de.webalf.seymour.util.InteractionUtils.*;
import static de.webalf.seymour.util.ModalInteractionUtils.getStringValue;
import static net.dv8tion.jda.api.interactions.DiscordLocale.GERMAN;

/**
 * This requires the following permissions to post messages
 * <ul>
 *     <li>{@link Permission#VIEW_CHANNEL}</li>
 *     <li>{@link Permission#MESSAGE_SEND}</li>
 * </ul>
 *
 * @author Alf
 * @since 21.02.2021
 */
@Slf4j
@SlashCommand(name = "post_message",
		localizedNames = {
				@DiscordLocalization(locale = GERMAN, name = "nachricht_senden")
		},
		description = "Makes the bot send a message to the same channel.",
		localizedDescriptions = {
				@DiscordLocalization(locale = GERMAN, name = "Lässt den Bot eine Nachricht an denselben Kanal senden.")
		},
		authorization = Permission.MESSAGE_MANAGE)
@ModalInteraction("postMessageModal")
public class PostMessage implements DiscordSlashCommand, DiscordModal {
	private static final String MESSAGE_CONTENT = "messageContent";

	@Override
	public void execute(@NonNull SlashCommandInteractionEvent event) {
		log.trace("Slash command: postMessage");
		final boolean isGerman = isGerman(event);

		if (!event.getMessageChannel().canTalk()) {
			replyNonDeferred(event, isGerman ? "Ich muss diesen Kanal sehen und Nachrichten senden können, um Nachrichten senden zu dürfen." : "I must be able to see this channel and send messages in order to send messages.");
			return;
		}

		final Modal modal = buildMessageModal(isGerman, isGerman ? "Nachricht senden" : "Post message", getClass().getAnnotation(ModalInteraction.class).value());

		replyModal(event, modal);
	}

	static Modal buildMessageModal(boolean german, String modalTitle, @NonNull String id) {
		final String inputLabel = "Text";
		final String inputPlaceholder = german ? "Zu versendender Text" : "Text to send";

		final TextInput textInput = TextInput.create(MESSAGE_CONTENT, TextInputStyle.PARAGRAPH)
				.setPlaceholder(inputPlaceholder)
				.setRequiredRange(1, Message.MAX_CONTENT_LENGTH)
				.build();

		return Modal.create(id, modalTitle)
				.addComponents(Label.of(inputLabel, textInput))
				.build();
	}

	@Override
	public void handle(@NonNull ModalInteractionEvent event) {
		log.trace("Modal: postMessageModal");

		@SuppressWarnings("ConstantConditions") //Required option
		final String message = getStringValue(event.getValue(MESSAGE_CONTENT));

		MessageUtils.sendMessage(event, message);

		finishedInteraction(event);
	}
}
