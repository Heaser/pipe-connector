package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.pipeconnectoritem.PipeConnectorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			PipeConnector.MODID);

	public static final RegistryObject<Item> PIPE_CONNECTOR = ITEMS.register("pipe_connector",
			() -> new PipeConnectorItem(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));
}