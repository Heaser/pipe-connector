package com.heaser.pipeconnector.client.outline;

import com.heaser.pipeconnector.PipeConnector;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = PipeConnector.MODID, value = Dist.CLIENT)
public final class PipeConnectorRenderType {
    private static final DepthStencilState NO_DEPTH_TEST = new DepthStencilState(CompareOp.ALWAYS_PASS, false);
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final Identifier WHITE_TEXTURE = Identifier.fromNamespaceAndPath(PipeConnector.MODID, "textures/white.png");

    public static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipelines.LINES_TRANSLUCENT.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(PipeConnector.MODID, "pipeline/lines_no_depth_test"))
            .withDepthStencilState(NO_DEPTH_TEST)
            .build();

    public static final RenderPipeline PARTICLE_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(PipeConnector.MODID, "pipeline/particle_no_depth_test"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(NO_DEPTH_TEST)
            .build();

    public static final RenderType LINES_NO_DEPTH_TEST = RenderType.create(
            "pipe_connector_lines_no_depth_test",
            RenderSetup.builder(LINES_NO_DEPTH_PIPELINE).createRenderSetup());

    public static final RenderType THIN_LINES_NO_DEPTH_TEST = RenderType.create(
            "pipe_connector_thin_lines_no_depth_test",
            RenderSetup.builder(LINES_NO_DEPTH_PIPELINE).createRenderSetup());

    public static final RenderType QUADS_TEXTURED_NO_DEPTH = RenderType.create(
            "pipe_connector_quads_textured_no_depth",
            RenderSetup.builder(PARTICLE_NO_DEPTH_PIPELINE)
                    .withTexture("Sampler0", WHITE_TEXTURE)
                    .useLightmap()
                    .createRenderSetup());

    public static final float LINE_WIDTH_THICK = 4.0f;
    public static final float LINE_WIDTH_THIN = 2.5f;

    private PipeConnectorRenderType() {}

    static void filledVertex(VertexConsumer vc, Matrix4f mat, float x, float y, float z, int color) {
        vc.addVertex(mat, x, y, z).setUv(0.5f, 0.5f).setColor(color).setLight(FULL_BRIGHT);
    }

    static void filledVertex(VertexConsumer vc, Matrix4f mat, float x, float y, float z, float r, float g, float b, float a) {
        filledVertex(vc, mat, x, y, z, (clamp255(a) << 24) | (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b));
    }

    private static int clamp255(float f) {
        return Math.min(255, Math.max(0, (int) (f * 255f)));
    }

    @SubscribeEvent
    public static void onRegisterPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(LINES_NO_DEPTH_PIPELINE);
        event.registerPipeline(PARTICLE_NO_DEPTH_PIPELINE);
    }
}
