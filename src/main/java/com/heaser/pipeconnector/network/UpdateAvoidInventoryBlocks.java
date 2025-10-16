package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record UpdateAvoidInventoryBlocks(boolean avoidInventoryBlocks) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateAvoidInventoryBlocks> STREAM_CODEC = StreamCodec
            .ofMember(
                    UpdateAvoidInventoryBlocks::write,
                    UpdateAvoidInventoryBlocks::decode);

    public static final Type<UpdateAvoidInventoryBlocks> TYPE = CustomPipeConnectorPayload.createType("update_avoid_inventory_blocks");
    @Override
    public Type<UpdateAvoidInventoryBlocks> type() {
        return TYPE;
    }

    public static UpdateAvoidInventoryBlocks decode(RegistryFriendlyByteBuf buf) {
        return new UpdateAvoidInventoryBlocks(buf.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.avoidInventoryBlocks);
    }

    public void handleOnServer(ServerPlayer sender) {
        if(sender == null)
            return;

        if (!GeneralUtils.isHoldingPipeConnector(sender)) {
            return;
        }
        ItemStack item = sender.getMainHandItem();
        TagUtils.setAvoidInventoryBlocks(item, this.avoidInventoryBlocks);
    }
}

