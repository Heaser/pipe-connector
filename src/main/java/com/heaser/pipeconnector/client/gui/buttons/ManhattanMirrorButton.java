package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.UpdateManhattanMirrorPacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ManhattanMirrorButton extends BaseButton {
    public ManhattanMirrorButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, 20);
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        boolean currentState = TagUtils.getMirrorManhattan(itemStack);
        boolean newState = !currentState;
        
        TagUtils.setMirrorManhattan(itemStack, newState);
        PacketDistributor.sendToServer(new UpdateManhattanMirrorPacket(newState));

        this.label = Component.literal("<>").withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED);
        this.button.setMessage(this.getLabel());
    }

    public void updateLabel(ItemStack itemStack) {
        boolean isMirrored = TagUtils.getMirrorManhattan(itemStack);
        this.label = Component.literal("<>").withStyle(isMirrored ? ChatFormatting.GREEN : ChatFormatting.RED);
        this.button.setMessage(this.getLabel());
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        return true;
    }

    private static Component getInitialLabel(ItemStack itemStack) {
        boolean isMirrored = TagUtils.getMirrorManhattan(itemStack);
        return Component.literal("<>").withStyle(isMirrored ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    @Override
    public Component getTooltip(ItemStack itemStack) {
        return Component.translatable("item.pipe_connector.gui.button.tooltip.manhattanMirror");
    }
}
