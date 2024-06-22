package com.heaser.pipeconnector.network;
import com.heaser.pipeconnector.constants.ComponentDataTags;
import com.heaser.pipeconnector.utils.GeneralUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
        CompoundTag tag = item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(ComponentDataTags.kPipeConnectorInventoryGuard, this.inventoryGuard);
    }

}
