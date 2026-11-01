package com.example.waymod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * «Спавнер (HHN)»: автоматический спавнер с ванильной прокачкой. Крафтится.
 *   сахар    — −2 тика задержки за штуку без предела;
 *   редстоун — режим «фарм»: мобы умирают автоматически, добыча ×5;
 *   изумруд  — выключить «фарм»; железо — +1 строка; сундук — +10 лимита (макс 150).
 * ПКМ с яйцом спавна задаёт моба; /way spawnerpro задаёт моба сразу.
 */
public class ProSpawnerBlock extends BaseEntityBlock {
	private final MapCodec<? extends ProSpawnerBlock> codec = simpleCodec(ProSpawnerBlock::new);

	public ProSpawnerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends ProSpawnerBlock> codec() {
		return this.codec;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ProSpawnerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return createTickerHelper(type, ModBlockEntities.PRO_SPAWNER_BE, ProSpawnerBlockEntity::tick);
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof ProSpawnerBlockEntity pro) {
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			if (stack.is(net.minecraft.world.item.Items.SUGAR)) {
				// Сахар — ванильное ускорение: −2 тика за штуку, без предела (до 1 тика).
				pro.applySugar();
				stack.shrink(1);
				pro.sendStatus(player);
			} else if (stack.is(net.minecraft.world.item.Items.REDSTONE)) {
				// Редстоун — ванильное включение режима «фарм» (авто-убийство, добыча ×5).
				pro.applyKillerCore();
				stack.shrink(1);
				pro.sendStatus(player);
			} else if (stack.is(net.minecraft.world.item.Items.EMERALD)) {
				// Изумруд — обратная сторона редстоуна: выключает «фарм».
				pro.applyKillerOff();
				stack.shrink(1);
				pro.sendStatus(player);
			} else if (stack.is(net.minecraft.world.item.Items.IRON_INGOT)) {
				// Железный слиток — +1 строка: спавнит на одного моба за раз больше (до 12).
				pro.applyIronRow();
				stack.shrink(1);
				pro.sendStatus(player);
			} else if (stack.is(net.minecraft.world.item.Items.CHEST)) {
				// Сундук — +10 к лимиту живых мобов (максимум 150).
				pro.applyChest();
				stack.shrink(1);
				pro.sendStatus(player);
			} else {
				pro.consumeSpawnEgg(stack, player);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof ProSpawnerBlockEntity pro) {
			if (!level.isClientSide()) {
				pro.sendStatus(player);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(state, level, pos, player, hit);
	}
}
