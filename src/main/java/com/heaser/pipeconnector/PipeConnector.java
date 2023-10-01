package com.heaser.pipeconnector;

import com.heaser.pipeconnector.client.ClientEvents;
import com.heaser.pipeconnector.items.ModItems;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.server.ServerEvents;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(PipeConnector.MODID)
public class PipeConnector {
    public static final String MODID = "pipe_connector";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PipeConnector() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::serverSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
        //MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    private void serverSetup(final FMLDedicatedServerSetupEvent event) {
        // MinecraftForge.EVENT_BUS.register(new ServerEvents());
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, ServerEvents::onChangeHeldItem);
    }
}
