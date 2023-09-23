package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.network.BuildPipesPacket;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BuildPipesButton extends BaseButton {
    public BuildPipesButton() {
        super("item.pipe_connector.gui.button.PlacePipes");
    }

    @Override
    public void onClick(Button clickedButton) {
        NetworkHandler.CHANNEL.sendToServer(new BuildPipesPacket());

    }

    @Override
    public boolean shouldClose() {
        return true;
    }

    @Override
    public boolean isActive(ItemStack itemStack) {
        BlockPos blockPos = PipeConnectorUtils.getEndPosition(itemStack);
        return blockPos != null;
    }

    @Override
    public Component getTooltip(ItemStack itemStack) {
        if (!isActive(itemStack)) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledPlacePipes");
        }
        return null;
    }
}
