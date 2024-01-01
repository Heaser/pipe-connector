package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.utils.GeneralUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdatePreventInventoryBlockBreaking {
    public boolean preventInventoryBlockBreaking;

    public UpdatePreventInventoryBlockBreaking(boolean shouldBreakInventoryBlocks) {
        this.preventInventoryBlockBreaking = shouldBreakInventoryBlocks;
    }

    public UpdatePreventInventoryBlockBreaking(FriendlyByteBuf buf) {
            this.preventInventoryBlockBreaking = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(this.preventInventoryBlockBreaking);
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
                tag.putBoolean("PreventInventoryBlockBreaking", this.preventInventoryBlockBreaking);
            });
            ctx.get().setPacketHandled(true);
        }

}
