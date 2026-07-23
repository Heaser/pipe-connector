package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.constants.ComponentDataTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public class TagUtils {
    public static List<NodeParameter> getNodesFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        List<NodeParameter> nodes = new ArrayList<>();
        ListTag nodeTags = tag.getListOrEmpty(ComponentDataTags.kPipeConnectorNodes);
        for (int index = 0; index < nodeTags.size(); index++) {
            CompoundTag nodeTag = nodeTags.getCompoundOrEmpty(index);
            nodes.add(new NodeParameter(nodeTag));
        }
        return nodes;
    }

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

    public static int getDepthFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getIntOr(ComponentDataTags.kPipeConnectorDepth, 0);
    }

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

    public static String getDimension(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getStringOr(ComponentDataTags.kPipeConnectorDimensionName, "");
    }

    public static void setDimension(ItemStack stack, String dimensionName) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(ComponentDataTags.kPipeConnectorDimensionName, dimensionName);
        });
    }

    public static BridgeType getBridgeType(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getString(ComponentDataTags.kPipeConnectorBridgeType)
                .map(name -> {
                    try {
                        return BridgeType.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        return BridgeType.DEFAULT;
                    }
                })
                .orElse(BridgeType.DEFAULT);
    }

    public static void setBridgeType(ItemStack stack, BridgeType bridgeType) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(ComponentDataTags.kPipeConnectorBridgeType, bridgeType.toString());
        });
    }

    public static boolean getUtilizeExistingPipes(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(ComponentDataTags.kPipeConnectorUtilizeExistingPipes, false);
    }

    public static void setUtilizeExistingPipes(ItemStack stack, boolean utilizeExistingPipes) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorUtilizeExistingPipes, utilizeExistingPipes);
        });
    }

    public static boolean getPreventInventoryBlockBreaking(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(ComponentDataTags.kPipeConnectorInventoryGuard, true);
    }

    public static void setInventoryGuard(ItemStack stack, boolean shouldBreakInventoryBlocks) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorInventoryGuard, shouldBreakInventoryBlocks);
        });
    }

    public static boolean getAvoidInventoryBlocks(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(ComponentDataTags.kPipeConnectorAvoidInventoryBlocks, false);
    }

    public static void setAvoidInventoryBlocks(ItemStack stack, boolean avoidInventoryBlocks) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorAvoidInventoryBlocks, avoidInventoryBlocks);
        });
    }

    public static void resetPositionAndDirectionTags(ItemStack stack, Player player, boolean shouldShowMessage) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(ComponentDataTags.kPipeConnectorNodes);
            tag.remove(ComponentDataTags.kPipeConnectorReplaceSeed);
        });
        if (shouldShowMessage) {
            player.sendOverlayMessage(Component.translatable("item.pipe_connector.message.resettingPositions"));
        }
    }

    public static boolean getSolidPreview(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(ComponentDataTags.kPipeConnectorSolidPreview, false);
    }

    public static void setSolidPreview(ItemStack stack, boolean solid) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorSolidPreview, solid);
        });
    }

    public static boolean getPipeVision(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(ComponentDataTags.kPipeConnectorPipeVision, false);
    }

    public static void setPipeVision(ItemStack stack, boolean enabled) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorPipeVision, enabled);
        });
    }

    public static boolean getReplaceMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(ComponentDataTags.kPipeConnectorReplaceMode, false);
    }

    public static void setReplaceMode(ItemStack stack, boolean replaceMode) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorReplaceMode, replaceMode);
        });
    }

    public static ReplaceSeed getReplaceSeed(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(ComponentDataTags.kPipeConnectorReplaceSeed)) {
            return null;
        }
        CompoundTag seedTag = tag.getCompoundOrEmpty(ComponentDataTags.kPipeConnectorReplaceSeed);
        BlockPos pos = new BlockPos(
                seedTag.getIntOr(ComponentDataTags.kPipeConnectorNodePositionX, 0),
                seedTag.getIntOr(ComponentDataTags.kPipeConnectorNodePositionY, 0),
                seedTag.getIntOr(ComponentDataTags.kPipeConnectorNodePositionZ, 0));
        return new ReplaceSeed(pos,
                seedTag.getStringOr(ComponentDataTags.kPipeConnectorReplaceSeedKind, ""),
                seedTag.getStringOr(ComponentDataTags.kPipeConnectorReplaceSeedDimension, ""));
    }

    public static void setReplaceSeed(ItemStack stack, ReplaceSeed seed) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag seedTag = new CompoundTag();
            seedTag.putInt(ComponentDataTags.kPipeConnectorNodePositionX, seed.pos().getX());
            seedTag.putInt(ComponentDataTags.kPipeConnectorNodePositionY, seed.pos().getY());
            seedTag.putInt(ComponentDataTags.kPipeConnectorNodePositionZ, seed.pos().getZ());
            seedTag.putString(ComponentDataTags.kPipeConnectorReplaceSeedKind, seed.kindDescriptor());
            seedTag.putString(ComponentDataTags.kPipeConnectorReplaceSeedDimension, seed.dimension());
            tag.put(ComponentDataTags.kPipeConnectorReplaceSeed, seedTag);
        });
    }

    public static void clearReplaceSeed(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(ComponentDataTags.kPipeConnectorReplaceSeed);
        });
    }

    public static boolean getMirrorManhattan(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(ComponentDataTags.kPipeConnectorMirrorManhattan, false);
    }

    public static void setMirrorManhattan(ItemStack stack, boolean mirrored) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ComponentDataTags.kPipeConnectorMirrorManhattan, mirrored);
        });
    }
}
