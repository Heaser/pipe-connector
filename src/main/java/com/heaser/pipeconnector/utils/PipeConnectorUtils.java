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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;

import java.util.*;

import static com.heaser.pipeconnector.utils.GeneralUtils.*;

public class PipeConnectorUtils {


    public static boolean connectPathWithSegments(Player player, BlockPos start, BlockPos end, int depth, UseOnContext context, BridgeType bridgeType, boolean utilizeExistingPipes) {
        Level level = player.level();
        ItemStack itemToPlace = player.getOffhandItem();
        Block blockToPlace = CompatibilityBlockGetter.getInstance().getBlock(itemToPlace);
        Map<BlockPos, BlockState> blockPosMap = getBlockPosMap(start, end, depth, level, bridgeType, blockToPlace, itemToPlace, utilizeExistingPipes);
        PipeConnector.LOGGER.debug(blockPosMap.toString());


        boolean isCreativeMode = player.getAbilities().instabuild;
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
                if (!isCreativeMode) {
                    reduceNumberOfPipesInInventory(player);
                }
                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, set.getKey());
                breakAndSetBlock(level, set.getKey(), blockToPlace, player, context);
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

    // -----------------------------------------------------------------------------------------------------------------
    // This Method returns a Map of BlockPos & And BlockStates which will eventually be used to bridge between the
    // two locations clicked by the Player.
    // -----------------------------------------------------------------------------------------------------------------

    public static Map<BlockPos, BlockState> getBlockPosMap(BlockPos start, BlockPos end, int depth, Level level, BridgeType bridgeType, Block placedBlock, ItemStack placedItemStack, boolean utilizeExistingPipes) {
        Map<BlockPos, BlockState> blockHashMap = new HashMap<>();

        int deltaY = Math.abs(start.getY() - end.getY());
        int startDepth = depth, endDepth = depth;

        if (start.getY() > end.getY()) {
            endDepth -= deltaY;
        } else {
            startDepth -= deltaY;
        }
        if (bridgeType == BridgeType.DEFAULT) {
            start = moveAndStoreStates(start, startDepth, 0, -1, 0, level, blockHashMap);
            end = moveAndStoreStates(end, endDepth, 0, -1, 0, level, blockHashMap);

        } else {
            PathfindingResult result = moveAndStoreStates(start, startDepth, level, blockHashMap, bridgeType, placedBlock, placedItemStack, utilizeExistingPipes);
            blockHashMap = result.blockPosMap;
            start = result.finalPosition;
            result = moveAndStoreStates(end, endDepth, level, blockHashMap, bridgeType, placedBlock, placedItemStack, utilizeExistingPipes);
            blockHashMap = result.blockPosMap;
            end = result.finalPosition;
        }
        List<BlockPos> blockPosPath = null;
        switch (bridgeType) {
            case A_STAR -> blockPosPath = PathfindingAStarAlgorithm.findPathAStar(start, end, -1, level,
                    new PathfindingAStarAlgorithm.PositionHeuristicChecker(utilizeExistingPipes, placedBlock, placedItemStack, level));
            case DEFAULT -> blockPosPath = ManhattanAlgorithm.findPathManhattan(start, end, level);
//          case STEP -> test = PathfindingAStarAlgorithm.findPathAStar(start, end, level);
        }
        if (blockPosPath == null) {
            return blockHashMap;
        }
        for (BlockPos pos : blockPosPath) {
            blockHashMap.putIfAbsent(pos, level.getBlockState(pos));
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

    private static PathfindingResult moveAndStoreStates(BlockPos start, int steps, Level level, Map<BlockPos, BlockState> map, BridgeType bridgeType, Block placedBlock, ItemStack placedItemStack, boolean utilizeExistingPipes) {
        List<BlockPos> blockPosPath = null;
        BlockPos end = start.below(steps);
        switch (bridgeType) {
            case A_STAR -> blockPosPath = PathfindingAStarAlgorithm.findPathAStar(start, null, end.getY(), level,
                    new PathfindingAStarAlgorithm.DepthHeuristicChecker(utilizeExistingPipes, placedBlock, placedItemStack, level));
//          case STEP -> test = PathfindingAStarAlgorithm.findPathAStar(start, end, level);
        }
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
    private static boolean breakAndSetBlock(Level level, BlockPos pos, Block block, Player player, UseOnContext context) {
        if (CompatibilityPlacer.getInstance().place(level, pos, player, player.getOffhandItem())) {
            // This is needed to update the blockStates of the blocks around the placed block
            handleBlockUpdates(level, pos);

            // This is required by some mods to recognize pipe placement
            BlockEvent.EntityPlaceEvent event = handlePlaceEvent(level, pos, level.getBlockState(pos), player);

            return !event.isCanceled();
        }
        return false;
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
        MinecraftForge.EVENT_BUS.post(event);
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
    public static void reduceNumberOfPipesInInventory(Player player) {
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

    // -----------------------------------------------------------------------------------------------------------------

    public static boolean connectBlocks(Player player,
                                        BlockPos startPos,
                                        Direction startDirection,
                                        BlockPos endPos,
                                        Direction endDirection,
                                        int depth,
                                        UseOnContext context,
                                        BridgeType bridgeType,
                                        boolean utilizeExistingPipes) {

        return PipeConnectorUtils.connectPathWithSegments(player,
                startPos.relative(startDirection),
                endPos.relative(endDirection),
                depth,
                context,
                bridgeType,
                utilizeExistingPipes);
    }

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
