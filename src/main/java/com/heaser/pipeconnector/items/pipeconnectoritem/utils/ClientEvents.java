package com.heaser.pipeconnector.items.pipeconnectoritem.utils;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.pipeconnectoritem.PipeConnectorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.nbt.CompoundTag;

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

        PipeConnectorUtils.setDepthToStack(pipeConnectorStack, depth + scroll);

        if (depth < 1) {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, 99);
        } else if (depth > 99) {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, 1);
        } else {
            PipeConnectorUtils.setDepthToStack(pipeConnectorStack, depth);
        }
        depthText = PipeConnectorUtils.getDepthFromStack(pipeConnectorStack) - 1;
        player.displayClientMessage(Component.literal("Depth was set to " + depthText).withStyle(ChatFormatting.YELLOW), true );

        event.setCanceled(true);
    }

}
