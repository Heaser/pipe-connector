package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateBridgeTypePacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BridgeTypeButton extends BaseButton {

    public BridgeTypeButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, 123);
    }


    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        BridgeType bridgeType = TagUtils.getBridgeType(itemStack);

        if(bridgeType == BridgeType.DEFAULT) {
            bridgeType = BridgeType.A_STAR;
            this.setLabel("item.pipe_connector.gui.button.aStarPathfinding", null);
            this.button.setMessage(this.getLabel());
        } else if(bridgeType == BridgeType.A_STAR) {
            bridgeType = BridgeType.DEFAULT;
            this.setLabel("item.pipe_connector.gui.button.defaultPathfinding", null);
            this.button.setMessage(this.getLabel());
        }
        NetworkHandler.CHANNEL.sendToServer(new UpdateBridgeTypePacket(bridgeType));
    }


    private static Component getInitialLabel(ItemStack itemStack) {
        BridgeType bridgeType = TagUtils.getBridgeType(itemStack);
        return switch (bridgeType) {
            case A_STAR -> Component.translatable("item.pipe_connector.gui.button.aStarPathfinding");
            default -> Component.translatable("item.pipe_connector.gui.button.defaultPathfinding");
        };
    }

    @Override
    public List<Component> getTooltipList(ItemStack itemStack) {
        List<Component> tooltipList = new ArrayList<>();
        tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.Pathfinding"));

        if(TagUtils.getBridgeType(itemStack) == BridgeType.DEFAULT) {
            tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.defaultPathfindingImportantInfo").withStyle(ChatFormatting.GOLD));
            tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.performanceImpact").withStyle(ChatFormatting.GRAY));
            tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.performanceImpactLow").withStyle(ChatFormatting.YELLOW));
            return tooltipList;
        } else if(TagUtils.getBridgeType(itemStack) == BridgeType.A_STAR) {
            tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.aStarPathfindingImportantInfo").withStyle(ChatFormatting.GOLD));
            tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.performanceImpact").withStyle(ChatFormatting.GRAY));
            tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.performanceImpactMediumHigh").withStyle(ChatFormatting.YELLOW));
            return tooltipList;
        }
        return null;
    }
}



