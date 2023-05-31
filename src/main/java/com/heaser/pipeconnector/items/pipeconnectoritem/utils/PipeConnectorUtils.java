package com.heaser.pipeconnector.items.pipeconnectoritem.utils;


import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.pipeconnectoritem.PipeConnectorItem;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PipeConnectorUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean connectPathWithSegments(Player player, BlockPos start, BlockPos end, int depth, Block block, UseOnContext context) {
        Level level = player.getLevel();
        Map<BlockPos, BlockState> blockPosMap = getBlockPosMap(start, end, depth, level);

        LOGGER.debug(blockPosMap.toString());


        boolean isCreativeMode = player.getAbilities().instabuild;
        int pipeLimit = 640;

        // Disable pipe check and reduction in creative mode.
        if (!isCreativeMode)
            {
                int numOfPipes = getNumberOfPipesInInventory(player);
                if (numOfPipes < blockPosMap.size()) {
                    int missingPipes = blockPosMap.size() - numOfPipes;
                    LOGGER.debug("Not enough pipes in inventory, missing " + missingPipes + " pipes.");
                    player.displayClientMessage(Component.translatable("item.pipe_connector.message.notEnoughPipes", missingPipes).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
                    return false;
                }
            }

        // Checks if the pipe limit has been reached
            if (pipeLimit < blockPosMap.size()) {
                LOGGER.debug("Unable to place more than " + pipeLimit + " at once");
                player.displayClientMessage(Component.translatable("item.pipe_connector.message.reachedPipeLimit", pipeLimit).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
                return false;
            }
        // Checks if the block is unbreakable
        for(Map.Entry<BlockPos, BlockState> set: blockPosMap.entrySet()) {
            if (isNotBreakable(level, set.getKey())) {
                String invalidBlockName = set.getValue().getBlock().getName().getString();
                player.displayClientMessage(Component.translatable("item.pipe_connector.message.unbreakableBlockReached",invalidBlockName).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
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

        if (!level.getBlockState(pos).is(TagKeys.VOIDABLE_BLOCKS)) {
            level.destroyBlock(pos, true, player);
            level.addDestroyBlockEffect(pos, level.getBlockState(pos));
        }

        if (blockState != null) {
            return level.setBlockAndUpdate(pos, blockState);
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private static boolean isNotBreakable(Level level, BlockPos pos) {
        return (level.getBlockState(pos).getDestroySpeed(level, pos) == -1
                && !level.getBlockState(pos).is(TagKeys.UNBREAKABLE_BLOCKS));
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static ItemStack holdingPipeConnector(Player player) {
        if (player.getItemInHand(InteractionHand.MAIN_HAND) != ItemStack.EMPTY && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PipeConnectorItem) {
            return player.getItemInHand(InteractionHand.MAIN_HAND);
        }
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static int getDepthFromStack(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("Depth");
        }
        return 0; // Default depth value if the tag is missing
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void setDepthToStack(ItemStack stack, int depth) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putInt("Depth", depth);
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
}
