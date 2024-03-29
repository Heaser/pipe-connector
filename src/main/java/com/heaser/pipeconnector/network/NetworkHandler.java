package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;



public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PipeConnector.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int packetId = 0;

        CHANNEL.messageBuilder(UpdateDepthPacket.class, packetId++)
                .encoder(UpdateDepthPacket::encode)
                .decoder(UpdateDepthPacket::new)
                .consumerMainThread(UpdateDepthPacket::handle)
                .add();


        CHANNEL.messageBuilder(ResetPacket.class, packetId++)
                .encoder(ResetPacket::encode)
                .decoder(ResetPacket::new)
                .consumerMainThread(ResetPacket::handle)
                .add();

        CHANNEL.messageBuilder(BuildPipesPacket.class, packetId++)
                .encoder(BuildPipesPacket::encode)
                .decoder(BuildPipesPacket::new)
                .consumerMainThread(BuildPipesPacket::handle)
                .add();

        CHANNEL.messageBuilder(UpdateBridgeTypePacket.class, packetId++)
                .encoder(UpdateBridgeTypePacket::encode)
                .decoder(UpdateBridgeTypePacket::new)
                .consumerMainThread(UpdateBridgeTypePacket::handle)
                .add();

        CHANNEL.messageBuilder(UpdateUtilizeExistingPipes.class,packetId++)
                .encoder(UpdateUtilizeExistingPipes::encode)
                .decoder(UpdateUtilizeExistingPipes::new)
                .consumerMainThread(UpdateUtilizeExistingPipes::handle)
                .add();

        CHANNEL.messageBuilder(UpdateInventoryGuard.class,packetId++)
                .encoder(UpdateInventoryGuard::encode)
                .decoder(UpdateInventoryGuard::new)
                .consumerMainThread(UpdateInventoryGuard::handle)
                .add();

        PipeConnector.LOGGER.debug("Registered {} Packets for {}", packetId, PipeConnector.MODID);
    }
}