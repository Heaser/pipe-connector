package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.items.pipeconnectoritem.PipeConnectorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = PipeConnector.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			PipeConnector.MODID);

	public static final RegistryObject<Item> PIPE_CONNECTOR = ITEMS.register("pipe_connector",
			() -> new PipeConnectorItem(new Item.Properties().stacksTo(1).requiredFeatures()));

	@SubscribeEvent
	public void buildContents(CreativeModeTabEvent.BuildContents event) {
		if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(PIPE_CONNECTOR);
		}
	}
}