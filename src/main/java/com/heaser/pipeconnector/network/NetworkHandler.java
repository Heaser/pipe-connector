package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


public class NetworkHandler {
    public static void register(RegisterPayloadHandlersEvent event) {
        int packetId = 0;
        var registrar = event.registrar(PipeConnector.MODID);
        serverbound(registrar, UpdateUtilizeExistingPipes.TYPE, UpdateUtilizeExistingPipes.STREAM_CODEC);
        serverbound(registrar, UpdateInventoryGuard.TYPE, UpdateInventoryGuard.STREAM_CODEC);
        serverbound(registrar, UpdateAvoidInventoryBlocks.TYPE, UpdateAvoidInventoryBlocks.STREAM_CODEC);
        serverbound(registrar, UpdateDepthPacket.TYPE, UpdateDepthPacket.STREAM_CODEC);
        serverbound(registrar, UpdateBridgeTypePacket.TYPE, UpdateBridgeTypePacket.STREAM_CODEC);
        serverbound(registrar, UpdateSolidPreview.TYPE, UpdateSolidPreview.STREAM_CODEC);
        serverbound(registrar, UpdatePipeVision.TYPE, UpdatePipeVision.STREAM_CODEC);
        serverbound(registrar, ResetPacket.TYPE, ResetPacket.STREAM_CODEC);
        serverbound(registrar, BuildPipesPacket.TYPE, BuildPipesPacket.STREAM_CODEC);

        PipeConnector.LOGGER.debug("Registered {} Packets for {}", packetId, PipeConnector.MODID);
    }

    private static <T extends ClientboundPacket> void clientbound(PayloadRegistrar registrar,
                                                                  CustomPacketPayload.Type<T> type,
                                                                  StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.playToClient(type, codec, ClientboundPacket::handleOnClient);
    }

    private static <T extends ServerboundPacket> void serverbound(PayloadRegistrar registrar,
                                                                  CustomPacketPayload.Type<T> type,
                                                                  StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.playToServer(type, codec, ServerboundPacket::handleOnServer);
    }

    private static <T extends ServerboundPacket & ClientboundPacket> void bidirectional(PayloadRegistrar registrar,
                                                                                        CustomPacketPayload.Type<T> type,
                                                                                        StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.playBidirectional(type, codec, (payload, context) -> {
            if (context.flow().isClientbound()) {
                payload.handleOnClient(context);
            } else if (context.flow().isServerbound()) {
                payload.handleOnServer(context);
            }
        });
    }
}
