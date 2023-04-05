package com.heaser.pipeconnector.network;


import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class UpdateDepthPacket {
    public int depth;

    public UpdateDepthPacket(int depth) {
        this.depth = depth;
    }

    public static void encode(UpdateDepthPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.depth);
    }

    public static UpdateDepthPacket decode(FriendlyByteBuf buf) {
        return new UpdateDepthPacket(buf.readInt());
    }

    public static void handle(UpdateDepthPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ctx.get().getSender().getMainHandItem().getOrCreateTag().putInt("Depth", packet.depth);
        });
        ctx.get().setPacketHandled(true);
    }
}
