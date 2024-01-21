package com.heaser.pipeconnector.compatibility;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.core.definitions.AEBlocks;
import appeng.items.parts.PartItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;


public class AE2Compatiblity implements IBlockGetter, IPlacer {
    static public Class<? extends Item> getItemStackClassToRegister() {
        return PartItem.class;
    }
    public Block getBlock(ItemStack placedStack) {
        return AEBlocks.CABLE_BUS.block();
    }

    public boolean place(Level level, BlockPos pos, Player player, Item item) {
        IPart part = null;
        if (item instanceof IPartItem<?>) {
            part = PartHelper.setPart((ServerLevel) level, pos, null, player, (IPartItem<?>)item);
        }
        return part != null;
    };
}

