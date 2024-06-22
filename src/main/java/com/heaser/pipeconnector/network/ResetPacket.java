package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ResetPacket() implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    ResetPacket::write,
                    ResetPacket::decode);

    public static final CustomPacketPayload.Type<ResetPacket> TYPE = CustomPipeconnectorPayload.createType("reset");

    @Override
    public CustomPacketPayload.Type<ResetPacket> type() {
        return TYPE;
    }

    public static ResetPacket decode(RegistryFriendlyByteBuf buf) {
        return new ResetPacket();
    }

    public void write(FriendlyByteBuf buf) {

    }

    public void handleOnServer(ServerPlayer sender) {
        if(sender == null)
            return;

        if (!GeneralUtils.isHoldingPipeConnector(sender)) {
            return;
        }
        TagUtils.resetPositionAndDirectionTags(sender.getMainHandItem(), sender, true);
    }
}
