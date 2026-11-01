package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Вей-спавнер (id craftable_spawner): копия ванильного спавнера, которая
 * выдаётся только оператором или в креативе; моб задаётся командой /way spawner.
 * Сахар (ПКМ) ускоряет: минус 2 тика задержки за штуку, без предела.
 */
public class CraftableSpawnerBlock extends SpawnerBlock {

	public CraftableSpawnerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CraftableSpawnerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(
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

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof CraftableSpawnerBlockEntity spawner && !level.isClientSide()) {
			if (stack.is(net.minecraft.world.item.Items.SUGAR)) {
				// Сахар — ванильное ускорение: −2 тика за штуку, без предела (мин 1 тик).
				// Поля minSpawnDelay/maxSpawnDelay/spawnDelay открыты через waymod.accesswidener.
				BaseSpawner logic = spawner.getSpawner();
				if (logic != null) {
					logic.minSpawnDelay = Math.max(1, logic.minSpawnDelay - 2);
					logic.maxSpawnDelay = Math.max(logic.minSpawnDelay, logic.maxSpawnDelay - 2);
					logic.spawnDelay = logic.minSpawnDelay;
					stack.shrink(1);
					player.displayClientMessage(Component.translatable(
							"message.waymod.sugar_applied", logic.minSpawnDelay, logic.maxSpawnDelay), true);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}
