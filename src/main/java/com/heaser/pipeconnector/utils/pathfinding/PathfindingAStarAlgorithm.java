package com.heaser.pipeconnector.utils.pathfinding;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;

import static com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker.isBlockStateSpecificBlock;
import static com.heaser.pipeconnector.utils.GeneralUtils.*;

public class
PathfindingAStarAlgorithm {

    public static List<BlockPos> findPathAStar(BlockPos start, BlockPos end, int endY, Level level, HeuristicChecker checker) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, Node> nodes = new HashMap<>();

        Node startNode = new Node(start, null, 0, checker.heuristic(start, end, endY));
        nodes.put(start, startNode);
        openSet.add(startNode);

        int iterationLimit = PipeConnectorConfig.MAX_ASTAR_ITERATIONS.get();
        int iterationCount = 0;

        while (!openSet.isEmpty()) {
            if (iterationCount++ > iterationLimit) {
                PipeConnector.LOGGER.warn("{}: Exceeded iteration limit on AStar algorithm", PipeConnector.MODID);
                return null;
            }

            Node currentNode = openSet.poll();
            BlockPos currentPos = currentNode.position;

            if (checker.isGoal(currentPos, end, endY)) {
                return reconstructPath(nodes, currentNode);
            }

            closedSet.add(currentPos);

            for (BlockPos neighbor : getNeighbors(currentPos, level, start, end, endY)) {
                if (closedSet.contains(neighbor)) continue;

                Node neighborNode = nodes.getOrDefault(neighbor, new Node(neighbor, null, Integer.MAX_VALUE, checker.heuristic(neighbor, end, endY)));

                int tentativeGCost = currentNode.gCost + 1;
                if (tentativeGCost < neighborNode.gCost) {
                    neighborNode.parent = currentPos;
                    neighborNode.gCost = tentativeGCost;
                    neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;

                    if (!openSet.contains(neighborNode)) {
                        nodes.put(neighbor, neighborNode);
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        return null;
    }

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

    private static List<BlockPos> getNeighbors(BlockPos pos, Level level, BlockPos start, BlockPos end, int endY) {
        List<BlockPos> neighbors = new ArrayList<>();
        BlockPos[] directions = {pos.north(), pos.south(), pos.east(), pos.west(), pos.below(), pos.above()};

        for (BlockPos neighbor : directions) {
            if (shouldAddNeighbor(neighbor, start, end, endY, level)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    private static boolean shouldAddNeighbor(BlockPos neighbor, BlockPos start, BlockPos end, int endY, Level level) {
        Player player = Minecraft.getInstance().player;
        ItemStack offhandItem = player.getOffhandItem();
        Block block = level.getBlockState(neighbor).getBlock();

        boolean isStartOrEnd = isStartOrEnd(neighbor, start, end);
        boolean isAtRequiredLevel = neighbor.getY() == endY;
        boolean isNotVoidable = !isVoidableBlock(level, neighbor);
        boolean canAvoid = !isPipeBlock(level, neighbor) || isOffhandItemAvoidable(level, neighbor, player);
        boolean isBlockSpecificBlock = isBlockStateSpecificBlock(neighbor, block, offhandItem, level);

        return (isStartOrEnd || isAtRequiredLevel || isNotVoidable) && canAvoid && isBlockSpecificBlock;
    }

    private static boolean isStartOrEnd(BlockPos pos, BlockPos start, BlockPos end) {
        return pos.equals(start) || pos.equals(end);
    }

    public interface HeuristicChecker {
        int heuristic(BlockPos current, BlockPos end, int endY);
        boolean isGoal(BlockPos current, BlockPos end, int endY);
    }

    public static class PositionHeuristicChecker implements HeuristicChecker {
        private final boolean useExistingPipes;
        private final Block placedBlock;
        private final ItemStack placedItemStack;
        private final Level level;


        public PositionHeuristicChecker(boolean useExistingPipes, Block placedBlock, ItemStack placedItemStack, Level level) {
            this.useExistingPipes = useExistingPipes;
            this.placedBlock = placedBlock;
            this.placedItemStack = placedItemStack;
            this.level = level;
        }

        public int heuristic(BlockPos current, BlockPos end, int endY) {
            int cost = Math.abs(current.getX() - end.getX()) + Math.abs(current.getZ() - end.getZ()) + Math.abs(current.getY() - end.getY());
            if (useExistingPipes) {
                if (CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(current, placedBlock, placedItemStack, level)) {
                    cost = (int)((double)cost * 0.3);
                }
            }
            return cost;
        }


        public boolean isGoal(BlockPos current, BlockPos end, int endY) {
            return current.equals(end);
        }
    }

    public static class DepthHeuristicChecker implements HeuristicChecker {
        private final boolean useExistingPipes;
        private final Block placedBlock;
        private final ItemStack placedItemStack;

        private final Level level;

        public DepthHeuristicChecker(boolean useExistingPipes, Block placedBlock, ItemStack placedItemStack, Level level) {
            this.useExistingPipes = useExistingPipes;
            this.placedBlock = placedBlock;
            this.placedItemStack = placedItemStack;
            this.level = level;
        }

        public int heuristic(BlockPos current, BlockPos end, int endY) {
            int cost = Math.abs(current.getY() - endY);
            if (useExistingPipes) {
                if (CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(current, placedBlock, placedItemStack, level)) {
                    cost = (int)((double)cost * 0.3);
                }
            }
            return cost;
        }

        public boolean isGoal(BlockPos current, BlockPos end, int endY) {
            return current.getY() == endY;
        }
    }

    static class Node implements Comparable<Node> {
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

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fCost, other.fCost);
        }
    }
}