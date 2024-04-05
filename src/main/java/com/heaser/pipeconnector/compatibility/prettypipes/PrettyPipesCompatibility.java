package com.heaser.pipeconnector.compatibility.prettypipes;

import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import de.ellpeck.prettypipes.items.PipeFrameItem;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;


import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class PrettyPipesCompatibility implements IPlacer {

    static public Class<? extends Block> getBlockToRegister() { return PipeBlock.class; }

    static public Class<? extends Item> getItemStackClassToRegister() { return PipeFrameItem.class; };

    public boolean place(Level level, BlockPos pos, Player player, Item item) {
        if (!isVoidableBlock(level, pos)) {
            level.addDestroyBlockEffect(pos, level.getBlockState(pos));
            level.destroyBlock(pos, true, player);
        }

        BlockState blockState = Block.byItem(item).defaultBlockState();

        boolean isWaterBlock = isWaterBlock(level, pos);
        boolean hasLevelBlockStateProperty = hasLevelBlockStateProperty(level, pos);

        if (isWaterBlock && hasLevelBlockStateProperty) {
            if (!isFlowingWaterBlock(level, pos)) {
                // Waterlog the pipe
                blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
            }
        }
        return level.setBlockAndUpdate(pos, blockState);
    }



    public boolean isWaterBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() == Blocks.WATER;
    }

    public boolean hasLevelBlockStateProperty(Level level, BlockPos pos) {
        return level.getBlockState(pos).hasProperty(BlockStateProperties.LEVEL);
    }

    public boolean isFlowingWaterBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).getValue(BlockStateProperties.LEVEL) > 0;
    }
}
