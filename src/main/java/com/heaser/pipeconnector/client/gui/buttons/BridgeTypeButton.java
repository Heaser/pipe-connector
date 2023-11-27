package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateBridgeTypePacket;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;

public class BridgeTypeButton extends BaseButton {

    public BridgeTypeButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, 100);
    }


    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        BridgeType bridgeType = PipeConnectorUtils.getBridgeType(itemStack);

        if(bridgeType == BridgeType.DEFAULT) {
            bridgeType = BridgeType.A_STAR;
            this.setLabel("item.pipe_connector.gui.button.aStarPathfinding");
            this.button.setMessage(this.getLabel());
        } else if(bridgeType == BridgeType.A_STAR) {
            bridgeType = BridgeType.DEFAULT;
            this.setLabel("item.pipe_connector.gui.button.defaultPathfinding");
            this.button.setMessage(this.getLabel());
        }
        NetworkHandler.CHANNEL.sendToServer(new UpdateBridgeTypePacket(bridgeType));
    }


    private static String getInitialLabel(ItemStack itemStack) {
        BridgeType bridgeType = PipeConnectorUtils.getBridgeType(itemStack);
        return switch (bridgeType) {
            case A_STAR -> "item.pipe_connector.gui.button.aStarPathfinding";
            default -> "item.pipe_connector.gui.button.defaultPathfinding";
        };
    }
}



