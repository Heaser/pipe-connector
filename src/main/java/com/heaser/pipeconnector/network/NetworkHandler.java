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

        // Syncs the depth of the pipe connector with the server
        CHANNEL.registerMessage(packetId++, UpdateDepthPacket.class, UpdateDepthPacket::encode, UpdateDepthPacket::decode, UpdateDepthPacket::handle);
        //TODO: No syncing particles you goof.
        //TODO: Nor syncing items....
    }
}