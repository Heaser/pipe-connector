package com.heaser.pipeconnector.client.gui.buttons;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public abstract class BaseButton {
    public Component label;

    public BaseButton(String name) {
        this.label = Component.translatable(name);
    }

    abstract public void onClick(Button clickedButton);

    public boolean shouldClose() {
        return false;
    }

    public boolean isActive(ItemStack itemStack) {
       return true;
    }

    @Nullable
    public Component getTooltip(ItemStack itemStack) {
        return null;
    }
}