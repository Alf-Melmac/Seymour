package de.webalf.seymour.service;

import de.webalf.seymour.model.annotations.ContextMenu;
import de.webalf.seymour.model.annotations.SlashCommand;
import de.webalf.seymour.util.CommandClassHelper;
import de.webalf.seymour.util.ContextMenuUtils;
import de.webalf.seymour.util.SlashCommandUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static de.webalf.seymour.util.CommandClassHelper.getContextMenu;
import static de.webalf.seymour.util.CommandClassHelper.getSlashCommand;

/**
 * @author Alf
 * @since 15.07.2021
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommandsService {
	private final CommandClassHelper commandClassHelper;

	/**
	 * Updates slash commands in the given guild
	 *
	 * @param guild to update commands for
	 */
	public void updateCommands(@NonNull Guild guild) {
		log.info("Updating commands for {}...", guild.getName());
		final List<SlashCommandData> slashCommands = SlashCommandUtils.commandToClassMap.values().stream()
				.map(slashCommandClass -> {
					final SlashCommand slashCommand = getSlashCommand(slashCommandClass);
					final SlashCommandData commandData = Commands
							.slash(slashCommand.name().toLowerCase(), slashCommand.description())
							.setDefaultPermissions(DefaultMemberPermissions.enabledFor(slashCommand.authorization()));
					if (slashCommand.optionPosition() >= 0) { //Add options if present
						commandData.addOptions(getOptions(slashCommandClass, slashCommand.optionPosition()));
					}
					return commandData;
				}).toList();
		log.info("Found {} slash commands.", slashCommands.size());

		final List<CommandData> contextMenus = ContextMenuUtils.commandToClassMap.values().stream()
				.map(contextMenuClass -> {
					final ContextMenu contextMenu = getContextMenu(contextMenuClass);
					return Commands
							.context(contextMenu.type(), contextMenu.name().toLowerCase());
				}).toList();
		log.info("Found {} context menus.", contextMenus.size());

		guild.updateCommands().addCommands(slashCommands).addCommands(contextMenus).queue();
		log.info("Queued command update for {}.", guild.getName());
	}

	@SuppressWarnings("unchecked") //The class must implement an interface, and thus we can assume the correct return type here
	private List<OptionData> getOptions(Class<?> commandClass, int optionPosition) {
		try {
			return (List<OptionData>) commandClass.getMethod("getOptions", int.class).invoke(commandClassHelper.getConstructor(commandClass), optionPosition);
		} catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
			log.error("Failed to getOptions {}", e.getMessage());
			return Collections.emptyList();
		}
	}
}
