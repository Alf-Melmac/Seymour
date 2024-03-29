package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.SlashCommand;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

/**
 * @author Alf
 * @since 18.07.2021
 */
public interface DiscordSlashCommand {
	@SuppressWarnings("unused") //Used by InteractionListener#onSlashCommandInteraction(SlashCommandInteractionEvent)
	void execute(@NonNull SlashCommandInteractionEvent event);

	/**
	 * List of all slash command options. For each slash command the index in this list is specified in {@link SlashCommand#optionPosition()}
	 *
	 * @return list of every option of all slash commands
	 */
	@SuppressWarnings("unused") //Used by CommandsService#getOptions(Class, int)
	default List<OptionData> getOptions(int optionPosition) {
		return Collections.emptyList();
	}
}
