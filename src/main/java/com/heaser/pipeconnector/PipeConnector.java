package com.heaser.pipeconnector;

import com.heaser.pipeconnector.items.ModItems;
import com.heaser.pipeconnector.items.pipeconnectoritem.utils.ClientEvents;
import com.heaser.pipeconnector.network.NetworkHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PipeConnector.MODID)
public class PipeConnector
{
    public static final String MODID = "pipe_connector";

    public PipeConnector()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        // Register the item to a creative tab
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        

    }


    private void commonSetup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.register();

    }

    private void addCreative(CreativeModeTabEvent.BuildContents event)
    {
        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES)
            event.accept(ModItems.PIPE_CONNECTOR.get());
    }
}
