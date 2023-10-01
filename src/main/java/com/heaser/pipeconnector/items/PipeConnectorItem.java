package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.client.gui.PipeConnectorGui;
import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.SyncBuildPath;
import com.heaser.pipeconnector.particles.ParticleHelper;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.PreviewInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;

public class PipeConnectorItem extends Item {
    public PipeConnectorItem(Properties properties) {
        super(properties);
    }

    // -----------------------------------------------------------------------------------------------------------------
    @ParametersAreNonnullByDefault
    @NotNull
    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack interactedItem = player.getItemInHand(useHand);
        if (level.isClientSide()) {
            BlockPos playerLookingAt = player.blockPosition().relative(player.getDirection());
            boolean isAir = level.getBlockState(playerLookingAt).isAir();
            boolean isShiftKeyDown = player.isShiftKeyDown();

            if (useHand == InteractionHand.MAIN_HAND && isShiftKeyDown && isAir) {
                Minecraft.getInstance().setScreen(new PipeConnectorGui(interactedItem));
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(useHand));
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack interactedItem = context.getItemInHand();

        // Handle logic for both client and server
        handleCommonUseOn(interactedItem);

        if (GeneralUtils.isServerSide((level))) {

        }

        // Handle Server logic
        if(GeneralUtils.isServerSide(level)) {
            return handleServerSideUseOn(context);
        }

        return InteractionResult.FAIL;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private void handleCommonUseOn(ItemStack interactedItem) {
        int depth = PipeConnectorUtils.getDepthFromStack(interactedItem);
        if (depth == 0) {
            PipeConnectorUtils.setDepthToStack(interactedItem, 2);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    private InteractionResult handleServerSideUseOn(UseOnContext context) {
        Player usingPlayer = context.getPlayer();
        Level level = context.getLevel();
        ItemStack interactedItem = context.getItemInHand();
        BlockPos clickedPosition = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if(usingPlayer == null) {
            return InteractionResult.FAIL;
        }
        boolean isShiftKeyDown = usingPlayer.isShiftKeyDown();

        boolean isHoldingAllowedPipe = usingPlayer.getOffhandItem().is(TagKeys.PLACEABLE_ITEMS);
        if (isShiftKeyDown) {
            if (!isHoldingAllowedPipe) {
                usingPlayer.displayClientMessage(Component.translatable("item.pipe_connector.message.holdValidItem")
                        .withStyle(ChatFormatting.GOLD), true);
                return InteractionResult.FAIL;
            }
            if (clickedFace == Direction.UP) {
                usingPlayer.displayClientMessage(Component.translatable("item.pipe_connector.message.UpSideNotAllowed")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), true);
                return InteractionResult.FAIL;
            }

            BlockPos startPos = PipeConnectorUtils.getStartPosition(interactedItem);
            Direction startDirection = PipeConnectorUtils.getStartDirection(interactedItem);

            if(startPos == null) {
                PipeConnectorUtils.setStartPositionAndDirection(interactedItem, clickedFace, clickedPosition);
                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, clickedPosition.relative(clickedFace));
            } else {
                if (clickedPosition.equals(startPos) && clickedFace.equals(startDirection)) {
                    PipeConnectorUtils.resetPositionAndDirectionTags(interactedItem, usingPlayer, true);
                    PipeConnectorUtils.resetBlockPreview((ServerPlayer) usingPlayer);
                    return InteractionResult.FAIL;
                }
                PipeConnectorUtils.setEndPositionAndDirection(interactedItem, clickedFace, clickedPosition);
                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, clickedPosition.relative(clickedFace));
                PipeConnectorUtils.updateBlockPreview((ServerPlayer) usingPlayer, interactedItem);
            }

        }
        return InteractionResult.SUCCESS;
    }

    // -----------------------------------------------------------------------------------------------------------------
    @ParametersAreNonnullByDefault
    @Override
    @OnlyIn(Dist.CLIENT)
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

}

