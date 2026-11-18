package com.ricedotwho.rsm.module.impl.render.visualwords;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class VisualWord {
    private String phrase;
    private String replacement;
    private boolean enabled;

    public VisualWord() {
        this("", "", false);
    }

    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }
}
