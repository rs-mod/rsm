package com.ricedotwho.rsm.module.api;

import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Keybind;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PROTECTED)
public abstract class ModuleBase implements Accessor {
    protected boolean enabled;
    protected Keybind keybind;

    public abstract void toggle();

    public abstract boolean onKeyToggle();
}
