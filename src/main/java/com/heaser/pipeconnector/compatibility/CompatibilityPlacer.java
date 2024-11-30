package com.heaser.pipeconnector.compatibility;

import com.heaser.pipeconnector.compatibility.ae2.AE2Compatiblity;
import com.heaser.pipeconnector.compatibility.gtceu.GTCEUCompatibility;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import com.heaser.pipeconnector.compatibility.prettypipes.PrettyPipesCompatibility;
import com.heaser.pipeconnector.compatibility.prettypipes.PrettyPipesFluidsCompatibility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;

public class CompatibilityPlacer {
    private static CompatibilityPlacer INSTANCE;
    private final HashMap<Class<? extends Item>, IPlacer> itemClassToPlacerMap = new HashMap<>();
    private final HashMap<Class<? extends Block>, IPlacer> blockClassToPlacerMap = new HashMap<>();
    private CompatibilityPlacer() {
        if (isModLoaded("ae2")) {
            itemClassToPlacerMap.put(AE2Compatiblity.getItemStackClassToRegister(), new AE2Compatiblity());
        }
        if(isModLoaded("prettypipes")) {
            blockClassToPlacerMap.put(PrettyPipesCompatibility.getBlockToRegister(), new PrettyPipesCompatibility());
        }
        if(isModLoaded("ppfluids")) {
            blockClassToPlacerMap.put(PrettyPipesFluidsCompatibility.getBlockToRegister(), new PrettyPipesCompatibility());
        }
        if(isModLoaded("gtceu")) {
            blockClassToPlacerMap.put(GTCEUCompatibility.getBlockToRegister(), new GTCEUCompatibility());
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

    public boolean place(Level level, BlockPos pos, Player player, ItemStack stack, List<Direction> adjacentDirectionSides) {
        IPlacer placer = null;
        Item item = stack.getItem();
        for (Map.Entry<Class<? extends Item>, IPlacer> set : itemClassToPlacerMap.entrySet()) {
            if (set.getKey().isAssignableFrom(item.getClass())) {

                placer = set.getValue();
                break;
            }
        }
        if (placer == null && item instanceof BlockItem) {
            for (Map.Entry<Class<? extends Block>, IPlacer> set : blockClassToPlacerMap.entrySet()) {
                if (set.getKey().isAssignableFrom(((BlockItem) item).getBlock().getClass())) {

                    placer = set.getValue();
                    break;
                }
            }
        }
        if (placer != null) {
            return placer.place(level, pos, player, stack.getItem(), adjacentDirectionSides);
        }
        else {
            return defaultPlace(level, pos, player, stack.getItem());
        }
    }
}
