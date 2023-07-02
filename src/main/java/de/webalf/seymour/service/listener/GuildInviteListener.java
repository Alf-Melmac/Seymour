package de.webalf.seymour.service.listener;

import de.webalf.seymour.service.InviteService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
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
 *             <li>{@link GatewayIntent#GUILD_INVITES}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Permissions
 *         <ul>
 *             <li>{@link Permission#MANAGE_CHANNEL} in every channel</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author Alf
 * @since 09.09.2021
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GuildInviteListener extends ListenerAdapter {
	private final InviteService inviteService;

	@Override
	public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {
		inviteService.newInvite(event.getGuild().getIdLong(), event.getInvite());
	}

	@Override
	public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {
		inviteService.deletedInvite(event.getGuild());
	}
}
