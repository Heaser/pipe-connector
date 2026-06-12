package com.heaser.pipeconnector.client.gui;

import com.heaser.pipeconnector.client.gui.editbox.DepthEditBox;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class DepthSlider extends AbstractSliderButton {
    private final ItemStack pipeConnectorStack;
    private final DepthEditBox linkedEditBox;
    private final int maxDepth;
    private int lastSentDepth;

    public DepthSlider(int x, int y, int width, int height, ItemStack pipeConnectorStack, DepthEditBox linkedEditBox) {
        super(x, y, width, height, Component.empty(), 0);
        this.pipeConnectorStack = pipeConnectorStack;
        this.linkedEditBox = linkedEditBox;
        this.maxDepth = Math.max(1, PipeConnectorConfig.MAX_DEPTH.get());
        int depth = TagUtils.getDepthFromStack(pipeConnectorStack);
        this.lastSentDepth = depth;
        this.value = (double) Math.clamp(depth, 0, maxDepth) / maxDepth;
        updateMessage();
    }

    public int getDepth() {
        return (int) Math.round(value * maxDepth);
    }

    public void syncDepth(int depth) {
        this.lastSentDepth = depth;
        this.value = (double) Math.clamp(depth, 0, maxDepth) / maxDepth;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.translatable("item.pipe_connector.gui.label.depthValue", getDepth()));
    }

    @Override
    protected void applyValue() {
        int depth = getDepth();
        if (depth == lastSentDepth) {
            return;
        }
        lastSentDepth = depth;
        TagUtils.setDepthToStack(pipeConnectorStack, depth);
        ClientPacketDistributor.sendToServer(new UpdateDepthPacket(depth));
        if (linkedEditBox != null) {
            linkedEditBox.updateValue(depth);
        }
    }
}
