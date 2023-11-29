package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.constants.BridgeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.heaser.pipeconnector.utils.PipeConnectorUtils.getBridgeType;
import static net.minecraft.world.level.material.MapColor.COLOR_RED;

public class BridgeTypeInfoButton extends InfoButton {

    public BridgeTypeInfoButton() {
        super();
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {}

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public List<Component> getTooltipList(ItemStack itemStack) {
        List<Component> tooltipList = new ArrayList<>();
        tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.Pathfinding"));

        if(getBridgeType(itemStack) == BridgeType.DEFAULT) {
            tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.defaultPathfindingImportantInfo").withStyle(ChatFormatting.YELLOW));
            return tooltipList;
        } else if(getBridgeType(itemStack) == BridgeType.A_STAR) {
            tooltipList.add(Component.translatable("item.pipe_connector.gui.tooltip.aStarPathfindingImportantInfo").withStyle(ChatFormatting.YELLOW));
            return tooltipList;
        }
        return null;
    }
}