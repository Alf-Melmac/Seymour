package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.DiscordLocalization;
import de.webalf.seymour.model.annotations.SlashCommand;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.webalf.seymour.util.InteractionUtils.failedSlashCommandAction;
import static de.webalf.seymour.util.InteractionUtils.reply;
import static de.webalf.seymour.util.SlashCommandUtils.getChannelOptionAsGuildMessageChannel;
import static de.webalf.seymour.util.SlashCommandUtils.getMessageIdOption;
import static net.dv8tion.jda.api.interactions.DiscordLocale.GERMAN;

/**
 * @author Alf
 * @since 17.09.2021
 */
@Slf4j
@SlashCommand(name = "copy",
		localizedNames = {
				@DiscordLocalization(locale = GERMAN, name = "kopieren")
		},
		description = "Copies a message from the current channel to another channel.",
		localizedDescriptions = {
				@DiscordLocalization(locale = GERMAN, name = "Kopiert eine Nachricht aus dem aktuellen Kanal in einen anderen Kanal.")
		},
		authorization = Permission.MESSAGE_MANAGE,
		optionPosition = 0)
public class Copy implements DiscordSlashCommand {
	private static final String OPTION_MESSAGE_ID = "message-id";
	private static final String OPTION_NEW_CHANNEL = "new-channel";
	private static final List<List<OptionData>> OPTIONS = List.of(
			List.of(new OptionData(OptionType.STRING, OPTION_MESSAGE_ID, "ID of the message to be copied.", true)
							.setNameLocalization(GERMAN, "nachrichten-id")
							.setDescriptionLocalization(GERMAN, "ID der Nachricht, die kopiert werden soll."),
					new OptionData(OptionType.CHANNEL, OPTION_NEW_CHANNEL, "Channel to which the copy should be sent.", true)
							.setNameLocalization(GERMAN, "neuer-kanal")
							.setDescriptionLocalization(GERMAN, "Kanal in den die Kopie versendet werden soll."))
	);

	@Override
	public void execute(@NonNull SlashCommandInteractionEvent event) {
		log.trace("Slash command: copy");

		@SuppressWarnings("ConstantConditions") //Required option
		final String messageId = getMessageIdOption(event.getOption(OPTION_MESSAGE_ID));
		if (messageId == null) {
			reply(event, "Das ist keine gültige Nachrichten-ID.");
			return;
		}

		event.getChannel().retrieveMessageById(messageId)
				.queue(message -> {
					final OptionMapping optionMapping = event.getOption(OPTION_NEW_CHANNEL);
					@SuppressWarnings("ConstantConditions") //Required option
					final GuildMessageChannel channelOption = getChannelOptionAsGuildMessageChannel(optionMapping);
					if (channelOption == null) {
						failedSlashCommandAction(event, "Der Kanal <#" + optionMapping.getAsString() + "> kann nicht erreicht werden.");
						return;
					}
					if (!channelOption.canTalk()) {
						failedSlashCommandAction(event, "Keine Schreibrecht im Zielkanal.");
						return;
					}

					final MessageCreateData messageCreateData = MessageCreateBuilder
							.fromMessage(message)
							.setFiles(message.getAttachments().stream().map(attachment -> {
								FileUpload fileUpload = null;
								try {
									fileUpload = FileUpload.fromData(attachment.getProxy().download().get(), attachment.getFileName());
								} catch (ExecutionException | InterruptedException e) {
									log.error("Failed to open attachment {} ({})", attachment.getFileName(), attachment.getProxyUrl(), e);
								}
								return fileUpload;
							}).toList())
							.build();

					channelOption.sendMessage(messageCreateData)
							.queue(sendMessage -> reply(event, "Kopiert nach " + sendMessage.getChannel().getAsMention()));
				}, ignored -> failedSlashCommandAction(event, "Nachricht nicht gefunden."));
	}

	@Override
	public List<OptionData> getOptions(int optionPosition) {
		return OPTIONS.get(optionPosition);
	}
}
