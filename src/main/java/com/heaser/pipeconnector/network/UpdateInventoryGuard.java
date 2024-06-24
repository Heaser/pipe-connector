package com.heaser.pipeconnector.network;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record UpdateInventoryGuard(boolean inventoryGuard) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateInventoryGuard> STREAM_CODEC = StreamCodec
            .ofMember(
                    UpdateInventoryGuard::write,
                    UpdateInventoryGuard::decode);

    public static final Type<UpdateInventoryGuard> TYPE = CustomPipeconnectorPayload.createType("update_inventory_guard");
    @Override
    public Type<UpdateInventoryGuard> type() {
        return TYPE;
    }

    public static UpdateInventoryGuard decode(RegistryFriendlyByteBuf buf) {
        return new UpdateInventoryGuard(buf.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.inventoryGuard);
    }

    public void handleOnServer(ServerPlayer sender) {
        if(sender == null)
            return;

        if (!GeneralUtils.isHoldingPipeConnector(sender)) {
            return;
        }
        ItemStack item = sender.getMainHandItem();
        TagUtils.setInventoryGuard(item, this.inventoryGuard);
    }

}
