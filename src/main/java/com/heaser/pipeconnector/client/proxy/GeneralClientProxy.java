package com.heaser.pipeconnector.client.proxy;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public class GeneralClientProxy implements IClientProxy {
    public boolean hasShiftDown() {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }
}
