package com.heaser.pipeconnector.utils.pathfinding;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.utils.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class PathfindingAStarAlgorithm {
    public static List<BlockPos> findPathAStar(BlockPos start, BlockPos end, Level level) {

        Set<BlockPos> openSet = new HashSet<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, Node> nodes = new HashMap<>();

        // Initializing the start node
        Node startNode = new Node(start, null, 0, heuristic(start, end));
        nodes.put(start, startNode);
        openSet.add(start);

        int iterationLimit = 20000;
        int iterationCount = 0;

//        if(getNeighbors(start, level, start, end).isEmpty()) {
//            PipeConnector.LOGGER.warn("{}: No neighbors found for start position", PipeConnector.MODID);
//            return null;
//        }

        // Main loop to process nodes.
        while (!openSet.isEmpty()) {
            if(iterationCount++ > iterationLimit) {
                PipeConnector.LOGGER.warn("{}: Exceeded iteration limit on AStar algorithm", PipeConnector.MODID);
                return null;
            }
            BlockPos currentPos = getLowestFCost(openSet, nodes);

            if (currentPos.equals(end)) {
                return reconstructPath(nodes, nodes.get(currentPos));
            }

            openSet.remove(currentPos);
            closedSet.add(currentPos);

            for (BlockPos neighbor : getNeighbors(currentPos, level, start, end)) {
                if (closedSet.contains(neighbor)) continue;

                Node neighborNode = nodes.getOrDefault(neighbor, new Node(neighbor, null, Integer.MAX_VALUE, heuristic(neighbor, end)));
                nodes.put(neighbor, neighborNode);

                int tentativeGCost = nodes.get(currentPos).gCost + 1;

                if (tentativeGCost < neighborNode.gCost) {
                    neighborNode.parent = currentPos;
                    neighborNode.gCost = tentativeGCost;
                    neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;

                    if (!openSet.contains(neighbor) && !closedSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static int heuristic(BlockPos current, BlockPos end) {
        return Math.abs(current.getX() - end.getX()) + Math.abs(current.getZ() - end.getZ());
    }

    private static BlockPos getLowestFCost(Set<BlockPos> openSet, Map<BlockPos, Node> nodes) {
        BlockPos lowest = null;
        for (BlockPos pos : openSet) {
            if (lowest == null || nodes.get(pos).fCost < nodes.get(lowest).fCost) {
                lowest = pos;
            }
        }
        return lowest;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static List<BlockPos> reconstructPath(Map<BlockPos, Node> nodes, Node endNode) {
        List<BlockPos> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            path.add(current.position);
            current = nodes.get(current.parent);
        }
        Collections.reverse(path);
        return path;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static List<BlockPos> getNeighbors(BlockPos pos, Level level, BlockPos start, BlockPos end) {
        List<BlockPos> neighbors = new ArrayList<>();

        BlockPos north = pos.north();
        BlockPos south = pos.south();
        BlockPos east = pos.east();
        BlockPos west = pos.west();
        BlockPos down = pos.below();
        BlockPos up = pos.above();

        if (north.equals(start) || north.equals(end) || !GeneralUtils.isVoidableBlock(level, north)) neighbors.add(north);
        if (south.equals(start) || south.equals(end) || !GeneralUtils.isVoidableBlock(level, south)) neighbors.add(south);
        if (east.equals(start) || east.equals(end) || !GeneralUtils.isVoidableBlock(level, east)) neighbors.add(east);
        if (west.equals(start) || west.equals(end) || !GeneralUtils.isVoidableBlock(level, west)) neighbors.add(west);
        if (down.equals(start) || down.equals(end) || !GeneralUtils.isVoidableBlock(level, down)) neighbors.add(down);
        if (up.equals(start) || up.equals((end)) || (!GeneralUtils.isVoidableBlock(level, up) &&
                !(up.getX() == start.getX() && up.getZ() == start.getZ()))) neighbors.add(up);
        return neighbors;
    }

    // -----------------------------------------------------------------------------------------------------------------

    static class Node {
        BlockPos position;
        BlockPos parent;
        int gCost;
        int hCost;
        int fCost;

        Node(BlockPos position, BlockPos parent, int gCost, int hCost) {
            this.position = position;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }

}
