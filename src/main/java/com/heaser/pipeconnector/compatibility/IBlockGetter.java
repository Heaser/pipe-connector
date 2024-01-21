package com.heaser.pipeconnector.compatibility;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public interface IBlockGetter {
    Block getBlock(ItemStack placedStack);
}
