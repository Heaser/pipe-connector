package com.heaser.pipeconnector.client.gui.labels;
import com.heaser.pipeconnector.utils.TagUtils;
import net.minecraft.world.item.ItemStack;

public class DepthLabel extends BaseLabel {
    public DepthLabel() {
        super("item.pipe_connector.gui.label.depth");
    }

    public String getValue(ItemStack itemStack) {
        int depth = TagUtils.getDepthFromStack(itemStack);
        return String.valueOf(depth);
    }
}


