package com.heaser.pipeconnector.client;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.iris.IrisCompatibility;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;


@EventBusSubscriber(modid = PipeConnector.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderWorld(RenderLevelStageEvent.AfterWeather event) {
        IrisCompatibility.registerShaderPipelines();
        PoseStack stack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Entity cameraEntity = camera.entity();
        if (cameraEntity instanceof Player) {
            ClientSetup.PREVIEW_DRAWER.handleOnRenderLevel(stack, buffer, (Player)cameraEntity);
            ClientSetup.PIPE_VISION_RENDERER.handleOnRenderLevel(stack, buffer, (Player)cameraEntity);
            ClientSetup.BUILD_ANIMATION_RENDERER.handleOnRenderLevel(stack, buffer);
            buffer.endBatch();
        }
    }
}
