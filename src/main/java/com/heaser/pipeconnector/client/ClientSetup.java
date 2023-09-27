package com.heaser.pipeconnector.client;

import com.heaser.pipeconnector.client.outline.PreviewDrawer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {
    public static final PreviewDrawer PREVIEW_DRAWER = new PreviewDrawer();
}
