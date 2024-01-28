package com.heaser.pipeconnector.compatibility.jei;

import com.heaser.pipeconnector.constants.TagKeys;
import com.heaser.pipeconnector.items.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class SupportedPipesRecipeInfo {
    public final ItemStack inputIngredient;

    public final List<Item> supportedPipes;

    private SupportedPipesRecipeInfo(List<Item> supportedPipes) {
        inputIngredient = ModItems.PIPE_CONNECTOR.get().getDefaultInstance();
        this.supportedPipes = supportedPipes;
    }

    static public List<SupportedPipesRecipeInfo> create() {
        List recipes = new ArrayList<SupportedPipesRecipeInfo>();
        int maxSlotsPerRow = 9;
        int maxRows = 6;
        int slotsPerPage = maxSlotsPerRow * maxRows;
        List<Item> supportedPipes = ForgeRegistries.ITEMS.tags().getTag(TagKeys.PLACEABLE_ITEMS)
                .stream()
                .sorted(Comparator.comparing(item -> item.getDescriptionId()))
                .toList();

        int pagesNum = (int)Math.ceil((float) supportedPipes.size() / (float) slotsPerPage);

        for (int page = 0; page < pagesNum; page++) {
            List supportedPipesInPage = supportedPipes.subList(page * slotsPerPage, Math.min((page + 1) * slotsPerPage, supportedPipes.size()));
            SupportedPipesRecipeInfo recipePage = new SupportedPipesRecipeInfo(supportedPipesInPage);
            recipes.add(recipePage);
        }

        return recipes;
    }
}
