package com.heaser.pipeconnector.compatibility.ae2;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.block.networking.CableBusBlock;
import appeng.core.definitions.AEBlocks;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockGetter;
import com.heaser.pipeconnector.compatibility.interfaces.IDirectionGetter;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;


public class AE2Compatiblity implements IBlockGetter, IPlacer, IBlockEqualsChecker, IDirectionGetter {
    static public Class<? extends Item> getItemStackClassToRegister() {
        return PartItem.class;
    }
    static public Class<? extends Block> getBlockToRegister() { return CableBusBlock.class; }
    public Block getBlock(ItemStack placedStack) {
        return AEBlocks.CABLE_BUS.block();
    }

    public boolean place(Level level, BlockPos pos, Player player, Item item) {
        IPart part = null;
        if (item instanceof IPartItem<?> partItem) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IPartHost host ? host.getPart(null) != null : !isVoidableBlock(level, pos)) {
                level.addDestroyBlockEffect(pos, level.getBlockState(pos));
                level.destroyBlock(pos, true, player);
            }
            part = PartHelper.setPart((ServerLevel) level, pos, null, player, partItem);
        }
        return part != null;
    };

    public boolean isBlockStateSpecificBlock(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Item placedItem = placedItemStack.getItem();
        if (level.getBlockState(pos).getBlock() instanceof CableBusBlock &&
                blockEntity instanceof IPartHost host &&
                placedItem instanceof IPartItem) {
            IPart part = host.getPart(null);
            if (part == null) {
                return false;
            }
            IPartItem<?> partItem = part.getPartItem();
            boolean colorsEqual = true;
            if (partItem instanceof ColoredPartItem<?> coloredPartItem && placedItem instanceof ColoredPartItem<?> coloredPlacedItem) {
                colorsEqual = coloredPartItem.getColor().equals(coloredPlacedItem.getColor());
            }
            Class<?> ownClass = ((IPartItem<?>) placedItem).getPartClass();
            Class<?> targetClass = partItem.getPartClass();
            return colorsEqual && targetClass == ownClass;
        } else {
            return false;
        }
    }

    @Override
    @Nullable
    public Direction getDirection(UseOnContext context) {
        BlockPos clickedPosition = context.getClickedPos();
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(clickedPosition);
        if (blockEntity instanceof IPartHost host && host.getPart(null) == null) {
            return null;
        }
        return context.getClickedFace();
    }
}

