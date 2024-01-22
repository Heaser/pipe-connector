package com.heaser.pipeconnector.client.outline;

import com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockGetter;
import com.heaser.pipeconnector.utils.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;

import static com.heaser.pipeconnector.utils.GeneralUtils.*;

public class PreviewDrawer {
    public HashSet<PreviewInfo> previewMap = new HashSet<>();
    private BuildParameters cachedParameters = new BuildParameters();

    public PreviewDrawer() {
    }

    public void handleOnRenderLevel(PoseStack pose, MultiBufferSource buffer, Player player) {
        ItemStack heldItem = GeneralUtils.heldPipeConnector(player);
        if (heldItem == null) {
            return;
        }
        else if(shouldUpdatePreview(heldItem)) {
            Level currentLevel = player.level();
            previewMap = getNewPreview(heldItem, currentLevel, player);
        }

        draw(pose, buffer, player, heldItem);
    }

    private HashSet<PreviewInfo> getNewPreview(ItemStack pipeConnector, Level currentLevel, Player player) {
        BlockPos startPos = TagUtils.getStartPosition(pipeConnector);
        BlockPos endPos = TagUtils.getEndPosition(pipeConnector);
        if (startPos == null || endPos == null) {
            return new HashSet<>();
        }
        int depth = TagUtils.getDepthFromStack(pipeConnector);
        boolean utilizeExitingPipes = TagUtils.getUtilizeExistingPipes(pipeConnector);
        Direction startDirection = TagUtils.getStartDirection(pipeConnector);
        Direction endDirection = TagUtils.getEndDirection(pipeConnector);
        startPos = startPos.relative(startDirection);
        endPos = endPos.relative(endDirection);
        return PipeConnectorUtils.getBlockPosSet(
                PipeConnectorUtils.getBlockPosMap(
                        startPos,
                        endPos,
                        depth,
                        currentLevel,
                        TagUtils.getBridgeType(pipeConnector),
                        CompatibilityBlockGetter.getInstance().getBlock(player.getOffhandItem()),
                        player.getOffhandItem(),
                        utilizeExitingPipes));
    }

    private void draw(PoseStack pose, MultiBufferSource buffer, Player player, ItemStack pipeConnector) {
        VertexConsumer builder = buffer.getBuffer(PipeConnectorRenderType.LINES_NO_DEPTH_TEST);
        Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        for (PreviewInfo previewInfo : previewMap) {
            AABB aabb = new AABB(previewInfo.pos).move(-offset.x, -offset.y, -offset.z);

            if(previewInfo.isRelativeStartPos(pipeConnector)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 0.5F, 0.7F, 0.9F, 1F);
            } else if(previewInfo.isRelativeEndPos(pipeConnector)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 1F, 0.5F, 0.3F, 1F);
            } else if (isNotBreakable(player.level(), previewInfo.pos) || hasInventoryCapabilities(player.level(), previewInfo.pos)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 1F, 0, 0, 1F);
            } else if (isVoidableBlock(player.level(), previewInfo.pos)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 1F, 1F, 0, 1F);
            } else if (CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(previewInfo.pos,
                    CompatibilityBlockGetter.getInstance().getBlock(player.getOffhandItem()),
                    player.getOffhandItem(), player.level())) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 0.87F, 0.25F, 0.87F, 0.5F);
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
