package com.heaser.pipeconnector.network;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class UpdateBridgeTypePacket {
    public String bridgeType;

    public UpdateBridgeTypePacket(BridgeType bridgeType) {
        this.bridgeType = bridgeType.toString();
    }

    public UpdateBridgeTypePacket(FriendlyByteBuf buf) {
        bridgeType = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(bridgeType);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if(sender == null)
                return;

            if (!GeneralUtils.isHoldingPipeConnector(sender)) {
                return;
            }

            PipeConnectorUtils.setBridgeType(sender.getMainHandItem(), BridgeType.valueOf(bridgeType));
        });
        ctx.get().setPacketHandled(true);
    }
}
