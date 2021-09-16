package de.webalf.seymour.model.annotations;

import de.webalf.seymour.util.PermissionHelper.Authorization;
import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static de.webalf.seymour.util.PermissionHelper.Authorization.ADMINISTRATIVE;

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

	Authorization authorization() default ADMINISTRATIVE;

	int optionPosition() default -1;
}
