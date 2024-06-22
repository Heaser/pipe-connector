package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.constants.ComponentDataTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

public class TagUtils {
    public static int getDepthFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains(ComponentDataTags.kPipeConnectorDepth, tag.TAG_INT)) {
            return tag.getInt(ComponentDataTags.kPipeConnectorDepth);
        }

        return 0; // Default depth value if the tag is missing
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setDepthToStack(ItemStack stack, int depth) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (depth < 0) {
            depth = PipeConnectorConfig.MAX_DEPTH.get();
        } else if (depth > PipeConnectorConfig.MAX_DEPTH.get()) {
            depth = 0;
        }
        tag.putInt(ComponentDataTags.kPipeConnectorDepth, depth);

    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setStartPositionAndDirection(ItemStack stack, Direction direction, BlockPos startPos) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        tag.putByte(ComponentDataTags.kPipeConnectorStartDirection, direction != null ? (byte) direction.get3DDataValue() : -1);
        tag.putInt(ComponentDataTags.kPipeConnectorStartX, startPos.getX());
        tag.putInt(ComponentDataTags.kPipeConnectorStartY, startPos.getY());
        tag.putInt(ComponentDataTags.kPipeConnectorStartZ, startPos.getZ());
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BlockPos getStartPosition(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (!tag.contains(ComponentDataTags.kPipeConnectorStartX) ||
            !tag.contains(ComponentDataTags.kPipeConnectorStartY) ||
            !tag.contains(ComponentDataTags.kPipeConnectorStartZ)) {
            return null;
        }
        int x = tag.getInt(ComponentDataTags.kPipeConnectorStartX);
        int y = tag.getInt(ComponentDataTags.kPipeConnectorStartY);
        int z = tag.getInt(ComponentDataTags.kPipeConnectorStartZ);
        return new BlockPos(x, y, z);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static BlockPos getRelativeStartPosition(ItemStack stack) {
        BlockPos startPos = getStartPosition(stack);
        if (startPos == null) {
            return null;
        }
        Direction startDirection = getStartDirection(stack);
        if (startDirection != null) {
            return startPos.relative(startDirection);
        }
        else {
            return startPos;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static BlockPos getRelativeEndPosition(ItemStack stack) {
        BlockPos endPos = getEndPosition(stack);
        if (endPos == null) {
            return null;
        }
        Direction endDirection = getEndDirection(stack);
        if (endDirection != null) {
            return endPos.relative(endDirection);
        }
        else {
            return endPos;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Nullable
    public static Direction getStartDirection(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        byte directionByte = tag.getByte(ComponentDataTags.kPipeConnectorStartDirection);
        if (directionByte != -1) {
            return Direction.from3DDataValue(directionByte);
        }
        else {
            return null;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setEndPositionAndDirection(ItemStack stack, Direction direction, BlockPos endPos) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        tag.putByte(ComponentDataTags.kPipeConnectorEndDirection, direction != null ? (byte) direction.get3DDataValue() : -1);
        tag.putInt(ComponentDataTags.kPipeConnectorEndX, endPos.getX());
        tag.putInt(ComponentDataTags.kPipeConnectorEndY , endPos.getY());
        tag.putInt(ComponentDataTags.kPipeConnectorEndZ, endPos.getZ());
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BlockPos getEndPosition(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (!tag.contains(ComponentDataTags.kPipeConnectorEndX) ||
            !tag.contains(ComponentDataTags.kPipeConnectorEndY) ||
            !tag.contains(ComponentDataTags.kPipeConnectorEndZ)) {
            return null;
        }
        int x = tag.getInt(ComponentDataTags.kPipeConnectorEndX);
        int y = tag.getInt(ComponentDataTags.kPipeConnectorEndY);
        int z = tag.getInt(ComponentDataTags.kPipeConnectorEndZ);
        return new BlockPos(x, y, z);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Nullable
    public static Direction getEndDirection(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        byte directionByte = tag.getByte(ComponentDataTags.kPipeConnectorEndDirection);
        if (directionByte != -1) {
            return Direction.from3DDataValue(directionByte);
        }
        else {
            return null;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static String getDimension(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        return tag.getString(ComponentDataTags.kPipeConnectorDimensionName);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setDimension(ItemStack stack, String dimensionName) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        tag.putString(ComponentDataTags.kPipeConnectorDimensionName, dimensionName);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BridgeType getBridgeType(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains("BridgeType", tag.TAG_STRING)) {
            return BridgeType.valueOf(tag.getString(ComponentDataTags.kPipeConnectorBridgeType));
        }
        return BridgeType.DEFAULT;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setBridgeType(ItemStack stack, BridgeType bridgeType) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        tag.putString(ComponentDataTags.kPipeConnectorBridgeType, bridgeType.toString());
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean getUtilizeExistingPipes(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains(ComponentDataTags.kPipeConnectorUtilizeExistingPipes, tag.TAG_BYTE)) {
            return tag.getBoolean(ComponentDataTags.kPipeConnectorUtilizeExistingPipes);
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setUtilizeExistingPipes(ItemStack stack, boolean utilizeExistingPipes) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        tag.putBoolean(ComponentDataTags.kPipeConnectorUtilizeExistingPipes, utilizeExistingPipes);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean getPreventInventoryBlockBreaking(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains(ComponentDataTags.kPipeConnectorInventoryGuard, tag.TAG_BYTE)) {
            return tag.getBoolean(ComponentDataTags.kPipeConnectorInventoryGuard);
        }
        return true;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setPreventInventoryBlockBreaking(ItemStack stack, boolean shouldBreakInventoryBlocks) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        tag.putBoolean(ComponentDataTags.kPipeConnectorInventoryGuard, shouldBreakInventoryBlocks);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void resetPositionAndDirectionTags(ItemStack stack, Player player, boolean shouldShowMessage) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        tag.remove(ComponentDataTags.kPipeConnectorStartDirection);
        tag.remove(ComponentDataTags.kPipeConnectorStartX);
        tag.remove(ComponentDataTags.kPipeConnectorStartY);
        tag.remove(ComponentDataTags.kPipeConnectorStartZ);
        tag.remove(ComponentDataTags.kPipeConnectorEndDirection);
        tag.remove(ComponentDataTags.kPipeConnectorEndX);
        tag.remove(ComponentDataTags.kPipeConnectorEndY);
        tag.remove(ComponentDataTags.kPipeConnectorEndZ);
        if (shouldShowMessage) {
            player.displayClientMessage(Component.translatable("item.pipe_connector.message.resettingPositions"), true);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Sets the custom model data tag to the item stack, which is used to determine if the item should use custom model
    // data. If useCustomModelData is true, the custom model will load, otherwise the default model will load.
    // -----------------------------------------------------------------------------------------------------------------
    public static void setCustomModelData(ItemStack stack, boolean useCustomModelData) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (useCustomModelData) {
            tag.putInt(ComponentDataTags.kPipeConnectorCustomModelData, 1);
        } else {
            tag.putInt(ComponentDataTags.kPipeConnectorCustomModelData, 0);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setCustomModelData(CompoundTag tag, boolean useCustomModelData) {
        if (useCustomModelData) {
            tag.putInt(ComponentDataTags.kPipeConnectorCustomModelData, 1);
        } else {
            tag.putInt(ComponentDataTags.kPipeConnectorCustomModelData, 0);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static int getCustomModelData(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains(ComponentDataTags.kPipeConnectorCustomModelData, tag.TAG_INT)) {
            return tag.getInt(ComponentDataTags.kPipeConnectorCustomModelData);
        }
        return 0;
    }

    // -----------------------------------------------------------------------------------------------------------------
}
