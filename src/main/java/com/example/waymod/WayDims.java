package com.example.waymod;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Работа с измерениями: псевдонимы, канонические id, проверка существования.
 */
public final class WayDims {
	/** Удобные псевдонимы: /way give overworld -1412 69 121 */
	private static final Map<String, String> ALIASES = Map.of(
			"overworld", "minecraft:overworld",
			"nether", "minecraft:the_nether",
			"the_end", "minecraft:the_end"
	);

	private WayDims() {
	}

	/**
	 * Разбирает аргумент измерения. Возвращает канонический id ("minecraft:overworld") или null, если это не измерение.
	 */
	public static String tryResolve(String argument) {
		if (argument == null) {
			return null;
		}
		String input = argument.toLowerCase(Locale.ROOT);
		String alias = ALIASES.get(input);
		if (alias != null) {
			return alias;
		}
		// Части после ":" уточняем по псевдонимам: "waymod:overworld" тоже работает.
		int colon = input.indexOf(':');
		if (colon < 0) {
			return "minecraft:" + input;
		}
		String namespace = input.substring(0, colon);
		String path = input.substring(colon + 1);
		if (namespace.equals(WayMod.MOD_ID)) {
			String innerAlias = ALIASES.get(path);
			if (innerAlias != null) {
				return innerAlias;
			}
		}
		// Проверяем, что строка вообще может быть id ("minecraft:xxx").
		try {
			ResourceLocation.parse(input);
		} catch (RuntimeException invalid) {
			return null;
		}
		return input;
	}

	/** Есть ли такое измерение на сервере. */
	public static boolean exists(MinecraftServer server, String dimensionId) {
		try {
			// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == server.levelKeys()
			return server.levelKeys().anyMatch(key -> key.location().equals(mustId(dimensionId)));
		} catch (RuntimeException unknown) {
			return false;
		}
	}

	public static ResourceKey<Level> keyOf(String dimensionId) {
		return ResourceKey.create(Registries.DIMENSION, mustId(dimensionId));
	}

	/** Подсказки в команде: псевдонимы измерений. */
	public static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder) {
		ALIASES.keySet().forEach(builder::suggest);
		return builder.buildFuture();
	}

	private static ResourceLocation mustId(String dimensionId) {
		if (dimensionId.indexOf(':') < 0) {
			// нет префикса — считаем ванильным измерением
			return ResourceLocation.withDefaultNamespace(dimensionId.toLowerCase(Locale.ROOT));
		}
		return ResourceLocation.parse(dimensionId.toLowerCase(Locale.ROOT));
	}
}
