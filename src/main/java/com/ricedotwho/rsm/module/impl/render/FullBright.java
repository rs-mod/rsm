package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.InfoSetting;
import lombok.Getter;

@ModuleInfo(aliases = "Fullbright", id = "fullbright", category = Category.RENDER)
public class FullBright extends Module {
    @Getter
    public static final FullBright instance = new FullBright();
    private final InfoSetting warning = new InfoSetting("This feature requires a restart to enable!", InfoSetting.Type.WARNING);
}
