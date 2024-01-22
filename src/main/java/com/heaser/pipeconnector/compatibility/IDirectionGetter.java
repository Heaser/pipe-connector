package com.heaser.pipeconnector.compatibility;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;

public interface IDirectionGetter {
    Direction getDirection(UseOnContext context);
}
