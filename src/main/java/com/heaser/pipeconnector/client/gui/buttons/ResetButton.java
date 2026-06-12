package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.ResetPacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ResetButton extends BaseButton {
    public ResetButton() {
        super(Component.translatable("item.pipe_connector.gui.button.ResetPipePos"), 20, 118);
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        ClientPacketDistributor.sendToServer(new ResetPacket());
    }

    @Override
    public boolean shouldClose() {
        return true;
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        return !TagUtils.getNodesFromStack(itemStack).isEmpty();
    }

    @Override
    public Component getTooltip(ItemStack itemStack) {
        if (!isActive(itemStack)) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledResetPipePos");
        }
        return null;
    }
}