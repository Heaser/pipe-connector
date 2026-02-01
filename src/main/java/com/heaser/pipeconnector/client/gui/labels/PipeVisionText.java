package com.heaser.pipeconnector.client.gui.labels;

import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PipeVisionText implements ILabelable {
    @Override
    public Component getLabel(ItemStack itemStack) {
        return Component.translatable("item.pipe_connector.gui.label.pipe_vision");
    }
}
