package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


public record UpdateReplaceModePacket(boolean replaceMode) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateReplaceModePacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    UpdateReplaceModePacket::write,
                    UpdateReplaceModePacket::decode);

    public static final Type<UpdateReplaceModePacket> TYPE = CustomPipeConnectorPayload.createType("update_replace_mode");
    @Override
    public Type<UpdateReplaceModePacket> type() {
        return TYPE;
    }

    public static UpdateReplaceModePacket decode(RegistryFriendlyByteBuf buf) {
        return new UpdateReplaceModePacket(buf.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.replaceMode);
    }

    public void handleOnServer(ServerPlayer player) {
        if (player == null)
            return;

        if (!GeneralUtils.isHoldingPipeConnector(player)) {
            return;
        }
        ItemStack item = player.getMainHandItem();
        TagUtils.setReplaceMode(item, this.replaceMode);
    }
}
