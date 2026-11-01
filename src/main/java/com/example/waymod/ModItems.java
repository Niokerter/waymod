package com.example.waymod;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Регистрация предметов и вкладки творчества.
 */
public final class ModItems {
	// --- Блоки (блок-предметы) ---
	public static final Item WAY_BLOCK_ITEM = register("way_block",
			new WayBlockItem(ModBlocks.WAY_BLOCK, blockItemProperties("way_block")));

	public static final Item CRAFTABLE_SPAWNER_ITEM = register("craftable_spawner",
			new CraftableSpawnerItem(ModBlocks.CRAFTABLE_SPAWNER, blockItemProperties("craftable_spawner")));

	public static final Item PRO_SPAWNER_ITEM = register("spawner_2000_pro",
			new ProSpawnerItem(ModBlocks.PRO_SPAWNER, blockItemProperties("spawner_2000_pro")));

	public static final Item AUTO_HOPPER_ITEM = register("auto_hopper",
			new BlockItem(ModBlocks.AUTO_HOPPER, blockItemProperties("auto_hopper")));

	public static final Item TRANSMITTER_ITEM = register("hopper_transmitter",
			new BlockItem(ModBlocks.TRANSMITTER, blockItemProperties("hopper_transmitter")));

	// --- Ядра прокачки ---
	public static final Item SPEED_CORE = register("speed_core", new Item(plainItemProperties("speed_core")));
	public static final Item CAPACITY_CORE = register("capacity_core", new Item(plainItemProperties("capacity_core")));
	public static final Item KILLER_CORE = register("killer_core", new Item(plainItemProperties("killer_core")));

	/** Собственная вкладка творчества с предметами мода. */
	public static final CreativeModeTab WAY_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
			Identifier.fromNamespaceAndPath(WayMod.MOD_ID, "main"),
			FabricItemGroup.builder()
					.title(Component.translatable("itemGroup.waymod"))
					.icon(() -> new ItemStack(WAY_BLOCK_ITEM))
					.displayItems((parameters, output) -> {
						output.accept(WAY_BLOCK_ITEM);
						output.accept(CRAFTABLE_SPAWNER_ITEM);
						output.accept(PRO_SPAWNER_ITEM);
						output.accept(SPEED_CORE);
						output.accept(CAPACITY_CORE);
						output.accept(KILLER_CORE);
						output.accept(AUTO_HOPPER_ITEM);
						output.accept(TRANSMITTER_ITEM);
					})
					.build());

	private ModItems() {
	}

	public static void initialize() {
		registered(WAY_BLOCK_ITEM);
		registered(CRAFTABLE_SPAWNER_ITEM);
		registered(PRO_SPAWNER_ITEM);
		registered(AUTO_HOPPER_ITEM);
		registered(TRANSMITTER_ITEM);
		registered(SPEED_CORE);
		registered(CAPACITY_CORE);
		registered(KILLER_CORE);
		registered(WAY_TAB);
	}

	private static void registered(Object ignored) {
	}

	/** Свойства для блок-предметов (описание с префиксом блока). */
	private static Item.Properties blockItemProperties(String path) {
		return new Item.Properties()
				.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WayMod.MOD_ID, path)))
				.useBlockDescriptionPrefix();
	}

	/** Свойства для обычных предметов (ядра). */
	private static Item.Properties plainItemProperties(String path) {
		return new Item.Properties()
				.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WayMod.MOD_ID, path)));
	}

	private static Item register(String path, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(WayMod.MOD_ID, path), item);
	}
}
