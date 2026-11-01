package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * Блок-сущность «Спавнер 2000 Pro»: сам спавнит мобов по три в строку,
 * поддерживает прокачку ядрами и авто-убийство с добычей ×5.
 */
public class ProSpawnerBlockEntity extends BlockEntity {
	private static final String KEY_MOB = "ProMob";
	private static final String KEY_DELAY = "ProDelay";
	private static final String KEY_CAPACITY = "ProCapacity";
	private static final String KEY_KILLER = "ProKiller";
	private static final String KEY_COOLDOWN = "ProCooldown";
	private static final String KEY_ROWS = "ProRows";

	/** Стартовые значения. */
	private static final int START_DELAY = 100;   // 5 секунд
	private static final int MIN_DELAY = 10;      // 0.5 секунды (пол ядра скорости)
	private static final int START_CAPACITY = 10;
	private static final int MAX_CAPACITY = 150;
	private static final int LOOT_MULTIPLIER = 5;
	private static final int START_ROWS = 3;      // 3 строки: по мобу в каждую
	private static final int MAX_ROWS = 12;

	private String mobId = "";
	private int delay = START_DELAY;
	private int capacity = START_CAPACITY;
	private boolean killer = false;
	private int cooldown = 0;
	private int rows = START_ROWS;

	public ProSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PRO_SPAWNER_BE, pos, state);
	}

	// ---------- прокачка ----------

	public void applySpeedCore() {
		this.delay = Math.max(MIN_DELAY, this.delay / 2);
		setChanged();
	}

	/** Ванильная версия: сахар — минус 2 тика задержки за штуку, **без предела** (минимум 1 тик). */
	public void applySugar() {
		this.delay = Math.max(1, this.delay - 2);
		setChanged();
	}

	public void applyCapacityCore() {
		this.capacity = Math.min(MAX_CAPACITY, this.capacity + 50);
		setChanged();
	}

	/** Ванильная версия: сундук — плюс 10 к лимиту (максимум 150). */
	public void applyChest() {
		this.capacity = Math.min(MAX_CAPACITY, this.capacity + 10);
		setChanged();
	}

	public void applyKillerCore() {
		this.killer = true;
		setChanged();
	}

	/** Ванильная версия: изумруд — выключает режим «фарм» (ванильного обратного пути у ядра убийцы нет). */
	public void applyKillerOff() {
		this.killer = false;
		setChanged();
	}

	/** Ванильная версия: железный слиток — +1 строка (сколько мобов спавнится за раз; старт 3, максимум 12). */
	public void applyIronRow() {
		this.rows = Math.min(MAX_ROWS, this.rows + 1);
		setChanged();
	}

	/** Задаёт моба напрямую (используется предметом и командой /way spawnerpro). */
	public void setMobId(String mobId) {
		this.mobId = mobId == null ? "" : mobId;
		setChanged();
	}

	/** ПКМ с яйцом моба: сохранить тип моба в спавнере. */
	public void consumeSpawnEgg(ItemStack stack, net.minecraft.world.entity.player.Player feedback) {
		if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel
				&& stack.getItem() instanceof net.minecraft.world.item.SpawnEggItem egg) {
			// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == SpawnEggItem#getType(ItemStack)
			EntityType<?> type = egg.getType(stack);
			if (type != null) {
				// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == реестр сущностей -> id ("minecraft:zombie")
				this.mobId = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
				setChanged();
				feedback.displayClientMessage(Component.translatable("message.waymod.pro.mob_set", this.mobId), true);
			}
		}
	}

	/** Короткий статус на панели действий. */
	public void sendStatus(net.minecraft.world.entity.player.Player player) {
		player.displayClientMessage(Component.translatable(
				"message.waymod.pro.status",
				Component.translatable(this.mobId.isEmpty() ? "message.waymod.pro.no_mob" : this.mobId),
				this.delay,
				this.capacity,
				this.rows,
				Component.translatable(this.killer ? "message.waymod.pro.on" : "message.waymod.pro.off")
		), true);
	}

	// ---------- тик ----------

	public static void tick(Level level, BlockPos pos, BlockState state, ProSpawnerBlockEntity pro) {
		if (level.isClientSide()) {
			return;
		}
		if (pro.cooldown > 0) {
			pro.cooldown--;
			pro.setChanged();
			return;
		}
		if (pro.mobId.isEmpty()) {
			return;
		}
		Optional<EntityType<?>> resolved = EntityType.byString(pro.mobId);
		if (resolved.isEmpty()) {
			return;
		}
		EntityType<?> type = resolved.get();
		pro.cooldown = pro.delay;
		pro.setChanged();

		AABB area = new AABB(
				pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
				pos.getX() + 8, pos.getY() + 8, pos.getZ() + 8);

		int alive = level.getEntitiesOfClass(Mob.class, area, mob -> mob.getType() == type).size();
		int canSpawn = Math.min(pro.capacity - alive, pro.rows); // по мобу в каждую строку
		if (canSpawn <= 0) {
			return;
		}

		for (int row = 0; row < pro.rows && canSpawn > 0; row++) {
			double z = pos.getZ() + 0.5 + (row - (pro.rows - 1) / 2.0);
			double x = pos.getX() + 0.5 + (level.random.nextFloat() - 0.5) * 4.0;
			double y = pos.getY() + 1.0;

			// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == EntityType#create(Level, EntitySpawnReason)
			Entity created = type.create(level, net.minecraft.world.entity.EntitySpawnReason.SPAWNER);
			if (created == null) {
				continue;
			}
			created.setPos(x, y, z);
			created.setYRot(level.random.nextFloat() * 360.0F);
			level.addFreshEntity(created);
			canSpawn--;
		}

		if (pro.killer) {
			pro.killAndHarvest(level, type, area, pos);
		}
	}

	/**
	 * Авто-убийца: убивает заспавненных мобов и размножает их добычу (×5).
	 */
	private void killAndHarvest(Level level, EntityType<?> type, AABB area, BlockPos pos) {
		// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА == DamageSources#genericKill()
		for (Mob mob : level.getEntitiesOfClass(Mob.class, area, mob -> mob.getType() == type)) {
			mob.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
		}
		// Собираем выпавшую добычу и размножаем ×5 (уровень Добычи 5 «на пальцах»)
		List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, area);
		for (ItemEntity item : drops) {
			double x = item.getX();
			double y = item.getY();
			double z = item.getZ();
			ItemStack stack = item.getItem();
			item.discard();
			for (int i = 0; i < LOOT_MULTIPLIER; i++) {
				ItemStack copy = stack.copy();
				ItemEntity extra = new ItemEntity(level, x, y, z, copy);
				level.addFreshEntity(extra);
			}
		}
	}

	// ---------- сохранение ----------

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString(KEY_MOB, this.mobId);
		output.putInt(KEY_DELAY, this.delay);
		output.putInt(KEY_CAPACITY, this.capacity);
		output.putInt(KEY_KILLER, this.killer ? 1 : 0);
		output.putInt(KEY_COOLDOWN, this.cooldown);
		output.putInt(KEY_ROWS, this.rows);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.mobId = input.getStringOr(KEY_MOB, "");
		this.delay = input.getIntOr(KEY_DELAY, START_DELAY);
		this.capacity = input.getIntOr(KEY_CAPACITY, START_CAPACITY);
		this.killer = input.getIntOr(KEY_KILLER, 0) != 0;
		this.cooldown = input.getIntOr(KEY_COOLDOWN, 0);
		this.rows = Math.max(1, input.getIntOr(KEY_ROWS, START_ROWS));
	}
}
