package de.webalf.seymour.service;

import de.webalf.seymour.configuration.properties.DiscordProperties;
import de.webalf.seymour.util.ChannelUtils;
import de.webalf.seymour.util.EmbedUtils.OneLineField;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VanityInvite;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.*;

import static de.webalf.seymour.util.EmbedUtils.embedBuilder;
import static de.webalf.seymour.util.MessageUtils.sendMessage;
import static de.webalf.seymour.util.StringUtils.inviteToString;
import static de.webalf.seymour.util.StringUtils.invitesToString;

/**
 * @author Alf
 * @since 09.09.2021
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class InviteService {
	private final DiscordProperties discordProperties;

	@Getter
	private static final Map<Long, List<Invite>> GUILD_INVITES = new HashMap<>();
	private static final Map<Long, Integer> VANITY_URL_USES = new HashMap<>();

	/**
	 * Fills the invite cache for the given guild
	 *
	 * @param guild to save invites for
	 */
	private static void fillInvitesMap(@NonNull Guild guild) {
		guild.retrieveInvites().queue(invites -> GUILD_INVITES.put(guild.getIdLong(), new ArrayList<>(invites)));
	}

	private static void setVanityUrlUses(@NonNull Guild guild) {
		if (guild.getVanityCode() != null) {
			guild.retrieveVanityInvite().queue(vanityInvite -> VANITY_URL_USES.put(guild.getIdLong(), vanityInvite.getUses()));
		}
	}

	/**
	 * @see #fillInvitesMap(Guild)
	 * @see #setVanityUrlUses(Guild)
	 */
	@Async
	public void initialize(@NonNull Guild guild) {
		log.info("Initializing invite cache for {}", guild.getName());
		final TextChannel modLogChannel = getModLogChannel(guild);
		if (modLogChannel == null) {
			log.warn("No mod log channel for {} ({}). No invite tracking", guild.getName(), guild.getIdLong());
			return;
		}
		fillInvitesMap(guild);
		setVanityUrlUses(guild);
	}

	private static boolean invalidGuild(@NonNull Guild guild) {
		return invalidGuild(guild.getIdLong());
	}

	private static boolean invalidGuild(long guildId) {
		final boolean validGuild = GUILD_INVITES.containsKey(guildId);
		if (!validGuild) log.info("Invalid guild {}. Missing modLog configuration?", guildId);
		return !validGuild;
	}

	/**
	 * Saves a new invite to the cache
	 *
	 * @param guildId in which the invite was created
	 * @param invite  created invite
	 */
	public void newInvite(long guildId, Invite invite) {
		if (invalidGuild(guildId)) {
			return;
		}
		log.trace("Detected new invite in {}", guildId);

		final List<Invite> invites = new ArrayList<>(GUILD_INVITES.get(guildId)); //Just to be sure it's modifiable
		invites.add(invite);
		GUILD_INVITES.put(guildId, invites);
	}

	/**
	 * Refreshes the invite cache for the given guild
	 *
	 * @param guild to refresh invites for
	 */
	public void deletedInvite(Guild guild) {
		if (invalidGuild(guild)) {
			return;
		}
		log.trace("Detected deleted invite in {}", guild.getIdLong());

		fillInvitesMap(guild);
	}

	/**
	 * Posts a message to the {@link DiscordProperties#getModLog()} channel and tries to append the used invite link
	 *
	 * @param guild  joined into this guild
	 * @param member joined member
	 */
	public void memberJoined(@NonNull Guild guild, Member member) {
		if (invalidGuild(guild)) {
			return;
		}
		log.trace("Detected member {} joined in {}", member.getIdLong(), guild.getIdLong());

		guild.retrieveInvites().queue(invites -> {
			final TextChannel modLogChannel = getModLogChannel(guild);
			if (modLogChannel == null) return;

			final List<Invite> oldInvites = GUILD_INVITES.get(guild.getIdLong());

			boolean found = false;
			if (invites.size() == oldInvites.size()) {
				for (Invite invite : invites) {
					final Optional<Invite> optionalInvite = oldInvites.stream()
							.filter(oldInvite -> oldInvite.getCode().equals(invite.getCode()) && oldInvite.getUses() != invite.getUses())
							.findAny();
					if (optionalInvite.isPresent()) {
						sendLogEmbed(member, modLogChannel, optionalInvite.get());
						found = true;
						break;
					}
				}
			} else { //Link is now invalid
				oldInvites.removeAll(invites);
				for (Invite oldInvite : oldInvites) {
					sendLogEmbed(member, modLogChannel, oldInvite);
					found = true;
				}
			}
			if (!found) {
				checkForVanityUrl(guild, member, invites, oldInvites, modLogChannel);
			}
			GUILD_INVITES.put(guild.getIdLong(), new ArrayList<>(invites));
		});
	}

	private static void checkForVanityUrl(@NotNull Guild guild, Member member, List<Invite> invites, List<Invite> oldInvites, TextChannel modLogChannel) {
		final Integer vanityUrlUses = VANITY_URL_USES.get(guild.getIdLong());
		if (vanityUrlUses != null) {
			guild.retrieveVanityInvite()
					.queue(vanityInvite -> {
						if (vanityUrlUses != vanityInvite.getUses()) {
							sendLogEmbed(member, modLogChannel, vanityInvite);
							VANITY_URL_USES.put(guild.getIdLong(), vanityInvite.getUses());
						} else {
							sendErrorEmbed(member, modLogChannel, oldInvites, invites, vanityUrlUses, vanityInvite);
						}
					}, fail -> sendErrorEmbed(member, modLogChannel, oldInvites, invites, fail));
		} else {
			sendErrorEmbed(member, modLogChannel, oldInvites, invites);
		}
	}

	/**
	 * Returns the configured mod log channel for the given guild
	 *
	 * @param guild to get channel for
	 * @return mod log channel or null
	 */
	private TextChannel getModLogChannel(@NonNull Guild guild) {
		return ChannelUtils.getChannel(discordProperties.getModLog().get(guild.getIdLong()), guild, "modLog");
	}

	private static void sendLogEmbed(Member member, TextChannel modLogChannel, Invite oldInvite) {
		sendMessage(modLogChannel, buildInviteEmbed(member, oldInvite));
	}

	private static void sendLogEmbed(Member member, TextChannel modLogChannel, VanityInvite oldInvite) {
		sendMessage(modLogChannel, buildInviteEmbed(member, oldInvite));
	}

	private static EmbedBuilder buildInviteEmbed(@NonNull Member member, @NonNull Invite invite) {
		return embedBuilder(Color.GREEN, member, new OneLineField("Member used invite", member.getAsMention() + " **" + invite.getCode() + "**"));
	}

	private static EmbedBuilder buildInviteEmbed(@NonNull Member member, @NonNull VanityInvite invite) {
		return embedBuilder(Color.ORANGE, member, new OneLineField("Member used vanity invite", member.getAsMention() + " **" + invite.getCode() + "**"));
	}

	private static void sendErrorEmbed(Member member, TextChannel modLogChannel, List<Invite> oldInvites, List<Invite> invites) {
		sendMessage(modLogChannel, buildErrorEmbed(member, oldInvites, invites));
	}

	private static void sendErrorEmbed(Member member, TextChannel modLogChannel, List<Invite> oldInvites, List<Invite> invites, Integer oldVanityUsages, VanityInvite vanityInvite) {
		sendMessage(modLogChannel, buildErrorEmbed(member, oldInvites, invites, oldVanityUsages, vanityInvite));
	}

	private static void sendErrorEmbed(Member member, TextChannel modLogChannel, List<Invite> oldInvites, List<Invite> invites, Throwable fail) {
		sendMessage(modLogChannel, buildErrorEmbed(member, oldInvites, invites, fail));
	}

	private static EmbedBuilder buildErrorEmbed(@NonNull Member member, List<Invite> oldInvites, List<Invite> invites) {
		return embedBuilder(Color.RED, member,
				new OneLineField("Member used invite", member.getAsMention() + " **Couldn't acquire invite code**"),
				new OneLineField("Old Invites", invitesToString(oldInvites)),
				new OneLineField("New Invites", invitesToString(invites)));
	}

	private static EmbedBuilder buildErrorEmbed(Member member, List<Invite> oldInvites, List<Invite> invites, Integer oldVanityUsages, VanityInvite vanityInvite) {
		return embedBuilder(buildErrorEmbed(member, oldInvites, invites),
				new OneLineField("Old Vanity Url usages", Integer.toString(oldVanityUsages)),
				new OneLineField("New Vanity Url", inviteToString(vanityInvite)));
	}

	private static EmbedBuilder buildErrorEmbed(Member member, List<Invite> oldInvites, List<Invite> invites, @NonNull Throwable fail) {
		return embedBuilder(buildErrorEmbed(member, oldInvites, invites),
				new OneLineField("Error while getting vanity url", fail.getMessage()));
	}

	@Async
	public void deinitialize(@NonNull Guild guild) {
		log.info("Deinitializing invite cache for {}", guild.getName());
		GUILD_INVITES.remove(guild.getIdLong());
		VANITY_URL_USES.remove(guild.getIdLong());
	}
}
