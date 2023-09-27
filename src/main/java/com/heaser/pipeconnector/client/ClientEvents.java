package com.heaser.pipeconnector.client;

import com.heaser.pipeconnector.PipeConnector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PipeConnector.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            PoseStack stack = event.getPoseStack();
            // event.getLevelRenderer()
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            double partialTicks = event.getPartialTick();
            Entity cameraEntity = event.getCamera().getEntity();
            if (cameraEntity instanceof Player) {
                ClientSetup.PREVIEW_DRAWER.draw(stack, buffer, partialTicks, (Player)cameraEntity);
            }
        }
    }

}
