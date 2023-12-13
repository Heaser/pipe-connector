package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateBridgeTypePacket;
import com.heaser.pipeconnector.network.UpdateUtilizeExistingPipes;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.heaser.pipeconnector.utils.PipeConnectorUtils.*;

public class UtilizeExistingPipesButton extends BaseButton {
    public UtilizeExistingPipesButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, 20);
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        if(getUtilizeExistingPipes(itemStack)) {
            this.setLabel("item.pipe_connector.gui.button.off");
            this.button.setMessage(this.getLabel());
            setUtilizeExistingPipes(itemStack, false);
        } else {
            this.setLabel("item.pipe_connector.gui.button.on");
            this.button.setMessage(this.getLabel());
            setUtilizeExistingPipes(itemStack, true);
        }
        NetworkHandler.CHANNEL.sendToServer(new UpdateUtilizeExistingPipes(getUtilizeExistingPipes(itemStack)));
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        return PipeConnectorUtils.getBridgeType(itemStack).equals(BridgeType.A_STAR);
    }

    public List<Component> getTooltipList(ItemStack itemStack) {
        List<Component> tooltipList = new ArrayList<>();
        if(!isActive(itemStack)) {
            tooltipList.add(Component.translatable("item.pipe_connector.gui.button.tooltip.inactiveUtilizeExistingPipes"));
        } else {
            tooltipList.add(Component.translatable("item.pipe_connector.gui.button.tooltip.utilizeExistingPipesInfoPartOne"));
            tooltipList.add(Component.translatable("item.pipe_connector.gui.button.tooltip.utilizeExistingPipesInfoPartTwo"));
        }
        return tooltipList;
    }

    private static String getInitialLabel(ItemStack itemStack) {
    if(getUtilizeExistingPipes(itemStack)) {
        return "item.pipe_connector.gui.button.on";
    } else {
        return "item.pipe_connector.gui.button.off";
        }
    }
}