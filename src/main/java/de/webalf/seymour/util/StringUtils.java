package de.webalf.seymour.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.VanityInvite;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alf
 * @since 22.12.2020
 */
@UtilityClass
public final class StringUtils {
	private static final String NON_DIGIT_REGEX = "\\D";

	public static String removeNonDigitCharacters(@NonNull String str) {
		return str.replaceAll(NON_DIGIT_REGEX, "");
	}

	public static String invitesToString(@NonNull List<Invite> invites) {
		return invites.stream().map(invite -> "**" + invite.getCode() + "**: " + invite.getUses()).collect(Collectors.joining(", "));
	}

	public static String inviteToString(@NonNull VanityInvite invite) {
		return "**" + invite.getCode() + "**: " + invite.getUses();
	}
}
