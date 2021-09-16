package de.webalf.seymour.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Util class to work with {@link net.dv8tion.jda.api.entities.AbstractChannel}
 *
 * @author Alf
 * @since 13.09.2021
 */
@UtilityClass
@Slf4j
public final class ChannelUtils {
	/**
	 * Returns the channel for the given channelId in the given guild
	 *
	 * @param channelId   channel to get
	 * @param guild       in which the channel is located
	 * @param channelType information added to error output
	 * @return channel found by id or null
	 */
	public static TextChannel getChannel(Long channelId, @NonNull Guild guild, String channelType) {
		if (channelId == null) {
			log.warn("Log channel {} not configured.", channelType);
			return null;
		}
		final TextChannel channel = guild.getJDA().getTextChannelById(channelId);
		if (channel == null) {
			log.error("Configured log channel {} doesn't exist.", channelType);
			return null;
		}
		return channel;
	}
}
