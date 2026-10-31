package com.example.waymod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа мода (entrypoint main из fabric.mod.json).
 */
public class WayMod implements ModInitializer {
	public static final String MOD_ID = "waymod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModComponents.initialize();
		ModBlocks.initialize();
		ModItems.initialize();
		ModBlockEntities.initialize();
		ModAttachments.initialize();
		WayCommand.initialize();

		LOGGER.info("[Way Mod] Загружен: блоки переходов, крафтовые спавнеры и команда /way.");
	}
}
