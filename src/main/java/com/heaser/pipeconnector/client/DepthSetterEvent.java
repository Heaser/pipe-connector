package com.heaser.pipeconnector.client;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PipeConnector.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DepthSetterEvent {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void MouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        int scroll = (int) event.getScrollDelta();

        if (player == null || !player.isShiftKeyDown() || scroll == 0) {
            return;
        }

        ItemStack pipeConnectorStack = PipeConnectorUtils.heldPipeConnector(player);
        if (pipeConnectorStack == null || pipeConnectorStack.isEmpty()) {
            return;
        }

        int depth = PipeConnectorUtils.getDepthFromStack(pipeConnectorStack);
        PipeConnectorUtils.setDepthToStack(pipeConnectorStack, depth + scroll);
        depth = PipeConnectorUtils.getDepthFromStack(pipeConnectorStack);

        // Syncs with server to prevent cases where the client and server are out of sync
        NetworkHandler.CHANNEL.sendToServer(new UpdateDepthPacket(depth));
        player.displayClientMessage(Component.translatable("item.pipe_connector.message.newDepth", depth - 1).withStyle(ChatFormatting.YELLOW), true);

        event.setCanceled(true);
    }
}
