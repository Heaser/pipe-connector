package com.heaser.pipeconnector.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PipeConnectorConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_DEPTH;
    public static final ForgeConfigSpec.IntValue MAX_ASTAR_ITERATIONS;
    public static final ForgeConfigSpec.IntValue MAX_ALLOWED_PIPES_TO_PLACE;

    static {
        MAX_DEPTH = BUILDER.comment("Maximum depth of the pipe connector")
                .defineInRange("maxDepth", 50, 10, 100);

        MAX_ASTAR_ITERATIONS = BUILDER.comment("Maximum iterations of the A* algorithm, Higher numbers will take longer to calculate but will reach longer distances")
                .defineInRange("maxAStarIterations", 20000, 10000, 200000);

        MAX_ALLOWED_PIPES_TO_PLACE = BUILDER.comment("Maximum number of pipes allowed to be placed at once")
                .defineInRange("maxPipeAllowed", 640, 100, 1280);

        SPEC = BUILDER.build();
    }
}
