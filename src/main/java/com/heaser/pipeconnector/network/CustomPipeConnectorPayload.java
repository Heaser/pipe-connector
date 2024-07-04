package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.minecraft.resources.ResourceLocation;

public interface CustomPipeConnectorPayload extends CustomPacketPayload {
    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String name) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PipeConnector.MODID, name));
    }
}