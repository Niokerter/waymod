package com.example.waymod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Авто-воронка: собирает предметы из всего чанка (с учётом фильтра) и складывает их
 * в соседний контейнер.
 * Настройка без GUI:
 *   ПКМ с предметом в руке          — добавить предмет в фильтр;
 *   Shift+ПКМ с предметом в руке    — убрать предмет из фильтра;
 *   ПКМ пусто́й рукой               — показать фильтр;
 *   Shift+ПКМ пусто́й рукой         — очистить фильтр (пустой фильтр = всё подряд).
 */
public class AutoHopperBlock extends BaseEntityBlock {
	private final MapCodec<? extends AutoHopperBlock> codec = simpleCodec(AutoHopperBlock::new);

	public AutoHopperBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends AutoHopperBlock> codec() {
		return this.codec;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AutoHopperBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return createTickerHelper(type, ModBlockEntities.AUTO_HOPPER_BE, AutoHopperBlockEntity::tick);
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof AutoHopperBlockEntity hopper) {
			if (player.isShiftKeyDown()) {
				hopper.removeFilter(serverLevel, player, stack);
			} else {
				hopper.addFilter(serverLevel, player, stack);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof AutoHopperBlockEntity hopper) {
			if (player.isShiftKeyDown()) {
				hopper.clearFilter(serverLevel, player);
			} else {
				hopper.showFilter(serverLevel, player);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}
}
