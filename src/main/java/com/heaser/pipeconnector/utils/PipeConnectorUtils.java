package com.heaser.pipeconnector.utils;


import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockGetter;
import com.heaser.pipeconnector.compatibility.CompatibilityPlacer;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.particles.ParticleHelper;
import com.heaser.pipeconnector.utils.pathfinding.ManhattanAlgorithm;
import com.heaser.pipeconnector.utils.pathfinding.PathfindingAStarAlgorithm;
import com.heaser.pipeconnector.utils.pathfinding.graphs.NodeGraph;
import com.heaser.pipeconnector.utils.pathfinding.graphs.NodeGraph.Edge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

import static com.heaser.pipeconnector.utils.GeneralUtils.*;

public class PipeConnectorUtils {


    public static boolean connectPathWithSegments(Player player, List<NodeParameter> nodes, int depth, UseOnContext context, BridgeType bridgeType, boolean utilizeExistingPipes) {
        Level level = player.level();
        ItemStack itemToPlace = player.getOffhandItem();
        Block blockToPlace = CompatibilityBlockGetter.getInstance().getBlock(itemToPlace);
        Map<BlockPos, BlockState> blockPosMap = getBlockPosMap(nodes, depth, level, bridgeType, blockToPlace, itemToPlace, utilizeExistingPipes, player);
        PipeConnector.LOGGER.debug(blockPosMap.toString());

        int pipeLimit = PipeConnectorConfig.MAX_ALLOWED_PIPES_TO_PLACE.get();

        int numOfPipes = getNumberOfPipesInInventory(player);
        int missingPipes = getMissingPipesInInventory(player, numOfPipes, blockPosMap, blockToPlace);
        if (missingPipes > 0) {
            PipeConnector.LOGGER.debug("Not enough pipes in inventory, missing " + missingPipes + " pipes.");
            player.displayClientMessage(Component.translatable("item.pipe_connector.message.notEnoughPipes", missingPipes).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
            return false;
        }

        // Checks if the pipe limit has been reached
        if (pipeLimit < blockPosMap.size()) {
            PipeConnector.LOGGER.debug("Unable to place more than " + pipeLimit + " at once");
            player.displayClientMessage(Component.translatable("item.pipe_connector.message.reachedPipeLimit", pipeLimit).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
            return false;
        }
        // Checks if the block is unbreakable
        for (Map.Entry<BlockPos, BlockState> set : blockPosMap.entrySet()) {
            String blockName = set.getValue().getBlock().getName().getString();
            if (isNotBreakable(level, set.getKey())) {
                player.displayClientMessage(Component.translatable("item.pipe_connector.message.unbreakableBlockReached", blockName).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED), true);
                return false;
            }
            if (hasInventoryCapabilities(level, set.getKey()) && TagUtils.getPreventInventoryBlockBreaking(context.getItemInHand())) {
                player.displayClientMessage(Component.translatable("item.pipe_connector.gui.button.tooltip.disabledInventoryInPath", blockName).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED), true);
                return false;
            }
        }

        for (Map.Entry<BlockPos, BlockState> set : blockPosMap.entrySet()) {
            if (!CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(set.getKey(), blockToPlace, itemToPlace, level)) {
                BlockPos position = set.getKey();
                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, set.getKey());
                List<Direction> directions = getNeighboringDirections(position, blockPosMap);
                breakAndSetBlock(level, position, blockToPlace, player, context, directions);
            }
        }
        return true;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static int getMissingPipesInInventory(Player player, int NumberOfPipesInInventory, int PathSize) {
        boolean isCreativeMode = player.getAbilities().instabuild;
        if (isCreativeMode) {
            return 0;
        } else {
            return PathSize - NumberOfPipesInInventory;
        }
    }
    public static int getMissingPipesInInventory(Player player, int NumberOfPipesInInventory, Map<BlockPos, BlockState> blockPosMap, Block block) {
        int existingPipesInPath = 0;
        for (Map.Entry<BlockPos, BlockState> set : blockPosMap.entrySet()) {
            if (CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(set.getKey(), block, player.getOffhandItem(), player.level())) {
                existingPipesInPath++;
            }
        }
        return getMissingPipesInInventory(player, NumberOfPipesInInventory, blockPosMap.size() - existingPipesInPath);
    }

    public static int getMissingPipesInInventory(Player player, int NumberOfPipesInInventory, Level level, HashSet<PreviewInfo> previewMap, Block block) {
        int existingPipesInPath = 0;

        for (PreviewInfo previewInfo : previewMap) {
            if (CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(previewInfo.pos, block, player.getOffhandItem(), level)) {
                existingPipesInPath++;
            }
        }
        return getMissingPipesInInventory(player, NumberOfPipesInInventory, previewMap.size() - existingPipesInPath);
    }
    // -----------------------------------------------------------------------------------------------------------------

    public static HashSet<PreviewInfo> getBlockPosSet(Map<BlockPos, BlockState> blockPosMap) {
        HashSet<PreviewInfo> previewSet = new HashSet<>();
        for (Map.Entry<BlockPos, BlockState> blockPair : blockPosMap.entrySet()) {
            previewSet.add(new PreviewInfo(blockPair.getKey()));
        }
        return previewSet;
    }

