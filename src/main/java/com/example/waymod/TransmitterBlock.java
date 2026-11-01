package com.example.waymod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Передатчик: тянет предметы из соседнего контейнера и пересылает их
 * в «связанный» контейнер (сундук), заданный командой /way link.
 * Привязка: Shift+ПКМ по передатчику пустой рукой, затем /way link <координаты>.
 */
public class TransmitterBlock extends BaseEntityBlock {
	private final MapCodec<? extends TransmitterBlock> codec = simpleCodec(TransmitterBlock::new);

	public TransmitterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends TransmitterBlock> codec() {
		return this.codec;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TransmitterBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return createTickerHelper(type, ModBlockEntities.TRANSMITTER_BE, TransmitterBlockEntity::tick);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof TransmitterBlockEntity transmitter) {
			if (player instanceof ServerPlayer serverPlayer) {
				if (player.isShiftKeyDown()) {
					// Запоминаем передатчик для следующего /way link
					serverPlayer.setAttached(ModAttachments.LINKED_TRANSMITTER,
							new WayTarget(serverLevel.dimension().identifier().toString(), pos.getX(), pos.getY(), pos.getZ()));
					serverPlayer.displayClientMessage(Component.translatable("message.waymod.transmitter.pair_mode"), true);
				} else if (transmitter.hasTarget()) {
					serverPlayer.displayClientMessage(Component.translatable(
							"message.waymod.transmitter.status", transmitter.targetX(), transmitter.targetY(), transmitter.targetZ()), true);
				} else {
					serverPlayer.displayClientMessage(Component.translatable("message.waymod.transmitter.unlinked"), true);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}
}
