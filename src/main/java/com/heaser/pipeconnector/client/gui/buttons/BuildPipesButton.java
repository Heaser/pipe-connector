package com.heaser.pipeconnector.client.gui.buttons;

import com.heaser.pipeconnector.client.ClientSetup;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockGetter;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.network.BuildPipesPacket;
import com.heaser.pipeconnector.utils.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.Optional;

import static com.heaser.pipeconnector.utils.GeneralUtils.*;

public class BuildPipesButton extends BaseButton {
    private final LocalPlayer player;

    public BuildPipesButton(LocalPlayer player) {
        super(Component.translatable("item.pipe_connector.gui.button.PlacePipes"), 20, 95);
        this.player = player;
    }

    @Override
    public void onClick(Button clickedButton, ItemStack itemStack) {
        ClientPacketDistributor.sendToServer(new BuildPipesPacket());
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
        List<NodeParameter> nodes = TagUtils.getNodesFromStack(itemStack);
        int existingPipes = PipeConnectorUtils.getNumberOfPipesInInventory(player);
        int missingPipes = PipeConnectorUtils.getMissingPipesInInventory(player, existingPipes, player.level(), ClientSetup.PREVIEW_DRAWER.previewMap,
                CompatibilityBlockGetter.getInstance().getBlock(player.getOffhandItem()));
        int maxAllowedPipes = PipeConnectorConfig.MAX_ALLOWED_PIPES_TO_PLACE.get();
        Optional<BlockPos> inventoryBlockPos = pathContainsInventory();
        Optional<BlockPos> unbreakableBlockPos = pathContainsUnbreakableBlocks();

        if (nodes.size() < 2) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledPlacePipes");
        } else if (!GeneralUtils.isPlaceableBlock(player)) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledButtonHoldValidItem");
        } else if (inventoryBlockPos.isPresent() && TagUtils.getPreventInventoryBlockBreaking(itemStack)) {
            blockName = player.level().getBlockState(inventoryBlockPos.get()).getBlock().getName().getString();
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledInventoryInPath", blockName);
        } else if (unbreakableBlockPos.isPresent()) {
            blockName = player.level().getBlockState(unbreakableBlockPos.get()).getBlock().getName().getString();
            return Component.translatable("item.pipe_connector.message.unbreakableBlockReached", blockName);
        }


        // If using A*, ensure a real path exists (not just isolated nodes)
        if (TagUtils.getBridgeType(itemStack) == BridgeType.A_STAR) {
            var nodePositions = nodes.stream().map(NodeParameter::getRelativePosition).collect(java.util.stream.Collectors.toSet());
            boolean anyNonNodePositions = ClientSetup.PREVIEW_DRAWER.previewMap.stream()
                    .anyMatch(preview -> !nodePositions.contains(preview.pos));
            if (!anyNonNodePositions) {
                return Component.translatable("item.pipe_connector.gui.button.tooltip.noPathFound");
            }
        }

        if (ClientSetup.PREVIEW_DRAWER.previewMap.size() >= maxAllowedPipes) {
            return Component.translatable("item.pipe_connector.gui.button.toolTip.maxAllowedPipesReached", maxAllowedPipes);
        } else if (missingPipes > 0) {
            return Component.translatable("item.pipe_connector.gui.button.tooltip.disabledNotEnoughPipes", missingPipes);
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