    public static Map<BlockPos, BlockState> useAStar(Map<BlockPos, BlockState> blockHashMap, Edge edge, Level level, int depth, Player player, PathfindingAStarAlgorithm.HeuristicChecker heuristicChecker, PathfindingAStarAlgorithm.DepthHeuristicChecker depthAlgorithm) {
        BlockPos startPos = edge.nodePosition;
        BlockPos endPos = edge.nextNodePosition;
        int deltaY = Math.abs(startPos.getY() - endPos.getY());
        int nextDepth = depth;

        if (startPos.getY() > endPos.getY()) {
            nextDepth -= deltaY;
        } else {
            nextDepth += deltaY;
        }

        PathfindingResult result = moveAndStoreStates(startPos, depth, level, blockHashMap, player, depthAlgorithm);
        blockHashMap = result.blockPosMap;
        startPos = result.finalPosition;
        result = moveAndStoreStates(endPos, nextDepth, level, blockHashMap, player, depthAlgorithm);
        blockHashMap = result.blockPosMap;
        endPos = result.finalPosition;

        List<BlockPos> blockPosPath = PathfindingAStarAlgorithm.findPathAStar(startPos, endPos, -1, level, player, heuristicChecker);

        if (blockPosPath == null) {
            return blockHashMap;
        }

        for (BlockPos pos : blockPosPath) {
            blockHashMap.putIfAbsent(pos, level.getBlockState(pos));
        }

        return blockHashMap;
    }

