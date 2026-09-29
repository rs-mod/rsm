package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = "Element Hider", id = "element-hider", category = Category.RENDER)
public class ElementHider extends Module {
    @Getter
    private static final ElementHider instance = new ElementHider();

    private final BooleanSetting hideArmour = new BooleanSetting("Hide Armour", false);
    private final BooleanSetting hideHunger = new BooleanSetting("Hide Hunger", false);
}
