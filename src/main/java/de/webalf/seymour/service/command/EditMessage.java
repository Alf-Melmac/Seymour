package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.DiscordLocalization;
import de.webalf.seymour.model.annotations.SlashCommand;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

import static de.webalf.seymour.util.InteractionUtils.finishedCommandAction;
import static de.webalf.seymour.util.InteractionUtils.reply;
import static de.webalf.seymour.util.SlashCommandUtils.getMessageIdOption;
import static de.webalf.seymour.util.SlashCommandUtils.getStringOption;
import static net.dv8tion.jda.api.interactions.DiscordLocale.GERMAN;

/**
 * @author Alf
 * @since 28.10.2021
 */
@Slf4j
@SlashCommand(name = "edit_message",
		localizedNames = {
				@DiscordLocalization(locale = GERMAN, name = "nachricht_bearbeiten")
		},
		description = "Replaces the message with the passed ID from the current channel with the passed text.",
		localizedDescriptions = {
				@DiscordLocalization(locale = GERMAN, name = "Ersetzt die Nachricht mit der übergebenen ID aus dem aktuellen Kanal mit dem übergebenen Text.")
		},
		authorization = Permission.MESSAGE_MANAGE,
		optionPosition = 0)
public class EditMessage implements DiscordSlashCommand {
	private static final String OPTION_MESSAGE_ID = "message-id";
	private static final String OPTION_MESSAGE = "text";
	private static final List<List<OptionData>> OPTIONS = List.of(
			List.of(
					new OptionData(OptionType.STRING, OPTION_MESSAGE_ID, "ID of the message to be edited.", true)
							.setNameLocalization(GERMAN, "nachrichten-id")
							.setDescriptionLocalization(GERMAN, "ID der Nachricht, die bearbeitet werden soll."),
					new OptionData(OptionType.STRING, OPTION_MESSAGE, "Text to send.", true)
							.setNameLocalization(GERMAN, "text")
							.setDescriptionLocalization(GERMAN, "Zu versendender Text."))
	);

	@Override
	public void execute(@NonNull SlashCommandInteractionEvent event) {
		log.trace("Slash command: editMessage");

		@SuppressWarnings("ConstantConditions") //Required option
		final String messageId = getMessageIdOption(event.getOption(OPTION_MESSAGE_ID));
		if (messageId == null) {
			reply(event, "Das ist keine gültige Nachrichten-ID.");
			return;
		}

		//noinspection ConstantConditions Required option
		event.getChannel().editMessageById(messageId, getStringOption(event.getOption(OPTION_MESSAGE)))
				.queue(unused -> finishedCommandAction(event),
						ignored -> reply(event, "Nachricht nicht bearbeitbar."));
	}

	@Override
	public List<OptionData> getOptions(int optionPosition) {
		return OPTIONS.get(optionPosition);
	}
}
