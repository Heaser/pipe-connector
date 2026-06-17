package com.heaser.pipeconnector.compatibility.iris;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.client.outline.PipeConnectorRenderType;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
public final class IrisCompatibility {
    private static boolean done = false;

    private IrisCompatibility() {}

    public static void registerShaderPipelines() {
        if (done) {
            return;
        }
        done = true;
        if (!ModList.get().isLoaded("iris")) {
            return;
        }
        try {
            Class<?> shaderKeyClass = Class.forName("net.irisshaders.iris.pipeline.programs.ShaderKey");
            Class<?> irisPipelines = Class.forName("net.irisshaders.iris.pipeline.IrisPipelines");
            Method assignPipeline = irisPipelines.getMethod("assignPipeline", RenderPipeline.class, shaderKeyClass);
            Object particlesKey = shaderKeyClass.getField("PARTICLES").get(null);
            assignPipeline.invoke(null, PipeConnectorRenderType.PARTICLE_NO_DEPTH_PIPELINE, particlesKey);
            PipeConnector.LOGGER.info("Registered filled-preview pipeline with Iris (PARTICLES program).");
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Could not register render pipeline with Iris; filled preview may be invisible under shaders.", e);
        }
    }
}
