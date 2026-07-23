package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.UpdateReplaceModePacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class BuildModeButton extends BaseButton {
    public BuildModeButton(ItemStack stack) {
        super(Component.translatable("item.pipe_connector.gui.button.buildModeTab"), 20, 48);
        updateLabel(stack);
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        TagUtils.setReplaceMode(itemStack, false);
        ClientPacketDistributor.sendToServer(new UpdateReplaceModePacket(false));
        updateLabel(itemStack);
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        return TagUtils.getReplaceMode(itemStack); // clickable only when not selected
    }

    @Override
    public Component getTooltip(ItemStack itemStack) {
        return Component.translatable("item.pipe_connector.gui.button.tooltip.buildMode");
    }

    public void updateLabel(ItemStack stack) {
        boolean replaceMode = TagUtils.getReplaceMode(stack);
        this.setLabel("item.pipe_connector.gui.button.buildModeTab", replaceMode ? ChatFormatting.WHITE : ChatFormatting.GREEN);
        if (this.button != null) this.button.setMessage(this.getLabel());
        if (this.button != null) this.button.active = replaceMode; // disable when already selected
    }
}
