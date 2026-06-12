package com.heaser.pipeconnector.client.gui;

import com.heaser.pipeconnector.client.ClientSetup;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockGetter;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.PreviewInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public final class PipeCostCalculator {

    public record PipeCost(int required, int reused, int available, boolean creative) {
        public int missing() {
            return creative ? 0 : required - available;
        }

        public boolean hasEnough() {
            return missing() <= 0;
        }
    }

    private static PipeCost cached = new PipeCost(0, 0, 0, false);
    private static int lastTick = -1;

    private PipeCostCalculator() {
    }

    public static PipeCost get(Player player) {
        if (player.tickCount != lastTick) {
            lastTick = player.tickCount;
            cached = compute(player);
        }
        return cached;
    }

    private static PipeCost compute(Player player) {
        var previewMap = ClientSetup.PREVIEW_DRAWER.previewMap;
        Block block = CompatibilityBlockGetter.getInstance().getBlock(player.getOffhandItem());
        int reused = 0;
        for (PreviewInfo previewInfo : previewMap) {
            if (CompatibilityBlockEqualsChecker.isPlacementAlreadySatisfied(previewInfo.pos, block, player.getOffhandItem(), player.level())) {
                reused++;
            }
        }
        int available = PipeConnectorUtils.getNumberOfPipesInInventory(player);
        return new PipeCost(previewMap.size() - reused, reused, available, player.getAbilities().instabuild);
    }
}
