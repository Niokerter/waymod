package com.example.waymod.client;

import com.example.waymod.ModComponents;
import com.example.waymod.WayTarget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;

/**
 * Клиентская часть мода: подсказки на предметах.
 */
public class WayModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Показываем на наковальных подсказках, куда телепортирует предмет и кто живёт в спавнере.
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipHeight, lines) -> {
			WayTarget target = stack.get(ModComponents.WAY_TARGET);
			if (target != null && !target.isEmpty()) {
				lines.add(Component.translatable(
						"tooltip.waymod.way_target",
						target.dimension(), target.x(), target.y(), target.z()));
			}
			String mob = stack.get(ModComponents.MOB);
			if (mob != null) {
				lines.add(Component.translatable("tooltip.waymod.spawner_mob", mob));
			}
		});
	}
}
