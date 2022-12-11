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
import static de.webalf.seymour.util.MessageUtils.sendMessage;
import static de.webalf.seymour.util.SlashCommandUtils.getStringOption;
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
		description = "Makes the bot send the passed message to the same channel.",
		localizedDescriptions = {
				@DiscordLocalization(locale = GERMAN, name = "Lässt den Bot die übergebene Nachricht in den gleichen Kanal versenden.")
		},
		authorization = Permission.MESSAGE_MANAGE,
		optionPosition = 0)
public class PostMessage implements DiscordSlashCommand {
	private static final String OPTION_MESSAGE = "text";
	private static final List<List<OptionData>> OPTIONS = List.of(
			List.of(new OptionData(OptionType.STRING, OPTION_MESSAGE, "Text to send.", true)
					.setNameLocalization(GERMAN, "text")
					.setDescriptionLocalization(GERMAN, "Zu versendender Text."))
	);

	@Override
	public void execute(@NonNull SlashCommandInteractionEvent event) {
		log.trace("Slash command: postMessage");

		@SuppressWarnings("ConstantConditions") //Required option
		final String message = getStringOption(event.getOption(OPTION_MESSAGE));

		sendMessage(event, message);

		finishedCommandAction(event);
	}

	@Override
	public List<OptionData> getOptions(int optionPosition) {
		return OPTIONS.get(optionPosition);
	}
}
