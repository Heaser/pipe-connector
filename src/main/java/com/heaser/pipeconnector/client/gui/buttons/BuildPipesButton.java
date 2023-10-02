package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.client.ClientSetup;
import com.heaser.pipeconnector.network.BuildPipesPacket;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BuildPipesButton extends BaseButton {
    private final LocalPlayer player;
    public BuildPipesButton(LocalPlayer player) {
        super("item.pipe_connector.gui.button.PlacePipes");
        this.player = player;
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
        return getErrorMessage(itemStack) == null;
    }

    @Override
    public Component getTooltip(ItemStack itemStack) {
        Component errorMessage = getErrorMessage(itemStack);
        return errorMessage;
    }

    private Component getErrorMessage(ItemStack itemStack) {
        BlockPos blockPos = PipeConnectorUtils.getEndPosition(itemStack);
        if (blockPos == null) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledPlacePipes");
        } else if (!GeneralUtils.isPlaceableBlock(player)) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledButtonHoldValidItem");
        }
        int existingPipes = PipeConnectorUtils.getNumberOfPipesInInventory(player);
        int neededPipes = ClientSetup.PREVIEW_DRAWER.previewMap.size();
        int missingPipes = neededPipes - existingPipes;
        if (!PipeConnectorUtils.hasEnoughPipesInInventory(existingPipes, neededPipes)) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledNotEnoughPipes", missingPipes);
        }
        return null;
    }
}
