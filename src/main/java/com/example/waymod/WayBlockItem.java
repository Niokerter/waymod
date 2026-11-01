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
 * Предмет блока перехода. При установке копирует компонент waymod:way_target
 * с предмета в блок-сущность.
 */
public class WayBlockItem extends BlockItem {
	public WayBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
		if (level.getBlockEntity(pos) instanceof WayBlockEntity way && !stack.isEmpty()) {
			WayTarget target = stack.get(ModComponents.WAY_TARGET);
			if (target != null) {
				way.setTarget(target);
			}
		}
		return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
	}
}
