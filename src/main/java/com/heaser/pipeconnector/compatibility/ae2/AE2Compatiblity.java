package com.heaser.pipeconnector.compatibility.ae2;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.block.networking.CableBusBlock;
import appeng.core.definitions.AEBlocks;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.api.util.AEColor;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockEqualsChecker;
import com.heaser.pipeconnector.compatibility.interfaces.IBlockGetter;
import com.heaser.pipeconnector.compatibility.interfaces.IColorProvider;
import com.heaser.pipeconnector.compatibility.interfaces.IDirectionGetter;
import com.heaser.pipeconnector.compatibility.interfaces.IPipeReplacer;
import com.heaser.pipeconnector.compatibility.interfaces.IPlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.heaser.pipeconnector.utils.GeneralUtils.isVoidableBlock;


public class AE2Compatiblity implements IBlockGetter, IPlacer, IBlockEqualsChecker, IDirectionGetter, IColorProvider, IPipeReplacer {
    static public Class<? extends Item> getItemStackClassToRegister() {
        return PartItem.class;
    }
    static public Class<? extends Block> getBlockToRegister() { return CableBusBlock.class; }
    public Block getBlock(ItemStack placedStack) {
        return AEBlocks.CABLE_BUS.block();
    }

    @Override
    public @Nullable Integer getColor(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IPartHost host) {
            IPart part = host.getPart(null);
            if (part != null) {
                IPartItem<?> partItem = part.getPartItem();
                if (partItem instanceof ColoredPartItem<?> coloredPartItem) {
                    AEColor aeColor = coloredPartItem.getColor();
                    return getAeColorInt(aeColor);
                }
            }
        }
        return null;
    }

    private int getAeColorInt(AEColor aeColor) {
        return switch (aeColor) {
            case WHITE -> 0xFFFFFFFF;
            case ORANGE -> 0xFFD87F33;
            case MAGENTA -> 0xFFB24CD8;
            case LIGHT_BLUE -> 0xFF6699D8;
            case YELLOW -> 0xFFE5E533;
            case LIME -> 0xFF7FCC19;
            case PINK -> 0xFFF27FA5;
            case GRAY -> 0xFF4C4C4C;
            case LIGHT_GRAY -> 0xFF999999;
            case CYAN -> 0xFF4C7F99;
            case PURPLE -> 0xFF7F3FB2;
            case BLUE -> 0xFF334CB2;
            case BROWN -> 0xFF664C33;
            case GREEN -> 0xFF667F33;
            case RED -> 0xFF993333;
            case BLACK -> 0xFF191919;
            case TRANSPARENT -> 0x80FFFFFF;
            default -> 0xFFFFFFFF;
        };
    }

    public boolean place(Level level, BlockPos pos, Player player, Item item, List<Direction> adjacentDirectionSides, ItemStack heldPipeItem) {
        IPart part = null;
        if (item instanceof IPartItem<?> partItem) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IPartHost host ? host.getPart(null) != null : !isVoidableBlock(level, pos)) {
                // Harvest drops and add to inventory
                BlockState state = level.getBlockState(pos);
                if (level instanceof ServerLevel serverLevel) {
                    List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, heldPipeItem);
                    for (ItemStack drop : drops) {
                        if (!player.getInventory().add(drop)) {
                            player.drop(drop, false);
                        }
                    }
                    // Animation and sound
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                }
                level.removeBlock(pos, false);
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
    public boolean isPlacementAlreadySatisfied(BlockPos pos, Block specificBlock, ItemStack placedItemStack, Level level) {
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
    public boolean isPassableForPathfinding(BlockPos pos, Level level, ItemStack placedItemStack) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (level.getBlockState(pos).getBlock() instanceof CableBusBlock &&
                blockEntity instanceof IPartHost host) {
            // Treat empty cable bus as passable for A* so depth 0 routing works.
            return host.getPart(null) == null;
        }
        return false;
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

    // IPipeReplacer: only the center (null-direction) cable part is swapped, side attachments stay put

    private record Ae2Kind(IPartItem<?> partItem, @Nullable AEColor color) {
    }

    @Nullable
    private static Ae2Kind kindAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof IPartHost host)) {
            return null;
        }
        IPart part = host.getPart(null);
        if (part == null) {
            return null;
        }
        IPartItem<?> partItem = part.getPartItem();
        AEColor color = partItem instanceof ColoredPartItem<?> colored ? colored.getColor() : null;
        return new Ae2Kind(partItem, color);
    }

    @Nullable
    private static AEColor colorOf(Item item) {
        return item instanceof ColoredPartItem<?> colored ? colored.getColor() : null;
    }

    @Override
    @Nullable
    public Object resolveKind(Level level, BlockPos pos, ItemStack offhandStack) {
        return kindAt(level, pos);
    }

    @Override
    public boolean matchesKind(Level level, BlockPos pos, Object kind) {
        Ae2Kind seedKind = (Ae2Kind) kind;
        Ae2Kind hereKind = kindAt(level, pos);
        return hereKind != null
                && hereKind.partItem().getPartClass() == seedKind.partItem().getPartClass()
                && Objects.equals(hereKind.color(), seedKind.color());
    }

    @Override
    public String describeKind(Object kind) {
        Ae2Kind ae2Kind = (Ae2Kind) kind;
        String id = BuiltInRegistries.ITEM.getKey(ae2Kind.partItem().asItem()).toString();
        return ae2Kind.color() != null ? id + "#" + ae2Kind.color().name() : id;
    }

    @Override
    public ItemStack getKindDisplayStack(Level level, BlockPos seedPos, Object kind) {
        return new ItemStack(((Ae2Kind) kind).partItem().asItem());
    }

    @Override
    public boolean isSameFamily(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack) {
        return offhandStack.getItem() instanceof IPartItem<?> partItem
                && ICablePart.class.isAssignableFrom(partItem.getPartClass());
    }

    @Override
    public boolean isAlreadyTarget(Level level, BlockPos seedPos, Object kind, ItemStack offhandStack) {
        Ae2Kind ae2Kind = (Ae2Kind) kind;
        return offhandStack.getItem() instanceof IPartItem<?> partItem
                && partItem.getPartClass() == ae2Kind.partItem().getPartClass()
                && Objects.equals(colorOf(offhandStack.getItem()), ae2Kind.color());
    }

    @Override
    public boolean replaceAt(Level level, BlockPos pos, Player player, ItemStack offhandStack, Object kind, List<Direction> adjacentInRun) {
        if (!(offhandStack.getItem() instanceof IPartItem<?> newPartItem)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof IPartHost host)) {
            return false;
        }
        IPart oldPart = host.getPart(null);
        if (oldPart == null) {
            return false;
        }
        IPartItem<?> oldPartItem = oldPart.getPartItem();

        List<ItemStack> drops = new ArrayList<>();
        oldPart.addPartDrop(drops, false);
        oldPart.addAdditionalDrops(drops, false);

        IPart newPart = host.replacePart(newPartItem, null, player, InteractionHand.OFF_HAND);
        if (newPart == null) {
            // replacePart removes before adding, put the old cable back so it isn't lost
            host.addPart(oldPartItem, null, player);
            return false;
        }
        for (ItemStack drop : drops) {
            if (!player.getInventory().add(drop)) {
                player.drop(drop, false);
            }
        }
        return true;
    }
}

