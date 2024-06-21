package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.utils.GeneralUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


public record UpdateUtilizeExistingPipes(boolean utilizeExistingPipes) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateUtilizeExistingPipes> STREAM_CODEC = StreamCodec
            .ofMember(
                    UpdateUtilizeExistingPipes::write,
                    UpdateUtilizeExistingPipes::decode);

    public static final Type<UpdateUtilizeExistingPipes> TYPE = CustomPipeconnectorPayload.createType("update_utilize_existing_pipes");
    @Override
    public Type<UpdateUtilizeExistingPipes> type() {
        return TYPE;
    }

    public static UpdateUtilizeExistingPipes decode(RegistryFriendlyByteBuf buf) {
        return new UpdateUtilizeExistingPipes(buf.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.utilizeExistingPipes);
    }

    public void handleOnServer(ServerPlayer player) {;
        if(player == null)
            return;

        if (!GeneralUtils.isHoldingPipeConnector(player)) {
            return;
        }
        ItemStack item = player.getMainHandItem();
        CompoundTag tag = item.getOrCreateTagElement(PipeConnector.MODID);
        tag.putBoolean("UtilizeExistingPipes", this.utilizeExistingPipes);
    }
}
