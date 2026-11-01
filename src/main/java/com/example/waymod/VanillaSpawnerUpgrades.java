package com.example.waymod;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * ПКМ с сахаром работает на ЛЮБОМ спавнере, включая ванильный:
 * −2 тика задержки за штуку, без предела. Так «ускорение сахаром»
 * одинаково работает на ванильном спавнере, Вей-спавнере и Спавнере (HHN).
 */
public final class VanillaSpawnerUpgrades {
	private VanillaSpawnerUpgrades() {
	}

	public static void initialize() {
		UseBlockCallback.EVENT.register(VanillaSpawnerUpgrades::onUseBlock);
	}

	private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(net.minecraft.world.item.Items.SUGAR)) {
			return InteractionResult.PASS;
		}
		// Подходит и ванильному спавнеру, и Вей-спавнеру (оба — SpawnerBlockEntity).
		if (!(level.getBlockEntity(hit.getBlockPos()) instanceof SpawnerBlockEntity spawner)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			// minSpawnDelay/maxSpawnDelay/spawnDelay открыты через waymod.accesswidener.
			BaseSpawner logic = spawner.getSpawner();
			if (logic != null) {
				logic.minSpawnDelay = Math.max(1, logic.minSpawnDelay - 2);
				logic.maxSpawnDelay = Math.max(logic.minSpawnDelay, logic.maxSpawnDelay - 2);
				logic.spawnDelay = logic.minSpawnDelay;
				spawner.setChanged();
				stack.shrink(1);
				player.displayClientMessage(Component.translatable(
						"message.waymod.sugar_applied", logic.minSpawnDelay, logic.maxSpawnDelay), true);
			}
		}
		return InteractionResult.SUCCESS;
	}
}
