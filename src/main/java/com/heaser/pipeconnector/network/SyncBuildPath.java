package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.client.ClientSetup;
import com.heaser.pipeconnector.utils.PreviewInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.function.Supplier;



public class SyncBuildPath {
    HashSet<PreviewInfo> buildPath;

    public SyncBuildPath(HashSet<PreviewInfo> buildPath) {
        this.buildPath = buildPath;
    }

    public SyncBuildPath(FriendlyByteBuf buf) {
        int x;
        int y;
        int z;
        this.buildPath = new HashSet<PreviewInfo>();
        while (buf.readableBytes() >= 3 * 4) {
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
            BlockPos pos = new BlockPos(x, y, z);
            this.buildPath.add(new PreviewInfo(pos));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        for (PreviewInfo info : this.buildPath) {
            buf.writeInt(info.pos.getX());
            buf.writeInt(info.pos.getY());
            buf.writeInt(info.pos.getZ());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkDirection networkDirection = ctx.get().getDirection();
            LogicalSide side = networkDirection.getReceptionSide();
            if (side == LogicalSide.CLIENT) {
                ClientSetup.PREVIEW_DRAWER.previewMap = this.buildPath;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
