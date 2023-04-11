package com.heaser.pipeconnector.items.pipeconnectoritem.utils;

import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import java.util.Random;



public class ClientEvents {
    private int depthText;
    public ClientEvents() {

    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void MouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        int scroll = (int)event.getScrollDelta();

        if(player == null || !player.isShiftKeyDown() || scroll == 0) {
            return;
        }
        ItemStack pipeConnectorStack = PipeConnectorUtils.holdingPipeConnector(player);
        if(pipeConnectorStack == null) {
            return;
        }

        int depth = PipeConnectorUtils.getDepthFromStack(pipeConnectorStack);

        PipeConnectorUtils.setDepthToStack(pipeConnectorStack, depth += scroll);

        if (depth < 1) {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, 99);
        } else if (depth > 99) {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, 1);
        } else {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, depth);
        }
        depthText = PipeConnectorUtils.getDepthFromStack(pipeConnectorStack) - 1;
        // Syncs with server to prevent cases where the
        NetworkHandler.CHANNEL.sendToServer(new UpdateDepthPacket(PipeConnectorUtils.getDepthFromStack(pipeConnectorStack)));
        player.displayClientMessage(Component.literal("Depth was set to " + depthText).withStyle(ChatFormatting.YELLOW), true );

        event.setCanceled(true);
    }
}
