package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record UpdateSolidPreview(boolean solid) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSolidPreview> STREAM_CODEC = StreamCodec
            .ofMember(UpdateSolidPreview::write, UpdateSolidPreview::decode);

    public static final Type<UpdateSolidPreview> TYPE = CustomPipeConnectorPayload.createType("update_solid_preview");
    @Override public Type<UpdateSolidPreview> type() { return TYPE; }

    public static UpdateSolidPreview decode(RegistryFriendlyByteBuf buf) { return new UpdateSolidPreview(buf.readBoolean()); }
    public void write(RegistryFriendlyByteBuf buf) { buf.writeBoolean(this.solid); }

    public void handleOnServer(ServerPlayer sender) {
        if (sender == null) return;
        if (!GeneralUtils.isHoldingPipeConnector(sender)) return;
        ItemStack item = sender.getMainHandItem();
        TagUtils.setSolidPreview(item, this.solid);
    }
}

