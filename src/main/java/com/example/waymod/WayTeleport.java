package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Телепортация. Вся «опасная» для компиляции логика собрана здесь:
 * в случае расхождений в маппингах достаточно поправить отдельные строки этого файла
 * (инструкция в README, раздел «Если компилятор ругается»).
 */
public final class WayTeleport {
	private WayTeleport() {
	}

	/**
	 * Обработка клика по блоку перехода.
	 * Обычный ПКМ — телепорт к цели блока (и сохранение точки возврата).
	 * Shift+ПКМ — телепорт в сохранённую точку возврата.
	 */
	public static void handleWayBlockClick(ServerPlayer player, ServerLevel level, WayTarget target) {
		if (player.isShiftKeyDown()) {
			teleportBack(player);
		} else {
			teleportToTarget(player, level, target);
		}
	}

	private static void teleportToTarget(ServerPlayer player, ServerLevel level, WayTarget target) {
		ServerLevel destination = levelOf(player.server, target.dimension());
		if (destination == null) {
			player.displayClientMessage(Component.translatable("message.waymod.bad_dimension", target.dimension()), true);
			return;
		}
		// Точка возврата: где стоим сейчас (координаты блока под нами).
		WayTarget back = new WayTarget(
				player.level().dimension().location().toString(),
				player.getBlockX(),
				player.getBlockY(),
				player.getBlockZ()
		);
		teleport(player, destination, target);
		player.setAttached(ModAttachments.RETURN_POINT, back);
		player.displayClientMessage(Component.translatable(
				"message.waymod.teleported", target.dimension(), target.x(), target.y(), target.z()), true);
	}

	private static void teleportBack(ServerPlayer player) {
		WayTarget back = player.getAttached(ModAttachments.RETURN_POINT);
		if (back == null) {
			player.displayClientMessage(Component.translatable("message.waymod.no_return"), true);
			return;
		}
		ServerLevel destination = levelOf(player.server, back.dimension());
		if (destination == null) {
			player.displayClientMessage(Component.translatable("message.waymod.no_return"), true);
			return;
		}
		teleport(player, destination, back);
		player.removeAttached(ModAttachments.RETURN_POINT);
		player.displayClientMessage(Component.translatable(
				"message.waymod.returned", back.dimension(), back.x(), back.y(), back.z()), true);
	}

	/** Канонический id измерения -> уровень сервера (или null, если измерения нет). */
	public static ServerLevel levelOf(MinecraftServer server, String dimensionId) {
		return server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionId)));
	}

	/**
	 * == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА ==
	 * Самая «хрупкая» строка мода: телепорт между измерениями (1.21.9+).
	 * Ожидаемое API:
	 *   player.teleport(new TeleportTarget(ServerLevel, Vec3, Vec3, float yaw, float pitch, TeleportTarget.NO_OP));
	 * Если компилятор не найдёт класс — попробуйте варианты import из README (раздел «Если компилятор ругается»).
	 */
	private static void teleport(ServerPlayer player, ServerLevel destination, WayTarget target) {
		player.teleport(new net.minecraft.world.entity.TeleportTarget(
				destination,
				Vec3.atBottomCenterOf(new BlockPos(target.x(), target.y(), target.z())),
				Vec3.ZERO,
				player.getYRot(),
				player.getXRot(),
				net.minecraft.world.entity.TeleportTarget.NO_OP
		));
	}
}
