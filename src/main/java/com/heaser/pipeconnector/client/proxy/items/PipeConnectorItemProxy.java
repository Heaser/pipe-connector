package com.heaser.pipeconnector.items;

import com.heaser.pipeconnector.client.gui.PipeConnectorGui;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class PipeConnectorItem extends Item {
    public PipeConnectorItem(Properties properties) {
        super(properties);
    }

    // -----------------------------------------------------------------------------------------------------------------
    @ParametersAreNonnullByDefault
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack interactedItem = player.getItemInHand(useHand);
        if (level.isClientSide()) {
            BlockPos playerLookingAt = player.blockPosition().relative(player.getDirection());
            boolean isAir = level.getBlockState(playerLookingAt).isAir();
            boolean isShiftKeyDown = player.isShiftKeyDown();

            if (useHand == InteractionHand.MAIN_HAND && isShiftKeyDown && isAir) {
                Minecraft.getInstance().setScreen(new PipeConnectorGui(interactedItem));
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(useHand));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            components.add(Component.translatable("item.pipe_connector.tooltip.usageExplanation").withStyle(ChatFormatting.DARK_AQUA));
            components.add(Component.translatable("item.pipe_connector.tooltip.openGui").withStyle(ChatFormatting.BLUE));
            components.add(Component.translatable("item.pipe_connector.tooltip.changeDepthExplanation").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            components.add(Component.translatable("item.pipe_connector.tooltip.shiftForMoreInfo").withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, level, components, tooltipFlag);
    }

    // -----------------------------------------------------------------------------------------------------------------

}

