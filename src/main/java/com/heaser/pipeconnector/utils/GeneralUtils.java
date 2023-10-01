package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class GeneralUtils
{
    public static boolean isServerSide(Level level) {
        return !level.isClientSide();
    }

    // ---------------------------------------------------------------------------------------------

    public static ItemStack heldPipeConnector(Player player) {
        if (isHoldingPipeConnector(player)) {
            return player.getItemInHand(InteractionHand.MAIN_HAND);
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    public static boolean isHoldingPipeConnector(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND) != ItemStack.EMPTY &&
                player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PipeConnectorItem;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static boolean isNotBreakable(Level level, BlockPos pos) {
        return (level.getBlockState(pos).getDestroySpeed(level, pos) == -1
                && !level.getBlockState(pos).is(TagKeys.UNBREAKABLE_BLOCKS));
    }
    // ---------------------------------------------------------------------------------------------

    public static boolean isVoidableBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TagKeys.VOIDABLE_BLOCKS);
    }

    // ---------------------------------------------------------------------------------------------
}


