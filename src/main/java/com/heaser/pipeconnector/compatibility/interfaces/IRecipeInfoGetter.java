package com.heaser.pipeconnector.compatibility.interfaces;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IRecipeInfoGetter {
    List<ItemStack> getSupportedPipeItems(ItemStack supportedBaseItem);
}
