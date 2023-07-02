package de.webalf.seymour.service.command;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

/**
 * @author Alf
 * @since 01.08.2021
 */
public interface DiscordStringSelect {
	@SuppressWarnings("unused") //Used by InteractionListener#onStringSelectInteraction(StringSelectInteractionEvent)
	void process(@NonNull StringSelectInteractionEvent event);
}
