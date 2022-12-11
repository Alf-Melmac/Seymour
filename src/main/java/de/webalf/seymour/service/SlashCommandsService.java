package de.webalf.seymour.service;

import de.webalf.seymour.model.annotations.SlashCommand;
import de.webalf.seymour.util.CommandClassHelper;
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
import java.util.stream.Collectors;

import static de.webalf.seymour.util.CommandClassHelper.getSlashCommand;

/**
 * @author Alf
 * @since 15.07.2021
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SlashCommandsService {
	private final CommandClassHelper commandClassHelper;

	/**
	 * Updates slash commands in the given guild
	 *
	 * @param guild to update commands for
	 */
	public void updateCommands(@NonNull Guild guild) {
		log.info("Updating commands for {}...", guild.getName());
		final List<CommandData> commandDataList = SlashCommandUtils.commandToClassMap.values().stream()
				.map(slashCommandClass -> { //For each slash command
					final SlashCommand slashCommand = getSlashCommand(slashCommandClass);
					final SlashCommandData commandData = Commands
							.slash(slashCommand.name().toLowerCase(), slashCommand.description())
							.setDefaultPermissions(DefaultMemberPermissions.enabledFor(slashCommand.authorization()));
					if (slashCommand.optionPosition() >= 0) { //Add options if present
						commandData.addOptions(getOptions(slashCommandClass, slashCommand.optionPosition()));
					}
					return commandData;
				}).collect(Collectors.toUnmodifiableList());
		log.info("Found {} commands. Starting update...", commandDataList.size());

		guild.updateCommands().addCommands(commandDataList).queue();
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
