package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.ResetPacket;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ResetButton extends BaseButton {
    public ResetButton() {
        super(Component.translatable("item.pipe_connector.gui.button.ResetPipePos"), 20, 80);
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        NetworkHandler.CHANNEL.sendToServer(new ResetPacket());
    }

    @Override
    public boolean shouldClose() {
        return true;
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        BlockPos blockPos = TagUtils.getStartPosition(itemStack);
        return blockPos != null;
    }

    @Override
    public Component getTooltip(ItemStack itemStack) {
        if (!isActive(itemStack)) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledResetPipePos");
        }
        return null;
    }
}