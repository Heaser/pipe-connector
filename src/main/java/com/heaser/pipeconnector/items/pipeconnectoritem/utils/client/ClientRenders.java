package com.heaser.pipeconnector.items.pipeconnectoritem.utils.client;

import com.heaser.pipeconnector.items.pipeconnectoritem.utils.PipeConnectorUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Set;


public class ClientRenders {




    public void renderInGame(PoseStack stack) {

    }


    public static Set<BlockPos> getPipePos(BlockPos start, BlockPos end, int depth, Level level) {
        return PipeConnectorUtils.getBlockPosSet(start, end, depth, level);
    }
}


