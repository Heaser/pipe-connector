package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public class GeneralUtils {
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

    public static boolean isPipeBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TagKeys.PIPE_BLOCK);
    }

    public static boolean isOffhandItemAvoidable(Level level, BlockPos pos, Player player) {
        return level.getBlockState(pos).getBlock().asItem() == player.getOffhandItem().getItem().asItem();
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean isPlaceableBlock(Player player) {
        return player.getOffhandItem().is(TagKeys.PLACEABLE_ITEMS);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean hasInventoryCapabilities(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof Container container && container.getContainerSize() > 0) {
            return true;
        }
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        if (handler != null && handler.size() > 0) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            handler = level.getCapability(Capabilities.Item.BLOCK, pos, dir);
            if (handler != null && handler.size() > 0) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean MaxAllowedNodesReached(List<NodeParameter> nodes) {
        return nodes.size() >= PipeConnectorConfig.MAX_ALLOWED_NODES.get();
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void handleNodeRemovalByPosition(List<NodeParameter> nodes, BlockPos positionToRemove, ItemStack stack) {
        for (int index = 0; index < nodes.size(); index++) {
            NodeParameter currentNode = nodes.get(index);
            if (currentNode.position.equals(positionToRemove)) {
                nodes.remove(currentNode);
                break;
            }
        }
        TagUtils.setNodesToStack(stack, nodes);
    }
}



