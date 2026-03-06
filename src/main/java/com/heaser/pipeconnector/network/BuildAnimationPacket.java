package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.client.ClientSetup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public record BuildAnimationPacket(List<BlockPos> positions) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, BuildAnimationPacket> STREAM_CODEC =
            StreamCodec.ofMember(BuildAnimationPacket::write, BuildAnimationPacket::decode);

    public static final CustomPacketPayload.Type<BuildAnimationPacket> TYPE =
            CustomPipeConnectorPayload.createType("build_animation");

    @Override
    public CustomPacketPayload.Type<BuildAnimationPacket> type() {
        return TYPE;
    }

    public static BuildAnimationPacket decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readInt();
        List<BlockPos> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(buf.readBlockPos());
        }
        return new BuildAnimationPacket(positions);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.positions.size());
        for (BlockPos pos : this.positions) {
            buf.writeBlockPos(pos);
        }
    }

    @Override
    public void handleOnClient(Player player) {
        ClientSetup.BUILD_ANIMATION_RENDERER.startAnimation(this.positions);
    }
}
