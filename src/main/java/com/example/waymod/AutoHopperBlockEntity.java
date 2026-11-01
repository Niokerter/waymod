package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Блок-сущность авто-воронки. Каждые 10 тиков собирает предметы в чанке
 * (если совпадают с фильтром) и вставляет их в соседний контейнер.
 */
public class AutoHopperBlockEntity extends BlockEntity {
	/** Через сколько тиков следующий самопроверка. */
	private static final int INTERVAL_TICKS = 10;

	private List<String> filter = new ArrayList<>();

	public AutoHopperBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.AUTO_HOPPER_BE, pos, state);
	}

	// ---------- фильтр ----------

	/** ПКМ с предметом: добавить предмет в фильтр. */
	public void addFilter(ServerLevel level, Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		String id = itemId(stack);
		if (!this.filter.contains(id)) {
			this.filter.add(id);
			setChanged();
		}
		player.displayClientMessage(Component.translatable("message.waymod.hopper.filter_added", stack.getHoverName()), true);
	}

	/** Shift+ПКМ с предметом: убрать предмет из фильтра. */
	public void removeFilter(ServerLevel level, Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		String id = itemId(stack);
		if (this.filter.remove(id)) {
			setChanged();
		}
		player.displayClientMessage(Component.translatable("message.waymod.hopper.filter_removed", stack.getHoverName()), true);
	}

	/** Shift+ПКМ без предмета: очистить фильтр. */
	public void clearFilter(ServerLevel level, Player player) {
		this.filter.clear();
		setChanged();
		player.displayClientMessage(Component.translatable("message.waymod.hopper.filter_cleared"), true);
	}

	/** ПКМ без предмета: показать фильтр. */
	public void showFilter(ServerLevel level, Player player) {
		if (this.filter.isEmpty()) {
			player.displayClientMessage(Component.translatable("message.waymod.hopper.filter_empty"), true);
		} else {
			player.displayClientMessage(Component.translatable("message.waymod.hopper.filter_list", String.join(", ", this.filter)), true);
		}
	}

	private static String itemId(ItemStack stack) {
		// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА ==
		// Встроенный реестр предметов -> id ("minecraft:iron_ingot").
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
	}

	/** Пропускает ли фильтр этот предмет (пустой фильтр = «всё подряд»). */
	private boolean matches(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (this.filter.isEmpty()) {
			return true;
		}
		return this.filter.contains(itemId(stack));
	}

	// ---------- работа ----------

	public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, AutoHopperBlockEntity hopper) {
		if (level.isClientSide() || level.getGameTime() % INTERVAL_TICKS != 0L) {
			return;
		}
		Container target = WayContainers.findNeighborContainer(level, pos);
		if (target == null) {
			return;
		}

		AABB chunkBox = chunkBoxOf(level, pos);
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, chunkBox);
		for (ItemEntity item : items) {
			ItemStack stack = item.getItem();
			if (!hopper.matches(stack)) {
				continue;
			}
			ItemStack leftover = WayContainers.insert(target, stack);
			if (leftover.isEmpty()) {
				item.discard(); // == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == Entity#discard()
			} else if (leftover.getCount() < stack.getCount()) {
				item.setItem(leftover); // == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == ItemEntity#setItem(ItemStack)
				hopper.setChanged();
			}
		}
	}

	private static AABB chunkBoxOf(net.minecraft.world.level.Level level, BlockPos pos) {
		int minX = pos.getX() & ~15;
		int minZ = pos.getZ() & ~15;
		// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА ==
		// Границы по высоте: граница мира из его типа измерения (minY / minY+height).
		int minY = level.dimensionType().minY();
		return new AABB(
				minX, minY, minZ,
				minX + 16, minY + level.dimensionType().height(), minZ + 16);
	}

	// ---------- сохранение ----------

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString(KEY_FILTER, String.join(",", this.filter));
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String joined = input.getStringOr(KEY_FILTER, "");
		this.filter = new ArrayList<>();
		for (String part : joined.split(",")) {
			if (!part.isEmpty()) {
				this.filter.add(part);
			}
		}
	}

	private static final String KEY_FILTER = "AutoFilter";
}
