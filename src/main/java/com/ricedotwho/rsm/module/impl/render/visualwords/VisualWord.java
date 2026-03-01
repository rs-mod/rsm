package com.ricedotwho.rsm.module.impl.render.visualwords;

import net.minecraft.network.chat.MutableComponent;

public class VisualWord {
    public MutableComponent replacement;
    public boolean enabled;

    public VisualWord(MutableComponent replacement, boolean enabled) {
        this.replacement = replacement;
        this.enabled = enabled;
    }

    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }
}
