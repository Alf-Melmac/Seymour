package de.webalf.seymour.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

/**
 * @author Alf
 * @since 15.09.2021
 */
@UtilityClass
public final class EmbedUtils {
	/**
	 * Simpler variant to create an {@link EmbedBuilder} with the given values
	 *
	 * @param color the embed should have
	 * @param author author that should be displayed in the embed
	 * @param fields embed fields
	 * @return embed builder with given values applied
	 */
	public static EmbedBuilder embedBuilder(Color color, @NonNull Member author, MessageEmbed.Field... fields) {
		final EmbedBuilder embedBuilder = new EmbedBuilder()
				.setColor(color)
				.setAuthor(author.getUser().getAsTag(), null, author.getUser().getAvatarUrl());
		for (MessageEmbed.Field field : fields) {
			embedBuilder.addField(field);
		}
		return embedBuilder;
	}

	public static EmbedBuilder embedBuilder(@NonNull EmbedBuilder existingBuilder, MessageEmbed.Field... fields) {
		for (MessageEmbed.Field field : fields) {
			existingBuilder.addField(field);
		}
		return existingBuilder;
	}

	/**
	 * {@link net.dv8tion.jda.api.entities.MessageEmbed.Field} which only adds a new constructor that defaults
	 * {@link net.dv8tion.jda.api.entities.MessageEmbed.Field#inline} to false
	 */
	public static class OneLineField extends MessageEmbed.Field {
		public OneLineField(String name, String value) {
			super(name, value, false);
		}
	}
}
