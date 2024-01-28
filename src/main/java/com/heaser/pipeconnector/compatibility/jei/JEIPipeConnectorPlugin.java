package com.heaser.pipeconnector.compatibility.jei;


import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
        List<SupportedPipesRecipeInfo> recipes = SupportedPipesRecipeInfo.create();
        registration.addIngredientInfo(new ItemStack(ModItems.PIPE_CONNECTOR.get()), VanillaTypes.ITEM_STACK, Component.translatable("item.pipe_connector.jei.description") );
        registration.addRecipes(category.getRecipeType(), recipes);
    }
}
