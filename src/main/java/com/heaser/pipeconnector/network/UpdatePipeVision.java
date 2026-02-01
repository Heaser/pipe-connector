package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record UpdatePipeVision(boolean enabled) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePipeVision> STREAM_CODEC = StreamCodec
            .ofMember(UpdatePipeVision::write, UpdatePipeVision::decode);

    public static final Type<UpdatePipeVision> TYPE = CustomPipeConnectorPayload.createType("update_pipe_vision");
    @Override public Type<UpdatePipeVision> type() { return TYPE; }

    public static UpdatePipeVision decode(RegistryFriendlyByteBuf buf) { return new UpdatePipeVision(buf.readBoolean()); }
    public void write(RegistryFriendlyByteBuf buf) { buf.writeBoolean(this.enabled); }

    public void handleOnServer(ServerPlayer sender) {
        if (sender == null) return;
        if (!GeneralUtils.isHoldingPipeConnector(sender)) return;
        ItemStack item = sender.getMainHandItem();
        TagUtils.setPipeVision(item, this.enabled);
    }
}
