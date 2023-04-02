package com.heaser.pipeconnector.items.pipeconnectoritem;

import com.heaser.pipeconnector.items.pipeconnectoritem.utils.PipeConnectorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;


import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

import net.minecraftforge.client.event.InputEvent;
import org.w3c.dom.UserDataHandler;

import java.util.List;

public class PipeConnectorItem extends Item {
    private BlockPos firstPosition;
    private BlockPos secondPosition;
    private static final Logger LOGGER = LogUtils.getLogger();
    Direction facingSideEnd;
    Direction facingSideStart;

    public PipeConnectorItem(Properties properties) {
        super(properties);
    }


    // -----------------------------------------------------------------------------------------------------------------



    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack stack = player.getItemInHand(useHand);
        int depth = getDepthFromStack(stack);

        if (!level.isClientSide() && useHand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
            player.sendSystemMessage(Component.literal("Depth: " + depth).withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE));
        }
        return super.use(level, player, useHand);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {

            ItemStack stack = context.getItemInHand();
            int depth = getDepthFromStack(stack);

            BlockPos clickedPosition = context.getClickedPos();
            if(context.getClickedFace() == Direction.UP) {
                context.getPlayer().sendSystemMessage(Component.literal("Connecting Pipe from Top is not allowed"));
                return InteractionResult.FAIL;

            }
            if(context.getPlayer().isShiftKeyDown()) {
                resetBlockPosFirstAndSecondPositions();
                return InteractionResult.SUCCESS;
            }

            if (firstPosition == null) {
                firstPosition = clickedPosition;
                LOGGER.info("firstPosition found: {}", firstPosition);
                facingSideStart = context.getClickedFace();
            } else if (secondPosition == null) {
                if(clickedPosition.equals(firstPosition)) {
                    resetBlockPosFirstAndSecondPositions();
                    context.getPlayer().sendSystemMessage(Component.literal("Reset Postions"));
                    return InteractionResult.SUCCESS;
                }
                secondPosition = clickedPosition;
                facingSideEnd = context.getClickedFace();
                LOGGER.info("secondPosition found: {}", secondPosition);
                if (secondPosition != null && firstPosition != null) {
                    connectBlocks(context.getLevel(), firstPosition, secondPosition, depth);
                    resetBlockPosFirstAndSecondPositions();
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {

        int depth = getDepthFromStack(stack);

        if (Screen.hasShiftDown()) {
            components.add(Component.literal("Shift + Right-Click to open GUI").withStyle(ChatFormatting.GREEN));
            components.add(Component.literal("Shift + Right-Click on a block will cancel selection").withStyle(ChatFormatting.BLUE));

            components.add(Component.literal("Current Depth: " + depth).withStyle(ChatFormatting.LIGHT_PURPLE));


        } else {
            components.add(Component.literal("Hold Shift for more info").withStyle(ChatFormatting.GOLD));
            components.add(Component.literal("Current Depth: " + depth).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        super.appendHoverText(stack, level, components, tooltipFlag);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);

        setDepthToStack(stack, 2);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public void connectBlocks(Level level, BlockPos start, BlockPos end, int depth) {

        BlockPos adjustedStart = PipeConnectorUtils.getNeighborInFacingDirection(start, facingSideStart);
        BlockPos adjustedEnd = PipeConnectorUtils.getNeighborInFacingDirection(end, facingSideEnd);

        PipeConnectorUtils.connectPathWithSegments(level, adjustedStart, adjustedEnd, depth);
    }

    private void resetBlockPosFirstAndSecondPositions() {
        firstPosition = null;
        secondPosition = null;
    }

    private int getDepthFromStack(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("Depth");
        }
        return 0; // Default depth value if the tag is missing
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void setDepthToStack(ItemStack stack, int depth) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putInt("Depth", depth);
    }

    // -----------------------------------------------------------------------------------------------------------------



}

