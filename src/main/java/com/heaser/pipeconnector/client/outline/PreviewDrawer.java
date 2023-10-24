package com.heaser.pipeconnector.client.outline;

import com.heaser.pipeconnector.utils.BuildParameters;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.PreviewInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;

public class PreviewDrawer {
    public HashSet<PreviewInfo> previewMap = new HashSet<>();
    private BuildParameters cachedParameters = new BuildParameters();

    public PreviewDrawer() {
    }

    public void handleOnRenderLevel(PoseStack pose, MultiBufferSource buffer, double partialTicks, Player player) {
        ItemStack heldItem = GeneralUtils.heldPipeConnector(player);
        if (heldItem == null) {
            return;
        }
        else if(shouldUpdatePreview(heldItem)) {
            Level currentLevel = player.getLevel();
            previewMap = getNewPreview(heldItem, currentLevel);
        }

        draw(pose, buffer, partialTicks, player, heldItem);
    }

    private HashSet<PreviewInfo> getNewPreview(ItemStack pipeConnector, Level currentLevel) {
        BlockPos startPos = PipeConnectorUtils.getStartPosition(pipeConnector);
        BlockPos endPos = PipeConnectorUtils.getEndPosition(pipeConnector);
        if (startPos == null || endPos == null) {
            return new HashSet<>();
        }
        int depth = PipeConnectorUtils.getDepthFromStack(pipeConnector);
        Direction startDirection = PipeConnectorUtils.getStartDirection(pipeConnector);
        Direction endDirection = PipeConnectorUtils.getEndDirection(pipeConnector);
        startPos = startPos.relative(startDirection);
        endPos = endPos.relative(endDirection);
        return PipeConnectorUtils.getBlockPosSet(
                PipeConnectorUtils.getBlockPosMap(startPos, endPos, depth, currentLevel)
        );
    }

    private void draw(PoseStack pose, MultiBufferSource buffer, double partialTicks, Player player, ItemStack pipeConnector) {
        VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
        double d0 = player.xOld + (player.getX() - player.xOld) * partialTicks;
        double d1 = player.yOld + player.getEyeHeight() + (player.getY() - player.yOld) * partialTicks;
        double d2 = player.zOld + (player.getZ() - player.zOld) * partialTicks;

        for (PreviewInfo previewInfo : previewMap) {
            AABB aabb = new AABB(previewInfo.pos).move(-d0, -d1, -d2);

            if(previewInfo.isRelativeStartPos(pipeConnector)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 0.5F, 0.7F, 0.9F, 1F);
            } else if(previewInfo.isRelativeEndPos(pipeConnector)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 1F, 0.5F, 0.3F, 1F);
            } else if (GeneralUtils.isNotBreakable(player.getLevel(), previewInfo.pos)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 1F, 0, 0, 1F);
            } else if (GeneralUtils.isVoidableBlock(player.getLevel(), previewInfo.pos)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 1F, 1F, 0, 1F);
            } else {
                LevelRenderer.renderLineBox(pose, builder, aabb, 0, 1F, 0, 0.5F);
            }
        }
    }

    private boolean shouldUpdatePreview(ItemStack pipeConnector) {
        boolean shouldUpdate = false;
        BuildParameters newParameters = new BuildParameters(pipeConnector);
        if (!cachedParameters.equals(newParameters)) {
            shouldUpdate = true;
            cachedParameters = newParameters;
        }
        return shouldUpdate;
    }
}
