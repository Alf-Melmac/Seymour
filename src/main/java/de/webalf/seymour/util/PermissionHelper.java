package de.webalf.seymour.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Set;

import static de.webalf.seymour.util.ArrayUtils.add;

/**
 * @author Alf
 * @since 29.10.2020
 */
@UtilityClass
public final class PermissionHelper {
	private static final String ROLE_PREFIX = "Slotbot_";
	private static final String ROLE_SYS_ADMIN = ROLE_PREFIX + "Sys_Admin";
	private static final String ROLE_ADMIN = ROLE_PREFIX + "Admin";
	private static final String ROLE_EVENT_MANGE = ROLE_PREFIX + "Event_Manage";
	private static final String ROLE_EVERYONE = "@everyone";
	public static final Set<String> KNOWN_ROLE_NAMES = Set.of(ROLE_SYS_ADMIN, ROLE_ADMIN, ROLE_EVENT_MANGE, ROLE_EVERYONE);

	@Getter
	@AllArgsConstructor
	public enum Authorization {
		SYS_ADMINISTRATION(new String[]{ROLE_SYS_ADMIN}),
		ADMINISTRATIVE(add(SYS_ADMINISTRATION.getRoles(), ROLE_ADMIN)),
		EVENT_MANAGE(add(ADMINISTRATIVE.getRoles(), ROLE_EVENT_MANGE)),
		NONE(new String[]{ROLE_EVERYONE});

		@NonNull
		private final String[] roles;
	}
}
