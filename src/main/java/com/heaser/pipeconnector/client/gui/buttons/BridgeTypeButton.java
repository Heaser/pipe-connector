package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateBridgeTypePacket;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;

public class BridgeTypeButton extends BaseButton {
    private Player player;
    public BridgeTypeButton(Player player) {
        super("item.pipe_connector.gui.button.defaultPathfinding", 20, 80);
        this.player = player;
    }

    @Override
    public void onClick(Button clickedButton) {
        BridgeType bridgeType = PipeConnectorUtils.getBridgeType(player.getMainHandItem());
        if(bridgeType == BridgeType.DEFAULT) {
            bridgeType = BridgeType.A_STAR;
            this.setLabel("item.pipe_connector.gui.button.aStarPathfinding");
            NetworkHandler.CHANNEL.sendToServer(new UpdateBridgeTypePacket(BridgeType.A_STAR));
        } else if(bridgeType == BridgeType.A_STAR) {
            bridgeType = BridgeType.STEP;
            this.setLabel("item.pipe_connector.gui.button.stepPathfinding");
            NetworkHandler.CHANNEL.sendToServer(new UpdateBridgeTypePacket(BridgeType.STEP));
        } else if(bridgeType == BridgeType.STEP) {
            bridgeType = BridgeType.DEFAULT;
            this.setLabel("item.pipe_connector.gui.button.defaultPathfinding");
            NetworkHandler.CHANNEL.sendToServer(new UpdateBridgeTypePacket(BridgeType.DEFAULT));
        }
    }
}

