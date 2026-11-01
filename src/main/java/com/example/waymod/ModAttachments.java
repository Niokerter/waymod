package com.example.waymod;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/**
 * Вложения (Attachments) Fabric API — данные «прикреплённые» к игроку.
 */
public final class ModAttachments {
	/** Точка возврата: куда игрок мог вернуться после последнего телепорта через блок перехода. */
	public static final AttachmentType<WayTarget> RETURN_POINT = AttachmentRegistry.<WayTarget>create(
			Identifier.fromNamespaceAndPath(WayMod.MOD_ID, "return_point"),
			builder -> builder.persistent(WayTarget.CODEC).copyOnDeath()
	);

	/** Позиция передатчика, «прицельного» последним Shift+ПКМ (для /way link). */
	public static final AttachmentType<WayTarget> LINKED_TRANSMITTER = AttachmentRegistry.<WayTarget>create(
			Identifier.fromNamespaceAndPath(WayMod.MOD_ID, "linked_transmitter"),
			builder -> builder.persistent(WayTarget.CODEC)
	);

	private ModAttachments() {
	}

	/** Точка вызова из WayMod (регистрация уже произошла в статических полях). */
	public static void initialize() {
		registered(RETURN_POINT);
		registered(LINKED_TRANSMITTER);
	}

	private static void registered(Object ignored) {
	}
}
