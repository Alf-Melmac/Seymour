package de.webalf.seymour.service.listener;

import de.webalf.seymour.service.CommandsService;
import de.webalf.seymour.service.WelcomeService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Alf
 * @since 15.07.2021
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GuildReadyListener extends ListenerAdapter {
	private final WelcomeService welcomeService;
	private final CommandsService commandsService;

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		final Guild guild = event.getGuild();
		log.trace("Guild ready: {}", guild.getName());
		initializeGuild(guild);
	}

	@Override
	public void onGuildAvailable(GuildAvailableEvent event) {
		final Guild guild = event.getGuild();
		log.trace("Guild available: {}", guild.getName());
		initializeGuild(guild);
	}

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		final Guild guild = event.getGuild();
		log.trace("Guild join: {}", guild.getName());
		initializeGuild(guild);
	}

	private void initializeGuild(@NonNull Guild guild) {
		welcomeService.initialize(guild);
		commandsService.updateCommands(guild);
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		final Guild guild = event.getGuild();
		welcomeService.deinitialize(guild);
	}
}
