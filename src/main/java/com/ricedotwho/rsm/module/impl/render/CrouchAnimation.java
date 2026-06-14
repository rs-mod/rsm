package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = "Crouch Speed", id = "CrouchAnimation", category = Category.PLAYER)
public class CrouchAnimation extends Module {
    private static CrouchAnimation INSTANCE;
    private static final NumberSetting speed = new NumberSetting("Speed", 0.01, 1, 0.5, 0.01);

    public CrouchAnimation() {
        INSTANCE = this;
        this.registerProperty(
                speed
        );
    }

    public static Float getFactor() {
        if (!INSTANCE.isEnabled()) return null;
        return speed.getValue().floatValue();
    }
}
