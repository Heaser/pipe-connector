package com.heaser.pipeconnector.client.gui;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.client.gui.buttons.*;
import com.heaser.pipeconnector.client.gui.editbox.DepthEditBox;
import com.heaser.pipeconnector.client.gui.interfaces.ILabelable;
import com.heaser.pipeconnector.client.gui.labels.*;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PipeConnectorGui extends Screen {
    public static final Identifier PIPE_CONNECTOR_TEXTURE = Identifier.fromNamespaceAndPath(PipeConnector.MODID, "textures/gui/settings.png");
    protected static final int imageWidth = 256;
    protected static final int imageHeight = 256;
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

        int depthControlY = (int) (imageHeight * 0.7) + getScreenY();
        int depthControlX = (int) (imageWidth * 0.247) + getScreenX();

        depthEditBox = addRenderableWidget(new DepthEditBox(this.font, depthControlX + 15, depthControlY, 25, 20, pipeConnectorStack));

        decreaseDepthButton = createButton(depthControlX, depthControlY, new DecreaseDepthButton(depthEditBox));
        increaseDepthButton = createButton(depthControlX + 15 + 25, depthControlY, new IncreaseDepthButton(depthEditBox));
        confirmDepthButton = createButton(depthControlX + 15 + 27 + 15, depthControlY, new ConfirmDepthButton(depthEditBox));
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int drawStartX = getScreenX();
        int drawStartY = getScreenY();

        drawTooltip(extractor, mouseX, mouseY, resetBaseButton);
        drawTooltip(extractor, mouseX, mouseY, buildPipesButton);
        drawTooltipList(extractor, mouseX, mouseY, bridgeTypeButton);
        drawTooltip(extractor, mouseX, mouseY, inventoryGuardButton);
        drawTooltipList(extractor, mouseX, mouseY, utilizeExistingPipesButton);
        drawTooltipList(extractor, mouseX, mouseY, avoidInventoryBlocksButton);
        drawTooltip(extractor, mouseX, mouseY, pipeVisionButton);
        drawTooltip(extractor, mouseX, mouseY, manhattanMirrorButton);
        drawTooltip(extractor, mouseX, mouseY, confirmDepthButton);

        extractor.blit(RenderPipelines.GUI_TEXTURED, PIPE_CONNECTOR_TEXTURE,
                drawStartX, drawStartY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

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
        confirmDepthButton.button.active = confirmDepthButton.isActive(pipeConnectorStack);
        if (outlinePreviewButton instanceof OutlinePreviewButton opb) {
            opb.updateLabel(pipeConnectorStack);
        }
        if (solidPreviewButton instanceof SolidPreviewButton spb) {
            spb.updateLabel(pipeConnectorStack);
        }

        createLabel(extractor, 0.07, 0.05, new TitleLabelText(), 3f);
        createLabel(extractor, 0.12, 0.32, new InventoryGuardText());
        createLabel(extractor, 0.08, 0.42, new AvoidInventoryBlocksText());
        createLabel(extractor, 0.08, 0.52, new UtilizeExistingPipesText());
        createLabel(extractor, 0.08, 0.62, new PipeVisionText());
        createLabel(extractor, 0.62, 0.55, new PreviewStyleText());
        createLabel(extractor, 0.12, 0.72, new DepthLabel());
        createLabel(extractor, 0.08, 0.79, new RequiredPipesText());
        createLabel(extractor, 0.08, 0.845, new AvailablePipesText());
        createLabel(extractor, 0.08, 0.90, new PipeCostStatusText());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256 || this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }

        if (this.depthEditBox.isFocused()) {
            if (keyCode == 257 || keyCode == 335) { // Enter and numpad enter
                if (this.depthEditBox.isValueValid()) {
                    confirmDepthButton.onClick(confirmDepthButton.button, pipeConnectorStack);
                    // Clear focus via the screen, not the widget — unfocusing the widget directly
                    // desyncs it from the screen's focus tracking and the box can't be re-clicked
                    this.setFocused(null);
                }
                return true;
            }
            if (this.depthEditBox.keyPressed(event) || this.depthEditBox.canConsumeInput()) {
                return true;
            }
        }

        return super.keyPressed(event);
    }

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

    private void createLabel(GuiGraphicsExtractor extractor, double marginXPercent, double marginYPercent, ILabelable label) {
        int marginX = (int) (imageWidth * marginXPercent);
        int marginY = (int) (imageHeight * marginYPercent);
        int drawStartX = this.getScreenX() + marginX;
        int drawStartY = this.getScreenY() + marginY;
        extractor.text(this.font, label.getLabel(pipeConnectorStack), drawStartX, drawStartY, 0xFF000000, false);
    }

    private void createLabel(GuiGraphicsExtractor extractor, double marginXPercent, double marginYPercent, ILabelable label, float scale) {
        int marginX = (int) (imageWidth * marginXPercent);
        int marginY = (int) (imageHeight * marginYPercent);
        int drawStartX = (this.getScreenX() + marginX) / (int) scale;
        int drawStartY = (this.getScreenY() + marginY) / (int) scale;

        extractor.pose().pushMatrix();
        extractor.pose().scale(scale, scale);
        extractor.text(this.font, label.getLabel(pipeConnectorStack), drawStartX, drawStartY, 0xFF000000, false);
        extractor.pose().popMatrix();
    }

    private void drawTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY, BaseButton baseButton) {
        Component tooltipText = baseButton.getTooltip(pipeConnectorStack);
        if (tooltipText != null && baseButton.button.isHovered()) {
            extractor.setTooltipForNextFrame(this.font, tooltipText, mouseX, mouseY);
        }
    }

    private void drawTooltipList(GuiGraphicsExtractor extractor, int mouseX, int mouseY, BaseButton baseButton) {
        List<Component> tooltipTextList = baseButton.getTooltipList(pipeConnectorStack);
        if (tooltipTextList != null && baseButton.button.isHovered()) {
            extractor.setComponentTooltipForNextFrame(this.font, tooltipTextList, mouseX, mouseY);
        }
    }

    private void onButtonClick(Button clickedButton, BaseButton baseButton) {
        baseButton.onClick(clickedButton, pipeConnectorStack);
        if (baseButton.shouldClose()) {
            this.onClose();
        }
    }

    private int getScreenX() {
        return (this.width - imageWidth) / 2;
    }

    private int getScreenY() {
        return (this.height - imageHeight) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
