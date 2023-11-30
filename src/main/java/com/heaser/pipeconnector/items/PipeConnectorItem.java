package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.client.proxy.GeneralClientProxy;
import com.heaser.pipeconnector.client.proxy.IClientProxy;
import com.heaser.pipeconnector.client.proxy.items.IPipeConnectorItemProxy;
import com.heaser.pipeconnector.client.proxy.items.PipeConnectorItemProxy;
import com.heaser.pipeconnector.particles.ParticleHelper;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.ChatFormatting;
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
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class PipeConnectorItem extends Item {
    private final IPipeConnectorItemProxy itemProxy = DistExecutor.unsafeRunForDist(() -> PipeConnectorItemProxy::new,
        () -> () -> null);
    private final IClientProxy clientProxy = DistExecutor.unsafeRunForDist(() -> GeneralClientProxy::new, () -> () -> null);

    public PipeConnectorItem(Properties properties) {
        super(properties);
    }

    @ParametersAreNonnullByDefault
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack interactedItem = player.getItemInHand(useHand);
        if (level.isClientSide() && itemProxy != null) {
            BlockPos playerLookingAt = player.blockPosition().relative(player.getDirection());
            boolean isAir = level.getBlockState(playerLookingAt).isAir();
            boolean isShiftKeyDown = player.isShiftKeyDown();

            if (useHand == InteractionHand.MAIN_HAND && isShiftKeyDown && isAir) {
                itemProxy.openPipeConnectorGui(interactedItem);
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

        // Handle Server logic
        if(GeneralUtils.isServerSide(level)) {
            return handleServerSideUseOn(context);
        }

        return InteractionResult.FAIL;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private void handleCommonUseOn(ItemStack interactedItem) {
        int depth = PipeConnectorUtils.getDepthFromStack(interactedItem);
        if (depth == -1) {
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

        if (isShiftKeyDown) {
            if (!GeneralUtils.isPlaceableBlock(usingPlayer)) {
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
                PipeConnectorUtils.setDimension(interactedItem, level.dimensionTypeId().toString());
                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, clickedPosition.relative(clickedFace));
            } else {
                if (!level.dimensionTypeId().toString().equals(PipeConnectorUtils.getDimension(interactedItem))) {
                    PipeConnectorUtils.resetPositionAndDirectionTags(interactedItem, usingPlayer, false);
                    return InteractionResult.FAIL;
                }
                else if (clickedPosition.equals(startPos) && clickedFace.equals(startDirection)) {
                    PipeConnectorUtils.resetPositionAndDirectionTags(interactedItem, usingPlayer, true);
                    return InteractionResult.FAIL;
                }
                PipeConnectorUtils.setEndPositionAndDirection(interactedItem, clickedFace, clickedPosition);

                ParticleHelper.serverSpawnMarkerParticle((ServerLevel) level, clickedPosition.relative(clickedFace));
            }

        }
        return InteractionResult.SUCCESS;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        if (clientProxy != null) {
            if (clientProxy.hasShiftDown()) {
                components.add(Component.translatable("item.pipe_connector.tooltip.usageExplanation").withStyle(ChatFormatting.DARK_AQUA));
                components.add(Component.translatable("item.pipe_connector.tooltip.openGui").withStyle(ChatFormatting.BLUE));
                components.add(Component.translatable("item.pipe_connector.tooltip.changeDepthExplanation").withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                components.add(Component.translatable("item.pipe_connector.tooltip.shiftForMoreInfo").withStyle(ChatFormatting.GOLD));
            }
        }
        super.appendHoverText(stack, level, components, tooltipFlag);
    }

    // -----------------------------------------------------------------------------------------------------------------
}

