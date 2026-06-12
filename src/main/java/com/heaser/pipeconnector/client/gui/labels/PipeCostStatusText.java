package com.heaser.pipeconnector.client.gui.labels;

import com.heaser.pipeconnector.client.gui.PipeCostCalculator;
import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PipeCostStatusText implements ILabelable {

    @Override
    public Component getLabel(ItemStack itemStack) {
        var player = Minecraft.getInstance().player;
        if (player == null || TagUtils.getNodesFromStack(itemStack).size() < 2 || !GeneralUtils.isPlaceableBlock(player)) {
            return Component.empty();
        }
        PipeCostCalculator.PipeCost cost = PipeCostCalculator.get(player);
        if (!cost.hasEnough()) {
            return Component.translatable("item.pipe_connector.gui.label.missingPipes", cost.missing())
                    .withStyle(ChatFormatting.DARK_RED);
        }
        if (cost.reused() > 0) {
            // Matches the magenta already-placed color used by the world preview
            return Component.translatable("item.pipe_connector.gui.label.reusedPipes", cost.reused())
                    .withStyle(ChatFormatting.DARK_PURPLE);
        }
        return Component.empty();
    }
}
