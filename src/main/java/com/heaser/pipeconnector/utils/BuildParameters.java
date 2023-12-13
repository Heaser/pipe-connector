package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.constants.BridgeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class BuildParameters {

    public int depth;
    public BlockPos startPosition;
    public BlockPos endPosition;
    public Direction startDirection;
    public Direction endDirection;
    public String dimension;
    public BridgeType bridgeType;
    public boolean utilizeExistingPipes;

    public BuildParameters() {
        this.depth = -1;
        this.startPosition = null;
        this.endPosition = null;
        this.startDirection = null;
        this.endDirection = null;
        this.dimension = "";
        this.bridgeType = BridgeType.DEFAULT;
        this.utilizeExistingPipes = true;
    }

    public BuildParameters(ItemStack pipeConnectorItem) {
        this.startPosition = PipeConnectorUtils.getStartPosition(pipeConnectorItem);
        this.endPosition = PipeConnectorUtils.getEndPosition(pipeConnectorItem);
        this.depth = PipeConnectorUtils.getDepthFromStack(pipeConnectorItem);
        this.startDirection = PipeConnectorUtils.getStartDirection(pipeConnectorItem);
        this.endDirection = PipeConnectorUtils.getEndDirection(pipeConnectorItem);
        this.dimension = PipeConnectorUtils.getDimension(pipeConnectorItem);
        this.bridgeType = PipeConnectorUtils.getBridgeType(pipeConnectorItem);
        this.utilizeExistingPipes = PipeConnectorUtils.getUtilizeExistingPipes(pipeConnectorItem);
    }

    public BuildParameters(int depth,
                           BlockPos startPosition,
                           BlockPos endPosition,
                           Direction startDirection,
                           Direction endDirection,
                           String dimension,
                           BridgeType bridgeType,
                           boolean utilizeExistingPipes) {
        this.depth = depth;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.startDirection = startDirection;
        this.endDirection = endDirection;
        this.dimension = dimension;
        this.bridgeType = bridgeType;
        this.utilizeExistingPipes = utilizeExistingPipes;
    }

    public boolean equals(BuildParameters other) {
        boolean depthEqual = other.depth == this.depth;
        boolean startDirEqual = other.startDirection == this.startDirection;
        boolean endDirEqual = other.endDirection == this.endDirection;
        boolean isDimensionEqual = other.dimension.equals(this.dimension);
        boolean isBridgeTypeEqual = other.bridgeType == this.bridgeType;
        boolean isUtilizeExistingPipesEqual = other.utilizeExistingPipes == this.utilizeExistingPipes;
        boolean startPosEqual;
        boolean endPosEqual;
        if (other.startPosition == null || this.startPosition == null) {
            startPosEqual = other.startPosition == this.startPosition;
        } else {
            startPosEqual = other.startPosition.equals(this.startPosition);
        }
        if (other.endPosition == null || this.endPosition == null) {
            endPosEqual = other.endPosition == this.endPosition;
        } else {
            endPosEqual = other.endPosition.equals(this.endPosition);
        }
        return depthEqual && startPosEqual && endPosEqual && startDirEqual && endDirEqual && isDimensionEqual
                && isBridgeTypeEqual && isUtilizeExistingPipesEqual;
    }
}
