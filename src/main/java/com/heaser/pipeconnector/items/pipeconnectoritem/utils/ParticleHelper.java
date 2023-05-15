package com.heaser.pipeconnector.items.pipeconnectoritem.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;


public class ParticleHelper {

    //TODO: Make separate methods for each particle you want to spawn though this class really isn't required with the current use.
    // It could easily be part of PipeConnectorItem instead. I'll leave it for now though as there's lots of info here.
    // Don't try to make a mega cover all method either as the particle spawner already handles that. Just use it.

    public static void serverSpawnMarkerParticle(ServerLevel level, BlockPos pos) {
        //TODO: We factored out the random noise, thus these variables aren't needed anymore.

        //TODO: Using the same source of random makes random more random.
        //TODO: Server spawned particles can handle a quality, don't need to loop over to repeat and get extra noise. It's already done for us.

        //TODO: If you wish for only a specific player to see the particles level.sendParticles also has another signature where you can define the player
        // that will see it.
        // I hate when Mojang changes var orders throughout methods... particles are annoying enough as is with all the options don't change the order dammit
        // (unused)Player, ParticleType, x, y, z, count, xDist, yDist, zDist, speed
        level.sendParticles(ParticleTypes.GLOW,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                10,
                0, 0.01, 0,
                0);
    }
}
