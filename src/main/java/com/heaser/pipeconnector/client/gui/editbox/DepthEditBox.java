package com.heaser.pipeconnector.client.gui.editbox;

import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DepthEditBox extends BaseEditBox {

    public DepthEditBox(Font font, int x, int y, int width, int height, ItemStack pipeConnectorStack) {
        super(font, x, y, width, height, Component.literal("Depth"));
        this.setMaxLength(2);
        this.setValue(String.valueOf(TagUtils.getDepthFromStack(pipeConnectorStack)));
        this.setFilter(s -> s.matches("\\d*"));
    }

    public void updateValue(int depth) {
        this.setValue(String.valueOf(depth));
    }
}
