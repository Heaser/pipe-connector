package com.heaser.pipeconnector.compatibility.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface IBlockEqualsChecker {
    boolean isBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level);

    // Returns true if no placement is needed at this position for the given held item.
    // Default delegates to isBlockStateSpecificBlock for backward compatibility.
    default boolean isPlacementAlreadySatisfied(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        return isBlockStateSpecificBlock(pos, specificBlock, placedItemStack, level);
    }

    // Returns true if this position should be considered traversable by pathfinding even when
    // isBlockStateSpecificBlock would return false (e.g., AE2 empty cable bus).
    // Default is false (no special pass-through).
    default boolean isPassableForPathfinding(BlockPos pos, Level level, ItemStack placedItemStack) {
        return false;
    }
}
