package com.heaser.pipeconnector.compatibility.enderio;

import com.enderio.conduits.api.ConduitApi;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.RightClickAction;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlock;
import com.enderio.conduits.common.init.ConduitComponents;
import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import com.heaser.pipeconnector.compatibility.interfaces.IRecipeInfoGetter;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
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


import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            RightClickAction action  = blockEntity.addType(conduit, player);
            PipeConnector.LOGGER.debug("Action: " + action);
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

        RegistryAccess registries = Minecraft.getInstance().getConnection().getLevel().registryAccess();
        ResourceKey key = EnderIOConduitsRegistries.Keys.CONDUIT;

        MappedRegistry<Conduit<?>> conduitRegistry = (MappedRegistry<Conduit<?>>)registries.registry(key).get();
        ;
        for (Map.Entry<ResourceKey<Conduit<?>>, Conduit<?>> entry : conduitRegistry.entrySet()) {
            Holder<Conduit<?>> conduit = conduitRegistry.getHolder(entry.getKey()).get();
            ItemStack itemStack = ConduitApi.INSTANCE.getStackForType(conduit);
            result.add(itemStack);
        }
        return result;
    }
}