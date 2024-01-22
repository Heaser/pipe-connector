package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.client.gui.buttons.BridgeTypeButton;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.TagUtils;
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

            if (!GeneralUtils.isHoldingPipeConnector(sender)) {
                return;
            }
            ItemStack interactedItem = sender.getMainHandItem();
            int depth = TagUtils.getDepthFromStack(interactedItem);
            BlockPos startPosition = TagUtils.getStartPosition(interactedItem);
            BlockPos endPosition = TagUtils.getEndPosition(interactedItem);
            Direction startDirection = TagUtils.getStartDirection(interactedItem);
            Direction endDirection = TagUtils.getEndDirection(interactedItem);
            BridgeType bridgeType = TagUtils.getBridgeType(interactedItem);
            boolean utilizeExistingPipes = TagUtils.getUtilizeExistingPipes(interactedItem);

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
                    new UseOnContext(sender, InteractionHand.MAIN_HAND, virtualHitResult),
                    bridgeType,
                    utilizeExistingPipes);
            TagUtils.resetPositionAndDirectionTags(interactedItem, sender, wasSuccessful);
        });
        context.get().setPacketHandled(true);
    }
}
