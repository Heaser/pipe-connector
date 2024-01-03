package com.heaser.pipeconnector.client.gui;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.client.gui.buttons.*;
import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import com.heaser.pipeconnector.client.gui.labels.DepthLabel;
import com.heaser.pipeconnector.client.gui.labels.InventoryGuardText;
import com.heaser.pipeconnector.client.gui.labels.TitleLabelText;
import com.heaser.pipeconnector.client.gui.labels.UtilizeExistingPipesText;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.heaser.pipeconnector.utils.GeneralUtils.isHoldingPipeConnector;

public class PipeConnectorGui extends Screen {
    public static final ResourceLocation PIPE_CONNECTOR_TEXTURE = new ResourceLocation(PipeConnector.MODID, "textures/gui/settings.png");
    protected static final Integer imageWidth = 256;
    protected static final Integer imageHeight = 256;
    private ItemStack pipeConnectorStack;
    private BaseButton resetBaseButton;
    private BaseButton buildPipesButton;
    private BaseButton bridgeTypeButton;
    private BaseButton utilizeExistingPipesButton;
    private BaseButton inventoryGuardButton;

    public PipeConnectorGui(ItemStack pipeConnectorStack) {
        super(Component.literal("PipeConnectorScreen"));
        this.pipeConnectorStack = pipeConnectorStack;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    protected void init() {
        bridgeTypeButton = createButton(0.05, 0.2, new BridgeTypeButton(pipeConnectorStack));
        inventoryGuardButton = createButton(0.45, 0.3, new InventoryGuardButton(pipeConnectorStack));
        utilizeExistingPipesButton = createButton(0.45, 0.4, new UtilizeExistingPipesButton(pipeConnectorStack));
        resetBaseButton = createButton(0.65, 0.7, new ResetButton());
        buildPipesButton = createButton(0.65, 0.8, new BuildPipesButton(this.getMinecraft().player));
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
        // Tooltips
        drawTooltip(guiGraphics, mouseX, mouseY, resetBaseButton);
        drawTooltip(guiGraphics, mouseX, mouseY, buildPipesButton);
        drawTooltipList(guiGraphics, mouseX, mouseY, bridgeTypeButton);
        drawTooltip(guiGraphics, mouseX, mouseY, inventoryGuardButton);
        drawTooltipList(guiGraphics, mouseX, mouseY, utilizeExistingPipesButton);
        guiGraphics.blit(PIPE_CONNECTOR_TEXTURE, drawStartX, drawStartY, 0, 0, imageWidth, imageHeight);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        utilizeExistingPipesButton.button.active = utilizeExistingPipesButton.isActive(pipeConnectorStack);
        buildPipesButton.button.active = buildPipesButton.isActive(pipeConnectorStack);

        // Labels
        createLabel(guiGraphics, 0.07, 0.05, new TitleLabelText(), 3f);
        createLabel(guiGraphics, 0.12, 0.32, new InventoryGuardText());
        createLabel(guiGraphics, 0.08, 0.42 , new UtilizeExistingPipesText());
        createLabel(guiGraphics, 0.65, 0.65, new DepthLabel());
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

    private void createLabel(GuiGraphics guiGraphics, double marginXPercent, double marginYPercent, ILabelable label) {
        int marginX = (int) (imageWidth * marginXPercent);
        int marginY = (int) (imageHeight * marginYPercent);
        int drawStartX = this.getScreenX() + marginX;
        int drawStartY = this.getScreenY() + marginY;

        guiGraphics.drawString(this.font, label.getLabel(pipeConnectorStack), drawStartX, drawStartY, 0x00000000, false);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void createLabel(GuiGraphics guiGraphics, double marginXPercent, double marginYPercent, ILabelable label, float scale) {
        int marginX = (int) (imageWidth * marginXPercent);
        int marginY = (int) (imageHeight * marginYPercent);
        int drawStartX = (this.getScreenX() + marginX) / (int)scale;;
        int drawStartY = (this.getScreenY() + marginY) / (int)scale;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale,scale,scale);
        guiGraphics.drawString(this.font, label.getLabel(pipeConnectorStack), drawStartX, drawStartY, 0x00000000, false);
        guiGraphics.pose().popPose();
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


