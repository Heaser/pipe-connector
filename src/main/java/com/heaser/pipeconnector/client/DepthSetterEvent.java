package com.heaser.pipeconnector.client;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import com.heaser.pipeconnector.utils.TagUtils;
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

import static com.heaser.pipeconnector.utils.TagUtils.getDepthFromStack;
import static com.heaser.pipeconnector.utils.TagUtils.setDepthToStack;

@Mod.EventBusSubscriber(modid = PipeConnector.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DepthSetterEvent {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void MouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        int scroll = (int) event.getScrollDelta();

        if (player == null || !player.isShiftKeyDown() || scroll == 0) {
            return;
        }

        ItemStack pipeConnectorStack = GeneralUtils.heldPipeConnector(player);
        if (pipeConnectorStack == null || pipeConnectorStack.isEmpty()) {
            return;
        }

        int depth = getDepthFromStack(pipeConnectorStack);
        setDepthToStack(pipeConnectorStack, depth + scroll);
        depth = getDepthFromStack(pipeConnectorStack);

        // Syncs with server to prevent cases where the client and server are out of sync
        NetworkHandler.CHANNEL.sendToServer(new UpdateDepthPacket(depth));
        player.displayClientMessage(Component.translatable("item.pipe_connector.message.newDepth", depth).withStyle(ChatFormatting.YELLOW), true);

        event.setCanceled(true);
    }
}
