package com.heaser.pipeconnector.items.pipeconnectoritem.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class ParticleHelper {
    public static void spawnDirectionHighlightParticles(Level level, BlockPos pos, float red, float green, float blue, float alpha) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        Vector3f color = new Vector3f(red, green, blue);

        for (int i = 0; i < 100; i++) {
            double offsetX = (Math.random() - 0.1) * 0.2;
            double offsetY = (Math.random() - 0.1) * 0.2;
            double offsetZ = (Math.random() - 0.1) * 0.2;

            // r g b a
            level.addParticle(new DustParticleOptions(color, alpha),
                    x + offsetX, y + offsetY, z + offsetZ,
                    0.0, 0.0, 0.0);
        }
    }
}
