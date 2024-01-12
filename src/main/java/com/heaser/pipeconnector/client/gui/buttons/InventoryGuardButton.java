package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateInventoryGuard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.heaser.pipeconnector.utils.PipeConnectorUtils.*;

public class InventoryGuardButton extends BaseButton {

    public InventoryGuardButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, 20);
    }

    public void onClick(Button clickedButton, ItemStack itemStack) {
        if(getPreventInventoryBlockBreaking(itemStack)) {
            this.setLabel("item.pipe_connector.gui.button.off", ChatFormatting.RED );
            this.button.setMessage(this.getLabel());
            setPreventInventoryBlockBreaking(itemStack, false);
        } else {
            this.setLabel("item.pipe_connector.gui.button.on", ChatFormatting.GREEN);
            this.button.setMessage(this.getLabel());
            setPreventInventoryBlockBreaking(itemStack, true);
        }
        NetworkHandler.CHANNEL.sendToServer(new UpdateInventoryGuard(getPreventInventoryBlockBreaking(itemStack)));
    }

    public Component getTooltip(ItemStack itemStack) {
        return Component.translatable("item.pipe_connector.gui.button.tooltip.InventoryGuard");
    }


    private static Component getInitialLabel(ItemStack itemStack) {
        if(getPreventInventoryBlockBreaking(itemStack)) {
            return Component.translatable("item.pipe_connector.gui.button.on").withStyle(ChatFormatting.GREEN);
        } else {
            return Component.translatable("item.pipe_connector.gui.button.off").withStyle(ChatFormatting.RED);
        }
    }

}
