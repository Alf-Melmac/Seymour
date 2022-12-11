package de.webalf.seymour.model.annotations;

import net.dv8tion.jda.api.Permission;
import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alf
 * @since 15.07.2021
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@IndexAnnotated
public @interface SlashCommand {
	String name();

	String description();

	Permission authorization();

	int optionPosition() default -1;
}
