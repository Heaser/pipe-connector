package com.heaser.pipeconnector.compatibility.enderio;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import com.heaser.pipeconnector.compatibility.interfaces.IRecipeInfoGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class EnderIoCompatibility implements IPlacer, IBlockEqualsChecker, IRecipeInfoGetter {
    // Reflections this time
    private record ConduitApiHandles(Class<? extends Block> bundleBlockClass,
                                     Class<? extends Item> conduitItemClass,
                                     Class<?> bundleInterface,
                                     DataComponentType<?> conduitComponent,
                                     ResourceKey<Registry<Object>> conduitRegistryKey,
                                     Object conduitApiInstance,
                                     Method hasCompatibleConduit,
                                     Method canAddConduit,
                                     Method addConduit,
                                     Method addResultHasChanged,
                                     Method getConduitItem) {
    }

    private static ConduitApiHandles api;
    private static boolean resolveAttempted = false;

    public static synchronized boolean isAvailable() {
        resolve();
        return api != null;
    }

    static public Class<? extends Block> getBlockToRegister() {
        return api.bundleBlockClass();
    }

    static public Class<? extends Item> getItemToRegister() {
        return api.conduitItemClass();
    }

    @SuppressWarnings("unchecked")
    private static void resolve() {
        if (resolveAttempted) {
            return;
        }
        resolveAttempted = true;
        if (!ModList.get().isLoaded("enderio")) {
            return;
        }
        try {
            Class<? extends Block> bundleBlockClass = Class.forName("com.enderio.enderio.content.conduits.bundle.ConduitBundleBlock").asSubclass(Block.class);
            Class<? extends Item> conduitItemClass = Class.forName("com.enderio.enderio.content.conduits.ConduitBlockItem").asSubclass(Item.class);
            Class<?> bundleInterface = Class.forName("com.enderio.enderio.api.conduits.bundle.ConduitBundle");
            Class<?> addResultClass = Class.forName("com.enderio.enderio.api.conduits.bundle.AddConduitResult");
            Class<?> dataComponentsClass = Class.forName("com.enderio.enderio.api.EnderIODataComponents");
            Class<?> registryKeysClass = Class.forName("com.enderio.enderio.api.EnderIORegistries$Keys");
            Class<?> conduitApiClass = Class.forName("com.enderio.enderio.api.conduits.ConduitApi");

            DataComponentType<?> conduitComponent = (DataComponentType<?>) dataComponentsClass.getField("CONDUIT").get(null);
            ResourceKey<Registry<Object>> conduitRegistryKey = (ResourceKey<Registry<Object>>) registryKeysClass.getField("CONDUIT").get(null);
            Object conduitApiInstance = conduitApiClass.getField("INSTANCE").get(null);

            Method hasCompatibleConduit = bundleInterface.getMethod("hasCompatibleConduit", Holder.class);
            Method canAddConduit = bundleInterface.getMethod("canAddConduit", Holder.class);
            Method addConduit = bundleInterface.getMethod("addConduit", Holder.class, Direction.class, Player.class);
            Method addResultHasChanged = addResultClass.getMethod("hasChanged");
            Method getConduitItem = conduitApiClass.getMethod("getConduitItem", Holder.class);

            if (conduitComponent == null || conduitRegistryKey == null || conduitApiInstance == null
                    || hasCompatibleConduit.getReturnType() != boolean.class
                    || canAddConduit.getReturnType() != boolean.class
                    || addResultHasChanged.getReturnType() != boolean.class
                    || getConduitItem.getReturnType() != ItemStack.class) {
                throw new NoSuchMethodException("Ender IO conduit API shape changed");
            }

            api = new ConduitApiHandles(bundleBlockClass, conduitItemClass, bundleInterface, conduitComponent,
                    conduitRegistryKey, conduitApiInstance, hasCompatibleConduit, canAddConduit, addConduit, addResultHasChanged, getConduitItem);
            PipeConnector.LOGGER.info("Resolved Ender IO conduit API, conduit support enabled.");
        } catch (Throwable t) {
            PipeConnector.LOGGER.warn("Could not resolve Ender IO conduit API, conduit support disabled. Their conduit API likely changed again.", t);
        }
    }

    @Override
    public boolean place(Level level, BlockPos pos, Player player, Item item, List<Direction> adjacentDirectionSides, ItemStack heldPipeItem) {
        if (!isAvailable()) {
            return false;
        }
        try {
            Object conduit = getConduit(heldPipeItem);
            if (conduit == null) {
                return false;
            }
            Direction direction = adjacentDirectionSides.isEmpty() ? Direction.UP : adjacentDirectionSides.getFirst();

            BlockEntity existingEntity = level.getBlockEntity(pos);
            if (isConduitBundle(existingEntity)) {
                if (hasCompatibleConduit(existingEntity, conduit)) {
                    return false; // Already has this conduit type, don't tier-upgrade it
                }
                if (!canAddConduit(existingEntity, conduit)) {
                    return false; // Bundle is full or the type doesn't fit
                }
                if (!addConduit(existingEntity, conduit, direction, player)) {
                    return false;
                }
                level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));
                return true;
            }

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
                    // Animation and sound
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                }
                level.removeBlock(pos, false);
            }

            BlockState blockState = Block.byItem(item).defaultBlockState();
            level.setBlock(pos, blockState, 11);
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, blockState));
            BlockEntity newEntity = level.getBlockEntity(pos);
            if (!isConduitBundle(newEntity)) {
                PipeConnector.LOGGER.warn("Ender IO compatibility: placed conduit block but entity is not a bundle at {}", pos);
                return false;
            }
            return addConduit(newEntity, conduit, direction, player);
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in place at {}", pos, e);
            return false;
        }
    }

    @Override
    public boolean isBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        if (!isAvailable()) {
            return false;
        }
        try {
            Object conduit = getConduit(placedItemStack);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (conduit == null || !isConduitBundle(blockEntity)) {
                return false;
            }
            return hasCompatibleConduit(blockEntity, conduit);
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in isBlockStateSpecificBlock at {}", pos, e);
            return false;
        }
    }

    @Override
    public boolean isPassableForPathfinding(BlockPos pos, Level level, ItemStack placedItemStack) {
        if (!isAvailable()) {
            return false;
        }
        try {
            Object conduit = getConduit(placedItemStack);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (conduit == null || !isConduitBundle(blockEntity)) {
                return false;
            }
            if (hasCompatibleConduit(blockEntity, conduit)) {
                return false;
            }
            // Only route through a bundle that still has room for  conduit
            return canAddConduit(blockEntity, conduit);
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in isPassableForPathfinding at {}", pos, e);
            return false;
        }
    }

    @Override
    public List<ItemStack> getSupportedPipeItems(ItemStack supportedBaseItem) {
        List<ItemStack> fallback = List.of(supportedBaseItem);
        if (!isAvailable() || Minecraft.getInstance().level == null) {
            return fallback;
        }
        try {
            var conduitRegistry = Minecraft.getInstance().level.registryAccess().lookup(api.conduitRegistryKey());
            if (conduitRegistry.isEmpty()) {
                return fallback;
            }
            List<ItemStack> result = new ArrayList<>();
            for (Holder<Object> conduit : conduitRegistry.get().listElements().toList()) {
                ItemStack itemStack = (ItemStack) api.getConduitItem().invoke(api.conduitApiInstance(), conduit);
                if (itemStack != null && !itemStack.isEmpty()) {
                    result.add(itemStack);
                }
            }
            return result.isEmpty() ? fallback : result;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in getSupportedPipeItems", e);
            return fallback;
        }
    }

    private static Object getConduit(ItemStack stack) {
        return stack.get(api.conduitComponent());
    }

    private static boolean isConduitBundle(BlockEntity blockEntity) {
        return api.bundleInterface().isInstance(blockEntity);
    }

    private static boolean hasCompatibleConduit(BlockEntity bundle, Object conduit) throws Exception {
        return (boolean) api.hasCompatibleConduit().invoke(bundle, conduit);
    }

    private static boolean canAddConduit(BlockEntity bundle, Object conduit) throws Exception {
        return (boolean) api.canAddConduit().invoke(bundle, conduit);
    }

    private static boolean addConduit(BlockEntity bundle, Object conduit, Direction direction, Player player) throws Exception {
        Object result = api.addConduit().invoke(bundle, conduit, direction, player);
        PipeConnector.LOGGER.debug("Ender IO addConduit result: {}", result);
        return result == null || (boolean) api.addResultHasChanged().invoke(result);
    }
}
