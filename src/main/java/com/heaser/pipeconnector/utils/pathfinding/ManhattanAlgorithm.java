package com.heaser.pipeconnector.utils.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.heaser.pipeconnector.utils.PipeConnectorUtils.moveAndStoreStates;

public class ManhattanAlgorithm {

    public static List<BlockPos> findPathManhattan(BlockPos start, BlockPos end, Level level) {

        Set<BlockPos> blockHashSet = new HashSet<>();

        // Create bridgeW
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int dz = end.getZ() - start.getZ();

        int xSteps = Math.abs(dx);
        int ySteps = Math.abs(dy);
        int zSteps = Math.abs(dz);


        int xDirection = calculateDirection(dx);
        int yDirection = calculateDirection(dy);
        int zDirection = calculateDirection(dz);

        BlockPos currentPos = start;

        currentPos = moveAndStoreStates(currentPos, ySteps, 0, yDirection, 0, level, blockHashSet);
        currentPos = moveAndStoreStates(currentPos, xSteps, xDirection, 0, 0, level, blockHashSet);
        moveAndStoreStates(currentPos, zSteps, 0, 0, zDirection, level, blockHashSet);

        blockHashSet.add(end);

        return new ArrayList<>(blockHashSet);

    }

    private static int calculateDirection(int delta) {
        return delta > 0 ? 1 : -1;
    }
}