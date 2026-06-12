package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SolidPreviewButton extends BaseButton {
    public SolidPreviewButton(ItemStack stack) {
        super(Component.translatable("item.pipe_connector.gui.button.preview_solid"), 20, 48);
        updateLabel(stack);
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        TagUtils.setSolidPreview(itemStack, true);
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new com.heaser.pipeconnector.network.UpdateSolidPreview(true));
        updateLabel(itemStack);
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        return !TagUtils.getSolidPreview(itemStack); // clickable only when not selected
    }

    public void updateLabel(ItemStack stack) {
        boolean solid = TagUtils.getSolidPreview(stack);
        this.setLabel("item.pipe_connector.gui.button.preview_solid", solid ? ChatFormatting.GREEN : ChatFormatting.WHITE);
        if (this.button != null) this.button.setMessage(this.getLabel());
        if (this.button != null) this.button.active = !solid; // disable when already selected
    }
}
