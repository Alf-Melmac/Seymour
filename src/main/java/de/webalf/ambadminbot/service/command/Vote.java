package de.webalf.ambadminbot.service.command;

import de.webalf.ambadminbot.model.annotations.SlashCommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

import static de.webalf.ambadminbot.constant.Emojis.THUMBS_DOWN;
import static de.webalf.ambadminbot.constant.Emojis.THUMBS_UP;
import static de.webalf.ambadminbot.util.InteractionUtils.finishedSlashCommandAction;
import static de.webalf.ambadminbot.util.InteractionUtils.reply;
import static de.webalf.ambadminbot.util.MentionUtils.isSnowflake;
import static de.webalf.ambadminbot.util.PermissionHelper.Authorization.NONE;
import static de.webalf.ambadminbot.util.SlashCommandUtils.getOptionalStringOption;
import static de.webalf.ambadminbot.util.StringUtils.removeNonDigitCharacters;

/**
 * @author Alf
 * @since 16.09.2021
 */
@Slf4j
@SlashCommand(name = "vote", description = "Voting für eine Nachricht starten. Wenn nicht angegeben, wird die letzte Nachricht verwendet.", authorization = NONE, optionPosition = 0)
public class Vote implements DiscordSlashCommand {
	private static final String OPTION_MESSAGE_ID = "messageid";
	private static final List<List<OptionData>> OPTIONS = List.of(
			List.of(new OptionData(OptionType.STRING, OPTION_MESSAGE_ID, "ID der Nachricht.", false))
	);

	@Override
	public void execute(SlashCommandEvent event) {
		log.trace("Slash command: vote");

		String messageId = getOptionalStringOption(event.getOption(OPTION_MESSAGE_ID));
		if (messageId == null) {
			messageId = event.getChannel().getLatestMessageId();
		} else {
			messageId = removeNonDigitCharacters(messageId);
		}
		if (!isSnowflake(messageId)) {
			reply(event, "Das ist keine gültige ID oder erkennbares Argument.");
			return;
		}

		final MessageChannel channel = event.getChannel();
		final String finalMessageId = messageId;
		channel.addReactionById(messageId, THUMBS_UP.getNotation()).queue(unused ->
						channel.addReactionById(finalMessageId, THUMBS_DOWN.getNotation()).queue(unused1 ->
								finishedSlashCommandAction(event)),
				ignored -> reply(event, "Nachricht nicht gefunden."));
	}

	@Override
	public List<OptionData> getOptions(int optionPosition) {
		return OPTIONS.get(optionPosition);
	}
}
