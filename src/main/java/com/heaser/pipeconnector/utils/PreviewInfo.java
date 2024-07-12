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
        return pos.equals(startNode.position.relative(startNode.direction));
    }
    public boolean isRelativeEndPos(ItemStack pipeConnector) {
        List<NodeParameter> nodes = TagUtils.getNodesFromStack(pipeConnector);
        NodeParameter endNode = nodes.isEmpty() ? null : nodes.getLast();
        return pos.equals(endNode.position.relative(endNode.direction));
    }

    public boolean isNode(ItemStack pipeConnector) {
        List<NodeParameter> nodes = TagUtils.getNodesFromStack(pipeConnector);
        List<BlockPos> nodeRelativePositions = nodes.stream().map((node) -> node.position.relative(node.direction))
                .toList();
        return nodeRelativePositions.contains(pos);
    }
}
