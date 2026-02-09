package com.heaser.pipeconnector.client.outline;

import com.heaser.pipeconnector.compatibility.CompatibilityBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IColorProvider;
import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.utils.TagUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

import static com.heaser.pipeconnector.utils.GeneralUtils.isHoldingPipeConnector;

public class PipeVisionRenderer {
    private final Map<BlockPos, PipeRenderInfo> cachedPipes = new HashMap<>();
    private Vec3 lastPlayerPos = Vec3.ZERO;
    private long lastUpdateTime = 0;
    private static final int SCAN_RADIUS = 24;
    private static final double UPDATE_DIST_SQR = 8 * 8;
    private static final long UPDATE_INTERVAL = 20; // ticks

    private static final float PIPE_RADIUS = 0.04f;
    private static final float ENDPOINT_RADIUS = 0.05f;

    public void handleOnRenderLevel(PoseStack pose, MultiBufferSource buffer, Player player) {
        if (!isHoldingPipeConnector(player)) {
            cachedPipes.clear();
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (!TagUtils.getPipeVision(heldItem)) {
            cachedPipes.clear();
            return;
        }

        updateCacheIfNeeded(player);
        render(pose, buffer, player);
    }

    private void updateCacheIfNeeded(Player player) {
        long now = System.currentTimeMillis() / 50; // approx ticks
        Vec3 playerPos = player.position();
        boolean moved = playerPos.distanceToSqr(lastPlayerPos) > UPDATE_DIST_SQR;
        boolean timeToUpdate = now - lastUpdateTime > UPDATE_INTERVAL;

        if (moved || timeToUpdate || cachedPipes.isEmpty()) {
            scan(player.level(), player.blockPosition());
            lastPlayerPos = playerPos;
            lastUpdateTime = now;
        }
    }

    private void scan(Level level, BlockPos center) {
        cachedPipes.clear();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(pos);

                    boolean isPipe = state.is(TagKeys.PIPE_BLOCK) || CompatibilityBlockEqualsChecker.isSupportedBlock(state);

                    if (isPipe) {
                        int color = getPipeColor(level, pos, state);
                        cachedPipes.put(pos.immutable(), new PipeRenderInfo(color));
                    }
                }
            }
        }

        // Second pass for connections
        for (Map.Entry<BlockPos, PipeRenderInfo> entry : cachedPipes.entrySet()) {
            BlockPos currentPos = entry.getKey();
            PipeRenderInfo info = entry.getValue();
            
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(dir);
                PipeRenderInfo neighborInfo = cachedPipes.get(neighborPos);

                if (neighborInfo != null) {
                    // Connect if same color (proxy for same type/variant)
                    if (neighborInfo.color == info.color) {
                        info.connections |= (1 << dir.ordinal());
                    }
                }
            }
        }
    }

    private int getPipeColor(Level level, BlockPos pos, BlockState state) {
        Integer compatColor = CompatibilityBlockEqualsChecker.getColor(pos, level);
        if (compatColor != null) {
            return compatColor;
        }
        // Deterministic color from BlockState hash or Block hash
        // Use a consistent hash to generate a stable color
        int hash = state.getBlock().getDescriptionId().hashCode();

        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = (hash & 0x0000FF);
        
        // Boost brightness to ensure visibility
        if (r < 50) r += 100;
        if (g < 50) g += 100;
        if (b < 50) b += 100;

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void render(PoseStack pose, MultiBufferSource buffer, Player player) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer vc = buffer.getBuffer(PipeConnectorRenderType.QUADS_NO_DEPTH_TEST);

        for (Map.Entry<BlockPos, PipeRenderInfo> entry : cachedPipes.entrySet()) {
            BlockPos pos = entry.getKey();
            PipeRenderInfo info = entry.getValue();
            
            double x = pos.getX() - cameraPos.x;
            double y = pos.getY() - cameraPos.y;
            double z = pos.getZ() - cameraPos.z;

            float a = 1.0f;
            float r = ((info.color >> 16) & 0xFF) / 255f;
            float g = ((info.color >> 8) & 0xFF) / 255f;
            float b = (info.color & 0xFF) / 255f;

            renderPipe(pose, vc, x, y, z, info.connections, r, g, b, a);
        }
    }

    private void renderPipe(PoseStack pose, VertexConsumer vc, double x, double y, double z, int connections, float r, float g, float b, float a) {
        org.joml.Matrix4f mat = pose.last().pose();
        double cx = x + 0.5;
        double cy = y + 0.5;
        double cz = z + 0.5;

        // Center Node
        drawBox(vc, mat, cx - PIPE_RADIUS, cy - PIPE_RADIUS, cz - PIPE_RADIUS, cx + PIPE_RADIUS, cy + PIPE_RADIUS, cz + PIPE_RADIUS, r, g, b, a);

        boolean hasConnection = false;

        // Arms
        for (Direction dir : Direction.values()) {
            if ((connections & (1 << dir.ordinal())) != 0) {
                hasConnection = true;
                double x0 = cx - PIPE_RADIUS;
                double y0 = cy - PIPE_RADIUS;
                double z0 = cz - PIPE_RADIUS;
                double x1 = cx + PIPE_RADIUS;
                double y1 = cy + PIPE_RADIUS;
                double z1 = cz + PIPE_RADIUS;

                switch (dir) {
                    case WEST -> x0 = x;
                    case EAST -> x1 = x + 1.0;
                    case DOWN -> y0 = y;
                    case UP -> y1 = y + 1.0;
                    case NORTH -> z0 = z;
                    case SOUTH -> z1 = z + 1.0;
                }
                drawBox(vc, mat, x0, y0, z0, x1, y1, z1, r, g, b, a);
            }
        }
        drawBox(vc, mat, cx - ENDPOINT_RADIUS, cy - ENDPOINT_RADIUS, cz - ENDPOINT_RADIUS, cx + ENDPOINT_RADIUS, cy + ENDPOINT_RADIUS, cz + ENDPOINT_RADIUS, r, g, b, a);
    }

    private void drawBox(VertexConsumer vc, org.joml.Matrix4f mat, double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {
        float X0 = (float)x0; float Y0 = (float)y0; float Z0 = (float)z0;
        float X1 = (float)x1; float Y1 = (float)y1; float Z1 = (float)z1;
        
        int ri = (int)(r * 255);
        int gi = (int)(g * 255);
        int bi = (int)(b * 255);
        int ai = (int)(a * 255);
        int color = (ai << 24) | (ri << 16) | (gi << 8) | bi;
        
        // -X
        vc.addVertex(mat, X0, Y0, Z0).setColor(color);
        vc.addVertex(mat, X0, Y1, Z0).setColor(color);
        vc.addVertex(mat, X0, Y1, Z1).setColor(color);
        vc.addVertex(mat, X0, Y0, Z1).setColor(color);
        // +X
        vc.addVertex(mat, X1, Y0, Z1).setColor(color);
        vc.addVertex(mat, X1, Y1, Z1).setColor(color);
        vc.addVertex(mat, X1, Y1, Z0).setColor(color);
        vc.addVertex(mat, X1, Y0, Z0).setColor(color);
        // -Y
        vc.addVertex(mat, X0, Y0, Z1).setColor(color);
        vc.addVertex(mat, X1, Y0, Z1).setColor(color);
        vc.addVertex(mat, X1, Y0, Z0).setColor(color);
        vc.addVertex(mat, X0, Y0, Z0).setColor(color);
        // +Y
        vc.addVertex(mat, X0, Y1, Z0).setColor(color);
        vc.addVertex(mat, X1, Y1, Z0).setColor(color);
        vc.addVertex(mat, X1, Y1, Z1).setColor(color);
        vc.addVertex(mat, X0, Y1, Z1).setColor(color);
        // -Z
        vc.addVertex(mat, X1, Y0, Z0).setColor(color);
        vc.addVertex(mat, X1, Y1, Z0).setColor(color);
        vc.addVertex(mat, X0, Y1, Z0).setColor(color);
        vc.addVertex(mat, X0, Y0, Z0).setColor(color);
        // +Z
        vc.addVertex(mat, X0, Y0, Z1).setColor(color);
        vc.addVertex(mat, X0, Y1, Z1).setColor(color);
        vc.addVertex(mat, X1, Y1, Z1).setColor(color);
        vc.addVertex(mat, X1, Y0, Z1).setColor(color);
    }

    private static class PipeRenderInfo {
        int color;
        int connections; // Bitmask of Direction.ordinal()

        public PipeRenderInfo(int color) {
            this.color = color;
            this.connections = 0;
        }
    }
}
