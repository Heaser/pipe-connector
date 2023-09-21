package com.heaser.pipeconnector.network;


import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class UpdateDepthPacket {
    public int depth;

    public UpdateDepthPacket(int depth) {
        this.depth = depth;
    }

    public UpdateDepthPacket(FriendlyByteBuf buf) {
        this.depth = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.depth);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if(sender == null)
                return;

            if (!PipeConnectorUtils.isHoldingPipeConnector(sender)) {
                return;
            }

            CompoundTag tag = sender.getMainHandItem().getOrCreateTagElement(PipeConnector.MODID);
            tag.putInt("Depth", this.depth);
        });
        ctx.get().setPacketHandled(true);
    }
}
