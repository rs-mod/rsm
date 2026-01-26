package com.ricedotwho.rsm.module.impl;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;

@Getter // please don't use spaces in the id
@ModuleInfo(aliases = "Template", id = "template", category = Category.OTHER)
public class Template extends Module {

    public Template() {
        this.registerProperty(
                // todo: register settings
        );
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void reset() {

    }
}
