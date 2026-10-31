package com.example.waymod;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Permissions;
import net.minecraft.server.level.ServerPlayer;

/**
 * Права доступа по веткам команды /way:
 *   give / set / link            — «команды модератора» (как /gamemode), нужен оператор;
 *   spawner / spawnerpro         — оператор ИЛИ игрок в креативе;
 *   выживание без прав — ничего не выдаёт.
 */
public final class WayPerms {
	private WayPerms() {
	}

	/** Уровень модератора: операторы, консоль/PvE-серверные источники. */
	public static boolean moderator(CommandSourceStack source) {
		// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == Permissions.COMMANDS_MODERATOR
		return source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR);
	}

	/**
	 * Выдача спавнеров: оператор ИЛИ игрок в креативе.
	 * Обычный выживающий игрок получить спавнер командой не может — вообще никак.
	 */
	public static boolean spawners(CommandSourceStack source) {
		if (moderator(source)) {
			return true;
		}
		ServerPlayer player = getPlayer(source);
		return player != null && player.isCreative();
	}

	/** == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == CommandSourceStack#getPlayer() */
	private static ServerPlayer getPlayer(CommandSourceStack source) {
		return source.getPlayer();
	}
}
