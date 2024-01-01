package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

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
    // -----------------------------------------------------------------------------------------------------------------

    public static boolean isVoidableBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TagKeys.VOIDABLE_BLOCKS);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean isAvoidableBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TagKeys.AVOIDABLE_BLOCKS);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean isPlaceableBlock(Player player) {
        return player.getOffhandItem().is(TagKeys.PLACEABLE_ITEMS);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean isBlockStateSpecificBlock(BlockState blockState, Block specificBlock) {
        return blockState.getBlock().equals(specificBlock);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean hasInventoryCapabilities(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) != null && Objects.requireNonNull(level.getBlockEntity(pos)).getCapability(ITEM_HANDLER).isPresent();
    }
}



