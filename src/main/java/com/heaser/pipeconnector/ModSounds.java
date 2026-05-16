package com.heaser.pipeconnector;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, PipeConnector.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BEEP = SOUND_EVENTS.register("beep_emma",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(PipeConnector.MODID, "beep_emma")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BOOP = SOUND_EVENTS.register("boop_emma",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(PipeConnector.MODID, "boop_emma")));

    private static final ConcurrentHashMap<UUID, Boolean> nextBeep = new ConcurrentHashMap<>();

    private ModSounds() {}

    public static SoundEvent nextAlternating(UUID playerId) {
        boolean beep = nextBeep.getOrDefault(playerId, true);
        nextBeep.put(playerId, !beep);
        return (beep ? BEEP.get() : BOOP.get());
    }
}
