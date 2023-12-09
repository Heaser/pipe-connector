package com.heaser.pipeconnector.utils.pathfinding;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class PathfindingAStarAlgorithm {

    public static List<BlockPos> findPathAStar(BlockPos start, BlockPos end, int endY, Level level, HeuristicChecker checker) {
        Set<BlockPos> openSet = new HashSet<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, Node> nodes = new HashMap<>();

        // Initializing the start node
        Node startNode = new Node(start, null, 0, checker.heuristic(start, end, endY));
        nodes.put(start, startNode);
        openSet.add(start);

        int iterationLimit = 20000;
        int iterationCount = 0;

        // Main loop to process nodes.
        while (!openSet.isEmpty()) {
            if(iterationCount++ > iterationLimit) {
                PipeConnector.LOGGER.warn("{}: Exceeded iteration limit on AStar algorithm", PipeConnector.MODID);
                return null;
            }
            BlockPos currentPos = getLowestFCost(openSet, nodes);

            if (checker.isGoal(currentPos, end, endY)) {
                return reconstructPath(nodes, nodes.get(currentPos));
            }

            openSet.remove(currentPos);
            closedSet.add(currentPos);

            for (BlockPos neighbor : getNeighbors(currentPos, level, start, end, endY)) {
                if (closedSet.contains(neighbor)) continue;

                Node neighborNode = nodes.getOrDefault(neighbor, new Node(neighbor, null, Integer.MAX_VALUE, checker.heuristic(neighbor, end, endY)));
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

        BlockPos[] directions = {pos.north(), pos.south(), pos.east(), pos.west(), pos.below(), pos.above()};
        for (BlockPos neighbor : directions) {
            if (neighbor.equals(start) || neighbor.equals(end) || !isVoidableBlock(level, neighbor)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
        //        !(up.getX() == start.getX() && up.getZ() == start.getZ()))) neighbors.add(up);
    }

    private static List<BlockPos> getNeighbors(BlockPos pos, Level level, BlockPos start, BlockPos end, int endY) {
        List<BlockPos> neighbors = new ArrayList<>();

        BlockPos[] directions = {pos.north(), pos.south(), pos.east(), pos.west(), pos.below(), pos.above()};
        for (BlockPos neighbor : directions) {
            if (neighbor.equals(start) || neighbor.equals(end) || neighbor.getY() == endY || !isVoidableBlock(level, neighbor)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    interface HeuristicChecker {
        int heuristic(BlockPos current, BlockPos end,  int endY);
        boolean isGoal(BlockPos current, BlockPos end,  int endY);
    }

    public static class PositionHeuristicChecker implements HeuristicChecker {
        public int heuristic(BlockPos current, BlockPos end, int endY) {
            return Math.abs(current.getX() - end.getX()) + Math.abs(current.getZ() - end.getZ()) + Math.abs(current.getY() - end.getY());
        }

        public boolean isGoal(BlockPos current, BlockPos end,  int endY) {
            return current.equals(end);
        }
    }

    public static class DepthHeuristicChecker implements HeuristicChecker {
        public int heuristic(BlockPos current, BlockPos end, int endY) {
            return Math.abs(current.getY() - endY);
        }

        public boolean isGoal(BlockPos current, BlockPos end,  int endY) {
            return current.getY() == endY;
        }
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
