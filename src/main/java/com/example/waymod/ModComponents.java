package com.example.waymod;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Пользовательские компоненты данных (Data Components) мода.
 */
public final class ModComponents {
	/** Цель телепортации, записанная на предмете блока перехода. */
	public static final DataComponentType<WayTarget> WAY_TARGET = DataComponentType.<WayTarget>builder()
			.persistent(WayTarget.CODEC)
			.build();

	/** Тип моба, заданный для крафтового спавнера (например "minecraft:zombie"). */
	public static final DataComponentType<String> MOB = DataComponentType.<String>builder()
			.persistent(Codec.STRING)
			.build();

	private static boolean initialized = false;

	private ModComponents() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}
		initialized = true;
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("way_target"), WAY_TARGET);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("mob"), MOB);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(WayMod.MOD_ID, path);
	}
}
