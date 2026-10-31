package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Блок-сущность крафтового спавнера. Наследует всю логику ванильного спавнера,
 * добавляя только способ задать моба из предмета.
 */
public class CraftableSpawnerBlockEntity extends SpawnerBlockEntity {
	public CraftableSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	/**
	 * Вызывается при установке спавнера из предмета, на котором есть компонент waymod:mob
	 * (компонент создаёт команда /way spawner).
	 */
	public void applyItemMob(String entityId, Level level, BlockPos pos) {
		if (entityId == null || entityId.isEmpty()) {
			return;
		}
		EntityType.byString(entityId).ifPresent(type -> setMob(type, level, pos));
	}

	/**
	 * Задаёт моба напрямую (обёртка над ванильной логикой спавнера).
	 */
	private void setMob(EntityType<?> type, Level level, BlockPos pos) {
		// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА ==
		// In вышем контексте: setEntityId(EntityType<?>, Level, RandomSource, BlockPos)
		this.getSpawner().setEntityId(type, level, level.getRandom(), pos);
		setChanged();
	}
}
