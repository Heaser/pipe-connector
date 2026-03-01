package com.heaser.pipeconnector.utils.pathfinding;

import net.minecraft.core.BlockPos;

public class PathfindingUtils {

    public static int getCost(BlockPos nodePosition, BlockPos nextNodePosition) {
        return Math.abs(nodePosition.getX() - nextNodePosition.getX())
                + Math.abs(nodePosition.getY() - nextNodePosition.getY())
                + Math.abs(nodePosition.getZ() - nextNodePosition.getZ());
    }
}