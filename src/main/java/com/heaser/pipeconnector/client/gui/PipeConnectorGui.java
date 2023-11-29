package com.heaser.pipeconnector.client.gui;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.client.gui.buttons.*;
import com.heaser.pipeconnector.client.gui.labels.BaseLabel;
import com.heaser.pipeconnector.client.gui.labels.DepthLabel;
import com.heaser.pipeconnector.items.PipeConnectorItem;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.heaser.pipeconnector.utils.GeneralUtils.isHoldingPipeConnector;

public class PipeConnectorGui extends Screen {
    public static final ResourceLocation PIPE_CONNECTOR_TEXTURE = new ResourceLocation(PipeConnector.MODID, "textures/gui/settings.png");
    protected static final Integer imageWidth = 256;
    protected static final Integer imageHeight = 256;
    private ItemStack pipeConnectorStack;
    private BaseButton resetBaseButton;
    private BaseButton buildBasePipesButton;
    private BaseButton bridgeTypeButton;
    private BaseButton bridgeTypeInfoButton;

    public PipeConnectorGui(ItemStack pipeConnectorStack) {
        super(Component.literal("PipeConnectorScreen"));
        this.pipeConnectorStack = pipeConnectorStack;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    protected void init() {
        bridgeTypeButton = createButton(0.05, 0.2, new BridgeTypeButton(pipeConnectorStack));
        bridgeTypeInfoButton = createButton(0.45, 0.2, new BridgeTypeInfoButton());
        resetBaseButton = createButton(0.65, 0.7, new ResetButton());
        buildBasePipesButton = createButton(0.65, 0.8, new BuildPipesButton(this.getMinecraft().player));
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.getMinecraft().player != null && isHoldingPipeConnector(this.getMinecraft().player)) {
            pipeConnectorStack = this.getMinecraft().player.getMainHandItem();
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PIPE_CONNECTOR_TEXTURE);
        int drawStartX = getScreenX();
        int drawStartY = getScreenY();
        drawTooltip(guiGraphics, mouseX, mouseY, resetBaseButton);
        drawTooltip(guiGraphics, mouseX, mouseY, buildBasePipesButton);
        drawTooltip(guiGraphics, mouseX, mouseY, bridgeTypeButton);
        drawTooltipList(guiGraphics, mouseX, mouseY, bridgeTypeInfoButton);
        guiGraphics.blit(PIPE_CONNECTOR_TEXTURE, drawStartX, drawStartY, 0, 0, imageWidth, imageHeight);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        createLabel(guiGraphics, 0.65, 0.6, new DepthLabel());
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 256 && this.shouldCloseOnEsc()) || keyCode == 69) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private BaseButton createButton(double marginXPercent, double marginYPercent, BaseButton baseButton) {
        int marginX = (int) (imageWidth * marginXPercent);
        int marginY = (int) (imageHeight * marginYPercent);
        int drawStartX = this.getScreenX() + marginX;
        int drawStartY = this.getScreenY() + marginY;

        Button button = Button.builder(baseButton.label, clickedButton -> onButtonClick(clickedButton, baseButton))
                .pos(drawStartX, drawStartY)
                .size(baseButton.getButtonWidth(), baseButton.getButtonHeight())
                .build();
        button.active = baseButton.isActive(pipeConnectorStack);
        baseButton.bindButton(button);
        addRenderableWidget(button);
        return baseButton;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void createLabel(GuiGraphics guiGraphics, double marginXPercent, double marginYPercent, BaseLabel label) {
        int marginX = (int) (imageWidth * marginXPercent);
        int marginY = (int) (imageHeight * marginYPercent);
        int drawStartX = this.getScreenX() + marginX;
        int drawStartY = this.getScreenY() + marginY;

        guiGraphics.drawString(this.font, label.getLabel(pipeConnectorStack), drawStartX, drawStartY, 12181157);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, BaseButton baseButton) {
        Component tooltipText = baseButton.getTooltip(pipeConnectorStack);
        if(tooltipText != null && baseButton.button.isHovered()) {
            guiGraphics.renderTooltip(this.font, tooltipText, mouseX, mouseY);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void drawTooltipList(GuiGraphics guiGraphics, int mouseX, int mouseY, BaseButton baseButton) {
        List<Component> tooltipTextList = baseButton.getTooltipList(pipeConnectorStack);
        if(tooltipTextList != null && baseButton.button.isHovered()) {
            guiGraphics.renderTooltip(this.font, tooltipTextList,
                    java.util.Optional.empty(),  mouseX, mouseY);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void onButtonClick(Button clickedButton, BaseButton baseButton) {
        baseButton.onClick(clickedButton, pipeConnectorStack);
        if (baseButton.shouldClose()) {
            this.onClose();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private int getScreenX() {
        return (this.width - imageWidth) / 2;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private int getScreenY() {
        return (this.height - imageHeight) / 2;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}


