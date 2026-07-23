package com.heaser.pipeconnector.compatibility;

import com.heaser.pipeconnector.compatibility.ae2.AE2Compatiblity;
import com.heaser.pipeconnector.compatibility.enderio.EnderIoCompatibility;
import com.heaser.pipeconnector.compatibility.interfaces.IPipeReplacer;
import com.heaser.pipeconnector.constants.TagKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompatibilityPipeReplacer {
    private static CompatibilityPipeReplacer INSTANCE;
    private static final HashMap<Class<? extends Block>, IPipeReplacer> classToReplacerMap = new HashMap<>();
    private static final IPipeReplacer DEFAULT = new DefaultPipeReplacer();

    private CompatibilityPipeReplacer() {
        if (isModLoaded("ae2")) {
            classToReplacerMap.put(AE2Compatiblity.getBlockToRegister(), new AE2Compatiblity());
        }
        if (isModLoaded("enderio") && EnderIoCompatibility.isAvailable()) {
            classToReplacerMap.put(EnderIoCompatibility.getBlockToRegister(), new EnderIoCompatibility());
        }
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static CompatibilityPipeReplacer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CompatibilityPipeReplacer();
        }

        return INSTANCE;
    }

    public static IPipeReplacer getReplacerFor(Level level, BlockPos pos) {
        getInstance();
        Block currentBlock = level.getBlockState(pos).getBlock();
        for (Map.Entry<Class<? extends Block>, IPipeReplacer> entry : classToReplacerMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(currentBlock.getClass())) {
                return entry.getValue();
            }
        }
        return DEFAULT;
    }

    @Nullable
    public static Object resolveKind(Level level, BlockPos pos, ItemStack offhandStack) {
        return getReplacerFor(level, pos).resolveKind(level, pos, offhandStack);
    }

    private static class DefaultPipeReplacer implements IPipeReplacer {
        @Override
        @Nullable
        public Object resolveKind(Level level, BlockPos pos, ItemStack offhandStack) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(TagKeys.PIPE_BLOCK)) {
                return null;
            }
            return state.getBlock();
        }

        @Override
        public boolean matchesKind(Level level, BlockPos pos, Object kind) {
            return level.getBlockState(pos).getBlock() == kind;
        }

        @Override
        public String describeKind(Object kind) {
            return BuiltInRegistries.BLOCK.getKey((Block) kind).toString();
        }

        @Override
        public ItemStack getKindDisplayStack(Level level, BlockPos seedPos, Object kind) {
            return new ItemStack((Block) kind);
        }

        @Override
        public boolean isSameFamily(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack) {
            Block offhandBlock = CompatibilityBlockGetter.getInstance().getBlock(offhandStack);
            String kindNamespace = BuiltInRegistries.BLOCK.getKey((Block) kind).getNamespace();
            String offhandNamespace = BuiltInRegistries.BLOCK.getKey(offhandBlock).getNamespace();
            return kindNamespace.equals(offhandNamespace);
        }

        @Override
        public boolean isAlreadyTarget(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack) {
            return CompatibilityBlockGetter.getInstance().getBlock(offhandStack) == kind;
        }

        @Override
        public boolean replaceAt(Level level, BlockPos pos, Player player, ItemStack offhandStack, Object kind, List<Direction> adjacentInRun) {
            return CompatibilityPlacer.getInstance().place(level, pos, player, offhandStack, adjacentInRun);
        }
    }
}
