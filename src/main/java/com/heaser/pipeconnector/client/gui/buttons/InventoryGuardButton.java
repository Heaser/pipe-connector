package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.UpdateInventoryGuard;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class InventoryGuardButton extends BaseButton {

    public InventoryGuardButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, 20);
    }

    public void onClick(Button clickedButton, ItemStack itemStack) {
        if(TagUtils.getPreventInventoryBlockBreaking(itemStack)) {
            this.setLabel("item.pipe_connector.gui.button.off", ChatFormatting.RED );
            this.button.setMessage(this.getLabel());
            TagUtils.setInventoryGuard(itemStack, false);
        } else {
            this.setLabel("item.pipe_connector.gui.button.on", ChatFormatting.GREEN);
            this.button.setMessage(this.getLabel());
            TagUtils.setInventoryGuard(itemStack, true);
        }
        ClientPacketDistributor.sendToServer(new UpdateInventoryGuard(TagUtils.getPreventInventoryBlockBreaking(itemStack)));
    }

    public Component getTooltip(ItemStack itemStack) {
        return Component.translatable("item.pipe_connector.gui.button.tooltip.InventoryGuard");
    }


    private static Component getInitialLabel(ItemStack itemStack) {
        if(TagUtils.getPreventInventoryBlockBreaking(itemStack)) {
            return Component.translatable("item.pipe_connector.gui.button.on").withStyle(ChatFormatting.GREEN);
        } else {
            return Component.translatable("item.pipe_connector.gui.button.off").withStyle(ChatFormatting.RED);
        }
    }

}
