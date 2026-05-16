package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.network.UpdateAvoidInventoryBlocks;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class AvoidInventoryBlocksButton extends BaseButton {
    public AvoidInventoryBlocksButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, 20);
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        if (TagUtils.getAvoidInventoryBlocks(itemStack)) {
            this.setLabel("item.pipe_connector.gui.button.off", ChatFormatting.RED);
            this.button.setMessage(this.getLabel());
            TagUtils.setAvoidInventoryBlocks(itemStack, false);
        } else {
            this.setLabel("item.pipe_connector.gui.button.on", ChatFormatting.GREEN);
            this.button.setMessage(this.getLabel());
            TagUtils.setAvoidInventoryBlocks(itemStack, true);
        }
        ClientPacketDistributor.sendToServer(new UpdateAvoidInventoryBlocks(TagUtils.getAvoidInventoryBlocks(itemStack)));
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        return TagUtils.getBridgeType(itemStack).equals(BridgeType.A_STAR);
    }

    public List<Component> getTooltipList(ItemStack itemStack) {
        List<Component> tooltipList = new ArrayList<>();
        if(!isActive(itemStack)) {
            tooltipList.add(Component.translatable("item.pipe_connector.gui.button.tooltip.inactiveUtilizeExistingPipes"));
        } else {
            tooltipList.add(Component.translatable("item.pipe_connector.gui.button.tooltip.avoidInventoryBlocks"));
        }
        return tooltipList;
    }

    private static Component getInitialLabel(ItemStack itemStack) {
        if (TagUtils.getAvoidInventoryBlocks(itemStack)) {
            return Component.translatable("item.pipe_connector.gui.button.on").withStyle(ChatFormatting.GREEN);
        } else {
            return Component.translatable("item.pipe_connector.gui.button.off").withStyle(ChatFormatting.RED);
        }
    }
}

