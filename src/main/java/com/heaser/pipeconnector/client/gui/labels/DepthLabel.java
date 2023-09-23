package com.heaser.pipeconnector.client.gui.labels;

import com.heaser.pipeconnector.utils.PipeConnectorUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DepthLabel extends BaseLabel {
    public DepthLabel() {
        super("item.pipe_connector.gui.label.depth");
    }

    public String getValue(ItemStack itemStack) {
        int depth = PipeConnectorUtils.getDepthFromStack(itemStack) - 1;
        return String.valueOf(depth);
    }
}

