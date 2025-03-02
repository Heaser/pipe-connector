package com.heaser.pipeconnector.compatibility.enderio;

import com.enderio.conduits.api.ConduitApi;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlock;
import com.enderio.conduits.common.init.ConduitComponents;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import com.heaser.pipeconnector.compatibility.interfaces.IRecipeInfoGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.api.Conduit;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;


import java.util.ArrayList;
import java.util.List;

public class EnderIoCompatibility implements IPlacer, IBlockEqualsChecker, IRecipeInfoGetter {
    static public Class<? extends Block> getBlockToRegister() {
        return ConduitBundleBlock.class;
    }
    static public Class<? extends Item> getItemToRegister() { return ConduitBlockItem.class; }

    // TODO: Fix conduits copying meta data when they shouldn't (when adding new conduits and not just when upgrading), They will will be connected to each other properly
    // TODO: Fix JEI support for conduits (currently shows <MISSING> Conduit)

    public boolean isBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Holder<Conduit<?>> conduit = placedItemStack.get(ConduitComponents.CONDUIT);
        if (conduit != null && level.getBlockState(pos).getBlock() instanceof ConduitBundleBlock && blockEntity instanceof ConduitBundleBlockEntity) {
            return ((ConduitBundleBlockEntity) blockEntity).hasType(conduit);
        }
        return false;
    }

    public boolean place(Level level, BlockPos pos, Player player, Item item, List<Direction> adjacentDirectionSides, ItemStack heldPipeItem) {
        Holder<Conduit<?>> conduit = heldPipeItem.get(ConduitComponents.CONDUIT);
        var existingEntity = level.getBlockEntity(pos);
        if (conduit != null && existingEntity instanceof ConduitBundleBlockEntity blockEntity) {
            if (blockEntity.hasType(conduit)) {
                return false;
            }
            BlockState blockState =  level.getBlockState(pos);
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, blockState));
            blockEntity.getBundle().addConduit(level, conduit, player);
            //blockEntity.addType(conduit, player);
            return true;
        }
        BlockState blockState = Block.byItem(item).defaultBlockState();
        level.setBlock(pos, blockState, 11);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, blockState));
        var entity = level.getBlockEntity(pos);
        if (conduit != null && entity instanceof ConduitBundleBlockEntity blockEntity) {
            blockEntity.addType(conduit, player);
        }
        return true;
    }

    public List<ItemStack> getSupportedPipeItems(ItemStack supportedBaseItem) {
        ArrayList<ItemStack> result = new ArrayList<>();
        // TODO: Fix this
        // Need to register the conduit types to show in JEI

//        Item item = supportedBaseItem.getItem();
//        if (item instanceof ConduitBlockItem conduitItem) {
//        EnderIOConduitsRegistries.Keys.CONDUIT.get.stream().map((conduitType -> {
//            ConduitApi.INSTANCE.getStackForType(0, conduitType);
//            ConduitBlockItem.getStackFor()
//         }));
//        }
        //baseConduitItem.ge
        //Conduit.DIRECT_CODEC.dispatchMap();
        //result.add(supportedBaseItem);



        //EnderIOConduitsRegistries.CONDUIT_TYPE.stream().map((conduitType -> )) //.byNameCodec().dispatch(Conduit::type, ConduitType::codec);

        return result;
    }
}