package com.heaser.pipeconnector.items.pipeconnectoritem;

import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.pipeconnectoritem.utils.ParticleHelper;
import com.heaser.pipeconnector.items.pipeconnectoritem.utils.PipeConnectorUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class PipeConnectorItem extends Item {
    Direction endFace;
    Direction startFace;
    private static final Logger LOGGER = LogUtils.getLogger();
    private BlockPos startPos;
    private BlockPos endPos;


    public PipeConnectorItem(Properties properties) {
        super(properties);
    }

    // -----------------------------------------------------------------------------------------------------------------
    @ParametersAreNonnullByDefault
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        if (!level.isClientSide) {
            BlockPos playerLookingAt = player.blockPosition().relative(player.getDirection());
            boolean isAir = level.getBlockState(playerLookingAt).isAir();
            boolean isShiftKeyDown = player.isShiftKeyDown();

            if (useHand == InteractionHand.MAIN_HAND && isShiftKeyDown && isAir) {
                resetBlockPositions(true, player);
            }
        }
        return super.use(level, player, useHand);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player usingPlayer = context.getPlayer();

        if (!context.getLevel().isClientSide && usingPlayer != null) {
            boolean holdingAllowedPipe = usingPlayer.getOffhandItem().is(TagKeys.PLACEABLE_ITEMS);
            ItemStack interactedItem = context.getItemInHand();
            int depth = PipeConnectorUtils.getDepthFromStack(interactedItem);
            boolean isShiftKeyDown = usingPlayer.isShiftKeyDown();
            Level level = context.getLevel();

            if (depth == 0) {
                PipeConnectorUtils.setDepthToStack(interactedItem, 1);
            }

            BlockPos clickedPosition = context.getClickedPos();
            if (isShiftKeyDown && !holdingAllowedPipe) {
                usingPlayer.displayClientMessage(Component.translatable("item.pipe_connector.message.holdValidItem").withStyle(ChatFormatting.GOLD), true);
                return InteractionResult.FAIL;
            }

            if (isShiftKeyDown && context.getClickedFace() == Direction.UP) {
                usingPlayer.displayClientMessage(Component.translatable("item.pipe_connector.message.UpSideNotAllowed").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), true);
                return InteractionResult.FAIL;
            }

            if (isShiftKeyDown) {
                if (startPos == null) {
                    startPos = clickedPosition;
                    startFace = context.getClickedFace();
                    ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, startPos.relative(startFace));

                } else {
                    if (clickedPosition.equals(startPos)) {
                        resetBlockPositions(true, usingPlayer);
                        return InteractionResult.SUCCESS;
                    }
                    endPos = clickedPosition;
                    endFace = context.getClickedFace();
                    ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, endPos.relative(endFace));

                    //LOGGER.debug("secondPosition found: {}", secondPosition);
                    resetBlockPositions(connectBlocks(usingPlayer, startPos, endPos, depth, context), usingPlayer);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @ParametersAreNonnullByDefault
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {

        // Depth will be displayed as depth-1 to the player to reduce confusion
        //int depth = PipeConnectorUtils.getDepthFromStack(stack) - 1;

        if (Screen.hasShiftDown()) {

            components.add(Component.translatable("item.pipe_connector.tooltip.usageExplanation").withStyle(ChatFormatting.DARK_AQUA));
            components.add(Component.translatable("item.pipe_connector.tooltip.CancelSelectionExplanation").withStyle(ChatFormatting.BLUE));
            components.add(Component.translatable("item.pipe_connector.tooltip.changeDepthExplanation").withStyle(ChatFormatting.LIGHT_PURPLE));

        } else {
            components.add(Component.translatable("item.pipe_connector.tooltip.shiftForMoreInfo").withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, level, components, tooltipFlag);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private boolean connectBlocks(Player player, BlockPos startPos, BlockPos endPos, int depth, UseOnContext context) {
        return PipeConnectorUtils.connectPathWithSegments(player, startPos.relative(startFace), endPos.relative(endFace), depth, Block.byItem(player.getOffhandItem().getItem()), context);
    }

    private void resetBlockPositions(boolean shouldDisplayMessage, Player player) {
        startPos = null;
        endPos = null;
        if (shouldDisplayMessage) {
            player.displayClientMessage(Component.translatable("item.pipe_connector.message.resettingPositions"), true);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

}

