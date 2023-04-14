package com.heaser.pipeconnector.constants;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;

public class TagKeys {
    public static final TagKey<Item> PIPEZ_ENERGY_PIPE = ItemTags.create(new ResourceLocation("pipez", "energy_pipe"));
    public static final TagKey<Item> PIPEZ_FLUID_PIPE = ItemTags.create(new ResourceLocation("pipez", "fluid_pipe"));
    public static final TagKey<Item> PIPEZ_ITEM_PIPE = ItemTags.create(new ResourceLocation("pipez", "item_pipe"));
    public static final TagKey<Item> PIPEZ_UNIVERSAL_PIPE = ItemTags.create(new ResourceLocation("pipez", "universal_pipe"));
}
