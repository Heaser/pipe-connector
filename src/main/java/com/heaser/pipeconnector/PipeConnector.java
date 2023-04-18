package com.heaser.pipeconnector;

import com.heaser.pipeconnector.items.pipeconnectoritem.PipeConnectorItem;
import com.heaser.pipeconnector.items.pipeconnectoritem.utils.ClientEvents;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import com.heaser.pipeconnector.items.ModItems;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PipeConnector.MODID)
public class PipeConnector
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "pipe_connector";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
//    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
//    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
//    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
//    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);


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
