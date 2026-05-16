package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.minecraft.resources.Identifier;

public interface CustomPipeConnectorPayload extends CustomPacketPayload {
    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String name) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PipeConnector.MODID, name));
    }
}