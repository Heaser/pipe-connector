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
import com.heaser.pipeconnector.client.ClientSetup;
import com.heaser.pipeconnector.compatibility.CompatibilityPipeReplacer;
import com.heaser.pipeconnector.compatibility.interfaces.IPipeReplacer;
import com.heaser.pipeconnector.network.UpdatePipeVision;
import com.heaser.pipeconnector.network.UpdateUtilizeExistingPipes;
import com.heaser.pipeconnector.utils.GeneralUtils;
import com.heaser.pipeconnector.utils.ReplaceSeed;
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
    private static final int MODE_TABS_Y = 8;

    private static final int LABEL_COLOR = 0xFF000000;
    private static final int HEADER_COLOR = 0xFF5A5A5A;
    private static final int HEADER_LINE_COLOR = 0xFF9D9D9D;

    private ItemStack pipeConnectorStack;
    private BaseButton resetBaseButton;
    private BaseButton buildPipesButton;
    private BaseButton bridgeTypeButton;
    private BaseButton outlinePreviewButton;
    private BaseButton solidPreviewButton;
    private BaseButton buildModeButton;
    private BaseButton replaceModeButton;

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
        // Mode tabs (top right)
        buildModeButton = createButton(x0 + RIGHT_X, y0 + MODE_TABS_Y, new BuildModeButton(pipeConnectorStack));
        replaceModeButton = createButton(x0 + RIGHT_X + RIGHT_WIDTH / 2, y0 + MODE_TABS_Y, new ReplaceModeButton(pipeConnectorStack));

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
        drawTooltip(extractor, mouseX, mouseY, buildModeButton);
        drawTooltip(extractor, mouseX, mouseY, replaceModeButton);

        extractor.blit(RenderPipelines.GUI_TEXTURED, PIPE_CONNECTOR_TEXTURE,
                x0, y0, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Title at 1.5x so it clears the mode tabs
        extractor.pose().pushMatrix();
        extractor.pose().scale(1.5f, 1.5f);
        extractor.text(this.font, this.getTitle(), Math.round((x0 + LEFT_X) / 1.5f), Math.round((y0 + TITLE_Y + 1) / 1.5f), LABEL_COLOR, false);
        extractor.pose().popMatrix();

        boolean isAStar = TagUtils.getBridgeType(pipeConnectorStack) == BridgeType.A_STAR;
        boolean replaceMode = TagUtils.getReplaceMode(pipeConnectorStack);

        // The left column belongs to the active mode
        bridgeTypeButton.button.visible = !replaceMode;
        depthEditBox.visible = !replaceMode;
        depthSlider.visible = !replaceMode;
        mirrorCheckbox.visible = !replaceMode && !isAStar;
        inventoryGuardCheckbox.visible = !replaceMode;
        avoidInventoryBlocksCheckbox.visible = !replaceMode;
        utilizeExistingPipesCheckbox.visible = !replaceMode;

        if (replaceMode) {
            drawSectionHeader(extractor, "item.pipe_connector.gui.section.replace", x0 + LEFT_X, y0 + HEADER_TOP_Y, LEFT_WIDTH);
            extractor.text(this.font, Component.translatable("item.pipe_connector.gui.label.replaceHint1"),
                    x0 + LEFT_X, y0 + ROW_1_Y, LABEL_COLOR, false);
            extractor.text(this.font, Component.translatable("item.pipe_connector.gui.label.replaceHint2"),
                    x0 + LEFT_X, y0 + ROW_1_Y + 12, LABEL_COLOR, false);
            extractor.text(this.font, Component.translatable("item.pipe_connector.gui.label.replaceHint3"),
                    x0 + LEFT_X, y0 + ROW_1_Y + 24, LABEL_COLOR, false);
            extractor.text(this.font, Component.translatable("item.pipe_connector.gui.label.replaceHint4"),
                    x0 + LEFT_X, y0 + ROW_1_Y + 36, HEADER_COLOR, false);

            drawSectionHeader(extractor, "item.pipe_connector.gui.section.selection", x0 + LEFT_X, y0 + HEADER_BOTTOM_Y, LEFT_WIDTH);
            if (!drawSwapReadout(extractor, x0 + LEFT_X, y0 + TOGGLE_ROW_1_Y)) {
                extractor.text(this.font, Component.translatable("item.pipe_connector.gui.label.noRunSelected"),
                        x0 + LEFT_X, y0 + TOGGLE_ROW_1_Y + 4, LABEL_COLOR, false);
            }
        } else {
            drawSectionHeader(extractor, "item.pipe_connector.gui.section.pathfinding", x0 + LEFT_X, y0 + HEADER_TOP_Y, LEFT_WIDTH);
            drawSectionHeader(extractor, "item.pipe_connector.gui.section.buildOptions", x0 + LEFT_X, y0 + HEADER_BOTTOM_Y, LEFT_WIDTH);
        }
        drawSectionHeader(extractor, "item.pipe_connector.gui.label.preview_style", x0 + RIGHT_X, y0 + HEADER_TOP_Y, RIGHT_WIDTH);
        drawSectionHeader(extractor, "item.pipe_connector.gui.section.pipes", x0 + RIGHT_X, y0 + HEADER_BOTTOM_Y, RIGHT_WIDTH);

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
        if (buildModeButton instanceof BuildModeButton bmb) {
            bmb.updateLabel(pipeConnectorStack);
        }
        if (replaceModeButton instanceof ReplaceModeButton rmb) {
            rmb.updateLabel(pipeConnectorStack);
        }
        buildPipesButton.button.active = buildPipesButton.isActive(pipeConnectorStack);
        resetBaseButton.button.active = resetBaseButton.isActive(pipeConnectorStack);
        resetBaseButton.button.setMessage(Component.translatable(replaceMode
                ? "item.pipe_connector.gui.button.ClearSelection"
                : "item.pipe_connector.gui.button.ResetPipePos"));
        buildPipesButton.button.setMessage(Component.translatable(replaceMode
                ? "item.pipe_connector.gui.button.ReplacePipes"
                : "item.pipe_connector.gui.button.PlacePipes"));

        // Toggle labels — black text left of each checkbox
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.mirrorPath", x0 + LEFT_X, mirrorCheckbox);
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.preventBlockWithInventoryDestruction", x0 + LEFT_X, inventoryGuardCheckbox);
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.avoidInventoryBlocks", x0 + LEFT_X, avoidInventoryBlocksCheckbox);
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.utilizeExistingPipes", x0 + LEFT_X, utilizeExistingPipesCheckbox);
        drawWidgetLabel(extractor, "item.pipe_connector.gui.label.pipe_vision", x0 + RIGHT_X, pipeVisionCheckbox);

        // Pipes section — offhand item icon + cost readout
        var player = this.getMinecraft().player;
        boolean showCost = player != null && GeneralUtils.isPlaceableBlock(player)
                && PipeCostCalculator.hasSelection(pipeConnectorStack);
        if (showCost) {
            extractor.item(player.getOffhandItem(), x0 + RIGHT_X, y0 + TOGGLE_ROW_1_Y);
            extractor.text(this.font, new RequiredPipesText().getLabel(pipeConnectorStack),
                    x0 + RIGHT_X + 20, y0 + TOGGLE_ROW_1_Y + 1, LABEL_COLOR, false);
            extractor.text(this.font, new AvailablePipesText().getLabel(pipeConnectorStack),
                    x0 + RIGHT_X + 20, y0 + TOGGLE_ROW_1_Y + 11, LABEL_COLOR, false);
            Component statusLabel = new PipeCostStatusText().getLabel(pipeConnectorStack);
            extractor.text(this.font, statusLabel,
                    x0 + RIGHT_X + 20, y0 + TOGGLE_ROW_1_Y + 21, LABEL_COLOR, false);
            if (replaceMode) {
                // Returned = the old pipes coming back, takes the status line's slot when it's free
                int returnedY = y0 + TOGGLE_ROW_1_Y + (statusLabel.getString().isEmpty() ? 21 : 31);
                ItemStack returnedStack = resolveSelectedRunStack();
                if (!returnedStack.isEmpty()) {
                    extractor.item(returnedStack, x0 + RIGHT_X, returnedY - 4);
                }
                extractor.text(this.font, Component.translatable("item.pipe_connector.gui.label.returnedPipes",
                                String.valueOf(ClientSetup.PREVIEW_DRAWER.previewMap.size())),
                        x0 + RIGHT_X + 20, returnedY, LABEL_COLOR, false);
            }
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

    // Empty when nothing is selected or the seed went stale
    private ItemStack resolveSelectedRunStack() {
        var player = this.getMinecraft().player;
        ReplaceSeed seed = TagUtils.getReplaceSeed(pipeConnectorStack);
        if (player == null || seed == null) {
            return ItemStack.EMPTY;
        }
        var level = player.level();
        IPipeReplacer replacer = CompatibilityPipeReplacer.getReplacerFor(level, seed.pos());
        Object kind = replacer.resolveKind(level, seed.pos(), player.getOffhandItem());
        if (kind == null || !replacer.describeKind(kind).equals(seed.kindDescriptor())) {
            return ItemStack.EMPTY;
        }
        return replacer.getKindDisplayStack(level, seed.pos(), kind);
    }

    // Returns false when there is no valid selection to show
    private boolean drawSwapReadout(GuiGraphicsExtractor extractor, int x, int y) {
        var player = this.getMinecraft().player;
        ReplaceSeed seed = TagUtils.getReplaceSeed(pipeConnectorStack);
        if (player == null || seed == null) {
            return false;
        }
        var level = player.level();
        IPipeReplacer replacer = CompatibilityPipeReplacer.getReplacerFor(level, seed.pos());
        Object kind = replacer.resolveKind(level, seed.pos(), player.getOffhandItem());
        if (kind == null || !replacer.describeKind(kind).equals(seed.kindDescriptor())) {
            return false;
        }

        ItemStack oldStack = replacer.getKindDisplayStack(level, seed.pos(), kind);
        Component oldName = oldStack.isEmpty() ? level.getBlockState(seed.pos()).getBlock().getName() : oldStack.getHoverName();
        drawPipeRow(extractor, oldStack, oldName, x, y);

        if (GeneralUtils.isPlaceableBlock(player)) {
            drawDownArrow(extractor, x + 8, y + 21);
            drawPipeRow(extractor, player.getOffhandItem(), player.getOffhandItem().getHoverName(), x, y + 28);
        }
        return true;
    }

    private void drawPipeRow(GuiGraphicsExtractor extractor, ItemStack stack, Component name, int x, int y) {
        if (!stack.isEmpty()) {
            extractor.item(stack, x, y);
        }
        int width = LEFT_WIDTH - 20;
        String full = name.getString();
        String line1 = this.font.plainSubstrByWidth(full, width);
        if (line1.length() < full.length()) {
            int lastSpace = line1.lastIndexOf(' ');
            if (lastSpace > 0) {
                line1 = line1.substring(0, lastSpace);
            }
        }
        String rest = full.substring(line1.length()).trim();
        if (rest.isEmpty()) {
            extractor.text(this.font, Component.literal(line1), x + 20, y + 4, LABEL_COLOR, false);
        } else {
            extractor.text(this.font, Component.literal(line1), x + 20, y, LABEL_COLOR, false);
            extractor.text(this.font, Component.literal(this.font.plainSubstrByWidth(rest, width)), x + 20, y + 10, LABEL_COLOR, false);
        }
    }

    private void drawDownArrow(GuiGraphicsExtractor extractor, int centerX, int y) {
        for (int i = 0; i < 5; i++) {
            extractor.horizontalLine(centerX - (4 - i), centerX + (4 - i) + 1, y + i, HEADER_COLOR);
        }
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
