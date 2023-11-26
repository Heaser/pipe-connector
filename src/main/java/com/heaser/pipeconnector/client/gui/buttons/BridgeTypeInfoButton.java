package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.constants.BridgeType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.heaser.pipeconnector.utils.PipeConnectorUtils.getBridgeType;

public class BridgeTypeInfoButton extends InfoButton {

    public BridgeTypeInfoButton() {
        super();
    }


    @Override
    public void onClick(Button clickedButton) {

    }

    @Override
    public Component getTooltip(ItemStack itemStack) {
        if(getBridgeType(itemStack) == BridgeType.DEFAULT) {
            return Component.translatable("item.pipe_connector.gui.tooltip.aStarPathfinding");
        } else if(getBridgeType(itemStack) == BridgeType.A_STAR) {
            return Component.translatable("item.pipe_connector.gui.tooltip.comingSoon");
        } else if(getBridgeType(itemStack) == BridgeType.STEP) {
            return Component.translatable("item.pipe_connector.gui.tooltip.defaultPathfinding");
        }
        return null;
    }
}
