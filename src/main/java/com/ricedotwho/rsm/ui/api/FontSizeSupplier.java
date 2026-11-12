package com.ricedotwho.rsm.ui.api;

import lombok.Getter;
import lombok.Setter;

public class FontSizeSupplier {

    public FontSizeSupplier(int fontSize) {
        this.fontSize = fontSize;
    }

    @Getter
    @Setter
    private int fontSize;
}
