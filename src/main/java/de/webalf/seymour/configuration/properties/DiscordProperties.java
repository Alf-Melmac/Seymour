package de.webalf.seymour.configuration.properties;

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
}
