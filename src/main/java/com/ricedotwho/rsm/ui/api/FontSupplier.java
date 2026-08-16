package com.ricedotwho.rsm.ui.api;

import com.ricedotwho.rsm.render.render2d.Font;
import lombok.Getter;
import lombok.Setter;

public final class FontSupplier {

    public FontSupplier(Font font) {
        this.font = font;
    }

    @Getter
    @Setter
    private Font font;
}
