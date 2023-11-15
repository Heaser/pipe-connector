package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			PipeConnector.MODID);

	public static final RegistryObject<Item> PIPE_CONNECTOR = ITEMS.register("pipe_connector",
			() -> new PipeConnectorItem(new Item.Properties().stacksTo(1)));

	@SubscribeEvent
	public static void buildContents(BuildCreativeModeTabContentsEvent event) {
		// Add to ingredients tab
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(PIPE_CONNECTOR);
		}
	}
}