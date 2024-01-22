package com.heaser.pipeconnector.compatibility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.Map;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class CompatibilityPlacer {
    private static CompatibilityPlacer INSTANCE;
    private final HashMap<Class<? extends Item>, IPlacer> classToPlacerMap = new HashMap<>();
    private CompatibilityPlacer() {
        if (isModLoaded("ae2")) {
            classToPlacerMap.put(AE2Compatiblity.getItemStackClassToRegister(), new AE2Compatiblity());
        }
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static CompatibilityPlacer getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CompatibilityPlacer();
        }

        return INSTANCE;
    }

    public boolean defaultPlace(Level level, BlockPos pos, Player player, Item item) {
        if (!isVoidableBlock(level, pos)) {
            level.addDestroyBlockEffect(pos, level.getBlockState(pos));
            level.destroyBlock(pos, true, player);
        }

        BlockState blockState = Block.byItem(item).defaultBlockState();
        return level.setBlockAndUpdate(pos, blockState);
    }

    public boolean place(Level level, BlockPos pos, Player player, ItemStack stack) {
        IPlacer placer = null;
        for (Map.Entry<Class<? extends Item>, IPlacer> set : classToPlacerMap.entrySet()) {
            if (set.getKey().isAssignableFrom(stack.getItem().getClass())) {
                placer = set.getValue();
                break;
            }
        }
        if (placer != null) {
            return placer.place(level, pos, player, stack.getItem());
        }
        else {
            return defaultPlace(level, pos, player, stack.getItem());
        }
    }
}
