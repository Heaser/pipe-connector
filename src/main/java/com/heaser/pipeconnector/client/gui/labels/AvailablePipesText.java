package com.heaser.pipeconnector.client.gui.labels;

import com.heaser.pipeconnector.client.gui.PipeCostCalculator;
import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class AvailablePipesText implements ILabelable {

    @Override
    public Component getLabel(ItemStack itemStack) {
        var player = Minecraft.getInstance().player;
        if (player == null || TagUtils.getNodesFromStack(itemStack).size() < 2 || !GeneralUtils.isPlaceableBlock(player)) {
            return Component.empty();
        }
        PipeCostCalculator.PipeCost cost = PipeCostCalculator.get(player);
        String available = cost.creative() ? "∞" : String.valueOf(cost.available());
        return Component.translatable("item.pipe_connector.gui.label.availablePipes", available);
    }
}
