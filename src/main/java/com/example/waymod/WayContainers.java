package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Мелкие общие утилиты для контейнеров (воронки/передатчик).
 * Используются только безопасные API интерфейса Container.
 */
public final class WayContainers {
	private WayContainers() {
	}

	/**
	 * Вставляет стак в контейнер. Возвращает остаток (может быть пустым).
	 * Мержит по смежным слотам, затем заполняет пустые.
	 */
	public static ItemStack insert(Container container, ItemStack stack) {
		if (container == null || stack.isEmpty()) {
			return stack;
		}
		ItemStack carried = stack.copy();
		int size = container.getContainerSize();

		// 1) докладываем в уже частично заполненные слоты того же предмета
		for (int i = 0; i < size && !carried.isEmpty(); i++) {
			ItemStack slot = container.getItem(i);
			if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, carried)) {
				int room = slot.getMaxStackSize() - slot.getCount();
				if (room > 0) {
					int moved = Math.min(room, carried.getCount());
					slot.setCount(slot.getCount() + moved);
					container.setItem(i, slot);
					carried.setCount(carried.getCount() - moved);
				}
			}
		}

		// 2) кладём целиком в пустой слот
		for (int i = 0; i < size && !carried.isEmpty(); i++) {
			if (container.getItem(i).isEmpty()) {
				container.setItem(i, carried.copy());
				carried.setCount(0);
			}
		}
		return carried;
	}

	/** Контейнер на позиции (или null). */
	public static Container containerAt(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof Container container && container.getContainerSize() > 0) {
			return container;
		}
		return null;
	}

	/** Ищет контейнер рядом с блоком (во всех 6 направлениях), кроме позиции source-блока. */
	public static Container findNeighborContainer(Level level, BlockPos beside) {
		for (Direction direction : Direction.values()) {
			Container container = containerAt(level, beside.relative(direction));
			if (container != null) {
				return container;
			}
		}
		return null;
	}
}
