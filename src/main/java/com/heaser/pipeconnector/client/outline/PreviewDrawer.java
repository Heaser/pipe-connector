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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        java.util.Set<net.minecraft.core.BlockPos> nodeSet = new java.util.HashSet<>(nodePositions);

        // Precompute lookup for connectivity
        java.util.Set<BlockPos> previewPositions = new java.util.HashSet<>();
        for (PreviewInfo pi : previewMap) previewPositions.add(pi.pos);

        // Pass 1: draw pipe-like segments (center joint + connectors to neighbors)
        for (PreviewInfo previewInfo : previewMap) {
            BlockPos pos = previewInfo.pos;
            float[] color = getColorForPos(player, pos);
            int nodeIdx = nodePositions.indexOf(pos);
            boolean isNode = nodeIdx >= 0;
            float[] nodeColor = null;
            if (isNode) {
                // Start cyan, end orange, intermediates use palette
                if (nodeIdx == 0) {
                    nodeColor = new float[]{0f, 1f, 1f, 1f}; // cyan
                } else if (nodeIdx == nodePositions.size() - 1) {
                    nodeColor = new float[]{1f, 0.65f, 0f, 1f}; // orange
                } else {
                    nodeColor = argbToRgba(getIndexColor(nodeIdx));
                }
            }
            drawPipePiece(pose, buffer, pos, offset, previewPositions, color, isNode, nodeColor);
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

    private float[] getColorForPos(Player player, BlockPos pos) {
        // Returns RGBA floats for this preview position, matching previous color semantics
        if (isNotBreakable(player.level(), pos) || hasInventoryCapabilities(player.level(), pos)) {
            return new float[]{1f, 0f, 0f, 1f}; // red
        } else if (isVoidableBlock(player.level(), pos)) {
            return new float[]{1f, 1f, 0f, 1f}; // yellow
        } else if (CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(pos,
                CompatibilityBlockGetter.getInstance().getBlock(player.getOffhandItem()),
                player.getOffhandItem(), player.level())) {
            return new float[]{0.87f, 0.25f, 0.87f, 0.5f}; // purple-ish
        }
        return new float[]{0f, 1f, 0f, 0.5f}; // green
    }

    private static final double PIPE_HALF = 0.075; // thickness for regular segments (overall 0.15)
    private static final double NODE_HALF = 0.20;  // thicker joint for nodes (overall 0.40)

    private void drawPipePiece(PoseStack pose, MultiBufferSource buffer, BlockPos pos, Vec3 offset,
                                java.util.Set<BlockPos> previewPositions, float[] rgba, boolean isNode, float[] nodeRgba) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        double half = isNode ? NODE_HALF : PIPE_HALF; // larger joint for nodes
        float[] drawColor = (isNode && nodeRgba != null) ? nodeRgba : rgba;

        // Center joint
        drawThinBox(pose, buffer,
                cx - half - offset.x, cy - half - offset.y, cz - half - offset.z,
                cx + half - offset.x, cy + half - offset.y, cz + half - offset.z,
                drawColor);

        // Connectors
        // West (x-)
        if (previewPositions.contains(pos.relative(Direction.WEST))) {
            drawThinBox(pose, buffer,
                    pos.getX() - offset.x, cy - half - offset.y, cz - half - offset.z,
                    cx - half - offset.x, cy + half - offset.y, cz + half - offset.z,
                    drawColor);
        }
        // East (x+)
        if (previewPositions.contains(pos.relative(Direction.EAST))) {
            drawThinBox(pose, buffer,
                    cx + half - offset.x, cy - half - offset.y, cz - half - offset.z,
                    pos.getX() + 1.0 - offset.x, cy + half - offset.y, cz + half - offset.z,
                    drawColor);
        }
        // North (z-)
        if (previewPositions.contains(pos.relative(Direction.NORTH))) {
            drawThinBox(pose, buffer,
                    cx - half - offset.x, cy - half - offset.y, pos.getZ() - offset.z,
                    cx + half - offset.x, cy + half - offset.y, cz - half - offset.z,
                    drawColor);
        }
        // South (z+)
        if (previewPositions.contains(pos.relative(Direction.SOUTH))) {
            drawThinBox(pose, buffer,
                    cx - half - offset.x, cy - half - offset.y, cz + half - offset.z,
                    cx + half - offset.x, cy + half - offset.y, pos.getZ() + 1.0 - offset.z,
                    drawColor);
        }
        // Down (y-)
        if (previewPositions.contains(pos.relative(Direction.DOWN))) {
            drawThinBox(pose, buffer,
                    cx - half - offset.x, pos.getY() - offset.y, cz - half - offset.z,
                    cx + half - offset.x, cy - half - offset.y, cz + half - offset.z,
                    drawColor);
        }
        // Up (y+)
        if (previewPositions.contains(pos.relative(Direction.UP))) {
            drawThinBox(pose, buffer,
                    cx - half - offset.x, cy + half - offset.y, cz - half - offset.z,
                    cx + half - offset.x, pos.getY() + 1.0 - offset.y, cz + half - offset.z,
                    drawColor);
        }

        // No large full-block outline; nodes are emphasized by thicker center and node-specific color
    }

    private void drawThinBox(PoseStack pose, MultiBufferSource buffer,
                              double x0, double y0, double z0,
                              double x1, double y1, double z1,
                              float[] rgba) {
        VertexConsumer builder = buffer.getBuffer(PipeConnectorRenderType.LINES_NO_DEPTH_TEST);
        AABB aabb = new AABB(
                Math.min(x0, x1), Math.min(y0, y1), Math.min(z0, z1),
                Math.max(x0, x1), Math.max(y0, y1), Math.max(z0, z1)
        );
        LevelRenderer.renderLineBox(pose, builder, aabb, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    private float[] argbToRgba(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        return new float[]{r, g, b, Math.max(a, 1.0f)}; // ensure fully opaque for node emphasis
    }

    private int getIndexColor(int zeroBasedIndex) {
        // Intermediate node palette (avoid reserved: Start=Cyan, End=Orange)
        int[] palette = new int[] {
                0xFFFFFFFF, // White
                0xFFFF00FF, // Magenta
                0xFFFFFF00, // Yellow
                0xFF7FFF00, // Chartreuse
                0xFF00FF7F, // Spring Green
                0xFF7B68EE, // Slate Blue
                0xFFFF69B4, // Hot Pink
                0xFFADFF2F  // Green Yellow
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
