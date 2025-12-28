package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.client.gui.editbox.DepthEditBox;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ConfirmDepthButton extends BaseButton {
    private final DepthEditBox depthEditBox;

    public ConfirmDepthButton(DepthEditBox depthEditBox) {
        super(Component.translatable("item.pipe_connector.gui.button.confirmDepth"), 20, 15);
        this.depthEditBox = depthEditBox;
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        if (depthEditBox == null) return;
        
        try {
            String val = depthEditBox.getValue();
            if (val.isEmpty()) return;
            int depth = Integer.parseInt(val);
            int max = PipeConnectorConfig.MAX_DEPTH.get();

            if (depth < 0) depth = 0;
            if (depth > max) depth = max;

            PacketDistributor.sendToServer(new UpdateDepthPacket(depth));
            TagUtils.setDepthToStack(itemStack, depth);
            depthEditBox.updateValue(depth);
        } catch (NumberFormatException e) {
            // ignore
        }
    }
}
