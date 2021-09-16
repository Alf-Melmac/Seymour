package de.webalf.seymour;

import de.webalf.seymour.service.BotService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

/**
 * @author Alf
 * @since 13.09.2021
 */
@SpringBootApplication
@ConfigurationPropertiesScan("de.webalf.seymour.configuration.properties")
public class SeymourBotApplication {

	public static void main(String[] args) {
		final ApplicationContext applicationContext = SpringApplication.run(SeymourBotApplication.class, args);

		//Start discord bot
		applicationContext.getBean(BotService.class);
	}

}
