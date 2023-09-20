package com.heaser.pipeconnector;

import com.heaser.pipeconnector.items.ModItems;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }
}
