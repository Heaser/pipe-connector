package com.heaser.pipeconnector.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PipeConnectorConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_DEPTH;
    public static final ModConfigSpec.IntValue MAX_ASTAR_ITERATIONS;
    public static final ModConfigSpec.IntValue MAX_ALLOWED_PIPES_TO_PLACE;
    public static final ModConfigSpec.IntValue MAX_ALLOWED_NODES;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("Pipe Connector Configurations");

        MAX_DEPTH = BUILDER.comment("Maximum depth of the pipe connector")
                .defineInRange("maxDepth", 50, 10, 100);

        MAX_ASTAR_ITERATIONS = BUILDER.comment("Maximum iterations of the A* algorithm, Higher numbers will take longer to calculate but will reach longer distances")
                .defineInRange("maxAStarIterations", 20000, 10000, 200000);

        MAX_ALLOWED_PIPES_TO_PLACE = BUILDER.comment("Maximum number of pipes allowed to be placed at once")
                .defineInRange("maxPipeAllowed", 640, 100, 1280);

        MAX_ALLOWED_NODES = BUILDER.comment("Maximum number of nodes allowed to connect between")
                .defineInRange("maxNodesAllowed", 8, 2, 16);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
