package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.ContextMenu;
import de.webalf.seymour.model.annotations.DiscordLocalization;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import static de.webalf.seymour.constant.Emojis.THUMBS_DOWN;
import static de.webalf.seymour.constant.Emojis.THUMBS_UP;
import static de.webalf.seymour.util.ChannelUtils.canAddReaction;
import static de.webalf.seymour.util.InteractionUtils.*;
import static net.dv8tion.jda.api.interactions.DiscordLocale.GERMAN;

/**
 * This requires the following permissions to add reactions
 * <ul>
 *     <li>{@link Permission#MESSAGE_HISTORY}</li>
 *     <li>{@link Permission#MESSAGE_ADD_REACTION}</li>
 * </ul>
 *
 * @author Alf
 * @since 16.09.2021
 */
@Slf4j
@ContextMenu(name = "Start vote",
		localizedNames = {
				@DiscordLocalization(locale = GERMAN, name = "Abstimmung starten")
		},
		type = Command.Type.MESSAGE,
		authorization = Permission.MESSAGE_ADD_REACTION)
public class StartVote implements DiscordMessageContext {
	@Override
	public void perform(@NonNull MessageContextInteractionEvent event) {
		log.trace("Message context: vote");
		final boolean isGerman = isGerman(event);

		//noinspection DataFlowIssue Guild only command
		if (!canAddReaction(event.getGuild().getSelfMember(), event.getGuildChannel())) {
			failedInteraction(event, isGerman ? "Keine Berechtigung zum Hinzufügen von Reaktionen." : "No permission to add reactions.");
			return;
		}

		final Message target = event.getTarget();
		target.addReaction(THUMBS_UP.getEmoji())
				.queue(unused -> target.addReaction(THUMBS_DOWN.getEmoji()).queue(unused1 ->
						finishedInteraction(event)));
	}
}
