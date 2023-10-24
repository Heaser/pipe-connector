package com.heaser.pipeconnector.client.proxy;

import net.minecraft.client.gui.screens.Screen;

public class GeneralClientProxy implements IClientProxy {
    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }
}
