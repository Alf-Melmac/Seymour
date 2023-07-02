package de.webalf.seymour.service;

import de.webalf.seymour.configuration.properties.DiscordProperties;
import de.webalf.seymour.model.NotificationMap;
import de.webalf.seymour.util.ChannelUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import static de.webalf.seymour.util.MessageUtils.sendMessage;

/**
 * @author Alf
 * @since 13.02.2022
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class WelcomeService {
	private final DiscordProperties discordProperties;
	private final SchedulerService schedulerService;

	/**
	 * Ready guilds with existing welcome-channel-config
	 */
	private static final Set<Long> GUILD_SET = new HashSet<>();
	private static final Map<WelcomeIdentifier, ScheduledFuture<?>> SCHEDULED_WELCOMES = new NotificationMap<>();

	@Value
	@Builder
	@EqualsAndHashCode
	public static class WelcomeIdentifier {
		long guildId;
		long memberId;
	}

	/**
	 * Checks if {@link de.webalf.seymour.configuration.properties.DiscordProperties.WelcomeChannel} is configured for the given guild
	 *
	 * @param guild to initialize
	 */
	@Async
	public void initialize(@NonNull Guild guild) {
		log.info("Initializing welcome messages for {}", guild.getName());
		if (!hasWelcomeChannelConfig(guild)) {
			log.warn("No welcome channel config for {} [{}]", guild.getName(), guild.getIdLong());
			return;
		}
		GUILD_SET.add(guild.getIdLong());
	}

	public void memberJoined(@NonNull Guild guild, Member member) {
		if (!GUILD_SET.contains(guild.getIdLong())) {
			return;
		}

		final DiscordProperties.WelcomeChannel welcomeChannel = getWelcomeChannel(guild);
		final TextChannel welcomeTextChannel = getWelcomeTextChannel(welcomeChannel, guild);
		if (welcomeTextChannel == null) return;

		final WelcomeIdentifier welcomeIdentifier = buildWelcomeIdentifier(guild, member);
		SCHEDULED_WELCOMES.put(
				welcomeIdentifier,
				schedulerService.schedule(() -> {
					sendMessage(welcomeTextChannel, welcomeChannel.getWelcomeMessage(member.getAsMention(), guild.getMemberCount()));
					SCHEDULED_WELCOMES.remove(welcomeIdentifier);
				}, 1)
		);
	}

	public void memberRemoved(Guild guild, User user) {
		if (!GUILD_SET.contains(guild.getIdLong())) {
			return;
		}

		final ScheduledFuture<?> removed = SCHEDULED_WELCOMES.remove(buildWelcomeIdentifier(guild, user));
		if (removed != null) {
			//User left before welcome message was send
			return;
		}
		final DiscordProperties.WelcomeChannel welcomeChannel = getWelcomeChannel(guild);
		final TextChannel welcomeTextChannel = getWelcomeTextChannel(welcomeChannel, guild);
		if (welcomeTextChannel == null) return;

		sendMessage(welcomeTextChannel, welcomeChannel.getLeaveMessage(user.getName(), guild.getMemberCount()));
	}

	private WelcomeIdentifier buildWelcomeIdentifier(@NonNull Guild guild, @NonNull IMentionable member) {
		return WelcomeIdentifier.builder()
				.guildId(guild.getIdLong())
				.memberId(member.getIdLong())
				.build();
	}

	private boolean hasWelcomeChannelConfig(@NonNull Guild guild) {
		return discordProperties.getWelcomeChannel().containsKey(guild.getIdLong());
	}

	private DiscordProperties.WelcomeChannel getWelcomeChannel(@NonNull Guild guild) {
		return discordProperties.getWelcomeChannel().get(guild.getIdLong());
	}

	/**
	 * Returns the configured welcome channel for the given guild
	 *
	 * @param guild to get channel for
	 * @return welcome channel or null
	 */
	private TextChannel getWelcomeTextChannel(@NonNull DiscordProperties.WelcomeChannel welcomeChannel, @NonNull Guild guild) {
		return ChannelUtils.getChannel(welcomeChannel.getId(), guild, "welcomeChannel");
	}

	@Async
	public void deinitialize(@NonNull Guild guild) {
		log.info("Deinitializing welcome messages for {}", guild.getName());
		GUILD_SET.remove(guild.getIdLong());
	}
}
