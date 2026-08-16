package com.ricedotwho.rsm.module.impl.movement;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;

@ModuleInfo(aliases = "Auto Sprint", id = "auto-sprint", category = Category.MOVEMENT)
public class AutoSprint extends Module {
    @Getter
    private static final AutoSprint instance = new AutoSprint();
}
