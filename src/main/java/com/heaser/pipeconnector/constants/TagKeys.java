package com.heaser.pipeconnector.constants;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.heaser.pipeconnector.PipeConnector.MODID;

public class TagKeys {

    public static final TagKey<Item> PLACEABLE_ITEMS = ItemTags.create(new ResourceLocation(MODID, "placeable_items"));
    public static final TagKey<Block> VOIDABLE_BLOCKS = BlockTags.create(new ResourceLocation(MODID, "voidable_blocks"));
    public static final TagKey<Block> UNBREAKABLE_BLOCKS = BlockTags.create(new ResourceLocation(MODID, "unbreakable_blocks"));
}
