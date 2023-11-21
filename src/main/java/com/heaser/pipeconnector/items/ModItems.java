package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			PipeConnector.MODID);

	public static final RegistryObject<Item> PIPE_CONNECTOR = ITEMS.register("pipe_connector",
			() -> new PipeConnectorItem(new Item.Properties().stacksTo(1)));



}