package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.UpdatePipeVision;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class PipeVisionButton extends BaseButton {
    public PipeVisionButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, 20);
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        boolean currentState = TagUtils.getPipeVision(itemStack);
        boolean newState = !currentState;
        
        TagUtils.setPipeVision(itemStack, newState);
        ClientPacketDistributor.sendToServer(new UpdatePipeVision(newState));
        
        this.setLabel(newState ? "item.pipe_connector.gui.button.on" : "item.pipe_connector.gui.button.off", 
                      newState ? ChatFormatting.GREEN : ChatFormatting.RED);
        this.button.setMessage(this.getLabel());
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        return true;
    }

    private static Component getInitialLabel(ItemStack itemStack) {
        if (TagUtils.getPipeVision(itemStack)) {
            return Component.translatable("item.pipe_connector.gui.button.on").withStyle(ChatFormatting.GREEN);
        } else {
            return Component.translatable("item.pipe_connector.gui.button.off").withStyle(ChatFormatting.RED);
        }
    }
}
