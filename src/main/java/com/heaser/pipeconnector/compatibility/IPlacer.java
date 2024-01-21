package com.heaser.pipeconnector.compatibility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public interface IPlacer {
    boolean place(Level level, BlockPos pos, Player player, Item item);
}
