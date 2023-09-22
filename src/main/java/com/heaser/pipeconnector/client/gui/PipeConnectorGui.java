package com.heaser.pipeconnector.client.gui;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.network.NetworkHandler;
import com.heaser.pipeconnector.network.ResetPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)


public class PipeConnectorGui extends Screen {
    public static final ResourceLocation PIPE_CONNECTOR_TEXTURE = new ResourceLocation(PipeConnector.MODID, "textures/gui/settings.png");
    protected static final Integer imageWidth = 256;
    protected static final Integer imageHeight = 256;
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;

    public PipeConnectorGui() {
        super(Component.literal("PipeConnectorScreen"));

    }

    @Override
    protected void init() {
        createButton();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PIPE_CONNECTOR_TEXTURE);
        int drawStartX = (this.width - PipeConnectorGui.imageWidth) / 2;
        int drawStartY = (this.height - PipeConnectorGui.imageHeight) / 2;
        blit(poseStack, drawStartX, drawStartY, 0, 0, PipeConnectorGui.imageWidth, PipeConnectorGui.imageHeight);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void createButton() {
        int drawStartX = (this.width - PipeConnectorGui.imageWidth) / 2;
        int drawStartY = (this.height - PipeConnectorGui.imageHeight) / 2;
        Button button = new Button((int)(drawStartX + imageWidth * 0.1), (int) (drawStartY + imageHeight * 0.8), BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("Reset"), clickedButton -> clickReset());
        button.active = true;
        addRenderableWidget(button);
    }

    private void clickReset() {
        NetworkHandler.CHANNEL.sendToServer(new ResetPacket());
    }
}


