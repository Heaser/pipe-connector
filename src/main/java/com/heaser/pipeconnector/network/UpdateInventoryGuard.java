package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.utils.GeneralUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateInventoryGuard {
    public boolean inventoryGuard;

    public UpdateInventoryGuard(boolean shouldInventoryGuard) {
        this.inventoryGuard = shouldInventoryGuard;
    }

    public UpdateInventoryGuard(FriendlyByteBuf buf) {
            this.inventoryGuard = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(this.inventoryGuard);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if(sender == null)
                    return;

                if (!GeneralUtils.isHoldingPipeConnector(sender)) {
                    return;
                }
                ItemStack item = sender.getMainHandItem();
                CompoundTag tag = item.getOrCreateTagElement(PipeConnector.MODID);
                tag.putBoolean("PreventInventoryBlockBreaking", this.inventoryGuard);
            });
            ctx.get().setPacketHandled(true);
        }

}
