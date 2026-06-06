package com.ricedotwho.rsm.ui.clickgui.impl.module.settings;

import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.TextInput;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class InputValueComponent<T extends Setting<?>> extends ValueComponent<T> {
    protected final List<TextInput> inputs = new ArrayList<>();

    public InputValueComponent(T setting, ModuleBase parent, TextInput input, TextInput ... extras) {
        super(setting, parent);
        this.inputs.add(input);
        this.inputs.addAll(Arrays.asList(extras));
    }

    public TextInput getInput() {
        return inputs.getFirst();
    }

    public boolean isWriting() {
        return this.inputs.stream().anyMatch(TextInput::isWriting);
    }

    @Nullable
    public TextInput getWriting() {
        return this.inputs.stream().filter(TextInput::isWriting).findFirst().orElse(null);
    }

    public void setAllNotWriting() {
        this.inputs.forEach(t -> t.setWriting(false));
    }
}
