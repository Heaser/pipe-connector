package com.heaser.pipeconnector.compatibility.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IColorProvider {
    /**
     * Returns the color (ARGB or RGB) for the block at the given position.
     * Return null to indicate no specific color (or use default).
     */
    @Nullable
    Integer getColor(Level level, BlockPos pos);
}
