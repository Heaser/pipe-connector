package com.heaser.pipeconnector.items.pipeconnectoritem.utils;


import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class PipeConnectorUtils {

    private static final Logger LOGGER = LogUtils.getLogger();


    public static void connectPathWithSegments(Level level, BlockPos start, BlockPos end, int depth) {

        Set<BlockPos> blockPosSet = getBlockPosSet(start, end, depth);

        LOGGER.info(blockPosSet.toString());
        blockPosSet.forEach((blockPos -> breakAndSetBlock(level, blockPos)));

    }

    // -----------------------------------------------------------------------------------------------------------------

    public static BlockPos getNeighborInFacingDirection(BlockPos pos, Direction facing) {
        return pos.relative(facing);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static Set<BlockPos> getBlockPosSet(BlockPos start, BlockPos end, int depth) {
          Set<BlockPos> blockPosList = new HashSet<>();


        int deltaY = (start.getY() > end.getY()) ? Math.abs((start.getY() - end.getY())) : Math.abs(end.getY() - start.getY());
        int startDepth = depth, endDepth = depth;

        if(start.getY() > end.getY()) {
            endDepth -= deltaY;
        } else {
            startDepth -= deltaY;
        }

        for(int i = 0; i < startDepth; i++) {
            blockPosList.add(start);
            start = start.below();
        }

        for (int i = 0; i < endDepth; i++) {
            blockPosList.add(end);
            end = end.below();
        }

        // Create bridge
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int dz = end.getZ() - start.getZ();

        int xSteps = Math.abs(dx);
        int ySteps = Math.abs(dy);
        int zSteps = Math.abs(dz);

        int xDirection = dx > 0 ? 1 : -1;
        int yDirection = dy > 0 ? 1 : -1;
        int zDirection = dz > 0 ? 1 : -1;

        BlockPos currentPos = start.above();

        //         Move along the Y-axis
        for (int i = 0; i < ySteps; i++) {
            blockPosList.add(currentPos);
            currentPos = currentPos.offset(0, yDirection, 0);
        }

        // Move along the X-axis
        for (int i = 0; i < xSteps; i++) {
            blockPosList.add(currentPos);
            currentPos = currentPos.offset(xDirection, 0, 0);
        }

        // Move along the Z-axis
        for (int i = 0; i < zSteps; i++) {
            blockPosList.add(currentPos);
            currentPos = currentPos.offset(0, 0, zDirection);
        }

        return blockPosList;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static void breakAndSetBlock(Level level, BlockPos pos) {
        if (isBreakable(level, pos)) {
            level.destroyBlock(pos,true);
            level.setBlockAndUpdate(pos, Blocks.GLOWSTONE.defaultBlockState());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static boolean isBreakable(Level level, BlockPos pos) {
        BlockState blockPos = level.getBlockState(pos);
        float hardness = blockPos.getDestroySpeed(level, pos);
        return hardness != -1;
    }






}
