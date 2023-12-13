package com.heaser.pipeconnector.client.gui.interfaces;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface ILabelable {
    Component getLabel(ItemStack itemStack);
}
