package com.heaser.pipeconnector.client.outline;

import com.heaser.pipeconnector.PipeConnector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.world.item.Items.DIAMOND_BLOCK;

@OnlyIn(Dist.CLIENT)
public class PreviewDrawer {
    public Map <BlockPos, BlockState> previewMap = new HashMap<>();

    // Testing location
    public BlockPos testPos = new BlockPos(230, 67, -244);
    public PreviewDrawer() {
        previewMap.putIfAbsent(testPos, Minecraft.getInstance().level.getBlockState(testPos));
    }



    public void draw(PoseStack pose, MultiBufferSource buffer, double partialTicks, Player player) {
        // pose.pushPose();
        VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
        double d0 = player.xOld + (player.getX() - player.xOld) * partialTicks;
        double d1 = player.yOld + player.getEyeHeight() + (player.getY() - player.yOld) * partialTicks;
        double d2 = player.zOld + (player.getZ() - player.zOld) * partialTicks;

        for (Map.Entry<BlockPos, BlockState> blockPair : previewMap.entrySet()) {
            // PipeConnector.LOGGER.debug("I drew a block, I think");
            AABB aabb = new AABB(blockPair.getKey()).move(-d0, -d1, -d2);
            LevelRenderer.renderLineBox(pose, builder, aabb, 255, 255, 255, 0.5F);
        }
        // pose.popPose();
    }
}
