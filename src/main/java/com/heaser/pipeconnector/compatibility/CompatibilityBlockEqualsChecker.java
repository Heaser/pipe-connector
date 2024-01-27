package com.heaser.pipeconnector.compatibility;

import com.heaser.pipeconnector.compatibility.ae2.AE2Compatiblity;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.Map;

public class CompatibilityBlockEqualsChecker {
    private static CompatibilityBlockEqualsChecker INSTANCE;
    private final HashMap<Class<? extends Block>, IBlockEqualsChecker> classToCheckerMap = new HashMap<>();
    private CompatibilityBlockEqualsChecker() {
        if (isModLoaded("ae2")) {
            classToCheckerMap.put(AE2Compatiblity.getBlockToRegister(), new AE2Compatiblity());
        }
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

    public boolean defaultIsBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        return level.getBlockState(pos).getBlock().equals(specificBlock);
    }

    public boolean isBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        IBlockEqualsChecker checker = null;
        for (Map.Entry<Class<? extends Block>, IBlockEqualsChecker> set : classToCheckerMap.entrySet()) {
            if (set.getKey().isAssignableFrom(specificBlock.getClass())) {
                checker = set.getValue();
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
}
