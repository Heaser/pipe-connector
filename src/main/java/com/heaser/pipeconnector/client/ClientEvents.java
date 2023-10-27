package com.heaser.pipeconnector.client;

import com.heaser.pipeconnector.PipeConnector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.openjdk.nashorn.internal.ir.annotations.Ignore;

@Mod.EventBusSubscriber(modid = PipeConnector.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {



    /*
    // Currently commented out to solve some compatibility issues with other mods - using onGameRenderOverlay instead.
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderWorld(RenderLevelStageEvent event) {

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            PoseStack stack = event.getPoseStack();
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            double partialTicks = event.getPartialTick();
            Entity cameraEntity = event.getCamera().getEntity();
            if (cameraEntity instanceof Player) {
                ClientSetup.PREVIEW_DRAWER.handleOnRenderLevel(stack, buffer, partialTicks, (Player)cameraEntity);
            }
        }
    }

}
