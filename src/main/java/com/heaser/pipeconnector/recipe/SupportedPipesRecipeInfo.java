package com.heaser.pipeconnector.recipe;

import com.heaser.pipeconnector.items.ModItems;
import net.minecraft.world.item.ItemStack;


public class SupportedPipesRecipeInfo {
    public final ItemStack inputIngredient;

    public SupportedPipesRecipeInfo() {
        inputIngredient = ModItems.PIPE_CONNECTOR.get().getDefaultInstance();
    }
}
