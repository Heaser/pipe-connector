package com.heaser.pipeconnector.compatibility.xnet;

import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class XNetCompatibility implements IPlacer {
    static public Class<? extends Block> getBlockToRegister() { return GenericCableBlock.class; }

    public boolean place(Level level, BlockPos pos, Player player, Item item, List<Direction> adjacentDirectionSides, ItemStack heldPipeItem) {

        Block block = Block.byItem(item);
        String id = item.getDescriptionId();
        String[] idParts = id.splitWithDelimiters("_", 3);
        String colorId = idParts[idParts.length - 1];
        CableColor color = CableColor.valueOf(colorId.toUpperCase());

        if ((block instanceof GenericCableBlock colorBlock)) {
            if (!isVoidableBlock(level, pos)) {
                level.addDestroyBlockEffect(pos, level.getBlockState(pos));
                level.destroyBlock(pos, true, player);
            }

            BlockHitResult hitResult = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), adjacentDirectionSides.getFirst(), pos, false);
            BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.OFF_HAND, heldPipeItem, hitResult);

            BlockState blockState = (colorBlock).calculateState(context.getLevel(),
                    context.getClickedPos(), colorBlock.defaultBlockState().setValue(GenericCableBlock.COLOR, color));

            boolean placed = level.setBlockAndUpdate(pos, blockState);
            if (placed) {
                blockState.getBlock().setPlacedBy(level, pos, blockState, player, heldPipeItem);
            }
            return placed;

        }
        return false;
    }
}
