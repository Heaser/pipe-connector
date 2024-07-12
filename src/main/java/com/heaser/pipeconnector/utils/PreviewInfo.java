package com.heaser.pipeconnector.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PreviewInfo {
    public final BlockPos pos;
    public PreviewInfo(BlockPos pos) {
        this.pos = pos;
    }

    public boolean isRelativeStartPos(ItemStack pipeConnector) {
        List<NodeParameter> nodes = TagUtils.getNodesFromStack(pipeConnector);
        NodeParameter startNode = nodes.isEmpty() ? null : nodes.getFirst();
        if (startNode == null) {
            return pos == null;
        }
        return pos.equals(startNode.getRelativePosition());
    }
    public boolean isRelativeEndPos(ItemStack pipeConnector) {
        List<NodeParameter> nodes = TagUtils.getNodesFromStack(pipeConnector);
        NodeParameter endNode = nodes.isEmpty() ? null : nodes.getLast();
        if (endNode == null) {
            return pos == null;
        }
        return pos.equals(endNode.getRelativePosition());
    }

    public boolean isNode(ItemStack pipeConnector) {
        List<NodeParameter> nodes = TagUtils.getNodesFromStack(pipeConnector);
        List<BlockPos> nodeRelativePositions = nodes.stream().map(NodeParameter::getRelativePosition)
                .toList();
        return nodeRelativePositions.contains(pos);
    }
}
