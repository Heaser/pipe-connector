package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.constants.ComponentDataTags;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TagUtils {
    public static List<NodeParameter> getNodesFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        List<NodeParameter> nodes = new ArrayList<>();
        if (tag.contains(ComponentDataTags.kPipeConnectorNodes, tag.TAG_LIST)) {
            ListTag nodeTags = tag.getList(ComponentDataTags.kPipeConnectorNodes, tag.TAG_COMPOUND);
            for (int index = 0; index < nodeTags.size(); index++) {
                CompoundTag nodeTag = nodeTags.getCompound((index));
                nodes.add(new NodeParameter(nodeTag));
            }
        }

        return nodes;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setNodesToStack(ItemStack stack, List<NodeParameter> nodes) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            ListTag list = new ListTag();
            for (NodeParameter node : nodes) {
                CompoundTag nodeTag = convertNodeToTag(node);
                list.addTag(list.size(), nodeTag);
            }
            tag.put(ComponentDataTags.kPipeConnectorNodes, list);
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static CompoundTag convertNodeToTag(NodeParameter node) {
        CompoundTag nodeTag = new CompoundTag();
        nodeTag.putInt(ComponentDataTags.kPipeConnectorNodePositionX, node.position.getX());
        nodeTag.putInt(ComponentDataTags.kPipeConnectorNodePositionY, node.position.getY());
        nodeTag.putInt(ComponentDataTags.kPipeConnectorNodePositionZ, node.position.getZ());
        if (node.direction != null) {
            nodeTag.putByte(ComponentDataTags.kPipeConnectorNodeDirection, (byte) node.direction.get3DDataValue());
        }
        return nodeTag;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static int getDepthFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(ComponentDataTags.kPipeConnectorDepth, tag.TAG_INT)) {
            return tag.getInt(ComponentDataTags.kPipeConnectorDepth);
        }

        return 0; // Default depth value if the tag is missing
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setDepthToStack(ItemStack stack, int depth) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            int finalDepth;
            if (depth < 0) {
                finalDepth = PipeConnectorConfig.MAX_DEPTH.get();
            } else if (depth > PipeConnectorConfig.MAX_DEPTH.get()) {
                finalDepth = 0;
            } else {
                finalDepth = depth;
            }

            tag.putInt(ComponentDataTags.kPipeConnectorDepth, finalDepth);
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static String getDimension(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(ComponentDataTags.kPipeConnectorDimensionName)) {
            return tag.getString(ComponentDataTags.kPipeConnectorDimensionName);
        }

        return "";
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setDimension(ItemStack stack, String dimensionName) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(ComponentDataTags.kPipeConnectorDimensionName, dimensionName);
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BridgeType getBridgeType(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains(ComponentDataTags.kPipeConnectorBridgeType, tag.TAG_STRING)) {
            return BridgeType.valueOf(tag.getString(ComponentDataTags.kPipeConnectorBridgeType));
        }
        return BridgeType.DEFAULT;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setBridgeType(ItemStack stack, BridgeType bridgeType) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(ComponentDataTags.kPipeConnectorBridgeType, bridgeType.toString());
        });
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
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorUtilizeExistingPipes, utilizeExistingPipes);
        });
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

    public static void setInventoryGuard(ItemStack stack, boolean shouldBreakInventoryBlocks) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorInventoryGuard, shouldBreakInventoryBlocks);
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean getAvoidInventoryBlocks(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains(ComponentDataTags.kPipeConnectorAvoidInventoryBlocks, tag.TAG_BYTE)) {
            return tag.getBoolean(ComponentDataTags.kPipeConnectorAvoidInventoryBlocks);
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setAvoidInventoryBlocks(ItemStack stack, boolean avoidInventoryBlocks) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorAvoidInventoryBlocks, avoidInventoryBlocks);
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void resetPositionAndDirectionTags(ItemStack stack, Player player, boolean shouldShowMessage) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(ComponentDataTags.kPipeConnectorNodes);
        });
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
    // Preview mode: solid vs outline-only. Default is outline (solid=false)
    // -----------------------------------------------------------------------------------------------------------------
    public static boolean getSolidPreview(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(ComponentDataTags.kPipeConnectorSolidPreview, tag.TAG_BYTE)) {
            return tag.getBoolean(ComponentDataTags.kPipeConnectorSolidPreview);
        }
        return false;
    }

    public static void setSolidPreview(ItemStack stack, boolean solid) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorSolidPreview, solid);
        });
    }

    // -----------------------------------------------------------------------------------------------------------------
}
