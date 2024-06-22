package com.heaser.pipeconnector.network;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

import static com.heaser.pipeconnector.utils.TagUtils.setBridgeType;


public record UpdateBridgeTypePacket(String bridgeType) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBridgeTypePacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    UpdateBridgeTypePacket::write,
                    UpdateBridgeTypePacket::decode);

    public static final CustomPacketPayload.Type<UpdateBridgeTypePacket> TYPE = CustomPipeconnectorPayload.createType("update_bridge_type");

    @Override
    public CustomPacketPayload.Type<UpdateBridgeTypePacket> type() {
        return TYPE;
    }

    public static UpdateBridgeTypePacket decode(RegistryFriendlyByteBuf buf) {
        return new UpdateBridgeTypePacket(buf.readUtf());
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(bridgeType);
    }

    public void handleOnServer(ServerPlayer sender) {
        if(sender == null)
            return;

        if (!GeneralUtils.isHoldingPipeConnector(sender)) {
            return;
        }

        setBridgeType(sender.getMainHandItem(), BridgeType.valueOf(bridgeType));
    }
}
