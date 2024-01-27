package com.heaser.pipeconnector.compatibility.jei;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.ModItems;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import com.heaser.pipeconnector.recipe.SupportedPipesRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;


public class SupportedPipesCategory implements IRecipeCategory<SupportedPipesRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(PipeConnector.MODID, "supported_pipes");
    public final static ResourceLocation TEXTURE = new ResourceLocation(PipeConnector.MODID, "textures/gui/jei_supported_pipes.png");

    static final RecipeType<SupportedPipesRecipe> RECIPE_TYPE = RecipeType.create(PipeConnector.MODID, "supported_pipes", SupportedPipesRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public SupportedPipesCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 160, 160);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.PIPE_CONNECTOR.get()));
    }

    @Override
    public RecipeType<SupportedPipesRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("item.pipe_connector.pipe_connector");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SupportedPipesRecipe recipe, IFocusGroup group) {

    }
}


