package com.ricedotwho.rsm.module;

import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;

@Getter
public abstract class ModuleBase implements Accessor {
    protected boolean enabled;
    protected Keybind keybind;

    public abstract void toggle();

    public abstract void onKeyToggle();
}
