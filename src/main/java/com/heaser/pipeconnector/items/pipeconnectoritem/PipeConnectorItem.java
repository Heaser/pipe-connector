package com.heaser.pipeconnector.items.pipeconnectoritem;

import com.heaser.pipeconnector.items.pipeconnectoritem.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.gui.PipeConnectorGui;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.Screen;


import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PipeConnectorItem extends Item {
    private BlockPos firstPosition;
    private BlockPos secondPosition;
    private static final Logger LOGGER = LogUtils.getLogger();
    Direction facingSideEnd;
    Direction facingSideStart;

    private int depth = 10;

    public PipeConnectorItem(Properties properties) {
        super(properties);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        if (!level.isClientSide() && useHand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
            player.sendSystemMessage(Component.literal("Depth: " + depth).withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE));
        }
        return super.use(level, player, useHand);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            BlockPos clickedPosition = context.getClickedPos();
            if(context.getPlayer().isShiftKeyDown()) {
                resetBlockPosFirstAndSecondPositions();
            }

            else if (firstPosition == null) {
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
                    connectBlocks(context.getLevel(), firstPosition, secondPosition);
                    resetBlockPosFirstAndSecondPositions();
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
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

    private void resetBlockPosFirstAndSecondPositions() {
        firstPosition = null;
        secondPosition = null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public void connectBlocks(Level level, BlockPos start, BlockPos end) {

        BlockPos adjustedStart = PipeConnectorUtils.getNeighborInFacingDirection(start, facingSideStart);
        BlockPos adjustedEnd = PipeConnectorUtils.getNeighborInFacingDirection(end, facingSideEnd);

        PipeConnectorUtils.connectPathWithSegments(level, adjustedStart, adjustedEnd, depth);
    }

}

