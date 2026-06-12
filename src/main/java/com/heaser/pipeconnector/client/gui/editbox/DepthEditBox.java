package com.heaser.pipeconnector.client.gui.editbox;

import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DepthEditBox extends BaseEditBox {
    private static final int VALID_TEXT_COLOR = 0xFFE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xFFFF5555;

    public DepthEditBox(Font font, int x, int y, int width, int height, ItemStack pipeConnectorStack) {
        super(font, x, y, width, height, Component.translatable("item.pipe_connector.gui.label.depth"));
        this.setMaxLength(String.valueOf(PipeConnectorConfig.MAX_DEPTH.get()).length());
        this.setValue(String.valueOf(TagUtils.getDepthFromStack(pipeConnectorStack)));
        this.setFilter(s -> s.matches("\\d*"));
        this.setResponder(s -> this.setTextColor(isValueValid() ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR));
    }

    public boolean isValueValid() {
        String value = this.getValue();
        if (value.isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(value) <= PipeConnectorConfig.MAX_DEPTH.get();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void updateValue(int depth) {
        this.setValue(String.valueOf(depth));
    }
}
