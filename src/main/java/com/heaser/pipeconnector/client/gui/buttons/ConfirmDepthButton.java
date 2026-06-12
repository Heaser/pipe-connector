package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.client.gui.editbox.DepthEditBox;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ConfirmDepthButton extends BaseButton {
    private final DepthEditBox depthEditBox;

    public ConfirmDepthButton(DepthEditBox depthEditBox) {
        super(Component.translatable("item.pipe_connector.gui.button.confirmDepth"), 20, 15);
        this.depthEditBox = depthEditBox;
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        if (depthEditBox == null || !depthEditBox.isValueValid()) return;

        try {
            int max = PipeConnectorConfig.MAX_DEPTH.get();
            int depth = Math.min(Integer.parseInt(depthEditBox.getValue()), max);

            ClientPacketDistributor.sendToServer(new UpdateDepthPacket(depth));
            TagUtils.setDepthToStack(itemStack, depth);
            depthEditBox.updateValue(depth);
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        return depthEditBox != null && depthEditBox.isValueValid();
    }

    @Override
    public Component getTooltip(ItemStack itemStack) {
        if (depthEditBox != null && !depthEditBox.isValueValid()) {
            return Component.translatable("item.pipe_connector.gui.tooltip.depthOutOfRange",
                    PipeConnectorConfig.MAX_DEPTH.get());
        }
        return null;
    }
}
