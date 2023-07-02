package de.webalf.seymour.service.command;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

/**
 * @author Alf
 * @since 11.12.2022
 */
public interface DiscordModal {
	@SuppressWarnings("unused") //Used by InteractionListener#onModalInteraction(ModalInteractionEvent)
	void handle(@NonNull ModalInteractionEvent event);
}
