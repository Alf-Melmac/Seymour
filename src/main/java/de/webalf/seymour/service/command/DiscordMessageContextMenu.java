package de.webalf.seymour.service.command;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

/**
 * @author Alf
 * @since 11.12.2022
 */
public interface DiscordMessageContextMenu {
	@SuppressWarnings("unused") //Used by InteractionListener#onMessageContextInteraction(MessageContextInteractionEvent)
	void perform(MessageContextInteractionEvent event);
}
