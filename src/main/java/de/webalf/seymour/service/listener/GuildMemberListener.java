package de.webalf.seymour.service.listener;

import de.webalf.seymour.service.InviteService;
import de.webalf.seymour.service.WelcomeService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This requires
 * <ul>
 *     <li>
 *         Intents
 *         <ul>
 *             <li>{@link GatewayIntent#GUILD_MEMBERS}</li>
 *             <li>{@link GatewayIntent#GUILD_INVITES}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Permissions
 *         <ul>
 *             <li>{@link Permission#MANAGE_CHANNEL} in every channel</li>
 *             <li>{@link Permission#MANAGE_SERVER} to retrieve invites</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author Alf
 * @since 13.02.2022
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GuildMemberListener extends ListenerAdapter {
	private final InviteService inviteService;
	private final WelcomeService welcomeService;

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		inviteService.memberJoined(event.getGuild(), event.getMember());
		welcomeService.memberJoined(event.getGuild(), event.getMember());
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		welcomeService.memberRemoved(event.getGuild(), event.getUser());
	}
}
