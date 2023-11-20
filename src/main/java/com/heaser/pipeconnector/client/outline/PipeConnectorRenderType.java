package com.heaser.pipeconnector.client.outline;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class PipeConnectorRenderType extends RenderType {
    public static final RenderType LINES_NO_DEPTH_TEST = RenderType.create("pipe_connector_lines", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES, 256, false, false, RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorShader))
            .setLineState(new LineStateShard(OptionalDouble.empty()))
            .setLayeringState(RenderStateShard.NO_LAYERING)
            .setTransparencyState(NO_TRANSPARENCY)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setDepthTestState(NO_DEPTH_TEST)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .createCompositeState(false));

    public PipeConnectorRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupTask, Runnable clearTask) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupTask, clearTask);
    }
}
