package com.heaser.pipeconnector.compatibility;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import java.util.HashMap;
import java.util.Map;

public class CompatibilityBlockGetter {
    private static CompatibilityBlockGetter INSTANCE;
    private HashMap<Class<? extends Item>, IBlockGetter> classToGetterMap = new HashMap<>();
    private CompatibilityBlockGetter() {
        if (isModLoaded("ae2")) {
            classToGetterMap.put(AE2Compatiblity.getItemStackClassToRegister(), new AE2Compatiblity());
        }
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static CompatibilityBlockGetter getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CompatibilityBlockGetter();
        }

        return INSTANCE;
    }

    public Block defaultGetBlock(ItemStack placedStack) {
        return Block.byItem(placedStack.getItem());
    }

    public Block getBlock(ItemStack placedStack) {
        IBlockGetter getter = null;
        for (Map.Entry<Class<? extends Item>, IBlockGetter> set : classToGetterMap.entrySet()) {
            if (set.getKey().isAssignableFrom(placedStack.getItem().getClass())) {
                getter = set.getValue();
                break;
            }
        }
        if (getter != null) {
            return getter.getBlock(placedStack);
        }
        else {
            return defaultGetBlock(placedStack);
        }
    }
}
