package de.webalf.seymour.service;

import de.webalf.seymour.configuration.properties.DiscordProperties;
import de.webalf.seymour.util.ChannelUtils;
import de.webalf.seymour.util.EmbedUtils.OneLineField;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
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
	private static final Map<Long, List<Invite>> GUILD_INVITES_MAP = new HashMap<>();
	private static Integer VANITY_URL_USES = null;

	/**
	 * Fills the invite cache for the given guild
	 *
	 * @param guild to save invites for
	 */
	private static void fillInvitesMap(@NonNull Guild guild) {
		guild.retrieveInvites().queue(invites -> GUILD_INVITES_MAP.put(guild.getIdLong(), new ArrayList<>(invites)));
	}

	private static void setVanityUrlUses(@NonNull Guild guild) {
		if (guild.getVanityCode() != null) {
			guild.retrieveVanityInvite().queue(vanityInvite -> VANITY_URL_USES = vanityInvite.getUses());
		}
	}

	/**
	 * @see #fillInvitesMap(Guild)
	 */
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

	private static boolean validGuild(@NonNull Guild guild) {
		return validGuild(guild.getIdLong());
	}

	private static boolean validGuild(long guildId) {
		final boolean validGuild = GUILD_INVITES_MAP.containsKey(guildId);
		if (!validGuild) log.info("Invalid guild {}. Missing modLog configuration?", guildId);
		return validGuild;
	}

	/**
	 * Saves a new invite to the cache
	 *
	 * @param guildId in which the invite was created
	 * @param invite  created invite
	 */
	public void newInvite(long guildId, Invite invite) {
		if (!validGuild(guildId)) {
			return;
		}
		log.trace("Detected new invite in {}", guildId);

		final List<Invite> invites = new ArrayList<>(GUILD_INVITES_MAP.get(guildId)); //Just to be sure it's modifiable
		invites.add(invite);
		GUILD_INVITES_MAP.put(guildId, invites);
	}

	/**
	 * Refreshes the invite cache for the given guild
	 *
	 * @param guild to refresh invites for
	 */
	public void deletedInvite(Guild guild) {
		if (!validGuild(guild)) {
			return;
		}
		log.trace("Detected deleted invite in {}", guild.getIdLong());

		fillInvitesMap(guild);
	}

	/**
	 * Posts a message to the {@link DiscordProperties#modLog} channel and tries to append the used invite link
	 *
	 * @param guild  joined into this guild
	 * @param member joined member
	 */
	public void memberJoined(@NonNull Guild guild, Member member) {
		if (!validGuild(guild)) {
			return;
		}
		log.trace("Detected member {} joined in {}", member.getIdLong(), guild.getIdLong());

		guild.retrieveInvites().queue(invites -> {
			final TextChannel modLogChannel = getModLogChannel(guild);
			if (modLogChannel == null) return;

			final List<Invite> oldInvites = GUILD_INVITES_MAP.get(guild.getIdLong());

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
			GUILD_INVITES_MAP.put(guild.getIdLong(), new ArrayList<>(invites));
		});
	}

	private static void checkForVanityUrl(@NotNull Guild guild, Member member, List<Invite> invites, List<Invite> oldInvites, TextChannel modLogChannel) {
		if (VANITY_URL_USES != null) {
			guild.retrieveVanityInvite()
					.queue(vanityInvite -> {
						if (VANITY_URL_USES != vanityInvite.getUses()) {
							sendLogEmbed(member, modLogChannel, vanityInvite);
							VANITY_URL_USES = vanityInvite.getUses();
						} else {
							sendErrorEmbed(member, modLogChannel, oldInvites, invites, VANITY_URL_USES, vanityInvite);
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
}
