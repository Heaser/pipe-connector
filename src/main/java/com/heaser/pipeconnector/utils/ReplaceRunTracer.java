package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.compatibility.interfaces.IPipeReplacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;

public class ReplaceRunTracer {

    // Insertion order = BFS order from the seed, reused as the animation sweep order
    public record TraceResult(LinkedHashSet<BlockPos> run, boolean truncated) {
    }

    public static TraceResult trace(Level level, BlockPos seed, IPipeReplacer replacer, Object kind, int cap) {
        LinkedHashSet<BlockPos> run = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        if (!level.isLoaded(seed) || !replacer.matchesKind(level, seed, kind)) {
            return new TraceResult(run, false);
        }

        run.add(seed);
        queue.add(seed);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (run.contains(next) || !level.isLoaded(next)) {
                    continue;
                }
                if (!replacer.matchesKind(level, next, kind)) {
                    continue;
                }
                if (run.size() >= cap) {
                    return new TraceResult(run, true);
                }
                run.add(next);
                queue.add(next);
            }
        }

        return new TraceResult(run, false);
    }
}
