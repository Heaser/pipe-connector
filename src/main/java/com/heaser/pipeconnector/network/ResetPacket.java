package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetPacket {
    public ResetPacket() {
    }

    public ResetPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {

    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if(sender == null)
                return;

            if (!PipeConnectorUtils.isHoldingPipeConnector(sender)) {
                return;
            }
            PipeConnectorUtils.resetPositionAndDirectionTags(sender.getMainHandItem(), sender, true);
            PipeConnectorUtils.resetBlockPreview(sender);
        });
        ctx.get().setPacketHandled(true);
    }
}
