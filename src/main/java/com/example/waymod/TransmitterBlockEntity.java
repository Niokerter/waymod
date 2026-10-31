package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Блок-сущность передатчика. Каждые 10 тиков тянет один стак из соседнего
 * контейнера (кроме другого передатчика) и вставляет его в связанный контейнер
 * (том же измерении; координаты задаются командой /way link).
 */
public class TransmitterBlockEntity extends BlockEntity {
	private static final int INTERVAL_TICKS = 10;

	private String linkedDimension = "";
	private int targetX;
	private int targetY;
	private int targetZ;

	public TransmitterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRANSMITTER_BE, pos, state);
	}

	public boolean hasTarget() {
		return !this.linkedDimension.isEmpty();
	}

	public int targetX() {
		return this.targetX;
	}

	public int targetY() {
		return this.targetY;
	}

	public int targetZ() {
		return this.targetZ;
	}

	/** Вызывается командой /way link. */
	public void setTarget(Level level, BlockPos pos, String dimension) {
		this.linkedDimension = dimension;
		this.targetX = pos.getX();
		this.targetY = pos.getY();
		this.targetZ = pos.getZ();
		setChanged();
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TransmitterBlockEntity transmitter) {
		if (level.isClientSide() || level.getGameTime() % INTERVAL_TICKS != 0L || !transmitter.hasTarget()) {
			return;
		}
		// Работаем в том же измерении, где стоит передатчик
		if (!level.dimension().location().toString().equals(transmitter.linkedDimension)) {
			return;
		}
		BlockPos targetPos = new BlockPos(transmitter.targetX, transmitter.targetY, transmitter.targetZ);
		// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА ==
		if (level.getBlockEntity(targetPos) instanceof BlockEntity target) {
			// исходный контейнер: любой сосед, кроме другого передатчика
			for (Direction direction : Direction.values()) {
				BlockPos neighbor = pos.relative(direction);
				BlockEntity neighborBe = level.getBlockEntity(neighbor);
				if (neighborBe instanceof TransmitterBlockEntity) {
					continue;
				}
				if (neighborBe instanceof Container source) {
					pullOneStack(source, target);
					break;
				}
			}
		}
	}

	/** Перекладывает один доступный стак из контейнера в контейнер. */
	private static void pullOneStack(Container source, Object targetContainer) {
		if (!(targetContainer instanceof Container target)) {
			return;
		}
		int size = source.getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = source.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			ItemStack leftover = WayContainers.insert(target, stack);
			if (leftover.getCount() < stack.getCount()) {
				source.setItem(i, leftover);
				((BlockEntity) source).setChanged();
				((BlockEntity) target).setChanged();
				return;
			}
		}
	}

	// ---------- сохранение ----------

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString(KEY_DIM, this.linkedDimension);
		output.putInt(KEY_X, this.targetX);
		output.putInt(KEY_Y, this.targetY);
		output.putInt(KEY_Z, this.targetZ);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.linkedDimension = input.getStringOr(KEY_DIM, "");
		this.targetX = input.getIntOr(KEY_X, 0);
		this.targetY = input.getIntOr(KEY_Y, 0);
		this.targetZ = input.getIntOr(KEY_Z, 0);
	}

	private static final String KEY_DIM = "LinkedDimension";
	private static final String KEY_X = "LinkedX";
	private static final String KEY_Y = "LinkedY";
	private static final String KEY_Z = "LinkedZ";
}
