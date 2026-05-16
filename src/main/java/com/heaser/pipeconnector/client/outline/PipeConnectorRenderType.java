package com.heaser.pipeconnector.client.outline;

import com.heaser.pipeconnector.PipeConnector;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = PipeConnector.MODID, value = Dist.CLIENT)
public final class PipeConnectorRenderType {
    private static final DepthStencilState NO_DEPTH_TEST = new DepthStencilState(CompareOp.ALWAYS_PASS, false);

    public static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipelines.LINES_TRANSLUCENT.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(PipeConnector.MODID, "pipeline/lines_no_depth_test"))
            .withDepthStencilState(NO_DEPTH_TEST)
            .build();

    public static final RenderPipeline QUADS_NO_DEPTH_PIPELINE = RenderPipelines.DEBUG_FILLED_BOX.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(PipeConnector.MODID, "pipeline/quads_no_depth_test"))
            .withDepthStencilState(NO_DEPTH_TEST)
            .build();

    public static final RenderType LINES_NO_DEPTH_TEST = RenderType.create(
            "pipe_connector_lines_no_depth_test",
            RenderSetup.builder(LINES_NO_DEPTH_PIPELINE).createRenderSetup());

    public static final RenderType THIN_LINES_NO_DEPTH_TEST = RenderType.create(
            "pipe_connector_thin_lines_no_depth_test",
            RenderSetup.builder(LINES_NO_DEPTH_PIPELINE).createRenderSetup());

    public static final RenderType QUADS_NO_DEPTH_TEST = RenderType.create(
            "pipe_connector_quads_no_depth_test",
            RenderSetup.builder(QUADS_NO_DEPTH_PIPELINE).createRenderSetup());

    public static final float LINE_WIDTH_THICK = 4.0f;
    public static final float LINE_WIDTH_THIN = 2.5f;

    private PipeConnectorRenderType() {}

    @SubscribeEvent
    public static void onRegisterPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(LINES_NO_DEPTH_PIPELINE);
        event.registerPipeline(QUADS_NO_DEPTH_PIPELINE);
    }
}
