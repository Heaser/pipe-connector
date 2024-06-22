package com.heaser.pipeconnector.network;


import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.constants.ComponentDataTags;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public record UpdateDepthPacket(int depth) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDepthPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    UpdateDepthPacket::write,
                    UpdateDepthPacket::decode);

    public static final CustomPacketPayload.Type<UpdateDepthPacket> TYPE = CustomPipeconnectorPayload.createType("update_depth");

    @Override
    public CustomPacketPayload.Type<UpdateDepthPacket> type() {
        return TYPE;
    }

    public static UpdateDepthPacket decode(RegistryFriendlyByteBuf buf) {
        return new UpdateDepthPacket(buf.readInt());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.depth);
    }

    public void handleOnServer(ServerPlayer sender) {
        if(sender == null)
            return;

        if (!GeneralUtils.isHoldingPipeConnector(sender)) {
            return;
        }

        ItemStack item = sender.getMainHandItem();
        TagUtils.setDepthToStack(item, this.depth);
//        CustomData.update(DataComponents.CUSTOM_DATA, item, tag -> {
//                tag.putInt(ComponentDataTags.kPipeConnectorDepth, this.depth);
//            });
    }
}
