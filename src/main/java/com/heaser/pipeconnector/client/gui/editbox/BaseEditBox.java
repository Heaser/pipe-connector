package com.heaser.pipeconnector.client.gui.editbox;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class BaseEditBox extends EditBox {

    public BaseEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }
}
