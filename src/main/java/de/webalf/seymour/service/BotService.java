package de.webalf.seymour.service;

import de.webalf.seymour.configuration.properties.DiscordProperties;
import de.webalf.seymour.service.listener.GuildInviteListener;
import de.webalf.seymour.service.listener.GuildMemberListener;
import de.webalf.seymour.service.listener.GuildReadyListener;
import de.webalf.seymour.service.listener.InteractionListener;
import de.webalf.seymour.util.CommandClassHelper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

/**
 * @author Alf
 * @since 13.09.2021
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BotService {
	private final DiscordProperties discordProperties;
	private final InviteService inviteService;
	private final WelcomeService welcomeService;
	private final CommandsService commandsService;
	private final CommandClassHelper commandClassHelper;

	private JDA jda;

	private static final String TOKEN_PREFIX = "Bot ";

	@PostConstruct
	private void init() {
		String token = discordProperties.getToken();
		if (token.startsWith(TOKEN_PREFIX)) {
			token = token.substring(TOKEN_PREFIX.length());
		}

		jda = JDABuilder
				.createLight(token)
				.enableIntents(GUILD_MEMBERS, GUILD_INVITES, MESSAGE_CONTENT)
				.addEventListeners(
						new GuildReadyListener(inviteService, welcomeService, commandsService),
						new GuildInviteListener(inviteService),
						new GuildMemberListener(inviteService, welcomeService),
						new InteractionListener(commandClassHelper))
				.disableIntents(GUILD_MODERATION, GUILD_EXPRESSIONS, GUILD_WEBHOOKS, GUILD_VOICE_STATES, GUILD_PRESENCES, GUILD_MESSAGES, GUILD_MESSAGE_REACTIONS, GUILD_MESSAGE_TYPING, DIRECT_MESSAGES, DIRECT_MESSAGE_REACTIONS, DIRECT_MESSAGE_TYPING, SCHEDULED_EVENTS, AUTO_MODERATION_CONFIGURATION, AUTO_MODERATION_EXECUTION)
				.build();
	}

	@PreDestroy
	private void cleanUp() {
		jda.shutdownNow();
	}
}
