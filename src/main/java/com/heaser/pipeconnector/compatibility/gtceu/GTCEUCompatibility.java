package com.heaser.pipeconnector.compatibility.gtceu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GTCEUCompatibility implements IPlacer {
    static public Class<? extends Block> getBlockToRegister() { return PipeBlock.class; }

    public boolean place(Level level, BlockPos pos, Player player, Item item, Direction side) {
        if (item instanceof PipeBlockItem) {
            BlockState blockState = Block.byItem(item).defaultBlockState();
            boolean superVal = level.setBlock(pos, blockState, 11);
            if (superVal && !level.isClientSide) {
                PipeBlockItem pipeBlockItem = (PipeBlockItem)item;
                IPipeNode selfTile = pipeBlockItem.getBlock().getPipeTile(level, pos);
                if (selfTile == null) {
                    return true;
                }

                if (selfTile.getPipeBlock().canConnect(selfTile, side)) {
                    selfTile.setConnection(side, true, false);
                }

                for(Direction facing : GTUtil.DIRECTIONS) {
                    BlockEntity te = selfTile.getNeighbor(facing);
                    if (te instanceof IPipeNode) {
                        IPipeNode otherPipe = (IPipeNode)te;
                        if (otherPipe.isConnected(facing.getOpposite())) {
                            if (otherPipe.getPipeBlock().canPipesConnect(otherPipe, facing.getOpposite(), selfTile)) {
                                selfTile.setConnection(facing, true, true);
                            } else {
                                otherPipe.setConnection(facing.getOpposite(), false, true);
                            }
                        }
                    } else if (!ConfigHolder.INSTANCE.machines.gt6StylePipesCables && selfTile.getPipeBlock().canPipeConnectToBlock(selfTile, facing, te)) {
                        selfTile.setConnection(facing, true, false);
                    }
                }
            }

            return superVal;
        }
        return false;
    }
}
