package com.heaser.pipeconnector.utils.pathfinding;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockGetter;
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

        // Fetch once outside the loop to avoid repeated calls in the hot path
        ItemStack connector = heldPipeConnector(player);

        while (!openSet.isEmpty()) {
            if (iterationCount++ > iterationLimit) {
                PipeConnector.LOGGER.warn("{}: Exceeded iteration limit on AStar algorithm", PipeConnector.MODID);
                return null;
            }

            Node currentNode = openSet.poll();
            BlockPos currentPos = currentNode.position;

            if (closedSet.contains(currentPos)) {
                continue;
            }

            if (checker.isGoal(currentPos, end, endY)) {
                return reconstructPath(nodes, currentNode);
            }

            closedSet.add(currentPos);

            for (BlockPos neighbor : getNeighbors(currentPos, level, start, end, endY, player, connector)) {
                if (closedSet.contains(neighbor)) continue;

                boolean isExistingPipe = checker.isExistingPipe(neighbor);
                int stepCost = isExistingPipe ? 0 : 1;
                int tentativeGCost = currentNode.gCost + stepCost;

                Node neighborNode = nodes.get(neighbor);

                if (neighborNode == null) {
                    neighborNode = new Node(neighbor, currentPos, tentativeGCost, checker.heuristic(neighbor, end, endY));
                    nodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeGCost < neighborNode.gCost) {
                    neighborNode.update(currentPos, tentativeGCost);
                    openSet.add(neighborNode);
                }
            }
        }
        return null;
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

    private static List<BlockPos> getNeighbors(BlockPos pos, Level level, BlockPos start, BlockPos end, int endY, Player player, ItemStack connector) {
        List<BlockPos> neighbors = new ArrayList<>(6);
        BlockPos[] directions = {pos.north(), pos.south(), pos.east(), pos.west(), pos.below(), pos.above()};

        for (BlockPos neighbor : directions) {
            if (shouldAddNeighbor(neighbor, start, end, endY, level, player, connector)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static boolean shouldAddNeighbor(BlockPos neighbor, BlockPos start, BlockPos end,
                                             int endY, Level level, Player player, ItemStack connector) {
        if (isStartOrEnd(neighbor, start, end)) return true;
        if (!isAtRequiredLevel(neighbor, endY) && isVoidableBlock(level, neighbor)) return false;

        ItemStack offhandItem = player.getOffhandItem();
        Block placedBlock = CompatibilityBlockGetter.getInstance().getBlock(offhandItem);

        // Check passability before pipe avoidance - mods like MI allow multiple pipe types per block
        if (CompatibilityBlockEqualsChecker.isPassableForPathfinding(neighbor, level, offhandItem)) return true;
        if (isBlockStateSpecificBlock(neighbor, placedBlock, offhandItem, level)) return true;

        if (!canAvoidPipe(level, neighbor, player)) return false;

        if (connector != null && TagUtils.getAvoidInventoryBlocks(connector)
                && hasInventoryCapabilities(level, neighbor)) return false;

        return true;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static boolean isStartOrEnd(BlockPos pos, BlockPos start, BlockPos end) {
        return pos.equals(start) || pos.equals(end);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static boolean isAtRequiredLevel(BlockPos pos, int endY) {
        return pos.getY() == endY;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static boolean canAvoidPipe(Level level, BlockPos pos, Player player) {
        return !isPipeBlock(level, pos) || isOffhandItemAvoidable(level, pos, player);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public interface HeuristicChecker {
        int heuristic(BlockPos current, BlockPos end, int endY);
        boolean isGoal(BlockPos current, BlockPos end, int endY);
        void addDraftPlacements(List<BlockPos> draftPlacements);
        boolean isExistingPipe(BlockPos pos);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public abstract static class BaseHeuristicChecker implements HeuristicChecker {
        protected final boolean useExistingPipes;
        protected final Block placedBlock;
        protected final ItemStack placedItemStack;
        protected final Level level;
        protected final Set<BlockPos> draftPlacements = new HashSet<>();

        protected BaseHeuristicChecker(boolean useExistingPipes, Block placedBlock,
                                       ItemStack placedItemStack, Level level) {
            this.useExistingPipes = useExistingPipes;
            this.placedBlock = placedBlock;
            this.placedItemStack = placedItemStack;
            this.level = level;
        }

        @Override
        public void addDraftPlacements(List<BlockPos> draftPlacements) {
            this.draftPlacements.addAll(draftPlacements);
        }

        @Override
        public boolean isExistingPipe(BlockPos pos) {
            return useExistingPipes && CompatibilityBlockEqualsChecker.getInstance()
                    .isBlockStateSpecificBlock(pos, placedBlock, placedItemStack, level);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static class PositionHeuristicChecker extends BaseHeuristicChecker {

        public PositionHeuristicChecker(boolean useExistingPipes, Block placedBlock,
                                        ItemStack placedItemStack, Level level) {
            super(useExistingPipes, placedBlock, placedItemStack, level);
        }

        @Override
        public int heuristic(BlockPos current, BlockPos end, int endY) {
            int cost = PathfindingUtils.getCost(current, end);
            boolean isExistingPipe = useExistingPipes && CompatibilityBlockEqualsChecker.getInstance()
                    .isBlockStateSpecificBlock(current, placedBlock, placedItemStack, level);

            if (isExistingPipe) return (int) (cost * 0.3);
            if (draftPlacements.contains(current)) return (int) (cost * 0.1);
            return cost;
        }

        @Override
        public boolean isGoal(BlockPos current, BlockPos end, int endY) {
            return current.equals(end);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static class DepthHeuristicChecker extends BaseHeuristicChecker {

        public DepthHeuristicChecker(boolean useExistingPipes, Block placedBlock,
                                     ItemStack placedItemStack, Level level) {
            super(useExistingPipes, placedBlock, placedItemStack, level);
        }

        @Override
        public int heuristic(BlockPos current, BlockPos end, int endY) {
            int cost = Math.abs(current.getY() - endY);
            boolean isExistingPipe = useExistingPipes && CompatibilityBlockEqualsChecker.getInstance()
                    .isBlockStateSpecificBlock(current, placedBlock, placedItemStack, level);

            if (isExistingPipe || draftPlacements.contains(current)) return (int) (cost * 0.3);
            return cost;
        }

        @Override
        public boolean isGoal(BlockPos current, BlockPos end, int endY) {
            return current.getY() == endY;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

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

        void update(BlockPos newParent, int newGCost) {
            this.parent = newParent;
            this.gCost = newGCost;
            this.fCost = newGCost + this.hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fCost, other.fCost);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
}