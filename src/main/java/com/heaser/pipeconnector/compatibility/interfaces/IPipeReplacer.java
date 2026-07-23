package com.heaser.pipeconnector.compatibility.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IPipeReplacer {
    @Nullable
    Object resolveKind(Level level, BlockPos pos, ItemStack offhandStack);

    @Nullable
    default Component getSeedRejectionReason(Level level, BlockPos pos, ItemStack offhandStack) {
        return null;
    }

    boolean matchesKind(Level level, BlockPos pos, Object kind);

    String describeKind(Object kind);

    default ItemStack getKindDisplayStack(Level level, BlockPos seedPos, Object kind) {
        return ItemStack.EMPTY;
    }

    boolean isSameFamily(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack);

    boolean isAlreadyTarget(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack);

    @Nullable
    default Component getReplaceBlockedReason(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack) {
        return null;
    }

    boolean replaceAt(Level level, BlockPos pos, Player player, ItemStack offhandStack, Object kind, List<Direction> adjacentInRun);
}
