package com.heaser.pipeconnector.compatibility.prettypipes;

//  Pretty Pipes not yet available for 26.2.1.
public final class PrettyPipesCompatibility {
    private PrettyPipesCompatibility() {}
}

/*

import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import de.ellpeck.prettypipes.items.PipeFrameItem;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;


import java.util.List;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class PrettyPipesCompatibility implements IPlacer {

    static public Class<? extends Block> getBlockToRegister() { return PipeBlock.class; }

    static public Class<? extends Item> getItemStackClassToRegister() { return PipeFrameItem.class; };

    public boolean place(Level level, BlockPos pos, Player player, Item item, List<Direction> side, ItemStack heldPipeItem) {
        if (!isVoidableBlock(level, pos)) {
            // Harvest drops and add to inventory
            BlockState state = level.getBlockState(pos);
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, heldPipeItem);
                for (ItemStack drop : drops) {
                    if (!player.getInventory().add(drop)) {
                        player.drop(drop, false);
                    }
                }
                // Animation and sound
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
            }
            level.removeBlock(pos, false);
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

*/
