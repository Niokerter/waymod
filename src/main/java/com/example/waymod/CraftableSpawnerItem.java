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
 * Предмет крафтового спавнера. При установке читает компонент waymod:mob
 * и настраивает спавнер на выбранного моба.
 */
public class CraftableSpawnerItem extends BlockItem {
	public CraftableSpawnerItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
		if (level.getBlockEntity(pos) instanceof CraftableSpawnerBlockEntity spawner && !stack.isEmpty()) {
			String mob = stack.get(ModComponents.MOB);
			if (mob != null) {
				spawner.applyItemMob(mob, level, pos);
			}
		}
		return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
	}
}
