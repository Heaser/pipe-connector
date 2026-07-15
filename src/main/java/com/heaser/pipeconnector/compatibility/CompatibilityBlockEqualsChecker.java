package com.heaser.pipeconnector.compatibility;

import com.heaser.pipeconnector.compatibility.ae2.AE2Compatiblity;
import com.heaser.pipeconnector.compatibility.enderio.EnderIoCompatibility;
// Disabled (mod not on this MC version):
// import com.heaser.pipeconnector.compatibility.mi.MICompatibility;
import com.heaser.pipeconnector.compatibility.interfaces.IColorProvider;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IMultiPipeColorProvider;
import com.heaser.pipeconnector.compatibility.interfaces.PipeRenderEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompatibilityBlockEqualsChecker {
    private static CompatibilityBlockEqualsChecker INSTANCE;
    private static final HashMap<Class<? extends Block>, IBlockEqualsChecker> classToCheckerMap = new HashMap<>();
    private CompatibilityBlockEqualsChecker() {
        if (isModLoaded("ae2")) {
            classToCheckerMap.put(AE2Compatiblity.getBlockToRegister(), new AE2Compatiblity());
        }
        if (isModLoaded("enderio") && EnderIoCompatibility.isAvailable()) {
            classToCheckerMap.put(EnderIoCompatibility.getBlockToRegister(), new EnderIoCompatibility());
        }
        // if (isModLoaded("modern_industrialization")) {
        //     classToCheckerMap.put(MICompatibility.getBlockToRegister(), new MICompatibility());
        // }
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static CompatibilityBlockEqualsChecker getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CompatibilityBlockEqualsChecker();
        }

        return INSTANCE;
    }

    @Nullable
    public static Integer getColor(BlockPos pos, Level level) {
        getInstance();
        IBlockEqualsChecker checker = null;
        Block currentBlock = level.getBlockState(pos).getBlock();
        for (Map.Entry<Class<? extends Block>, IBlockEqualsChecker> entry : classToCheckerMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(currentBlock.getClass())) {
                checker = entry.getValue();
                break;
            }
        }
        if (checker instanceof IColorProvider colorProvider) {
            return colorProvider.getColor(level, pos);
        }
        return null;
    }

    public static List<PipeRenderEntry> getPipeRenderEntries(BlockPos pos, Level level) {
        getInstance();
        IBlockEqualsChecker checker = null;
        Block currentBlock = level.getBlockState(pos).getBlock();
        for (Map.Entry<Class<? extends Block>, IBlockEqualsChecker> entry : classToCheckerMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(currentBlock.getClass())) {
                checker = entry.getValue();
                break;
            }
        }
        if (checker instanceof IMultiPipeColorProvider multi) {
            return multi.getPipeEntries(level, pos);
        }
        if (checker instanceof IColorProvider colorProvider) {
            Integer color = colorProvider.getColor(level, pos);
            if (color != null) {
                return List.of(new PipeRenderEntry(color, color));
            }
        }
        return List.of();
    }

    public static boolean isSupportedBlock(BlockState state) {
        getInstance();
        Block block = state.getBlock();
        for (Class<? extends Block> supportedClass : classToCheckerMap.keySet()) {
            if (supportedClass.isAssignableFrom(block.getClass())) {
                return true;
            }
        }
        return false;
    }

    public static boolean defaultIsBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        return level.getBlockState(pos).getBlock().equals(specificBlock);
    }

    public static boolean isBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        getInstance();
        IBlockEqualsChecker checker = null;
        for (Map.Entry<Class<? extends Block>, IBlockEqualsChecker> entry : classToCheckerMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(specificBlock.getClass())) {
                checker = entry.getValue();
                break;
            }
        }
        if (checker != null) {
            return checker.isBlockStateSpecificBlock(pos, specificBlock, placedItemStack, level);
        }
        else {
            return defaultIsBlockStateSpecificBlock(pos, specificBlock, placedItemStack, level);
        }
    }

    public static boolean isPlacementAlreadySatisfied(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        getInstance();
        IBlockEqualsChecker checker = null;
        for (Map.Entry<Class<? extends Block>, IBlockEqualsChecker> entry : classToCheckerMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(specificBlock.getClass())) {
                checker = entry.getValue();
                break;
            }
        }
        if (checker != null) {
            return checker.isPlacementAlreadySatisfied(pos, specificBlock, placedItemStack, level);
        } else {
            return defaultIsBlockStateSpecificBlock(pos, specificBlock, placedItemStack, level);
        }
    }

    public static boolean isPassableForPathfinding(BlockPos pos, Level level, ItemStack placedItemStack) {
        getInstance();
        IBlockEqualsChecker checker = null;
        Block currentBlock = level.getBlockState(pos).getBlock();
        for (Map.Entry<Class<? extends Block>, IBlockEqualsChecker> entry : classToCheckerMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(currentBlock.getClass())) {
                checker = entry.getValue();
                break;
            }
        }
        if (checker != null) {
            return checker.isPassableForPathfinding(pos, level, placedItemStack);
        }
        return false;
    }
}
