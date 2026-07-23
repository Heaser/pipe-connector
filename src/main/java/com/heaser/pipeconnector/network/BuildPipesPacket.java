package com.heaser.pipeconnector.network;

import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.NodeParameter;
import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.heaser.pipeconnector.utils.PipeConnectorUtils.connectBlocks;

public record BuildPipesPacket() implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, BuildPipesPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    BuildPipesPacket::write,
                    BuildPipesPacket::decode);

    public static final CustomPacketPayload.Type<BuildPipesPacket> TYPE = CustomPipeConnectorPayload.createType("build_pipes");

    public static BuildPipesPacket decode(RegistryFriendlyByteBuf buf) {
        return new BuildPipesPacket();
    }

    @Override
    public CustomPacketPayload.Type<BuildPipesPacket> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buf) {

    }

    public void handleOnServer(ServerPlayer sender) {
        if (sender == null)
            return;

        if (!GeneralUtils.isHoldingPipeConnector(sender)) {
            return;
        }
        ItemStack interactedItem = sender.getMainHandItem();

        if (TagUtils.getReplaceMode(interactedItem)) {
            boolean wasSuccessful = PipeConnectorUtils.replacePipeRun(sender, interactedItem);
            if (wasSuccessful) {
                TagUtils.clearReplaceSeed(interactedItem);
            }
            return;
        }

        int depth = TagUtils.getDepthFromStack(interactedItem);
        List<NodeParameter> nodes = TagUtils.getNodesFromStack(interactedItem);

        if (nodes.size() < 2) {
            return;
        }

        NodeParameter endNode = nodes.getLast();
        BlockPos endPosition = endNode.position;
        Direction endDirection = endNode.direction;
        BridgeType bridgeType = TagUtils.getBridgeType(interactedItem);
        boolean utilizeExistingPipes = TagUtils.getUtilizeExistingPipes(interactedItem);

        BlockHitResult virtualHitResult = new BlockHitResult(Vec3.ZERO, endDirection, endPosition, false);

        boolean wasSuccessful = connectBlocks(sender,
                nodes,
                depth,
                new UseOnContext(sender, InteractionHand.MAIN_HAND, virtualHitResult),
                bridgeType,
                utilizeExistingPipes);
        TagUtils.resetPositionAndDirectionTags(interactedItem, sender, wasSuccessful);
    }
}
