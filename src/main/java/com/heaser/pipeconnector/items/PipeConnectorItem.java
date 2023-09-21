package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.particles.ParticleHelper;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class PipeConnectorItem extends Item {
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
                PipeConnectorUtils.resetPositionAndDirectionTags(player.getMainHandItem(), player, true);
            }
        }
        return super.use(level, player, useHand);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext context) {
        boolean success = false;
        Player usingPlayer = context.getPlayer();
        Level level = context.getLevel();
        ItemStack interactedItem = context.getItemInHand();
        BlockPos clickedPosition = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if(usingPlayer != null) {
            boolean isShiftKeyDown = usingPlayer.isShiftKeyDown();
            // Handle logic for both client and server
            handleCommonLogic(interactedItem);

            // Handle Server logic
            if(!level.isClientSide) {
                success = handleServerSideLogic(usingPlayer, isShiftKeyDown, level, interactedItem, clickedPosition, clickedFace, context);
            }
        }

        return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void handleCommonLogic(ItemStack interactedItem) {
        int depth = PipeConnectorUtils.getDepthFromStack(interactedItem);
        if (depth == 0) {
            PipeConnectorUtils.setDepthToStack(interactedItem, 1);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    private boolean handleServerSideLogic(Player usingPlayer, boolean isShiftKeyDown, Level level, ItemStack interactedItem, BlockPos clickedPosition, Direction clickedFace, UseOnContext context) {
        boolean holdingAllowedPipe = usingPlayer.getOffhandItem().is(TagKeys.PLACEABLE_ITEMS);
        if (isShiftKeyDown) {
            if (!holdingAllowedPipe) {
                usingPlayer.displayClientMessage(Component.translatable("item.pipe_connector.message.holdValidItem").withStyle(ChatFormatting.GOLD), true);
                return false;
            }
            if (clickedFace == Direction.UP) {
                usingPlayer.displayClientMessage(Component.translatable("item.pipe_connector.message.UpSideNotAllowed").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), true);
                return false;
            }
            if(PipeConnectorUtils.getStartPosition(interactedItem) == null) {
                PipeConnectorUtils.setStartPositionAndDirection(interactedItem, clickedFace, clickedPosition);
                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, clickedPosition.relative(clickedFace));
            } else {
                if (clickedPosition.equals(PipeConnectorUtils.getStartPosition(interactedItem))
                        && clickedFace.equals(PipeConnectorUtils.getStartDirection(interactedItem))) {
                    PipeConnectorUtils.resetPositionAndDirectionTags(interactedItem, usingPlayer, true);
                    return false;
                }
                PipeConnectorUtils.setEndPositionAndDirection(interactedItem, clickedFace, clickedPosition);
                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, clickedPosition.relative(clickedFace));

               Direction startDirection = PipeConnectorUtils.getStartDirection(interactedItem);
               BlockPos StartPos = PipeConnectorUtils.getStartPosition(interactedItem);
               int depth = PipeConnectorUtils.getDepthFromStack(interactedItem);
               boolean wasSuccessful = connectBlocks(usingPlayer,
                       StartPos,
                       startDirection,
                       clickedPosition,
                       clickedFace,
                       depth,
                       context);
               PipeConnectorUtils.resetPositionAndDirectionTags(interactedItem, usingPlayer, wasSuccessful);
            }

        }
        return true;
    }

    // -----------------------------------------------------------------------------------------------------------------
    @ParametersAreNonnullByDefault
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
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

    private boolean connectBlocks(Player player,
                                  BlockPos startPos,
                                  Direction startDirection,
                                  BlockPos endPos,
                                  Direction endDirection,
                                  int depth,
                                  UseOnContext context) {
// TODO: Return this to how they were, the following is just for testing.
        return PipeConnectorUtils.connectPathWithSegments(player,
                startPos.relative(startDirection),
                endPos.relative(endDirection),
                depth,
                Block.byItem(player.getOffhandItem().getItem()),
                context);
    }

    // -----------------------------------------------------------------------------------------------------------------

}

