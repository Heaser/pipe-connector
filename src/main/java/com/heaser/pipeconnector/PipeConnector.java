package com.heaser.pipeconnector;

import com.heaser.pipeconnector.items.ModItems;
import com.heaser.pipeconnector.items.pipeconnectoritem.utils.ClientEvents;
import com.heaser.pipeconnector.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PipeConnector.MODID)
public class PipeConnector {
    public static final String MODID = "pipe_connector";

    public PipeConnector() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        //Register this class to the Event Bus, then we don't need to do crazy event subscriptions.
        MinecraftForge.EVENT_BUS.register(this);

        // This constructor is already registered and ran when the registries exist, we don't need another method to do so.
        NetworkHandler.register();

        // We are using a deferred register, so we can just directly register the registry we make instead of whatever mess
        // there was before with handing off the mod bus all over the place.
        ModItems.ITEMS.register(modEventBus);
    }

    // Client things should generally always be separated from the rest.
    // It could also be handled with "if (FMLEnvironment.dist == Dist.CLIENT) {}" in the constructor, but we will just
    // do this for your sanity's sake.
    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }
}
