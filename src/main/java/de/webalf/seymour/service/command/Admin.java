package de.webalf.seymour.service.command;

import de.webalf.seymour.constant.Emojis;
import de.webalf.seymour.model.annotations.SlashCommand;
import de.webalf.seymour.model.annotations.StringSelectInteraction;
import de.webalf.seymour.service.InviteService;
import de.webalf.seymour.util.StringSelectUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.List;

import static de.webalf.seymour.util.InteractionUtils.*;
import static de.webalf.seymour.util.StringUtils.invitesToString;

/**
 * @author Alf
 * @since 16.09.2021
 */
@Slf4j
@SlashCommand(name = "admin",
		description = "Admin Utilities",
		authorization = Permission.ADMINISTRATOR)
@StringSelectInteraction("admin")
public class Admin implements DiscordSlashCommand, DiscordStringSelect {
	private enum Command {
		PING,
		GUILD_TEST,
		CHANNEL_TEST,
		USER_TEST,
		CLEAR_CHANNEL,
		INVITE_LIST
	}

	@Override
	public void execute(@NonNull SlashCommandInteractionEvent event) {
		log.trace("Slash command: admin");
		final boolean isGerman = isGerman(event);

		final StringSelectMenu.Builder selectionMenuBuilder = StringSelectMenu.create(getClass().getAnnotation(StringSelectInteraction.class).value())
				.setPlaceholder(isGerman ? "Befehl auswählen..." : "Select command...");

		for (Command command : Command.values()) {
			selectionMenuBuilder.addOption(StringSelectUtils.buildSelectLabel(command.name()), command.name());
		}

		addSelectionMenu(event, selectionMenuBuilder.build());
	}

	@Override
	public void process(StringSelectInteractionEvent event) {
		log.trace("Selection menu: admin");

		switch (Command.valueOf(event.getValues().get(0))) {
			case PING -> replyAndRemoveComponents(event, "Pong");
			case GUILD_TEST -> {
				final Guild guild = event.getGuild();
				//noinspection DataFlowIssue Guild only command
				replyAndRemoveComponents(event, "Guild ID: `" + guild.getId() + "` Guild Name: `" + guild.getName() + "`");
			}
			case CHANNEL_TEST -> {
				final Channel channel = event.getChannel();
				replyAndRemoveComponents(event, "Channel ID: `" + channel.getId() + "` Channel Name: `" + channel.getName() + "`");
			}
			case USER_TEST -> {
				final User author = event.getUser();
				replyAndRemoveComponents(event, "Author ID: `" + author.getId() + "` Author Name: `" + author.getName() + "`");
			}
			case CLEAR_CHANNEL -> {
				final TextChannel textChannel = event.getChannel().asTextChannel();
				final List<Message> messages = new ArrayList<>();
				textChannel.getIterableHistory()
						.forEachAsync(m -> {
							if (!m.isFromGuild()) return false;
							messages.add(m);
							return true;
						})
						.thenRun(() -> textChannel.purgeMessages(messages));
				replyAndRemoveComponents(event, "Deletion started");
			}
			case INVITE_LIST ->
				//noinspection DataFlowIssue Guild only command
					replyAndRemoveComponents(event, invitesToString(InviteService.getGUILD_INVITES().get(event.getGuild().getIdLong())));
			default -> replyAndRemoveComponents(event, Emojis.CHECKBOX.getFormatted());
		}
	}
}
