package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = "Animations", id = "animations", category = Category.RENDER)
public class Animations extends Module {
    @Getter
    private static final Animations instance = new Animations();

    private final BooleanSetting noEquip = new BooleanSetting("No Equip", false);
    private final BooleanSetting noSwing = new BooleanSetting("No Swing", false);
    private final BooleanSetting noHaste = new BooleanSetting("No Haste", false);
    private final NumberSetting<Integer> speed = new NumberSetting<>("Speed", 0, 32, 6, 1).isVisible(noHaste::getValue);

    private final BooleanSetting rescale = new BooleanSetting("Scaling", false);
    private final NumberSetting<Float> scale = new NumberSetting<>("Scale", 0.1F, 3F, 1F, 0.05F);
    private final NumberSetting<Float> x = new NumberSetting<>("X", -5F, 5F, 0F, 0.05F);
    private final NumberSetting<Float> y = new NumberSetting<>("Y", -5F, 5F, 0F, 0.05F);
    private final NumberSetting<Float> z = new NumberSetting<>("Z", -5F, 5F, 0F, 0.05F);

    private final NumberSetting<Float> yaw = new NumberSetting<>("Yaw", -180F, 180F, 0F, 0.05F);
    private final NumberSetting<Float> pitch = new NumberSetting<>("Pitch", -180F, 180F, 0F, 0.05F);
    private final NumberSetting<Float> roll = new NumberSetting<>("Roll", -180F, 180F, 0F, 0.05F);
}
