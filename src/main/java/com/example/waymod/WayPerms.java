package com.example.waymod;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

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
		// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == net.minecraft.server.permissions.*
		// У серверных источников набор прав всегда «уровневый» (LevelBasedPermissionSet):
		// оператор, гейммейстер, админ и владелец — все выше или на уровне модератора.
		if (source.permissions() instanceof LevelBasedPermissionSet set) {
			return set.level().isEqualOrHigherThan(PermissionLevel.MODERATORS);
		}
		return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.MODERATORS));
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

	/** CommandSourceStack#getPlayer() */
	private static ServerPlayer getPlayer(CommandSourceStack source) {
		return source.getPlayer();
	}
}
