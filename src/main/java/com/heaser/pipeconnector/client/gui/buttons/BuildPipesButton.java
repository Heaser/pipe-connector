package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.client.ClientSetup;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.network.BuildPipesPacket;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.PreviewInfo;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.heaser.pipeconnector.utils.GeneralUtils.*;
import static com.heaser.pipeconnector.utils.PipeConnectorUtils.getPreventInventoryBlockBreaking;

public class BuildPipesButton extends BaseButton {
    private final LocalPlayer player;

    public BuildPipesButton(LocalPlayer player) {
        super("item.pipe_connector.gui.button.PlacePipes", 20, 80);
        this.player = player;
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
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
        return getErrorMessage(itemStack);
    }

    private Component getErrorMessage(ItemStack itemStack) {
        String blockName;
        BlockPos blockPos = PipeConnectorUtils.getEndPosition(itemStack);
        int existingPipes = PipeConnectorUtils.getNumberOfPipesInInventory(player);
        int missingPipes = PipeConnectorUtils.getMissingPipesInInventory(player, existingPipes, player.level(), ClientSetup.PREVIEW_DRAWER.previewMap,
                Block.byItem(player.getOffhandItem().getItem()));
        int maxAllowedPipes = PipeConnectorConfig.MAX_ALLOWED_PIPES_TO_PLACE.get();
        Optional<BlockPos> inventoryBlockPos = pathContainsInventory();
        Optional<BlockPos> unbreakableBlockPos = pathContainsUnbreakableBlocks();

        if (blockPos == null) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledPlacePipes");
        } else if (!GeneralUtils.isPlaceableBlock(player)) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledButtonHoldValidItem");
        } else if (inventoryBlockPos.isPresent() && getPreventInventoryBlockBreaking(itemStack)) {
            blockName = player.level().getBlockState(inventoryBlockPos.get()).getBlock().getName().getString();
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledInventoryInPath", blockName);
        } else if (unbreakableBlockPos.isPresent()) {
            blockName = player.level().getBlockState(unbreakableBlockPos.get()).getBlock().getName().getString();
            return Component.translatable("item.pipe_connector.message.unbreakableBlockReached", blockName);
        }


        if (missingPipes > 0) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledNotEnoughPipes", missingPipes);
        } else if (ClientSetup.PREVIEW_DRAWER.previewMap.size() >= maxAllowedPipes) {
            return Component.translatable("item.pipe_connector.gui.button.toolTip.maxAllowedPipesReached", maxAllowedPipes);
        }
        return null;
    }

    private Optional<BlockPos> pathContainsInventory() {
        return ClientSetup.PREVIEW_DRAWER.previewMap.stream()
                .filter(block -> hasInventoryCapabilities(player.level(), block.pos))
                .map(block -> block.pos)
                .findFirst();
    }

    private Optional<BlockPos> pathContainsUnbreakableBlocks() {
        return ClientSetup.PREVIEW_DRAWER.previewMap.stream()
                .filter(block -> isNotBreakable(player.level(), block.pos))
                .map(block -> block.pos)
                .findFirst();
}
}

