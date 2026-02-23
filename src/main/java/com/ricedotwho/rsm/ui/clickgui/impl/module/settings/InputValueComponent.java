package com.ricedotwho.rsm.ui.clickgui.impl.module.settings;

import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.TextInput;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;

public abstract class InputValueComponent<T extends Setting<?>> extends ValueComponent<T> {
    public boolean writing = false;
    protected final TextInput input;

    public InputValueComponent(T setting, ModuleBase parent, TextInput input) {
        super(setting, parent);
        this.input = input;
    }
}
