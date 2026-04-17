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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;

import static com.heaser.pipeconnector.utils.GeneralUtils.*;

public class PreviewDrawer {
    public HashSet<PreviewInfo> previewMap = new HashSet<>();
    private BuildParameters cachedParameters = new BuildParameters();
    private Item cachedOffhandItem = Items.AIR;

    public PreviewDrawer() {
    }

    public void handleOnRenderLevel(PoseStack pose, MultiBufferSource buffer, Player player) {
        ItemStack heldItem = GeneralUtils.heldPipeConnector(player);
        if (heldItem == null) {
            return;
        }

        Item offhandItem = player.getOffhandItem().getItem();
        boolean offhandChanged = offhandItem != cachedOffhandItem;
        cachedOffhandItem = offhandItem;

        if (!GeneralUtils.isPlaceableBlock(player)) {
            previewMap.clear();
        } else if (shouldUpdatePreview(heldItem) || offhandChanged) {
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
        boolean mirrorManhattan = TagUtils.getMirrorManhattan(pipeConnector);
        return PipeConnectorUtils.getBlockPosSet(
                PipeConnectorUtils.getBlockPosMap(
                        nodes,
                        depth,
                        currentLevel,
                        TagUtils.getBridgeType(pipeConnector),
                        CompatibilityBlockGetter.getInstance().getBlock(player.getOffhandItem()),
                        player.getOffhandItem(),
                        utilizeExitingPipes,
                        mirrorManhattan,
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

        // Animation pulse: oscillates between 0.6 and 0.95 for alpha, and 0.98 to 1.02 for scale
        long time = System.currentTimeMillis();
        float pulseAlpha = (float) (Math.sin(time / 250.0) * 0.15 + 0.75); // 0.60 .. 0.90
        float pulseScale = (float) (Math.sin(time / 250.0) * 0.02 + 1.0);  // 0.98 .. 1.02

        // Pass 1: draw pipe-like segments (center joint + connectors to neighbors)
        boolean solid = TagUtils.getSolidPreview(pipeConnector);
        for (PreviewInfo previewInfo : previewMap) {
            BlockPos pos = previewInfo.pos;
            float[] color = getColorForPos(player, pos);
            // Apply pulse to alpha
            color[3] *= pulseAlpha;

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
                // Nodes pulse a bit brighter/more opaque
                nodeColor[3] = Math.min(1.0f, nodeColor[3] * (pulseAlpha + 0.2f));
            }
            drawPipePiece(pose, buffer, pos, offset, previewPositions, color, isNode, nodeColor, solid, pulseScale);
        }

        // Pass 2: render node indices so text rendering does not interfere with line buffers
        for (int i = 0; i < nodePositions.size(); i++) {
            // Match the node AABB color semantics for contrast computation
            int nodeColorInt;
            if (i == 0) {
                nodeColorInt = 0xFF00FFFF; // cyan (start)
            } else if (i == nodePositions.size() - 1) {
                nodeColorInt = 0xFFFFA500; // orange (end)
            } else {
                nodeColorInt = getIndexColor(i); // intermediate palette color
            }
            renderNodeIndexNumber(pose, buffer, nodePositions.get(i), i + 1, offset, nodeColorInt);
        }
    }

    private void renderNodeIndexNumber(PoseStack pose, MultiBufferSource buffer, net.minecraft.core.BlockPos pos, int index, Vec3 cameraOffset, int color) {
        var mc = Minecraft.getInstance();
        Font font = mc.font;
        String label = net.minecraft.network.chat.Component.translatable("item.pipe_connector.preview.position").getString();
        String number = Integer.toString(index);

        pose.pushPose();
        // Always-visible label: place near block center with slight upward offset; text is rendered see-through
        double x = pos.getX() + 0.5 - cameraOffset.x;
        double y = pos.getY() + 0.75 - cameraOffset.y; // small lift above center
        double z = pos.getZ() + 0.5 - cameraOffset.z;
        pose.translate(x, y, z);

        // Make text face the camera
        pose.mulPose(mc.gameRenderer.getMainCamera().rotation());
        // Slightly larger for readability; flip Y so text isn't mirrored
        float scale = 0.040f;
        pose.scale(scale, -scale, scale);
        // Slightly offset towards the camera to avoid z-fighting with line boxes
        pose.translate(0.0, 0.0, 0.01);

        // Position label above the number, both centered horizontally
        float labelWidth = font.width(label);
        float numberWidth = font.width(number);
        int white = 0xFFFFFFFF;

        // Draw label shadow behind text (depth-tested) then main glyph see-through in front
        pose.pushPose();
        pose.translate(0.0, 0.0, -0.001); // push slightly away so shadow never overdraws the text
        font.drawInBatch(label,
                -labelWidth / 2.0f,
                -font.lineHeight,
                0xFF000000,
                true,
                pose.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                15728880);
        pose.popPose();

        font.drawInBatch(label,
                -labelWidth / 2.0f,
                -font.lineHeight,
                white,
                false,
                pose.last().pose(),
                buffer,
                Font.DisplayMode.SEE_THROUGH,
                0,
                15728880);

        int textColor = 0xFFFFFFFF;
        // Number shadow (depth-tested) behind, then main number see-through
        pose.pushPose();
        pose.translate(0.0, 0.0, -0.001);
        font.drawInBatch(number,
                -numberWidth / 2.0f,
                0f,
                0xFF000000,
                true,
                pose.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                15728880);
        pose.popPose();

        font.drawInBatch(number,
                -numberWidth / 2.0f,
                0f,
                textColor,
                false,
                pose.last().pose(),
                buffer,
                Font.DisplayMode.SEE_THROUGH,
                0,
                15728880);

        pose.popPose();
    }

    private float[] getColorForPos(Player player, BlockPos pos) {
        // Returns RGBA floats for this preview position, matching previous color semantics
        if (isNotBreakable(player.level(), pos) || hasInventoryCapabilities(player.level(), pos)) {
            return new float[]{1f, 0.2f, 0.2f, 1f}; // Neon Red
        } else if (isVoidableBlock(player.level(), pos)) {
            return new float[]{1f, 0.9f, 0.1f, 1f}; // Bright Yellow
        } else if (CompatibilityBlockEqualsChecker.getInstance().isBlockStateSpecificBlock(pos,
                CompatibilityBlockGetter.getInstance().getBlock(player.getOffhandItem()),
                player.getOffhandItem(), player.level())) {
            return new float[]{0.9f, 0.4f, 1.0f, 0.5f}; // Bright Magenta
        }
        return new float[]{0.2f, 1f, 0.2f, 0.5f}; // Neon Lime
    }

    private static final double PIPE_HALF = 0.075; // thickness for regular segments (overall 0.15)
    private static final double NODE_HALF = 0.20;  // thicker joint for nodes (overall 0.40)

    private void drawBox(PoseStack pose, MultiBufferSource buffer, boolean solid,
                         double x0, double y0, double z0,
                         double x1, double y1, double z1,
                         float[] rgba) {
        if (solid) drawFilledBox(pose, buffer, x0, y0, z0, x1, y1, z1, rgba);
        else drawWireBox(pose, buffer, x0, y0, z0, x1, y1, z1, rgba);
    }

    private void drawPipePiece(PoseStack pose, MultiBufferSource buffer, BlockPos pos, Vec3 offset, java.util.Set<BlockPos> previewPositions, float[] rgba, boolean isNode, float[] nodeRgba, boolean solid, float pulseScale) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        double baseHalf = isNode ? NODE_HALF : PIPE_HALF; // larger joint for nodes
        double half = baseHalf * pulseScale;

        float[] drawColor = (isNode && nodeRgba != null) ? nodeRgba : rgba;

        // Center joint
        if (solid && isNode) {
            drawShadedBox(pose, buffer,
                    cx - half - offset.x, cy - half - offset.y, cz - half - offset.z,
                    cx + half - offset.x, cy + half - offset.y, cz + half - offset.z,
                    drawColor);
        } else {
            drawBox(pose, buffer, solid,
                    cx - half - offset.x, cy - half - offset.y, cz - half - offset.z,
                    cx + half - offset.x, cy + half - offset.y, cz + half - offset.z,
                    drawColor);
        }

        // Connectors
        // West (x-)
        if (previewPositions.contains(pos.relative(Direction.WEST))) {
            drawBox(pose, buffer, solid,
                    pos.getX() - offset.x, cy - half - offset.y, cz - half - offset.z,
                    cx - half - offset.x, cy + half - offset.y, cz + half - offset.z,
                    drawColor);
        }
        // East (x+)
        if (previewPositions.contains(pos.relative(Direction.EAST))) {
            drawBox(pose, buffer, solid,
                    cx + half - offset.x, cy - half - offset.y, cz - half - offset.z,
                    pos.getX() + 1.0 - offset.x, cy + half - offset.y, cz + half - offset.z,
                    drawColor);
        }
        // North (z-)
        if (previewPositions.contains(pos.relative(Direction.NORTH))) {
            drawBox(pose, buffer, solid,
                    cx - half - offset.x, cy - half - offset.y, pos.getZ() - offset.z,
                    cx + half - offset.x, cy + half - offset.y, cz - half - offset.z,
                    drawColor);
        }
        // South (z+)
        if (previewPositions.contains(pos.relative(Direction.SOUTH))) {
            drawBox(pose, buffer, solid,
                    cx - half - offset.x, cy - half - offset.y, cz + half - offset.z,
                    cx + half - offset.x, cy + half - offset.y, pos.getZ() + 1.0 - offset.z,
                    drawColor);
        }
        // Down (y-)
        if (previewPositions.contains(pos.relative(Direction.DOWN))) {
            drawBox(pose, buffer, solid,
                    cx - half - offset.x, pos.getY() - offset.y, cz - half - offset.z,
                    cx + half - offset.x, cy - half - offset.y, cz + half - offset.z,
                    drawColor);
        }
        // Up (y+)
        if (previewPositions.contains(pos.relative(Direction.UP))) {
            drawBox(pose, buffer, solid,
                    cx - half - offset.x, cy + half - offset.y, cz - half - offset.z,
                    cx + half - offset.x, pos.getY() + 1.0 - offset.y, cz + half - offset.z,
                    drawColor);
        }
    }

    private void drawFilledBox(PoseStack pose, MultiBufferSource buffer,
                               double x0, double y0, double z0,
                               double x1, double y1, double z1,
                               float[] rgba) {
        float r = rgba[0], g = rgba[1], b = rgba[2], a = rgba[3];
        VertexConsumer vc = buffer.getBuffer(PipeConnectorRenderType.QUADS_NO_DEPTH_TEST);
        org.joml.Matrix4f mat = pose.last().pose();

        float X0 = (float)Math.min(x0, x1);
        float Y0 = (float)Math.min(y0, y1);
        float Z0 = (float)Math.min(z0, z1);
        float X1 = (float)Math.max(x0, x1);
        float Y1 = (float)Math.max(y0, y1);
        float Z1 = (float)Math.max(z0, z1);

        // -X
        v(vc, mat, X0, Y0, Z0, r,g,b,a);
        v(vc, mat, X0, Y1, Z0, r,g,b,a);
        v(vc, mat, X0, Y1, Z1, r,g,b,a);
        v(vc, mat, X0, Y0, Z1, r,g,b,a);
        // +X
        v(vc, mat, X1, Y0, Z1, r,g,b,a);
        v(vc, mat, X1, Y1, Z1, r,g,b,a);
        v(vc, mat, X1, Y1, Z0, r,g,b,a);
        v(vc, mat, X1, Y0, Z0, r,g,b,a);
        // -Y
        v(vc, mat, X0, Y0, Z1, r,g,b,a);
        v(vc, mat, X1, Y0, Z1, r,g,b,a);
        v(vc, mat, X1, Y0, Z0, r,g,b,a);
        v(vc, mat, X0, Y0, Z0, r,g,b,a);
        // +Y
        v(vc, mat, X0, Y1, Z0, r,g,b,a);
        v(vc, mat, X1, Y1, Z0, r,g,b,a);
        v(vc, mat, X1, Y1, Z1, r,g,b,a);
        v(vc, mat, X0, Y1, Z1, r,g,b,a);
        // -Z
        v(vc, mat, X1, Y0, Z0, r,g,b,a);
        v(vc, mat, X1, Y1, Z0, r,g,b,a);
        v(vc, mat, X0, Y1, Z0, r,g,b,a);
        v(vc, mat, X0, Y0, Z0, r,g,b,a);
        // +Z
        v(vc, mat, X0, Y0, Z1, r,g,b,a);
        v(vc, mat, X0, Y1, Z1, r,g,b,a);
        v(vc, mat, X1, Y1, Z1, r,g,b,a);
        v(vc, mat, X1, Y0, Z1, r,g,b,a);

    }

    private void drawShadedBox(PoseStack pose, MultiBufferSource buffer,
                                double x0, double y0, double z0,
                                double x1, double y1, double z1,
                                float[] rgba) {
        org.joml.Matrix4f mat = pose.last().pose();
        VertexConsumer vc = buffer.getBuffer(PipeConnectorRenderType.QUADS_NO_DEPTH_TEST);

        float X0 = (float)Math.min(x0, x1);
        float Y0 = (float)Math.min(y0, y1);
        float Z0 = (float)Math.min(z0, z1);
        float X1 = (float)Math.max(x0, x1);
        float Y1 = (float)Math.max(y0, y1);
        float Z1 = (float)Math.max(z0, z1);

        // Fake light from above-front-left
        org.joml.Vector3f sun = new org.joml.Vector3f(-0.35f, 0.9f, -0.25f).normalize();
        org.joml.Vector3f[] normals = new org.joml.Vector3f[] {
                new org.joml.Vector3f(-1,0,0), new org.joml.Vector3f(1,0,0),
                new org.joml.Vector3f(0,-1,0), new org.joml.Vector3f(0,1,0),
                new org.joml.Vector3f(0,0,-1), new org.joml.Vector3f(0,0,1)
        };
        float[] m = new float[6];
        for (int i=0;i<6;i++) {
            float dot = Math.max(0f, normals[i].dot(sun));
            m[i] = 0.45f + dot * 0.55f; // 0.45..1.0
        }
        // Stronger contrast on top face
        m[3] = Math.min(1.15f, m[3] + 0.15f);

        float r = rgba[0], g = rgba[1], b = rgba[2], a = rgba[3];

        // -X
        v(vc, mat, X0, Y0, Z0, r*m[0],g*m[0],b*m[0],a);
        v(vc, mat, X0, Y1, Z0, r*m[0],g*m[0],b*m[0],a);
        v(vc, mat, X0, Y1, Z1, r*m[0],g*m[0],b*m[0],a);
        v(vc, mat, X0, Y0, Z1, r*m[0],g*m[0],b*m[0],a);
        // +X
        v(vc, mat, X1, Y0, Z1, r*m[1],g*m[1],b*m[1],a);
        v(vc, mat, X1, Y1, Z1, r*m[1],g*m[1],b*m[1],a);
        v(vc, mat, X1, Y1, Z0, r*m[1],g*m[1],b*m[1],a);
        v(vc, mat, X1, Y0, Z0, r*m[1],g*m[1],b*m[1],a);
        // -Y
        v(vc, mat, X0, Y0, Z1, r*m[2],g*m[2],b*m[2],a);
        v(vc, mat, X1, Y0, Z1, r*m[2],g*m[2],b*m[2],a);
        v(vc, mat, X1, Y0, Z0, r*m[2],g*m[2],b*m[2],a);
        v(vc, mat, X0, Y0, Z0, r*m[2],g*m[2],b*m[2],a);
        // +Y (top) — a bit brighter
        v(vc, mat, X0, Y1, Z0, r*m[3],g*m[3],b*m[3],a);
        v(vc, mat, X1, Y1, Z0, r*m[3],g*m[3],b*m[3],a);
        v(vc, mat, X1, Y1, Z1, r*m[3],g*m[3],b*m[3],a);
        v(vc, mat, X0, Y1, Z1, r*m[3],g*m[3],b*m[3],a);
        // -Z
        v(vc, mat, X1, Y0, Z0, r*m[4],g*m[4],b*m[4],a);
        v(vc, mat, X1, Y1, Z0, r*m[4],g*m[4],b*m[4],a);
        v(vc, mat, X0, Y1, Z0, r*m[4],g*m[4],b*m[4],a);
        v(vc, mat, X0, Y0, Z0, r*m[4],g*m[4],b*m[4],a);
        // +Z
        v(vc, mat, X0, Y0, Z1, r*m[5],g*m[5],b*m[5],a);
        v(vc, mat, X0, Y1, Z1, r*m[5],g*m[5],b*m[5],a);
        v(vc, mat, X1, Y1, Z1, r*m[5],g*m[5],b*m[5],a);
        v(vc, mat, X1, Y0, Z1, r*m[5],g*m[5],b*m[5],a);

        // Small top highlight inset to enhance 3D read
        float inset = 0.12f;
        float eps = 0.0006f;
        float hx0 = X0 + inset;
        float hz0 = Z0 + inset;
        float hx1 = X1 - inset;
        float hz1 = Z1 - inset;
        float hr = Math.min(1.0f, r * 1.2f), hg = Math.min(1.0f, g * 1.2f), hb = Math.min(1.0f, b * 1.2f);
        float ha = Math.min(1.0f, a * 0.8f);
        v(vc, mat, hx0, Y1 + eps, hz0, hr, hg, hb, ha);
        v(vc, mat, hx1, Y1 + eps, hz0, hr, hg, hb, ha);
        v(vc, mat, hx1, Y1 + eps, hz1, hr, hg, hb, ha);
        v(vc, mat, hx0, Y1 + eps, hz1, hr, hg, hb, ha);

        VertexConsumer lineVC = buffer.getBuffer(PipeConnectorRenderType.THIN_LINES_NO_DEPTH_TEST);
        double epsOutline = 0.0015;
        AABB aabb = new AABB(X0 - (float)epsOutline, Y0 - (float)epsOutline, Z0 - (float)epsOutline, X1 + (float)epsOutline, Y1 + (float)epsOutline, Z1 + (float)epsOutline);
        LevelRenderer.renderLineBox(pose, lineVC, aabb, 0f, 0f, 0f, 1.0f);
    }

    private void drawWireBox(PoseStack pose, MultiBufferSource buffer,
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

    private void v(VertexConsumer vc, org.joml.Matrix4f mat,
                   float x, float y, float z,
                   float r, float g, float b, float a) {
        int ri = Math.min(255, Math.max(0, (int)(r * 255f)));
        int gi = Math.min(255, Math.max(0, (int)(g * 255f)));
        int bi = Math.min(255, Math.max(0, (int)(b * 255f)));
        int ai = Math.min(255, Math.max(0, (int)(a * 255f)));
        int color = (ai << 24) | (ri << 16) | (gi << 8) | bi;
        vc.addVertex(mat, x, y, z).setColor(color);
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