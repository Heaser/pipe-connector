package com.heaser.pipeconnector.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public class ParticleHelper {
    public static void serverSpawnMarkerParticle(ServerLevel level, BlockPos pos) {
        RandomSource rand = level.getRandom();

        for (int i = 0; i < 30; i++) {
            ParticleOptions particleType = switch (rand.nextInt(4)) {
                case 0 -> ParticleTypes.END_ROD;
                case 1 -> ParticleTypes.PORTAL;
                case 2 -> ParticleTypes.ELECTRIC_SPARK;
                default -> ParticleTypes.WITCH;
            };

            double angle = 2 * Math.PI * i / 30 + rand.nextDouble() * 0.2;
            double radius = 0.4 + rand.nextDouble() * 0.3;
            double dx = Math.cos(angle) * radius;
            double dy = rand.nextDouble() * 0.3;
            double dz = Math.sin(angle) * radius;

            level.sendParticles(particleType,
                    pos.getX() + 0.5 + dx,
                    pos.getY() + 0.2 + rand.nextDouble() * 0.6,
                    pos.getZ() + 0.5 + dz,
                    1,
                    dx * 0.05, dy * 0.05, dz * 0.05,
                    0.05);
        }
    }
}