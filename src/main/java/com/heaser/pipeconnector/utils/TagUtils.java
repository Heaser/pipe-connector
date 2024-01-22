package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.constants.BridgeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TagUtils {
    public static int getDepthFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if (tag.contains("Depth", tag.TAG_INT)) {
            return tag.getInt("Depth");
        }

        return 0; // Default depth value if the tag is missing
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setDepthToStack(ItemStack stack, int depth) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if (depth < 0) {
            depth = PipeConnectorConfig.MAX_DEPTH.get();
        } else if (depth > PipeConnectorConfig.MAX_DEPTH.get()) {
            depth = 0;
        }
        tag.putInt("Depth", depth);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setStartPositionAndDirection(ItemStack stack, Direction direction, BlockPos startPos) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.putByte("StartDirection", (byte) direction.get3DDataValue());
        tag.putInt("StartX", startPos.getX());
        tag.putInt("StartY", startPos.getY());
        tag.putInt("StartZ", startPos.getZ());
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BlockPos getStartPosition(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if (!tag.contains("StartX") || !tag.contains("StartY") || !tag.contains("StartZ")) {
            return null;
        }
        int x = tag.getInt("StartX");
        int y = tag.getInt("StartY");
        int z = tag.getInt("StartZ");
        return new BlockPos(x, y, z);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static BlockPos getRelativeStartPosition(ItemStack stack) {
        BlockPos startPos = getStartPosition(stack);
        if (startPos == null) {
            return null;
        }
        Direction startDirection = getStartDirection(stack);
        return startPos.relative(startDirection);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static BlockPos getRelativeEndPosition(ItemStack stack) {
        BlockPos endPos = getEndPosition(stack);
        if (endPos == null) {
            return null;
        }
        Direction endDirection = getEndDirection(stack);
        return endPos.relative(endDirection);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static Direction getStartDirection(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        byte direction = tag.getByte("StartDirection");
        return Direction.from3DDataValue(direction);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setEndPositionAndDirection(ItemStack stack, Direction direction, BlockPos endPos) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.putByte("EndDirection", (byte) direction.get3DDataValue());
        tag.putInt("EndX", endPos.getX());
        tag.putInt("EndY", endPos.getY());
        tag.putInt("EndZ", endPos.getZ());
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BlockPos getEndPosition(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if (!tag.contains("EndX") || !tag.contains("EndY") || !tag.contains("EndZ")) {
            return null;
        }
        int x = tag.getInt("EndX");
        int y = tag.getInt("EndY");
        int z = tag.getInt("EndZ");
        return new BlockPos(x, y, z);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static Direction getEndDirection(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        byte direction = tag.getByte("EndDirection");
        return Direction.from3DDataValue(direction);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static String getDimension(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        return tag.getString("DimensionName");
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setDimension(ItemStack stack, String dimensionName) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.putString("DimensionName", dimensionName);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BridgeType getBridgeType(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if (tag.contains("BridgeType", tag.TAG_STRING)) {
            return BridgeType.valueOf(tag.getString("BridgeType"));
        }
        return BridgeType.DEFAULT;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setBridgeType(ItemStack stack, BridgeType bridgeType) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.putString("BridgeType", bridgeType.toString());
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean getUtilizeExistingPipes(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if (tag.contains("UtilizeExistingPipes", tag.TAG_BYTE)) {
            return tag.getBoolean("UtilizeExistingPipes");
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setUtilizeExistingPipes(ItemStack stack, boolean utilizeExistingPipes) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.putBoolean("UtilizeExistingPipes", utilizeExistingPipes);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean getPreventInventoryBlockBreaking(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if (tag.contains("PreventInventoryBlockBreaking", tag.TAG_BYTE)) {
            return tag.getBoolean("PreventInventoryBlockBreaking");
        }
        return true;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setPreventInventoryBlockBreaking(ItemStack stack, boolean shouldBreakInventoryBlocks) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.putBoolean("PreventInventoryBlockBreaking", shouldBreakInventoryBlocks);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void resetPositionAndDirectionTags(ItemStack stack, Player player, boolean shouldShowMessage) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.remove("StartDirection");
        tag.remove("StartX");
        tag.remove("StartY");
        tag.remove("StartZ");
        tag.remove("EndDirection");
        tag.remove("EndX");
        tag.remove("EndY");
        tag.remove("EndZ");
        if (shouldShowMessage) {
            player.displayClientMessage(Component.translatable("item.pipe_connector.message.resettingPositions"), true);
        }
    }
}
