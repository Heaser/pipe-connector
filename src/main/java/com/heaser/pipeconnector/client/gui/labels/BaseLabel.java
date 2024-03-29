package com.heaser.pipeconnector.client.gui.labels;

import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BaseLabel implements ILabelable {
    public String name;

    public BaseLabel(String name) {
        this.name = name;
    }

    public abstract String getValue(ItemStack itemStack);

    public Component getLabel(ItemStack itemStack) {
        return Component.translatable(name, getValue(itemStack));
    }
}

