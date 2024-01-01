package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.utils.GeneralUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateUtilizeExistingPipes {
    public boolean utilizeExistingPipes;

    public UpdateUtilizeExistingPipes(boolean ShouldUtilizeExistingPipes) {
        this.utilizeExistingPipes = ShouldUtilizeExistingPipes;
    }

    public UpdateUtilizeExistingPipes(FriendlyByteBuf buf) {
        this.utilizeExistingPipes = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.utilizeExistingPipes);
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
            tag.putBoolean("UtilizeExistingPipes", this.utilizeExistingPipes);
        });
        ctx.get().setPacketHandled(true);
    }
}
