package com.heaser.pipeconnector;

import com.heaser.pipeconnector.client.ClientEvents;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.items.ModItems;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(PipeConnector.MODID)
public class PipeConnector {
    public static final String MODID = "pipe_connector";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PipeConnector(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);

        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, PipeConnectorConfig.SPEC);
        modEventBus.addListener(NetworkHandler::register);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ModItems());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientEvents());
    }
}