    public static  Map<BlockPos, BlockState> useManhattan(Map<BlockPos, BlockState> blockHashMap, Edge edge, Level level, int depth) {
        BlockPos startPos = edge.nodePosition;
        BlockPos endPos = edge.nextNodePosition;
        int deltaY = Math.abs(startPos.getY() - endPos.getY());
        int nextDepth = depth;

        if (startPos.getY() > endPos.getY()) {
            nextDepth -= deltaY;
        } else {
            nextDepth += deltaY;
        }
        startPos = moveAndStoreStates(startPos, depth, 0, -1, 0, level, blockHashMap);
        endPos = moveAndStoreStates(endPos, nextDepth, 0, -1, 0, level, blockHashMap);

        List<BlockPos> blockPosPath = ManhattanAlgorithm.findPathManhattan(startPos, endPos, level);

        for (BlockPos pos : blockPosPath) {
            blockHashMap.putIfAbsent(pos, level.getBlockState(pos));
        }

        return blockHashMap;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // This Method returns a Map of BlockPos & And BlockStates which will eventually be used to bridge between the
    // two locations clicked by the Player.
    // -----------------------------------------------------------------------------------------------------------------
    public static Map<BlockPos, BlockState> getBlockPosMap(List<NodeParameter> nodes, int depth, Level level, BridgeType bridgeType, Block placedBlock, ItemStack placedItemStack, boolean utilizeExistingPipes, Player player) {
        Map<BlockPos, BlockState> blockHashMap = new HashMap<>();
        PathfindingAStarAlgorithm.PositionHeuristicChecker PositionAlgorithm = new PathfindingAStarAlgorithm.PositionHeuristicChecker(utilizeExistingPipes, placedBlock, placedItemStack, level);
        PathfindingAStarAlgorithm.DepthHeuristicChecker DepthAlgorithm = new PathfindingAStarAlgorithm.DepthHeuristicChecker(utilizeExistingPipes, placedBlock, placedItemStack, level);
        NodeParameter startNode = nodes.getFirst();
        NodeParameter endNode = nodes.getLast();

        BlockPos depthAnchorPos = endNode.position;
        if (startNode.position.getY() > endNode.position.getY()) {
            depthAnchorPos = startNode.position;
        }

        NodeGraph graph = new NodeGraph(nodes);
        List<Edge> path = graph.createPath();

        for (Edge edge : path) {
            int deltaY = depthAnchorPos.getY() - edge.nodePosition.getY();
            int relativeDepth = depth - deltaY;

            switch (bridgeType) {
                case A_STAR -> useAStar(blockHashMap, edge, level, relativeDepth, player, PositionAlgorithm, DepthAlgorithm);
                case DEFAULT -> useManhattan(blockHashMap, edge, level, relativeDepth);
            }
        }

        return blockHashMap;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static BlockPos moveAndStoreStates(BlockPos start, int steps, int xDirection, int yDirection, int zDirection, Level level, Map<BlockPos, BlockState> map) {
        BlockPos currentPos = start;
        for (int i = 0; i < steps; i++) {
            map.putIfAbsent(currentPos, level.getBlockState(currentPos));
            currentPos = currentPos.offset(xDirection, yDirection, zDirection);
        }
        return currentPos;
    }

    private static PathfindingResult moveAndStoreStates(BlockPos start, int steps, Level level, Map<BlockPos, BlockState> map, Player player, PathfindingAStarAlgorithm.DepthHeuristicChecker algorithm) {
        BlockPos end = start.below(steps);
        List<BlockPos> blockPosPath = PathfindingAStarAlgorithm.findPathAStar(start, null, end.getY(), level, player, algorithm);;

        if (blockPosPath == null) {
            return new PathfindingResult(map, start, start.below(steps));
        }
        for (BlockPos pos : blockPosPath) {
            map.putIfAbsent(pos, level.getBlockState(pos));
        }
        return new PathfindingResult(map, blockPosPath.get(0), blockPosPath.get(blockPosPath.size() - 1));
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BlockPos moveAndStoreStates(BlockPos start, int steps, int xDirection, int yDirection, int zDirection, Level level, Set<BlockPos> set) {
        BlockPos currentPos = start;
        for (int i = 0; i < steps; i++) {
            set.add(currentPos);
            currentPos = currentPos.offset(xDirection, yDirection, zDirection);
        }
        return currentPos;
    }


    // -----------------------------------------------------------------------------------------------------------------
    private static boolean breakAndSetBlock(Level level, BlockPos pos, Block block, Player player, UseOnContext context, List<Direction> adjacentDirectionSides) {
        if (CompatibilityPlacer.getInstance().place(level, pos, player, player.getOffhandItem(), adjacentDirectionSides)) {
            // This is needed to update the blockStates of the blocks around the placed block
            handleBlockUpdates(level, pos);
            // This is required by some mods to recognize pipe placement
            BlockEvent.EntityPlaceEvent event = handlePlaceEvent(level, pos, level.getBlockState(pos), player);
            reduceNumberOfPipesInInventory(player);
            return !event.isCanceled();
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private static List<Direction> getNeighboringDirections(BlockPos pos, Map<BlockPos, BlockState> blockPosBlockStateMap) {
        List<Direction> directions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos relativePos = pos.relative(direction);
            if (blockPosBlockStateMap.containsKey(relativePos)) {
                directions.add(direction);
            }
        }
        return directions;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private static void handleBlockUpdates(Level level, BlockPos pos) {
        for(Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            neighborState.updateNeighbourShapes(level, neighborPos, 3);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static BlockEvent.EntityPlaceEvent handlePlaceEvent(Level level, BlockPos pos, BlockState blockState, Player player) {
        BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(level.dimension(), level, pos), blockState, player);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Check how many items that were in the players offhand are also available in the players inventory
    public static int getNumberOfPipesInInventory(Player player) {
        Item pipe = player.getOffhandItem().getItem();
        int numberOfPipes;
        numberOfPipes = player.getOffhandItem().getCount();
        Inventory inventory = player.getInventory();


        for (ItemStack itemStack : inventory.items) {
            if (itemStack.getItem() == pipe) {
                numberOfPipes += itemStack.getCount();
            }
        }

        return numberOfPipes;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Reduce the number of pipes in the players inventory by one, start with the inventory and only finish with
    // the offhand if needed
    // -----------------------------------------------------------------------------------------------------------------
    public static void reduceNumberOfPipesInInventory(Player player) {

        // Check if the player is not in creative mode
        if(!player.getAbilities().instabuild) {
            Item pipe = player.getOffhandItem().getItem();
            Inventory inventory = player.getInventory();

            for (int i = 0; i < inventory.items.size(); i++) {
                if (inventory.items.get(i).getItem() == pipe) {
                    if (inventory.items.get(i).getCount() > 1) {
                        inventory.items.get(i).shrink(1);
                    } else {
                        inventory.items.set(i, ItemStack.EMPTY);
                    }
                    return;
                }
            }
            // Reduce the number of pipes in the players offhand by one
            if (player.getOffhandItem().getCount() > 1) {
                player.getOffhandItem().shrink(1);
            } else {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean connectBlocks(Player player,
                                        List<NodeParameter> nodes,
                                        int depth,
                                        UseOnContext context,
                                        BridgeType bridgeType,
                                        boolean utilizeExistingPipes) {
        return PipeConnectorUtils.connectPathWithSegments(player,
                nodes,
                depth,
                context,
                bridgeType,
                utilizeExistingPipes);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static int getRelativeDepth(BlockPos startNode, BlockPos endNode, int depth) {
        if (startNode.getY() > endNode.getY()) {
            return depth;
        }
        int deltaY = Math.abs(startNode.getY() - endNode.getY());
        return depth - deltaY;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static List<NodeParameter> getSortedNodes(List<NodeParameter> nodes) {
        return nodes.stream().sorted((node1, node2) -> Math.abs(node1.position.getX() - node2.position.getX()) + Math.abs(node1.position.getZ() - node2.position.getZ())).toList();
    }

    // -----------------------------------------------------------------------------------------------------------------

    static public class PathfindingResult {
        BlockPos finalPosition;
        BlockPos startPosition;
        Map<BlockPos, BlockState> blockPosMap;

        public PathfindingResult(Map<BlockPos, BlockState> blockPosMap, BlockPos startPosition, BlockPos finalPosition) {
            this.startPosition = startPosition;
            this.finalPosition = finalPosition;
            this.blockPosMap = blockPosMap;
        }
    }
}
