package de.webalf.seymour.service.command;

import de.webalf.seymour.model.annotations.ContextMenu;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import static de.webalf.seymour.constant.Emojis.THUMBS_DOWN;
import static de.webalf.seymour.constant.Emojis.THUMBS_UP;
import static de.webalf.seymour.util.InteractionUtils.finishedCommandAction;

/**
 * @author Alf
 * @since 16.09.2021
 */
@Slf4j
@ContextMenu(name = "vote", type = Command.Type.MESSAGE)
public class Vote implements DiscordMessageContextMenu {
	@Override
	public void perform(MessageContextInteractionEvent event) {
		log.trace("Message context interaction: vote");
		final Message target = event.getTarget();
		target.addReaction(THUMBS_UP.getEmoji())
				.queue(unused -> target.addReaction(THUMBS_DOWN.getEmoji()).queue(unused1 ->
						finishedCommandAction(event)));
	}
}
