package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.constants.BridgeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BuildParameters {

    public int depth;
    public List<NodeParameter> nodes;
    public BridgeType bridgeType;
    public boolean utilizeExistingPipes;
    public boolean avoidInventoryBlocks;
    public boolean mirrorManhattan;
    public String dimension;
    public boolean replaceMode;
    public ReplaceSeed replaceSeed;

    public BuildParameters() {
        this.depth = -1;
        this.nodes = new ArrayList<>();
        this.dimension = "";
        this.bridgeType = BridgeType.DEFAULT;
        this.utilizeExistingPipes = true;
        this.avoidInventoryBlocks = false;
        this.mirrorManhattan = false;
        this.replaceMode = false;
        this.replaceSeed = null;
    }

    public BuildParameters(ItemStack pipeConnectorItem) {
        this.nodes = TagUtils.getNodesFromStack(pipeConnectorItem);
        this.depth = TagUtils.getDepthFromStack(pipeConnectorItem);
        this.dimension = TagUtils.getDimension(pipeConnectorItem);
        this.bridgeType = TagUtils.getBridgeType(pipeConnectorItem);
        this.utilizeExistingPipes = TagUtils.getUtilizeExistingPipes(pipeConnectorItem);
        this.avoidInventoryBlocks = TagUtils.getAvoidInventoryBlocks(pipeConnectorItem);
        this.mirrorManhattan = TagUtils.getMirrorManhattan(pipeConnectorItem);
        this.replaceMode = TagUtils.getReplaceMode(pipeConnectorItem);
        this.replaceSeed = TagUtils.getReplaceSeed(pipeConnectorItem);
    }

    public BuildParameters(int depth,
                           List<NodeParameter> nodes,
                           String dimension,
                           BridgeType bridgeType,
                           boolean utilizeExistingPipes,
                           boolean avoidInventoryBlocks,
                           boolean mirrorManhattan) {
        this.depth = depth;
        this.nodes = (List<NodeParameter>) new ArrayList<>(nodes).clone();
        this.dimension = dimension;
        this.bridgeType = bridgeType;
        this.utilizeExistingPipes = utilizeExistingPipes;
        this.avoidInventoryBlocks = avoidInventoryBlocks;
        this.mirrorManhattan = mirrorManhattan;
    }

    public boolean equals(BuildParameters other) {
        boolean depthEqual = other.depth == this.depth;
        boolean isDimensionEqual = other.dimension.equals(this.dimension);
        boolean isBridgeTypeEqual = other.bridgeType == this.bridgeType;
        boolean isUtilizeExistingPipesEqual = other.utilizeExistingPipes == this.utilizeExistingPipes;
        boolean isAvoidInventoryBlocksEqual = other.avoidInventoryBlocks == this.avoidInventoryBlocks;
        boolean isMirrorManhattanEqual = other.mirrorManhattan == this.mirrorManhattan;
        boolean isReplaceModeEqual = other.replaceMode == this.replaceMode;
        boolean isReplaceSeedEqual = java.util.Objects.equals(other.replaceSeed, this.replaceSeed);
        boolean nodesEqual;
        nodesEqual = other.nodes.size() == this.nodes.size();
        if (nodesEqual) {
            for (int index = 0; index < this.nodes.size(); index++) {
                NodeParameter otherNode = other.nodes.get(index);
                NodeParameter node = this.nodes.get(index);
                nodesEqual = nodesEqual && otherNode.equals(node);
            }
        }
        return depthEqual && nodesEqual && isDimensionEqual
                && isBridgeTypeEqual && isUtilizeExistingPipesEqual
                && isAvoidInventoryBlocksEqual && isMirrorManhattanEqual
                && isReplaceModeEqual && isReplaceSeedEqual;
    }
}
