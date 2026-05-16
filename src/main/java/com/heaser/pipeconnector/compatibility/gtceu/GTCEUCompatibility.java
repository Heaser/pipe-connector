package com.heaser.pipeconnector.compatibility.gtceu;

//  GTCEU Modern not yet available for 26.2.1
public final class GTCEUCompatibility {
    private GTCEUCompatibility() {}
}

/*

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class GTCEUCompatibility implements IPlacer {
    static public Class<? extends Block> getBlockToRegister() { return PipeBlock.class; }

    public boolean place(Level level, BlockPos pos, Player player, Item item, List<Direction> adjacentDirectionSides, ItemStack heldPipeItem) {
        if (!isVoidableBlock(level, pos)) {
            // Harvest drops and add to inventory
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
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

        if (item instanceof PipeBlockItem) {
            BlockState blockState = Block.byItem(item).defaultBlockState();
            boolean superVal = level.setBlock(pos, blockState, 11);
            if (superVal && !level.isClientSide) {
                PipeBlockItem pipeBlockItem = (PipeBlockItem)item;
                IPipeNode selfTile = pipeBlockItem.getBlock().getPipeTile(level, pos);
                if (selfTile == null) {
                    return true;
                }

                for (Direction side : adjacentDirectionSides) {
                    if (selfTile.getPipeBlock().canConnect(selfTile, side)) {
                        selfTile.setConnection(side, true, false);
                    }
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
                selfTile.notifyBlockUpdate();
            }

            return superVal;
        }
        return false;
    }
}

*/
