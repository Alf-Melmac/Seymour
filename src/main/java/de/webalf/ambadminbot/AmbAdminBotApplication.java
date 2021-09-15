package de.webalf.ambadminbot;

import de.webalf.ambadminbot.service.BotService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

/**
 * @author Alf
 * @since 13.09.2021
 */
@SpringBootApplication
@ConfigurationPropertiesScan("de.webalf.ambadminbot.configuration.properties")
public class AmbAdminBotApplication {

	public static void main(String[] args) {
		final ApplicationContext applicationContext = SpringApplication.run(AmbAdminBotApplication.class, args);

		//Start discord bot
		applicationContext.getBean(BotService.class);
	}

}
