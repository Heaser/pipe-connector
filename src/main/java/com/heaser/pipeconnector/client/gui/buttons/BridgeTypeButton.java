package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.UpdateBridgeTypePacket;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class BridgeTypeButton extends BaseButton {
    private static final int MANHATTAN_WIDTH = 123;
    private static final int ASTAR_WIDTH = 123;

    public BridgeTypeButton(ItemStack itemStack) {
        super(getInitialLabel(itemStack), 20, getInitialWidth(itemStack));
    }


    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        BridgeType bridgeType = TagUtils.getBridgeType(itemStack);

        if(bridgeType == BridgeType.DEFAULT) {
            bridgeType = BridgeType.A_STAR;
            this.setLabel("item.pipe_connector.gui.button.aStarPathfinding", null);
            this.button.setMessage(this.getLabel());
            this.setButtonWidth(ASTAR_WIDTH);
        } else if(bridgeType == BridgeType.A_STAR) {
            bridgeType = BridgeType.DEFAULT;
            this.setLabel("item.pipe_connector.gui.button.defaultPathfinding", null);
            this.button.setMessage(this.getLabel());
            this.setButtonWidth(MANHATTAN_WIDTH);
        }

        TagUtils.setBridgeType(itemStack, bridgeType);
        PacketDistributor.sendToServer(new UpdateBridgeTypePacket(bridgeType.toString()));
    }


    private static Component getInitialLabel(ItemStack itemStack) {
        BridgeType bridgeType = TagUtils.getBridgeType(itemStack);
        return switch (bridgeType) {
            case A_STAR -> Component.translatable("item.pipe_connector.gui.button.aStarPathfinding");
            default -> Component.translatable("item.pipe_connector.gui.button.defaultPathfinding");
        };
    }

    private static int getInitialWidth(ItemStack itemStack) {
        BridgeType bridgeType = TagUtils.getBridgeType(itemStack);
        return bridgeType == BridgeType.A_STAR ? ASTAR_WIDTH : MANHATTAN_WIDTH;
    }

    public void updateLabel(ItemStack itemStack) {
        BridgeType bridgeType = TagUtils.getBridgeType(itemStack);
        if (bridgeType == BridgeType.A_STAR) {
            this.setLabel("item.pipe_connector.gui.button.aStarPathfinding", null);
            this.setButtonWidth(ASTAR_WIDTH);
        } else {
            this.setLabel("item.pipe_connector.gui.button.defaultPathfinding", null);
            this.setButtonWidth(MANHATTAN_WIDTH);
        }
        this.button.setMessage(this.getLabel());
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



