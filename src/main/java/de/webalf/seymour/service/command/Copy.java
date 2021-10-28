package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.SlashCommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

import static de.webalf.seymour.util.InteractionUtils.finishedSlashCommandAction;
import static de.webalf.seymour.util.InteractionUtils.reply;
import static de.webalf.seymour.util.PermissionHelper.Authorization.ADMINISTRATIVE;
import static de.webalf.seymour.util.SlashCommandUtils.getChannelOption;
import static de.webalf.seymour.util.SlashCommandUtils.getMessageIdOption;

/**
 * @author Alf
 * @since 17.09.2021
 */
@Slf4j
@SlashCommand(name = "copy",
		description = "Kopiert eine Nachricht aus dem aktuellen Kanal in einen anderen Kanal.",
		authorization = ADMINISTRATIVE,
		optionPosition = 0)
public class Copy implements DiscordSlashCommand {
	private static final String OPTION_MESSAGE_ID = "messageid";
	private static final String OPTION_NEW_CHANNEL = "newchannel";
	private static final List<List<OptionData>> OPTIONS = List.of(
			List.of(new OptionData(OptionType.STRING, OPTION_MESSAGE_ID, "ID der Nachricht, die kopiert werden soll.", true),
					new OptionData(OptionType.CHANNEL, OPTION_NEW_CHANNEL, "Kanal in den die Kopie versendet werden soll.", true))
	);

	@Override
	public void execute(SlashCommandEvent event) {
		log.trace("Slash command: copy");

		@SuppressWarnings("ConstantConditions") //Required option
		final String messageId = getMessageIdOption(event.getOption(OPTION_MESSAGE_ID));
		if (messageId == null) {
			reply(event, "Das ist keine gültige Nachrichten-ID.");
			return;
		}

		event.getChannel().retrieveMessageById(messageId)
				.queue(message -> {
					@SuppressWarnings("ConstantConditions") //Required option
					final MessageChannel channelOption = getChannelOption(event.getOption(OPTION_NEW_CHANNEL));
					if (channelOption == null) {
						reply(event, "Bitte einen Kanal auswählen.");
						return;
					}
					channelOption.sendMessage(message).queue(ignored -> finishedSlashCommandAction(event));
				}, ignored -> reply(event, "Nachricht nicht gefunden."));
	}

	@Override
	public List<OptionData> getOptions(int optionPosition) {
		return OPTIONS.get(optionPosition);
	}
}
