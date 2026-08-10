package com.ricedotwho.rsm.module.impl.movement;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;

@ModuleInfo(aliases = "Auto Sprint", id = "auto-sprint", category = Category.MOVEMENT)
public class AutoSprint extends Module {
    @Getter
    @SuppressWarnings("unused")
    private static final AutoSprint instance = new AutoSprint();
}
