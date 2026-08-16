package com.ricedotwho.rsm.ui.api;

import lombok.Getter;
import lombok.Setter;

public class FontSizeSupplier {

    public FontSizeSupplier(float fontSize) {
        this.fontSize = fontSize;
    }

    @Getter
    @Setter
    private float fontSize;
}
