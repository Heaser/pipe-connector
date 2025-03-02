package com.heaser.pipeconnector.compatibility.jei;
import com.heaser.pipeconnector.compatibility.enderio.EnderIoCompatibility;
import com.heaser.pipeconnector.compatibility.interfaces.IRecipeInfoGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CompatibilityRecipeInfoGetter {
    private static CompatibilityRecipeInfoGetter INSTANCE;
    private static final HashMap<Class<? extends Item>, IRecipeInfoGetter> classToGetterMap = new HashMap<>();

    private CompatibilityRecipeInfoGetter() {
        if (isModLoaded("enderio_conduits")) {
            classToGetterMap.put(EnderIoCompatibility.getItemToRegister(), new EnderIoCompatibility());
        }
    }

    public static CompatibilityRecipeInfoGetter getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CompatibilityRecipeInfoGetter();
        }

        return INSTANCE;
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public Stream<ItemStack> getSupportedPipeItems(ItemStack supportedBaseItemStack) {
        for (Map.Entry<Class<? extends Item>, IRecipeInfoGetter> set : classToGetterMap.entrySet()) {
            Item supportedBaseItem = supportedBaseItemStack.getItem();
            if (set.getKey().isAssignableFrom(supportedBaseItem.getClass())) {
                return set.getValue().getSupportedPipeItems(supportedBaseItemStack).stream();
            }
        }
        ArrayList<ItemStack> result = new ArrayList<>();
        result.add(supportedBaseItemStack);
        return result.stream();
    }
}
