package com.ricedotwho.rsm.ui.impl.popups;

import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Widget;

public class Popup extends Widget {
    public Popup(Node node) {
        super(node);
    }

    public void preFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {

    }

    @Override
    protected void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        super.frame(parentX, parentY, mouseX, mouseY, scrollY);
    }
}
