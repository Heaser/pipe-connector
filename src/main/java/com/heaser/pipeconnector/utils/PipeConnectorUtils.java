package com.heaser.pipeconnector.utils;


import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.SyncBuildPath;
import com.heaser.pipeconnector.particles.ParticleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PipeConnectorUtils {

    public static boolean connectPathWithSegments(Player player, BlockPos start, BlockPos end, int depth, Block block, UseOnContext context) {
        Level level = player.getLevel();
        Map<BlockPos, BlockState> blockPosMap = getBlockPosMap(start, end, depth, level);

        PipeConnector.LOGGER.debug(blockPosMap.toString());


        boolean isCreativeMode = player.getAbilities().instabuild;
        int pipeLimit = 640;

        // Disable pipe check and reduction in creative mode.
        if (!isCreativeMode)
            {
                int numOfPipes = getNumberOfPipesInInventory(player);
                if (numOfPipes < blockPosMap.size()) {
                    int missingPipes = blockPosMap.size() - numOfPipes;
                    PipeConnector.LOGGER.debug("Not enough pipes in inventory, missing " + missingPipes + " pipes.");
                    player.displayClientMessage(Component.translatable("item.pipe_connector.message.notEnoughPipes", missingPipes).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
                    return false;
                }
            }

        // Checks if the pipe limit has been reached
            if (pipeLimit < blockPosMap.size()) {
                PipeConnector.LOGGER.debug("Unable to place more than " + pipeLimit + " at once");
                player.displayClientMessage(Component.translatable("item.pipe_connector.message.reachedPipeLimit", pipeLimit).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
                return false;
            }
        // Checks if the block is unbreakable
        for(Map.Entry<BlockPos, BlockState> set: blockPosMap.entrySet()) {
            if (isNotBreakable(level, set.getKey())) {
                String invalidBlockName = set.getValue().getBlock().getName().getString();
                player.displayClientMessage(Component.translatable("item.pipe_connector.message.unbreakableBlockReached",invalidBlockName).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED), true);
                return false;
            }
        }

        for (Map.Entry<BlockPos, BlockState> set : blockPosMap.entrySet()) {
            if (!isCreativeMode) {
                reduceNumberOfPipesInInventory(player);
            }
            ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, set.getKey());
            boolean wasSet = breakAndSetBlock(level, set.getKey(), block, player, context);
        }
        return true;
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
    public static Map<BlockPos, BlockState> getBlockPosMap(BlockPos start, BlockPos end, int depth, Level level) {
        Map <BlockPos, BlockState> blockHashMap = new HashMap<>();


        int deltaY = Math.abs(start.getY() - end.getY());
        int startDepth = depth, endDepth = depth;

        if (start.getY() > end.getY()) {
            endDepth -= deltaY;
        } else {
            startDepth -= deltaY;
        }

        start = moveAndStoreStates(start, startDepth, 0, -1, 0, level, blockHashMap);
        end = moveAndStoreStates(end, endDepth, 0, -1, 0, level, blockHashMap);

        // Create bridge
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int dz = end.getZ() - start.getZ();

        int xSteps = Math.abs(dx);
        int ySteps = Math.abs(dy);
        int zSteps = Math.abs(dz);

        int xDirection = dx > 0 ? 1 : -1;
        int yDirection = dy > 0 ? 1 : -1;
        int zDirection = dz > 0 ? 1 : -1;

        BlockPos currentPos = start.above();

        currentPos = moveAndStoreStates(currentPos, ySteps, 0, yDirection, 0, level, blockHashMap);
        currentPos = moveAndStoreStates(currentPos, xSteps, xDirection, 0, 0, level, blockHashMap);
        moveAndStoreStates(currentPos, zSteps, 0, 0, zDirection, level, blockHashMap);

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

    // -----------------------------------------------------------------------------------------------------------------
    private static boolean breakAndSetBlock(Level level, BlockPos pos, Block block, Player player, UseOnContext context) {

        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
        BlockState blockState = block.getStateForPlacement(blockPlaceContext);

        if (!isVoidableBlock(level, pos)) {
            level.destroyBlock(pos, true, player);
            level.addDestroyBlockEffect(pos, level.getBlockState(pos));
        }

        if (blockState != null) {
            return level.setBlockAndUpdate(pos, blockState);
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static boolean isNotBreakable(Level level, BlockPos pos) {
        return (level.getBlockState(pos).getDestroySpeed(level, pos) == -1
                && !level.getBlockState(pos).is(TagKeys.UNBREAKABLE_BLOCKS));
    }


    // -----------------------------------------------------------------------------------------------------------------

    public static boolean isVoidableBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TagKeys.VOIDABLE_BLOCKS);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static ItemStack heldPipeConnector(Player player) {
        if (isHoldingPipeConnector(player)) {
            return player.getItemInHand(InteractionHand.MAIN_HAND);
        }
        return null;
    }

    public static boolean isHoldingPipeConnector(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND) != ItemStack.EMPTY &&
                player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PipeConnectorItem;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static int getDepthFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if(tag.contains("Depth",tag.TAG_INT)) {
            return tag.getInt("Depth");
        }

        return 0; // Default depth value if the tag is missing
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setDepthToStack(ItemStack stack, int depth) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if (depth < 2) {
            depth = 99;
        } else if (depth > 99) {
            depth = 2;
        }
        tag.putInt("Depth", depth);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setStartPositionAndDirection(ItemStack stack, Direction direction, BlockPos startPos) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.putByte("StartDirection", (byte) direction.get3DDataValue());
        tag.putInt("StartX", startPos.getX());
        tag.putInt("StartY", startPos.getY());
        tag.putInt("StartZ", startPos.getZ());
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BlockPos getStartPosition(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if(!tag.contains("StartX") || !tag.contains("StartY") || !tag.contains("StartZ")) {
            return null;
        }
        int x = tag.getInt("StartX");
        int y = tag.getInt("StartY");
        int z = tag.getInt("StartZ");
        return new BlockPos(x, y, z);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static Direction getStartDirection(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        byte direction = tag.getByte("StartDirection");
        return Direction.from3DDataValue(direction);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setEndPositionAndDirection(ItemStack stack, Direction direction, BlockPos endPos) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.putByte("EndDirection", (byte) direction.get3DDataValue());
        tag.putInt("EndX", endPos.getX());
        tag.putInt("EndY", endPos.getY());
        tag.putInt("EndZ", endPos.getZ());
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BlockPos getEndPosition(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        if(!tag.contains("EndX") || !tag.contains("EndY") || !tag.contains("EndZ")) {
            return null;
        }
        int x = tag.getInt("EndX");
        int y = tag.getInt("EndY");
        int z = tag.getInt("EndZ");
        return new BlockPos(x, y, z);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static Direction getEndDirection(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        byte direction = tag.getByte("EndDirection");
        return Direction.from3DDataValue(direction);
    }

    public static void resetPositionAndDirectionTags(ItemStack stack, Player player, boolean shouldShowMessage) {
        CompoundTag tag = stack.getOrCreateTagElement(PipeConnector.MODID);
        tag.remove("StartDirection");
        tag.remove("StartX");
        tag.remove("StartY");
        tag.remove("StartZ");
        tag.remove("EndDirection");
        tag.remove("EndX");
        tag.remove("EndY");
        tag.remove("EndZ");
        if(shouldShowMessage) {
            player.displayClientMessage(Component.translatable("item.pipe_connector.message.resettingPositions"), true);
        }
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
                                  UseOnContext context) {

        return PipeConnectorUtils.connectPathWithSegments(player,
                startPos.relative(startDirection),
                endPos.relative(endDirection),
                depth,
                Block.byItem(player.getOffhandItem().getItem()),
                context);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void updateBlockPreview(ServerPlayer player, ItemStack interactedItem) {
        Level level = player.getLevel();
        if (interactedItem.getItem() instanceof PipeConnectorItem) {
            int depth = getDepthFromStack(interactedItem);
            BlockPos startPos = getStartPosition(interactedItem);
            BlockPos endPos = getEndPosition(interactedItem);
            Direction startDirection = getStartDirection(interactedItem);
            Direction endDirection = getEndDirection(interactedItem);
            if (startPos != null && endPos != null) {
                startPos = startPos.relative(startDirection);
                endPos = endPos.relative(endDirection);
                HashSet<PreviewInfo> buildPath = PipeConnectorUtils.getBlockPosSet(
                        PipeConnectorUtils.getBlockPosMap(startPos, endPos, depth, level)
                );

                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncBuildPath(buildPath));
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void resetBlockPreview(ServerPlayer player) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncBuildPath(new HashSet<>()));
    }
}
