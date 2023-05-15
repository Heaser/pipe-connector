package com.heaser.pipeconnector.items.pipeconnectoritem.utils;


import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.pipeconnectoritem.PipeConnectorItem;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class PipeConnectorUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean connectPathWithSegments(Player player, BlockPos start, BlockPos end, int depth, Block block) {
        //TODO: Don't need to get the player, just pass it in from where this used.
        //TODO: The player already contains the level, don't really need to keep passing it from up the chain. Less is more.
        Level level = player.getLevel();

        // TODO: Set your types instead of raw classes so you don't need to cast things and it helps the IDE check stuff.
        Tuple<Boolean, String> validPathValueAndBlock = isValidPipePath(level, start, end, depth);
        boolean isValidPath = validPathValueAndBlock.getA();
        String InvalidBlockName = validPathValueAndBlock.getB();

        Set<BlockPos> blockPosSet = getBlockPosSet(start, end, depth);

        LOGGER.debug(blockPosSet.toString());

        //TODO: Don't use assert keyword. It doesn't work outside of dev unless you have JVM flags set. Player already isn't null.
        boolean isCreativeMode = player.isCreative();
        int pipeLimit = 640;

        // Disable pipe check and reduction in creative mode.
        if (!isCreativeMode) {
            int numOfPipes = getNumberOfPipesInInventory(player);
            if (numOfPipes < blockPosSet.size()) {
                int missingPipes = blockPosSet.size() - numOfPipes;
                LOGGER.debug("Not enough pipes in inventory, missing " + missingPipes + " pipes.");
                player.displayClientMessage(Component.translatable("item.pipe_connector.message.notEnoughPipes", missingPipes).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
                return false;
            }
            if (pipeLimit < blockPosSet.size()) {
                LOGGER.debug("Unable to place more than " + pipeLimit + " at once");
                player.displayClientMessage(Component.translatable("item.pipe_connector.message.reachedPipeLimit", pipeLimit).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
                return false;
            }
        }
        if (isValidPath) {
            //TODO: Renamed for easier reading.
            blockPosSet.forEach((pathPos -> {
                if (!isCreativeMode) {
                    //TODO: Test pipe subtraction.
                    reduceNumberOfPipesInInventory(player);
                    //TODO: Networking shouldn't be needed for removing items. This would indicate you aren't on the right side.
                }
                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, pathPos);
                //TODO: Make this come from the player to fix bypassing other mods player restrictions.

                breakAndSetBlock(level, pathPos, block, player);
            }));
            return true;
        }
        player.displayClientMessage(Component.translatable("item.pipe_connector.message.unbreakableBlockReached", InvalidBlockName).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
        return false;
    }

    // TODO: Method call and variables is longer than just inlining the code it ran and making the variables easier to understand.

    // -----------------------------------------------------------------------------------------------------------------

    //TODO: I'm not really sure what the goal here is? Traversing the whole path only to store and return the last block seen that's not valid doesn't make sense.
    // Would be better to fail at the first block hit unless you want to store every invalid spot for another reason. You don't need a Tuple for that, just an ArrayList of positions.
    // Won't replace this as I can't tell what the goal is.
    public static Tuple<Boolean, String> isValidPipePath(Level level, BlockPos start, BlockPos end, int depth) {
        boolean isValid = true;
        String invalidBlockName = "";
        Set<BlockPos> blockPosSet = getBlockPosSet(start, end, depth);
        for (BlockPos blockPos : blockPosSet) {
            if (isNotBreakable(level, blockPos)) {
                isValid = false;
                //TODO: Can't use `.toString()` on translatable components as you print the object as a string instead of the name. Use .getString() instead.
                invalidBlockName = level.getBlockState(blockPos).getBlock().getName().getString();
            }
        }
        return new Tuple<>(isValid, invalidBlockName);
    }

    private static Set<BlockPos> getBlockPosSet(BlockPos start, BlockPos end, int depth) {
        Set<BlockPos> blockPosList = new HashSet<>();


        int deltaY = (start.getY() > end.getY()) ? Math.abs((start.getY() - end.getY())) : Math.abs(end.getY() - start.getY());
        int startDepth = depth, endDepth = depth;

        if (start.getY() > end.getY()) {
            endDepth -= deltaY;
        } else {
            startDepth -= deltaY;
        }

        for (int i = 0; i < startDepth; i++) {
            blockPosList.add(start);
            start = start.below();
        }

        for (int i = 0; i < endDepth; i++) {
            blockPosList.add(end);
            end = end.below();
        }

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

        //         Move along the Y-axis
        for (int i = 0; i < ySteps; i++) {
            blockPosList.add(currentPos);
            currentPos = currentPos.offset(0, yDirection, 0);
        }

        // Move along the X-axis
        for (int i = 0; i < xSteps; i++) {
            blockPosList.add(currentPos);
            currentPos = currentPos.offset(xDirection, 0, 0);
        }

        // Move along the Z-axis
        for (int i = 0; i < zSteps; i++) {
            blockPosList.add(currentPos);
            currentPos = currentPos.offset(0, 0, zDirection);
        }

        return blockPosList;
    }

    // -----------------------------------------------------------------------------------------------------------------

    //TODO: This method isn't ideal either as you should probably bundle your validations together into one as currently this will still probably bypass things it shouldn't.
    private static void breakAndSetBlock(Level level, BlockPos pos, Block block, Player player) {

        if (!level.getBlockState(pos).is(TagKeys.VOIDABLE_BLOCKS)) {
            //TODO: Need to add the player as the breaker for reasons.
            level.destroyBlock(pos, true, player);
        }
        level.setBlockAndUpdate(pos, block.defaultBlockState());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Prevent pipe connector from placing blocks in if an unbreakable block was reached
    private static boolean reachedUnbreakableBlock(Level level, BlockPos pos) {
        return isNotBreakable(level, pos);
    }

    private static boolean isNotBreakable(Level level, BlockPos pos) {
        //TODO: Pointless variables.
        return level.getBlockState(pos).getDestroySpeed(level, pos) == -1;
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
    //TODO: Don't need a method for a single call that does a single thing. Break out larger groups of logic or repeatable stuff instead.

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
