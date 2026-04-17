package com.heaser.pipeconnector.compatibility.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;


public interface IMultiPipeColorProvider {
    List<PipeRenderEntry> getPipeEntries(Level level, BlockPos pos);
}
