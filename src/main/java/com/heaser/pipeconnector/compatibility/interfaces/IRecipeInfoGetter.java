package com.heaser.pipeconnector.compatibility.interfaces;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import java.util.List;

public interface IRecipeInfoGetter {
    List<Holder<Item>> getSupportedPipeItems(Holder<Item> supportedBaseItem);
}
