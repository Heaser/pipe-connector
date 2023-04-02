package com.heaser.pipeconnector.items.pipeconnectoritem.utils;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.pipeconnectoritem.PipeConnectorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.nbt.CompoundTag;

public class ClientEvents {
    public ClientEvents() {

    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void MouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        double scroll = event.getScrollDelta();

        if(player == null || !player.isShiftKeyDown() || scroll == 0) {
            return;
        }
        ItemStack pipeConnectorStack = PipeConnectorUtils.holdingPipeConnector(player);
        if(pipeConnectorStack == null) {
            return;
        }

        CompoundTag nbtData = pipeConnectorStack.getOrCreateTag();
        int depth = nbtData.getInt("Depth");

        if(depth >= 0 && depth <= 100) {
            if(depth <= 100)
            {
                nbtData.putInt("Depth", 100);
            } else if(depth >= 0) {
                nbtData.putInt("Depth", 0);
            }
            nbtData.putInt("Depth", depth = depth + (int)scroll);
        }
        event.setCanceled(true);
    }

}
