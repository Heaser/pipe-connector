package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;

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

        // Your existing packet registration
        CHANNEL.registerMessage(packetId++, UpdateDepthPacket.class, UpdateDepthPacket::encode, UpdateDepthPacket::decode, UpdateDepthPacket::handle);

        // Register the new packet
        CHANNEL.registerMessage(packetId++, PipeConnectorHighlightPacket.class, PipeConnectorHighlightPacket::encode, PipeConnectorHighlightPacket::decode, PipeConnectorHighlightPacket::handle);
    }
}