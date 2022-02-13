package de.webalf.seymour.configuration.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author Alf
 * @since 29.10.2020
 */
@ConfigurationProperties("discord")
@Getter
@Setter
public class DiscordProperties {
	@NotNull
	private String token;

	private Map<Long, Long> modLog;

	private Map<Long, WelcomeChannel> welcomeChannel;

	@Data
	public static class WelcomeChannel {
		private long id;
		private String welcomeMessage;
		private String leaveMessage;

		public String getWelcomeMessage(String memberMention, int memberCount) {
			return welcomeMessage.replace("{mention}", memberMention).replace("{server-members}", Integer.toString(memberCount));
		}

		public String getLeaveMessage(String userName, int memberCount) {
			return leaveMessage.replace("{user}", userName).replace("{server-members}", Integer.toString(memberCount));
		}
	}
}
