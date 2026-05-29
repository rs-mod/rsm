package com.ricedotwho.rsm.utils;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.mixins.accessor.AccessorKeyMapping;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.experimental.UtilityClass;
import net.minecraft.client.KeyMapping;

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
}
