package com.heaser.pipeconnector.client.proxy.items;

import com.heaser.pipeconnector.client.gui.PipeConnectorGui;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class PipeConnectorItemProxy implements IPipeConnectorItemProxy {
    // -----------------------------------------------------------------------------------------------------------------
    public void openPipeConnectorGui(ItemStack interactedItem) {
        Minecraft.getInstance().setScreen(new PipeConnectorGui(interactedItem));
    }
}

