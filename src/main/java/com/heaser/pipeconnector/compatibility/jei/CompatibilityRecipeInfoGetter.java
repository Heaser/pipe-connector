package com.heaser.pipeconnector.compatibility.jei;

import com.heaser.pipeconnector.compatibility.enderio.EnderIoCompatibility;
import com.heaser.pipeconnector.compatibility.interfaces.IRecipeInfoGetter;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class CompatibilityRecipeInfoGetter {
    private static CompatibilityRecipeInfoGetter INSTANCE;
    private static final HashMap<Class<? extends Item>, IRecipeInfoGetter> classToGetterMap = new HashMap<>();

    private CompatibilityRecipeInfoGetter() {
        if (isModLoaded("enderio_conduits")) {
            classToGetterMap.put(EnderIoCompatibility.getItemToRegister(), new EnderIoCompatibility());
        }
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static Stream<Holder<Item>> getSupportedPipeItems(Holder<Item> supportedBaseItem) {
        if (classToGetterMap.containsKey(supportedBaseItem.value().getClass())) {
            return classToGetterMap.get(supportedBaseItem.getClass()).getSupportedPipeItems(supportedBaseItem).stream();
        }
        else {
            ArrayList<Holder<Item>> result = new ArrayList<>();
            result.add(supportedBaseItem);
            return result.stream();
        }
    }
}
