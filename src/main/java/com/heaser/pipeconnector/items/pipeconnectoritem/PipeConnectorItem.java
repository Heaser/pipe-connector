package com.heaser.pipeconnector.items.pipeconnectoritem;

import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.pipeconnectoritem.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.PipeConnectorHighlightPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

import javax.annotation.Nullable;
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
        if (!level.isClientSide) {
            BlockPos playerLookingAt = player.blockPosition().relative(player.getDirection());
            boolean isAir = level.getBlockState(playerLookingAt).isAir();
            boolean isShiftKeyDown = player.isShiftKeyDown();

            if (useHand == InteractionHand.MAIN_HAND && isShiftKeyDown && isAir) {
                resetBlockPosFirstAndSecondPositions(true);
            }
        }
        return super.use(level, player, useHand);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public InteractionResult useOn(UseOnContext context) {

        if (!context.getLevel().isClientSide) {
            boolean isPipe = PipeConnectorUtils.holdingAllowedPipe(TagKeys.PLACEABLE_ITEMS, context.getPlayer());
            ItemStack stack = context.getItemInHand();
            int depth = PipeConnectorUtils.getDepthFromStack(stack);
            boolean isShiftKeyDown = context.getPlayer().isShiftKeyDown();

            BlockPos clickedPosition = context.getClickedPos();
            if (isShiftKeyDown && !isPipe) {
                context.getPlayer().displayClientMessage(Component.translatable("item.pipe_connector.message.holdValidItem").withStyle(ChatFormatting.GOLD), true);
                return InteractionResult.FAIL;
            }

            if (isShiftKeyDown && context.getClickedFace() == Direction.UP) {
                context.getPlayer().displayClientMessage(Component.translatable("item.pipe_connector.message.UpSideNotAllowed").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), true);
                return InteractionResult.FAIL;
            }

            if (firstPosition == null && isShiftKeyDown) {
                firstPosition = clickedPosition;

                facingSideStart = context.getClickedFace();
                PipeConnectorHighlightPacket packet = new PipeConnectorHighlightPacket(firstPosition, facingSideStart);
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) context.getPlayer()), packet);

                LOGGER.debug("firstPosition found: {}", firstPosition);
            } else if (secondPosition == null && isShiftKeyDown) {
                if (clickedPosition.equals(firstPosition)) {
                    resetBlockPosFirstAndSecondPositions(true);
                    return InteractionResult.SUCCESS;
                }
                secondPosition = clickedPosition;
                facingSideEnd = context.getClickedFace();

                PipeConnectorHighlightPacket packet = new PipeConnectorHighlightPacket(secondPosition, facingSideEnd);
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) context.getPlayer()), packet);

                LOGGER.debug("secondPosition found: {}", secondPosition);
                if (secondPosition != null && firstPosition != null) {
                    boolean connectedPipesSuccessfully = connectBlocks(context.getLevel(), firstPosition, secondPosition, depth,  (ServerPlayer) context.getPlayer());
                    resetBlockPosFirstAndSecondPositions(connectedPipesSuccessfully);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {

        // Depth will be displayed as depth-1 to the player to reduce confusion
        int depth = PipeConnectorUtils.getDepthFromStack(stack) - 1;

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

    private boolean connectBlocks(Level level, BlockPos start, BlockPos end, int depth, ServerPlayer serverPlayer) {

        Player player = Minecraft.getInstance().player;

        BlockPos adjustedStart = PipeConnectorUtils.getNeighborInFacingDirection(start, facingSideStart);
        BlockPos adjustedEnd = PipeConnectorUtils.getNeighborInFacingDirection(end, facingSideEnd);

        Item pipe = player.getOffhandItem().getItem();
        Block pipeBlock = Block.byItem(pipe);

        boolean success = PipeConnectorUtils.connectPathWithSegments(level, adjustedStart, adjustedEnd, depth, pipeBlock, serverPlayer);
        return success;
    }

    private void resetBlockPosFirstAndSecondPositions(boolean shouldDisplayMessage) {
        Player player = Minecraft.getInstance().player;
        firstPosition = null;
        secondPosition = null;
        if (shouldDisplayMessage) {
            player.displayClientMessage(Component.translatable("item.pipe_connector.message.resettingPositions"), true);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

}

