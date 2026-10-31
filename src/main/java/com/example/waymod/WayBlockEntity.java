package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Блок-сущность блока перехода: хранит измерение и координаты цели телепортации.
 */
public class WayBlockEntity extends BlockEntity {
	private static final String KEY_DIMENSION = "WayDimension";
	private static final String KEY_X = "WayX";
	private static final String KEY_Y = "WayY";
	private static final String KEY_Z = "WayZ";

	/** Пустая строка = цель ещё не задана. */
	private String dimension = "";
	private int x;
	private int y;
	private int z;

	public WayBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WAY_BE, pos, state);
	}

	public boolean hasTarget() {
		return !this.dimension.isEmpty();
	}

	public WayTarget getAsTarget() {
		return new WayTarget(this.dimension, this.x, this.y, this.z);
	}

	public void setTarget(WayTarget target) {
		this.dimension = target.dimension() == null ? "" : target.dimension();
		this.x = target.x();
		this.y = target.y();
		this.z = target.z();
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString(KEY_DIMENSION, this.dimension);
		output.putInt(KEY_X, this.x);
		output.putInt(KEY_Y, this.y);
		output.putInt(KEY_Z, this.z);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.dimension = input.getStringOr(KEY_DIMENSION, "");
		this.x = input.getIntOr(KEY_X, 0);
		this.y = input.getIntOr(KEY_Y, 0);
		this.z = input.getIntOr(KEY_Z, 0);
	}
}
