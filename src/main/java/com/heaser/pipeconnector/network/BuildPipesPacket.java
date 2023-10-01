package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.heaser.pipeconnector.utils.PipeConnectorUtils.connectBlocks;

public class BuildPipesPacket {
    public BuildPipesPacket() {
    }

    public BuildPipesPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {

    }
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if(sender == null)
                return;

            if (!PipeConnectorUtils.isHoldingPipeConnector(sender)) {
                return;
            }
            ItemStack interactedItem = sender.getMainHandItem();
            int depth = PipeConnectorUtils.getDepthFromStack(interactedItem);
            BlockPos startPosition = PipeConnectorUtils.getStartPosition(interactedItem);
            BlockPos endPosition = PipeConnectorUtils.getEndPosition(interactedItem);
            Direction startDirection = PipeConnectorUtils.getStartDirection(interactedItem);
            Direction endDirection = PipeConnectorUtils.getEndDirection(interactedItem);

            if (startPosition == null || endPosition == null) {
                return;
            }

            BlockHitResult virtualHitResult = new BlockHitResult(Vec3.ZERO, endDirection, endPosition, false);

            boolean wasSuccessful = connectBlocks(sender,
                    startPosition,
                    startDirection,
                    endPosition,
                    endDirection,
                    depth,
                    new UseOnContext(sender, InteractionHand.MAIN_HAND, virtualHitResult));
            PipeConnectorUtils.resetPositionAndDirectionTags(interactedItem, sender, wasSuccessful);
            PipeConnectorUtils.resetBlockPreview(sender);
        });
        context.get().setPacketHandled(true);
    }
}
