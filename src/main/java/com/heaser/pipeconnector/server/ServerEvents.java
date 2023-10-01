package com.heaser.pipeconnector.server;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PipeConnector.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ServerEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChangeHeldItem(LivingEquipmentChangeEvent event) {
        PipeConnector.LOGGER.debug("please why you do dis");
        if (event.getSlot() == EquipmentSlot.MAINHAND && event.getEntity() instanceof Player) {
            PipeConnector.LOGGER.debug("please why you do dis!!!");
            ServerPlayer player = (ServerPlayer) event.getEntity();
            ItemStack previousItem = event.getFrom();
            ItemStack newItem = event.getTo();
            if (newItem.getItem() instanceof PipeConnectorItem) {
                PipeConnectorUtils.updateBlockPreview(player, newItem);
            }
            else if (previousItem.getItem() instanceof PipeConnectorItem) {
                PipeConnectorUtils.resetBlockPreview(player);
            }
        }
    }
}
