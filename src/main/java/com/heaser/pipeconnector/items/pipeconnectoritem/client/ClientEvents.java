package com.heaser.pipeconnector.items.pipeconnectoritem.client;

import com.heaser.pipeconnector.items.pipeconnectoritem.PipeConnectorItem;
import com.heaser.pipeconnector.items.pipeconnectoritem.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class ClientEvents {
    private static PoseStack poseStack = new PoseStack();

    public ClientEvents() {

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void MouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        int scroll = (int) event.getScrollDelta();

        if (player == null || !player.isShiftKeyDown() || scroll == 0) {
            return;
        }
        ItemStack pipeConnectorStack = PipeConnectorUtils.holdingPipeConnector(player);
        if (pipeConnectorStack == null) {
            return;
        }

        int depth = PipeConnectorUtils.getDepthFromStack(pipeConnectorStack);

        PipeConnectorUtils.setDepthToStack(pipeConnectorStack, depth += scroll);

        if (depth < 2) {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, 99);
        } else if (depth > 99) {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, 2);
        } else {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, depth);
        }
        int depthText = PipeConnectorUtils.getDepthFromStack(pipeConnectorStack) - 1;
        // Syncs with server to prevent cases where the
        NetworkHandler.CHANNEL.sendToServer(new UpdateDepthPacket(PipeConnectorUtils.getDepthFromStack(pipeConnectorStack)));
        player.displayClientMessage(Component.translatable("item.pipe_connector.message.newDepth", depthText).withStyle(ChatFormatting.YELLOW), true);

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderLast(RenderLevelStageEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            BlockPos blockPos = LookAtBlock.LookingAtBlock((BlockHitResult) player.pick(4.5f, 0, false));
            Direction directionLookedAt = LookAtBlock.LookingAtBlockWithDirection((BlockHitResult) player.pick(4.5f, 0, false));
            ItemStack pipeConnectorStack = PipeConnectorUtils.holdingPipeConnector(player);

            if (PipeConnectorItem.getStartPosClient() == null ||
                    !player.isShiftKeyDown() ||
                    pipeConnectorStack == null ||
                    player.level.getBlockState(blockPos).getBlock() == Blocks.AIR ||
                    directionLookedAt == Direction.UP) {
                return;
            }

            Map<BlockPos, BlockState> map = PipeConnectorUtils.getBlockPosMap(PipeConnectorItem.getStartPosClient(),
                    blockPos,
                    PipeConnectorUtils.getDepthFromStack(pipeConnectorStack),
                    player.level);
        }
    }
}
