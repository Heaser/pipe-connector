package com.heaser.pipeconnector.compatibility;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CompatibilityDirectionGetter {
    private static CompatibilityDirectionGetter INSTANCE;
    private final HashMap<Class<? extends Item>, IDirectionGetter> classToGetterMap = new HashMap<>();
    private CompatibilityDirectionGetter() {
        if (isModLoaded("ae2")) {
            classToGetterMap.put(AE2Compatiblity.getItemStackClassToRegister(), new AE2Compatiblity());
        }
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static CompatibilityDirectionGetter getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CompatibilityDirectionGetter();
        }

        return INSTANCE;
    }

    public Direction defaultGetDirection(UseOnContext context) {
        return context.getClickedFace();
    }

    @Nullable
    public Direction getDirection(UseOnContext context) {
        IDirectionGetter getter = null;
        for (Map.Entry<Class<? extends Item>, IDirectionGetter> set : classToGetterMap.entrySet()) {
            Player player = context.getPlayer();
            if (player != null && set.getKey().isAssignableFrom(player.getOffhandItem().getItem().getClass())) {
                getter = set.getValue();
                break;
            }
        }
        if (getter != null) {
            return getter.getDirection(context);
        }
        else {
            return defaultGetDirection(context);
        }
    }
}
