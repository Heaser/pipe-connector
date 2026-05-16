package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.client.gui.editbox.DepthEditBox;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class IncreaseDepthButton extends BaseButton {
    private final DepthEditBox depthEditBox;

    public IncreaseDepthButton(DepthEditBox depthEditBox) {
        super(Component.translatable("item.pipe_connector.gui.button.increaseDepth"), 20, 15);
        this.depthEditBox = depthEditBox;
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        int current = TagUtils.getDepthFromStack(itemStack);
        int newDepth = current + 1;
        int max = PipeConnectorConfig.MAX_DEPTH.get();

        if (newDepth < 0) {
            newDepth = max;
        } else if (newDepth > max) {
            newDepth = 0;
        }

        ClientPacketDistributor.sendToServer(new UpdateDepthPacket(newDepth));
        TagUtils.setDepthToStack(itemStack, newDepth);
        if (depthEditBox != null) {
            depthEditBox.updateValue(newDepth);
        }
    }
}
