package com.heaser.pipeconnector.compatibility.mi;

//  Modern Industrialization not yet available for 26.2.1
public final class MICompatibility {
    private MICompatibility() {}
}

/*

import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeBlock;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import aztech.modern_industrialization.pipes.impl.PipeVoxelShape;
import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockGetter;
import com.heaser.pipeconnector.compatibility.interfaces.IMultiPipeColorProvider;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import com.heaser.pipeconnector.compatibility.interfaces.PipeRenderEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class MICompatibility implements IPlacer, IBlockEqualsChecker, IBlockGetter, IMultiPipeColorProvider {

    private static final int MAX_PIPES_PER_BLOCK = 3;

    public static Class<? extends Item> getItemToRegister() {
        return PipeItem.class;
    }

    public static Class<? extends Block> getBlockToRegister() {
        return PipeBlock.class;
    }

    @Override
    public Block getBlock(ItemStack placedStack) {
        try {
            Block block = MIPipes.BLOCK_PIPE.get();
            if (block == null) {
                PipeConnector.LOGGER.warn("MI compatibility: BLOCK_PIPE returned null");
            }
            return block;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("MI compatibility error in getBlock: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean place(Level level, BlockPos pos, Player player, Item item, List<Direction> adjacentDirectionSides, ItemStack heldPipeItem) {
        try {
            if (!(item instanceof PipeItem pipeItem)) {
                return false;
            }

            PipeNetworkType type = pipeItem.type;
            if (type == null) {
                PipeConnector.LOGGER.warn("MI compatibility: PipeItem.type is null");
                return false;
            }

            BlockEntity existingEntity = level.getBlockEntity(pos);

            // Case 1: Existing MI pipe block - add our pipe type to it
            if (existingEntity instanceof PipeBlockEntity pipeBlockEntity) {
                if (hasNetworkType(pipeBlockEntity, type)) {
                    return false; // Already has this pipe type
                }
                if (getPipeCount(pipeBlockEntity) >= MAX_PIPES_PER_BLOCK) {
                    return false; // Block is full (max 3 pipe types)
                }
                pipeBlockEntity.addPipe(type, pipeItem.defaultData.clone());
                level.blockUpdated(pos, Blocks.AIR);
                openFacesForInventories(pipeBlockEntity, type, player, pos);
                return true;
            }

            // Case 2: Non-voidable block - harvest drops first, then place
            if (!isVoidableBlock(level, pos)) {
                BlockState state = level.getBlockState(pos);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (level instanceof ServerLevel serverLevel) {
                    List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, heldPipeItem);
                    for (ItemStack drop : drops) {
                        if (!player.getInventory().add(drop)) {
                            player.drop(drop, false);
                        }
                    }
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.POOF,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            5, 0.1, 0.1, 0.1, 0.05
                    );
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.2f,
                            ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                }
                level.removeBlock(pos, false);
            }

            // Case 3: Place new MI pipe block
            Block pipeBlock = MIPipes.BLOCK_PIPE.get();
            if (pipeBlock == null) {
                PipeConnector.LOGGER.warn("MI compatibility: BLOCK_PIPE returned null during placement");
                return false;
            }

            boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
            BlockState blockState = pipeBlock.defaultBlockState()
                    .setValue(PipeBlock.WATERLOGGED, waterlogged);
            level.setBlock(pos, blockState, 3);

            BlockEntity newEntity = level.getBlockEntity(pos);
            if (newEntity instanceof PipeBlockEntity newPipeEntity) {
                newPipeEntity.addPipe(type, pipeItem.defaultData.clone());
                level.blockUpdated(pos, Blocks.AIR);
                openFacesForInventories(newPipeEntity, type, player, pos);
                return true;
            }

            PipeConnector.LOGGER.warn("MI compatibility: placed pipe block but entity is not PipeBlockEntity at {}", pos);
            return false;

        } catch (Exception e) {
            PipeConnector.LOGGER.warn("MI compatibility error in place at {}: {}", pos, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        try {
            if (!(placedItemStack.getItem() instanceof PipeItem pipeItem)) {
                return false;
            }
            PipeNetworkType type = pipeItem.type;
            if (type == null) {
                return false;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (level.getBlockState(pos).getBlock() instanceof PipeBlock && blockEntity instanceof PipeBlockEntity pipeBlockEntity) {
                return hasNetworkType(pipeBlockEntity, type);
            }
            return false;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("MI compatibility error in isBlockStateSpecificBlock at {}: {}", pos, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isPlacementAlreadySatisfied(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        return isBlockStateSpecificBlock(pos, specificBlock, placedItemStack, level);
    }

    @Override
    public boolean isPassableForPathfinding(BlockPos pos, Level level, ItemStack placedItemStack) {
        try {
            if (!(placedItemStack.getItem() instanceof PipeItem pipeItem)) {
                return false;
            }
            PipeNetworkType type = pipeItem.type;
            if (type == null) {
                return false;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (level.getBlockState(pos).getBlock() instanceof PipeBlock && blockEntity instanceof PipeBlockEntity pipeBlockEntity) {
                if (hasNetworkType(pipeBlockEntity, type)) {
                    return false;
                }
                // Max 3 pipe types per block (currently)
                return getPipeCount(pipeBlockEntity) < MAX_PIPES_PER_BLOCK;
            }
            return false;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("MI compatibility error in isPassableForPathfinding at {}: {}", pos, e.getMessage());
            return false;
        }
    }

    @Override
    public List<PipeRenderEntry> getPipeEntries(Level level, BlockPos pos) {
        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof PipeBlockEntity pipeBlockEntity)) {
                return List.of();
            }
            List<PipeRenderEntry> entries = new ArrayList<>();
            for (PipeNetworkType type : getNetworkTypes(pipeBlockEntity)) {
                entries.add(new PipeRenderEntry(type, type.getColor()));
            }
            return entries;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("MI compatibility error in getPipeEntries at {}: {}", pos, e.getMessage());
            return List.of();
        }
    }

    private Set<PipeNetworkType> getNetworkTypes(PipeBlockEntity entity) {
        Set<PipeNetworkType> types = new HashSet<>();
        for (PipeVoxelShape shape : entity.getPartShapes()) {
            types.add(shape.type);
        }
        if (types.isEmpty()) {
            for (PipeNetworkNode node : entity.getNodes()) {
                types.add(node.getType());
            }
        }
        return types;
    }

    private boolean hasNetworkType(PipeBlockEntity entity, PipeNetworkType type) {
        return getNetworkTypes(entity).contains(type);
    }

    private int getPipeCount(PipeBlockEntity entity) {
        return getNetworkTypes(entity).size();
    }

    // simulates wrench use
    private void openFacesForInventories(PipeBlockEntity pipeBlockEntity, PipeNetworkType type, Player player, BlockPos pos) {
        try {
            Level level = pipeBlockEntity.getLevel();
            if (level == null) return;
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                if (level.getBlockState(neighborPos).getBlock() instanceof PipeBlock) {
                    continue;
                }
                pipeBlockEntity.addConnection(player, type, dir);
            }
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("MI compatibility error in openFacesForInventories at {}: {}", pos, e.getMessage());
        }
    }
}

*/
