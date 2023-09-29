package com.heaser.pipeconnector.client.outline;

import com.heaser.pipeconnector.utils.PreviewInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;

@OnlyIn(Dist.CLIENT)
public class PreviewDrawer {
    public HashSet<PreviewInfo> previewMap = new HashSet<>();

    public PreviewDrawer() {
    }

    public void draw(PoseStack pose, MultiBufferSource buffer, double partialTicks, Player player) {
        VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
        double d0 = player.xOld + (player.getX() - player.xOld) * partialTicks;
        double d1 = player.yOld + player.getEyeHeight() + (player.getY() - player.yOld) * partialTicks;
        double d2 = player.zOld + (player.getZ() - player.zOld) * partialTicks;

        for (PreviewInfo previewInfo : previewMap) {
            AABB aabb = new AABB(previewInfo.pos).move(-d0, -d1, -d2);
            LevelRenderer.renderLineBox(pose, builder, aabb, 1F, 0, 0, 1F);
        }
    }
}
