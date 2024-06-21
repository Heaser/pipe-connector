package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    /*public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PipeConnector.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );*/

    public static void register(RegisterPayloadHandlersEvent event) {
        int packetId = 0;
        var registrar = event.registrar(PipeConnector.MODID);

        /*CHANNEL.messageBuilder(UpdateDepthPacket.class, packetId++)
                .encoder(UpdateDepthPacket::encode)
                .decoder(UpdateDepthPacket::new)
                .consumerMainThread(UpdateDepthPacket::handle)
                .add();*/


        /*CHANNEL.messageBuilder(ResetPacket.class, packetId++)
                .encoder(ResetPacket::encode)
                .decoder(ResetPacket::new)
                .consumerMainThread(ResetPacket::handle)
                .add();*/

        /*CHANNEL.messageBuilder(BuildPipesPacket.class, packetId++)
                .encoder(BuildPipesPacket::encode)
                .decoder(BuildPipesPacket::new)
                .consumerMainThread(BuildPipesPacket::handle)
                .add();*/

        /*CHANNEL.messageBuilder(UpdateBridgeTypePacket.class, packetId++)
                .encoder(UpdateBridgeTypePacket::encode)
                .decoder(UpdateBridgeTypePacket::new)
                .consumerMainThread(UpdateBridgeTypePacket::handle)
                .add();*/

        /*CHANNEL.messageBuilder(UpdateUtilizeExistingPipes.class,packetId++)
                .encoder(UpdateUtilizeExistingPipes::encode)
                .decoder(UpdateUtilizeExistingPipes::new)
                .consumerMainThread(UpdateUtilizeExistingPipes::handle)
                .add();*/

        /*CHANNEL.messageBuilder(UpdateInventoryGuard.class,packetId++)
                .encoder(UpdateInventoryGuard::encode)
                .decoder(UpdateInventoryGuard::new)
                .consumerMainThread(UpdateInventoryGuard::handle)
                .add();*/

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