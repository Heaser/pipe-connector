package com.heaser.pipeconnector.compatibility;

import com.heaser.pipeconnector.compatibility.ae2.AE2Compatiblity;
// Disabled (mod not on this MC version):
// import com.heaser.pipeconnector.compatibility.mi.MICompatibility;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import java.util.HashMap;
import java.util.Map;

public class CompatibilityBlockGetter {
    private static CompatibilityBlockGetter INSTANCE;
    private final HashMap<Class<? extends Item>, IBlockGetter> classToGetterMap = new HashMap<>();
    private CompatibilityBlockGetter() {
        if (isModLoaded("ae2")) {
            classToGetterMap.put(AE2Compatiblity.getItemStackClassToRegister(), new AE2Compatiblity());
        }
        // if (isModLoaded("modern_industrialization")) {
        //     classToGetterMap.put(MICompatibility.getItemToRegister(), new MICompatibility());
        // }
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
        for (Map.Entry<Class<? extends Item>, IBlockGetter> entry : classToGetterMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(placedStack.getItem().getClass())) {
                getter = entry.getValue();
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
