package com.heaser.pipeconnector.client.gui.labels;

import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class AvoidInventoryBlocksText implements ILabelable {
    public AvoidInventoryBlocksText() {}

    @Override
    public Component getLabel(ItemStack itemStack) {
        return Component.translatable("item.pipe_connector.gui.label.avoidInventoryBlocks");
    }
}

