package com.heaser.pipeconnector.compatibility;

import com.heaser.pipeconnector.compatibility.ae2.AE2Compatiblity;
//import com.heaser.pipeconnector.compatibility.enderio.EnderIoCompatibility;
import com.heaser.pipeconnector.compatibility.gtceu.GTCEUCompatibility;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import com.heaser.pipeconnector.compatibility.prettypipes.PrettyPipesCompatibility;
import com.heaser.pipeconnector.compatibility.prettypipes.PrettyPipesFluidsCompatibility;
import com.heaser.pipeconnector.compatibility.xnet.XNetCompatibility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
//      if(isModLoaded("enderio_conduits")) {
//          blockClassToPlacerMap.put(EnderIoCompatibility.getBlockToRegister(), new EnderIoCompatibility());
//      }
        if (isModLoaded("xnet")) {
            blockClassToPlacerMap.put(XNetCompatibility.getBlockToRegister(), new XNetCompatibility());
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

    public boolean defaultPlace(Level level, BlockPos pos, Player player, Item item, List<Direction> directions, ItemStack stack) {
        if (!isVoidableBlock(level, pos)) {
            // Harvest drops and add to inventory
            BlockState state = level.getBlockState(pos);
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, stack);
                for (ItemStack drop : drops) {
                    if (!player.getInventory().add(drop)) {
                        player.drop(drop, false);
                    }
                }
                // Animation and sound
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
            }
            // Remove the block without dropping items (since we manually handled them)
            level.removeBlock(pos, false);
        }

        BlockHitResult hitResult = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), directions.getFirst(), pos, false);
        BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.OFF_HAND, stack, hitResult);
        BlockState blockState = Block.byItem(item).getStateForPlacement(context);
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
            return placer.place(level, pos, player, stack.getItem(), adjacentDirectionSides, stack);
        }
        else {
            return defaultPlace(level, pos, player, stack.getItem(), adjacentDirectionSides, stack);
        }
    }
}
