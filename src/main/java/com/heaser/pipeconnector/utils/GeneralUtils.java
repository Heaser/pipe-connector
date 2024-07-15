package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import java.time.LocalDateTime;
import java.util.List;

import static net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;

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
        if (level.getBlockEntity(pos) != null) {
            // TODO: double check this works with testing the inventory guard feature
            IItemHandler capabilities = level.getCapability(ItemHandler.BLOCK, pos, null);
            if (capabilities != null) {
                int slotNum = capabilities.getSlots();
                return slotNum != 0;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean isAprilFoolsDay() {
        LocalDateTime now = LocalDateTime.now();
        return now.getMonthValue() == 4 && now.getDayOfMonth() == 1;
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



