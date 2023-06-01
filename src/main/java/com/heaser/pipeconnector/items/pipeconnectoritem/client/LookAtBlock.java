package com.heaser.pipeconnector.items.pipeconnectoritem.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

public class LookAtBlock {
    public final BlockHitResult rayTraceResult;

    public LookAtBlock(BlockHitResult rayTraceResult) {
        this.rayTraceResult = rayTraceResult;
    }

    public static BlockPos LookingAtBlock(BlockHitResult rayTraceResult) {
        return rayTraceResult.getBlockPos();
    }

    public static Direction LookingAtBlockWithDirection(BlockHitResult rayTraceResult) {
        return rayTraceResult.getDirection();
    }

}

