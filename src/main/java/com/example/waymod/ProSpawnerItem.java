package com.example.waymod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Предмет «Спавнер 2000 Pro». При установке применяет компонент waymod:mob
 * (заданный командой /way spawnerpro) к блок-сущности.
 */
public class ProSpawnerItem extends BlockItem {
	public ProSpawnerItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	protected boolean postPlacement(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
		if (level.getBlockEntity(pos) instanceof ProSpawnerBlockEntity pro && !stack.isEmpty()) {
			String mob = stack.get(ModComponents.MOB);
			if (mob != null) {
				pro.setMobId(mob);
			}
		}
		return super.postPlacement(pos, level, player, stack, state);
	}
}
