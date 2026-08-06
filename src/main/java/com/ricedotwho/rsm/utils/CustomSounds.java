package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.core.Init;
import lombok.experimental.UtilityClass;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

@UtilityClass
public class CustomSounds {
	public static final SoundEvent BELL = registerSound("bell");
	public static final SoundEvent WIN = registerSound("win");

	private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath("rsmpack", id);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
	}

	@Init
    public static void init() {

    }
}