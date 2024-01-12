package com.heaser.pipeconnector.client.gui.buttons;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public abstract class BaseButton  {
    private int buttonHeight;
    private int buttonWidth;
    public Component label;
    public Button button;

    public BaseButton(Component initTitle, int buttonHeight, int buttonWidth) {
        this.label = initTitle;
        this.buttonHeight = buttonHeight;
        this.buttonWidth = buttonWidth;
    }

    public void setLabel(String name, ChatFormatting chatFormatting) {
        if(chatFormatting == null) {
            chatFormatting = ChatFormatting.WHITE;
        }
        this.label = Component.translatable(name).withStyle(chatFormatting);
    }

    public Component getLabel() {
        return label;
    }
    public void bindButton(Button button) {this.button = button;}

    abstract public void onClick(Button clickedButton, ItemStack itemStack);

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

    @Nullable
    public List<Component> getTooltipList(ItemStack itemStack) {
        return null;
    }

    public int getButtonHeight() {
        return buttonHeight;
    }
    public int getButtonWidth() {
        return buttonWidth;
    }

}