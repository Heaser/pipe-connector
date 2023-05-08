package com.heaser.pipeconnector.items.pipeconnectoritem.utils;

import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;


public class ParticleHelper {
    public static void spawnDirectionHighlightParticles(Level level, BlockPos pos, float red, float green, float blue, float alpha) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Keeping this instead I'll want to use more complex particles
        Vector3d color = new Vector3d(red, green, blue);

        for (int i = 0; i < 10; i++) {
            double offsetX = (Math.random() - 0.1) * 0.2;
            double offsetY = (Math.random() - 0.1) * 0.2;
            double offsetZ = (Math.random() - 0.1) * 0.2;


            level.addParticle(ParticleTypes.GLOW, x + offsetX, y + offsetY, z + offsetZ,
                    0, 0.01, 0);


        }
    }
}
