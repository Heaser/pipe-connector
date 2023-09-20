package com.heaser.pipeconnector.items.pipeconnectoritem.client;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.pipeconnectoritem.utils.PipeConnectorUtils;
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
public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void MouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        int scroll = (int) event.getScrollDelta();

        if (player == null || !player.isShiftKeyDown() || scroll == 0) {
            return;
        }

        ItemStack pipeConnectorStack = PipeConnectorUtils.holdingPipeConnector(player);
        if (pipeConnectorStack == null || pipeConnectorStack.isEmpty()) {
            return;
        }

        int depth = PipeConnectorUtils.getDepthFromStack(pipeConnectorStack);

        depth += scroll;
        if (depth < 2) {
            depth = 99;
        } else if (depth > 99) {
            depth = 2;
        }
        PipeConnectorUtils.setDepthToStack(pipeConnectorStack, depth);


        // Syncs with server to prevent cases where the client and server are out of sync
        NetworkHandler.CHANNEL.sendToServer(new UpdateDepthPacket(depth));
        player.displayClientMessage(Component.translatable("item.pipe_connector.message.newDepth", depth - 1).withStyle(ChatFormatting.YELLOW), true);

        event.setCanceled(true);
    }
}
