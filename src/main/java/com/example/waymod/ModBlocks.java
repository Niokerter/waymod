package com.example.waymod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Регистрация блоков.
 */
public final class ModBlocks {
	/** Блок перехода: ПКМ телепортирует игрока в заданную точку. */
	public static final Block WAY_BLOCK = register("way_block",
			new WayBlock(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, id("way_block")))
					.strength(2.0F, 8.0F)
					.sound(SoundType.STONE)));

	/** Крафтовый спавнер (сделан из железных решёток и око Эндера). */
	public static final Block CRAFTABLE_SPAWNER = register("craftable_spawner",
			new CraftableSpawnerBlock(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, id("craftable_spawner")))
					.strength(5.0F)
					.sound(SoundType.METAL)));

	/** Спавнер 2000 Pro: автоспавнер с прокачкой ядрами. */
	public static final Block PRO_SPAWNER = register("spawner_2000_pro",
			new ProSpawnerBlock(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, id("spawner_2000_pro")))
					.strength(5.0F, 10.0F)
					.sound(SoundType.METAL)));

	/** Авто-воронка: собирает предметы из чанка по фильтру в соседний контейнер. */
	public static final Block AUTO_HOPPER = register("auto_hopper",
			new AutoHopperBlock(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, id("auto_hopper")))
					.strength(3.0F, 8.0F)
					.sound(SoundType.METAL)));

	/** Передатчик: пересылает предметы из соседнего контейнера в связанный (по /way link). */
	public static final Block TRANSMITTER = register("hopper_transmitter",
			new TransmitterBlock(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, id("hopper_transmitter")))
					.strength(3.0F, 8.0F)
					.sound(SoundType.METAL)));

	private ModBlocks() {
	}

	/**
	 * Явный вызов гарантирует порядок регистрации: блоки -> предметы -> блок-сущности.
	 * Сами блоки регистрируются в статических полях (при инициализации класса).
	 */
	public static void initialize() {
		registered(WAY_BLOCK);
		registered(CRAFTABLE_SPAWNER);
		registered(PRO_SPAWNER);
		registered(AUTO_HOPPER);
		registered(TRANSMITTER);
	}

	private static void registered(Object ignored) {
	}

	private static Block register(String path, Block block) {
		return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(WayMod.MOD_ID, path), block);
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(WayMod.MOD_ID, path);
	}
}
