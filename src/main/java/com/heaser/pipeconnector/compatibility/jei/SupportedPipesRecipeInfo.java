package com.heaser.pipeconnector.compatibility.jei;

import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class SupportedPipesRecipeInfo {
    public final ItemStack inputIngredient;

    public final List<ItemStack> supportedPipes;

    private SupportedPipesRecipeInfo(List<ItemStack> supportedPipes) {
        inputIngredient = ModItems.PIPE_CONNECTOR.get().getDefaultInstance();
        this.supportedPipes = supportedPipes;
    }

    static public List<SupportedPipesRecipeInfo> create() {
        List recipes = new ArrayList<SupportedPipesRecipeInfo>();
        int maxSlotsPerRow = 9;
        int maxRows = 6;
        int slotsPerPage = maxSlotsPerRow * maxRows;
        List<Holder<Item>> supportedPipesHolders = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(TagKeys.PLACEABLE_ITEMS).forEach(supportedPipesHolders::add);
        CompatibilityRecipeInfoGetter instance = CompatibilityRecipeInfoGetter.getInstance();
        List<ItemStack> supportedPipes = supportedPipesHolders.stream()
                .map(( Holder<Item> item) -> new ItemStack(item))
                .flatMap(instance::getSupportedPipeItems)
                .sorted(Comparator.comparing((ItemStack itemStack) -> itemStack.getItem().getDescriptionId())).toList();

        int pagesNum = (int)Math.ceil((float) supportedPipes.size() / (float) slotsPerPage);

        for (int page = 0; page < pagesNum; page++) {
            List supportedPipesInPage = supportedPipes.subList(page * slotsPerPage, Math.min((page + 1) * slotsPerPage, supportedPipes.size()));
            SupportedPipesRecipeInfo recipePage = new SupportedPipesRecipeInfo(supportedPipesInPage);
            recipes.add(recipePage);
        }

        return recipes;
    }
}
