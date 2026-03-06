package com.heaser.pipeconnector.client;

import com.heaser.pipeconnector.client.outline.BuildAnimationRenderer;
import com.heaser.pipeconnector.client.outline.PipeVisionRenderer;
import com.heaser.pipeconnector.client.outline.PreviewDrawer;

public class ClientSetup {
    public static final PreviewDrawer PREVIEW_DRAWER = new PreviewDrawer();
    public static final PipeVisionRenderer PIPE_VISION_RENDERER = new PipeVisionRenderer();
    public static final BuildAnimationRenderer BUILD_ANIMATION_RENDERER = new BuildAnimationRenderer();
}
