package com.heaser.pipeconnector.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class PreviewInfo {
    public final BlockPos pos;
    public PreviewInfo(BlockPos pos) {
        this.pos = pos;
    }

    public boolean isRelativeStartPos(ItemStack pipeConnector) {
        return pos.equals(PipeConnectorUtils.getRelativeStartPosition(pipeConnector));
    }
    public boolean isRelativeEndPos(ItemStack pipeConnector) {
        return pos.equals(PipeConnectorUtils.getRelativeEndPosition(pipeConnector));
    }
}
