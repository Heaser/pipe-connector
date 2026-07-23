package com.heaser.pipeconnector.client.gui.labels;

import com.heaser.pipeconnector.client.gui.PipeCostCalculator;
import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import com.heaser.pipeconnector.utils.GeneralUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PipeCostStatusText implements ILabelable {

    @Override
    public Component getLabel(ItemStack itemStack) {
        var player = Minecraft.getInstance().player;
        if (player == null || !PipeCostCalculator.hasSelection(itemStack) || !GeneralUtils.isPlaceableBlock(player)) {
            return Component.empty();
        }
        PipeCostCalculator.PipeCost cost = PipeCostCalculator.get(player);
        if (!cost.hasEnough()) {
            Component count = Component.literal(String.valueOf(cost.missing())).withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("item.pipe_connector.gui.label.missingPipes", count);
        }
        if (cost.reused() > 0) {
            // same color as render path for used pipes
            Component count = Component.literal(String.valueOf(cost.reused())).withStyle(ChatFormatting.DARK_PURPLE);
            return Component.translatable("item.pipe_connector.gui.label.reusedPipes", count);
        }
        return Component.empty();
    }
}
