package com.heaser.pipeconnector.client.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuildAnimationRenderer {

    private static final long TOTAL_SWEEP_MS = 1500L;
    private static final long FADE_DURATION_MS = 500L;
    private static final double GLOW_HALF = 0.12;
    private static final float GLOW_R = 0.1f;
    private static final float GLOW_G = 0.9f;
    private static final float GLOW_B = 1.0f;
    private static final float PEAK_ALPHA = 0.9f;

    private List<BlockPos> animationPath = new ArrayList<>();
    private Set<BlockPos> pathSet = new HashSet<>();
    private long animationStartMs = -1L;
    private boolean isActive = false;

    public void startAnimation(List<BlockPos> path) {
        this.animationPath = new ArrayList<>(path);
        this.pathSet = new HashSet<>(path);
        this.animationStartMs = System.currentTimeMillis();
        this.isActive = !path.isEmpty();
    }

    public void handleOnRenderLevel(PoseStack pose, MultiBufferSource buffer) {
        if (!isActive) return;

        long elapsed = System.currentTimeMillis() - animationStartMs;
        if (elapsed >= TOTAL_SWEEP_MS + FADE_DURATION_MS) {
            isActive = false;
            animationPath.clear();
            pathSet.clear();
            return;
        }

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        int n = animationPath.size();

        for (int i = 0; i < n; i++) {
            float alpha = computeAlpha(i, n, elapsed);
            if (alpha <= 0.005f) continue;
            drawGlowBlock(pose, buffer, animationPath.get(i), camera, alpha);
        }
    }

    private float computeAlpha(int i, int n, long elapsed) {
        if (n <= 1) {
            return Math.max(0f, PEAK_ALPHA * (1f - elapsed / (float) FADE_DURATION_MS));
        }
        float peakTime = ((float) i / (n - 1)) * TOTAL_SWEEP_MS;
        float timeSincePeak = elapsed - peakTime;
        if (timeSincePeak < 0f) return 0f;
        return Math.max(0f, PEAK_ALPHA * (1f - timeSincePeak / FADE_DURATION_MS));
    }

    private void drawGlowBlock(PoseStack pose, MultiBufferSource buffer, BlockPos pos, Vec3 camera, float alpha) {
        double cx = pos.getX() + 0.5 - camera.x;
        double cy = pos.getY() + 0.5 - camera.y;
        double cz = pos.getZ() + 0.5 - camera.z;
        double h = GLOW_HALF;

        // Center joint
        drawFilledBox(pose, buffer, cx - h, cy - h, cz - h, cx + h, cy + h, cz + h, alpha);

        // Connector arms toward adjacent path blocks
        if (pathSet.contains(pos.relative(Direction.WEST))) {
            drawFilledBox(pose, buffer, pos.getX() - camera.x, cy - h, cz - h, cx - h, cy + h, cz + h, alpha);
        }
        if (pathSet.contains(pos.relative(Direction.EAST))) {
            drawFilledBox(pose, buffer, cx + h, cy - h, cz - h, pos.getX() + 1.0 - camera.x, cy + h, cz + h, alpha);
        }
        if (pathSet.contains(pos.relative(Direction.NORTH))) {
            drawFilledBox(pose, buffer, cx - h, cy - h, pos.getZ() - camera.z, cx + h, cy + h, cz - h, alpha);
        }
        if (pathSet.contains(pos.relative(Direction.SOUTH))) {
            drawFilledBox(pose, buffer, cx - h, cy - h, cz + h, cx + h, cy + h, pos.getZ() + 1.0 - camera.z, alpha);
        }
        if (pathSet.contains(pos.relative(Direction.DOWN))) {
            drawFilledBox(pose, buffer, cx - h, pos.getY() - camera.y, cz - h, cx + h, cy - h, cz + h, alpha);
        }
        if (pathSet.contains(pos.relative(Direction.UP))) {
            drawFilledBox(pose, buffer, cx - h, cy + h, cz - h, cx + h, pos.getY() + 1.0 - camera.y, cz + h, alpha);
        }

        // Wire outline on center joint
        VertexConsumer lineVC = buffer.getBuffer(PipeConnectorRenderType.LINES_NO_DEPTH_TEST);
        AABB aabb = new AABB(cx - h - 0.002, cy - h - 0.002, cz - h - 0.002,
                cx + h + 0.002, cy + h + 0.002, cz + h + 0.002);
        LevelRenderer.renderLineBox(pose, lineVC, aabb, GLOW_R, GLOW_G, GLOW_B, alpha);
    }

    private void drawFilledBox(PoseStack pose, MultiBufferSource buffer,
                               double x0, double y0, double z0,
                               double x1, double y1, double z1,
                               float alpha) {
        float r = GLOW_R, g = GLOW_G, b = GLOW_B, a = alpha;
        VertexConsumer vc = buffer.getBuffer(PipeConnectorRenderType.QUADS_NO_DEPTH_TEST);
        Matrix4f mat = pose.last().pose();

        float X0 = (float) Math.min(x0, x1);
        float Y0 = (float) Math.min(y0, y1);
        float Z0 = (float) Math.min(z0, z1);
        float X1 = (float) Math.max(x0, x1);
        float Y1 = (float) Math.max(y0, y1);
        float Z1 = (float) Math.max(z0, z1);

        // -X
        v(vc, mat, X0, Y0, Z0, r, g, b, a); v(vc, mat, X0, Y1, Z0, r, g, b, a);
        v(vc, mat, X0, Y1, Z1, r, g, b, a); v(vc, mat, X0, Y0, Z1, r, g, b, a);
        // +X
        v(vc, mat, X1, Y0, Z1, r, g, b, a); v(vc, mat, X1, Y1, Z1, r, g, b, a);
        v(vc, mat, X1, Y1, Z0, r, g, b, a); v(vc, mat, X1, Y0, Z0, r, g, b, a);
        // -Y
        v(vc, mat, X0, Y0, Z1, r, g, b, a); v(vc, mat, X1, Y0, Z1, r, g, b, a);
        v(vc, mat, X1, Y0, Z0, r, g, b, a); v(vc, mat, X0, Y0, Z0, r, g, b, a);
        // +Y
        v(vc, mat, X0, Y1, Z0, r, g, b, a); v(vc, mat, X1, Y1, Z0, r, g, b, a);
        v(vc, mat, X1, Y1, Z1, r, g, b, a); v(vc, mat, X0, Y1, Z1, r, g, b, a);
        // -Z
        v(vc, mat, X1, Y0, Z0, r, g, b, a); v(vc, mat, X1, Y1, Z0, r, g, b, a);
        v(vc, mat, X0, Y1, Z0, r, g, b, a); v(vc, mat, X0, Y0, Z0, r, g, b, a);
        // +Z
        v(vc, mat, X0, Y0, Z1, r, g, b, a); v(vc, mat, X0, Y1, Z1, r, g, b, a);
        v(vc, mat, X1, Y1, Z1, r, g, b, a); v(vc, mat, X1, Y0, Z1, r, g, b, a);
    }

    private void v(VertexConsumer vc, Matrix4f mat, float x, float y, float z,
                   float r, float g, float b, float a) {
        int ri = Math.min(255, Math.max(0, (int) (r * 255f)));
        int gi = Math.min(255, Math.max(0, (int) (g * 255f)));
        int bi = Math.min(255, Math.max(0, (int) (b * 255f)));
        int ai = Math.min(255, Math.max(0, (int) (a * 255f)));
        int color = (ai << 24) | (ri << 16) | (gi << 8) | bi;
        vc.addVertex(mat, x, y, z).setColor(color);
    }
}
