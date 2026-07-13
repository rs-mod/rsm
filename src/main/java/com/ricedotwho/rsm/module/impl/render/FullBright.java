package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = "Fullbright", id = "fullbright", category = Category.RENDER)
public class FullBright extends Module {
    public static FullBright INSTANCE;

    public FullBright() {
        INSTANCE = this;
    }
}
