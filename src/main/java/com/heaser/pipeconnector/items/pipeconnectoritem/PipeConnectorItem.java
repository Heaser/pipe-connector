package com.heaser.pipeconnector.items.pipeconnectoritem;

import com.heaser.pipeconnector.utils.gui.PipeConnectorGui;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.List;

public class PipeConnectorItem extends Item {
    private BlockPos firstPosition;
    private BlockPos secondPosition;
    private static final Logger LOGGER = LogUtils.getLogger();
    Direction facingSideStart;
    Direction facingSideEnd;

    public PipeConnectorItem(Properties properties) {
        super(properties);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        if (!level.isClientSide() && useHand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {

            // TODO: Call a method that opens the a GUI screen
            player.sendSystemMessage(Component.literal("POOP").withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE));
        }
        return super.use(level, player, useHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        if (!context.getLevel().isClientSide) {
            BlockPos clickedPosition = context.getClickedPos();
            if (firstPosition == null) {
                firstPosition = clickedPosition;
                LOGGER.info("firstPosition found: {}", firstPosition);
                facingSideStart = context.getClickedFace();
            } else if (secondPosition == null && !clickedPosition.equals(firstPosition)) {
                secondPosition = clickedPosition;
                facingSideEnd = context.getClickedFace();
                LOGGER.info("secondPosition found: {}", secondPosition);
                if (secondPosition != null && firstPosition != null) {
                    connectBlocks(context.getLevel(), firstPosition, secondPosition);
                    firstPosition = null;
                    secondPosition = null;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            components.add(Component.literal("Shift + Right-Click to open GUI").withStyle(ChatFormatting.GREEN));
        } else {
            components.add(Component.literal("Hold Shift for more info").withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, level, components, tooltipFlag);
    }

    private void connectBlocks(Level level, BlockPos start, BlockPos end) {

        int depth = 5;

        BlockPos setFacingS = getNeighborInFacingDirection(start, facingSideStart);
        BlockPos setFacingE = getNeighborInFacingDirection(end, facingSideEnd);

        BlockPos adjustedStart = setFacingS.below();
        BlockPos adjustedEnd = setFacingE.below();


        connectPathWithSegments(level, adjustedStart, adjustedEnd, depth);
    }

    private void connectPathWithSegments(Level level, BlockPos start, BlockPos end, int depth) {

        // TODO Fix a bug where big depth numbers create an H shape instead of a bridge
        // Create downward segment
        for (int i = 0; i < depth; i++) {
            setBlockAtDepth(level, start, i);
            start = start.below();
        }
        for (int i = 0; i < depth; i++) {
            setBlockAtDepth(level, end, i);
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

        BlockPos currentPos = start;

        // Move along the X-axis
        for (int i = 0; i < xSteps; i++) {
            currentPos = currentPos.offset(xDirection, 0, 0);
            setBlockAtDepth(level, currentPos, 0);
        }

        // Move along the Z-axis
        for (int i = 0; i < zSteps; i++) {
            currentPos = currentPos.offset(0, 0, zDirection);
            setBlockAtDepth(level, currentPos, 0);
        }

//         Move along the Y-axis
        for (int i = 0; i < ySteps; i++) {
            currentPos = currentPos.offset(0, yDirection, 0);
            setBlockAtDepth(level, currentPos, 0);
        }

    }


    private void setBlockAtDepth(Level level, BlockPos pos, int depth) {
        // Loop through each depth level from the clicked position to the specified
        // depth
        for (int i = 0; i <= depth; i++) {
            BlockPos targetPos = pos.below(i);
            BlockState targetBlockState = level.getBlockState(targetPos);

            if (isBreakable(level, pos)) {
                level.setBlockAndUpdate(targetPos, Blocks.COBBLESTONE.defaultBlockState());
            }
        }
    }

    private boolean isBreakable(Level level, BlockPos pos) {
        BlockState blockPos = level.getBlockState(pos);
        float hardness = blockPos.getDestroySpeed(level, pos);
        return hardness != -1;
    }


    public BlockPos getNeighborInFacingDirection(BlockPos pos, Direction facing) {
        return pos.relative(facing);
    }

    PipeConnectorGui openGui = new PipeConnectorGui(Component.literal("Test"));
// Todo: Implement a Method that opens the GUI




}

