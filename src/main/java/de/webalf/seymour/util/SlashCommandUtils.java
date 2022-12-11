package de.webalf.seymour.util;

import de.webalf.seymour.model.annotations.SlashCommand;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.atteo.classindex.ClassIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static de.webalf.seymour.util.MentionUtils.isSnowflake;
import static de.webalf.seymour.util.StringUtils.removeNonDigitCharacters;

/**
 * @author Alf
 * @since 18.07.2021
 */
@UtilityClass
public final class SlashCommandUtils {
	public static final Map<String, Class<?>> commandToClassMap = new HashMap<>();

	static {
		final Iterable<Class<?>> commandList = ClassIndex.getAnnotated(SlashCommand.class);
		StreamSupport.stream(commandList.spliterator(), false)
				.forEach(command -> commandToClassMap.put(CommandClassHelper.getSlashCommand(command).name().toLowerCase(), command));
	}

	/**
	 * Searches for the given slash command the matching class annotated with {@link SlashCommand}
	 *
	 * @param command to search
	 * @return matching class or null if not found
	 */
	public static Class<?> get(@NonNull String command) {
		return commandToClassMap.get(command.toLowerCase());
	}

	/**
	 * Returns the string value of the given not null {@link OptionMapping}
	 *
	 * @param option to get text from
	 * @return string
	 */
	public static String getStringOption(@NonNull OptionMapping option) {
		return option.getAsString();
	}

	/**
	 * Returns the string value of the given nullable {@link OptionMapping}
	 *
	 * @param option to get text from
	 * @return string or null
	 */
	public static String getOptionalStringOption(OptionMapping option) {
		return option == null ? null : getStringOption(option);
	}

	/**
	 * Returns the {@link GuildMessageChannel} of the given not null {@link OptionMapping}.
	 * Returns null if the channel can't be accessed (e.g. private thread)
	 *
	 * @param option to get channel from
	 * @return guild message channel
	 */
	public static GuildMessageChannel getChannelOptionAsGuildMessageChannel(@NonNull OptionMapping option) {
		GuildChannelUnion channel;
		try {
			channel = option.getAsChannel();
		} catch (Exception ignored) {
			return null;
		}

		if (!(channel instanceof GuildMessageChannel)) {
			return null;
		}

		return channel.asGuildMessageChannel();
	}

	public static String getMessageIdOption(@NonNull OptionMapping option) {
		final String messageId = removeNonDigitCharacters(getStringOption(option));
		return isSnowflake(messageId) ? messageId : null;
	}
}
