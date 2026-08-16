package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = "Fullbright", id = "fullbright", category = Category.RENDER)
public class FullBright extends Module {
    public static final FullBright instance = new FullBright();
}
