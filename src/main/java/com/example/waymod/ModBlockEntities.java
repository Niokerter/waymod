package com.example.waymod;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Регистрация блок-сущностей (BlockEntity).
 */
public final class ModBlockEntities {
	public static final BlockEntityType<WayBlockEntity> WAY_BE = register("way_block",
			FabricBlockEntityTypeBuilder.<WayBlockEntity>create(WayBlockEntity::new, ModBlocks.WAY_BLOCK).build());

	public static final BlockEntityType<CraftableSpawnerBlockEntity> CRAFT_SPAWNER_BE = register("craftable_spawner",
			FabricBlockEntityTypeBuilder.<CraftableSpawnerBlockEntity>create(CraftableSpawnerBlockEntity::new, ModBlocks.CRAFTABLE_SPAWNER).build());

	public static final BlockEntityType<ProSpawnerBlockEntity> PRO_SPAWNER_BE = register("spawner_2000_pro",
			FabricBlockEntityTypeBuilder.<ProSpawnerBlockEntity>create(ProSpawnerBlockEntity::new, ModBlocks.PRO_SPAWNER).build());

	public static final BlockEntityType<AutoHopperBlockEntity> AUTO_HOPPER_BE = register("auto_hopper",
			FabricBlockEntityTypeBuilder.<AutoHopperBlockEntity>create(AutoHopperBlockEntity::new, ModBlocks.AUTO_HOPPER).build());

	public static final BlockEntityType<TransmitterBlockEntity> TRANSMITTER_BE = register("hopper_transmitter",
			FabricBlockEntityTypeBuilder.<TransmitterBlockEntity>create(TransmitterBlockEntity::new, ModBlocks.TRANSMITTER).build());

	private ModBlockEntities() {
	}

	public static void initialize() {
		registered(WAY_BE);
		registered(CRAFT_SPAWNER_BE);
		registered(PRO_SPAWNER_BE);
		registered(AUTO_HOPPER_BE);
		registered(TRANSMITTER_BE);
	}

	private static void registered(Object ignored) {
	}

	private static <T extends BlockEntity> BlockEntityType<T> register(String path, BlockEntityType<T> type) {
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(WayMod.MOD_ID, path), type);
	}
}
