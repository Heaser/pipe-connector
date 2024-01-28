package com.heaser.pipeconnector.compatibility.jei;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;

public class SupportedPipesInfoCategory implements IRecipeCategory<SupportedPipesRecipeInfo> {
    public final static ResourceLocation UID = new ResourceLocation(PipeConnector.MODID, "supported_pipes");
    public final static ResourceLocation TEXTURE = new ResourceLocation(PipeConnector.MODID, "textures/gui/jei_supported_pipes.png");

    static final RecipeType<SupportedPipesRecipeInfo> RECIPE_TYPE = RecipeType.create(PipeConnector.MODID, "supported_pipes", SupportedPipesRecipeInfo.class);

    private final IDrawable background;
    private final IDrawable slotBackground;
    private final IDrawable icon;


    public SupportedPipesInfoCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(160, 125);
        this.slotBackground = helper.getSlotDrawable();
        //this.background = helper.createDrawable(TEXTURE, 0, 0, 229, 173);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.PIPE_CONNECTOR.get()));
    }

    @Override
    public RecipeType<SupportedPipesRecipeInfo> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("item.pipe_connector.jei.supportedPipesInfo");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SupportedPipesRecipeInfo recipe, IFocusGroup group) {
        int xPos = 72;
        int startPosWidth = 0;
        int startPosHeight = 20;

        IRecipeSlotBuilder inputSlotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, xPos, 1).setBackground(slotBackground, -1, -1);
        addIngredient(VanillaTypes.ITEM_STACK, recipe.inputIngredient, inputSlotBuilder);

        for (Item item : recipe.supportedPipes) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, startPosWidth, startPosHeight).setBackground(slotBackground, -1, -1).addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item));
            startPosWidth += 18;
            if (startPosWidth > 161) {
                startPosHeight += 18;
                startPosWidth = 0;
            }
        }
    }

    private static <T> void addIngredient(IIngredientType<T> type, T ingredient, IIngredientAcceptor<?> slotBuilder) {
        slotBuilder.addIngredient(type, ingredient);
    }
}