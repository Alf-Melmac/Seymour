package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.DiscordLocalization;
import de.webalf.seymour.model.annotations.SlashCommand;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.webalf.seymour.util.ChannelUtils.canViewChannel;
import static de.webalf.seymour.util.InteractionUtils.*;
import static de.webalf.seymour.util.SlashCommandUtils.getChannelOptionAsGuildMessageChannel;
import static de.webalf.seymour.util.SlashCommandUtils.getMessageIdOption;
import static net.dv8tion.jda.api.interactions.DiscordLocale.GERMAN;

/**
 * This requires the following permissions to copy messages
 * <ul>
 *     <li>
 *         Start channel
 *         <ul>
 *             <li>{@link Permission#VIEW_CHANNEL}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Target channel
 *         <ul>
 *             <li>{@link Permission#VIEW_CHANNEL}</li>
 *             <li>{@link Permission#MESSAGE_SEND}</li>
 *         </ul>
 *     </li>
 * </ul>
 *
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
		final boolean isGerman = isGerman(event);

		@SuppressWarnings("ConstantConditions") //Required option
		final String messageId = getMessageIdOption(event.getOption(OPTION_MESSAGE_ID));
		if (messageId == null) {
			reply(event, isGerman ? "Keine gültige Nachrichten-ID angegeben." : "Not a valid message id provided.");
			return;
		}

		final MessageChannelUnion channel = event.getChannel();
		//noinspection DataFlowIssue Guild only command
		if (!canViewChannel(event.getGuild().getSelfMember(), channel.asGuildMessageChannel())) {
			failedInteraction(event, isGerman ? "Diesen Kanal darf ich nicht sehen." : "I am not allowed to see this channel.");
			return;
		}
		channel.retrieveMessageById(messageId)
				.queue(message -> {
					final OptionMapping optionMapping = event.getOption(OPTION_NEW_CHANNEL);
					@SuppressWarnings("ConstantConditions") //Required option
					final GuildMessageChannel channelOption = getChannelOptionAsGuildMessageChannel(optionMapping);
					if (channelOption == null) {
						failedInteraction(event, isGerman ? "Der Kanal <#" + optionMapping.getAsString() + "> kann nicht erreicht werden." :
								"Channel <#" + optionMapping.getAsString() + "> can't be accessed.");
						return;
					}
					if (!channelOption.canTalk()) {
						failedInteraction(event, isGerman ? "Fehlende Schreibrechte im Zielkanal." : "Missing write permission in the target channel.");
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
							.queue(sendMessage -> reply(event, (isGerman ? "Kopiert nach " : "Copied to ") + sendMessage.getChannel().getAsMention()));
				}, ignored -> failedInteraction(event, isGerman ? "Nachricht nicht gefunden." : "Message not found."));
	}

	@Override
	public List<OptionData> getOptions(int optionPosition) {
		return OPTIONS.get(optionPosition);
	}
}
