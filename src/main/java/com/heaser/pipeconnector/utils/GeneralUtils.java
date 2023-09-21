package com.heaser.pipeconnector.utils;

import net.minecraft.world.level.Level;

public class GeneralUtils
{
    public static boolean isServerSide(Level level) {
        return !level.isClientSide();
    }
}
