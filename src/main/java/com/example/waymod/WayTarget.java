package com.example.waymod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Запись «куда телепортироваться».
 * Хранит измерение каноническим id (например "minecraft:overworld" или "minecraft:the_nether")
 * и координаты блока, на пол которого игрок встаёт после телепорта.
 */
public record WayTarget(String dimension, int x, int y, int z) {
	public static final Codec<WayTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("dimension").forGetter(WayTarget::dimension),
			Codec.INT.fieldOf("x").forGetter(WayTarget::x),
			Codec.INT.fieldOf("y").forGetter(WayTarget::y),
			Codec.INT.fieldOf("z").forGetter(WayTarget::z)
	).apply(instance, WayTarget::new));

	public boolean isEmpty() {
		return this.dimension == null || this.dimension.isEmpty();
	}
}
