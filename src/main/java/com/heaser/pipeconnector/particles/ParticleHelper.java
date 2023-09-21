package com.heaser.pipeconnector.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;


public class ParticleHelper {
    public static void serverSpawnMarkerParticle(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.GLOW,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                10,
                0, 0.01, 0,
                0);
    }
}
