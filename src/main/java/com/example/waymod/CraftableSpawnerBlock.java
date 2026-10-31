package com.example.waymod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Крафтовый спавнер: копия ванильного спавнера, которая крафтится
 * из железных решёток и око Эндера и чей моб задаётся командой /way spawner.
 */
public class CraftableSpawnerBlock extends SpawnerBlock {
	private final MapCodec<? extends CraftableSpawnerBlock> codec = simpleCodec(CraftableSpawnerBlock::new);

	public CraftableSpawnerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends CraftableSpawnerBlock> codec() {
		return this.codec;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CraftableSpawnerBlockEntity(pos, state);
	}

	@Override
	public BlockEntityType<? extends BlockEntity> getType() {
		return ModBlockEntities.CRAFT_SPAWNER_BE;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return BlockEntity.createTickerHelper(
				type,
				ModBlockEntities.CRAFT_SPAWNER_BE,
				level.isClientSide() ? SpawnerBlockEntity::clientTick : SpawnerBlockEntity::serverTick
		);
	}

	// Ванильные блок-сущности рисуются «невидимым» блоком (модель рисует рендерер блок-сущности).
	// У нас рендерер не свой — рисуем обычную модель блока.
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
}
