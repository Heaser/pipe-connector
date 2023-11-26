package com.heaser.pipeconnector.client.gui.buttons;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public abstract class BaseButton  {
    private int buttonHeight;
    private int buttonWidth;
    public Component label;
    public Button button;

    public BaseButton(String name, int buttonHeight, int buttonWidth) {
        this.label = Component.translatable(name);
        this.buttonHeight = buttonHeight;
        this.buttonWidth = buttonWidth;
    }

    public void setLabel(String name) {
        this.label = Component.translatable(name);
    }
    public void bindButton(Button button) {this.button = button;}

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

    public int getButtonHeight() {
        return buttonHeight;
    }
    public int getButtonWidth() {
        return buttonWidth;
    }

}