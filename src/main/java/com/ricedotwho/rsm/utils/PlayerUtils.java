package com.ricedotwho.rsm.utils;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.mixins.accessor.AccessorKeyMapping;
import com.ricedotwho.rsm.type.Accessor;
import lombok.experimental.UtilityClass;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

@UtilityClass
public class PlayerUtils implements Accessor {

    public void leftClick() {
        press(mc.options.keyAttack);
    }

    public void rightClick() {
        press(mc.options.keyUse);
    }

    public void press(KeyMapping mapping) {
        InputConstants.Key key = ((AccessorKeyMapping) mapping).getKey();
        KeyMapping.set(key, true);
        KeyMapping.click(key);
        KeyMapping.set(key, false);
    }

    public void set(KeyMapping mapping, boolean state) {
        InputConstants.Key key = ((AccessorKeyMapping) mapping).getKey();
        KeyMapping.set(key, state);
    }

    public void playSound(SoundEvent event) {
        playSound(event, 1f, 1f);
    }

    public void playSound(SoundEvent event, float pitch, float volume) {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
    }
}
