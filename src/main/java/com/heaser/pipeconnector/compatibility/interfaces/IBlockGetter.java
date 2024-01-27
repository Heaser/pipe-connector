package com.heaser.pipeconnector.compatibility.interfaces;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public interface IBlockGetter {
    Block getBlock(ItemStack placedStack);
}
