package com.heaser.pipeconnector.utils.pathfinding;

import com.heaser.pipeconnector.config.PipeConnectorConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static com.heaser.pipeconnector.utils.PipeConnectorUtils.moveAndStoreStates;

public class ManhattanAlgorithm {

    public static List<BlockPos> findPathManhattan(BlockPos start, BlockPos end, Level level, boolean mirror, Set<BlockPos> blockedPositions) {

        Set<BlockPos> blockHashSet = new HashSet<>();
        Set<BlockPos> bridgeHashSet = new HashSet<>();

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
        BlockPos bridgeStart = currentPos;
        if (mirror) {
            currentPos = moveAndStoreStates(currentPos, zSteps, 0, 0, zDirection, level, bridgeHashSet);
            moveAndStoreStates(currentPos, xSteps, xDirection, 0, 0, level, bridgeHashSet);
        } else {
            currentPos = moveAndStoreStates(currentPos, xSteps, xDirection, 0, 0, level, bridgeHashSet);
            moveAndStoreStates(currentPos, zSteps, 0, 0, zDirection, level, bridgeHashSet);
        }

        bridgeHashSet.add(end);

        if (!Collections.disjoint(bridgeHashSet, blockedPositions)
                && bridgeHashSet.size() <= PipeConnectorConfig.MAX_ALLOWED_PIPES_TO_PLACE.get()) {
            List<BlockPos> pathAround = findPathAround(bridgeStart, end, mirror, blockedPositions);
            if (pathAround != null) {
                bridgeHashSet = new HashSet<>(pathAround);
            }
        }

        blockHashSet.addAll(bridgeHashSet);

        return new ArrayList<>(blockHashSet);

    }

    private static List<BlockPos> findPathAround(BlockPos start, BlockPos end, boolean mirror, Set<BlockPos> blockedPositions) {
        int minX = Math.min(start.getX(), end.getX()) - 1;
        int maxX = Math.max(start.getX(), end.getX()) + 1;
        int minZ = Math.min(start.getZ(), end.getZ()) - 1;
        int maxZ = Math.max(start.getZ(), end.getZ()) + 1;

        Direction xFacing = end.getX() > start.getX() ? Direction.EAST : Direction.WEST;
        Direction zFacing = end.getZ() > start.getZ() ? Direction.SOUTH : Direction.NORTH;
        List<Direction> directions = mirror
                ? List.of(zFacing, xFacing, zFacing.getOpposite(), xFacing.getOpposite())
                : List.of(xFacing, zFacing, xFacing.getOpposite(), zFacing.getOpposite());

        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        cameFrom.put(start, start);
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            if (currentPos.equals(end)) {
                List<BlockPos> path = new ArrayList<>();
                while (!currentPos.equals(start)) {
                    path.add(currentPos);
                    currentPos = cameFrom.get(currentPos);
                }
                path.add(start);
                return path;
            }

            for (Direction direction : directions) {
                BlockPos neighbor = currentPos.relative(direction);
                if (neighbor.getX() < minX || neighbor.getX() > maxX || neighbor.getZ() < minZ || neighbor.getZ() > maxZ) continue;
                if (blockedPositions.contains(neighbor) || cameFrom.containsKey(neighbor)) continue;

                cameFrom.put(neighbor, currentPos);
                queue.add(neighbor);
            }
        }
        return null;
    }

    private static int calculateDirection(int delta) {
        return delta > 0 ? 1 : -1;
    }
}