package de.webalf.ambadminbot.service.command;

import de.webalf.ambadminbot.constant.Emojis;
import de.webalf.ambadminbot.model.annotations.SelectionMenuListener;
import de.webalf.ambadminbot.model.annotations.SlashCommand;
import de.webalf.ambadminbot.util.SelectionMenuUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.Arrays;

import static de.webalf.ambadminbot.util.InteractionUtils.addSelectionMenu;
import static de.webalf.ambadminbot.util.InteractionUtils.replyAndRemoveComponents;
import static de.webalf.ambadminbot.util.PermissionHelper.Authorization.SYS_ADMINISTRATION;

/**
 * @author Alf
 * @since 16.09.2021
 */
@Slf4j
@SlashCommand(name = "admin", description = "Admin Utilities", authorization = SYS_ADMINISTRATION)
@SelectionMenuListener("admin")
public class Admin implements DiscordSlashCommand, DiscordSelectionMenu {
	private enum Command {
		PING,
		GUILD_TEST,
		CHANNEL_TEST,
		USER_TEST,
		CLEAR_CHANNEL;
	}

	@Override
	public void execute(SlashCommandEvent event) {
		log.trace("Slash command: admin");

		final SelectionMenu.Builder selectionMenuBuilder = SelectionMenu.create(getClass().getAnnotation(SelectionMenuListener.class).value())
				.setPlaceholder("Befehl auswählen...");

		for (Command command : Command.values()) {
			selectionMenuBuilder.addOption(SelectionMenuUtils.buildSelectionLabel(command.name()), command.name());
		}

		addSelectionMenu(event, selectionMenuBuilder.build());
	}

	@Override
	public void process(SelectionMenuEvent event) {
		log.trace("Selection menu: admin");

		switch (Command.valueOf(event.getValues().get(0))) {
			case PING:
				replyAndRemoveComponents(event, "Pong");
				break;
			case GUILD_TEST:
				final Guild guild = event.getGuild();
				replyAndRemoveComponents(event, "Guild ID: `" + guild.getId() + "` Channel Name: `" + guild.getName() + "`");
				break;
			case CHANNEL_TEST:
				final MessageChannel channel = event.getChannel();
				replyAndRemoveComponents(event, "Channel ID: `" + channel.getId() + "` Channel Name: `" + channel.getName() + "`");
				break;
			case USER_TEST:
				final User author = event.getUser();
				replyAndRemoveComponents(event, "Author ID: `" + author.getId() + "` Author Tag: `" + author.getAsTag() + "`" + "` Author Name: `" + author.getName() + "`");
				break;
			case CLEAR_CHANNEL:
				TextChannel textChannel = (TextChannel) event.getChannel();
				//Explicitly do NOT use the bulk delete method, because it cannot delete messages older than two weeks
				Arrays.stream(textChannel.getHistory().retrievePast(100).complete().toArray(new Message[0])).forEach(message -> {
					if (!message.isFromGuild()) {
						return;
					}
					message.delete().queue();
				});
				replyAndRemoveComponents(event, "Deletion started");
				break;
			default:
				replyAndRemoveComponents(event, Emojis.CHECKBOX.getNotation());
				break;
		}
	}
}
