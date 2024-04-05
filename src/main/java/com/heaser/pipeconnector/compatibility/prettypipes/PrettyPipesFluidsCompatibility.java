package com.heaser.pipeconnector.compatibility.prettypipes;
import dev.quarris.ppfluids.pipe.FluidPipeBlock;
import net.minecraft.world.level.block.Block;

public class PrettyPipesFluidsCompatibility {
    static public Class<? extends Block> getBlockToRegister() { return FluidPipeBlock.class; }
}
