package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.DiscordLocalization;
import de.webalf.seymour.model.annotations.ModalInteraction;
import de.webalf.seymour.model.annotations.SlashCommand;
import de.webalf.seymour.util.MessageUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import static de.webalf.seymour.util.InteractionUtils.*;
import static de.webalf.seymour.util.ModalInteractionUtils.getStringValue;
import static net.dv8tion.jda.api.interactions.DiscordLocale.GERMAN;

/**
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

		final boolean german = isGerman(event);
		final Modal modal = buildMessageModal(german, german ? "Nachricht senden" : "Post message", getClass().getAnnotation(ModalInteraction.class).value());

		replyModal(event, modal);
	}

	static Modal buildMessageModal(boolean german, String modalTitle, @NonNull String id) {
		final String inputLabel = "Text";
		final String inputPlaceholder = german ? "Zu versendender Text" : "Text to send";

		final TextInput textInput = TextInput.create(MESSAGE_CONTENT, inputLabel, TextInputStyle.PARAGRAPH)
				.setPlaceholder(inputPlaceholder)
				.setRequiredRange(1, Message.MAX_CONTENT_LENGTH)
				.build();

		return Modal.create(id, modalTitle)
				.addActionRow(textInput)
				.build();
	}

	@Override
	public void handle(ModalInteractionEvent event) {
		log.trace("Modal: postMessageModal");

		@SuppressWarnings("ConstantConditions") //Required option
		final String message = getStringValue(event.getValue(MESSAGE_CONTENT));

		MessageUtils.sendMessage(event, message);

		finishedCommandAction(event);
	}
}
