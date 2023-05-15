package com.heaser.pipeconnector.items.pipeconnectoritem.utils;

import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {
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
}
