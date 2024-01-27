package com.heaser.pipeconnector.compatibility.jei;


import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.ModItems;
import com.heaser.pipeconnector.recipe.SupportedPipesRecipe;
import com.heaser.pipeconnector.recipe.SupportedPipesRecipeInfo;
import com.refinedmods.refinedstorage.integration.jei.JeiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.library.runtime.JeiHelpers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

@JeiPlugin
public class JEIPipeConnectorPlugin implements IModPlugin {
    private SupportedPipesInfoCategory category;
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(PipeConnector.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        category = new SupportedPipesInfoCategory(guiHelper);
        registration.addRecipeCategories(category);

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ArrayList<SupportedPipesRecipeInfo> recipes = new ArrayList<SupportedPipesRecipeInfo>();
        recipes.add(new SupportedPipesRecipeInfo());
        registration.addRecipes(category.getRecipeType(), recipes);
        registration.addIngredientInfo(new ItemStack(ModItems.PIPE_CONNECTOR.get()), VanillaTypes.ITEM_STACK, Component.translatable("item.pipe_connector.jei.description") );
        //IModPlugin.super.registerRecipes(registration);

    }
}
