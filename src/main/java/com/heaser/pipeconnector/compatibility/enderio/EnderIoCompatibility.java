package com.heaser.pipeconnector.compatibility.enderio;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IMultiPipeColorProvider;
import com.heaser.pipeconnector.compatibility.interfaces.IPipeReplacer;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import com.heaser.pipeconnector.compatibility.interfaces.IRecipeInfoGetter;
import com.heaser.pipeconnector.compatibility.interfaces.PipeRenderEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.Nullable;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class EnderIoCompatibility implements IPlacer, IBlockEqualsChecker, IRecipeInfoGetter, IMultiPipeColorProvider, IPipeReplacer {
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
                                     Method getConduitItem,
                                     Method getConduits,
                                     Method hasConduitStrict,
                                     Method getCompatibleConduit,
                                     Class<?> upgradeResultClass,
                                     Method upgradeReplacedConduit) {
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

            Class<?> upgradeResultClass = Class.forName("com.enderio.enderio.api.conduits.bundle.AddConduitResult$Upgrade");

            Method hasCompatibleConduit = bundleInterface.getMethod("hasCompatibleConduit", Holder.class);
            Method canAddConduit = bundleInterface.getMethod("canAddConduit", Holder.class);
            Method addConduit = bundleInterface.getMethod("addConduit", Holder.class, Direction.class, Player.class);
            Method addResultHasChanged = addResultClass.getMethod("hasChanged");
            Method getConduitItem = conduitApiClass.getMethod("getConduitItem", Holder.class);
            Method getConduits = bundleInterface.getMethod("getConduits");
            Method hasConduitStrict = bundleInterface.getMethod("hasConduitStrict", Holder.class);
            Method getCompatibleConduit = bundleInterface.getMethod("getCompatibleConduit", Holder.class);
            Method upgradeReplacedConduit = upgradeResultClass.getMethod("replacedConduit");

            if (conduitComponent == null || conduitRegistryKey == null || conduitApiInstance == null
                    || hasCompatibleConduit.getReturnType() != boolean.class
                    || canAddConduit.getReturnType() != boolean.class
                    || addResultHasChanged.getReturnType() != boolean.class
                    || getConduitItem.getReturnType() != ItemStack.class
                    || getConduits.getReturnType() != List.class
                    || hasConduitStrict.getReturnType() != boolean.class
                    || getCompatibleConduit.getReturnType() != Holder.class
                    || upgradeReplacedConduit.getReturnType() != Holder.class
                    || !addResultClass.isAssignableFrom(upgradeResultClass)) {
                throw new NoSuchMethodException("Ender IO conduit API shape changed");
            }

            api = new ConduitApiHandles(bundleBlockClass, conduitItemClass, bundleInterface, conduitComponent,
                    conduitRegistryKey, conduitApiInstance, hasCompatibleConduit, canAddConduit, addConduit, addResultHasChanged, getConduitItem, getConduits,
                    hasConduitStrict, getCompatibleConduit, upgradeResultClass, upgradeReplacedConduit);
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

    @Override
    public List<PipeRenderEntry> getPipeEntries(Level level, BlockPos pos) {
        if (!isAvailable()) {
            return List.of();
        }
        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!isConduitBundle(blockEntity)) {
                return List.of();
            }
            List<?> conduits = (List<?>) api.getConduits().invoke(blockEntity);
            List<PipeRenderEntry> entries = new ArrayList<>(conduits.size());
            for (Object element : conduits) {
                if (!(element instanceof Holder<?> holder)) {
                    continue;
                }
                ResourceKey<?> key = holder.unwrapKey().orElse(null);
                if (key == null) {
                    continue;
                }
                entries.add(new PipeRenderEntry(key, conduitColor(key)));
            }
            return entries;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in getPipeEntries at {}", pos, e);
            return List.of();
        }
    }

    private static int conduitColor(ResourceKey<?> key) {
        String path = key.identifier().getPath();
        if (path.contains("redstone")) return 0xFFE53935;
        if (path.contains("energy")) return 0xFF4CAF50;
        if (path.contains("fluid")) return 0xFF2196F3;
        if (path.contains("item")) return 0xFFFFC107;

        int hash = key.identifier().toString().hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = (hash & 0x0000FF);
        if (r < 50) r += 100;
        if (g < 50) g += 100;
        if (b < 50) b += 100;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // IPipeReplacer: kind = the conduit Holder being swapped. Only that conduit is touched (mixed bundles keep
    // the rest), only upgrades no downgrades

    @Override
    @Nullable
    public Object resolveKind(Level level, BlockPos pos, ItemStack offhandStack) {
        if (!isAvailable()) {
            return null;
        }
        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!isConduitBundle(blockEntity)) {
                return null;
            }
            Object offhandConduit = getConduit(offhandStack);
            if (offhandConduit != null) {
                // Offhand conduit picks which conduit of a mixed bundle gets replaced
                Object compatible = getCompatibleConduit(blockEntity, offhandConduit);
                if (compatible != null) {
                    return compatible;
                }
            }
            List<?> conduits = (List<?>) api.getConduits().invoke(blockEntity);
            return conduits.size() == 1 ? conduits.getFirst() : null;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in resolveKind at {}", pos, e);
            return null;
        }
    }

    @Override
    @Nullable
    public Component getSeedRejectionReason(Level level, BlockPos pos, ItemStack offhandStack) {
        if (!isAvailable()) {
            return null;
        }
        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!isConduitBundle(blockEntity)) {
                return null;
            }
            List<?> conduits = (List<?>) api.getConduits().invoke(blockEntity);
            if (conduits.size() > 1) {
                return Component.translatable("item.pipe_connector.message.replaceEioAmbiguous");
            }
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in getSeedRejectionReason at {}", pos, e);
        }
        return null;
    }

    @Override
    public boolean matchesKind(Level level, BlockPos pos, Object kind) {
        if (!isAvailable()) {
            return false;
        }
        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            // Strict match so a tier boundary ends the path
            return isConduitBundle(blockEntity) && (boolean) api.hasConduitStrict().invoke(blockEntity, kind);
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in matchesKind at {}", pos, e);
            return false;
        }
    }

    @Override
    public String describeKind(Object kind) {
        if (kind instanceof Holder<?> holder) {
            ResourceKey<?> key = holder.unwrapKey().orElse(null);
            if (key != null) {
                return key.identifier().toString();
            }
        }
        return "enderio:unknown_conduit";
    }

    @Override
    public ItemStack getKindDisplayStack(Level level, BlockPos seedPos, Object kind) {
        if (!isAvailable()) {
            return ItemStack.EMPTY;
        }
        try {
            ItemStack stack = (ItemStack) api.getConduitItem().invoke(api.conduitApiInstance(), kind);
            return stack != null ? stack : ItemStack.EMPTY;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in getKindDisplayStack at {}", seedPos, e);
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean isSameFamily(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack) {
        if (!isAvailable()) {
            return false;
        }
        try {
            Object offhandConduit = getConduit(offhandStack);
            BlockEntity blockEntity = level.getBlockEntity(seedPos);
            if (offhandConduit == null || !isConduitBundle(blockEntity)) {
                return false;
            }
            return kind.equals(getCompatibleConduit(blockEntity, offhandConduit));
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in isSameFamily at {}", seedPos, e);
            return false;
        }
    }

    @Override
    public boolean isAlreadyTarget(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack) {
        return kind.equals(getConduit(offhandStack));
    }

    @Override
    @Nullable
    public Component getReplaceBlockedReason(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack) {
        if (!isAvailable()) {
            return Component.translatable("item.pipe_connector.message.replaceInvalidSeed");
        }
        try {
            Object offhandConduit = getConduit(offhandStack);
            BlockEntity blockEntity = level.getBlockEntity(seedPos);
            if (offhandConduit == null || !isConduitBundle(blockEntity)) {
                return null; // family check already rejects these
            }
            // canAddConduit is false unless the swap is a sanctioned upgrade (or the bundle is full)
            if (!canAddConduit(blockEntity, offhandConduit)) {
                return Component.translatable("item.pipe_connector.message.replaceEioDowngrade");
            }
            return null;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in getReplaceBlockedReason at {}", seedPos, e);
            return Component.translatable("item.pipe_connector.message.replaceInvalidSeed");
        }
    }

    @Override
    public boolean replaceAt(Level level, BlockPos pos, Player player, ItemStack offhandStack, Object kind, List<Direction> adjacentInRun) {
        if (!isAvailable()) {
            return false;
        }
        try {
            Object newConduit = getConduit(offhandStack);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (newConduit == null || !isConduitBundle(blockEntity)) {
                return false;
            }
            Direction direction = adjacentInRun.isEmpty() ? Direction.UP : adjacentInRun.getFirst();
            Object result = api.addConduit().invoke(blockEntity, newConduit, direction, player);
            if (result == null || !api.upgradeResultClass().isInstance(result)) {
                return false; // Blocked, nothing was changed
            }
            Object replacedConduit = api.upgradeReplacedConduit().invoke(result);
            ItemStack oldConduitItem = (ItemStack) api.getConduitItem().invoke(api.conduitApiInstance(), replacedConduit);
            if (oldConduitItem != null && !oldConduitItem.isEmpty()) {
                if (!player.getInventory().add(oldConduitItem)) {
                    player.drop(oldConduitItem, false);
                }
            }
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));
            return true;
        } catch (Exception e) {
            PipeConnector.LOGGER.warn("Ender IO compatibility error in replaceAt at {}", pos, e);
            return false;
        }
    }

    private static Object getCompatibleConduit(BlockEntity bundle, Object conduit) throws Exception {
        return api.getCompatibleConduit().invoke(bundle, conduit);
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
