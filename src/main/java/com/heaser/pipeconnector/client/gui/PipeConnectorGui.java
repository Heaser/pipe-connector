package com.heaser.pipeconnector.client.gui;

import com.heaser.pipeconnector.PipeConnector;
import com.heaser.pipeconnector.client.gui.buttons.*;
import com.heaser.pipeconnector.client.gui.editbox.DepthEditBox;
import com.heaser.pipeconnector.client.gui.labels.AvailablePipesText;
import com.heaser.pipeconnector.client.gui.labels.PipeCostStatusText;
import com.heaser.pipeconnector.client.gui.labels.RequiredPipesText;
import com.heaser.pipeconnector.config.PipeConnectorConfig;
import com.heaser.pipeconnector.constants.BridgeType;
import com.heaser.pipeconnector.network.UpdateAvoidInventoryBlocks;
import com.heaser.pipeconnector.network.UpdateDepthPacket;
import com.heaser.pipeconnector.network.UpdateInventoryGuard;
import com.heaser.pipeconnector.network.UpdateManhattanMirrorPacket;
import com.heaser.pipeconnector.network.UpdatePipeVision;
import com.heaser.pipeconnector.network.UpdateUtilizeExistingPipes;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PipeConnectorGui extends Screen {
    public static final Identifier PIPE_CONNECTOR_TEXTURE = Identifier.fromNamespaceAndPath(PipeConnector.MODID, "textures/gui/settings.png");
    protected static final int imageWidth = 256;
    protected static final int imageHeight = 256;

    // Layout grid — two columns on a fixed row pitch
    private static final int LEFT_X = 16;
    private static final int LEFT_WIDTH = 118;
    private static final int RIGHT_X = 146;
    private static final int RIGHT_WIDTH = 96;
    private static final int TITLE_Y = 10;
    private static final int HEADER_TOP_Y = 36;
    private static final int ROW_1_Y = 48;
    private static final int ROW_2_Y = 72;
    private static final int ROW_3_Y = 96;
    private static final int HEADER_BOTTOM_Y = 124;
    private static final int TOGGLE_ROW_1_Y = 136;
    private static final int TOGGLE_ROW_2_Y = 158;
    private static final int TOGGLE_ROW_3_Y = 180;
    private static final int ACTIONS_Y = 212;

    private static final int LABEL_COLOR = 0xFF000000;
    private static final int HEADER_COLOR = 0xFF5A5A5A;
    private static final int HEADER_LINE_COLOR = 0xFF9D9D9D;

    private ItemStack pipeConnectorStack;
    private BaseButton resetBaseButton;
    private BaseButton buildPipesButton;
    private BaseButton bridgeTypeButton;
    private BaseButton outlinePreviewButton;
    private BaseButton solidPreviewButton;

    private Checkbox mirrorCheckbox;
    private Checkbox inventoryGuardCheckbox;
    private Checkbox avoidInventoryBlocksCheckbox;
    private Checkbox utilizeExistingPipesCheckbox;
    private Checkbox pipeVisionCheckbox;

    private DepthEditBox depthEditBox;
    private DepthSlider depthSlider;

    public PipeConnectorGui(ItemStack pipeConnectorStack) {
        super(Component.translatable("item.pipe_connector.pipe_connector"));
        this.pipeConnectorStack = pipeConnectorStack;
    }

    @Override
    protected void init() {
        int x0 = getScreenX();
        int y0 = getScreenY();
        int boxSize = Checkbox.getBoxSize(this.font);
        int leftBoxX = x0 + LEFT_X + LEFT_WIDTH - boxSize;
        int rightBoxX = x0 + RIGHT_X + RIGHT_WIDTH - boxSize;

        // Pathfinding section (left)
        bridgeTypeButton = createButton(x0 + LEFT_X, y0 + ROW_1_Y, new BridgeTypeButton(pipeConnectorStack));
        depthEditBox = addRenderableWidget(new DepthEditBox(this.font, x0 + LEFT_X + 88, y0 + ROW_2_Y, 30, 20, pipeConnectorStack));
        depthSlider = addRenderableWidget(new DepthSlider(x0 + LEFT_X, y0 + ROW_2_Y, 84, 20, pipeConnectorStack, depthEditBox));

        // Mirror sits on the last row so the gap it leaves in A* mode is at the section's bottom edge
        mirrorCheckbox = createToggle(leftBoxX, y0 + ROW_3_Y, TagUtils.getMirrorManhattan(pipeConnectorStack), (checkbox, value) -> {
            TagUtils.setMirrorManhattan(pipeConnectorStack, value);
            ClientPacketDistributor.sendToServer(new UpdateManhattanMirrorPacket(value));
        });
        mirrorCheckbox.setTooltip(Tooltip.create(Component.translatable("item.pipe_connector.gui.button.tooltip.manhattanMirror")));

        // Safety section (left)
        inventoryGuardCheckbox = createToggle(leftBoxX, y0 + TOGGLE_ROW_1_Y, TagUtils.getPreventInventoryBlockBreaking(pipeConnectorStack), (checkbox, value) -> {
            TagUtils.setInventoryGuard(pipeConnectorStack, value);
            ClientPacketDistributor.sendToServer(new UpdateInventoryGuard(value));
        });
        inventoryGuardCheckbox.setTooltip(Tooltip.create(Component.translatable("item.pipe_connector.gui.button.tooltip.InventoryGuard")));
        avoidInventoryBlocksCheckbox = createToggle(leftBoxX, y0 + TOGGLE_ROW_2_Y, TagUtils.getAvoidInventoryBlocks(pipeConnectorStack), (checkbox, value) -> {
            TagUtils.setAvoidInventoryBlocks(pipeConnectorStack, value);
            ClientPacketDistributor.sendToServer(new UpdateAvoidInventoryBlocks(value));
        });
        utilizeExistingPipesCheckbox = createToggle(leftBoxX, y0 + TOGGLE_ROW_3_Y, TagUtils.getUtilizeExistingPipes(pipeConnectorStack), (checkbox, value) -> {
            TagUtils.setUtilizeExistingPipes(pipeConnectorStack, value);
            ClientPacketDistributor.sendToServer(new UpdateUtilizeExistingPipes(value));
        });

        // Preview section (right)
        outlinePreviewButton = createButton(x0 + RIGHT_X, y0 + ROW_1_Y, new OutlinePreviewButton(pipeConnectorStack));
        solidPreviewButton = createButton(x0 + RIGHT_X + RIGHT_WIDTH / 2, y0 + ROW_1_Y, new SolidPreviewButton(pipeConnectorStack));
        pipeVisionCheckbox = createToggle(rightBoxX, y0 + ROW_2_Y, TagUtils.getPipeVision(pipeConnectorStack), (checkbox, value) -> {
            TagUtils.setPipeVision(pipeConnectorStack, value);
            ClientPacketDistributor.sendToServer(new UpdatePipeVision(value));
        });

        // Actions
        resetBaseButton = createButton(x0 + LEFT_X, y0 + ACTIONS_Y, new ResetButton());
        buildPipesButton = createButton(x0 + RIGHT_X, y0 + ACTIONS_Y, new BuildPipesButton(this.getMinecraft().player));
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x0 = getScreenX();
        int y0 = getScreenY();

        drawTooltipList(extractor, mouseX, mouseY, bridgeTypeButton);
        drawTooltip(extractor, mouseX, mouseY, resetBaseButton);
        drawTooltip(extractor, mouseX, mouseY, buildPipesButton);

        extractor.blit(RenderPipelines.GUI_TEXTURED, PIPE_CONNECTOR_TEXTURE,
                x0, y0, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Title
        extractor.pose().pushMatrix();
        extractor.pose().scale(2f, 2f);
        extractor.text(this.font, this.getTitle(), (x0 + LEFT_X) / 2, (y0 + TITLE_Y) / 2, LABEL_COLOR, false);
        extractor.pose().popMatrix();

        drawSectionHeader(extractor, "item.pipe_connector.gui.section.pathfinding", x0 + LEFT_X, y0 + HEADER_TOP_Y, LEFT_WIDTH);
        drawSectionHeader(extractor, "item.pipe_connector.gui.label.preview_style", x0 + RIGHT_X, y0 + HEADER_TOP_Y, RIGHT_WIDTH);
        drawSectionHeader(extractor, "item.pipe_connector.gui.section.buildOptions", x0 + LEFT_X, y0 + HEADER_BOTTOM_Y, LEFT_WIDTH);
        drawSectionHeader(extractor, "item.pipe_connector.gui.section.pipes", x0 + RIGHT_X, y0 + HEADER_BOTTOM_Y, RIGHT_WIDTH);

        boolean isAStar = TagUtils.getBridgeType(pipeConnectorStack) == BridgeType.A_STAR;
        mirrorCheckbox.visible = !isAStar;
        avoidInventoryBlocksCheckbox.active = isAStar;
        utilizeExistingPipesCheckbox.active = isAStar;
        avoidInventoryBlocksCheckbox.setTooltip(Tooltip.create(isAStar
                ? Component.translatable("item.pipe_connector.gui.button.tooltip.avoidInventoryBlocks")
                : Component.translatable("item.pipe_connector.gui.button.tooltip.inactiveUtilizeExistingPipes")));
        utilizeExistingPipesCheckbox.setTooltip(Tooltip.create(isAStar
                ? Component.translatable("item.pipe_connector.gui.button.tooltip.utilizeExistingPipesInfoPartOne")
                        .append(" ")
                        .append(Component.translatable("item.pipe_connector.gui.button.tooltip.utilizeExistingPipesInfoPartTwo"))
                : Component.translatable("item.pipe_connector.gui.button.tooltip.inactiveUtilizeExistingPipes")));
        depthEditBox.setTooltip(depthEditBox.isValueValid() ? null
                : Tooltip.create(Component.translatable("item.pipe_connector.gui.tooltip.depthOutOfRange", PipeConnectorConfig.MAX_DEPTH.get())));

        if (bridgeTypeButton instanceof BridgeTypeButton btb) {
            btb.updateLabel(pipeConnectorStack);
        }
        if (outlinePreviewButton instanceof OutlinePreviewButton opb) {
            opb.updateLabel(pipeConnectorStack);
        }
        if (solidPreviewButton instanceof SolidPreviewButton spb) {
            spb.updateLabel(pipeConnectorStack);
        }
        buildPipesButton.button.active = buildPipesButton.isActive(pipeConnectorStack);

        // Toggle labels — black text left of each checkbox
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.mirrorPath", x0 + LEFT_X, mirrorCheckbox);
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.preventBlockWithInventoryDestruction", x0 + LEFT_X, inventoryGuardCheckbox);
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.avoidInventoryBlocks", x0 + LEFT_X, avoidInventoryBlocksCheckbox);
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.utilizeExistingPipes", x0 + LEFT_X, utilizeExistingPipesCheckbox);
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.pipe_vision", x0 + RIGHT_X, pipeVisionCheckbox);

        // Pipes section — offhand item icon + cost readout
        var player = this.getMinecraft().player;
        boolean showCost = player != null && GeneralUtils.isPlaceableBlock(player)
                && TagUtils.getNodesFromStack(pipeConnectorStack).size() >= 2;
        if (showCost) {
            extractor.item(player.getOffhandItem(), x0 + RIGHT_X, y0 + TOGGLE_ROW_1_Y);
            extractor.text(this.font, new RequiredPipesText().getLabel(pipeConnectorStack),
                    x0 + RIGHT_X + 20, y0 + TOGGLE_ROW_1_Y + 1, LABEL_COLOR, false);
            extractor.text(this.font, new AvailablePipesText().getLabel(pipeConnectorStack),
                    x0 + RIGHT_X + 20, y0 + TOGGLE_ROW_1_Y + 11, LABEL_COLOR, false);
            extractor.text(this.font, new PipeCostStatusText().getLabel(pipeConnectorStack),
                    x0 + RIGHT_X + 20, y0 + TOGGLE_ROW_1_Y + 21, LABEL_COLOR, false);
        }

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
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
                    applyTypedDepth();
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

    private void applyTypedDepth() {
        int depth;
        try {
            depth = Math.min(Integer.parseInt(depthEditBox.getValue()), PipeConnectorConfig.MAX_DEPTH.get());
        } catch (NumberFormatException e) {
            return;
        }
        TagUtils.setDepthToStack(pipeConnectorStack, depth);
        ClientPacketDistributor.sendToServer(new UpdateDepthPacket(depth));
        depthEditBox.updateValue(depth);
        depthSlider.syncDepth(depth);
    }

    private Checkbox createToggle(int x, int y, boolean selected, Checkbox.OnValueChange onValueChange) {
        return addRenderableWidget(Checkbox.builder(Component.empty(), this.font)
                .pos(x, y)
                .selected(selected)
                .onValueChange(onValueChange)
                .build());
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

    private void drawSectionHeader(GuiGraphicsExtractor extractor, String translationKey, int x, int y, int width) {
        Component header = Component.translatable(translationKey);
        extractor.text(this.font, header, x, y, HEADER_COLOR, false);
        int textEnd = x + this.font.width(header) + 4;
        if (textEnd < x + width) {
            extractor.horizontalLine(textEnd, x + width, y + 4, HEADER_LINE_COLOR);
        }
    }

    private void drawWidgetLabel(GuiGraphicsExtractor extractor, String translationKey, int x, AbstractWidget widget) {
        if (!widget.visible) {
            return;
        }
        extractor.text(this.font, Component.translatable(translationKey), x, widget.getY() + 5, LABEL_COLOR, false);
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
