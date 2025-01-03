package com.heaser.pipeconnector.compatibility.jei;

import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public class SupportedPipesRecipeInfo {
    public final ItemStack inputIngredient;

    public final List<Holder<Item>> supportedPipes;

    private SupportedPipesRecipeInfo(List<Holder<Item>> supportedPipes) {
        inputIngredient = ModItems.PIPE_CONNECTOR.get().getDefaultInstance();
        this.supportedPipes = supportedPipes;
    }

    static public List<SupportedPipesRecipeInfo> create() {
        List recipes = new ArrayList<SupportedPipesRecipeInfo>();
        int maxSlotsPerRow = 9;
        int maxRows = 6;
        int slotsPerPage = maxSlotsPerRow * maxRows;
        HolderSet.Named<Item> supportedPipesHolderSet = BuiltInRegistries.ITEM.getTag(TagKeys.PLACEABLE_ITEMS).get();
        List<Holder<Item>> supportedPipes = supportedPipesHolderSet.stream().flatMap(CompatibilityRecipeInfoGetter::getSupportedPipeItems).sorted(Comparator.comparing((Holder<Item> item) ->
                item.value().getDescriptionId())).toList();

        int pagesNum = (int)Math.ceil((float) supportedPipes.size() / (float) slotsPerPage);

        for (int page = 0; page < pagesNum; page++) {
            List supportedPipesInPage = supportedPipes.subList(page * slotsPerPage, Math.min((page + 1) * slotsPerPage, supportedPipes.size()));
            SupportedPipesRecipeInfo recipePage = new SupportedPipesRecipeInfo(supportedPipesInPage);
            recipes.add(recipePage);
        }

        return recipes;
    }
}
