package de.webalf.seymour.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.MessageChannelMixin;

import static net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION;
import static net.dv8tion.jda.api.Permission.MESSAGE_HISTORY;

/**
 * Util class to work with discord channels
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
			log.warn("Channel {} not configured.", channelType);
			return null;
		}
		final TextChannel channel = guild.getTextChannelById(channelId);
		if (channel == null) {
			log.error("Configured channel {} with id {} doesn't exist.", channelType, channelId);
			return null;
		}
		return channel;
	}

	/**
	 * Checks if the given member can view the given channel
	 *
	 * @param member  to check
	 * @param channel to view
	 * @return <code>true</code> if the member can view the given channel
	 * @see Member#hasAccess(GuildChannel)
	 */
	public static boolean canViewChannel(@NonNull Member member, @NonNull GuildChannel channel) {
		return member.hasAccess(channel);
	}

	/**
	 * Checks if the given member can add reactions in the given channel
	 *
	 * @param member  to check
	 * @param channel to add reactions in
	 * @return <code>true</code> if the member can add reactions in the given channel
	 * @see MessageChannelMixin#checkCanAddReactions()
	 */
	public static boolean canAddReaction(@NonNull Member member, @NonNull GuildChannel channel) {
		return member.hasPermission(channel, MESSAGE_HISTORY, MESSAGE_ADD_REACTION);
	}
}
