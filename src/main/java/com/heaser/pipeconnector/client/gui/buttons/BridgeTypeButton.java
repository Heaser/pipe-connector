package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.BuildPipesPacket;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BridgeTypeButton extends BaseButton {

    public BridgeTypeButton() {
        super("item.pipe_connector.gui.button.PlacePipes");
    }

    @Override
    public void onClick(Button clickedButton) {

    }
}

