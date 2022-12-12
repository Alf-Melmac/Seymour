package de.webalf.seymour.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Util class to work with {@link Message}s
 *
 * @author Alf
 * @since 02.01.2021
 */
@UtilityClass
@Slf4j
public final class MessageUtils {
	/**
	 * Sends the given message in the given channel
	 *
	 * @param channel to send into
	 * @param message to send
	 */
	public static void sendMessage(@NonNull MessageChannel channel, @NotNull String message) {
		sendMessage(channel, message, doNothing());
	}

	/**
	 * Sends the given text in the channel of the given message
	 * Shortcut for {@code sendMessage(interaction.getMessageChannel(), text)}
	 *
	 * @param interaction on which channel text should be sent
	 * @param message     to send
	 */
	public static void sendMessage(@NonNull Interaction interaction, @NotNull String message) {
		sendMessage(interaction.getMessageChannel(), message);
	}

	/**
	 * Sends the given message in the given channel and queues the given success consumer
	 *
	 * @param channel to send into
	 * @param message to send
	 * @param success message consumer
	 */
	public static void sendMessage(@NonNull MessageChannel channel, @NotNull String message, Consumer<Message> success) {
		channel.sendMessage(message).queue(success);
	}

	public static void sendMessage(@NonNull MessageChannel channel, @NotNull EmbedBuilder embedBuilder) {
		channel.sendMessageEmbeds(embedBuilder.build()).queue();
	}

	static Consumer<Message> doNothing() {
		return unused -> {};
	}
}
