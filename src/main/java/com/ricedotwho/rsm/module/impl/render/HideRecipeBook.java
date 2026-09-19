package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;

@ModuleInfo(aliases = "Hide Recipe Book", id = "hide-recipe-book", category = Category.RENDER)
public class HideRecipeBook extends Module {
    @Getter
    private static final HideRecipeBook instance = new HideRecipeBook();
}
