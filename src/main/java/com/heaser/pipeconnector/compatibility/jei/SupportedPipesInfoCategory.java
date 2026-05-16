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
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class SupportedPipesInfoCategory implements IRecipeCategory<SupportedPipesRecipeInfo> {
    public final static Identifier UID = Identifier.fromNamespaceAndPath(PipeConnector.MODID, "supported_pipes");
    public final static Identifier TEXTURE = Identifier.fromNamespaceAndPath(PipeConnector.MODID, "textures/gui/jei_supported_pipes.png");

    static final IRecipeType<SupportedPipesRecipeInfo> RECIPE_TYPE = IRecipeType.create(PipeConnector.MODID, "supported_pipes", SupportedPipesRecipeInfo.class);

    private static final int CATEGORY_WIDTH = 160;
    private static final int CATEGORY_HEIGHT = 125;

    private final IDrawable slotBackground;
    private final IDrawable icon;


    public SupportedPipesInfoCategory(IGuiHelper helper) {
        this.slotBackground = helper.getSlotDrawable();
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.PIPE_CONNECTOR.get()));
    }

    @Override
    public IRecipeType<SupportedPipesRecipeInfo> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("item.pipe_connector.jei.supportedPipesInfo");
    }

    @Override
    public int getWidth() {
        return CATEGORY_WIDTH;
    }

    @Override
    public int getHeight() {
        return CATEGORY_HEIGHT;
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

        for (ItemStack itemStack : recipe.supportedPipes) {

            //Item item = itemStack.value().getItem();
            builder.addSlot(RecipeIngredientRole.OUTPUT, startPosWidth, startPosHeight).setBackground(slotBackground, -1, -1).add(VanillaTypes.ITEM_STACK, itemStack);
            startPosWidth += 18;
            if (startPosWidth > 161) {
                startPosHeight += 18;
                startPosWidth = 0;
            }
        }
    }

    private static <T> void addIngredient(IIngredientType<T> type, T ingredient, IIngredientAcceptor<?> slotBuilder) {
        slotBuilder.add(type, ingredient);
    }
}