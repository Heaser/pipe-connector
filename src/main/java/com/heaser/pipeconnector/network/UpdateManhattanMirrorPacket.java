package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record UpdateManhattanMirrorPacket(boolean mirrored) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateManhattanMirrorPacket> STREAM_CODEC = StreamCodec
            .ofMember(UpdateManhattanMirrorPacket::write, UpdateManhattanMirrorPacket::decode);

    public static final Type<UpdateManhattanMirrorPacket> TYPE = CustomPipeConnectorPayload.createType("update_manhattan_mirror");
    @Override public Type<UpdateManhattanMirrorPacket> type() { return TYPE; }

    public static UpdateManhattanMirrorPacket decode(RegistryFriendlyByteBuf buf) { return new UpdateManhattanMirrorPacket(buf.readBoolean()); }
    public void write(RegistryFriendlyByteBuf buf) { buf.writeBoolean(this.mirrored); }

    public void handleOnServer(ServerPlayer sender) {
        if (sender == null) return;
        if (!GeneralUtils.isHoldingPipeConnector(sender)) return;
        ItemStack item = sender.getMainHandItem();
        TagUtils.setMirrorManhattan(item, this.mirrored);
    }
}
