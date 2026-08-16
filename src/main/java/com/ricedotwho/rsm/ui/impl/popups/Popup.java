package com.ricedotwho.rsm.ui.impl.popups;

import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Widget;
import org.jetbrains.annotations.ApiStatus;

public class Popup extends Widget {
    public Popup(Node node) {
        super(node);
    }

    @ApiStatus.OverrideOnly
    public void onGuiClose() {}

    @Override
    protected void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        super.frame(parentX, parentY, mouseX, mouseY, scrollY);
    }
}
