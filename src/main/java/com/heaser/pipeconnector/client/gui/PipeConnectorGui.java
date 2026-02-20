package com.heaser.pipeconnector.client.gui;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.client.gui.buttons.*;
import com.heaser.pipeconnector.client.gui.editbox.DepthEditBox;
import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import com.heaser.pipeconnector.client.gui.labels.*;
import com.heaser.pipeconnector.utils.TagUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.heaser.pipeconnector.utils.GeneralUtils.isHoldingPipeConnector;

public class PipeConnectorGui extends Screen {
    public static final ResourceLocation PIPE_CONNECTOR_TEXTURE = ResourceLocation.fromNamespaceAndPath(PipeConnector.MODID, "textures/gui/settings.png");
    protected static final Integer imageWidth = 256;
    protected static final Integer imageHeight = 256;
    private ItemStack pipeConnectorStack;
    private BaseButton resetBaseButton;
    private BaseButton buildPipesButton;
    private BaseButton bridgeTypeButton;
    private BaseButton utilizeExistingPipesButton;
    private BaseButton inventoryGuardButton;
    private BaseButton avoidInventoryBlocksButton;
    private BaseButton pipeVisionButton;
    private BaseButton outlinePreviewButton;
    private BaseButton solidPreviewButton;
    private BaseButton manhattanMirrorButton;

    private DepthEditBox depthEditBox;
    private DecreaseDepthButton decreaseDepthButton;
    private IncreaseDepthButton increaseDepthButton;
    private ConfirmDepthButton confirmDepthButton;

    public PipeConnectorGui(ItemStack pipeConnectorStack) {
        super(Component.literal("PipeConnectorScreen"));
        this.pipeConnectorStack = pipeConnectorStack;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    protected void init() {
        bridgeTypeButton = createButton(0.05, 0.2, new BridgeTypeButton(pipeConnectorStack));
        manhattanMirrorButton = createButton(0.05, 0.2, new ManhattanMirrorButton(pipeConnectorStack));
        inventoryGuardButton = createButton(0.45, 0.3, new InventoryGuardButton(pipeConnectorStack));
        avoidInventoryBlocksButton = createButton(0.45, 0.4, new AvoidInventoryBlocksButton(pipeConnectorStack));
        utilizeExistingPipesButton = createButton(0.45, 0.5, new UtilizeExistingPipesButton(pipeConnectorStack));
        pipeVisionButton = createButton(0.45, 0.6, new PipeVisionButton(pipeConnectorStack));
        outlinePreviewButton = createButton(0.58, 0.6, new OutlinePreviewButton(pipeConnectorStack));
        solidPreviewButton = createButton(0.76, 0.6, new SolidPreviewButton(pipeConnectorStack));
        resetBaseButton = createButton(0.62, 0.7, new ResetButton());
        buildPipesButton = createButton(0.60, 0.8, new BuildPipesButton(this.getMinecraft().player));

        // Depth Control Widgets
        int depthControlY = (int) (imageHeight * 0.7) + getScreenY();
        int depthControlX = (int) (imageWidth * 0.247) + getScreenX();

        depthEditBox = addRenderableWidget(new DepthEditBox(this.font, depthControlX + 15, depthControlY, 25, 20, pipeConnectorStack));

        decreaseDepthButton = createButton(depthControlX, depthControlY, new DecreaseDepthButton(depthEditBox));
        increaseDepthButton = createButton(depthControlX + 15 + 25, depthControlY, new IncreaseDepthButton(depthEditBox));
        confirmDepthButton = createButton(depthControlX + 15 + 27 + 15, depthControlY, new ConfirmDepthButton(depthEditBox));
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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
        drawTooltipList(guiGraphics, mouseX, mouseY, avoidInventoryBlocksButton);
        drawTooltip(guiGraphics, mouseX, mouseY, pipeVisionButton);
        drawTooltip(guiGraphics, mouseX, mouseY, manhattanMirrorButton);
        guiGraphics.blit(PIPE_CONNECTOR_TEXTURE, drawStartX, drawStartY, 0, 0, imageWidth, imageHeight);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        utilizeExistingPipesButton.button.active = utilizeExistingPipesButton.isActive(pipeConnectorStack);
        avoidInventoryBlocksButton.button.active = avoidInventoryBlocksButton.isActive(pipeConnectorStack);
        pipeVisionButton.button.active = pipeVisionButton.isActive(pipeConnectorStack);

        if (bridgeTypeButton instanceof BridgeTypeButton btb) {
            btb.updateLabel(pipeConnectorStack);
        }
        
        boolean isDefaultPathfinding = TagUtils.getBridgeType(pipeConnectorStack) == com.heaser.pipeconnector.constants.BridgeType.DEFAULT;
        manhattanMirrorButton.button.visible = isDefaultPathfinding;
        if (isDefaultPathfinding) {
            manhattanMirrorButton.button.setX(bridgeTypeButton.button.getX() + bridgeTypeButton.button.getWidth() + 5);
            if (manhattanMirrorButton instanceof ManhattanMirrorButton mmb) {
                mmb.updateLabel(pipeConnectorStack);
            }
        }

        buildPipesButton.button.active = buildPipesButton.isActive(pipeConnectorStack);
        if (outlinePreviewButton instanceof OutlinePreviewButton opb) {
            opb.updateLabel(pipeConnectorStack);
        }
        if (solidPreviewButton instanceof SolidPreviewButton spb) {
            spb.updateLabel(pipeConnectorStack);
        }

        // Labels
        createLabel(guiGraphics, 0.07, 0.05, new TitleLabelText(), 3f);
        createLabel(guiGraphics, 0.12, 0.32, new InventoryGuardText());
        createLabel(guiGraphics, 0.08, 0.42, new AvoidInventoryBlocksText());
        createLabel(guiGraphics, 0.08, 0.52, new UtilizeExistingPipesText());
        createLabel(guiGraphics, 0.08, 0.62, new PipeVisionText());
        createLabel(guiGraphics, 0.62, 0.55, new PreviewStyleText());
        createLabel(guiGraphics, 0.12, 0.72, new DepthLabel());
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.minecraft.level == null) {
            this.renderPanorama(pGuiGraphics, pPartialTick);
        }

        this.renderMenuBackground(pGuiGraphics);

        NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Post(this, pGuiGraphics, pMouseX, pMouseY, pPartialTick));
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || (this.minecraft.options.keyInventory.matches(keyCode, scanCode))) {
            this.onClose();
            return true;
        }

        if (this.depthEditBox.isFocused()) {
            if (keyCode == 257 || keyCode == 335) { // Enter and numpad enter
                confirmDepthButton.onClick(confirmDepthButton.button, pipeConnectorStack);
                this.depthEditBox.setFocused(false);
                return true;
            }
            if (this.depthEditBox.keyPressed(keyCode, scanCode, modifiers) || this.depthEditBox.canConsumeInput()) {
                return true;
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private <T extends BaseButton> T createButton(double marginXPercent, double marginYPercent, T baseButton) {
        int marginX = (int) (imageWidth * marginXPercent);
        int marginY = (int) (imageHeight * marginYPercent);
        int drawStartX = this.getScreenX() + marginX;
        int drawStartY = this.getScreenY() + marginY;

        return createButton(drawStartX, drawStartY, baseButton);
    }

    private <T extends BaseButton> T createButton(int x, int y, T baseButton) {
        Button button = Button.builder(baseButton.label, clickedButton -> onButtonClick(clickedButton, baseButton))
                .pos(x, y)
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
        int drawStartX = (this.getScreenX() + marginX) / (int)scale;
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