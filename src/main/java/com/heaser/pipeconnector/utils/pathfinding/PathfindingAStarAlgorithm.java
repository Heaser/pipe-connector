package com.heaser.pipeconnector.utils.pathfinding;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;

import static com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker.isBlockStateSpecificBlock;
import static com.heaser.pipeconnector.utils.GeneralUtils.*;

public class PathfindingAStarAlgorithm {

    public static List<BlockPos> findPathAStar(BlockPos start, BlockPos end, int endY, Level level, Player player, HeuristicChecker checker) {
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

            // If position alrady processed this position with a lower or equal cost, skip it to avoid dupes
            if (closedSet.contains(currentPos)) {
                continue;
            }

            if (checker.isGoal(currentPos, end, endY)) {
                return reconstructPath(nodes, currentNode);
            }

            closedSet.add(currentPos);

            for (BlockPos neighbor : getNeighbors(currentPos, level, start, end, endY, player)) {
                if (closedSet.contains(neighbor)) continue;

                int tentativeGCost = currentNode.gCost + 1;
                Node neighborNode = nodes.get(neighbor);

                if (neighborNode == null) {
                    // New neighbor discovered
                    neighborNode = new Node(neighbor, currentPos, tentativeGCost, checker.heuristic(neighbor, end, endY));
                    nodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeGCost < neighborNode.gCost) {
                    // Found a better path to an existing neighbor
                    neighborNode.parent = currentPos;
                    neighborNode.gCost = tentativeGCost;
                    neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;
                    
                    // Re-add to openSet to update position in priority queue (duplicates handled by closedSet check)
                    openSet.add(neighborNode);
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

    private static List<BlockPos> getNeighbors(BlockPos pos, Level level, BlockPos start, BlockPos end, int endY, Player player) {
        List<BlockPos> neighbors = new ArrayList<>(6);
        // Direct instantiation to avoid array overhead if possible, but array is cleaner for iteration.
        // Keeping array for readability as performance impact is minimal compared to other parts.
        BlockPos[] directions = {pos.north(), pos.south(), pos.east(), pos.west(), pos.below(), pos.above()};

        for (BlockPos neighbor : directions) {
            if (shouldAddNeighbor(neighbor, start, end, endY, level, player)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    private static boolean shouldAddNeighbor(BlockPos neighbor, BlockPos start, BlockPos end, int endY, Level level, Player player) {
        // Perform quick checks first
        boolean isStartOrEnd = isStartOrEnd(neighbor, start, end);
        if (isStartOrEnd) return true;

        // Check level requirement
        boolean isAtRequiredLevel = neighbor.getY() == endY;
        boolean isNotVoidable = !isVoidableBlock(level, neighbor);
        
        // If not at required level and voidable, fast fail (unless start/end covered above)
        // Original logic: (isStartOrEnd || isAtRequiredLevel || isNotVoidable)
        if (!isAtRequiredLevel && !isNotVoidable) return false;

        ItemStack offhandItem = player.getOffhandItem();
        Block block = level.getBlockState(neighbor).getBlock();

        boolean canAvoid = !isPipeBlock(level, neighbor) || isOffhandItemAvoidable(level, neighbor, player);
        if (!canAvoid) return false;

        boolean isBlockSpecificBlock = isBlockStateSpecificBlock(neighbor, block, offhandItem, level);
        if (isBlockSpecificBlock) return true;

        boolean isCompatPassable = CompatibilityBlockEqualsChecker.isPassableForPathfinding(neighbor, level, offhandItem);
        if (isCompatPassable) return true;

        // Inventory check is expensive, do it last and only if needed
        ItemStack connector = heldPipeConnector(player);
        if (connector != null && TagUtils.getAvoidInventoryBlocks(connector)) {
             if (hasInventoryCapabilities(level, neighbor)) {
                return false;
            }
        }
        
        // If we got here, it means (isStartOrEnd || isAtRequiredLevel || isNotVoidable) was true
        // AND canAvoid was true
        // BUT (isBlockSpecificBlock || isCompatPassable) was false.
        // Wait, the original logic was:
        // return (...) && canAvoid && (isBlockSpecificBlock || isCompatPassable);
        // So if neither block specific nor compat passable, we return false.
        
        return false;
    }

    private static boolean isStartOrEnd(BlockPos pos, BlockPos start, BlockPos end) {
        return pos.equals(start) || pos.equals(end);
    }

    public interface HeuristicChecker {
        int heuristic(BlockPos current, BlockPos end, int endY);
        boolean isGoal(BlockPos current, BlockPos end, int endY);
        void addDraftPlacements(List<BlockPos> draftPlacements);
    }

    public static class PositionHeuristicChecker implements HeuristicChecker {
        private final boolean useExistingPipes;
        private final Block placedBlock;
        private final ItemStack placedItemStack;
        private final Level level;
        private final Set<BlockPos> draftPlacements = new HashSet<>();

        public PositionHeuristicChecker(boolean useExistingPipes, Block placedBlock, ItemStack placedItemStack, Level level) {
            this.useExistingPipes = useExistingPipes;
            this.placedBlock = placedBlock;
            this.placedItemStack = placedItemStack;
            this.level = level;
        }

        public void addDraftPlacements(List<BlockPos> draftPlacements) {
            this.draftPlacements.addAll(draftPlacements);
        }

        public int heuristic(BlockPos current, BlockPos end, int endY) {
            int cost = PathfindingUtils.getCost(current, end);
            boolean isExistingPipe = useExistingPipes && CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(current, placedBlock, placedItemStack, level);
            
            if (isExistingPipe) {
                return (int)(cost * 0.3);
            }
            if (this.draftPlacements.contains(current)) {
                return (int)(cost * 0.1);
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
        private final Set<BlockPos> draftPlacements = new HashSet<>();

        private final Level level;

        public DepthHeuristicChecker(boolean useExistingPipes, Block placedBlock, ItemStack placedItemStack, Level level) {
            this.useExistingPipes = useExistingPipes;
            this.placedBlock = placedBlock;
            this.placedItemStack = placedItemStack;
            this.level = level;
        }

        public void addDraftPlacements(List<BlockPos> draftPlacements) {
            this.draftPlacements.addAll(draftPlacements);
        }

        public int heuristic(BlockPos current, BlockPos end, int endY) {
            int cost = Math.abs(current.getY() - endY);
            boolean isExistingPipe = useExistingPipes && CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(current, placedBlock, placedItemStack, level);
            
            if (isExistingPipe || this.draftPlacements.contains(current)) {
                return (int)(cost * 0.3);
            }
            return cost;
        }

        public boolean isGoal(BlockPos current, BlockPos end, int endY) {
            return current.getY() == endY;
        }
    }

    static class Node implements Comparable<Node> {
        final BlockPos position;
        BlockPos parent;
        int gCost;
        final int hCost;
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