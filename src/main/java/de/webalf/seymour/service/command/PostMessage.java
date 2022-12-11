package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.SlashCommand;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

import static de.webalf.seymour.util.InteractionUtils.finishedSlashCommandAction;
import static de.webalf.seymour.util.MessageUtils.sendMessage;
import static de.webalf.seymour.util.SlashCommandUtils.getStringOption;

/**
 * @author Alf
 * @since 21.02.2021
 */
@Slf4j
@SlashCommand(name = "postMessage",
		description = "Lässt den Bot die übergebene Nachricht in den gleichen Kanal versenden.",
		authorization = Permission.MESSAGE_MANAGE,
		optionPosition = 0)
public class PostMessage implements DiscordSlashCommand {
	private static final String OPTION_MESSAGE = "nachricht";
	private static final List<List<OptionData>> OPTIONS = List.of(
			List.of(new OptionData(OptionType.STRING, OPTION_MESSAGE, "Zu versendender Text.", true))
	);

	@Override
	public void execute(@NonNull SlashCommandInteractionEvent event) {
		log.trace("Slash command: postMessage");

		@SuppressWarnings("ConstantConditions") //Required option
		final String message = getStringOption(event.getOption(OPTION_MESSAGE));

		sendMessage(event, message);

		finishedSlashCommandAction(event);
	}

	@Override
	public List<OptionData> getOptions(int optionPosition) {
		return OPTIONS.get(optionPosition);
	}
}
