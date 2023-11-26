package com.heaser.pipeconnector.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import java.util.Random;

public class ParticleHelper {
    private static final Random random = new Random();

    public static void serverSpawnMarkerParticle(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 30; i++) { // Slightly increased count for a denser effect
            ParticleOptions particleType = switch (random.nextInt(3)) {
                case 0 -> ParticleTypes.END_ROD;
                case 1 -> ParticleTypes.PORTAL;
                default -> ParticleTypes.ELECTRIC_SPARK;
            };

            // Selecting futuristic particles

            double angle = 2 * Math.PI * i / 30;
            double dx = Math.cos(angle) * 0.1;
            double dy = 0.05;
            double dz = Math.sin(angle) * 0.1;

            level.sendParticles(particleType,
                    pos.getX() + 0.5 + dx, pos.getY() + 0.5, pos.getZ() + 0.5 + dz,
                    1,
                    dx, dy, dz,
                    0.1);
        }
    }
}