package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PipeConnector.MODID);
	public static final DeferredItem<PipeConnectorItem> PIPE_CONNECTOR = ITEMS.registerItem("pipe_connector",
			properties -> new PipeConnectorItem(properties.stacksTo(1)));

	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}
