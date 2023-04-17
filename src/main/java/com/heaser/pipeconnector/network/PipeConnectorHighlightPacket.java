package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.items.pipeconnectoritem.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PipeConnectorHighlightPacket {
    private BlockPos position;
    private Direction facingSidePosition;

    public PipeConnectorHighlightPacket(BlockPos firstPosition, Direction facingSideStart) {
        this.position = firstPosition;
        this.facingSidePosition = facingSideStart;
    }

    public static void encode(PipeConnectorHighlightPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.position);
        buffer.writeEnum(packet.facingSidePosition);
    }

    public static PipeConnectorHighlightPacket decode(FriendlyByteBuf buffer) {
        BlockPos position = buffer.readBlockPos();
        Direction facingSidePosition = buffer.readEnum(Direction.class);
        return new PipeConnectorHighlightPacket(position, facingSidePosition);
    }

    public static void handle(PipeConnectorHighlightPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Your client-side code to spawn particles
            Minecraft mc = Minecraft.getInstance();
            BlockPos pos = packet.getPosition().relative(packet.getFacingSidePosition());
            ParticleHelper.spawnDirectionHighlightParticles(mc.level, pos, 0.0f, 0.0f, 1.0f, 1.0f);
        });
        context.setPacketHandled(true);
    }

    private BlockPos getPosition() {
        return position;
    }

    private Direction getFacingSidePosition() {
        return facingSidePosition;
    }
}