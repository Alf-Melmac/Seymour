package de.webalf.seymour.util;

import de.webalf.seymour.constant.Emojis;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.function.Consumer;

import static de.webalf.seymour.util.MessageUtils.doNothing;
import static net.dv8tion.jda.api.EmbedBuilder.ZERO_WIDTH_SPACE;
import static net.dv8tion.jda.api.interactions.DiscordLocale.GERMAN;

/**
 * Util class to work with {@link Interaction}s
 *
 * @author Alf
 * @since 27.07.2021
 */
@UtilityClass
@Slf4j
public final class InteractionUtils {
	/**
	 * Acknowledge this interaction and defer the reply to a later time
	 *
	 * @param interaction to acknowledge
	 */
	public static void ephemeralDeferReply(@NonNull IReplyCallback interaction) {
		interaction.deferReply(true).queue();
	}

	/**
	 * Replies to the given interaction with the given reply
	 *
	 * @param interaction to reply to
	 * @param reply       reply text
	 */
	public static void reply(@NonNull IDeferrableCallback interaction, @NonNull String reply) {
		reply(interaction, reply, doNothing());
	}

	private static void reply(@NonNull IDeferrableCallback interaction, @NonNull String reply, Consumer<Message> success) {
		interaction.getHook().sendMessage(reply).queue(success, fail -> log.warn("Failed to send interaction reply", fail));
	}

	/**
	 * Replies to the given non-deferred interaction with the given reply
	 *
	 * @param interaction to reply to
	 * @param reply       reply text
	 */
	public static void replyNonDeferred(@NonNull IReplyCallback interaction, @NonNull String reply) {
		interaction.reply(reply).setEphemeral(true).queue();
	}

	/**
	 * Replies to the given interaction with the given modal
	 *
	 * @param interaction to reply to
	 * @param modal       reply modal
	 */
	public static void replyModal(@NonNull GenericCommandInteractionEvent interaction, @NonNull Modal modal) {
		interaction.replyModal(modal).queue();
	}

	/**
	 * Sends an empty message with the given {@link StringSelectMenu}
	 *
	 * @param interaction   to add selection menu to
	 * @param selectionMenu to add
	 */
	public static void addSelectionMenu(@NonNull IDeferrableCallback interaction, StringSelectMenu selectionMenu) {
		interaction.getHook().sendMessage(ZERO_WIDTH_SPACE).addActionRow(selectionMenu).queue();
	}

	/**
	 * Replies to the given interaction with the given reply and removes all action rows
	 *
	 * @param interaction to reply to
	 * @param reply       reply text
	 */
	public static void replyAndRemoveComponents(@NonNull ComponentInteraction interaction, @NonNull String reply) {
		interaction.editMessage(reply).setComponents().queue();
	}

	/**
	 * Replies with a checkbox
	 *
	 * @param interaction finished interaction
	 */
	public static void finishedInteraction(@NonNull IDeferrableCallback interaction) {
		interaction.getHook().deleteOriginal().queue();
	}

	/**
	 * Replies with an error message
	 *
	 * @param interaction failed interaction
	 * @param message     additional information about the error
	 */
	public static void failedSlashCommandAction(@NonNull IDeferrableCallback interaction, String message) {
		reply(interaction, Emojis.CROSS_MARK.getFormatted() + " " + message);
	}

	/**
	 * Checks if the event user has the {@link net.dv8tion.jda.api.interactions.DiscordLocale#GERMAN} locale set.
	 *
	 * @param event to check user locale for
	 * @return if german configured
	 */
	public static boolean isGerman(@NonNull GenericInteractionCreateEvent event) {
		return event.getUserLocale().equals(GERMAN);
	}
}
