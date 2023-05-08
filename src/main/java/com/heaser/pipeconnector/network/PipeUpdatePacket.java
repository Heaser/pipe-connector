package com.heaser.pipeconnector.network;

import java.util.function.Supplier;

import com.heaser.pipeconnector.items.pipeconnectoritem.utils.PipeConnectorUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PipeUpdatePacket {
    public PipeUpdatePacket() {
    }

    public static void encode(PipeUpdatePacket msg, FriendlyByteBuf buf) {
    }

    public static PipeUpdatePacket decode(FriendlyByteBuf buf) {
        return new PipeUpdatePacket();
    }

    public static void handle(PipeUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Call the method to reduce the number of pipes in the inventory
                PipeConnectorUtils.reduceNumberOfPipesInInventory(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
