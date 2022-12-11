package de.webalf.seymour.model.annotations;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alf
 * @since 11.12.2022
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DiscordLocalization {
	DiscordLocale locale();

	String name();
}
