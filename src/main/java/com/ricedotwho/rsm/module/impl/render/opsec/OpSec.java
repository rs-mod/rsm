package com.ricedotwho.rsm.module.impl.render.opsec;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.GroupSetting;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = "OpSec", id = "OpSec", category = Category.RENDER)
public class OpSec extends Module {
    @Getter
    private static final OpSec instance = new OpSec();
    private final GroupSetting<NickHider> nickHider = new GroupSetting<>("Nick Hider", new NickHider(this));
    private final GroupSetting<ServerIdHider> serverIdHider = new GroupSetting<>("Server ID Hider", new ServerIdHider(this));

}
