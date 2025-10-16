package com.heaser.pipeconnector.client.outline;

import com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.CompatibilityBlockGetter;
import com.heaser.pipeconnector.utils.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;

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
        } else if (shouldUpdatePreview(heldItem)) {
            Level currentLevel = player.level();
            previewMap = getNewPreview(heldItem, currentLevel, player);
        }

        draw(pose, buffer, player, heldItem);
    }

    private HashSet<PreviewInfo> getNewPreview(ItemStack pipeConnector, Level currentLevel, Player player) {
        List<NodeParameter> nodes = TagUtils.getNodesFromStack(pipeConnector);

        // Don't show preview if at least two positions have not been set
        if (nodes.size() < 2) {
            return new HashSet<>();
        }
        int depth = TagUtils.getDepthFromStack(pipeConnector);
        boolean utilizeExitingPipes = TagUtils.getUtilizeExistingPipes(pipeConnector);
        return PipeConnectorUtils.getBlockPosSet(
                PipeConnectorUtils.getBlockPosMap(
                        nodes,
                        depth,
                        currentLevel,
                        TagUtils.getBridgeType(pipeConnector),
                        CompatibilityBlockGetter.getInstance().getBlock(player.getOffhandItem()),
                        player.getOffhandItem(),
                        utilizeExitingPipes,
                        player));
    }

    private void draw(PoseStack pose, MultiBufferSource buffer, Player player, ItemStack pipeConnector) {
        Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        // Prepare node index lookup (1-based indexing in preview)
        java.util.List<NodeParameter> nodes = TagUtils.getNodesFromStack(pipeConnector);
        java.util.List<net.minecraft.core.BlockPos> nodePositions = nodes.stream().map(NodeParameter::getRelativePosition).toList();

        // Pass 1: draw all boxes
        for (PreviewInfo previewInfo : previewMap) {
            VertexConsumer builder = buffer.getBuffer(PipeConnectorRenderType.LINES_NO_DEPTH_TEST);
            AABB aabb = new AABB(previewInfo.pos).move(-offset.x, -offset.y, -offset.z);
            if (previewInfo.isNode(pipeConnector)) {
                LevelRenderer.renderLineBox(pose, builder, aabb, 1F, 1F, 1F, 1F);
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

        // Pass 2: render node indices so text rendering does not interfere with line buffers
        for (int i = 0; i < nodePositions.size(); i++) {
            renderNodeIndexNumber(pose, buffer, nodePositions.get(i), i + 1, offset, getIndexColor(i));
        }
    }

    private void renderNodeIndexNumber(PoseStack pose, MultiBufferSource buffer, net.minecraft.core.BlockPos pos, int index, Vec3 cameraOffset, int color) {
        var mc = Minecraft.getInstance();
        Font font = mc.font;
        String label = net.minecraft.network.chat.Component.translatable("item.pipe_connector.preview.position").getString();
        String number = Integer.toString(index);

        pose.pushPose();
        // Position text at block center (middle of the preview block)
        double x = pos.getX() + 0.5 - cameraOffset.x;
        double y = pos.getY() + 0.5 - cameraOffset.y;
        double z = pos.getZ() + 0.5 - cameraOffset.z;
        pose.translate(x, y, z);

        // Make text face the camera
        pose.mulPose(mc.gameRenderer.getMainCamera().rotation());
        // Slightly smaller overall; flip Y so text isn't mirrored
        float scale = 0.035f;
        pose.scale(scale, -scale, scale);
        // Slightly offset towards the camera to avoid z-fighting with line boxes
        pose.translate(0.0, 0.0, 0.01);

        // Position label above the number, both centered horizontally
        float labelWidth = font.width(label);
        float numberWidth = font.width(number);
        int white = 0xFFFFFFFF;

        // Draw "Position" label (white)
        font.drawInBatch(label,
                -labelWidth / 2.0f,
                -font.lineHeight,
                white,
                true,
                pose.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                15728880);

        // Draw index number (colored)
        font.drawInBatch(number,
                -numberWidth / 2.0f,
                0f,
                color,
                true,
                pose.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                15728880);

        pose.popPose();
    }

    private int getIndexColor(int zeroBasedIndex) {
        int[] palette = new int[] {
                0xFFFFFFFF, // 1 - White
                0xFFFFA500, // 2 - Orange
                0xFF00FFFF, // 3 - Cyan
                0xFFFFFF00, // 4 - Yellow
                0xFFFF00FF, // 5 - Magenta
                0xFF00FF7F, // 6 - Spring Green
                0xFF7B68EE, // 7 - Slate Blue
                0xFFFF69B4  // 8 - Hot Pink
        };
        return palette[zeroBasedIndex % palette.length];
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
