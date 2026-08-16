package com.ricedotwho.rsm.module.api;

import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Keybind;
import lombok.Getter;

@Getter
public abstract class ModuleBase implements Accessor {
    protected boolean enabled;
    protected Keybind keybind;

    public abstract void toggle();

    public abstract boolean onKeyToggle();
}
